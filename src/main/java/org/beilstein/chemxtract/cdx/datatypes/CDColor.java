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

/** An RGB color. */
public class CDColor {

  private static final float DELTA = 0.001f;

  public static final CDColor WHITE = new CDColor(1, 1, 1);
  public static final CDColor BLACK = new CDColor(0, 0, 0);
  public static final CDColor RED = new CDColor(1, 0, 0);
  public static final CDColor YELLOW = new CDColor(1, 1, 0);
  public static final CDColor GREEN = new CDColor(0, 1, 0);
  public static final CDColor CYAN = new CDColor(0, 1, 1);
  public static final CDColor BLUE = new CDColor(0, 0, 1);
  public static final CDColor MAGENTA = new CDColor(1, 0, 1);

  private float myRed;
  private float myGreen;
  private float myBlue;

  public CDColor() {}

  public CDColor(float red, float green, float blue) {
    this.myRed = red;
    this.myGreen = green;
    this.myBlue = blue;
  }

  public float getRed() {
    return myRed;
  }

  public void setRed(float red) {
    this.myRed = red;
  }

  public float getGreen() {
    return myGreen;
  }

  public void setGreen(float green) {
    this.myGreen = green;
  }

  public float getBlue() {
    return myBlue;
  }

  public void setBlue(float blue) {
    this.myBlue = blue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (myBlue / DELTA);
    result = prime * result + (int) (myGreen / DELTA);
    result = prime * result + (int) (myRed / DELTA);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CDColor other = (CDColor) obj;

    if (!(Math.abs(myBlue - other.myBlue) <= DELTA)) {
      return false;
    }

    if (!(Math.abs(myGreen - other.myGreen) <= DELTA)) {
      return false;
    }

    if (!(Math.abs(myRed - other.myRed) <= DELTA)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Color[red=" + myRed + " green=" + myGreen + " blue=" + myBlue + "]";
  }
}
