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
package org.beilstein.chemxtract.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.datatypes.CDRadical;
import org.beilstein.chemxtract.lookups.SmilesAbbreviations;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.utils.StereoHandler;
import org.beilstein.chemxtract.visitor.AtomVisitor;
import org.beilstein.chemxtract.visitor.BondVisitor;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.IOException;
import java.util.*;

/**
 * Converts a {@link CDFragment} (e.g., a ChemDraw fragment) into a
 * CDK {@link IAtomContainer}.
 *
 * <p>
 * The {@code FragmentConverter} serves as the top-level converter for ChemDraw
 * fragments. It delegates conversion of atoms and bonds to
 * {@link AtomConverter} and {@link BondConverter} respectively,
 * assembles the resulting {@link IAtomContainer}, resolves abbreviations
 * (e.g., "Ph", "Me", "Et"), and performs post-processing such as
 * radical detection, implicit hydrogen addition, and stereochemistry
 * perception.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
 * FragmentConverter converter = new FragmentConverter(builder);
 * IAtomContainer molecule = converter.convert(cdFragment);
 * }</pre>
 */
public class FragmentConverter {

  private final IChemObjectBuilder builder;
  private final IChemObjectReader.Mode mode;
  private final SmilesParser smilesParser;
  private static final Log logger = LogFactory.getLog(FragmentConverter.class);

  /**
   * Constructs a new {@code FragmentConverter} using the given
   * {@link IChemObjectBuilder} and reader mode.
   *
   * @param builder the CDK {@link IChemObjectBuilder} used to create new instances
   * @param mode the conversion mode (e.g., {@link IChemObjectReader.Mode#STRICT} or
   *             {@link IChemObjectReader.Mode#RELAXED})
   */
  public FragmentConverter(IChemObjectBuilder builder, IChemObjectReader.Mode mode) {
    this.builder = builder;
    this.mode = mode;
    this.smilesParser = new SmilesParser(this.builder);
  }

  /**
   * Constructs a new {@code FragmentConverter} using the given builder and
   * {@link IChemObjectReader.Mode#RELAXED} as the default mode.
   *
   * @param builder the CDK {@link IChemObjectBuilder}
   */
  public FragmentConverter(IChemObjectBuilder builder) {
    this(builder, IChemObjectReader.Mode.RELAXED);
  }


  /**
   * Converts a ChemDraw {@link CDFragment} into a CDK {@link IAtomContainer}.
   *
   * <p>
   * This method performs full reconstruction of the molecular graph:
   * it converts atoms and bonds, handles pseudoatoms and abbreviations,
   * and configures radicals, atom types, hydrogens, and stereo elements.
   * </p>
   *
   * @param fragment the ChemDraw fragment to convert
   * @return the converted {@link IAtomContainer} representation
   * @throws CDKException if a conversion error occurs or invalid data is encountered
   */
  public IAtomContainer convert(CDFragment fragment) throws CDKException {
    Objects.requireNonNull(fragment, "Instance of CDFragment must not be null.");

    // check for and "clean" dot allene
    checkForDotAllene(fragment);

    // get atoms
    AtomVisitor atomVisitor = new AtomVisitor(fragment);
    List<CDAtom> cdAtoms = atomVisitor.getAtoms();

    // get bonds
    BondVisitor bondVisitor = new BondVisitor(fragment);
    List<CDBond> cdBonds = bondVisitor.getBonds();

    // convert CDAtoms to IAtoms
    AtomConverter atomConverter = new AtomConverter(builder, mode);
    List<IAtom> atoms = new ArrayList<>(cdAtoms.size());
    for (CDAtom cdAtom : cdAtoms) {
      atoms.add(atomConverter.convert(cdAtom));
    }
    // convert CDBonds to IBonds
    BondConverter bondConverter = new BondConverter(builder, atomConverter.getAtomMap());
    List<IBond> bonds = new ArrayList<>(cdBonds.size());
    for (CDBond cdBond : cdBonds) {
      bonds.add(bondConverter.convert(cdBond));
    }
    // create IAtomContainer
    IAtomContainer atomContainer = createAtomContainer(atoms.toArray(IAtom[]::new), bonds.toArray(IBond[]::new));
    // check for radicals
    setRadicals(atomContainer, atomConverter.getAtomMap());
    // add implicit hydrogens
    addImplicitHydrogens(atomContainer);
    // perceive atom types and configure atoms
    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
    // check for tetrahedral stereo
    StereoHandler.setStereo(atomContainer, bondConverter.getBondMap(), atomConverter.getAtomMap());
    return atomContainer;
  }

  /**
   * Extracts all chemical abbreviations used within the given fragment.
   *
   * @param fragment the {@link CDFragment} to inspect
   * @return a set of abbreviation labels (e.g., "Ph", "Et", "tBu")
   */
  public Set<String> getAbbreviations(CDFragment fragment) {
    return new AtomVisitor(fragment).getAbbreviations();
  }

  /**
   * Returns a mapping of nickname labels to corresponding
   * {@link CDFragment} definitions contained in the given fragment.
   *
   * @param fragment the {@link CDFragment} to inspect
   * @return a map of nicknames to {@link CDFragment} structures
   */
  public Map<String,CDFragment> getNicknames(CDFragment fragment) {
    return new AtomVisitor(fragment).getNicknames();
  }

  /**
   * Checks the given fragment for “dot allene” artifacts
   * (central carbon drawn as a dot) and corrects them by converting
   * the dot node to a proper carbon element.
   *
   * @param fragment the {@link CDFragment} to clean
   */
  private void checkForDotAllene(CDFragment fragment) {
    fragment.getAtoms().stream()
            .filter(this::isDotAlleneCandidate)
            .forEach(dot -> {
              List<CDBond> connectedBonds = getConnectedBonds(fragment, dot);
              if (formsAllene(connectedBonds)) {
                dot.setNodeType(CDNodeType.Element);
                dot.setText(null);
              }
            });
  }

  /**
   * Creates a new {@link IAtomContainer} from the provided atoms and bonds.
   * Performs a post-processing step to re-substitute abbreviations
   * into full structures.
   *
   * @param atoms array of {@link IAtom}
   * @param bonds array of {@link IBond}
   * @return the assembled {@link IAtomContainer}
   */
  private IAtomContainer createAtomContainer(IAtom[] atoms, IBond[] bonds) {
    IAtomContainer atomContainer = builder.newAtomContainer();
    atomContainer.setAtoms(atoms);
    atomContainer.setBonds(bonds);
    resubstituteAbbreviation(atomContainer);
    return atomContainer;
  }

  /**
   * Expands pseudoatom abbreviations (e.g., "Ph", "Ac") into explicit
   * substructures defined in {@link SmilesAbbreviations}.
   * <p>
   *
   * @param atomContainer the container in which abbreviations are replaced
   */
  private void resubstituteAbbreviation(IAtomContainer atomContainer) {
    List<IBond> bondsToRemove = new ArrayList<>();
    List<IAtom> atomsToRemove = new ArrayList<>();

    for (IAtom atom : atomContainer.atoms()) {
      if (!(atom instanceof IPseudoAtom pseudoAtom)) {
        continue; // only process pseudo atoms
      }

      String abbreviation = pseudoAtom.getLabel().toLowerCase();
      String smiles;
      try {
        smiles = SmilesAbbreviations.get(abbreviation);
      } catch (IOException e) {
        logger.error("Could not lookup SMILES for: " + abbreviation);
        continue;
      }
      if (smiles == null) {
        continue; // unknown abbreviation
      }

      IAtomContainer expandedStructure;
      try {
        expandedStructure = smilesParser.parseSmiles(smiles);
      } catch (InvalidSmilesException e) {
        logger.error("SMILES could not be parsed to AtomContainer: " +
                abbreviation + ": " + smiles);
        continue;
      }

      if (expandedStructure.getAtomCount() == 1) {
        replaceSingleAtom(atomContainer, pseudoAtom, expandedStructure.getAtom(0), atomsToRemove);
      } else {
        replaceMultiAtom(atomContainer, pseudoAtom, expandedStructure, bondsToRemove, atomsToRemove);
      }
    }
    bondsToRemove.forEach(atomContainer::removeBond);
    atomsToRemove.forEach(atomContainer::removeAtom);
  }

  /**
   * Replaces the given single R-Atom with the given new IAtom in the IAtomContainer and adds the R-Atom to the list
   * of atoms to be removed
   *
   * @param atomContainer IAtomContainer
   * @param pseudoAtom abbreviation/R atom (IAtom) to be replaced
   * @param newAtom IAtom to replace the rAtom
   * @param atomsToRemove list of atoms that will be removed from the IAtomContainer
   */
  private void replaceSingleAtom(IAtomContainer atomContainer, IAtom pseudoAtom, IAtom newAtom, List<IAtom> atomsToRemove) {
    atomContainer.addAtom(newAtom);

    List<IBond> connectedBonds = new ArrayList<>();
    pseudoAtom.bonds().forEach(connectedBonds::add);
    connectedBonds.forEach(bond ->
            bond.setAtoms(new IAtom[] { bond.getOther(pseudoAtom), newAtom }));
    newAtom.setValency(connectedBonds.size());
    newAtom.setImplicitHydrogenCount(Math.max(newAtom.getImplicitHydrogenCount() - connectedBonds.size(), 0));

    atomsToRemove.add(pseudoAtom);
  }

  /**
   * Replaces and reconnects the given IAtomContainer parsed from a SMILES with the residue IAtoms in the original
   * IAtomContainer.
   *
   * @param atomContainer IAtomContainer
   * @param pseudoAtom List of residue IAtoms
   * @param expandedStructure IAtomContainer of the structure parsed from the abbreviation SMILES
   * @param bondsToRemove list of bonds that will be removed from the IAtomContainer
   * @param atomsToRemove list of atoms that will be removed from the IAtomContainer
   */
  private void replaceMultiAtom(IAtomContainer atomContainer, IAtom pseudoAtom, IAtomContainer expandedStructure, List<IBond> bondsToRemove,
          List<IAtom> atomsToRemove) {
    List<IAtom> connectionPoints = new ArrayList<>();
    for (IAtom atom : expandedStructure.atoms()) {
      if (atom instanceof IPseudoAtom) {
        connectionPoints.add(atom);
      }
    }
    if (connectionPoints.size() != 1) {
      throw new IllegalStateException("Expected exactly one connection point for abbreviation: " + pseudoAtom);
    }
    IAtom connectionPoint = connectionPoints.get(0);
    // Find bond between pseudoAtom and its origin
    IBond bondOrigin = pseudoAtom.bonds().iterator().next();
    IAtom originAtom = bondOrigin.getOther(pseudoAtom);
    // Find bond inside abbreviation connecting to connection point
    IBond bondInsideAbbr = connectionPoint.bonds().iterator().next();
    IAtom atomInsideAbbr = bondInsideAbbr.getOther(connectionPoint);
    // Reconnect: origin to abbreviation atom
    IBond newBond;
    try {
      newBond = bondOrigin.clone();
    } catch (CloneNotSupportedException e) {
      logger.error("Bond could not be cloned.");
      return;
    }
    newBond.setAtoms(new IAtom[] { originAtom, atomInsideAbbr });

    atomContainer.add(expandedStructure);
    atomContainer.addBond(newBond);

    bondsToRemove.add(bondOrigin);
    bondsToRemove.add(bondInsideAbbr);
    atomsToRemove.add(pseudoAtom);
    atomsToRemove.add(connectionPoint);
  }

  /**
   * Adds implicit hydrogens accordingly. It does not create 2D or 3D
   * coordinates for the new hydrogens.
   *
   * @param container to which implicit hydrogens are added.
   * Copied from CDK
   */
  private void addImplicitHydrogens(IAtomContainer container) {
    try {
      CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(container.getBuilder());
      int atomCount = container.getAtomCount();
      String[] originalAtomTypeNames = new String[atomCount];
      for (int i = 0; i < atomCount; i++) {
        IAtom atom = container.getAtom(i);
        if (atom instanceof IPseudoAtom)
          atom.setImplicitHydrogenCount(0);
        IAtomType type = matcher.findMatchingAtomType(container, atom);
        originalAtomTypeNames[i] = atom.getAtomTypeName();
        atom.setAtomTypeName(type.getAtomTypeName());
      }
      CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(container.getBuilder());
      hAdder.addImplicitHydrogens(container);
      // reset to the original atom types
      for (int i = 0; i < atomCount; i++) {
        IAtom atom = container.getAtom(i);
        atom.setAtomTypeName(originalAtomTypeNames[i]);
      }
    } catch (CDKException e) {
      logger.error("Unable to add implicit hydrogens, due to: " + e.getMessage());
    }
  }

  /**
   * Determines whether a set of bonds forms an allene structure,
   * i.e. a carbon atom connected to exactly two double bonds.
   *
   * @param bonds the list of bonds connected to a carbon atom
   * @return {@code true} if the bonds form an allene, otherwise {@code false}
   */
  private boolean formsAllene(List<CDBond> bonds) {
    return bonds.size() == 2 &&
            bonds.stream().allMatch(b -> b.getBondOrder().equals(CDBondOrder.Double));
  }

  /**
   * Retrieves all bonds in the fragment that are connected to a given atom.
   *
   * @param fragment the fragment to inspect
   * @param atom the atom whose bonds should be found
   * @return a list of connected {@link CDBond}
   */
  private List<CDBond> getConnectedBonds(CDFragment fragment, CDAtom atom) {
    return fragment.getBonds().stream()
            .filter(b -> b.getBegin().equals(atom) || b.getEnd().equals(atom))
            .toList();
  }

  /**
   * Checks whether a ChemDraw atom represents a “dot allene” candidate,
   * i.e. a carbon atom with a single “.” label.
   *
   * @param candidate the candidate {@link CDAtom}
   * @return {@code true} if the atom is a dot allene candidate
   */
  private boolean isDotAlleneCandidate(CDAtom candidate) {
    return candidate.getElementNumber() == 6 &&
            Optional.ofNullable(candidate.getText())
                    .map(t -> t.getText().getText())
                    .filter("."::equals)
                    .isPresent();
  }

  /**
   * Sets radicals on atoms according to the {@link CDRadical}
   * information encoded in the ChemDraw fragment.
   * <p>
   *
   * @param atomContainer the {@link IAtomContainer} to modify
   * @param atomMap the mapping from {@link CDAtom} to {@link IAtom}
   */
  private void setRadicals(IAtomContainer atomContainer, Map<CDAtom,IAtom> atomMap) {
    try {
      List<CDAtom> cdRadicals = atomMap.keySet().stream().filter(a -> !CDRadical.None.equals(a.getRadical())).toList();
      for (CDAtom cdRadical : cdRadicals) {
        IAtom radical = atomMap.get(cdRadical);
        int rad = convertRadical(cdRadical.getRadical());
        MDLV2000Writer.SPIN_MULTIPLICITY spin = MDLV2000Writer.SPIN_MULTIPLICITY.ofValue(rad);
        for (int i = 0; i < spin.getSingleElectrons(); i++) {
          atomContainer.addSingleElectron(this.builder.newInstance(ISingleElectron.class, radical));
        }
      }
    } catch (CDKException e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Converts a {@link CDRadical} value to its integer spin multiplicity.
   *
   * @param cdRadical the {@link CDRadical} enumeration value
   * @return the spin multiplicity (1 = singlet, 2 = doublet, 3 = triplet)
   */
  private int convertRadical(CDRadical cdRadical) {
    switch (cdRadical) {
      case Singlet -> {
        return 1;
      }
      case Doublet -> {
        return 2;
      }
      case Triplet -> {
        return 3;
      }
      default -> {
        return 0;
      }
    }
  }
}
