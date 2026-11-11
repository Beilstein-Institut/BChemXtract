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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.junit.jupiter.api.Test;

class DrawnRGroupValuesTest {

  /** A chain ending on one wavy line is one drawn substituent. */
  @Test
  void groupWithOneEndOnAWavyLineIsADrawing() {
    CDAtom a = carbon(0, 0);
    CDAtom b = carbon(1, 0);
    CDAtom end = carbon(2, 0);
    CDFragment group = fragment(List.of(a, b, end), List.of(bond(a, b), bond(b, end)));

    List<DrawnRGroupValues.Drawing> drawings =
        DrawnRGroupValues.find(List.of(group, wavyLineAt(2, 0)));

    assertEquals(1, drawings.size(), "one end on a wavy line");
    assertEquals(end, drawings.get(0).end(), "the end on the wavy line");
  }

  /**
   * A group with three ends on wavy lines bridges three scaffold atoms; it is not a single
   * substituent, however many ends lie on lines.
   */
  @Test
  void groupWithThreeEndsOnWavyLinesIsNoDrawing() {
    CDAtom centre = carbon(0, 0);
    CDAtom right = carbon(1, 0);
    CDAtom left = carbon(-1, 0);
    CDAtom down = carbon(0, 1);
    CDFragment group =
        fragment(
            List.of(centre, right, left, down),
            List.of(bond(centre, right), bond(centre, left), bond(centre, down)));

    List<DrawnRGroupValues.Drawing> drawings =
        DrawnRGroupValues.find(
            List.of(group, wavyLineAt(1, 0), wavyLineAt(-1, 0), wavyLineAt(0, 1)));

    assertTrue(drawings.isEmpty(), "three ends on wavy lines: " + drawings.size());
  }

  private static CDAtom carbon(float x, float y) {
    CDAtom atom = new CDAtom();
    atom.setPosition2D(new CDPoint2D(x, y));
    return atom;
  }

  private static CDBond bond(CDAtom begin, CDAtom end) {
    CDBond bond = new CDBond();
    bond.setBegin(begin);
    bond.setEnd(end);
    return bond;
  }

  private static CDFragment fragment(List<CDAtom> atoms, List<CDBond> bonds) {
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(new ArrayList<>(atoms));
    bonds.forEach(fragment::addBond);
    return fragment;
  }

  /** A wavy line of unit length crossing the point at a right angle to the x axis. */
  private static CDFragment wavyLineAt(float x, float y) {
    CDAtom top = carbon(x, y - 0.5f);
    CDAtom bottom = carbon(x, y + 0.5f);
    CDBond wavy = bond(top, bottom);
    wavy.setBondDisplay(CDBondDisplay.Wavy);
    return fragment(List.of(top, bottom), List.of(wavy));
  }
}
