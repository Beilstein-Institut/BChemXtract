package org.beilstein.chemxtract.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.lookups.SmilesAbbreviations;
import org.beilstein.chemxtract.visitor.TextVisitor;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.IOException;
import java.util.*;

/**
 * Class for handling Markush structures and replacing R-groups in molecules.
 * <p>
 * This class processes {@link IAtomContainer} instances containing pseudo-atoms (R-groups)
 * and generates all possible structures by replacing R-groups with their corresponding
 * substituents defined in the residue labels.
 * </p>
 * <p>
 * The replacement handles single-bonded residues as well as dual-bonded residues,
 * reconnecting the generated structures properly.
 * </p>
 *
 * <h2>Example usage:</h2>
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

  private static final Log logger = LogFactory.getLog(MarkushHandler.class);
  private final Map<String, List<String>> residueLabels;
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
    smilesParser = new SmilesParser(builder);
  }

  /**
   * Generates all possible IAtomContainer structures by replacing R-groups
   * in the given atom container with their substituents.
   *
   * @param atomContainer molecule containing pseudo-atoms (R-groups)
   * @return list of all substituted atom containers
   * @throws CloneNotSupportedException if atom container cloning fails
   * @throws IOException if reading SMILES definitions fails
   * @throws InvalidSmilesException if a SMILES string is invalid
   */
  public List<IAtomContainer> replaceRGroups(IAtomContainer atomContainer)
          throws CloneNotSupportedException, IOException, CDKException {

    List<IAtomContainer> atomContainers = new ArrayList<>();

    Set<String> atomContainerLabels = new HashSet<>();
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom pseudoAtom && Definitions.RGROUP_LABEL_PATTERN.matcher(pseudoAtom.getLabel()).find()) {
        atomContainerLabels.add(pseudoAtom.getLabel());
      }
    }
    // if atom container does not contain any rgroup return
    if (atomContainerLabels.isEmpty()) return List.of(atomContainer);

    // create a new map with all possible combinations of residue labels and atom container labels
     // key: label, value: smiles

    Map<String, List<String>> filteredLabels = filterRelevantRGroups(atomContainer, residueLabels);
    List<Map<String, String>> combinations = generateCombinations(filteredLabels);


    for (Map<String, String> combination : combinations) {
      IAtomContainer clone = atomContainer.clone();

      for (Map.Entry<String, String> entry : combination.entrySet()) {
        String rLabel = entry.getKey();
        String definition = entry.getValue();
        String smiles = SmilesAbbreviations.contains(definition) ? SmilesAbbreviations.get(definition) : definition;
        replaceRGroup(clone, rLabel, smiles);
      }
      atomContainers.add(clone);

    }


    return atomContainers;
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
  private void backtrack(Map<String, List<String>> residueLabels,
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
      backtrack(residueLabels, rLabels, index+1, current, results);
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
          IAtomContainer atomContainer,
          Map<String, List<String>> definitions) {

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
   * Replaces a single R-group in the atom container with the structure defined by the SMILES string.
   *
   * @param atomContainer molecule to modify
   * @param residueKey label of the R-group to replace
   * @param smiles SMILES string defining the substituent
   * @throws InvalidSmilesException if SMILES parsing fails
   * @throws CloneNotSupportedException if cloning fails
   */
  private void replaceRGroup(IAtomContainer atomContainer, String residueKey, String smiles) throws
          CDKException, CloneNotSupportedException {
    IAtomContainer extendedStructure = smilesParser.parseSmiles(smiles);
    AtomContainerManipulator.suppressHydrogens(extendedStructure);
    long nStars = smiles.chars().filter(c -> '*' == c).count();
    if (nStars == 2){
      replaceDualBondedResidue(atomContainer, extendedStructure, residueKey);
    } else if(nStars == 1) {
      replaceSingleBondedResidue(atomContainer, extendedStructure, residueKey);
    } else {
      logger.info("Unlinkable residue found: " + smiles);
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
  private void replaceSingleBondedResidue (IAtomContainer atomContainer, IAtomContainer extendedStructure, String residueKey)
          throws CloneNotSupportedException {
    List<IBond> bondsToRemove = new ArrayList<>();
    List<IAtom> atomsToRemove = new ArrayList<>();
    for (IAtom atom : atomContainer.atoms()) {
      if (!(atom instanceof IPseudoAtom pseudoAtom)) continue;
      if (!residueKey.equals(pseudoAtom.getLabel())) continue;
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
  private void replaceDualBondedResidue(IAtomContainer atomContainer, IAtomContainer extendedStructure, String residueKey)
          throws CloneNotSupportedException {
    Set<IAtom> visitedAtoms = new HashSet<>();
    List<IBond> bondsToRemove = new ArrayList<>();
    List<IAtom> atomsToRemove = new ArrayList<>();

    for (IAtom atom : atomContainer.atoms()) {
      if (!(atom instanceof IPseudoAtom pseudoAtom)) continue;
      if (!residueKey.equals(pseudoAtom.getLabel())) continue;
      if (!visitedAtoms.add(pseudoAtom)) continue; // already processed
      IAtomContainer extendedClone = extendedStructure.clone();

      IAtom nearestOtherResidue = ChemicalUtils.findNearestResidueAtom(pseudoAtom, atomContainer);
      visitedAtoms.add(nearestOtherResidue);

      reconnectResidue(atomContainer, extendedClone, List.of(pseudoAtom, nearestOtherResidue), bondsToRemove, atomsToRemove);
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
  private void reconnectResidue(IAtomContainer atomContainer, IAtomContainer extendedStructure, List<IAtom> pseudoAtoms,
          List<IBond> bondsToRemove, List<IAtom> atomsToRemove) {
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
   * Returns a map of residue labels extracted from the page.
   *
   * @return map of R-group labels and their corresponding substituents
   */
  public Map<String, List<String>> getResidueLabels() {
    return residueLabels;
  }
}
