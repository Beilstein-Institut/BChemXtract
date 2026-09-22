/*
 * Copyright (c) 2025-2030 Beilstein-Institut
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.beilstein.chemxtract.utils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cheminf.AbbreviationLayout;
import org.beilstein.chemxtract.cheminf.RingSystemNumbering;
import org.beilstein.chemxtract.lookups.SmilesAbbreviations;
import org.beilstein.chemxtract.visitor.CorrelatedGroup;
import org.beilstein.chemxtract.visitor.RGroupDefinitionBlock;
import org.beilstein.chemxtract.visitor.TextVisitor;
import org.openscience.cdk.Bond;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling Markush structures and replacing R-groups in molecules.
 *
 * <p>This class processes {@link IAtomContainer} instances containing pseudo-atoms (R-groups) and
 * generates all possible structures by replacing R-groups with their corresponding substituents
 * defined in the residue labels.
 *
 * <p>The replacement handles single-bonded residues as well as dual-bonded residues, reconnecting
 * the generated structures properly.
 *
 * <h2>Example usage:</h2>
 *
 * <pre>{@code
 * CDPage page = ...;
 * IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
 * IAtomContainer molecule = ...;
 *
 * MarkushHandler handler = new MarkushHandler(page, builder);
 * List<IAtomContainer> substitutedMolecules = handler.replaceRGroups(molecule);
 * }</pre>
 */
public class MarkushHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarkushHandler.class);

  /**
   * A substituent written as {@code <positions>-<group>}, e.g. {@code 3-OMe}, {@code p-Cl} or
   * {@code 5,7-Me2}: the group sits on each named ring position, counted from the ring's attachment
   * atom. A position is either a number or one of the ortho/meta/para prefixes, which chemists use
   * interchangeably with 2/3/4. Only tried after the plain abbreviation lookup fails, because many
   * abbreviations legitimately start the same way ({@code 2-py}, {@code 4-ClPh}, {@code p-Tol}) and
   * there the prefix belongs to the substituent's own name.
   */
  private static final Pattern POSITIONAL_SUBSTITUENT =
      Pattern.compile("(\\d{1,2}(?:,\\d{1,2})*|[omp])-(.+)");

  /**
   * The multiplier a multi-locant value carries on its group: the {@code 2} of {@code Me2}, or the
   * brackets and {@code 3} of {@code (OMe)3}. It states how often the group appears and is only
   * stripped when it agrees with the number of locants.
   */
  private static final Pattern GROUP_MULTIPLIER = Pattern.compile("\\(?(.+?)\\)?(\\d+)");

  private final Map<String, List<String>> residueLabels;
  private final Map<String, List<String>> structuralDefinitions = new LinkedHashMap<>();
  private final List<RGroupDefinitionBlock> blocks;
  private final SmilesParser smilesParser;

  /**
   * Label/value pairs already reported as unresolved. A value is retried for every scaffold and
   * every combination that mentions it, always with the same outcome, so without this the same
   * warning is emitted tens of thousands of times for one drawing.
   */
  private final Set<String> reportedUnresolved = new HashSet<>();

  /**
   * Constructs a MarkushHandler using a CDPage and a CDK builder.
   *
   * @param page CDPage containing the chemical diagram
   * @param builder CDK object builder for creating atom containers
   */
  public MarkushHandler(CDPage page, IChemObjectBuilder builder) {
    TextVisitor textVisitor = new TextVisitor(page);
    residueLabels = textVisitor.getRgroups();
    blocks = mergeColumnBlocks(textVisitor.getBlocks());
    smilesParser = new SmilesParser(builder);
  }

  /**
   * One independent choice in the enumeration: exactly one of {@code options} is taken. An option
   * assigns one label (an independent R-group) or several at once (a row of a correlated group).
   *
   * @param options the mutually exclusive partial assignments
   */
  private record Choice(List<Map<String, String>> options) {}

  /**
   * Merges definition blocks that form one legend column drawn as several stacked single-line text
   * nodes. ChemDraw authors often list a label's substituents one per line, each its own text
   * object; without merging, nearest-block scoping would pick a single line and enumerate only one
   * substituent per scaffold. Blocks are merged when they define the same label set, overlap
   * horizontally, and sit within about a line-height of one another vertically.
   *
   * <p>The same legend may also be laid out as two side-by-side columns, which {@link
   * #sideBySideLegend} folds together as well. Legends that sit far apart (one per scaffold) stay
   * separate, so nearest-block scoping keeps disambiguating those.
   *
   * @param blocks the per-text-node blocks from {@link TextVisitor}
   * @return the blocks with same-label runs of one legend merged into one block each
   */
  private static List<RGroupDefinitionBlock> mergeColumnBlocks(List<RGroupDefinitionBlock> blocks) {
    List<RGroupDefinitionBlock> result = new ArrayList<>();
    boolean[] merged = new boolean[blocks.size()];
    for (int i = 0; i < blocks.size(); i++) {
      if (merged[i]) {
        continue;
      }
      RGroupDefinitionBlock base = blocks.get(i);
      // Only column-merge positioned, single-legend (independent) blocks; correlated tables and
      // unpositioned blocks are left untouched.
      if (base.bounds() == null || base.definitions().isEmpty()) {
        result.add(base);
        merged[i] = true;
        continue;
      }
      List<RGroupDefinitionBlock> cluster = new ArrayList<>();
      cluster.add(base);
      merged[i] = true;
      boolean grew = true;
      while (grew) {
        grew = false;
        for (int j = i + 1; j < blocks.size(); j++) {
          if (merged[j]) {
            continue;
          }
          RGroupDefinitionBlock candidate = blocks.get(j);
          if (candidate.bounds() == null
              || candidate.definitions().isEmpty()
              || !candidate.definitions().keySet().equals(base.definitions().keySet())) {
            continue;
          }
          if (cluster.stream()
              .anyMatch(
                  member ->
                      sameColumnAdjacent(member, candidate)
                          || sideBySideLegend(member, candidate))) {
            cluster.add(candidate);
            merged[j] = true;
            grew = true;
          }
        }
      }
      result.add(cluster.size() == 1 ? base : mergeCluster(cluster));
    }
    return result;
  }

  /**
   * Whether two blocks are stacked lines of the same column: horizontally overlapping and within
   * roughly one line-height vertically.
   */
  private static boolean sameColumnAdjacent(RGroupDefinitionBlock a, RGroupDefinitionBlock b) {
    CDRectangle ra = a.bounds();
    CDRectangle rb = b.bounds();
    double horizontalOverlap =
        Math.min(ra.getRight(), rb.getRight()) - Math.max(ra.getLeft(), rb.getLeft());
    if (horizontalOverlap <= 0) {
      return false; // different columns
    }
    double verticalGap =
        Math.max(0, Math.max(ra.getTop() - rb.getBottom(), rb.getTop() - ra.getBottom()));
    double lineHeight = Math.max(ra.getBottom() - ra.getTop(), rb.getBottom() - rb.getTop());
    return verticalGap <= lineHeight * 1.5;
  }

  /**
   * Whether two blocks are the two columns of one legend: no horizontal overlap, spanning
   * essentially the same rows, and separated by a gutter narrow relative to the columns themselves.
   * A substrate scope listing 11 substituents as two adjacent columns is one legend for the
   * scaffold, not two competing definitions of the same label.
   *
   * <p>Correlated tables are excluded: their row-tuples would have to be paired across the columns
   * rather than unioned, which this merge does not do.
   */
  private static boolean sideBySideLegend(RGroupDefinitionBlock a, RGroupDefinitionBlock b) {
    if (!a.correlatedGroups().isEmpty() || !b.correlatedGroups().isEmpty()) {
      return false;
    }
    CDRectangle ra = a.bounds();
    CDRectangle rb = b.bounds();
    double horizontalGap =
        Math.max(0, Math.max(ra.getLeft() - rb.getRight(), rb.getLeft() - ra.getRight()));
    if (horizontalGap <= 0) {
      return false; // overlapping columns are the stacked-lines case above
    }
    double verticalOverlap =
        Math.min(ra.getBottom(), rb.getBottom()) - Math.max(ra.getTop(), rb.getTop());
    double minHeight = Math.min(ra.getBottom() - ra.getTop(), rb.getBottom() - rb.getTop());
    double minWidth = Math.min(ra.getRight() - ra.getLeft(), rb.getRight() - rb.getLeft());
    // ponytail: geometric heuristic — a gutter under half a column wide plus near-total row
    // overlap.
    // Two scaffolds whose legends happen to sit this close would merge wrongly; the fix then is
    // scaffold-aware association, not a tighter threshold.
    return verticalOverlap >= minHeight * 0.6 && horizontalGap <= minWidth * 0.5;
  }

  /**
   * Combines a cluster of same-column blocks into one, unioning per-label values (order-preserving,
   * de-duplicated) and taking the bounding box of the sources.
   */
  private static RGroupDefinitionBlock mergeCluster(List<RGroupDefinitionBlock> cluster) {
    Map<String, List<String>> definitions = new LinkedHashMap<>();
    List<CorrelatedGroup> correlated = new ArrayList<>();
    float top = Float.MAX_VALUE;
    float left = Float.MAX_VALUE;
    float bottom = -Float.MAX_VALUE;
    float right = -Float.MAX_VALUE;
    for (RGroupDefinitionBlock block : cluster) {
      CDRectangle b = block.bounds();
      top = Math.min(top, b.getTop());
      left = Math.min(left, b.getLeft());
      bottom = Math.max(bottom, b.getBottom());
      right = Math.max(right, b.getRight());
      block
          .definitions()
          .forEach(
              (label, values) -> {
                List<String> list = definitions.computeIfAbsent(label, _ -> new ArrayList<>());
                for (String value : values) {
                  if (!list.contains(value)) {
                    list.add(value);
                  }
                }
              });
      correlated.addAll(block.correlatedGroups());
    }
    CDRectangle bounds = new CDRectangle();
    bounds.setTop(top);
    bounds.setLeft(left);
    bounds.setBottom(bottom);
    bounds.setRight(right);
    return new RGroupDefinitionBlock(bounds, definitions, correlated);
  }

  /**
   * Generates all possible {@link IAtomContainer} structures by replacing R-groups in the given
   * atom container with their substituents, using the page-wide union of definitions.
   *
   * @param atomContainer molecule containing pseudo-atoms (R-groups)
   * @return list of all substituted atom containers
   * @throws CloneNotSupportedException if atom container cloning fails
   * @throws IOException if reading SMILES definitions fails
   * @throws InvalidSmilesException if a SMILES string is invalid
   */
  public List<IAtomContainer> replaceRGroups(IAtomContainer atomContainer)
      throws CloneNotSupportedException, IOException, CDKException {
    return replaceRGroups(List.of(atomContainer), residueLabels, new HashSet<>());
  }

  /**
   * Generates all structures for a scaffold, resolving its R-groups from the definition block
   * nearest to it, without sharing produced structures with any other scaffold.
   *
   * @param atomContainer molecule containing pseudo-atoms (R-groups)
   * @param scaffoldBounds bounding box of the scaffold, used to pick the nearest definition block
   * @return list of all substituted atom containers
   * @throws CloneNotSupportedException if atom container cloning fails
   * @throws IOException if reading SMILES definitions fails
   * @throws InvalidSmilesException if a SMILES string is invalid
   */
  public List<IAtomContainer> replaceRGroups(
      IAtomContainer atomContainer, CDRectangle scaffoldBounds)
      throws CloneNotSupportedException, IOException, CDKException {
    return replaceRGroups(atomContainer, scaffoldBounds, new HashSet<>());
  }

  /**
   * Generates all possible structures for a scaffold, resolving its R-groups from the definition
   * block nearest to the scaffold. This prevents definitions of one scaffold from leaking into
   * another when several scaffolds on the same page reuse the same R-group labels.
   *
   * @param atomContainer molecule containing pseudo-atoms (R-groups)
   * @param scaffoldBounds bounding box of the scaffold, used to pick the nearest definition block
   * @param produced keys of the structures already produced for this fragment, added to as more are
   *     produced; a scaffold drawn with a position-variation attachment is expanded once per
   *     candidate atom, and a legend giving substituents by ring position makes those expansions
   *     yield the same structures, which this lets the later ones skip before they are laid out
   * @return list of all substituted atom containers not already in {@code produced}
   * @throws CloneNotSupportedException if atom container cloning fails
   * @throws IOException if reading SMILES definitions fails
   * @throws InvalidSmilesException if a SMILES string is invalid
   */
  public List<IAtomContainer> replaceRGroups(
      IAtomContainer atomContainer, CDRectangle scaffoldBounds, Set<String> produced)
      throws CloneNotSupportedException, IOException, CDKException {
    return replaceRGroups(List.of(atomContainer), scaffoldBounds, produced);
  }

  /**
   * Generates all structures for a scaffold that a position-variation attachment drew as several
   * candidates, resolving its R-groups from the definition block nearest to it.
   *
   * <p>The candidates are the same scaffold with one residue bonded to a different atom each. They
   * are enumerated together rather than one after another: the assignments are enumerated once, and
   * an assignment is built only once per attachment its substituents actually reach. A value that
   * names a ring position moves the residue onto the atom it names, and a value that is a lone
   * hydrogen leaves the atom it replaces as it was, so neither can tell the candidates apart — for
   * an assignment whose every value is of that kind, one structure is built instead of one per
   * candidate.
   *
   * @param candidates the scaffold as drawn, one per candidate attachment atom; a scaffold without
   *     a position-variation attachment is a single-element list
   * @param scaffoldBounds bounding box of the scaffold, used to pick the nearest definition block
   * @param produced keys of the structures already produced for this fragment, added to as more are
   *     produced
   * @return list of all substituted atom containers not already in {@code produced}
   * @throws CloneNotSupportedException if atom container cloning fails
   * @throws IOException if reading SMILES definitions fails
   * @throws InvalidSmilesException if a SMILES string is invalid
   */
  public List<IAtomContainer> replaceRGroups(
      List<IAtomContainer> candidates, CDRectangle scaffoldBounds, Set<String> produced)
      throws CloneNotSupportedException, IOException, CDKException {
    if (candidates.isEmpty()) {
      return List.of();
    }
    Set<String> present = presentResidueLabels(candidates.getFirst());
    List<Map<String, String>> combinations =
        viableCombinations(candidates, residueChoicesNear(present, scaffoldBounds));
    if (combinations.isEmpty()) {
      // No scoped definitions apply to this scaffold, or none of them resolved; defer to the
      // page-wide union.
      return replaceRGroups(candidates, residueLabels, produced);
    }
    List<IAtomContainer> scoped = applyCombinations(candidates, combinations, produced);
    // Scoping is a refinement: if it narrowed the definitions down to something that produced no
    // structures, fall back to the page-wide union so scoping never does worse than no scoping.
    if (scoped.isEmpty()) {
      return replaceRGroups(candidates, residueLabels, produced);
    }
    return scoped;
  }

  /** Labels of the pseudo-atoms present in the container (candidate R-groups to resolve). */
  private Set<String> presentResidueLabels(IAtomContainer atomContainer) {
    Set<String> present = new HashSet<>();
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom pseudo && pseudo.getLabel() != null) {
        present.add(pseudo.getLabel());
      }
    }
    return present;
  }

  /**
   * Builds the choices to enumerate a scaffold over, honouring correlated (positional-table)
   * groups: their labels vary together as fixed row-tuples, and so form one choice, while every
   * remaining label is a choice of its own. Definitions are scoped to the blocks nearest the
   * scaffold.
   *
   * @param present labels of the pseudo-atoms in the scaffold
   * @param scaffoldBounds bounding box of the scaffold, for nearest-block scoping
   * @return one choice per correlated group and per independent label; empty if no definition
   *     applies
   */
  private List<Choice> residueChoicesNear(Set<String> present, CDRectangle scaffoldBounds) {
    List<Choice> choices = new ArrayList<>();

    // Correlated groups first: for each distinct label set, pick the nearest block's group; its
    // explicit row-tuples are the options, so correlated labels vary together rather than freely.
    Set<String> claimed = new HashSet<>();
    for (CorrelatedGroup group : nearestCorrelatedGroups(present, scaffoldBounds)) {
      choices.add(new Choice(List.copyOf(group.tuples())));
      claimed.addAll(group.labels());
    }

    // Independent labels: one choice each, scoped per-label to the nearest defining block.
    Map<String, List<String>> scopedIndependent = residueLabelsNear(scaffoldBounds);
    for (String label : present) {
      if (claimed.contains(label)) {
        continue;
      }
      List<String> values = scopedIndependent.get(label);
      if (values == null || values.isEmpty()) {
        continue;
      }
      List<Map<String, String>> options = new ArrayList<>(values.size());
      for (String value : values) {
        options.add(Map.of(label, value));
      }
      choices.add(new Choice(options));
    }

    return choices;
  }

  /**
   * Enumerates the assignments that actually graft, pruning the rest as it goes.
   *
   * <p>The assignments are the cartesian product of the choices, which grows multiplicatively: six
   * labels of six or seven substituents each make close to fifty thousand assignments for one
   * scaffold, and a drawing that also varies the attachment position repeats that per variant.
   * Nearly all of them are dropped again, because one of their values does not resolve.
   *
   * <p>Whether a value grafts depends only on the scaffold and the values already applied, never on
   * the choices still to be made, so a value that fails rules out every assignment extending it.
   * Walking the product depth-first and abandoning a prefix as soon as one of its values fails
   * therefore discards those subtrees whole, rather than building and re-failing each assignment in
   * them individually.
   *
   * @param scaffold the structure the assignments apply to; left unmodified
   * @param choices the independent choices to enumerate over
   * @return the assignments whose every value grafted, in cartesian order
   */
  private List<Map<String, String>> viableCombinations(
      List<IAtomContainer> candidates, List<Choice> choices)
      throws CDKException, CloneNotSupportedException, IOException {
    if (choices.isEmpty()) {
      return List.of();
    }
    // A value can graft on one candidate attachment and not on another — a ring position counted
    // from where the residue is drawn need not resolve from every candidate — so the assignments
    // viable on any of them are enumerated, not only those the first one reaches.
    Set<Map<String, String>> viable = new LinkedHashSet<>();
    for (IAtomContainer candidate : candidates) {
      List<Map<String, String>> reached = new ArrayList<>();
      extendCombination(candidate, choices, 0, new LinkedHashMap<>(), reached);
      viable.addAll(reached);
    }
    return new ArrayList<>(viable);
  }

  /**
   * Extends a partial assignment by one choice, recursing into the options that graft.
   *
   * @param container the scaffold with the assignments so far applied
   * @param choices the choices being enumerated
   * @param index the choice to take next
   * @param assigned the assignment built so far, mutated during the walk
   * @param viable collects the complete assignments that grafted
   */
  private void extendCombination(
      IAtomContainer container,
      List<Choice> choices,
      int index,
      Map<String, String> assigned,
      List<Map<String, String>> viable)
      throws CDKException, CloneNotSupportedException, IOException {
    if (index == choices.size()) {
      viable.add(new LinkedHashMap<>(assigned));
      return;
    }
    for (Map<String, String> option : choices.get(index).options()) {
      IAtomContainer candidate = container.clone();
      if (!applyOption(candidate, option)) {
        continue;
      }
      assigned.putAll(option);
      extendCombination(candidate, choices, index + 1, assigned, viable);
      option.keySet().forEach(assigned::remove);
    }
  }

  /**
   * Applies every label of one option, stopping at the first that does not resolve.
   *
   * @param container the structure to modify
   * @param option the partial assignment to apply
   * @return {@code true} if every label grafted
   */
  private boolean applyOption(IAtomContainer container, Map<String, String> option)
      throws CDKException, CloneNotSupportedException, IOException {
    for (Map.Entry<String, String> entry : option.entrySet()) {
      if (!applyEntry(container, entry.getKey(), entry.getValue())) {
        reportUnresolved(entry.getKey(), entry.getValue());
        return false;
      }
    }
    return true;
  }

  /**
   * Grafts one label's substituent, by plain SMILES if the value resolves to one and by positional
   * notation otherwise.
   *
   * @param container the structure to modify
   * @param label the R-group label to substitute
   * @param value the raw legend value
   * @return {@code true} if the substituent was grafted; {@code false} if the value could not be
   *     resolved at all, in which case the container is left unchanged
   */
  private boolean applyEntry(IAtomContainer container, String label, String value)
      throws CDKException, CloneNotSupportedException, IOException {
    String smiles = resolveSmiles(value);
    if (ChemicalUtils.isValidSmiles(smiles)) {
      replaceRGroup(container, label, smiles);
      return true;
    }
    return replacePositionalRGroup(container, label, value);
  }

  /** Warns about a label/value that does not resolve, once per drawing. */
  private void reportUnresolved(String label, String value) {
    if (reportedUnresolved.add(label + "=" + value)) {
      LOGGER.warn(
          "Unresolved R-group label {}=\"{}\"; dropping the substituent combinations using it.",
          label,
          value);
    }
  }

  /**
   * Selects the correlated groups that apply to the scaffold: those whose labels are all present,
   * choosing, per distinct label set, the group whose source block is nearest to the scaffold.
   *
   * @param present labels present in the scaffold
   * @param scaffoldBounds bounding box of the scaffold
   * @return the chosen correlated groups
   */
  private List<CorrelatedGroup> nearestCorrelatedGroups(
      Set<String> present, CDRectangle scaffoldBounds) {
    Map<List<String>, CorrelatedGroup> chosen = new LinkedHashMap<>();
    Map<List<String>, Double> bestDistance = new HashMap<>();
    for (RGroupDefinitionBlock block : blocks) {
      for (CorrelatedGroup group : block.correlatedGroups()) {
        if (!present.containsAll(group.labels())) {
          continue;
        }
        double distance = blockDistance(scaffoldBounds, block.bounds());
        Double current = bestDistance.get(group.labels());
        if (current == null || distance < current) {
          bestDistance.put(group.labels(), distance);
          chosen.put(group.labels(), group);
        }
      }
    }
    return new ArrayList<>(chosen.values());
  }

  /** Distance from a scaffold to a block, treating a block with no bounds as maximally far. */
  private static double blockDistance(CDRectangle scaffoldBounds, CDRectangle blockBounds) {
    if (scaffoldBounds == null || blockBounds == null) {
      return Double.MAX_VALUE;
    }
    return rectangleDistance(scaffoldBounds, blockBounds);
  }

  private List<IAtomContainer> replaceRGroups(
      List<IAtomContainer> candidates, Map<String, List<String>> definitions, Set<String> produced)
      throws CloneNotSupportedException, IOException, CDKException {

    Map<String, List<String>> relevantRGroups =
        filterRelevantRGroups(candidates.getFirst(), definitions);

    if (relevantRGroups.isEmpty()) {
      return List.copyOf(candidates);
    }
    return applyCombinations(
        candidates, viableCombinations(candidates, independentChoices(relevantRGroups)), produced);
  }

  /**
   * Applies each label-to-substituent combination to a fresh clone of the container, laying out the
   * grafted atoms. Clones for which no substituent could be applied are dropped.
   *
   * @param atomContainer the scaffold to substitute
   * @param combinations the assignments to apply, one resulting structure each
   * @return the substituted structures
   */
  private List<IAtomContainer> applyCombinations(
      List<IAtomContainer> candidates, List<Map<String, String>> combinations, Set<String> produced)
      throws CloneNotSupportedException, IOException, CDKException {
    List<IAtomContainer> results = new ArrayList<>(combinations.size());
    AttachmentIndex attachments = new AttachmentIndex(candidates);

    for (Map<String, String> combination : combinations) {
      // Candidates an assignment cannot tell apart would each build the same structure; one of
      // them stands for the group. The others are only reached when it cannot be substituted.
      for (List<IAtomContainer> group : attachments.groupsFor(combination)) {
        Substitution substitution = null;
        Iterator<IAtomContainer> candidate = group.iterator();
        while (substitution == null && candidate.hasNext()) {
          substitution = substitute(candidate.next(), combination);
        }
        // A structure an earlier assignment or fragment variant already produced is dropped before
        // it is laid out: building and laying out each copy only to merge them again at the end of
        // extraction is the bulk of the work here.
        if (substitution != null
            && produced.add(ChemicalUtils.structureKey(substitution.structure()))) {
          layoutGraftedAtoms(substitution.structure(), substitution.scaffoldAtoms());
          results.add(substitution.structure());
        }
      }
    }
    return results;
  }

  /**
   * A substituted structure and the atoms it had before substitution, which are the ones that keep
   * their drawn coordinates when the grafted atoms are laid out.
   */
  private record Substitution(IAtomContainer structure, Set<IAtom> scaffoldAtoms) {}

  /**
   * Applies one assignment to a clone of the candidate.
   *
   * @param candidate the scaffold to substitute; left unmodified
   * @param combination the assignment to apply
   * @return the substituted structure, or {@code null} if one of the labels did not resolve
   */
  private Substitution substitute(IAtomContainer candidate, Map<String, String> combination)
      throws CloneNotSupportedException, IOException, CDKException {
    IAtomContainer clone = candidate.clone();
    // Snapshot the pre-substitution atoms so the grafted (coordinate-less) atoms can be
    // distinguished from the retained scaffold afterwards.
    Set<IAtom> scaffoldAtoms = Collections.newSetFromMap(new IdentityHashMap<>());
    clone.atoms().forEach(scaffoldAtoms::add);
    boolean substituted = false;

    for (Map.Entry<String, String> entry : combination.entrySet()) {
      if (applyEntry(clone, entry.getKey(), entry.getValue())) {
        substituted = true;
      } else {
        // An unresolvable label (unknown abbreviation, cross-referenced R-group, ...) would
        // leave a dangling pseudo-atom. A structure with an unresolved R-group is never emitted,
        // so the whole combination is dropped rather than substituting only some of its labels.
        // Enumeration already pruned these, so this is a guard rather than the usual path.
        reportUnresolved(entry.getKey(), entry.getValue());
        return null;
      }
    }
    return substituted ? new Substitution(clone, scaffoldAtoms) : null;
  }

  /**
   * Gives 2D coordinates to the atoms grafted in during substitution (parsed from SMILES, they
   * carry none) via partial layout, keeping the original scaffold coordinates fixed. Best-effort:
   * on failure the structure is kept with whatever coordinates it had.
   *
   * @param container the substituted structure
   * @param scaffoldAtoms the atoms that existed before substitution (everything else is grafted)
   */
  private void layoutGraftedAtoms(IAtomContainer container, Set<IAtom> scaffoldAtoms) {
    Set<IAtom> graftedAtoms = Collections.newSetFromMap(new IdentityHashMap<>());
    for (IAtom atom : container.atoms()) {
      if (!scaffoldAtoms.contains(atom)) {
        graftedAtoms.add(atom);
      }
    }
    if (graftedAtoms.isEmpty()) {
      return;
    }
    try {
      AbbreviationLayout.layoutExpandedAbbreviations(container, graftedAtoms);
    } catch (CDKException | RuntimeException e) {
      LOGGER.warn("R-group layout failed; keeping partial coordinates.", e);
    }
  }

  /**
   * Resolves a substituent definition to a SMILES string, looking up abbreviations if the
   * definition matches a known alias, and bracketing bare element symbols that fall outside the
   * SMILES organic subset (e.g. {@code Se}, {@code Te}) so they parse.
   *
   * @param definition a SMILES string or a known abbreviation
   * @return the resolved SMILES string
   */
  private String resolveSmiles(String definition) throws IOException {
    if (SmilesAbbreviations.contains(definition)) {
      return SmilesAbbreviations.get(definition);
    }
    // A bare element symbol outside the SMILES organic subset (Se, Te, Si, ...) is not valid SMILES
    // on its own; wrap it in brackets so it parses as that atom.
    if (!ChemicalUtils.isValidSmiles(definition)
        && Elements.ofString(definition) != Elements.Unknown) {
      return "[" + definition + "]";
    }
    return definition;
  }

  /**
   * Applies a substituent given in positional notation, {@code <position>-<group>} (e.g. {@code
   * 3-OMe}, {@code 4-Br}). The position counts round the ring from its attachment (ipso) atom, so
   * the substituent belongs on that ring atom regardless of where the R-group itself was drawn —
   * ChemDraw authors routinely draw one R (often as a position-variation attachment) and let the
   * legend state the position. The residue is therefore moved onto the named ring atom and then
   * substituted through the ordinary replacement path.
   *
   * @param container the structure to modify
   * @param label the R-group label to substitute
   * @param value the raw legend value
   * @return {@code true} if the value was positional notation and could be applied; {@code false}
   *     if it is not positional notation, its group does not resolve, or the ring position cannot
   *     be determined — in all of which cases the container is left untouched
   */
  private boolean replacePositionalRGroup(IAtomContainer container, String label, String value)
      throws CDKException, CloneNotSupportedException, IOException {
    PositionalValue positional = parsePositional(value);
    if (positional == null) {
      return false;
    }
    if (!placeResidues(container, label, positional.positions())) {
      return false;
    }
    replaceRGroup(container, label, positional.smiles());
    return true;
  }

  /** A value written in positional notation: the ring positions it names, and its group. */
  private record PositionalValue(List<Integer> positions, String smiles) {}

  /**
   * Which of a scaffold's candidate attachments an assignment can tell apart.
   *
   * <p>A position-variation attachment is drawn once and expanded into one scaffold per candidate
   * atom. Substituting them all is only warranted for values that stay where the residue is drawn:
   * a value naming a ring position moves the residue onto the atom it names, and a value that is a
   * lone hydrogen gives the atom it replaces back the hydrogen it had, so neither reaches the drawn
   * position at all. Candidates an assignment leaves indistinguishable are grouped, and the group
   * is substituted once.
   *
   * <p>Where each label ends up depends only on the candidate and the one value, never on the rest
   * of the assignment, so it is resolved once per candidate and value and read back from there.
   */
  private final class AttachmentIndex {

    private final List<IAtomContainer> candidates;
    private final List<IRingSet> rings;
    private final List<Map<IAtom, Integer>> atomIndices;
    private final List<Map<String, String>> attachments;
    private final List<String> skeletons;
    private final Map<String, Boolean> loneHydrogen = new HashMap<>();

    AttachmentIndex(List<IAtomContainer> candidates) {
      this.candidates = candidates;
      this.rings = new ArrayList<>(Collections.nCopies(candidates.size(), null));
      this.atomIndices = new ArrayList<>(Collections.nCopies(candidates.size(), null));
      this.attachments = new ArrayList<>();
      this.skeletons = new ArrayList<>();
      // A scaffold drawn without a position variation has nothing to tell apart.
      for (int i = 0; i < candidates.size() && candidates.size() > 1; i++) {
        this.attachments.add(new HashMap<>());
        this.skeletons.add(skeleton(candidates.get(i)));
      }
    }

    /**
     * Groups the candidates by where the assignment's substituents land on them.
     *
     * @param combination the assignment about to be applied
     * @return the candidate groups, each to be substituted once
     */
    List<List<IAtomContainer>> groupsFor(Map<String, String> combination) throws IOException {
      if (candidates.size() < 2) {
        return List.of(candidates);
      }
      Map<String, List<IAtomContainer>> byAttachment = new LinkedHashMap<>();
      for (int i = 0; i < candidates.size(); i++) {
        byAttachment
            .computeIfAbsent(signature(i, combination), _ -> new ArrayList<>())
            .add(candidates.get(i));
      }
      return new ArrayList<>(byAttachment.values());
    }

    /** Where every substituent of the assignment lands on one candidate. */
    private String signature(int candidate, Map<String, String> combination) throws IOException {
      // A variable attachment can move a drawn group rather than a residue, and then the candidates
      // are different structures before any substitution. Their skeletons open the signature so
      // that only candidates which really are the same scaffold can be grouped.
      StringBuilder key = new StringBuilder(skeletons.get(candidate)).append('|');
      for (Map.Entry<String, String> entry : new TreeMap<>(combination).entrySet()) {
        key.append(entry.getKey()).append('=').append(attachment(candidate, entry)).append(';');
      }
      return key.toString();
    }

    /** Where one label's substituent lands on one candidate, resolved once and cached. */
    private String attachment(int candidate, Map.Entry<String, String> entry) throws IOException {
      Map<String, String> cache = attachments.get(candidate);
      String cached = cache.get(entry.getKey() + '=' + entry.getValue());
      if (cached != null) {
        return cached;
      }
      String resolved = resolveAttachment(candidate, entry.getKey(), entry.getValue());
      cache.put(entry.getKey() + '=' + entry.getValue(), resolved);
      return resolved;
    }

    private String resolveAttachment(int candidate, String label, String value) throws IOException {
      if (graftsLoneHydrogen(value)) {
        // The residue is replaced by the hydrogen the atom already carried, wherever it was drawn.
        return "*";
      }
      IAtomContainer container = candidates.get(candidate);
      PositionalValue positional = parsePositional(value);
      List<Integer> landing = new ArrayList<>();
      for (IAtom residue : residues(container, label)) {
        List<IAtom> targets =
            positional == null
                ? null
                : residueTargets(container, rings(candidate), residue, positional.positions());
        if (targets == null) {
          // Not positional notation, or a position this candidate cannot resolve: the substituent
          // stays on the atom the residue is drawn on, which is what the candidates differ in.
          landing.add(index(candidate, drawnOn(residue)));
        } else {
          targets.forEach(target -> landing.add(index(candidate, target)));
        }
      }
      Collections.sort(landing);
      return landing.toString();
    }

    /** Whether the value's substituent is a single hydrogen. */
    private boolean graftsLoneHydrogen(String value) throws IOException {
      Boolean known = loneHydrogen.get(value);
      if (known != null) {
        return known;
      }
      boolean lone = false;
      String smiles = resolveSmiles(value);
      if (ChemicalUtils.isValidSmiles(smiles)) {
        try {
          IAtomContainer substituent = smilesParser.parseSmiles(smiles);
          lone =
              substituent.getAtomCount() == 1
                  && "H".equals(substituent.getAtom(0).getSymbol())
                  && substituent.getAtom(0).getImplicitHydrogenCount() == 0;
        } catch (CDKException e) {
          LOGGER.debug("Substituent {} does not parse; treated as drawn in place.", smiles);
        }
      }
      loneHydrogen.put(value, lone);
      return lone;
    }

    /**
     * The candidate's structure without its residues: what the candidates share when they differ
     * only in where a residue is drawn.
     */
    private String skeleton(IAtomContainer container) {
      StringBuilder key = new StringBuilder(container.getAtomCount() * 8);
      for (IAtom atom : container.atoms()) {
        if (atom instanceof IPseudoAtom) {
          continue;
        }
        // Hydrogen counts are left out: the atom a residue is drawn on carries one less, which is
        // the very difference between candidates that the substitution then settles.
        key.append(atom.getSymbol()).append(':').append(atom.getFormalCharge()).append(',');
      }
      List<String> bonds = new ArrayList<>(container.getBondCount());
      for (IBond bond : container.bonds()) {
        if (bond.getBegin() instanceof IPseudoAtom || bond.getEnd() instanceof IPseudoAtom) {
          continue;
        }
        int begin = container.indexOf(bond.getBegin());
        int end = container.indexOf(bond.getEnd());
        bonds.add(Math.min(begin, end) + "-" + Math.max(begin, end) + ':' + bond.getOrder());
      }
      Collections.sort(bonds);
      return key.append('|').append(bonds).toString();
    }

    /** The atom a residue is drawn on, or the residue itself when it carries no bond. */
    private IAtom drawnOn(IAtom residue) {
      Iterator<IBond> bonds = residue.bonds().iterator();
      return bonds.hasNext() ? bonds.next().getOther(residue) : residue;
    }

    private IRingSet rings(int candidate) {
      IRingSet known = rings.get(candidate);
      if (known == null) {
        known = Cycles.mcb(candidates.get(candidate)).toRingSet();
        rings.set(candidate, known);
      }
      return known;
    }

    private int index(int candidate, IAtom atom) {
      Map<IAtom, Integer> known = atomIndices.get(candidate);
      if (known == null) {
        known = new IdentityHashMap<>();
        IAtomContainer container = candidates.get(candidate);
        for (int i = 0; i < container.getAtomCount(); i++) {
          known.put(container.getAtom(i), i);
        }
        atomIndices.set(candidate, known);
      }
      return known.getOrDefault(atom, -1);
    }
  }

  /**
   * Reads a value written in positional notation, {@code <position>-<group>}.
   *
   * @param value the raw legend value
   * @return the positions it names and the SMILES of its group, or {@code null} if the value is not
   *     positional notation or its group does not resolve
   * @throws IOException if reading SMILES definitions fails
   */
  private PositionalValue parsePositional(String value) throws IOException {
    Matcher matcher = POSITIONAL_SUBSTITUENT.matcher(value);
    if (!matcher.matches()) {
      return null;
    }
    List<Integer> positions = new ArrayList<>();
    for (String token : matcher.group(1).split(",")) {
      positions.add(ringPosition(token));
    }
    String smiles = resolveSmiles(stripMultiplier(matcher.group(2), positions.size()));
    return ChemicalUtils.isValidSmiles(smiles) ? new PositionalValue(positions, smiles) : null;
  }

  /**
   * The group of a multi-locant value, without the multiplier that states how often it appears:
   * {@code Me2} on two locants is two methyls, {@code (OMe)3} on three is three methoxys. A
   * multiplier that disagrees with the number of locants is not one — the digit belongs to the
   * group's own name — and the group is left as written.
   *
   * @param group the group part of a positional value
   * @param locants how many positions the value names
   * @return the group without its multiplier
   */
  private static String stripMultiplier(String group, int locants) {
    if (locants < 2) {
      return group;
    }
    Matcher matcher = GROUP_MULTIPLIER.matcher(group);
    if (matcher.matches()) {
      try {
        if (Integer.parseInt(matcher.group(2)) == locants) {
          return matcher.group(1);
        }
      } catch (NumberFormatException e) {
        LOGGER.debug("Multiplier of {} is not a number, leaving the group as written.", group);
      }
    }
    return group;
  }

  /**
   * Puts a residue on each position the value names. The drawn residue moves to the first; every
   * further position gets a residue of its own, carrying the same label, so that the single
   * replacement that follows grafts a copy of the group onto each of them.
   *
   * @param container the structure to modify
   * @param label the R-group label
   * @param positions the 1-based ring positions the value names, in the order written
   * @return {@code true} if every residue now sits on the named positions
   */
  private static boolean placeResidues(
      IAtomContainer container, String label, List<Integer> positions) {
    List<IAtom> residues = residues(container, label);
    if (residues.isEmpty()) {
      return false;
    }
    IRingSet rings = Cycles.mcb(container).toRingSet();
    for (IAtom residue : residues) {
      if (!placeResidue(container, rings, residue, label, positions)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Places one residue on the named positions.
   *
   * <p>The further positions are resolved before the residue moves, and their residues attached
   * afterwards: the ring numbering is read from the structure as drawn, and a residue added to it
   * would be another attachment to count from.
   *
   * @param container the structure to modify
   * @param rings the ring set of the container
   * @param residue the drawn residue
   * @param label the R-group label
   * @param positions the 1-based ring positions the value names, in the order written
   * @return {@code true} if the residue and its copies sit on the named positions
   */
  private static boolean placeResidue(
      IAtomContainer container,
      IRingSet rings,
      IAtom residue,
      String label,
      List<Integer> positions) {
    List<IAtom> targets = residueTargets(container, rings, residue, positions);
    if (targets == null) {
      return false;
    }
    if (!moveResidueToAtom(container, residue, targets.getFirst())) {
      return false;
    }
    for (IAtom target : targets.subList(1, targets.size())) {
      attachResidue(container, target, label);
    }
    return true;
  }

  /** The residues of one label, in container order. */
  private static List<IAtom> residues(IAtomContainer container, String label) {
    List<IAtom> residues = new ArrayList<>();
    for (IAtom atom : container.atoms()) {
      if (atom instanceof IPseudoAtom pseudo && label.equals(pseudo.getLabel())) {
        residues.add(atom);
      }
    }
    return residues;
  }

  /**
   * The atoms a positional value's positions name for one residue, counted from where the residue
   * is drawn.
   *
   * @param container the structure the residue belongs to
   * @param rings the container's ring set
   * @param residue the drawn residue
   * @param positions the 1-based ring positions the value names, in the order written
   * @return one atom per position, or {@code null} if the residue carries no bond, a position
   *     cannot be determined, or two of them land on the same atom — a single ring counted from its
   *     attachment has no way to tell 3 from 5, for instance, and stacking both groups on the one
   *     atom it picks would be a plausible but wrong structure
   */
  private static List<IAtom> residueTargets(
      IAtomContainer container, IRingSet rings, IAtom residue, List<Integer> positions) {
    Iterator<IBond> bonds = residue.bonds().iterator();
    if (!bonds.hasNext()) {
      return null;
    }
    IAtom anchor = bonds.next().getOther(residue);
    List<IAtom> targets = new ArrayList<>();
    for (int position : positions) {
      IAtom target = ringPositionAtom(container, rings, anchor, residue, position);
      if (target == null || containsSame(targets, target)) {
        return null;
      }
      targets.add(target);
    }
    return targets;
  }

  /** Whether the list already holds this very atom. */
  private static boolean containsSame(List<IAtom> atoms, IAtom atom) {
    for (IAtom candidate : atoms) {
      if (candidate == atom) {
        return true;
      }
    }
    return false;
  }

  /**
   * Attaches a further residue of the given label to an atom, correcting its implicit hydrogen
   * count. The residue carries no coordinates; the graft it becomes is laid out with the rest.
   *
   * @param container the structure to modify
   * @param target the atom to attach the residue to
   * @param label the R-group label the residue carries
   */
  private static void attachResidue(IAtomContainer container, IAtom target, String label) {
    IPseudoAtom residue = container.getBuilder().newInstance(IPseudoAtom.class, label);
    residue.setLabel(label);
    container.addAtom(residue);
    container.addBond(new Bond(target, residue, IBond.Order.SINGLE));
    shiftImplicitHydrogens(target, -1);
  }

  /**
   * The 1-based ring position a positional token names: {@code o}/{@code m}/{@code p} are the
   * ortho/meta/para positions 2/3/4, any other token is the number itself.
   *
   * <p>{@link #POSITIONAL_SUBSTITUENT} already restricts the token to one or two digits or a single
   * {@code o}/{@code m}/{@code p}, so the parse cannot currently fail. It is guarded anyway: the
   * guarantee lives in a pattern far from here, and an unreadable token must drop the one legend
   * value rather than abort the whole document — {@code NumberFormatException} is unchecked and
   * {@code SubstanceXtractor} catches only {@code IOException} and {@code
   * CloneNotSupportedException} around R-group expansion. {@code -1} is a position no ring has, so
   * the existing range check in {@link #ringAtomAtDistance} rejects it and the value is skipped.
   *
   * @param token the position part of a {@code <position>-<group>} value
   * @return the ring position counted from the attachment (ipso) atom, or {@code -1} if unreadable
   */
  private static int ringPosition(String token) {
    return switch (token) {
      case "o" -> 2;
      case "m" -> 3;
      case "p" -> 4;
      default -> {
        try {
          yield Integer.parseInt(token);
        } catch (NumberFormatException _) {
          yield -1;
        }
      }
    };
  }

  /**
   * Re-bonds a single residue to the given ring atom, correcting the implicit hydrogen count of the
   * atom it left and the one it arrived at.
   *
   * @param container the structure to modify
   * @param residue the residue (pseudo-atom) to move
   * @param target the ring atom the residue belongs on
   * @return {@code true} if the residue sits on that atom afterwards
   */
  private static boolean moveResidueToAtom(IAtomContainer container, IAtom residue, IAtom target) {
    Iterator<IBond> bonds = residue.bonds().iterator();
    if (!bonds.hasNext()) {
      return false;
    }
    IBond bond = bonds.next();
    IAtom anchor = bond.getOther(residue);
    if (target == anchor) {
      return true;
    }
    container.removeBond(bond);
    container.addBond(new Bond(target, residue, bond.getOrder()));
    shiftImplicitHydrogens(anchor, 1);
    shiftImplicitHydrogens(target, -1);
    return true;
  }

  /**
   * The atom a positional substituent names, for the ring system the residue is attached to.
   *
   * <p>Which numbering applies depends on the ring system. A fused system is numbered the way its
   * name is — indole's positions 5, 6 and 7 are on its benzo ring, counted round the periphery from
   * the nitrogen — which {@link RingSystemNumbering} works out and which the ortho/meta/para model
   * below cannot express. A single ring has no such numbering to appeal to, so its positions are
   * counted round it from its attachment atom, {@code o}/{@code m}/{@code p} being 2/3/4.
   *
   * @param container the structure the residue belongs to
   * @param rings the container's ring set
   * @param anchor the ring atom the residue is currently attached to
   * @param residue the residue being placed, used to break the direction tie on a single ring
   * @param position the 1-based position named by the legend
   * @return the atom at that position, or {@code null} if it cannot be determined
   */
  private static IAtom ringPositionAtom(
      IAtomContainer container, IRingSet rings, IAtom anchor, IAtom residue, int position) {
    Map<Integer, IAtom> locants = RingSystemNumbering.locants(container, rings, anchor);
    if (!locants.isEmpty()) {
      return locants.get(position);
    }
    IAtomContainer ring = smallestRingContaining(rings, anchor);
    if (ring == null) {
      return null;
    }
    IAtom ipso = soleAttachmentAtom(container, ring);
    if (ipso == null) {
      return null;
    }
    return ringAtomAtDistance(ring, ipso, position - 1, residue);
  }

  /** The smallest ring containing the atom, or {@code null} if it lies in none. */
  private static IAtomContainer smallestRingContaining(IRingSet rings, IAtom atom) {
    IAtomContainer smallest = null;
    for (IAtomContainer ring : rings.atomContainers()) {
      if (ring.contains(atom)
          && (smallest == null || ring.getAtomCount() < smallest.getAtomCount())) {
        smallest = ring;
      }
    }
    return smallest;
  }

  /**
   * The ring's attachment (ipso) atom: the one ring atom bonded to a real atom outside the ring.
   * Residues are ignored, since they are what the position index is about to place. Position
   * numbers are only meaningful relative to a single attachment, so a ring with none, or with
   * several, is rejected rather than guessed at.
   *
   * @param container the structure the ring belongs to
   * @param ring the ring to inspect
   * @return the ipso atom, or {@code null} if it is not unique
   */
  private static IAtom soleAttachmentAtom(IAtomContainer container, IAtomContainer ring) {
    IAtom ipso = null;
    for (IAtom atom : ring.atoms()) {
      for (IBond bond : container.getConnectedBondsList(atom)) {
        IAtom other = bond.getOther(atom);
        if (ring.contains(other) || other instanceof IPseudoAtom) {
          continue;
        }
        if (ipso != null && ipso != atom) {
          return null;
        }
        ipso = atom;
      }
    }
    return ipso;
  }

  /**
   * The ring atom a given number of bonds from the ipso atom. Both directions round the ring are
   * walked; when they reach different atoms (positions 2/6, 3/5, ... of a six-ring) the one nearer
   * the drawn residue is taken — for a ring that is symmetric about the ipso atom the two are the
   * same structure anyway, and otherwise the drawing states which side is meant.
   *
   * @param ring the ring to walk
   * @param ipso the ring's attachment atom, position 1
   * @param distance the number of bonds to walk, i.e. position - 1
   * @param residue the residue being placed, used to break the direction tie
   * @return the ring atom at that position, or {@code null} if the position does not exist
   */
  private static IAtom ringAtomAtDistance(
      IAtomContainer ring, IAtom ipso, int distance, IAtom residue) {
    if (distance <= 0 || distance > ring.getAtomCount() / 2) {
      return null;
    }
    Map<IAtom, Integer> distances = new IdentityHashMap<>();
    distances.put(ipso, 0);
    Deque<IAtom> queue = new ArrayDeque<>();
    queue.add(ipso);
    List<IAtom> reached = new ArrayList<>();
    while (!queue.isEmpty()) {
      IAtom current = queue.poll();
      int next = distances.get(current) + 1;
      for (IAtom neighbour : ring.getConnectedAtomsList(current)) {
        if (distances.containsKey(neighbour)) {
          continue;
        }
        distances.put(neighbour, next);
        if (next == distance) {
          reached.add(neighbour);
        } else {
          queue.add(neighbour);
        }
      }
    }
    return nearestTo(reached, residue.getPoint2d());
  }

  /** The atom of the list closest to the given point; the first one when there is no point. */
  private static IAtom nearestTo(List<IAtom> atoms, Point2d reference) {
    if (atoms.isEmpty()) {
      return null;
    }
    if (atoms.size() == 1 || reference == null) {
      return atoms.getFirst();
    }
    IAtom nearest = atoms.getFirst();
    double bestDistance = Double.MAX_VALUE;
    for (IAtom atom : atoms) {
      Point2d point = atom.getPoint2d();
      if (point == null) {
        continue;
      }
      double distance = point.distance(reference);
      if (distance < bestDistance) {
        bestDistance = distance;
        nearest = atom;
      }
    }
    return nearest;
  }

  /** Adjusts an atom's implicit hydrogen count as it gains or loses a substituent. */
  private static void shiftImplicitHydrogens(IAtom atom, int delta) {
    Integer count = atom.getImplicitHydrogenCount();
    if (count != null) {
      atom.setImplicitHydrogenCount(Math.max(0, count + delta));
    }
  }

  /**
   * Turns a label-to-substituents map into one independent choice per label.
   *
   * @param residueLabels map of residue labels and possible substituents
   * @return one choice per label, in the map's iteration order
   */
  private static List<Choice> independentChoices(Map<String, List<String>> residueLabels) {
    List<Choice> choices = new ArrayList<>(residueLabels.size());
    for (Map.Entry<String, List<String>> entry : residueLabels.entrySet()) {
      List<Map<String, String>> options = new ArrayList<>(entry.getValue().size());
      for (String substituent : entry.getValue()) {
        options.add(Map.of(entry.getKey(), substituent));
      }
      choices.add(new Choice(options));
    }
    return choices;
  }

  /**
   * Filters residue labels to only include those present in the atom container.
   *
   * @param atomContainer molecule containing pseudo-atoms
   * @param definitions map of all residue definitions
   * @return filtered map containing only relevant residue labels
   */
  private Map<String, List<String>> filterRelevantRGroups(
      IAtomContainer atomContainer, Map<String, List<String>> definitions) {

    Set<String> present = new HashSet<>();
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom pseudo) {
        String label = pseudo.getLabel();
        if (label != null && definitions.containsKey(label)) {
          present.add(label);
        }
      }
    }

    Map<String, List<String>> filtered = new LinkedHashMap<>();
    for (String key : present) {
      filtered.put(key, definitions.get(key));
    }
    return filtered;
  }

  /**
   * Replaces a single R-group in the atom container with the structure defined by the SMILES
   * string.
   *
   * @param atomContainer molecule to modify
   * @param residueKey label of the R-group to replace
   * @param smiles SMILES string defining the substituent
   * @throws InvalidSmilesException if SMILES parsing fails
   * @throws CloneNotSupportedException if cloning fails
   */
  private void replaceRGroup(IAtomContainer atomContainer, String residueKey, String smiles)
      throws CDKException, CloneNotSupportedException {
    IAtomContainer extendedStructure = smilesParser.parseSmiles(smiles);
    AtomContainerManipulator.suppressHydrogens(extendedStructure);
    long nStars = smiles.chars().filter(c -> '*' == c).count();
    if (nStars == 2) {
      replaceDualBondedResidue(atomContainer, extendedStructure, residueKey);
    } else {
      replaceSingleBondedResidue(atomContainer, extendedStructure, residueKey);
    }
  }

  /**
   * Replaces a single-bonded R-group in the atom container.
   *
   * @param atomContainer molecule to modify
   * @param extendedStructure structure to replace the R-group with
   * @param residueKey label of the R-group
   * @throws CloneNotSupportedException if cloning fails
   */
  private void replaceSingleBondedResidue(
      IAtomContainer atomContainer, IAtomContainer extendedStructure, String residueKey)
      throws CloneNotSupportedException {
    List<IBond> bondsToRemove = new ArrayList<>();
    List<IAtom> atomsToRemove = new ArrayList<>();
    for (IAtom atom : atomContainer.atoms()) {
      if (!(atom instanceof IPseudoAtom pseudoAtom)) {
        continue;
      }
      if (!residueKey.equals(pseudoAtom.getLabel())) {
        continue;
      }
      IAtomContainer extendedClone = extendedStructure.clone();
      if (extendedClone.getAtomCount() == 1) {
        replaceSingleAtom(atomContainer, pseudoAtom, extendedClone.getAtom(0), atomsToRemove);
      } else {
        replaceMultiAtom(atomContainer, pseudoAtom, extendedClone, bondsToRemove, atomsToRemove);
      }
    }
    for (IBond bond : bondsToRemove) {
      atomContainer.removeBond(bond);
    }
    for (IAtom atom : atomsToRemove) {
      atomContainer.removeAtom(atom);
    }
  }

  /**
   * Replaces a dual-bonded R-group and reconnects the structure properly.
   *
   * @param atomContainer molecule to modify
   * @param extendedStructure structure to replace the R-group with
   * @param residueKey label of the R-group
   * @throws CloneNotSupportedException if cloning fails
   */
  private void replaceDualBondedResidue(
      IAtomContainer atomContainer, IAtomContainer extendedStructure, String residueKey)
      throws CloneNotSupportedException {
    Set<IAtom> visitedAtoms = new HashSet<>();
    Set<IBond> bondsToRemove = new HashSet<>();
    Set<IAtom> atomsToRemove = new HashSet<>();

    for (IAtom atom : atomContainer.atoms()) {
      if (!(atom instanceof IPseudoAtom pseudoAtom)) {
        continue;
      }
      if (!residueKey.equals(pseudoAtom.getLabel())) {
        continue;
      }
      if (!visitedAtoms.add(pseudoAtom)) {
        continue; // already processed
      }
      IAtomContainer extendedClone = extendedStructure.clone();

      List<IAtom> pseudos = new ArrayList<>();
      pseudos.add(pseudoAtom);
      // A pseudo-atom that already carries two bonds *is* the bivalent position — a ring member or
      // a chain link — and its own two bonds are the attachment points. Only a monovalent residue
      // needs a partner to bridge to, and that partner must carry the same label: the nearest
      // pseudo-atom of any label would swallow an unrelated R-group and silently drop its
      // substituent.
      if (atomContainer.getConnectedBondsCount(pseudoAtom) < 2) {
        IAtom nearestOtherResidue =
            ChemicalUtils.findNearestResidueAtom(pseudoAtom, atomContainer, residueKey);
        if (nearestOtherResidue != null) {
          visitedAtoms.add(nearestOtherResidue);
          pseudos.add(nearestOtherResidue);
        }
      }

      reconnectResidue(atomContainer, extendedClone, pseudos, bondsToRemove, atomsToRemove);
    }
    for (IBond bond : bondsToRemove) {
      atomContainer.removeBond(bond);
    }
    for (IAtom atom : atomsToRemove) {
      atomContainer.removeAtom(atom);
    }
  }

  /**
   * Reconnects substituted residues to the original atom container. Every bond of every replaced
   * pseudo-atom is one attachment, so a single bivalent residue (a ring member or chain link)
   * contributes both of its bonds while two monovalent residues contribute one each. Attachments
   * are wired to the substituent's connection points in order.
   *
   * @param atomContainer original molecule
   * @param extendedStructure substituted structure
   * @param pseudoAtoms pseudo-atoms to replace
   * @param bondsToRemove list of bonds to remove after reconnection
   * @param atomsToRemove list of atoms to remove after reconnection
   */
  private void reconnectResidue(
      IAtomContainer atomContainer,
      IAtomContainer extendedStructure,
      List<IAtom> pseudoAtoms,
      Set<IBond> bondsToRemove,
      Set<IAtom> atomsToRemove) {
    record Attachment(IAtom residue, IBond bond) {}

    List<IAtom> connectionPoints = new ArrayList<>();
    for (IAtom smilesAtom : extendedStructure.atoms()) {
      if (smilesAtom instanceof IPseudoAtom) {
        connectionPoints.add(smilesAtom);
      }
    }
    List<Attachment> attachments = new ArrayList<>();
    for (IAtom rAtom : pseudoAtoms) {
      rAtom.bonds().forEach(bond -> attachments.add(new Attachment(rAtom, bond)));
    }
    if (attachments.size() != connectionPoints.size()) {
      // Wiring only some of the attachments would drop a bond and silently open a ring or split
      // the molecule. Leaving the pseudo-atom in place instead keeps the structure out of the
      // output, since SubstanceXtractor skips structures with unresolved pseudo-atoms.
      LOGGER.warn(
          "Residue has {} attachment(s) but its substituent offers {} connection point(s);"
              + " leaving the R-group unsubstituted.",
          attachments.size(),
          connectionPoints.size());
      return;
    }

    atomContainer.add(extendedStructure);
    for (int i = 0; i < attachments.size(); i++) {
      IAtom rAtom = attachments.get(i).residue();
      IBond bondOrigin = attachments.get(i).bond();
      IAtom atomOrigin = bondOrigin.getOther(rAtom);
      IAtom conPoint = connectionPoints.get(i);
      IBond bondAbbr = conPoint.bonds().iterator().next();
      IAtom atomAbbr = bondAbbr.getOther(conPoint);
      atomContainer.addBond(new Bond(atomOrigin, atomAbbr, bondOrigin.getOrder()));
      bondsToRemove.add(bondOrigin);
      bondsToRemove.add(bondAbbr);
      atomsToRemove.add(rAtom);
      atomsToRemove.add(conPoint);
    }

    for (IAtom conPoint : connectionPoints) {
      atomsToRemove.add(conPoint);
      if (conPoint.bonds().iterator().hasNext()) {
        bondsToRemove.add(conPoint.bonds().iterator().next());
      }
    }
  }

  /**
   * Replaces the given single R-Atom with the given new IAtom in the IAtomContainer and adds the
   * R-Atom to the list of atoms to be removed
   *
   * @param atomContainer IAtomContainer
   * @param pseudoAtom abbreviation/R atom (IAtom) to be replaced
   * @param newAtom IAtom to replace the rAtom
   * @param atomsToRemove list of atoms that will be removed from the IAtomContainer
   */
  private void replaceSingleAtom(
      IAtomContainer atomContainer, IAtom pseudoAtom, IAtom newAtom, List<IAtom> atomsToRemove) {
    atomContainer.addAtom(newAtom);

    List<IBond> connectedBonds = new ArrayList<>();
    pseudoAtom.bonds().forEach(connectedBonds::add);
    connectedBonds.forEach(bond -> bond.setAtoms(new IAtom[] {bond.getOther(pseudoAtom), newAtom}));
    newAtom.setValency(connectedBonds.size());
    int bondOrderSum = 0;
    for (IBond bond : connectedBonds) {
      bondOrderSum += bond.getOrder().numeric();
    }
    newAtom.setImplicitHydrogenCount(
        Math.max(newAtom.getImplicitHydrogenCount() - bondOrderSum, 0));

    atomsToRemove.add(pseudoAtom);
  }

  /**
   * Replaces and reconnects the given IAtomContainer parsed from a SMILES with the residue IAtoms
   * in the original IAtomContainer.
   *
   * @param atomContainer IAtomContainer
   * @param pseudoAtom List of residue IAtoms
   * @param expandedStructure IAtomContainer of the structure parsed from the abbreviation SMILES
   * @param bondsToRemove list of bonds that will be removed from the IAtomContainer
   * @param atomsToRemove list of atoms that will be removed from the IAtomContainer
   */
  private void replaceMultiAtom(
      IAtomContainer atomContainer,
      IAtom pseudoAtom,
      IAtomContainer expandedStructure,
      List<IBond> bondsToRemove,
      List<IAtom> atomsToRemove) {
    List<IAtom> connectionPoints = new ArrayList<>();
    for (IAtom atom : expandedStructure.atoms()) {
      if (atom instanceof IPseudoAtom) {
        connectionPoints.add(atom);
      }
    }
    if (connectionPoints.size() != 1) {
      LOGGER.error("More than one or none connection point found.");
      return;
    }
    IAtom connectionPoint = connectionPoints.getFirst();
    // Find bond between pseudoAtom and its origin
    IBond bondOrigin = null;
    if (!pseudoAtom.bonds().iterator().hasNext()) {
      return;
    }
    bondOrigin = pseudoAtom.bonds().iterator().next();
    IAtom originAtom = bondOrigin.getOther(pseudoAtom);
    // Find bond inside abbreviation connecting to connection point
    IBond bondInsideAbbr = connectionPoint.bonds().iterator().next();
    IAtom atomInsideAbbr = bondInsideAbbr.getOther(connectionPoint);
    // Reconnect: origin to abbreviation atom
    IBond newBond;
    try {
      newBond = bondOrigin.clone();
    } catch (CloneNotSupportedException _) {
      LOGGER.error("Bond could not be cloned.");
      return;
    }
    newBond.setAtoms(new IAtom[] {originAtom, atomInsideAbbr});

    atomContainer.add(expandedStructure);
    atomContainer.addBond(newBond);

    bondsToRemove.add(bondOrigin);
    bondsToRemove.add(bondInsideAbbr);
    atomsToRemove.add(pseudoAtom);
    atomsToRemove.add(connectionPoint);
  }

  /**
   * Adds structural residue definitions (e.g. resolved from ChemDraw {@code
   * NamedAlternativeGroup}s) to the ones parsed from text. Structural definitions take precedence
   * over any text definition for the same label, since they are unambiguous.
   *
   * @param definitions map of R-group labels to their substituent SMILES
   */
  public void addResidueDefinitions(Map<String, List<String>> definitions) {
    definitions.forEach(residueLabels::put);
    definitions.forEach(structuralDefinitions::put);
  }

  /**
   * Resolves the R-group definitions applicable to a scaffold at the given position: the text
   * definition block nearest to the scaffold (falling back to the page-wide union when the scaffold
   * has no position or no text block exists), overlaid with any structural (alternative-group)
   * definitions, which are unambiguous.
   *
   * @param scaffoldBounds bounding box of the scaffold
   * @return the scoped map of R-group labels to substituents
   */
  public Map<String, List<String>> residueLabelsNear(CDRectangle scaffoldBounds) {
    Map<String, List<String>> scoped = new LinkedHashMap<>();
    // Resolve each label independently: definitions for different scaffolds may share a legend
    // (one block per label) or sit beside each scaffold (a label defined in several blocks).
    // Picking
    // the nearest block *that defines the label* handles both, and only disambiguates when a label
    // genuinely has competing definitions.
    for (Map.Entry<String, List<String>> entry : residueLabels.entrySet()) {
      String label = entry.getKey();
      RGroupDefinitionBlock nearest = nearestBlockDefining(label, scaffoldBounds);
      List<String> values = nearest != null ? nearest.definitions().get(label) : entry.getValue();
      scoped.put(label, new ArrayList<>(values));
    }
    structuralDefinitions.forEach((label, values) -> scoped.put(label, new ArrayList<>(values)));
    return scoped;
  }

  /**
   * Finds the definition block that defines the given label whose source text is closest to the
   * scaffold's bounding box.
   *
   * @param label the R-group label to resolve
   * @param scaffoldBounds bounding box of the scaffold
   * @return the nearest block defining the label, or {@code null} if none has a usable position
   */
  private RGroupDefinitionBlock nearestBlockDefining(String label, CDRectangle scaffoldBounds) {
    if (scaffoldBounds == null) {
      return null;
    }
    RGroupDefinitionBlock best = null;
    double bestDistance = Double.MAX_VALUE;
    for (RGroupDefinitionBlock block : blocks) {
      if (block.bounds() == null || !block.definitions().containsKey(label)) {
        continue;
      }
      double distance = rectangleDistance(scaffoldBounds, block.bounds());
      if (distance < bestDistance) {
        bestDistance = distance;
        best = block;
      }
    }
    return best;
  }

  /**
   * Returns the gap between two rectangles (0 if they overlap).
   *
   * @param a first rectangle
   * @param b second rectangle
   * @return the Euclidean gap between the rectangles
   */
  private static double rectangleDistance(CDRectangle a, CDRectangle b) {
    double dx = Math.max(0, Math.max(a.getLeft() - b.getRight(), b.getLeft() - a.getRight()));
    double dy = Math.max(0, Math.max(a.getTop() - b.getBottom(), b.getTop() - a.getBottom()));
    return Math.hypot(dx, dy);
  }

  /**
   * Returns a map of residue labels extracted from the page.
   *
   * @return map of R-group labels and their corresponding substituents
   */
  public Map<String, List<String>> getResidueLabels() {
    return residueLabels;
  }
}
