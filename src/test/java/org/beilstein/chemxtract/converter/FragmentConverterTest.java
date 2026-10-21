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
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  /**
   * A wedge keeps its narrow end where it was drawn when the abbreviation at its other end is
   * expanded. Stored from the label to the centre as {@code WedgeEnd}, it is the same wedge as one
   * stored from the centre as {@code WedgeBegin}; {@code OH} expands to one atom, {@code OMe} to
   * several.
   */
  @ParameterizedTest
  @ValueSource(strings = {"OH", "OMe"})
  public void wedgeStoredFromAnAbbreviationKeepsItsCentre(String label) throws Exception {
    SmilesGenerator smiles = new SmilesGenerator(SmiFlavor.Isomeric);

    String fromCentre =
        smiles.create(converter().convert(wedgedToLabel(label, false, CDBondDisplay.WedgeBegin)));
    String fromLabel =
        smiles.create(converter().convert(wedgedToLabel(label, true, CDBondDisplay.WedgeEnd)));

    assertThat(fromCentre).as("the wedged drawing defines a stereocentre").contains("@");
    assertThat(fromLabel)
        .as("the narrow end stays on the centre whichever atom the bond was stored from")
        .isEqualTo(fromCentre);
  }

  /** A wedge whose narrow end is on the abbreviation defines no centre at the carbon. */
  @ParameterizedTest
  @ValueSource(strings = {"OH", "OMe"})
  public void wedgeNarrowAtAnAbbreviationAddsNoCentre(String label) throws Exception {
    String smiles =
        new SmilesGenerator(SmiFlavor.Isomeric)
            .create(converter().convert(wedgedToLabel(label, true, CDBondDisplay.WedgeBegin)));

    assertThat(smiles).as("the wedge points at the label, not at the carbon").doesNotContain("@");
  }

  /**
   * ChemDraw's R/S label settles a centre the drawing leaves without a wedge, centre by centre: a
   * double bond elsewhere in the fragment, whose E/Z is perceived, must not switch it off.
   */
  @Test
  public void storedLabelSettlesAnUnwedgedCentreBesideAPerceivedDoubleBond() throws Exception {
    SmilesGenerator smiles = new SmilesGenerator(SmiFlavor.Isomeric);

    String alone = smiles.create(converter().convert(labelledCentre(false)));
    String withAlkene = smiles.create(converter().convert(labelledCentre(true)));

    assertThat(alone).as("the stored R settles the unwedged centre").contains("@");
    assertThat(withAlkene).as("the alkene's E/Z is perceived").containsAnyOf("/", "\\");
    assertThat(withAlkene)
        .as("the stored R still settles the centre beside the perceived alkene")
        .contains("@");
  }

  private FragmentConverter converter() {
    return new FragmentConverter(SilentChemObjectBuilder.getInstance());
  }

  /**
   * Builds chlorofluoromethane carrying an abbreviation, the bond to which is wedged.
   *
   * @param label the abbreviation's text
   * @param fromLabel {@code true} to store the bond from the label to the carbon
   * @param display the wedge as stored
   */
  private CDFragment wedgedToLabel(String label, boolean fromLabel, CDBondDisplay display) {
    CDAtom carbon = element(6, 0, 0);
    CDAtom fluorine = element(9, 0.20f, -0.98f);
    CDAtom chlorine = element(17, -0.97f, -0.22f);
    CDAtom abbreviation = new CDAtom();
    abbreviation.setNodeType(CDNodeType.Unspecified);
    abbreviation.setLabelText(label);
    abbreviation.setChemicalWarning("ChemDraw can't interpret this label.");
    abbreviation.setPosition2D(new CDPoint2D(0.89f, -0.46f));
    CDBond wedge = fromLabel ? bond(abbreviation, carbon) : bond(carbon, abbreviation);
    wedge.setBondDisplay(display);
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(new ArrayList<>(List.of(carbon, fluorine, chlorine, abbreviation)));
    fragment.addBond(bond(carbon, fluorine));
    fragment.addBond(bond(carbon, chlorine));
    fragment.addBond(wedge);
    return fragment;
  }

  /**
   * Builds chlorofluoromethanol drawn without a wedge but labelled R by ChemDraw, optionally with a
   * (E)-but-2-enyl ether on the oxygen.
   */
  private CDFragment labelledCentre(boolean withAlkene) {
    CDAtom carbon = element(6, 0, 0);
    carbon.setAtomGeometry(CDAtomGeometry.Tetrahedral);
    carbon.setStereochemistry(CDAtomCIPType.R);
    CDAtom fluorine = element(9, 0.20f, -0.98f);
    CDAtom chlorine = element(17, -0.97f, -0.22f);
    CDAtom oxygen = element(8, 0.89f, 0.46f);
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(new ArrayList<>(List.of(carbon, fluorine, chlorine, oxygen)));
    fragment.addBond(bond(carbon, fluorine));
    fragment.addBond(bond(carbon, chlorine));
    fragment.addBond(bond(carbon, oxygen));
    if (withAlkene) {
      CDAtom c1 = element(6, 3, 0);
      CDAtom c2 = element(6, 3.87f, 0.5f);
      CDAtom c3 = element(6, 4.73f, 0);
      CDAtom c4 = element(6, 5.6f, 0.5f);
      fragment.addAllAtoms(List.of(c1, c2, c3, c4));
      fragment.addBond(bond(oxygen, c1));
      fragment.addBond(bond(c1, c2));
      CDBond doubleBond = bond(c2, c3);
      doubleBond.setBondOrder(CDBondOrder.Double);
      fragment.addBond(doubleBond);
      fragment.addBond(bond(c3, c4));
    }
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
