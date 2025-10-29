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

import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;

/** Used for ChemDraw templates. */
public class CDTemplateGrid {

  private CDPoint2D extent;
  private float paneHeight;
  private int numRows;
  private int numColumns;

  public CDPoint2D getExtent() {
    return extent;
  }

  public void setExtent(CDPoint2D extent) {
    this.extent = extent;
  }

  public float getPaneHeight() {
    return paneHeight;
  }

  public void setPaneHeight(float paneHeight) {
    this.paneHeight = paneHeight;
  }

  public int getNumRows() {
    return numRows;
  }

  public void setNumRows(int numRows) {
    this.numRows = numRows;
  }

  public int getNumColumns() {
    return numColumns;
  }

  public void setNumColumns(int numColumns) {
    this.numColumns = numColumns;
  }
}
