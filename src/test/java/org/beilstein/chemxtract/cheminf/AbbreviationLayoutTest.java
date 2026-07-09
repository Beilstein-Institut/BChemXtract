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
package org.beilstein.chemxtract.cheminf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.vecmath.Point2d;
import org.beilstein.chemxtract.utils.ChemicalUtils;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

class AbbreviationLayoutTest {

  private static final IChemObjectBuilder BUILDER = SilentChemObjectBuilder.getInstance();

  private static IAtom carbon(double x, double y) {
    IAtom atom = BUILDER.newAtom();
    atom.setSymbol("C");
    atom.setAtomicNumber(6);
    atom.setPoint2d(new Point2d(x, y));
    return atom;
  }

  /** Scaffold c1-c2, with a 3-atom substituent all collapsed onto the connection point (2,0). */
  private static IAtomContainer collapsedMolecule() {
    IAtomContainer mol = BUILDER.newAtomContainer();
    IAtom c1 = carbon(0, 0);
    IAtom c2 = carbon(1, 0);
    IAtom c3 = carbon(2, 0);
    IAtom c4 = carbon(2, 0);
    IAtom c5 = carbon(2, 0);
    mol.addAtom(c1);
    mol.addAtom(c2);
    mol.addAtom(c3);
    mol.addAtom(c4);
    mol.addAtom(c5);
    mol.addBond(0, 1, IBond.Order.SINGLE);
    mol.addBond(1, 2, IBond.Order.SINGLE);
    mol.addBond(2, 3, IBond.Order.SINGLE);
    mol.addBond(3, 4, IBond.Order.SINGLE);
    return mol;
  }

  @Test
  void collapsedAtomsGetDistinctCoordinates() throws Exception {
    IAtomContainer mol = collapsedMolecule();
    Point2d c1Before = new Point2d(mol.getAtom(0).getPoint2d());
    Point2d c2Before = new Point2d(mol.getAtom(1).getPoint2d());

    AbbreviationLayout.layoutExpandedAbbreviations(mol);

    assertFalse(ChemicalUtils.hasDuplicateCoordinates(mol), "collapsed atoms should be spread out");
    for (IAtom atom : mol.atoms()) {
      assertNotNull(atom.getPoint2d(), "every atom must have a 2D coordinate");
    }
    assertTrue(
        c1Before.equals(mol.getAtom(0).getPoint2d()), "fixed scaffold atom c1 must not move");
    assertTrue(
        c2Before.equals(mol.getAtom(1).getPoint2d()), "fixed scaffold atom c2 must not move");
  }

  @Test
  void moleculeWithoutDuplicatesIsUntouched() throws Exception {
    IAtomContainer mol = BUILDER.newAtomContainer();
    mol.addAtom(carbon(0, 0));
    mol.addAtom(carbon(1, 0));
    mol.addBond(0, 1, IBond.Order.SINGLE);
    Point2d a0 = new Point2d(mol.getAtom(0).getPoint2d());
    Point2d a1 = new Point2d(mol.getAtom(1).getPoint2d());

    AbbreviationLayout.layoutExpandedAbbreviations(mol);

    assertTrue(a0.equals(mol.getAtom(0).getPoint2d()), "coordinates must be unchanged");
    assertTrue(a1.equals(mol.getAtom(1).getPoint2d()), "coordinates must be unchanged");
  }

  @Test
  void allAtomsCollapsedFallsBackToFullLayout() throws Exception {
    IAtomContainer mol = BUILDER.newAtomContainer();
    mol.addAtom(carbon(0, 0));
    mol.addAtom(carbon(0, 0));
    mol.addAtom(carbon(0, 0));
    mol.addBond(0, 1, IBond.Order.SINGLE);
    mol.addBond(1, 2, IBond.Order.SINGLE);

    AbbreviationLayout.layoutExpandedAbbreviations(mol);

    assertFalse(ChemicalUtils.hasDuplicateCoordinates(mol), "full layout should spread atoms");
    for (IAtom atom : mol.atoms()) {
      assertNotNull(atom.getPoint2d(), "every atom must have a 2D coordinate");
    }
  }

  @Test
  void oversizedMoleculeIsSkipped() throws Exception {
    IAtomContainer mol = BUILDER.newAtomContainer();
    for (int i = 0; i < 502; i++) {
      mol.addAtom(carbon(0, 0)); // all identical -> duplicates present
    }

    AbbreviationLayout.layoutExpandedAbbreviations(mol);

    assertTrue(
        ChemicalUtils.hasDuplicateCoordinates(mol),
        "layout must be skipped above MAX_ATOM_COUNT, leaving coordinates collapsed");
  }

  @Test
  void newBondsRoughlyMatchScaffoldBondLength() throws Exception {
    // Scaffold bond c1-c2 spans distance 1.0; laid-out bonds should be near that.
    IAtomContainer mol = collapsedMolecule();

    AbbreviationLayout.layoutExpandedAbbreviations(mol);

    double sum = 0.0;
    int count = 0;
    for (IBond bond : mol.bonds()) {
      sum += bond.getBegin().getPoint2d().distance(bond.getEnd().getPoint2d());
      count++;
    }
    double average = sum / count;
    assertEquals(1.0, average, 0.3, "laid-out bonds should roughly match the scaffold bond length");
  }
}
