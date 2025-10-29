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
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.converter.FragmentConverter;
import org.beilstein.chemxtract.lookups.SmilesAbbreviations;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.model.BCXSubstanceOccurrence;
import org.beilstein.chemxtract.utils.ChemicalUtils;
import org.beilstein.chemxtract.utils.MarkushHandler;
import org.beilstein.chemxtract.utils.SgroupHandler;
import org.beilstein.chemxtract.visitor.FragmentVisitor;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * Class for extracting chemical substances from a {@link CDDocument}.
 *
 * <p>The {@code SubstanceXtractor} traverses ChemDraw pages and fragments, converting them into
 * {@link BCXSubstance} objects with associated chemical information such as SMILES, InChI,
 * InChIKey, molecular formula, and occurrences in the document.
 */
public class SubstanceXtractor {

  private final IChemObjectBuilder builder;
  private static final Log logger = LogFactory.getLog(SubstanceXtractor.class);

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
      substanceInfo.setNoFragments(fragments.size());
      for (CDFragment fragment : fragments) {
        try {
          substances.addAll(xtractSubstances(fragment, page, resolveRGroups));
        } catch (IOException | CDKException e) {
          logger.error("Could not extract structures from fragment.");
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
    List<BCXSubstance> substances = xtractSubstances(fragment, page, false);
    if (!substances.isEmpty()) return substances.get(0);
    else return null;
  }

  /**
   * Extracts all {@link BCXSubstance} objects from a fragment.
   *
   * <p>Handles multi-atom groups, R-groups (if requested), and adds fragment occurrences and
   * abbreviations.
   *
   * @param fragment the {@link CDFragment} to process
   * @param page the {@link CDPage} containing the fragment
   * @param resolveRGroups whether to resolve R-groups
   * @return a list of extracted {@link BCXSubstance} objects
   * @throws IOException if IO operations fail
   * @throws CDKException if CDK operations fail
   */
  protected List<BCXSubstance> xtractSubstances(
      CDFragment fragment, CDPage page, boolean resolveRGroups) throws IOException, CDKException {
    Objects.requireNonNull(fragment, "Fragment must not be null.");
    List<BCXSubstance> substances = new ArrayList<>();

    // validate fragment
    if (!fragment.isValid()) {
      logger.info("Fragment validation failed: The fragment has one or zero atoms.");
      return substances;
    }

    SgroupHandler.addMultipleGroupBrackets(fragment, page);

    FragmentConverter fragmentConverter = new FragmentConverter(this.builder);
    IAtomContainer atomContainer = null;
    try {
      atomContainer = fragmentConverter.convert(fragment);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
      return substances;
    }

    MarkushHandler markushHandler = new MarkushHandler(page, this.builder);

    try {
      if (resolveRGroups && fragment.hasRGroup() && !markushHandler.getResidueLabels().isEmpty()) {
        List<IAtomContainer> atomContainers = markushHandler.replaceRGroups(atomContainer);
        for (IAtomContainer container : atomContainers) {
          BCXSubstance substance = createAndFillBCXSubstance(container);
          Optional<CDRectangle> boundsOptional = Optional.ofNullable(fragment.getBounds());
          // add occurrence
          if (boundsOptional.isPresent()) {
            CDRectangle bounds = boundsOptional.get();
            BCXSubstanceOccurrence occurrence =
                new BCXSubstanceOccurrence(
                    bounds.getTop(), bounds.getLeft(), bounds.getBottom(), bounds.getRight());
            substance.addOccurrence(occurrence);
          }
          addAbbreviations(substance, fragment);
          substances.add(substance);
        }
        return substances;
      }
    } catch (IOException | CloneNotSupportedException e) {
      logger.error(e.getMessage());
    }
    BCXSubstance substance = createAndFillBCXSubstance(atomContainer);
    Optional<CDRectangle> boundsOptional = Optional.ofNullable(fragment.getBounds());
    // add occurrence
    if (boundsOptional.isPresent()) {
      CDRectangle bounds = boundsOptional.get();
      BCXSubstanceOccurrence occurrence =
          new BCXSubstanceOccurrence(
              bounds.getTop(), bounds.getLeft(), bounds.getBottom(), bounds.getRight());
      substance.addOccurrence(occurrence);
    }
    addAbbreviations(substance, fragment);
    substances.add(substance);
    return substances;
  }

  /**
   * Creates and populates a {@link BCXSubstance} from the given {@link IAtomContainer}.
   *
   * @param atomContainer the CDK atom container
   * @return a {@link BCXSubstance}
   * @throws CDKException if InChI generation fails
   */
  private BCXSubstance createAndFillBCXSubstance(IAtomContainer atomContainer) throws CDKException {
    // create and fill BCXSubtances
    BCXSubstance substance = new BCXSubstance();
    // add AtomContainer
    substance.setAtomContainer(atomContainer);
    // set SMILES
    String smiles =
        Optional.ofNullable(ChemicalUtils.createAbsoluteSmiles(atomContainer))
            .orElseGet(() -> ChemicalUtils.createSmiles(atomContainer, SmiFlavor.Canonical));
    substance.setSmiles(smiles);
    substance.setExtendedSmiles(ChemicalUtils.createExtendedSmiles(atomContainer));
    // set InChI, InChIKey and AuxInfo
    InChIGenerator gen = ChemicalUtils.getInChI(atomContainer);
    substance.setInchi(gen.getInchi());
    substance.setInchiKey(gen.getInchiKey());
    if (gen.getAuxInfo().length() < 4000) {
      substance.setAuxInfo(gen.getAuxInfo());
    }
    // set molecular formula
    IMolecularFormula molecularFormula =
        MolecularFormulaManipulator.getMolecularFormula(atomContainer);
    substance.setMolecularFormula(MolecularFormulaManipulator.getString(molecularFormula));
    return substance;
  }

  /**
   * Adds abbreviations and nicknames from the fragment to the substance.
   *
   * @param substance the {@link BCXSubstance} to populate
   * @param fragment the {@link CDFragment} containing abbreviations/nicknames
   * @throws IOException if fragment conversion fails
   */
  private void addAbbreviations(BCXSubstance substance, CDFragment fragment) throws IOException {
    FragmentConverter fragmentConverter = new FragmentConverter(builder);

    for (String abbreviation : fragmentConverter.getAbbreviations(fragment)) {
      String smiles = SmilesAbbreviations.get(abbreviation);
      if (smiles == null) {
        logger.info("No SMILES found for: " + abbreviation);
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
        nestedAc = fragmentConverter.convert(nested);
      } catch (CDKException e) {
        logger.error("Nested fragment could not be converted: " + nickname);
        continue;
      }
      substance.addAbbreviation(ChemicalUtils.createAbsoluteSmiles(nestedAc), nickname);
    }
  }
}
