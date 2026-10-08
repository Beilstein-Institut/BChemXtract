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
package org.beilstein.chemxtract.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.stereo.Projection;
import org.openscience.cdk.stereo.StereoElementFactory;

class StereoHandlerTest {

  private static final SmilesGenerator SMIGEN = new SmilesGenerator(SmiFlavor.Absolute);

  @Test
  void cipLabelGivesTheSameConfigurationWhateverTheLigandOrder() throws CDKException {
    // (R)- and (S)-bromochlorofluoromethane
    Map<CDAtomCIPType, String> expected =
        Map.of(
            CDAtomCIPType.R,
            canonical("F[C@H](Cl)Br"),
            CDAtomCIPType.S,
            canonical("F[C@@H](Cl)Br"));
    for (List<String> order : List.of(List.of("F", "Cl", "Br"), List.of("Br", "Cl", "F"))) {
      for (CDAtomCIPType label : expected.keySet()) {
        IAtomContainer container = chbrclf(order, false);
        StereoHandler.setStereo(container, Map.of(), labelled(container, label));
        assertThat(SMIGEN.create(container))
            .as("%s, ligand order %s", label, order)
            .isEqualTo(expected.get(label));
      }
    }
  }

  @Test
  void cipLabelDoesNotOverrideAWedgePerceivedCentre() throws CDKException {
    IAtomContainer fromWedge = chbrclf(List.of("F", "Cl", "Br"), true);
    StereoHandler.setStereo(fromWedge, Map.of(), Map.of());
    for (CDAtomCIPType label : List.of(CDAtomCIPType.R, CDAtomCIPType.S)) {
      IAtomContainer container = chbrclf(List.of("F", "Cl", "Br"), true);
      StereoHandler.setStereo(container, Map.of(), labelled(container, label));
      assertThat(container.stereoElements()).as("one element for the centre, %s", label).hasSize(1);
      assertThat(SMIGEN.create(container))
          .as("the wedge decides, %s", label)
          .isEqualTo(SMIGEN.create(fromWedge));
    }
  }

  @Test
  void haworthWithBoldFrontEdgeIsReadAsHaworth() throws CDKException {
    // the reference: CDK's own Haworth reading of the same drawing with plain bonds
    IAtomContainer plain = haworthPyranose();
    List<IStereoElement> reference =
        StereoElementFactory.using2DCoordinates(plain)
            .interpretProjections(Projection.Haworth)
            .createAll();
    reference.forEach(plain::addStereoElement);
    assertThat(reference).as("CDK reads every ring carbon of the plain drawing").hasSize(5);

    // ChemDraw draws the front edge bold; BondConverter turns bold into a wedge
    IAtomContainer bold = haworthPyranose();
    Map<CDBond, IBond> bondMap = new HashMap<>();
    for (int i = 0; i < 3; i++) {
      IBond bond = bold.getBond(i);
      bond.setDisplay(IBond.Display.WedgeBegin);
      CDBond cdBond = new CDBond();
      cdBond.setBondDisplay(CDBondDisplay.Bold);
      bondMap.put(cdBond, bond);
    }
    StereoHandler.setStereo(bold, bondMap, Map.of());
    assertThat(SMIGEN.create(bold))
        .as("a bold front edge is perspective, not a wedge")
        .isEqualTo(SMIGEN.create(plain));
  }

  /**
   * A 6-deoxy pyranose in Haworth projection: ring C1-C2-C3-C4-C5-O5 with C1-C2, C2-C3 and C3-C4 as
   * the front edges (bonds 0-2), and every substituent straight up or down.
   */
  private static IAtomContainer haworthPyranose() {
    IAtomContainer container = SilentChemObjectBuilder.getInstance().newAtomContainer();
    double[][] ring = {{1, 0}, {0.5, -0.4}, {-0.5, -0.4}, {-1, 0}, {-0.5, 0.4}, {0.5, 0.4}};
    for (int i = 0; i < ring.length; i++) {
      atom(container, i < 5 ? "C" : "O", i < 5 ? 1 : 0, new Point2d(ring[i][0], ring[i][1]));
    }
    for (int i = 0; i < ring.length; i++) {
      container.addBond(i, (i + 1) % ring.length, IBond.Order.SINGLE);
    }
    // substituent on each ring carbon: O1 down, O2 down, O3 up, O4 down, C6 up
    double[] dy = {-0.8, -0.8, 0.5, -0.8, 0.8};
    for (int i = 0; i < 5; i++) {
      atom(
          container, i < 4 ? "O" : "C", i < 4 ? 1 : 3, new Point2d(ring[i][0], ring[i][1] + dy[i]));
      container.addBond(i, container.getAtomCount() - 1, IBond.Order.SINGLE);
    }
    return container;
  }

  /**
   * CHBrClF, centre at index 0. With {@code wedged}, the first halogen bond is a wedge and an argon
   * atom sits on the last halogen's position, so the CIP fallback runs on duplicate coordinates.
   */
  private static IAtomContainer chbrclf(List<String> order, boolean wedged) {
    IAtomContainer container = SilentChemObjectBuilder.getInstance().newAtomContainer();
    Point2d[] points = {new Point2d(0, 1), new Point2d(0.87, -0.5), new Point2d(-0.87, -0.5)};
    atom(container, "C", 1, new Point2d(0, 0));
    for (int i = 0; i < 3; i++) {
      atom(container, order.get(i), 0, points[i]);
      container.addBond(0, i + 1, IBond.Order.SINGLE);
    }
    if (wedged) {
      container.getBond(0).setDisplay(IBond.Display.WedgeBegin);
      atom(container, "Ar", 0, new Point2d(points[2]));
    }
    return container;
  }

  private static void atom(IAtomContainer container, String symbol, int hydrogens, Point2d point) {
    IAtom atom = container.getBuilder().newAtom();
    atom.setSymbol(symbol);
    atom.setAtomicNumber(Elements.ofString(symbol).number());
    atom.setImplicitHydrogenCount(hydrogens);
    atom.setPoint2d(point);
    container.addAtom(atom);
  }

  /** Atom map with a ChemDraw node carrying the given CIP label for the centre. */
  private static Map<CDAtom, IAtom> labelled(IAtomContainer container, CDAtomCIPType label) {
    CDAtom cdAtom = new CDAtom();
    cdAtom.setAtomGeometry(CDAtomGeometry.Tetrahedral);
    cdAtom.setStereochemistry(label);
    return Map.of(cdAtom, container.getAtom(0));
  }

  private static String canonical(String smiles) throws CDKException {
    return SMIGEN.create(
        new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles));
  }
}
