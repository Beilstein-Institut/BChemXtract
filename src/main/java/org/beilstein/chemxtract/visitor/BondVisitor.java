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
package org.beilstein.chemxtract.visitor;

import java.io.IOException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.lookups.UnwantedAbbreviations;
import org.beilstein.chemxtract.utils.Definitions;

/** Visitor class for traversing a ChemDraw fragment and collecting bond-related information. */
public class BondVisitor extends CDVisitor {

  private final List<CDBond> bonds;
  private final Set<CDBond> skip;
  private static final Log logger = LogFactory.getLog(BondVisitor.class);

  /**
   * Constructs a {@code BondVisitor} and immediately traverses the provided fragment to collect
   * relevant bonds.
   *
   * @param fragment the {@link CDFragment} to traverse
   */
  public BondVisitor(CDFragment fragment) {
    bonds = new ArrayList<>();
    skip = new HashSet<>();
    fragment.accept(this);
  }

  /**
   * Visits a {@link CDBond} node during the fragment traversal.
   *
   * @param bond the {@link CDBond} node being visited
   */
  @Override
  public void visitBond(CDBond bond) {

    // if one of the bonded atoms is fragment, add bonds of fragment and reconnect fragment to
    // structure
    if (hasNestedFragment(bond)) {
      CDFragment fragment = getNestedFragment(bond);
      // if nested fragment is unwanted abbreviation skip all nested bonds
      if (isNestedFragmentUnwantedAbbreviation(bond)) {
        skip.addAll(fragment.getBonds());
      } else {
        CDAtom extCon =
            fragment.getAtoms().stream()
                .filter(a -> CDNodeType.ExternalConnectionPoint.equals(a.getNodeType()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Missing external connection point in fragment: " + fragment));

        CDAtom conAtom = resolveConnectionAtom(fragment, extCon);
        if (!bond.getBegin().getFragments().isEmpty()) bond.setBegin(conAtom);
        else bond.setEnd(conAtom);
      }
    }
    if ((onlyElementsAtBond(bond)
            || isRGroupBond(bond)
            || isMultiAttachmentBond(bond)
            || isAbbreviationAtBond(bond))
        && !skip.contains(bond)) bonds.add(bond);
  }

  /**
   * Checks if the bond connects only standard element atoms.
   *
   * @param bond the bond to check
   * @return true if both ends of the bond are element atoms, false otherwise
   */
  private boolean onlyElementsAtBond(CDBond bond) {
    return CDNodeType.Element.equals(bond.getEnd().getNodeType())
        && CDNodeType.Element.equals(bond.getBegin().getNodeType());
  }

  /**
   * Checks if the bond involves any abbreviations (unspecified nodes with chemical warnings).
   *
   * @param bond the bond to check
   * @return true if one or both ends are abbreviations, false otherwise
   */
  private boolean isAbbreviationAtBond(CDBond bond) {
    return (!CDNodeType.Element.equals(bond.getBegin().getNodeType())
            && bond.getBegin().getChemicalWarning() != null)
        || (!CDNodeType.Element.equals(bond.getEnd().getNodeType())
            && bond.getEnd().getChemicalWarning() != null);
  }

  /**
   * Checks if the bond involves any R-group labeled atoms.
   *
   * @param bond the bond to check
   * @return true if one or both ends are R-group labeled atoms, false otherwise
   */
  private boolean isRGroupBond(CDBond bond) {
    return (bond.getBegin().getText() != null
            && bond.getBegin()
                .getText()
                .getText()
                .getText()
                .matches(Definitions.RGROUP_LABEL_STRING))
        || (bond.getEnd().getText() != null
            && bond.getEnd()
                .getText()
                .getText()
                .getText()
                .matches(Definitions.RGROUP_LABEL_STRING));
  }

  /**
   * Resolves the connection atom for a nested fragment's external atom.
   *
   * @param fragment the nested fragment
   * @param external the external connection atom
   * @return the atom in the fragment connected to the external atom
   */
  private CDAtom resolveConnectionAtom(CDFragment fragment, CDAtom external) {
    CDBond exBond =
        fragment.getBonds().stream()
            .filter(b -> external.equals(b.getBegin()) || external.equals(b.getEnd()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No bond connected to external atom: " + external));
    return external.equals(exBond.getBegin()) ? exBond.getEnd() : exBond.getBegin();
  }

  /**
   * Checks if the bond is connected to a nested fragment.
   *
   * @param bond the bond to check
   * @return true if either end has nested fragments, false otherwise
   */
  private boolean hasNestedFragment(CDBond bond) {
    return (bond.getBegin() != null && !bond.getBegin().getFragments().isEmpty())
        || (bond.getEnd() != null && !bond.getEnd().getFragments().isEmpty());
  }

  private boolean isNestedFragmentUnwantedAbbreviation(CDBond bond) {
    String textBegin =
        Optional.ofNullable(bond.getBegin().getText())
            .map(CDText::getText)
            .map(CDStyledString::getText)
            .orElseGet(() -> bond.getBegin().getLabelText());

    String textEnd =
        Optional.ofNullable(bond.getEnd().getText())
            .map(CDText::getText)
            .map(CDStyledString::getText)
            .orElseGet(() -> bond.getEnd().getLabelText());

    try {
      return (UnwantedAbbreviations.contains(textBegin) || UnwantedAbbreviations.contains(textEnd));
    } catch (IOException e) {
      logger.error("Unable to load unwanted abbreviations: " + e.getMessage());
    }
    return false;
  }

  /**
   * Checks if the bond is connected to a multi attachment point.
   *
   * @param bond the bond to check
   * @return true if either end is multi attachment, false otherwise.
   */
  private boolean isMultiAttachmentBond(CDBond bond) {
    return CDNodeType.MultiAttachment.equals(bond.getBegin().getNodeType())
        || CDNodeType.MultiAttachment.equals(bond.getEnd().getNodeType());
  }

  /**
   * Returns the nested fragment associated with a bond.
   *
   * @param bond the bond that contains a nested fragment
   * @return the first fragment of the bonded atom that contains fragments
   */
  private CDFragment getNestedFragment(CDBond bond) {
    return !bond.getBegin().getFragments().isEmpty()
        ? bond.getBegin().getFragments().get(0)
        : bond.getEnd().getFragments().get(0);
  }

  /**
   * Returns the list of {@link CDBond} objects collected during the visit.
   *
   * @return list of collected bonds
   */
  public List<CDBond> getBonds() {
    return bonds;
  }
}
