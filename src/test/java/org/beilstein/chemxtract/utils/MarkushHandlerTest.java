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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/** Tests for scaffold-scoped resolution of R-group definitions in {@link MarkushHandler}. */
public class MarkushHandlerTest {

  private static CDRectangle rect(float left, float top, float right, float bottom) {
    CDRectangle r = new CDRectangle();
    r.setLeft(left);
    r.setTop(top);
    r.setRight(right);
    r.setBottom(bottom);
    return r;
  }

  private static CDText textAt(CDRectangle bounds, String content) {
    CDStyledString styled = new CDStyledString();
    styled.addChunk(new CDStyledString.CDXChunk(null, 10f, null, null, content));
    CDText text = new CDText();
    text.setText(styled);
    text.setBounds(bounds);
    return text;
  }

  /**
   * Two scaffolds on the same page, each with its own "R = ..." definition block. Each scaffold
   * must resolve to its own block, not a page-wide merge of both (the {@code putIfAbsent}
   * collapse).
   */
  @Test
  public void scopesDefinitionsToNearestScaffold() {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "R = Cl"));
    page.addText(textAt(rect(200, 0, 250, 50), "R = Br"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    Map<String, List<String>> nearLeft = handler.residueLabelsNear(rect(0, 0, 40, 40));
    Map<String, List<String>> nearRight = handler.residueLabelsNear(rect(210, 0, 240, 40));

    assertEquals(List.of("Cl"), nearLeft.get("R"), "left scaffold must resolve to its own block");
    assertEquals(List.of("Br"), nearRight.get("R"), "right scaffold must resolve to its own block");
  }

  /**
   * A grafted multi-atom substituent (parsed from SMILES, hence coordinate-less) must receive 2D
   * coordinates via partial layout, while the scaffold keeps its original coordinates.
   */
  @Test
  public void graftedSubstituentAtomsGetCoordinates()
      throws IOException, CloneNotSupportedException, CDKException {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "R = *CC"));
    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    // Scaffold: C0-C1-R with known coordinates.
    IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    IAtomContainer scaffold = builder.newAtomContainer();
    IAtom c0 = builder.newInstance(IAtom.class, "C");
    c0.setPoint2d(new Point2d(0.0, 0.0));
    IAtom c1 = builder.newInstance(IAtom.class, "C");
    c1.setPoint2d(new Point2d(1.5, 0.0));
    IPseudoAtom r = builder.newInstance(IPseudoAtom.class, "R");
    r.setLabel("R");
    r.setPoint2d(new Point2d(3.0, 0.0));
    scaffold.addAtom(c0);
    scaffold.addAtom(c1);
    scaffold.addAtom(r);
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(4, product.getAtomCount(), "two scaffold carbons plus grafted ethyl (2 C)");
    for (IAtom atom : product.atoms()) {
      assertNotNull(atom.getPoint2d(), "every atom must have 2D coordinates after grafting");
    }
    // Scaffold coordinates are preserved (held fixed during partial layout).
    assertEquals(0.0, product.getAtom(0).getPoint2d().distance(new Point2d(0.0, 0.0)), 1e-6);
    assertEquals(0.0, product.getAtom(1).getPoint2d().distance(new Point2d(1.5, 0.0)), 1e-6);
  }

  /**
   * A positional table ("R1 = R2 = H", "R1 = F, R2 = H", "R1 = H, R2 = F") enumerates exactly its
   * three row-tuples, not the 2x2 cartesian product (which would invent the spurious R1=F,R2=F).
   */
  @Test
  public void correlatedTableEnumeratesRowsNotCartesian()
      throws IOException, CloneNotSupportedException, CDKException {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "R1 = R2 = H\rR1 = F, R2 = H\rR1 = H, R2 = F"));
    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    // Scaffold: R1-C0-C1-R2 with coordinates.
    IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    IAtomContainer scaffold = builder.newAtomContainer();
    IPseudoAtom r1 = builder.newInstance(IPseudoAtom.class, "R1");
    r1.setLabel("R1");
    r1.setPoint2d(new Point2d(-1.5, 0.0));
    IAtom c0 = builder.newInstance(IAtom.class, "C");
    c0.setPoint2d(new Point2d(0.0, 0.0));
    IAtom c1 = builder.newInstance(IAtom.class, "C");
    c1.setPoint2d(new Point2d(1.5, 0.0));
    IPseudoAtom r2 = builder.newInstance(IPseudoAtom.class, "R2");
    r2.setLabel("R2");
    r2.setPoint2d(new Point2d(3.0, 0.0));
    scaffold.addAtom(r1);
    scaffold.addAtom(c0);
    scaffold.addAtom(c1);
    scaffold.addAtom(r2);
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);
    scaffold.addBond(2, 3, IBond.Order.SINGLE);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold, rect(0, 0, 40, 40));

    assertEquals(3, results.size(), "one structure per table row, no (F,F) corner");
  }

  /**
   * A positional table written on a single line — "10 X = PhCONMe, Y = N; 11 X = PhCONH, Y = C" —
   * enumerates its row-tuples (not the cartesian product), and both the composite substituent (X)
   * and the ring-atom variation (Y) resolve fully so no pseudo-atom is left behind.
   */
  @Test
  public void sameLineCorrelatedTableResolvesRowTuplesNotCartesian()
      throws IOException, CloneNotSupportedException, CDKException {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "10 X = PhCONMe, Y = N; 11 X = PhCONH, Y = C"));
    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    IAtomContainer scaffold = builder.newAtomContainer();
    IAtom c0 = builder.newInstance(IAtom.class, "C");
    c0.setPoint2d(new Point2d(0.0, 0.0));
    IPseudoAtom x = builder.newInstance(IPseudoAtom.class, "X");
    x.setLabel("X");
    x.setPoint2d(new Point2d(1.5, 0.0));
    IPseudoAtom y = builder.newInstance(IPseudoAtom.class, "Y");
    y.setLabel("Y");
    y.setPoint2d(new Point2d(-1.5, 0.0));
    scaffold.addAtom(c0);
    scaffold.addAtom(x);
    scaffold.addAtom(y);
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(0, 2, IBond.Order.SINGLE);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold, rect(0, 0, 40, 40));

    assertEquals(2, results.size(), "two table rows, not the 2x2 cartesian");
    for (IAtomContainer product : results) {
      for (IAtom atom : product.atoms()) {
        assertFalse(atom instanceof IPseudoAtom, "every X/Y must be resolved to real atoms");
      }
    }
  }

  /**
   * A placeholder whose substituents are bare element symbols outside the SMILES organic subset
   * (Se, Te) must still resolve — they need bracketing to parse. Y = S, Se, Te yields all three.
   */
  @Test
  public void bareNonOrganicElementSubstituentsResolve()
      throws IOException, CloneNotSupportedException, CDKException {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "Y = S, Se, Te"));
    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    // Scaffold: C0-Y-C1 (Y is a divalent bridge, like a chalcogen in a ring).
    IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    IAtomContainer scaffold = builder.newAtomContainer();
    IAtom c0 = builder.newInstance(IAtom.class, "C");
    c0.setPoint2d(new Point2d(0.0, 0.0));
    IPseudoAtom y = builder.newInstance(IPseudoAtom.class, "Y");
    y.setLabel("Y");
    y.setPoint2d(new Point2d(1.5, 0.0));
    IAtom c1 = builder.newInstance(IAtom.class, "C");
    c1.setPoint2d(new Point2d(3.0, 0.0));
    scaffold.addAtom(c0);
    scaffold.addAtom(y);
    scaffold.addAtom(c1);
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold, rect(0, 0, 40, 40));

    assertEquals(3, results.size(), "S, Se and Te must all resolve");
    Set<Integer> chalcogens = new HashSet<>();
    for (IAtomContainer product : results) {
      for (IAtom atom : product.atoms()) {
        Integer z = atom.getAtomicNumber();
        if (z != null && (z == 16 || z == 34 || z == 52)) {
          chalcogens.add(z);
        }
      }
    }
    assertEquals(Set.of(16, 34, 52), chalcogens, "expected S (16), Se (34) and Te (52)");
  }

  /**
   * A substrate-scope legend split into two adjacent columns is one list for the scaffold. Geometry
   * mirrors {@code volumeTest/markush/mantis11158/20-14-i2}: two blocks spanning the same rows, a
   * gutter narrow relative to the columns, 6 + 5 values.
   */
  @Test
  public void sideBySideLegendColumnsMergeIntoOneList() {
    CDPage page = new CDPage();
    page.addText(textAt(rect(149.5f, 117.6f, 247.1f, 174.0f), "R = H, 2-Me, 3-OMe"));
    page.addText(textAt(rect(274.7f, 118.3f, 375.0f, 170.8f), "R = 3-Cl, 4-Br"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    // Scaffold sits above the left column; both columns must still reach it.
    Map<String, List<String>> scoped = handler.residueLabelsNear(rect(70f, 59f, 133f, 100f));

    assertEquals(
        List.of("H", "2-Me", "3-OMe", "3-Cl", "4-Br"),
        scoped.get("R"),
        "both legend columns must reach the scaffold, left column first");
  }

  /**
   * The column merge must not swallow the case nearest-block scoping exists for: two scaffolds far
   * apart, each with its own definition of the same label, stay separate.
   */
  @Test
  public void farApartLegendsAreNotColumnMerged() {
    CDPage page = new CDPage();
    page.addText(textAt(rect(0, 0, 50, 50), "R = Cl"));
    page.addText(textAt(rect(200, 0, 250, 50), "R = Br"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());

    assertEquals(List.of("Cl"), handler.residueLabelsNear(rect(0, 0, 40, 40)).get("R"));
    assertEquals(List.of("Br"), handler.residueLabelsNear(rect(210, 0, 240, 40)).get("R"));
  }

  /** Scaffold atom with 2D coordinates at the given x, on the y = 0 axis. */
  private static IAtom carbonAt(double x) {
    IAtom atom = SilentChemObjectBuilder.getInstance().newInstance(IAtom.class, "C");
    atom.setPoint2d(new Point2d(x, 0.0));
    return atom;
  }

  /** Pseudo-atom (R-group placeholder) with 2D coordinates at the given x. */
  private static IPseudoAtom residueAt(String label, double x) {
    IPseudoAtom atom = SilentChemObjectBuilder.getInstance().newInstance(IPseudoAtom.class, label);
    atom.setLabel(label);
    atom.setPoint2d(new Point2d(x, 0.0));
    return atom;
  }

  /** Handler with structural definitions only, so substituent SMILES bypass legend parsing. */
  private static MarkushHandler handlerWith(Map<String, List<String>> definitions) {
    MarkushHandler handler =
        new MarkushHandler(new CDPage(), SilentChemObjectBuilder.getInstance());
    handler.addResidueDefinitions(definitions);
    return handler;
  }

  private static void assertNoPseudoAtoms(IAtomContainer product) {
    for (IAtom atom : product.atoms()) {
      assertFalse(atom instanceof IPseudoAtom, "no pseudo-atom may survive substitution");
    }
  }

  /**
   * A residue drawn as a chain link carries two bonds, and a two-attachment substituent must keep
   * both: wiring only the first bond leaves the molecule cut in two.
   */
  @Test
  public void bivalentResidueInChainKeepsBothConnections()
      throws IOException, CloneNotSupportedException, CDKException {
    // Scaffold: C0-X-C1, X bivalent.
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    scaffold.addAtom(carbonAt(0.0));
    scaffold.addAtom(residueAt("X", 1.5));
    scaffold.addAtom(carbonAt(3.0));
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);

    List<IAtomContainer> results =
        handlerWith(Map.of("X", List.of("[*]C[*]"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(3, product.getAtomCount(), "two scaffold carbons plus the bridging carbon");
    assertEquals(2, product.getBondCount(), "both original bonds must be re-made");
    assertEquals(
        1,
        ConnectivityChecker.partitionIntoMolecules(product).getAtomContainerCount(),
        "the chain must stay in one piece");
    assertNoPseudoAtoms(product);
  }

  /**
   * A residue drawn as a ring member must not open the ring: both of its bonds are attachment
   * points for the two-attachment substituent.
   */
  @Test
  public void bivalentResidueInRingKeepsRingClosed()
      throws IOException, CloneNotSupportedException, CDKException {
    // Scaffold: five carbons plus X closing a six-membered ring.
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    for (int i = 0; i < 5; i++) {
      scaffold.addAtom(carbonAt(i * 1.5));
    }
    scaffold.addAtom(residueAt("X", 7.5));
    for (int i = 0; i < 5; i++) {
      scaffold.addBond(i, i + 1, IBond.Order.SINGLE);
    }
    scaffold.addBond(5, 0, IBond.Order.SINGLE);

    List<IAtomContainer> results =
        handlerWith(Map.of("X", List.of("[*]C[*]"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(6, product.getAtomCount());
    assertEquals(6, product.getBondCount(), "ring bond count is preserved");
    assertEquals(1, Cycles.mcb(product).numberOfCycles(), "the ring must stay closed");
    assertNoPseudoAtoms(product);
  }

  /**
   * The layout a two-attachment substituent was written for: two monovalent residues of the same
   * label, bridged by one substituent. Regression guard for the bivalent fix.
   */
  @Test
  public void twoMonovalentResiduesAreBridgedByOneSubstituent()
      throws IOException, CloneNotSupportedException, CDKException {
    // Scaffold: X-C0-C1-C2-X, both X monovalent, bridged into a four-membered ring.
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    scaffold.addAtom(carbonAt(0.0));
    scaffold.addAtom(carbonAt(1.5));
    scaffold.addAtom(carbonAt(3.0));
    scaffold.addAtom(residueAt("X", -1.5));
    scaffold.addAtom(residueAt("X", 4.5));
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);
    scaffold.addBond(0, 3, IBond.Order.SINGLE);
    scaffold.addBond(2, 4, IBond.Order.SINGLE);

    List<IAtomContainer> results =
        handlerWith(Map.of("X", List.of("[*]C[*]"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(4, product.getAtomCount(), "three scaffold carbons plus the bridge");
    assertEquals(4, product.getBondCount());
    assertEquals(1, Cycles.mcb(product).numberOfCycles(), "the bridge closes one ring");
    assertNoPseudoAtoms(product);
  }

  /**
   * A bivalent residue must not steal an unrelated R-group as its second attachment point: doing so
   * consumed the foreign residue and dropped its substituent entirely.
   */
  @Test
  public void bivalentResidueDoesNotConsumeForeignLabel()
      throws IOException, CloneNotSupportedException, CDKException {
    // Scaffold: C0-X-C1-R1, X bivalent, R1 a separate monovalent residue.
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    scaffold.addAtom(carbonAt(0.0));
    scaffold.addAtom(residueAt("X", 1.5));
    scaffold.addAtom(carbonAt(3.0));
    scaffold.addAtom(residueAt("R1", 4.5));
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(1, 2, IBond.Order.SINGLE);
    scaffold.addBond(2, 3, IBond.Order.SINGLE);

    List<IAtomContainer> results =
        handlerWith(Map.of("X", List.of("[*]C[*]"), "R1", List.of("Cl"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(4, product.getAtomCount(), "three carbons plus the chlorine");
    assertEquals(3, product.getBondCount());
    assertEquals(
        1,
        ConnectivityChecker.partitionIntoMolecules(product).getAtomContainerCount(),
        "R1 must still be attached, not consumed as X's second attachment point");
    boolean hasChlorine = false;
    for (IAtom atom : product.atoms()) {
      Integer z = atom.getAtomicNumber();
      if (z != null && z == 17) {
        hasChlorine = true;
      }
    }
    assertTrue(hasChlorine, "the foreign residue's own substituent must survive");
    assertNoPseudoAtoms(product);
  }

  /**
   * When the residue's valence and the substituent's connection points disagree, the R-group is
   * left unsubstituted rather than grafted with a dropped bond — SubstanceXtractor then skips the
   * structure instead of emitting a mis-connected one.
   */
  @Test
  public void attachmentCountMismatchLeavesResidueUnsubstituted()
      throws IOException, CloneNotSupportedException, CDKException {
    // Scaffold: X bonded to three carbons, substituent offers only two connection points.
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    scaffold.addAtom(residueAt("X", 0.0));
    scaffold.addAtom(carbonAt(1.5));
    scaffold.addAtom(carbonAt(3.0));
    scaffold.addAtom(carbonAt(4.5));
    scaffold.addBond(0, 1, IBond.Order.SINGLE);
    scaffold.addBond(0, 2, IBond.Order.SINGLE);
    scaffold.addBond(0, 3, IBond.Order.SINGLE);

    List<IAtomContainer> results =
        handlerWith(Map.of("X", List.of("[*]C[*]"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(4, product.getAtomCount(), "nothing may be grafted");
    assertEquals(3, product.getBondCount(), "no bond may be lost");
    long pseudoAtoms = 0;
    for (IAtom atom : product.atoms()) {
      if (atom instanceof IPseudoAtom) {
        pseudoAtoms++;
      }
    }
    assertEquals(1, pseudoAtoms, "the unresolvable residue stays in place");
  }

  /**
   * A single-atom definition on a bivalent residue (X = O in a ring) goes through the
   * single-attachment path, which rewires every bond of the residue. Guards the common case that
   * already worked.
   */
  @Test
  public void singleAtomDefinitionOnBivalentResidueKeepsRingClosed()
      throws IOException, CloneNotSupportedException, CDKException {
    IAtomContainer scaffold = SilentChemObjectBuilder.getInstance().newAtomContainer();
    for (int i = 0; i < 5; i++) {
      scaffold.addAtom(carbonAt(i * 1.5));
    }
    scaffold.addAtom(residueAt("X", 7.5));
    for (int i = 0; i < 5; i++) {
      scaffold.addBond(i, i + 1, IBond.Order.SINGLE);
    }
    scaffold.addBond(5, 0, IBond.Order.SINGLE);

    List<IAtomContainer> results = handlerWith(Map.of("X", List.of("O"))).replaceRGroups(scaffold);

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(6, product.getAtomCount());
    assertEquals(6, product.getBondCount());
    assertEquals(1, Cycles.mcb(product).numberOfCycles(), "the oxygen closes the ring");
    assertNoPseudoAtoms(product);
  }
}
