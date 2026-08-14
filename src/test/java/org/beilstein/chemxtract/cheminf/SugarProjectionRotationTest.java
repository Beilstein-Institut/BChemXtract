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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Specifies how a sugar ring drawn in a chair conformation behaves when the drawing is not laid out
 * horizontally on the page.
 *
 * <p>Reported concern: a chair tilted by a few degrees might drop out of {@link
 * SugarProjectionDetector}'s cardinality window and be treated as a non-sugar. It does not - tilt
 * is handled. Specified behaviour, measured by rotating the fixture in 5 degree steps over the full
 * circle:
 *
 * <ul>
 *   <li>|angle| &lt; 90 degrees - ring detected, stereochemistry unchanged. Chair recognition reads
 *       the winding of the ring, which is rotation invariant, and CDK de-rotates each substituent
 *       vector by the measured axis angle before classifying it.
 *   <li>|angle| == 90 degrees - ring still detected, but no stereo is emitted. A near vertical
 *       chair axis is genuinely ambiguous and CDK refuses to guess rather than assign.
 *   <li>|angle| &gt; 90 degrees - ring still detected, and the enantiomer is extracted.
 * </ul>
 *
 * <p>The third case is the drawing convention, not a defect. The chair ring outline is exactly
 * centrosymmetric, so a half turn maps it onto itself and no ring geometry can tell the two
 * orientations apart. In a chair the axial bond points up at a peak vertex and down at a valley
 * vertex; a half turn swaps every atom between peak and valley while its substituent swaps between
 * up and down, so the implied z of every atom flips. Flipping all z is a reflection, hence the
 * enantiomer - an upside down drawing of D-glucose is the conventional drawing of L-glucose, the
 * same way a Fischer projection rotated by 90 degrees denotes a different compound.
 *
 * <p>Forcing rotation invariance is therefore not possible without inventing an arbitrary "up",
 * which would silently invert normally drawn structures. See {@code
 * doc/sugar-projection-orientation.md}.
 */
public class SugarProjectionRotationTest {

  /** beta-D-glucopyranose chair, drawn with an upward anomeric bond. */
  private static final String CHAIR_FIXTURE = "/integrationTests/sugars/bond_up_SRSSR.cdx";

  /** InChIKey of the fixture as drawn. */
  private static final String EXPECTED_KEY = "WQZGKKKJIJFFOK-DVKNGEFBSA-N";

  /** Same skeleton, all stereo centres inverted - the enantiomer. */
  private static final String ENANTIOMER_KEY = "WQZGKKKJIJFFOK-MDMQIMBFSA-N";

  /** Same skeleton without any stereo layer. */
  private static final String NO_STEREO_KEY = "WQZGKKKJIJFFOK-UHFFFAOYSA-N";

  /** Tilts a user might plausibly apply to a drawing, in degrees. */
  private static final double[] TILTS = {1, 2, 5, 10, 15, 30, 45, 60, 85, -7, -30, -85};

  /** Rotations that turn the drawing upside down or onto its side. */
  private static final double[] FLIPS = {90, -90, 135, 180, -180};

  /**
   * Chair recognition is derived from the winding pattern of the ring, which is invariant under
   * rotation. Detection must therefore survive any rotation, including the ones the up/down reading
   * further down the pipeline treats differently.
   */
  @Test
  public void chairIsDetectedAtAnyRotationTest() throws IOException {
    assertTrue(
        new SugarProjectionDetector(containerOf(readFixture())).containsChairProjections(),
        "precondition: unrotated fixture must be recognised as a chair");

    for (double angle : concat(TILTS, FLIPS)) {
      IAtomContainer rotated = containerOf(readFixture());
      rotateContainer(rotated, angle);
      assertTrue(
          new SugarProjectionDetector(rotated).containsChairProjections(),
          "chair must still be detected after rotating by " + angle + " degrees");
    }
  }

  /**
   * A chair that is tilted rather than drawn perfectly horizontally must extract to the same
   * structure. This is the case the user reported; it holds for every tilt short of a quarter turn.
   */
  @Test
  public void tiltedChairKeepsStereochemistryTest() throws IOException {
    assertEquals(
        EXPECTED_KEY,
        extract(readFixture()).get(0).getInchiKey(),
        "precondition: unrotated fixture");

    for (double angle : TILTS) {
      CDDocument document = readFixture();
      rotateDocument(document, angle);
      assertEquals(
          EXPECTED_KEY,
          extract(document).get(0).getInchiKey(),
          "stereochemistry must survive a tilt of " + angle + " degrees");
    }
  }

  /**
   * A chair drawn upside down is the conventional drawing of the enantiomer, and is read as such;
   * with the chair axis vertical the reading is ambiguous and no stereo is assigned. Both follow
   * the chair projection convention - see the class javadoc for why neither can be made rotation
   * invariant - so this test pins the contract rather than a defect.
   */
  @Test
  public void upsideDownChairReadsAsTheEnantiomerTest() throws IOException {
    for (double angle : FLIPS) {
      CDDocument document = readFixture();
      rotateDocument(document, angle);
      boolean vertical = Math.abs(angle) == 90;
      assertEquals(
          vertical ? NO_STEREO_KEY : ENANTIOMER_KEY,
          extract(document).get(0).getInchiKey(),
          vertical
              ? "a vertical chair axis is ambiguous, no stereo may be assigned at " + angle
              : "a chair rotated by " + angle + " degrees denotes the enantiomer");
    }
  }

  /** Guards the rotation helpers: a rigid rotation must not deform the ring. */
  @Test
  public void rotationPreservesGeometryTest() throws IOException {
    IAtomContainer plain = containerOf(readFixture());
    IAtomContainer tilted = containerOf(readFixture());
    rotateContainer(tilted, 23);

    for (int i = 0; i < plain.getAtomCount(); i++) {
      Point2d a = plain.getAtom(i).getPoint2d();
      Point2d b = tilted.getAtom(i).getPoint2d();
      assertNotNull(a, "atom " + i + " has no 2D coordinates");
      assertNotNull(b, "atom " + i + " has no 2D coordinates");
      assertEquals(
          Math.hypot(a.x, a.y),
          Math.hypot(b.x, b.y),
          1e-4,
          "atom " + i + " must keep its distance to the origin");
    }
  }

  private static CDDocument readFixture() throws IOException {
    InputStream in = SugarProjectionRotationTest.class.getResourceAsStream(CHAIR_FIXTURE);
    assertNotNull(in, CHAIR_FIXTURE);
    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document, CHAIR_FIXTURE);
    return document;
  }

  private static List<BCXSubstance> extract(CDDocument document) {
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    assertFalse(substances.isEmpty(), "extraction produced no substance");
    return substances;
  }

  private static IAtomContainer containerOf(CDDocument document) {
    return extract(document).get(0).getAtomContainer();
  }

  /** Rotates every ChemDraw atom position about the page origin. */
  private static void rotateDocument(CDDocument document, double degrees) {
    double rad = Math.toRadians(degrees);
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);
    CDVisitor rotator =
        new CDVisitor() {
          @Override
          public void visitAtom(CDAtom node) {
            CDPoint2D p = node.getPosition2D();
            if (p == null) {
              return;
            }
            node.setPosition2D(
                new CDPoint2D(
                    (float) (p.getX() * cos - p.getY() * sin),
                    (float) (p.getX() * sin + p.getY() * cos)));
          }
        };
    document.getPages().forEach(page -> page.accept(rotator));
  }

  /** Rotates every CDK atom position about the origin. */
  private static void rotateContainer(IAtomContainer container, double degrees) {
    double rad = Math.toRadians(degrees);
    double cos = Math.cos(rad);
    double sin = Math.sin(rad);
    for (IAtom atom : container.atoms()) {
      Point2d p = atom.getPoint2d();
      if (p == null) {
        continue;
      }
      atom.setPoint2d(new Point2d(p.x * cos - p.y * sin, p.x * sin + p.y * cos));
    }
  }

  private static double[] concat(double[] first, double[] second) {
    double[] all = new double[first.length + second.length];
    System.arraycopy(first, 0, all, 0, first.length);
    System.arraycopy(second, 0, all, first.length, second.length);
    return all;
  }
}
