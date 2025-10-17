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

/**
 * The number of ring bonds attached to an atom.
 * <p>
 * If posed as a structure query in some database, this atom should not match
 * only those atoms with the specified number of ring bonds.
 * <p>
 * The value stored in this property exactly corresponds to the Ring Bond Count
 * property in ISIS.
 */
public enum CDRingBondCount{
  /** Unspecified number of ring bonds. */
  Unspecified,
  /** Exactly 0 ring bonds. */
  NoRingBonds,
  /** Ring bonds as drawn. */
  AsDrawn,
  /** Exactly 2 ring bonds. */
  SimpleRing,
  /** Exactly 3 ring bonds. */
  Fusion,
  /** 4 or more ring bonds. */
  SpiroOrHigher;
}
