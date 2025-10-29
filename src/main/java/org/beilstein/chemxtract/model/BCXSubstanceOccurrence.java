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
package org.beilstein.chemxtract.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * A holder for substance coordinates (original CDX coordinates). While the same substance is
 * considered unique across one or more CDX schemes (due to its unique Inchi key), it may have
 * several occurrences with different coordinates within one or more CDX schemes.
 */
public class BCXSubstanceOccurrence implements Serializable {

  @Serial private static final long serialVersionUID = -142252683267224832L;

  /** The coordinates in the CDX file */
  private float cdxTop;

  private float cdxLeft;
  private float cdxBottom;
  private float cdxRight;

  public BCXSubstanceOccurrence() {
    super();
  }

  public BCXSubstanceOccurrence(float cdxTop, float cdxLeft, float cdxBottom, float cdxRight) {
    super();
    this.cdxTop = cdxTop;
    this.cdxLeft = cdxLeft;
    this.cdxBottom = cdxBottom;
    this.cdxRight = cdxRight;
  }

  public float getCdxTop() {
    return cdxTop;
  }

  public void setCdxTop(float cdxTop) {
    this.cdxTop = cdxTop;
  }

  public float getCdxLeft() {
    return cdxLeft;
  }

  public void setCdxLeft(float cdxLeft) {
    this.cdxLeft = cdxLeft;
  }

  public float getCdxBottom() {
    return cdxBottom;
  }

  public void setCdxBottom(float cdxBottom) {
    this.cdxBottom = cdxBottom;
  }

  public float getCdxRight() {
    return cdxRight;
  }

  public void setCdxRight(float cdxRight) {
    this.cdxRight = cdxRight;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(cdxBottom);
    result = prime * result + Float.floatToIntBits(cdxLeft);
    result = prime * result + Float.floatToIntBits(cdxRight);
    result = prime * result + Float.floatToIntBits(cdxTop);
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
    BCXSubstanceOccurrence other = (BCXSubstanceOccurrence) obj;
    if (Float.floatToIntBits(cdxBottom) != Float.floatToIntBits(other.cdxBottom)) {
      return false;
    }
    if (Float.floatToIntBits(cdxLeft) != Float.floatToIntBits(other.cdxLeft)) {
      return false;
    }
    if (Float.floatToIntBits(cdxRight) != Float.floatToIntBits(other.cdxRight)) {
      return false;
    }
    if (Float.floatToIntBits(cdxTop) != Float.floatToIntBits(other.cdxTop)) {
      return false;
    }
    return true;
  }
}
