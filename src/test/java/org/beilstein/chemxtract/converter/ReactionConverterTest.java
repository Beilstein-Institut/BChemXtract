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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openscience.cdk.interfaces.IChemObjectBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ReactionConverterTest {

  // Mocks required to satisfy the constructor
  @Mock private Map<CDFragment, BCXSubstance> mockFragmentMap;
  @Mock private IChemObjectBuilder mockBuilder;

  private ReactionConverter converter;

  @Before
  public void setUp() {
    // Instantiate the class with mocks.
    // We set 'sanitize' to false as it doesn't affect the geometry logic.
    converter = new ReactionConverter(mockFragmentMap, mockBuilder, false);
  }

  /** Helper method to access and invoke the private 'intersectsRectangle' method via Reflection. */
  private boolean invokeIntersects(
      double gx1,
      double gy1,
      double gx2,
      double gy2,
      double rx1,
      double ry1,
      double rx2,
      double ry2)
      throws Exception {

    Method method =
        ReactionConverter.class.getDeclaredMethod(
            "intersectsRectangle",
            double.class,
            double.class,
            double.class,
            double.class,
            double.class,
            double.class,
            double.class,
            double.class);

    method.setAccessible(true);
    return (boolean) method.invoke(converter, gx1, gy1, gx2, gy2, rx1, ry1, rx2, ry2);
  }

  @Test
  public void testDiagonalIntersection() throws Exception {
    // Rectangle: (0,0) to (10,10)
    // Line: (-5,-5) to (15,15) -> Perfectly diagonal
    assertTrue("Diagonal line should intersect", invokeIntersects(-5, -5, 15, 15, 0, 0, 10, 10));
  }

  @Test
  public void testNoIntersection() throws Exception {
    // Rectangle: (0,0) to (10,10)
    // Line: (12,0) to (12,10) -> Vertical line at x=12 (outside)
    assertFalse(
        "Line outside rectangle should not intersect",
        invokeIntersects(12, 0, 12, 10, 0, 0, 10, 10));
  }

  @Test
  public void testInfiniteLineLogic() throws Exception {
    // Rectangle: (0,0) to (10,10)
    // Segment: (20,20) to (30,30)
    // NOTE: The segment is far away, but the INFINITE line (y=x) hits the rect.
    // Your current implementation checks the infinite line.
    assertTrue(
        "Infinite line extension should intersect", invokeIntersects(20, 20, 30, 30, 0, 0, 10, 10));
  }

  @Test
  public void testEdgeIntersection() throws Exception {
    // Rectangle: (0,0) to (10,10)
    // Line: (0,0) to (0,10) -> Left vertical edge
    assertTrue("Line touching edge should intersect", invokeIntersects(0, 0, 0, 10, 0, 0, 10, 10));
  }

  @Test
  public void testUnorderedRectangleCoordinates() throws Exception {
    // Rectangle defined via (10,10) and (0,0) instead of (0,0) and (10,10)
    // Line: (5, -5) to (5, 15) -> Vertical cut through middle
    assertTrue(
        "Unordered coordinates should be handled correctly",
        invokeIntersects(5, -5, 5, 15, 10, 10, 0, 0));
  }
}
