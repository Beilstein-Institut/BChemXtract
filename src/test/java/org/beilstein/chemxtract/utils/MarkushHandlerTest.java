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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.visitor.FragmentVisitor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.exception.CDKException;
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
   * m48800432 has two separate scaffolds that both use label R, defined by two different legends.
   * Per the {@code putIfAbsent} bug these collapsed to one meaning; scoping must give each scaffold
   * its own R value set.
   */
  @Test
  public void scopesRealMultiScaffoldPage() throws IOException {
    InputStream in =
        MarkushHandlerTest.class.getResourceAsStream("/cheminf/bugs/markush/m48800432-2.cdx");
    assertNotNull(in, "fixture missing on classpath");
    CDDocument document = CDXReader.readDocument(in);

    List<List<String>> rOnlyValueSets = new ArrayList<>();
    for (CDPage page : document.getPages()) {
      MarkushHandler handler = new MarkushHandler(page, SilentChemObjectBuilder.getInstance());
      for (CDFragment fragment : new FragmentVisitor(page).getFragments()) {
        if (rLabelsOf(fragment).equals(List.of("R"))) {
          rOnlyValueSets.add(handler.residueLabelsNear(fragment.getBounds()).get("R"));
        }
      }
    }

    assertEquals(2, rOnlyValueSets.size(), "expected two R-only scaffolds");
    assertTrue(
        rOnlyValueSets.stream().anyMatch(v -> v.size() == 8 && v.contains("F")),
        "one scaffold must see the 8-value R legend (incl. F): " + rOnlyValueSets);
    assertTrue(
        rOnlyValueSets.stream().anyMatch(v -> v.size() == 6 && !v.contains("F")),
        "the other must see the 6-value R legend (no F): " + rOnlyValueSets);
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

  private static List<String> rLabelsOf(CDFragment fragment) {
    List<String> labels = new ArrayList<>();
    for (CDAtom atom : fragment.getAtoms()) {
      String label = null;
      try {
        if (atom.getText() != null && atom.getText().getText() != null) {
          label = atom.getText().getText().getText();
        }
      } catch (RuntimeException ignored) {
        label = null;
      }
      if (label != null && label.matches("^(R|X|Y|Ar|E|L)\\d*$")) {
        labels.add(label);
      }
    }
    return labels;
  }
}
