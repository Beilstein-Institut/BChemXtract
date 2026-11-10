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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.lookups.UnwantedAbbreviations;
import org.beilstein.chemxtract.utils.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Visitor class for traversing a ChemDraw fragment and collecting bond-related information. */
public class BondVisitor extends CDVisitor {

  private final List<CDBond> bonds;
  private final Set<CDBond> skip;
  private final Set<CDFragment> labelsChecked = new HashSet<>();
  private static final Logger LOGGER = LoggerFactory.getLogger(BondVisitor.class);
  private boolean rawMode;

  /**
   * Constructs a {@code BondVisitor} and immediately traverses the provided fragment to collect
   * relevant bonds.
   *
   * @param fragment the {@link CDFragment} to traverse
   */
  /**
   * Constructs a {@code BondVisitor} with configurable raw mode and immediately traverses the
   * provided fragment to collect relevant bonds.
   *
   * @param fragment the {@link CDFragment} to traverse
   * @param rawMode {@code true} to enable raw mode for abbreviation processing, {@code false} for
   *     standard bond processing
   */
  public BondVisitor(CDFragment fragment, boolean rawMode) {
    bonds = new ArrayList<>();
    skip = new HashSet<>();
    this.rawMode = rawMode;
    fragment.accept(this);
  }

  /**
   * Constructs a {@code BondVisitor} and immediately traverses the provided fragment to collect
   * relevant bonds. Uses default processing mode (raw mode = false).
   *
   * @param fragment the {@link CDFragment} to traverse
   */
  public BondVisitor(CDFragment fragment) {
    this(fragment, false);
  }

  /**
   * Visits a {@link CDBond} node during the fragment traversal.
   *
   * @param bond the {@link CDBond} node being visited
   */
  @Override
  public void visitBond(CDBond bond) {
    if (rawMode) {
      bonds.add(bond);
    } else {
      // if one of the bonded atoms is fragment, add bonds of fragment and reconnect fragment to
      // structure
      boolean attachesUnwantedAbbreviation = false;
      if (hasNestedFragment(bond)) {
        CDFragment fragment = getNestedFragment(bond);
        // if nested fragment is unwanted abbreviation skip all nested bonds
        if (isNestedFragmentUnwantedAbbreviation(bond)) {
          skip.addAll(fragment.getBonds());
          attachesUnwantedAbbreviation = true;
        } else {
          CDAtom extCon =
              fragment.getAtoms().stream()
                  .filter(a -> CDNodeType.ExternalConnectionPoint.equals(a.getNodeType()))
                  .findFirst()
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "Missing external connection point in fragment with "
                                  + fragment.getAtoms().size()
                                  + " atoms"));

          CDAtom conAtom = resolveConnectionAtom(fragment, extCon);
          CDAtom holder =
              !bond.getBegin().getFragments().isEmpty() ? bond.getBegin() : bond.getEnd();
          if (labelsChecked.add(fragment)) {
            bondTrailingLabel(holder, fragment, extCon, conAtom);
          }
          if (!bond.getBegin().getFragments().isEmpty()) {
            bond.setBegin(conAtom);
          } else {
            bond.setEnd(conAtom);
          }
        }
      }
      if ((onlyElementsAtBond(bond)
              || isRGroupBond(bond)
              || isMultiAttachmentBond(bond)
              || isAbbreviationAtBond(bond)
              || attachesUnwantedAbbreviation)
          && !skip.contains(bond)) {
        bonds.add(bond);
      }
    }
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
            && bond.getEnd().getChemicalWarning() != null)
        || CDNodeType.Fragment.equals(bond.getBegin().getNodeType())
        || CDNodeType.Fragment.equals(bond.getEnd().getNodeType())
        || CDNodeType.Nickname.equals(bond.getBegin().getNodeType())
        || CDNodeType.Nickname.equals(bond.getEnd().getNodeType());
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
   * Bonds an R-group label that ends a label to the rest of it. ChemDraw cannot interpret labels
   * such as {@code SAr} or {@code OAr}; it stores the S (or O) bonded to the connection point and
   * the {@code Ar} unbonded, as argon, so the aryl never reached the structure. The label atom is
   * bonded to the free end of the chain the connection point starts, and only when the label text
   * ends with it ({@code X[Ni]}, a ligand written before its metal, is left alone).
   *
   * @param holder the atom whose label the nested fragment expands
   * @param fragment the nested fragment
   * @param external the fragment's external connection point
   * @param connection the fragment atom bonded to the external connection point
   */
  private void bondTrailingLabel(
      CDAtom holder, CDFragment fragment, CDAtom external, CDAtom connection) {
    String text = textOf(holder);
    if (text == null) {
      return;
    }
    List<CDAtom> unbonded =
        fragment.getAtoms().stream()
            .filter(atom -> atom != external && neighbours(fragment, atom).isEmpty())
            .toList();
    if (unbonded.size() != 1) {
      return;
    }
    CDAtom label = unbonded.get(0);
    String labelText = textOf(label);
    if (labelText == null
        || !labelText.matches(Definitions.RGROUP_LABEL_STRING)
        || !text.endsWith(labelText)
        || text.equals(labelText)) {
      return;
    }
    CDAtom end = chainEnd(fragment, external, connection);
    if (end == null) {
      return;
    }
    CDBond added = new CDBond();
    added.setBegin(end);
    added.setEnd(label);
    added.setBondOrder(CDBondOrder.Single);
    bonds.add(added);
  }

  /**
   * The free end of the unbranched chain that starts at the connection atom, or {@code null} if the
   * chain branches.
   */
  private static CDAtom chainEnd(CDFragment fragment, CDAtom external, CDAtom connection) {
    CDAtom previous = external;
    CDAtom current = connection;
    while (true) {
      CDAtom from = previous;
      List<CDAtom> onward =
          neighbours(fragment, current).stream().filter(atom -> atom != from).toList();
      if (onward.isEmpty()) {
        return current;
      }
      if (onward.size() > 1) {
        return null;
      }
      previous = current;
      current = onward.get(0);
    }
  }

  private static List<CDAtom> neighbours(CDFragment fragment, CDAtom atom) {
    List<CDAtom> result = new ArrayList<>();
    for (CDBond bond : fragment.getBonds()) {
      if (bond.getBegin() == atom) {
        result.add(bond.getEnd());
      } else if (bond.getEnd() == atom) {
        result.add(bond.getBegin());
      }
    }
    return result;
  }

  private static String textOf(CDAtom atom) {
    return Optional.ofNullable(atom.getText())
        .map(CDText::getText)
        .map(CDStyledString::getText)
        .orElseGet(atom::getLabelText);
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
                        "No bond connected to external atom (atomNumber="
                            + external.getAtomNumber()
                            + ")"));
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
      return UnwantedAbbreviations.contains(textBegin) || UnwantedAbbreviations.contains(textEnd);
    } catch (IOException e) {
      LOGGER.error("Unable to load unwanted abbreviations", e);
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
