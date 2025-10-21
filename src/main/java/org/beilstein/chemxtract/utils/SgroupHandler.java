package org.beilstein.chemxtract.utils;

import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.visitor.BracketVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling S-groups (repeating or bracketed groups) within chemical fragments.
 * <p>
 * This class provides methods to duplicate and connect atoms from multiple-atom or single-atom
 * brackets in a {@link CDFragment} based on the structure information in a {@link CDPage}.
 * It ensures proper connectivity between repeated units, handles crossing bonds, and
 * reconstructs internal structures for multiple repetitions.
 * </p>
 */
public class SgroupHandler {

  private SgroupHandler() {
    // private constructor to hide implicit public one
  }

  /**
   * Adds and connects atoms from multiple-atom groups (brackets) to the given fragment.
   * <p>
   * This method scans the specified {@link CDPage} for all multiple-group brackets using a
   * {@link BracketVisitor}. For each bracket, it collects the contained {@link CDAtom} instances
   * and integrates them into the provided {@link CDFragment} if they are relevant.
   * </p>
   *
   * @param fragment the fragment to which the bracketed atoms will be added and connected
   * @param page the page containing the chemical structure and bracket information
   */
  public static void addMultipleGroupBrackets(CDFragment fragment, CDPage page) {
    BracketVisitor bracketVisitor = new BracketVisitor(page);
    List<CDBracket> brackets = bracketVisitor.getMultipleGroups();

    for (CDBracket bracket : brackets) {
      // Collect bracketed atoms
      List<CDAtom> bracketAtoms = bracket.getBracketedObjects().stream()
              .filter(CDAtom.class::isInstance)
              .map(CDAtom.class::cast)
              .toList();

      if (!bracketAtoms.isEmpty() && !fragment.getAtoms().contains(bracketAtoms.get(0)))
        continue;



      // Handle single atom or multiple atom groups
      if (bracketAtoms.size() == 1) {
        addAndConnectSingleMultipleGroupAtom(fragment, bracket, bracketAtoms.get(0));
      } else if (!bracketAtoms.isEmpty()) {
        addAndConnectMultipleGroupStructure(fragment, bracket, bracketAtoms);
      }
    }
  }

  /**
   * Creates and connects a single copy of a multiple group atom within the specified fragment.
   * The method duplicates the given atom, connects it to the original atom via a new bond,
   * and updates the provided bond to reconnect with the new atom.
   *
   * @param fragment       The {@code CDFragment} to which the atom and bonds will be added.
   * @param bracket        The {@code CDBracket} defining the multiple group structure, including
   *                       its repeat count and attachments.
   * @param atom           The {@code CDAtom} to be duplicated and connected.
   */
  private static void addAndConnectSingleMultipleGroupAtom(CDFragment fragment, CDBracket bracket, CDAtom atom) {
    CDBond crossingBond = bracket.getBracketAttachments().stream()
            .filter(a -> !a.getCrossingBonds().isEmpty())
            .findFirst()                                    // Optional<CDBracketAttachment>
            .flatMap(a -> a.getCrossingBonds().stream()
                    .findFirst()                     // Optional<CrossingBond>
                    .map(CDCrossingBond::getBond))      // Optional<CDBond>
            .orElse(null);

    if (crossingBond == null)
      return;

    int repeatCount = (int) bracket.getRepeatCount();
    CDAtom atom1 = atom;


    for (int i = 0; i < repeatCount - 1; i++) { //repeat count -1, as the multiple group already exists once
      CDAtom atom2 = new CDAtom(atom1);
      fragment.getAtoms().add(atom2);
      CDBond newBond = new CDBond(crossingBond);
      newBond.setBegin(atom1);
      newBond.setEnd(atom2);
      fragment.getBonds().add(newBond);
      atom1 = atom2;
      if (i == repeatCount - 2) {
        if (crossingBond.getBegin().equals(atom))
          crossingBond.setBegin(atom2);
        else
          crossingBond.setEnd(atom2);
      }
    }
  }

  /**
   * Adds and connects a multiple group structure within a fragment based on the provided bracket
   * and its associated atoms. This method replicates the bonds and atoms within the bracket's
   * multiple group structure for the specified number of repetitions, while ensuring proper
   * connectivity within the fragment. Additionally, internal connections for the multiple group
   * are reconstructed if the bracket has attachments with crossing bonds.
   *
   * @param fragment      The {@code CDFragment} to which the multiple group structure will be added.
   * @param bracket       The {@code CDBracket} defining the multiple group structure, including
   *                      its repeat count and attachments.
   * @param bracketAtoms  A list of {@code CDAtom} objects that are part of the bracket's structure.
   */
  private static void addAndConnectMultipleGroupStructure(CDFragment fragment, CDBracket bracket, List<CDAtom> bracketAtoms) {
    // Collect bonds that are part of the multiple group
    List<CDBond> bracketBonds = fragment.getBonds().stream()
            .filter(bond -> bracketAtoms.contains(bond.getBegin()) && bracketAtoms.contains(bond.getEnd()))
            .toList();

    int repeatCount = (int) bracket.getRepeatCount();

    for (int i = 0; i < repeatCount - 1; i++) { //repeat count -1, as the multiple group already exists once
      // Map to ensure unique copies of atoms
      Map<CDAtom,CDAtom> atomMap = new HashMap<>();

      // Copy bonds and corresponding atoms
      for (CDBond bond : bracketBonds) {
        CDAtom copyBegin = atomMap.computeIfAbsent(bond.getBegin(), CDAtom::new);
        CDAtom copyEnd = atomMap.computeIfAbsent(bond.getEnd(), CDAtom::new);

        CDBond copyBond = new CDBond(bond);
        copyBond.setBegin(copyBegin);
        copyBond.setEnd(copyEnd);

        fragment.getBonds().add(copyBond);
      }
      fragment.getAtoms().addAll(atomMap.values());

      // Reconnect internal multiple group if the bracket has attachments.
      // Bracket must have an even number of crossing bonds, i.e. zero or two.
      // If the bracket has no crossing bonds, the multiple group is just a copy of itself and
      // does not need to be reconnected.
      if (bracket.getBracketAttachments().size() == 2
              && bracket.getBracketAttachments().stream().noneMatch(b -> b.getCrossingBonds().isEmpty())) {
        reconnectInternalMultipleGroup(fragment, bracket, atomMap);
      }
    }
  }

  /**
   * Reconnects the internal structure of a multiple group within a fragment by adjusting
   * and duplicating the crossing bonds to link the copied atoms back to the original structure.
   *
   * @param fragment The {@code CDFragment} where the multiple group structure exists.
   * @param bracket  The {@code CDBracket} representing the multiple group.
   * @param atomMap  A mapping of original atoms to their corresponding copied atoms.
   */
  private static void reconnectInternalMultipleGroup(CDFragment fragment, CDBracket bracket, Map<CDAtom,CDAtom> atomMap) {
    // Identify the first and last bonds as connection points
    CDBond firstBond = bracket.getBracketAttachments().get(0).getCrossingBonds().get(0).getBond();
    CDBond lastBond = bracket.getBracketAttachments().get(1).getCrossingBonds().get(0).getBond();

    // Determine the copied and original atoms involved in the last bond
    CDAtom firstCopyAtom = atomMap.getOrDefault(firstBond.getBegin(), atomMap.get(firstBond.getEnd()));
    CDAtom lastCopyAtom = atomMap.getOrDefault(lastBond.getBegin(), atomMap.get(lastBond.getEnd()));
    CDAtom lastOriginAtom = atomMap.containsKey(lastBond.getBegin()) ? lastBond.getBegin() : lastBond.getEnd();
    CDAtom firstNonMultipleGroupAtom = atomMap.containsKey(lastBond.getBegin()) ? lastBond.getEnd() : lastBond.getBegin();

    // Adjust the original last bond to connect to the first copied atom
    lastBond.setBegin(lastOriginAtom);
    lastBond.setEnd(firstCopyAtom);

    // Create a new bond connecting the last copied atom to the non-multiple-group atom
    CDBond copyLastBond = new CDBond(lastBond);
    copyLastBond.setBegin(lastCopyAtom);
    copyLastBond.setEnd(firstNonMultipleGroupAtom);

    // Add the new bond to the fragment
    fragment.getBonds().add(copyLastBond);
  }
}
