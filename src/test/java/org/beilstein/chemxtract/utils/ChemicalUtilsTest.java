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

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

class ChemicalUtilsTest {

  private static IAtomContainer twoCarbons() {
    IAtomContainer container =
        SilentChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
    container.addAtom(SilentChemObjectBuilder.getInstance().newInstance(IAtom.class, "C"));
    container.addAtom(SilentChemObjectBuilder.getInstance().newInstance(IAtom.class, "C"));
    return container;
  }

  @Test
  void atomsWithinSixDecimalsAreDuplicates() {
    IAtomContainer container = twoCarbons();
    container.getAtom(0).setPoint2d(new Point2d(1.0, 2.0));
    container.getAtom(1).setPoint2d(new Point2d(1.0000001, 2.0000001));

    assertThat(ChemicalUtils.hasDuplicateCoordinates(container))
        .as("atoms closer than 1e-6 coincide")
        .isTrue();
  }

  @Test
  void atomsApartAtSixDecimalsAreDistinct() {
    IAtomContainer container = twoCarbons();
    container.getAtom(0).setPoint2d(new Point2d(1.0, 2.0));
    container.getAtom(1).setPoint2d(new Point2d(1.00001, 2.0));

    assertThat(ChemicalUtils.hasDuplicateCoordinates(container))
        .as("atoms 1e-5 apart are distinct")
        .isFalse();
  }

  @Test
  void threeDimensionalCoordinatesTakePrecedence() {
    IAtomContainer container = twoCarbons();
    container.getAtom(0).setPoint2d(new Point2d(1.0, 2.0));
    container.getAtom(1).setPoint2d(new Point2d(1.0, 2.0));
    container.getAtom(0).setPoint3d(new Point3d(1.0, 2.0, 0.0));
    container.getAtom(1).setPoint3d(new Point3d(1.0, 2.0, 1.0));

    assertThat(ChemicalUtils.hasDuplicateCoordinates(container))
        .as("atoms apart in 3D are distinct even where their 2D points coincide")
        .isFalse();
  }

  @Test
  void atomsWithoutCoordinatesAreIgnored() {
    assertThat(ChemicalUtils.hasDuplicateCoordinates(twoCarbons()))
        .as("atoms without coordinates cannot coincide")
        .isFalse();
  }
}
