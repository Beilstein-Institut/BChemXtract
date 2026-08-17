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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cheminf.AbbreviationLayout;
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
 * IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
 * IAtomContainer molecule = ...;
 *
 * MarkushHandler handler = new MarkushHandler(page, builder);
 * List<IAtomContainer> substitutedMolecules = handler.replaceRGroups(molecule);
 * }</pre>
 */
public class MarkushHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarkushHandler.class);

  /**
   * A substituent written as {@code <position>-<group>}, e.g. {@code 3-OMe}: the group sits on ring
   * position 3, counted from the ring's attachment atom. Only tried after the plain abbreviation
   * lookup fails, because many abbreviations legitimately start the same way ({@code 2-py}, {@code
   * 4-ClPh}) and there the leading digit belongs to the substituent's own name.
   */
  private static final Pattern POSITIONAL_SUBSTITUENT = Pattern.compile("(\\d{1,2})-(.+)");

  private final Map<String, List<String>> residueLabels;
  private final Map<String, List<String>> structuralDefinitions = new LinkedHashMap<>();
  private final List<RGroupDefinitionBlock> blocks;
  private final SmilesParser smilesParser;

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
                List<String> list = definitions.computeIfAbsent(label, key -> new ArrayList<>());
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
    return replaceRGroups(atomContainer, residueLabels);
  }

  /**
   * Generates all possible structures for a scaffold, resolving its R-groups from the definition
   * block nearest to the scaffold. This prevents definitions of one scaffold from leaking into
   * another when several scaffolds on the same page reuse the same R-group labels.
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
    Set<String> present = presentResidueLabels(atomContainer);
    List<Map<String, String>> combinations = residueCombinationsNear(present, scaffoldBounds);
    if (combinations.isEmpty()) {
      // No scoped definitions apply to this scaffold; defer to the page-wide union.
      return replaceRGroups(atomContainer, residueLabels);
    }
    List<IAtomContainer> scoped = applyCombinations(atomContainer, combinations);
    // Scoping is a refinement: if it narrowed the definitions down to something that produced no
    // structures, fall back to the page-wide union so scoping never does worse than no scoping.
    if (scoped.isEmpty()) {
      return replaceRGroups(atomContainer, residueLabels);
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
   * Builds the substituent combinations for a scaffold, honouring correlated (positional-table)
   * groups: their labels vary together as fixed row-tuples, while remaining labels vary
   * independently (cartesian). Definitions are scoped to the blocks nearest the scaffold.
   *
   * @param present labels of the pseudo-atoms in the scaffold
   * @param scaffoldBounds bounding box of the scaffold, for nearest-block scoping
   * @return the list of label-to-substituent assignments; empty if no definition applies
   */
  private List<Map<String, String>> residueCombinationsNear(
      Set<String> present, CDRectangle scaffoldBounds) {
    List<Map<String, String>> combinations = new ArrayList<>();
    combinations.add(new LinkedHashMap<>());

    // Correlated groups first: for each distinct label set, pick the nearest block's group and
    // expand the running combinations by its explicit row-tuples.
    Set<String> claimed = new HashSet<>();
    for (CorrelatedGroup group : nearestCorrelatedGroups(present, scaffoldBounds)) {
      List<Map<String, String>> expanded = new ArrayList<>();
      for (Map<String, String> base : combinations) {
        for (Map<String, String> tuple : group.tuples()) {
          Map<String, String> merged = new LinkedHashMap<>(base);
          merged.putAll(tuple);
          expanded.add(merged);
        }
      }
      combinations = expanded;
      claimed.addAll(group.labels());
    }

    // Independent labels: cartesian expansion, scoped per-label to the nearest defining block.
    Map<String, List<String>> scopedIndependent = residueLabelsNear(scaffoldBounds);
    for (String label : present) {
      if (claimed.contains(label)) {
        continue;
      }
      List<String> values = scopedIndependent.get(label);
      if (values == null || values.isEmpty()) {
        continue;
      }
      List<Map<String, String>> expanded = new ArrayList<>();
      for (Map<String, String> base : combinations) {
        for (String value : values) {
          Map<String, String> merged = new LinkedHashMap<>(base);
          merged.put(label, value);
          expanded.add(merged);
        }
      }
      combinations = expanded;
    }

    // A single empty assignment means nothing was applicable.
    if (combinations.size() == 1 && combinations.get(0).isEmpty()) {
      return List.of();
    }
    return combinations;
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
      IAtomContainer atomContainer, Map<String, List<String>> definitions)
      throws CloneNotSupportedException, IOException, CDKException {

    Map<String, List<String>> relevantRGroups = filterRelevantRGroups(atomContainer, definitions);

    if (relevantRGroups.isEmpty()) {
      return List.of(atomContainer);
    }
    return applyCombinations(atomContainer, generateCombinations(relevantRGroups));
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
      IAtomContainer atomContainer, List<Map<String, String>> combinations)
      throws CloneNotSupportedException, IOException, CDKException {
    List<IAtomContainer> results = new ArrayList<>(combinations.size());

    for (Map<String, String> combination : combinations) {
      IAtomContainer clone = atomContainer.clone();
      // Snapshot the pre-substitution atoms so the grafted (coordinate-less) atoms can be
      // distinguished from the retained scaffold afterwards.
      Set<IAtom> scaffoldAtoms = Collections.newSetFromMap(new IdentityHashMap<>());
      clone.atoms().forEach(scaffoldAtoms::add);
      boolean substituted = false;
      boolean allResolved = true;

      for (Map.Entry<String, String> entry : combination.entrySet()) {
        String smiles = resolveSmiles(entry.getValue());
        if (ChemicalUtils.isValidSmiles(smiles)) {
          replaceRGroup(clone, entry.getKey(), smiles);
          substituted = true;
        } else if (replacePositionalRGroup(clone, entry.getKey(), entry.getValue())) {
          substituted = true;
        } else {
          // An unresolvable label (unknown abbreviation, cross-referenced R-group, ...) would
          // leave a dangling pseudo-atom. A structure with an unresolved R-group is never emitted,
          // so the whole combination is dropped rather than substituting only some of its labels.
          LOGGER.warn(
              "Unresolved R-group label {}=\"{}\"; dropping this substituent combination.",
              entry.getKey(),
              entry.getValue());
          allResolved = false;
          break;
        }
      }
      if (substituted && allResolved) {
        layoutGraftedAtoms(clone, scaffoldAtoms);
        results.add(clone);
      }
    }
    return results;
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
    Matcher matcher = POSITIONAL_SUBSTITUENT.matcher(value);
    if (!matcher.matches()) {
      return false;
    }
    String smiles = resolveSmiles(matcher.group(2));
    if (!ChemicalUtils.isValidSmiles(smiles)) {
      return false;
    }
    if (!moveResiduesToRingPosition(container, label, Integer.parseInt(matcher.group(1)))) {
      return false;
    }
    replaceRGroup(container, label, smiles);
    return true;
  }

  /**
   * Moves every residue carrying the given label onto the ring atom named by the position index.
   *
   * @param container the structure to modify
   * @param label the R-group label
   * @param position the 1-based ring position, counted from the ring's attachment atom
   * @return {@code true} if every such residue now sits on the named position
   */
  private static boolean moveResiduesToRingPosition(
      IAtomContainer container, String label, int position) {
    List<IAtom> residues = new ArrayList<>();
    for (IAtom atom : container.atoms()) {
      if (atom instanceof IPseudoAtom pseudo && label.equals(pseudo.getLabel())) {
        residues.add(atom);
      }
    }
    if (residues.isEmpty()) {
      return false;
    }
    IRingSet rings = Cycles.mcb(container).toRingSet();
    for (IAtom residue : residues) {
      if (!moveResidueToRingPosition(container, rings, residue, position)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Re-bonds a single residue to the ring atom at the given position, correcting the implicit
   * hydrogen count of the atom it left and the one it arrived at.
   *
   * @param container the structure to modify
   * @param rings the ring set of the container
   * @param residue the residue (pseudo-atom) to move
   * @param position the 1-based ring position, counted from the ring's attachment atom
   * @return {@code true} if the residue sits on the named position afterwards
   */
  private static boolean moveResidueToRingPosition(
      IAtomContainer container, IRingSet rings, IAtom residue, int position) {
    Iterator<IBond> bonds = residue.bonds().iterator();
    if (!bonds.hasNext()) {
      return false;
    }
    IBond bond = bonds.next();
    IAtom anchor = bond.getOther(residue);
    IAtomContainer ring = smallestRingContaining(rings, anchor);
    if (ring == null) {
      return false;
    }
    IAtom ipso = soleAttachmentAtom(container, ring);
    if (ipso == null) {
      return false;
    }
    IAtom target = ringAtomAtDistance(ring, ipso, position - 1, residue);
    if (target == null) {
      return false;
    }
    if (target == anchor) {
      return true;
    }
    container.removeBond(bond);
    container.addBond(new Bond(target, residue, bond.getOrder()));
    shiftImplicitHydrogens(anchor, 1);
    shiftImplicitHydrogens(target, -1);
    return true;
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
      return atoms.get(0);
    }
    IAtom nearest = atoms.get(0);
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
   * Generates all combinations of residue labels for replacement.
   *
   * @param residueLabels map of residue labels and possible substituents
   * @return list of maps representing all possible label-to-substituent combinations
   */
  private List<Map<String, String>> generateCombinations(Map<String, List<String>> residueLabels) {
    List<Map<String, String>> result = new ArrayList<>();
    List<String> labels = new ArrayList<>(residueLabels.keySet());

    backtrack(residueLabels, labels, 0, new HashMap<>(), result);
    return result;
  }

  /**
   * Recursive helper method to backtrack through all R-group combinations.
   *
   * @param residueLabels residue labels map
   * @param rLabels list of R-group labels
   * @param index current index in recursion
   * @param current current combination being built
   * @param results list of all combinations generated
   */
  private void backtrack(
      Map<String, List<String>> residueLabels,
      List<String> rLabels,
      int index,
      Map<String, String> current,
      List<Map<String, String>> results) {
    if (index == rLabels.size()) {
      results.add(new LinkedHashMap<>(current));
      return;
    }
    String currentRLabel = rLabels.get(index);
    for (String substituent : residueLabels.get(currentRLabel)) {
      current.put(currentRLabel, substituent);
      backtrack(residueLabels, rLabels, index + 1, current, results);
    }
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
      IAtom nearestOtherResidue = ChemicalUtils.findNearestResidueAtom(pseudoAtom, atomContainer);
      if (nearestOtherResidue != null) {
        visitedAtoms.add(nearestOtherResidue);
        pseudos.add(nearestOtherResidue);
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
   * Reconnects substituted residues to the original atom container.
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
    atomContainer.add(extendedStructure);
    List<IAtom> connectionPoints = new ArrayList<>();

    for (IAtom smilesAtom : extendedStructure.atoms()) {
      if (smilesAtom instanceof IPseudoAtom) {
        connectionPoints.add(smilesAtom);
      }
    }
    for (int i = 0; i < pseudoAtoms.size(); i++) {
      IAtom rAtom = pseudoAtoms.get(i);
      IBond bondOrigin = rAtom.bonds().iterator().next();
      IAtom atomOrigin = bondOrigin.getOther(rAtom);
      IAtom conPoint = connectionPoints.get(i);
      IBond bondAbbr = conPoint.bonds().iterator().next();
      IAtom atomAbbr = bondAbbr.getOther(conPoint);
      IBond bond = new Bond(atomOrigin, atomAbbr, rAtom.bonds().iterator().next().getOrder());
      atomContainer.addBond(bond);
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
    IAtom connectionPoint = connectionPoints.get(0);
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
    } catch (CloneNotSupportedException e) {
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
