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
package org.beilstein.chemxtract.cdx.datatypes;

/** The type of orbital object. */
public enum CDOrbitalType {
  /** s orbital */
  s,
  /** Oval-shaped sigma or pi orbital */
  oval,
  /** One lobe of a p orbital */
  lobe,
  /** Complete p orbital */
  p,
  /** hydrid orbital */
  hybridPlus,
  /** hydrid orbital (opposite shading) */
  hybridMinus,
  /** d<sub>z<sup>2</sup></sub> orbital */
  dz2Plus,
  /** d<sub>z<sup>2</sup></sub> orbital (opposite shading) */
  dz2Minus,
  /** d<sub>xy</sub> orbital */
  dxy,
  /** shaded s orbital */
  sShaded,
  /** shaded Oval-shaped sigma or pi orbital */
  ovalShaded,
  /** shaded single lobe of a p orbital */
  lobeShaded,
  /** shaded Complete p orbital */
  pShaded,
  /** filled s orbital */
  sFilled,
  /** filled Oval-shaped sigma or pi orbital */
  ovalFilled,
  /** filled single lobe of a p orbital */
  lobeFilled,
  /** filled Complete p orbital */
  pFilled,
  /** filled hydrid orbital */
  hybridPlusFilled,
  /** filled hydrid orbital (opposite shading) */
  hybridMinusFilled,
  /** filled d<sub>z<sup>2</sup></sub> orbital */
  dz2PlusFilled,
  /** filled d<sub>z<sup>2</sup></sub> orbital (opposite shading) */
  dz2MinusFilled,
  /** filled d<sub>xy</sub> orbital */
  dxyFilled;
}
