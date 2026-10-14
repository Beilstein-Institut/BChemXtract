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
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint3D;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

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
   * ChemDraw stores 3D positions in the frame of its 2D drawing, x right and y down, so z points
   * into the screen. A fluorine wedged towards the viewer in 2D and the same fluorine placed at
   * negative z in 3D are one configuration.
   */
  @Test
  public void threeDimensionalCentreKeepsTheHandednessOfTheDrawing() throws Exception {
    FragmentConverter converter = new FragmentConverter(SilentChemObjectBuilder.getInstance());
    SmilesGenerator smiles = new SmilesGenerator(SmiFlavor.Isomeric);

    String wedged = smiles.create(converter.convert(halomethane(false)));
    String threeD = smiles.create(converter.convert(halomethane(true)));

    assertThat(wedged).as("the wedged drawing defines a stereocentre").contains("@");
    assertThat(threeD)
        .as("3D coordinates give the configuration of the wedged drawing")
        .isEqualTo(wedged);
  }

  /**
   * ChemDraw can store a flat 3D position (one z for every atom) next to a 2D drawing. Flat
   * coordinates carry no configuration, so the wedges of the drawing still decide it.
   */
  @Test
  public void flatThreeDimensionalCoordinatesKeepTheWedges() throws Exception {
    FragmentConverter converter = new FragmentConverter(SilentChemObjectBuilder.getInstance());
    SmilesGenerator smiles = new SmilesGenerator(SmiFlavor.Isomeric);
    CDFragment flat = halomethane(false);
    flat.getAtoms()
        .forEach(
            atom ->
                atom.setPosition3D(
                    new CDPoint3D(
                        atom.getPosition2D().getX(), atom.getPosition2D().getY(), -0.0005f)));

    String wedged = smiles.create(converter.convert(halomethane(false)));

    assertThat(smiles.create(converter.convert(flat)))
        .as("flat 3D coordinates keep the configuration of the wedges")
        .isEqualTo(wedged);
  }

  /**
   * Builds bromochlorofluoromethane with fluorine towards the viewer, either drawn in 2D with a
   * wedge or placed in 3D, both in ChemDraw's frame.
   *
   * @param threeD {@code true} for 3D positions, {@code false} for a 2D drawing with a wedge
   * @return the assembled {@link CDFragment}
   */
  private CDFragment halomethane(boolean threeD) {
    CDAtom carbon = element(6, 0, 0);
    CDAtom fluorine = element(9, 0, -1);
    CDAtom chlorine = element(17, -0.87f, 0.5f);
    CDAtom bromine = element(35, 0.87f, 0.5f);
    CDBond toFluorine = bond(carbon, fluorine);
    if (threeD) {
      carbon.setPosition3D(new CDPoint3D(0, 0, 0));
      fluorine.setPosition3D(new CDPoint3D(0, -1, -0.8f));
      chlorine.setPosition3D(new CDPoint3D(-0.87f, 0.5f, 0.3f));
      bromine.setPosition3D(new CDPoint3D(0.87f, 0.5f, 0.3f));
    } else {
      toFluorine.setBondDisplay(CDBondDisplay.WedgeBegin);
    }
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(new ArrayList<>(List.of(carbon, fluorine, chlorine, bromine)));
    fragment.addBond(toFluorine);
    fragment.addBond(bond(carbon, chlorine));
    fragment.addBond(bond(carbon, bromine));
    return fragment;
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
    nested.addBond(bond(external, chain.getFirst()));

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
