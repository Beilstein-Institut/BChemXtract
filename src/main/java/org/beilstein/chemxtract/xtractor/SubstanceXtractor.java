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
package org.beilstein.chemxtract.xtractor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import org.beilstein.chemxtract.cdx.CDAltGroup;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.converter.FragmentConverter;
import org.beilstein.chemxtract.lookups.SmilesAbbreviations;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.model.BCXSubstanceOccurrence;
import org.beilstein.chemxtract.utils.AttachmentHandler;
import org.beilstein.chemxtract.utils.ChemicalUtils;
import org.beilstein.chemxtract.utils.Definitions;
import org.beilstein.chemxtract.utils.MarkushHandler;
import org.beilstein.chemxtract.utils.SgroupHandler;
import org.beilstein.chemxtract.visitor.AltGroupVisitor;
import org.beilstein.chemxtract.visitor.FragmentVisitor;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.io.MDLV3000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for extracting chemical substances from a {@link CDDocument}.
 *
 * <p>The {@code SubstanceXtractor} traverses ChemDraw pages and fragments, converting them into
 * {@link BCXSubstance} objects with associated chemical information such as SMILES, InChI,
 * InChIKey, molecular formula, and occurrences in the document.
 */
public class SubstanceXtractor {

  private final IChemObjectBuilder builder;
  private static final Logger LOGGER = LoggerFactory.getLogger(SubstanceXtractor.class);

  /**
   * Constructs a {@code SubstanceXtractor} using a custom CDK {@link IChemObjectBuilder}.
   *
   * @param builder the CDK chem object builder used for creating atom containers
   */
  public SubstanceXtractor(IChemObjectBuilder builder) {
    this.builder = builder;
  }

  /** Constructs a {@code SubstanceXtractor} using the default CDK object builder. */
  public SubstanceXtractor() {
    this(DefaultChemObjectBuilder.getInstance());
  }

  /**
   * Extracts all chemical substances from the given ChemDraw document.
   *
   * <p>Each fragment is processed and converted into one or more {@link BCXSubstance} objects,
   * optionally resolving R-groups if specified.
   *
   * @param document the ChemDraw {@link CDDocument} to extract substances from
   * @param substanceInfo object for tracking extraction metadata (e.g., number of fragments)
   * @param resolveRGroups if {@code true}, R-groups are resolved to generate all possible variants
   * @return a list of extracted {@link BCXSubstance} objects
   */
  public List<BCXSubstance> xtract(
      CDDocument document, BCXSubstanceInfo substanceInfo, boolean resolveRGroups) {
    Objects.requireNonNull(document, "Document must not be null.");
    List<BCXSubstance> substances = new ArrayList<>();
    for (CDPage page : document.getPages()) {

      FragmentVisitor fragmentVisitor = new FragmentVisitor(page);
      List<CDFragment> fragments = fragmentVisitor.getFragments();
      MarkushHandler markushHandler = null;
      if (resolveRGroups) {
        markushHandler = new MarkushHandler(page, this.builder);
        markushHandler.addResidueDefinitions(resolveAltGroupDefinitions(page));
      }

      substanceInfo.setNoFragments(fragments.size());
      for (CDFragment fragment : fragments) {
        try {
          substances.addAll(xtractSubstances(fragment, page, markushHandler));
        } catch (IOException | CDKException e) {
          LOGGER.error("Could not extract structures from fragment.", e);
        }
      }
    }
    return substances;
  }

  /**
   * Extracts all chemical substances without resolving R-groups.
   *
   * @param document the ChemDraw {@link CDDocument} to extract substances from
   * @param substanceInfo object for tracking extraction metadata
   * @return a list of extracted {@link BCXSubstance} objects
   */
  public List<BCXSubstance> xtract(CDDocument document, BCXSubstanceInfo substanceInfo) {
    return this.xtract(document, substanceInfo, false);
  }

  /**
   * Resolves R-group definitions from ChemDraw {@code NamedAlternativeGroup}s on the page. Each
   * alternative fragment is converted to a substituent SMILES so it can feed the same replacement
   * machinery used for text-defined residues.
   *
   * @param page the page to scan for alternative groups
   * @return map of R-group label to the SMILES of each alternative substituent
   */
  private Map<String, List<String>> resolveAltGroupDefinitions(CDPage page) {
    Map<String, List<String>> definitions = new LinkedHashMap<>();
    for (CDAltGroup altGroup : new AltGroupVisitor(page).getAltGroups()) {
      String label = altGroupLabel(altGroup);
      if (label == null) {
        continue;
      }
      List<String> alternatives = new ArrayList<>();
      for (CDFragment alternative : altGroup.getFragments()) {
        try {
          IAtomContainer container = new FragmentConverter(this.builder).convert(alternative);
          String smiles = ChemicalUtils.createSmiles(container, SmiFlavor.Canonical);
          if (smiles != null && !smiles.isEmpty()) {
            alternatives.add(smiles);
          }
        } catch (CDKException | IllegalArgumentException e) {
          LOGGER.error("Could not convert alternative group fragment for label {}.", label, e);
        }
      }
      if (!alternatives.isEmpty()) {
        definitions.put(label, alternatives);
      }
    }
    return definitions;
  }

  /**
   * Derives the R-group label of an alternative group from its caption (e.g. {@code "R1"}).
   *
   * @param altGroup the alternative group
   * @return the label, or {@code null} if no caption yields a recognisable R-group identifier
   */
  private String altGroupLabel(CDAltGroup altGroup) {
    for (CDText caption : altGroup.getCaptions()) {
      if (caption.getText() == null || caption.getText().getText() == null) {
        continue;
      }
      Matcher matcher =
          Definitions.RGROUP_LABEL_PATTERN.matcher(caption.getText().getText().trim());
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  /**
   * Extracts unique chemical substances from the given document.
   *
   * <p>Substances are considered unique based on their InChI identifiers.
   *
   * @param document the ChemDraw {@link CDDocument} to extract substances from
   * @param substanceInfo object for tracking extraction metadata
   * @param resolveRGroups if {@code true}, R-groups are resolved
   * @return a list of unique {@link BCXSubstance} objects
   */
  public List<BCXSubstance> xtractUnique(
      CDDocument document, BCXSubstanceInfo substanceInfo, boolean resolveRGroups) {
    List<BCXSubstance> substances = xtract(document, substanceInfo, resolveRGroups);
    List<BCXSubstance> result = new ArrayList<>();
    substanceInfo.setNoInchis(
        (int)
            substances.stream()
                .filter(s -> s.getInchi() != null && !s.getInchi().isEmpty())
                .count());
    Set<String> seen = new HashSet<>(substanceInfo.getNoInchis());
    substances.forEach(
        s -> {
          if (seen.add(s.getInchi())) {
            result.add(s);
          }
        });
    substanceInfo.setNoSubstances(result.size());
    return result;
  }

  /**
   * Extracts unique chemical substances without resolving R-groups.
   *
   * @param document the ChemDraw {@link CDDocument} to extract substances from
   * @param substanceInfo object for tracking extraction metadata
   * @return a list of unique {@link BCXSubstance} objects
   */
  public List<BCXSubstance> xtractUnique(CDDocument document, BCXSubstanceInfo substanceInfo) {
    return xtractUnique(document, substanceInfo, false);
  }

  /**
   * Extracts a single {@link BCXSubstance} from a fragment.
   *
   * @param fragment the {@link CDFragment} to extract
   * @param page the {@link CDPage} containing the fragment
   * @return the extracted {@link BCXSubstance}, or {@code null} if none could be extracted
   * @throws CDKException if CDK processing fails
   * @throws IOException if IO operations fail
   */
  protected BCXSubstance xtractSubstance(CDFragment fragment, CDPage page)
      throws CDKException, IOException {
    List<BCXSubstance> substances = xtractSubstances(fragment, page, null);
    if (!substances.isEmpty()) {
      return substances.get(0);
    } else {
      return null;
    }
  }

  /**
   * Extracts all {@link BCXSubstance} objects from a fragment.
   *
   * <p>Handles multi-atom groups, R-groups (if requested), and adds fragment occurrences and
   * abbreviations.
   *
   * @param fragment the {@link CDFragment} to process
   * @param page the {@link CDPage} containing the fragment
   * @param markushHandler the {@link MarkushHandler} used to resolve R-groups (Markush expansion);
   *     may be {@code null} to skip R-group resolution
   * @return a list of extracted {@link BCXSubstance} objects
   * @throws IOException if IO operations fail
   * @throws CDKException if CDK operations fail
   */
  protected List<BCXSubstance> xtractSubstances(
      CDFragment fragment, CDPage page, MarkushHandler markushHandler)
      throws IOException, CDKException {
    Objects.requireNonNull(fragment, "Fragment must not be null.");
    List<BCXSubstance> substances = new ArrayList<>();

    // validate fragment
    if (!fragment.isValid()) {
      LOGGER.info("Fragment validation failed: The fragment has one or zero atoms.");
      return substances;
    }

    SgroupHandler.addMultipleGroupBrackets(fragment, page);
    // Materialise multicenter (haptic) bonds in place before conversion.
    AttachmentHandler.resolveMultiAttachments(fragment);

    // A position-variation scaffold may legitimately retain unresolved R-groups across its
    // enumerated isomers, so those substances are emitted even when InChI cannot represent them.
    boolean variablePosition = AttachmentHandler.hasVariableAttachment(fragment);

    FragmentConverter fragmentConverter = new FragmentConverter(this.builder);
    // Position-variation nodes expand to one fragment per candidate atom; structures without a
    // variable attachment yield the original fragment unchanged.
    for (CDFragment variant : AttachmentHandler.expandVariableAttachments(fragment)) {
      IAtomContainer atomContainer;
      try {
        atomContainer = fragmentConverter.convert(variant);
      } catch (IllegalArgumentException e) {
        LOGGER.error("Fragment conversion failed", e);
        continue;
      }

      boolean expandedRGroups = false;
      if (markushHandler != null
          && variant.hasRGroup()
          && !markushHandler.getResidueLabels().isEmpty()) {
        try {
          for (IAtomContainer container :
              markushHandler.replaceRGroups(atomContainer, fragment.getBounds())) {
            substances.add(buildSubstance(container, fragment, variablePosition));
          }
          expandedRGroups = true;
        } catch (IOException | CloneNotSupportedException e) {
          LOGGER.error("R-group replacement failed", e);
        }
      }
      if (!expandedRGroups) {
        substances.add(buildSubstance(atomContainer, fragment, variablePosition));
      }
    }
    return substances;
  }

  /**
   * Creates a {@link BCXSubstance} from the given atom container and enriches it with the
   * fragment's document occurrence and abbreviations.
   *
   * @param atomContainer the CDK atom container for the substance
   * @param fragment the source fragment providing occurrence bounds and abbreviations
   * @param tolerateMissingInchi when {@code true}, a substance whose unresolved pseudo-atoms
   *     prevent InChI generation is still produced (carrying SMILES and molecular formula)
   * @return the populated {@link BCXSubstance}
   * @throws IOException if abbreviation resolution fails
   * @throws CDKException if InChI generation or nested fragment conversion fails
   */
  private BCXSubstance buildSubstance(
      IAtomContainer atomContainer, CDFragment fragment, boolean tolerateMissingInchi)
      throws IOException, CDKException {
    BCXSubstance substance = createAndFillBCXSubstance(atomContainer, tolerateMissingInchi);
    Optional<CDRectangle> boundsOptional = Optional.ofNullable(fragment.getBounds());
    if (boundsOptional.isPresent()) {
      CDRectangle bounds = boundsOptional.get();
      substance.addOccurrence(
          new BCXSubstanceOccurrence(
              bounds.getTop(), bounds.getLeft(), bounds.getBottom(), bounds.getRight()));
    }
    addAbbreviations(substance, fragment);
    return substance;
  }

  /**
   * Creates and populates a {@link BCXSubstance} from the given {@link IAtomContainer}.
   *
   * @param atomContainer the CDK atom container
   * @param tolerateMissingInchi when {@code true}, an InChI failure caused by unresolved
   *     pseudo-atoms is logged and skipped rather than propagated
   * @return a {@link BCXSubstance}
   * @throws CDKException if InChI generation fails for a structure without pseudo-atoms, or SMILES
   *     generation fails
   */
  private BCXSubstance createAndFillBCXSubstance(
      IAtomContainer atomContainer, boolean tolerateMissingInchi) throws CDKException {
    // create and fill BCXSubtances
    BCXSubstance substance = new BCXSubstance();
    // add AtomContainer
    substance.setAtomContainer(atomContainer);
    // add MDLV3000 mol file as string
    final StringWriter sw = new StringWriter();
    try (MDLV3000Writer mdlw = new MDLV3000Writer(sw)) {
      mdlw.write(withWedgeBonds(atomContainer));
      substance.setMdlv3000(sw.toString());
    } catch (IOException e) {
      LOGGER.error("Could not generate MDL V3000 mol file;");
    }
    // set SMILES
    String smiles;
    if (atomContainer.getAtomCount() > Definitions.MAX_ATOM_COUNT) {
      smiles = ChemicalUtils.createSmiles(atomContainer, SmiFlavor.Isomeric);
    } else {
      smiles = ChemicalUtils.createAbsoluteSmiles(atomContainer);
    }
    if (smiles == null) {
      // Fallback to canonical SMILES
      smiles = ChemicalUtils.createSmiles(atomContainer, SmiFlavor.Canonical);
      LOGGER.error("Generated canonical SMILES instead of absolute.");
    }
    substance.setSmiles(smiles);
    substance.setExtendedSmiles(ChemicalUtils.createExtendedSmiles(atomContainer));
    // set InChI, InChIKey and AuxInfo. A structure containing unresolved pseudo-atoms (e.g. an
    // R-group whose attachment position varies) cannot be represented in InChI, but is still a
    // valid extraction carrying SMILES and a molecular formula; other InChI failures remain fatal.
    if (atomContainer.getAtomCount() <= Definitions.MAX_ATOM_COUNT) {
      try {
        InChIGenerator gen = ChemicalUtils.getInChI(atomContainer);
        substance.setInchi(gen.getInchi());
        substance.setInchiKey(gen.getInchiKey());
        if (gen.getAuxInfo().length() < 4000) {
          substance.setAuxInfo(gen.getAuxInfo());
        }
      } catch (CDKException e) {
        if (!(tolerateMissingInchi && containsPseudoAtom(atomContainer))) {
          throw e;
        }
        LOGGER.warn("InChI generation skipped for substance with unresolved pseudo-atoms", e);
      }
    }
    // set molecular formula
    IMolecularFormula molecularFormula =
        MolecularFormulaManipulator.getMolecularFormula(atomContainer);
    substance.setMolecularFormula(MolecularFormulaManipulator.getString(molecularFormula));
    return substance;
  }

  /**
   * Returns a copy of {@code atomContainer} with up/down wedge bonds assigned from its stereo
   * elements, for writing the MDL V3000 mol file.
   *
   * <p>CDK's {@code MDLV3000Reader} deliberately ignores the atom parity ({@code CFG}) field for
   * structures that carry 2D coordinates and instead re-perceives tetrahedral stereochemistry from
   * wedge/hash bonds. The extractor's containers hold {@link
   * org.openscience.cdk.interfaces.ITetrahedralChirality} elements but no wedge bonds, so {@code
   * MDLV3000Writer} emits stereo only as atom {@code CFG} — which the reader discards, losing the
   * stereo on round-trip. {@link StructureDiagramGenerator#generateWedges} assigns the wedge bonds
   * from the stereo elements without altering the existing layout, making the mol file round-trip
   * (and interoperate with other toolkits) correctly.
   *
   * <p>Wedges are assigned on a clone so the shared container used for SMILES/InChI generation and
   * exposed via {@link BCXSubstance#getAtomContainer()} is not mutated. If cloning or wedge
   * assignment fails the original container is returned, preserving the previous behaviour.
   *
   * @param atomContainer the container to derive the mol-file structure from
   * @return a wedge-annotated copy, or the original container if wedge assignment is not possible
   */
  private static IAtomContainer withWedgeBonds(IAtomContainer atomContainer) {
    try {
      IAtomContainer copy = atomContainer.clone();
      new StructureDiagramGenerator().generateWedges(copy);
      return copy;
    } catch (CloneNotSupportedException | RuntimeException e) {
      LOGGER.warn(
          "Could not assign wedge bonds for MDL V3000 mol file; writing without wedges.", e);
      return atomContainer;
    }
  }

  /**
   * Indicates whether the atom container holds any pseudo-atom (e.g. an unresolved R-group or
   * abbreviation), which InChI cannot represent.
   *
   * @param atomContainer the atom container to inspect
   * @return {@code true} if at least one atom is an {@link IPseudoAtom}
   */
  private static boolean containsPseudoAtom(IAtomContainer atomContainer) {
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds abbreviations and nicknames from the fragment to the substance.
   *
   * @param substance the {@link BCXSubstance} to populate
   * @param fragment the {@link CDFragment} containing abbreviations/nicknames
   * @throws IOException if fragment conversion fails
   */
  private void addAbbreviations(BCXSubstance substance, CDFragment fragment)
      throws IOException, CDKException {
    FragmentConverter fragmentConverter = new FragmentConverter(builder);

    for (String abbreviation : fragmentConverter.getAbbreviations(fragment)) {
      String smiles = SmilesAbbreviations.get(abbreviation);
      if (smiles == null) {
        LOGGER.info("No SMILES found for: {}", abbreviation);
        continue;
      }
      substance.addAbbreviation(smiles, abbreviation);
    }

    for (Map.Entry<String, CDFragment> entry :
        fragmentConverter.getNicknames(fragment).entrySet()) {
      String nickname = entry.getKey();
      CDFragment nested = entry.getValue();
      IAtomContainer nestedAc;
      try {
        nestedAc = fragmentConverter.convert(nested, true);
      } catch (CDKException e) {
        LOGGER.error("Nested fragment could not be converted: {}", nickname);
        continue;
      }
      substance.addAbbreviation(ChemicalUtils.createAbsoluteSmiles(nestedAc), nickname);
    }
  }
}
