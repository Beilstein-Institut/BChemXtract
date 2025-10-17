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
package org.beilstein.chemxtract.cdx;

import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDLineType;
import org.beilstein.chemxtract.cdx.datatypes.CDSideType;

/**
 * Groups information about the edge of an object. Usually appears in page
 * objects that are part of a table.
 */
public class CDBorder {

  /** The color of the border line. */
  private CDColor color;
  /** The width of the border line, in points. */
  private float width;

  private CDSideType side;
  private CDLineType lineType;

  public CDColor getForegroundColor() {
    return color;
  }

  public void setColor(CDColor color) {
    this.color = color;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float lineWidth) {
    width = lineWidth;
  }

  public CDSideType getSide() {
    return side;
  }

  public void setSide(CDSideType side) {
    this.side = side;
  }

  public CDLineType getLineType() {
    return lineType;
  }

  public void setLineType(CDLineType lineType) {
    this.lineType = lineType;
  }

}
