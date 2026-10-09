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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

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
    IAtomContainer product = results.getFirst();
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
   * A positional table split into two side-by-side text boxes is one table: each box holds whole
   * rows, so its rows are unioned. Geometry mirrors {@code volumeTest/markush/m28188630-7}, where
   * the scaffold sits closer to the right box (19f-i) and lost the left one (19a-e).
   */
  @Test
  public void sideBySideCorrelatedTableColumnsMergeIntoOneTable() throws Exception {
    CDPage page = new CDPage();
    page.addText(
        textAt(
            rect(293.4f, 332.6f, 364.3f, 388.8f),
            "19a: R1 = R2 = OMe\n19b: R1 = R2 = H\n19c: R1 = R2 = F"));
    page.addText(
        textAt(
            rect(367.1f, 321.2f, 456.9f, 377.5f),
            "19f: R1 = R2 = Cl\n19g: R1 = CF3, R2 = OMe\n19h: R1 = CF3, R2 = F"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());
    List<IAtomContainer> results =
        handler.replaceRGroups(
            List.of(twoResidueScaffold()), rect(327f, 213f, 414f, 329f), new HashSet<>());

    assertEquals(6, results.size(), "the rows of both boxes must reach the scaffold");
  }

  /**
   * Two positional tables stacked one above the other with a blank gap of about two lines between
   * them are separate tables, one per scaffold, not one table split over two boxes. Geometry
   * mirrors {@code volumeTest/markush/m28144482-i5}, rows 6/12a-c and 13l-n.
   */
  @Test
  public void stackedSeparateTablesAreNotMerged() throws Exception {
    CDPage page = new CDPage();
    page.addText(
        textAt(
            rect(102.3f, 261.3f, 235.0f, 295.0f),
            "6: R1 = R2 = H\n12a: R1 = Me, R2 = H\n12b: R1 = F, R2 = H"));
    page.addText(
        textAt(
            rect(102.7f, 322.3f, 229.0f, 354.2f),
            "13l: R1 = H, R2 = CN\n13m: R1 = H, R2 = Br\n13n: R1 = H, R2 = OMe"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());
    List<IAtomContainer> results =
        handler.replaceRGroups(
            List.of(twoResidueScaffold()), rect(7f, 310f, 91f, 387f), new HashSet<>());

    assertEquals(3, results.size(), "only the table beside the scaffold may reach it");
  }

  /**
   * A scaffold carrying R1-R5 takes the R1-R5 table drawn for it, not also an R1-R3 table that
   * belongs to another scaffold: the smaller table's labels are all covered by the larger one.
   * Taking both made two varying choices, so the scaffold was not expanded at all (m28144482-i5,
   * nucleophiles 5 and 18-25).
   */
  @Test
  public void tableOverASubsetOfTheLabelsGivesWayToTheFullTable() throws Exception {
    CDPage page = new CDPage();
    page.addText(
        textAt(rect(100f, 0f, 230f, 22f), "6: R1 = R2 = R3 = H\n12a: R1 = Me, R2 = R3 = H"));
    page.addText(
        textAt(
            rect(100f, 100f, 250f, 133f),
            "5: R1 = R3 = R5 = OMe, R2 = R4 = H\n"
                + "18: R1 = R3 = OMe, R2 = R4 = R5 = H\n"
                + "20: R1 = R3 = R5 = Me, R2 = R4 = H"));

    MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());
    IAtomContainer scaffold = residueScaffold(Map.of("R1", 0, "R2", 1, "R3", 2, "R4", 3, "R5", 4));
    List<IAtomContainer> results =
        handler.replaceRGroups(List.of(scaffold), rect(40f, 90f, 90f, 140f), new HashSet<>());

    assertEquals(3, results.size(), "the R1-R5 table must expand the scaffold on its own");
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
    return handlerWith(definitions, false);
  }

  private static MarkushHandler handlerWith(
      Map<String, List<String>> definitions, boolean unrestricted) {
    MarkushHandler handler =
        new MarkushHandler(new CDPage(), SilentChemObjectBuilder.getInstance(), unrestricted);
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

  /** The indole of #166 with an R-group drawn on its benzo ring, laid out so grafting can work. */
  private static IAtomContainer scaffoldFromSmiles(String smiles, String label, int anchorIndex)
      throws CDKException {
    IAtomContainer scaffold =
        new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles(smiles);
    IPseudoAtom residue =
        SilentChemObjectBuilder.getInstance().newInstance(IPseudoAtom.class, label);
    residue.setLabel(label);
    IAtom anchor = scaffold.getAtom(anchorIndex);
    scaffold.addAtom(residue);
    scaffold.addBond(new Bond(anchor, residue, IBond.Order.SINGLE));
    Integer hydrogens = anchor.getImplicitHydrogenCount();
    if (hydrogens != null && hydrogens > 0) {
      anchor.setImplicitHydrogenCount(hydrogens - 1);
    }
    new StructureDiagramGenerator().generateCoordinates(scaffold);
    return scaffold;
  }

  private static String canonicalSmiles(IAtomContainer container) throws CDKException {
    return new SmilesGenerator(SmiFlavor.Canonical).create(container);
  }

  /**
   * A value naming several locants ("5,7-Me2") puts one copy of the group on each of them: the
   * drawn residue moves to the first, the others are grafted onto the atoms their numbers name
   * (#166).
   */
  @Test
  public void multiLocantValueSubstitutesEveryPositionItNames() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("5,7-Me2")));
    // Indole, R drawn on a benzo carbon; the legend states where the methyls actually go.
    IAtomContainer scaffold = scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 1);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold);

    assertEquals(1, results.size());
    assertNoPseudoAtoms(results.getFirst());
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance())
                .parseSmiles("[nH]1ccc2cc(C)cc(C)c12")),
        canonicalSmiles(results.getFirst()),
        "5,7-Me2 must give 5,7-dimethylindole");
  }

  /** The bracketed multiplier form, on a single ring numbered from its attachment atom. */
  @Test
  public void bracketedMultiplierValueSubstitutesEveryPositionItNames() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("3,4-(OMe)2")));
    // Chlorobenzene: the chlorine is the ring's attachment atom, so positions count from it.
    IAtomContainer scaffold = scaffoldFromSmiles("Clc1ccccc1", "R", 3);

    List<IAtomContainer> results = handler.replaceRGroups(scaffold);

    assertEquals(1, results.size());
    assertNoPseudoAtoms(results.getFirst());
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance())
                .parseSmiles("COc1ccc(Cl)cc1OC")),
        canonicalSmiles(results.getFirst()),
        "3,4-(OMe)2 must give the 3,4-dimethoxy ring");
  }

  /**
   * Counted from its attachment atom, a single ring cannot tell position 3 from position 5 — both
   * are two bonds away. Rather than stack both methyls on the one atom it picks, the value is
   * dropped.
   */
  @Test
  public void indistinguishablePositionsOnOneRingDropTheValue() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("3,5-Me2")));
    IAtomContainer scaffold = scaffoldFromSmiles("Clc1ccccc1", "R", 3);

    assertTrue(handler.replaceRGroups(scaffold).isEmpty(), "an ambiguous position must not graft");
  }

  /**
   * A scaffold drawn with a position-variation attachment is converted once per candidate atom.
   * When the legend gives the substituent by ring position, the residue moves onto the position it
   * names and every candidate yields the same structure, so only one of them is built.
   */
  @Test
  public void positionalValueBuildsOneStructureForAllAttachmentCandidates() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("5-Me")));
    List<IAtomContainer> candidates =
        List.of(
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 1),
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 2));

    List<IAtomContainer> results =
        handler.replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>());

    assertEquals(1, results.size(), "the legend names the position, so the candidates collapse");
    assertNoPseudoAtoms(results.getFirst());
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance())
                .parseSmiles("[nH]1ccc2cc(C)ccc12")),
        canonicalSmiles(results.getFirst()),
        "5-Me must give 5-methylindole whichever candidate carried the residue");
  }

  /**
   * A substituent that is a lone hydrogen leaves the ring atom it replaces with the hydrogen count
   * it already had, so it too gives the same structure from every candidate atom.
   */
  @Test
  public void hydrogenValueBuildsOneStructureForAllAttachmentCandidates() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("H")));
    List<IAtomContainer> candidates =
        List.of(
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 1),
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 2));

    List<IAtomContainer> results =
        handler.replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>());

    assertEquals(1, results.size(), "substituting H cannot depend on which candidate was drawn");
    assertNoPseudoAtoms(results.getFirst());
    assertEquals(
        ChemicalUtils.getInChI(
                new SmilesParser(SilentChemObjectBuilder.getInstance())
                    .parseSmiles("c1ccc2[nH]ccc2c1"))
            .getInchiKey(),
        ChemicalUtils.getInChI(results.getFirst()).getInchiKey(),
        "R = H must give the bare scaffold, the hydrogen it grafts being the one the atom had");
  }

  /**
   * A substituent that stays where it is drawn does depend on the candidate atom: those structures
   * are different substances and every candidate must be built.
   */
  @Test
  public void substituentThatStaysWhereDrawnBuildsEveryAttachmentCandidate() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("Me")), true);
    List<IAtomContainer> candidates =
        List.of(
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 1),
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 6));

    List<IAtomContainer> results =
        handler.replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>());

    assertEquals(2, results.size(), "benzo and pyrrole methylation are different substances");
  }

  /**
   * A variable attachment can move a drawn group rather than a residue, and then the candidates are
   * already different structures. Nothing may merge them, whatever the assignment does with the
   * residue — here a hydrogen, which on one scaffold alone would collapse them.
   */
  @Test
  public void candidatesThatAreDifferentScaffoldsAreNeverMerged() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R", List.of("H")));
    List<IAtomContainer> candidates =
        List.of(scaffoldFromSmiles("Fc1ccccc1", "R", 3), scaffoldFromSmiles("Clc1ccccc1", "R", 3));

    List<IAtomContainer> results =
        handler.replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>());

    assertEquals(2, results.size(), "candidates drawn on different skeletons stay apart");
  }

  /** Benzene with {@code R1} and {@code R2} para to each other, laid out so grafting can work. */
  private static IAtomContainer twoResidueScaffold() throws CDKException {
    return residueScaffold(Map.of("R1", 0, "R2", 3));
  }

  /** Benzene carrying the given residues on the given ring atoms, laid out for grafting. */
  private static IAtomContainer residueScaffold(Map<String, Integer> residues) throws CDKException {
    IAtomContainer scaffold =
        new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("c1ccccc1");
    for (Map.Entry<String, Integer> entry : residues.entrySet()) {
      IPseudoAtom residue =
          SilentChemObjectBuilder.getInstance().newInstance(IPseudoAtom.class, entry.getKey());
      residue.setLabel(entry.getKey());
      IAtom anchor = scaffold.getAtom(entry.getValue());
      scaffold.addAtom(residue);
      scaffold.addBond(new Bond(anchor, residue, IBond.Order.SINGLE));
      anchor.setImplicitHydrogenCount(0);
    }
    new StructureDiagramGenerator().generateCoordinates(scaffold);
    return scaffold;
  }

  /** Two R-groups each listing several substituents are a family claim, not a compound list. */
  @Test
  public void twoVaryingRGroupsAreNotExpanded() throws Exception {
    Map<String, List<String>> definitions =
        Map.of("R1", List.of("Cl", "Br"), "R2", List.of("F", "I"));

    assertTrue(
        handlerWith(definitions).replaceRGroups(twoResidueScaffold()).isEmpty(),
        "R1 = Cl, Br; R2 = F, I must not be expanded");
    assertEquals(
        4,
        handlerWith(definitions, true).replaceRGroups(twoResidueScaffold()).size(),
        "unrestricted, the cartesian product is enumerated");
  }

  /** One varying R-group beside fixed ones names each compound, and is expanded. */
  @Test
  public void oneVaryingRGroupBesideFixedOnesIsExpanded() throws Exception {
    MarkushHandler handler =
        handlerWith(Map.of("R1", List.of("Cl", "Br", "I"), "R2", List.of("F")));

    List<IAtomContainer> results = handler.replaceRGroups(twoResidueScaffold());

    assertEquals(3, results.size(), "R1 = Cl, Br, I; R2 = F gives three compounds");
    results.forEach(MarkushHandlerTest::assertNoPseudoAtoms);
  }

  /** R-groups that each have a single substituent name exactly one compound. */
  @Test
  public void singleValuedRGroupsAreExpanded() throws Exception {
    MarkushHandler handler = handlerWith(Map.of("R1", List.of("Cl"), "R2", List.of("F")));

    List<IAtomContainer> results = handler.replaceRGroups(twoResidueScaffold());

    assertEquals(1, results.size(), "R1 = Cl; R2 = F gives one compound");
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("Fc1ccc(Cl)cc1")),
        canonicalSmiles(results.getFirst()));
  }

  /**
   * On a position-variation attachment the drawing leaves the carrying atom open, so only values
   * that name their position or are hydrogen apply there; {@code Me} is dropped.
   */
  @Test
  public void positionVariationTakesOnlyPositionedOrHydrogenValues() throws Exception {
    List<IAtomContainer> candidates =
        List.of(
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 1),
            scaffoldFromSmiles("c1ccc2[nH]ccc2c1", "R", 6));

    assertTrue(
        handlerWith(Map.of("R", List.of("Me")))
            .replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>())
            .isEmpty(),
        "an unplaced Me on a position variation must not be expanded");

    List<IAtomContainer> results =
        handlerWith(Map.of("R", List.of("5-Me", "Me", "H")))
            .replaceRGroups(candidates, rect(0, 0, 40, 40), new HashSet<>());
    assertEquals(2, results.size(), "5-Me and H apply, Me is dropped");
  }

  /**
   * A legend naming ring positions describes an R-group on a ring. On a scaffold whose R-group is
   * drawn off any ring only its hydrogen entry would graft, which is not what the legend states, so
   * it is not applied there (22-9-i2: R on the sulfur of the reaction scheme).
   */
  @Test
  public void positionalLegendIsNotAppliedToAnOffRingRGroup() throws Exception {
    Map<String, List<String>> definitions = Map.of("R", List.of("H", "p-OMe"));

    assertTrue(
        handlerWith(definitions)
            .replaceRGroups(scaffoldFromSmiles("CC(=O)NS", "R", 4), rect(0, 0, 40, 40))
            .isEmpty(),
        "R = H, p-OMe must not be applied to an R on sulfur");
    assertEquals(
        2,
        handlerWith(definitions)
            .replaceRGroups(scaffoldFromSmiles("CSc1ccccc1", "R", 3), rect(0, 0, 40, 40))
            .size(),
        "on an aryl R both entries apply");
    assertEquals(
        1,
        handlerWith(definitions, true)
            .replaceRGroups(scaffoldFromSmiles("CC(=O)NS", "R", 4), rect(0, 0, 40, 40))
            .size(),
        "unrestricted, the hydrogen entry still grafts");
  }

  /**
   * A value such as {@code o-Cl-Ph-} names a substituted phenyl group, not a position on the
   * scaffold: it grafts as 2-chlorophenyl even where R is off every ring (22-9-i3, 3a'-3g'). The
   * locant may also be written 2-4 or ortho/meta/para, the phenyl {@code C6H4}, and the substituent
   * after the phenyl ({@code p-PhNO2}, {@code p-C6H4(OMe)}).
   */
  @Test
  public void substitutedPhenylValueGraftsAsAnArylGroup() throws Exception {
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("o-Cl-Ph-", "CC(=O)c1ccccc1Cl");
    expected.put("m-Br-Ph-", "CC(=O)c1cccc(Br)c1");
    expected.put("p-OMe-Ph-", "CC(=O)c1ccc(OC)cc1");
    expected.put("p-OMe-Ph", "CC(=O)c1ccc(OC)cc1");
    expected.put("p-ClPh", "CC(=O)c1ccc(Cl)cc1");
    expected.put("2-F-Ph", "CC(=O)c1ccccc1F");
    expected.put("3-CF3Ph", "CC(=O)c1cccc(C(F)(F)F)c1");
    expected.put("ortho-Br-Ph-", "CC(=O)c1ccccc1Br");
    expected.put("meta-I-Ph", "CC(=O)c1cccc(I)c1");
    expected.put("para-NO2-Ph", "CC(=O)c1ccc([N+](=O)[O-])cc1");
    // The phenyl may also be written C6H4, and the substituent may follow it (m28188630-7).
    expected.put("p-C6H4(Cl)", "CC(=O)c1ccc(Cl)cc1");
    expected.put("p-C6H4(OMe)", "CC(=O)c1ccc(OC)cc1");
    expected.put("p-C6H4(Me)", "CC(=O)c1ccc(C)cc1");
    expected.put("o-C6H4Br", "CC(=O)c1ccccc1Br");
    expected.put("4-ClC6H4", "CC(=O)c1ccc(Cl)cc1");
    expected.put("m-F-C6H4-", "CC(=O)c1cccc(F)c1");
    expected.put("p-PhNO2", "CC(=O)c1ccc([N+](=O)[O-])cc1");
    expected.put("m-Ph-Br", "CC(=O)c1cccc(Br)c1");
    expected.put("2-Ph(CF3)", "CC(=O)c1ccccc1C(F)(F)F");
    SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    for (Map.Entry<String, String> entry : expected.entrySet()) {
      List<IAtomContainer> results =
          handlerWith(Map.of("R", List.of(entry.getKey())))
              .replaceRGroups(scaffoldFromSmiles("CC=O", "R", 1), rect(0, 0, 40, 40));

      assertEquals(1, results.size(), entry.getKey() + " must graft on an R off every ring");
      assertNoPseudoAtoms(results.getFirst());
      assertEquals(
          canonicalSmiles(parser.parseSmiles(entry.getValue())),
          canonicalSmiles(results.getFirst()),
          entry.getKey() + " must graft as the substituted phenyl it names");
    }
    // A lone O, S or N is a linker to the phenyl, which then sits on the named scaffold position.
    assertTrue(
        handlerWith(Map.of("R", List.of("p-OPh")))
            .replaceRGroups(scaffoldFromSmiles("CC=O", "R", 1), rect(0, 0, 40, 40))
            .isEmpty(),
        "p-OPh names a scaffold position, not 4-hydroxyphenyl");
  }

  /**
   * Thioanisole with {@code R} drawn on the meta carbon next to ortho carbon {@code c3}, and the
   * S-methyl moved onto the spot outside {@code c3} where a substituent placed there would go, the
   * way the carbonyl O sits over the ring in 22-9-i2.
   */
  private static IAtomContainer scaffoldWithBlockedOrtho(String smiles) throws CDKException {
    IAtomContainer scaffold = scaffoldFromSmiles(smiles, "R", 4);
    IAtom ortho = scaffold.getAtom(3);
    Point2d centre = new Point2d();
    List<IAtom> ring = List.of(2, 3, 4, 5, 6, 7).stream().map(scaffold::getAtom).toList();
    ring.forEach(atom -> centre.add(atom.getPoint2d()));
    centre.scale(1.0 / ring.size());
    Vector2d outward = new Vector2d(ortho.getPoint2d());
    outward.sub(centre);
    outward.normalize();
    outward.scale(ortho.getPoint2d().distance(scaffold.getAtom(2).getPoint2d()));
    Point2d outside = new Point2d(ortho.getPoint2d());
    outside.add(outward);
    scaffold.getAtom(0).setPoint2d(outside);
    return scaffold;
  }

  /** The distance from the atom to the nearest atom it is not bonded to, in mean bond lengths. */
  private static double clearance(IAtomContainer container, IAtom atom) {
    double bondLength = 0;
    for (IBond bond : container.bonds()) {
      bondLength += bond.getBegin().getPoint2d().distance(bond.getEnd().getPoint2d());
    }
    bondLength /= container.getBondCount();
    double nearest = Double.MAX_VALUE;
    for (IAtom other : container.atoms()) {
      if (other != atom && container.getBond(atom, other) == null) {
        nearest = Math.min(nearest, atom.getPoint2d().distance(other.getPoint2d()));
      }
    }
    return nearest / bondLength;
  }

  private static IAtom firstOf(IAtomContainer container, String symbol) {
    for (IAtom atom : container.atoms()) {
      if (symbol.equals(atom.getSymbol())) {
        return atom;
      }
    }
    throw new AssertionError("no " + symbol + " in the structure");
  }

  /**
   * On a ring symmetric about its attachment, ortho positions 2 and 6 are the same compound, so the
   * substituent goes to the one with room rather than onto an atom drawn over the other.
   */
  @Test
  public void orthoSubstituentTakesTheFreeSideOfASymmetricRing() throws Exception {
    List<IAtomContainer> results =
        handlerWith(Map.of("R", List.of("o-F")))
            .replaceRGroups(scaffoldWithBlockedOrtho("CSc1ccccc1"), rect(0, 0, 40, 40));

    assertEquals(1, results.size());
    IAtomContainer product = results.get(0);
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance()).parseSmiles("CSc1ccccc1F")),
        canonicalSmiles(product));
    assertTrue(
        clearance(product, firstOf(product, "F")) > 0.5, "the F must not sit on the S-methyl");
  }

  /**
   * A second R-group on the ring makes ortho positions 2 and 6 different compounds, and the drawn R
   * says which is meant, even where the layout is then crowded.
   */
  @Test
  public void orthoSubstituentFollowsTheDrawnSideOfAnAsymmetricRing() throws Exception {
    IAtomContainer scaffold = scaffoldWithBlockedOrtho("CSc1ccccc1");
    IAtom meta = scaffold.getAtom(6);
    IPseudoAtom other = SilentChemObjectBuilder.getInstance().newInstance(IPseudoAtom.class, "R1");
    other.setLabel("R1");
    Point2d point = new Point2d(meta.getPoint2d());
    point.scale(1.5);
    other.setPoint2d(point);
    scaffold.addAtom(other);
    scaffold.addBond(new Bond(meta, other, IBond.Order.SINGLE));
    meta.setImplicitHydrogenCount(0);

    List<IAtomContainer> results =
        handlerWith(Map.of("R", List.of("o-F"), "R1", List.of("Cl")))
            .replaceRGroups(scaffold, rect(0, 0, 40, 40));

    assertEquals(1, results.size());
    assertEquals(
        canonicalSmiles(
            new SmilesParser(SilentChemObjectBuilder.getInstance())
                .parseSmiles("CSc1c(F)ccc(Cl)c1")),
        canonicalSmiles(results.get(0)),
        "F goes on the ortho carbon next to the drawn R");
  }
}
