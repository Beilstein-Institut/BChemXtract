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
 * The type of graphical object.
 */
public enum CDGraphicType{
  /** Undefined. */
  Undefined,
  /**
   * Line. ({@link org.beilstein.boa.io.cdx.CDGraphic#getLineType()} should be present.
   * {@link org.beilstein.boa.io.cdx.CDGraphic#getArrowType()} may also be present.)
   * The two points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()}
   * represent the start and end of the line.
   */
  Line,
  /**
   * Arc. ({@link org.beilstein.boa.io.cdx.CDGraphic#getLineType()} should be present.
   * {@link org.beilstein.boa.io.cdx.CDGraphic#getArcAngularSize()} should be present.
   * {@link org.beilstein.boa.io.cdx.CDGraphic#getArrowType()} may also be present.) The
   * two points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()}
   * represent the center and end of he arc.
   */
  Arc,
  /**
   * Rectangle. ({@link org.beilstein.boa.io.cdx.CDGraphic#getRectangleType()} should be present)
   * The two points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()} represent
   * two opposing corners of the rectangle.
   */
  Rectangle,
  /**
   * Oval. ({@link org.beilstein.boa.io.cdx.CDGraphic#getOvalType()} should be present) The two points
   * stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()} represent the center and
   * semimajor end of the oval.
   */
  Oval,
  /**
   * Orbital. ({@link org.beilstein.boa.io.cdx.CDGraphic#getOrbitalType()} should be present) The two
   * points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()} represent the center
   * and end of the orbital.
   */
  Orbital,
  /**
   * Bracket. ({@link org.beilstein.boa.io.cdx.CDGraphic#getBracketType()} should be present) The two
   * points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()} represent the two
   * ends of the bracket.
   */
  Bracket,
  /**
   * Symbol. ({@link org.beilstein.boa.io.cdx.CDGraphic#getSymbolType()} should be present) The two
   * points stored within the {@link org.beilstein.boa.io.cdx.CDGraphic#getBounds()} represent the
   * center of the symbol and a second point indicating the symbol's size.
   */
  Symbol;
}
