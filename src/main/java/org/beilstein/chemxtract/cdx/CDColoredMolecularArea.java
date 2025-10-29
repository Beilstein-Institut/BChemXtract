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

import java.util.List;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;

/** A colored molecular area between bonds, usually the inside of a ring. */
public class CDColoredMolecularArea extends CDObject {

  CDColor backgroundColor;
  List<CDBond> basisObjects;

  public CDColor getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(CDColor backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public List<CDBond> getBasisObjects() {
    return basisObjects;
  }

  public void setBasisObjects(List<CDBond> basisObjects) {
    this.basisObjects = basisObjects;
  }
}
