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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Issue #144: ChemDraw reads an atom-numbering label such as {@code P2} as the formula P&#8322; and
 * expands it into a nested fragment of two phosphorus atoms. Homoatomic formulas are legitimate
 * substituents too ({@code N3} is azide), so only labels ChemDraw itself rejected with a chemical
 * warning are collapsed back into a single atom.
 */
public class FragmentConverterTest {

  private static final String INVALID_VALENCE = "An atom in this label has an invalid valence.";

  @Test
  public void numberedAtomLabelFlaggedByChemDrawCollapsesToOneAtom() throws Exception {
    CDFragment fragment = labelledNodeOnCarbon("P2", 15, 2, INVALID_VALENCE);

    IAtomContainer container =
        new FragmentConverter(SilentChemObjectBuilder.getInstance()).convert(fragment);

    assertThat(countSymbol(container, "P"))
        .as("P2 labels one numbered phosphorus, it is not the formula P2")
        .isEqualTo(1);
  }

  @Test
  public void homoatomicFormulaWithoutAChemDrawWarningIsKept() throws Exception {
    CDFragment fragment = labelledNodeOnCarbon("N3", 7, 3, null);

    IAtomContainer container =
        new FragmentConverter(SilentChemObjectBuilder.getInstance()).convert(fragment);

    assertThat(countSymbol(container, "N"))
        .as("N3 is azide and must keep all three nitrogen atoms")
        .isEqualTo(3);
  }

  /**
   * Builds a carbon bonded to a labelled node whose nested fragment is the homoatomic chain the
   * label spells out, mirroring how ChemDraw stores such a label.
   *
   * @param label the node label, e.g. {@code P2}
   * @param elementNumber atomic number of the element in the label
   * @param count number of element atoms the label spells out
   * @param chemicalWarning ChemDraw's warning for the node, or {@code null}
   * @return the assembled {@link CDFragment}
   */
  private CDFragment labelledNodeOnCarbon(
      String label, int elementNumber, int count, String chemicalWarning) {
    CDFragment nested = new CDFragment();
    List<CDAtom> chain = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      chain.add(element(elementNumber, i, 0));
    }
    nested.setAtoms(new ArrayList<>(chain));
    for (int i = 1; i < count; i++) {
      nested.addBond(bond(chain.get(i - 1), chain.get(i)));
    }
    CDAtom external = new CDAtom();
    external.setNodeType(CDNodeType.ExternalConnectionPoint);
    external.setPosition2D(new CDPoint2D(-1, 0));
    nested.addAtom(external);
    nested.addBond(bond(external, chain.get(0)));

    CDAtom labelled = new CDAtom();
    labelled.setNodeType(CDNodeType.Fragment);
    labelled.setElementNumber(6);
    labelled.setLabelText(label);
    labelled.setChemicalWarning(chemicalWarning);
    labelled.setPosition2D(new CDPoint2D(1, 0));
    labelled.addFragment(nested);

    CDAtom carbon = element(6, 0, 0);

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(new ArrayList<>(List.of(carbon, labelled)));
    fragment.addBond(bond(carbon, labelled));
    return fragment;
  }

  private CDAtom element(int elementNumber, float x, float y) {
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.Element);
    atom.setElementNumber(elementNumber);
    atom.setPosition2D(new CDPoint2D(x, y));
    return atom;
  }

  private CDBond bond(CDAtom begin, CDAtom end) {
    CDBond bond = new CDBond();
    bond.setBegin(begin);
    bond.setEnd(end);
    bond.setBondOrder(CDBondOrder.Single);
    return bond;
  }

  private long countSymbol(IAtomContainer container, String symbol) {
    long count = 0;
    for (IAtom atom : container.atoms()) {
      if (symbol.equals(atom.getSymbol())) {
        count++;
      }
    }
    return count;
  }
}
