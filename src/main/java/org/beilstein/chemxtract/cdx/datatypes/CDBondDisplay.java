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
 * The display type of a bond object. ChemDraw does not support all display
 * values (unsupport values are provided for future compatibility). For single
 * bonds, all but Dot and DashDot are supported. For double bonds, Solid, Dash,
 * and Bold are supported, and Wavy is treated as a double-either (crossed)
 * bond. For triple and quadruple bonds, only Solid is supported.
 */
public enum CDBondDisplay{
  /** Solid bond */
  Solid,
  /** Dashed bond */
  Dash,
  /** Hashed bond */
  Hash,
  /** Wedged hashed bond with the narrow end on the "begin" atom */
  WedgedHashBegin,
  /** Wedged hashed bond with the narrow end on the "end" atom */
  WedgedHashEnd,
  /** Bold bond */
  Bold,
  /** Wedged solid bond with the narrow end on the "begin" atom */
  WedgeBegin,
  /** Wedged solid bond with the narrow end on the "end" atom */
  WedgeEnd,
  /** Wavy bond */
  Wavy,
  /** Wedged hollow bond with the narrow end on the "begin" atom */
  HollowWedgeBegin,
  /** Wedged hollow bond with the narrow end on the "end" atom */
  HollowWedgeEnd,
  /** Wedged wavy bond with the narrow end on the "begin" atom */
  @Deprecated
  WavyWedgeBegin,
  /** Wedged wavy bond with the narrow end on the "end" atom */
  @Deprecated
  WavyWedgeEnd,
  /** Dotted bond */
  @Deprecated
  Dot,
  /** Dashed-and-dotted bond */
  @Deprecated
  DashDot;
}
