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
 * The type of curve object. If a curve has arrows of any type, it cannot also be closed, filled,
 * shaded, or doubled. If it has an arrow at start or end, it cannot also have a half-arrow at the
 * same location. A dashed spline cannot be filled, shaded, or doubled.
 */
public class CDSplineType {
  private boolean dashed = false;
  private boolean bold = false;
  private boolean doubled = false;

  private boolean closed = false;
  private boolean filled = false;
  private boolean shaded = false;

  private boolean arrowAtStart = false;
  private boolean arrowAtEnd = false;
  private boolean halfArrowAtStart = false;
  private boolean halfArrowAtEnd = false;

  public boolean isPlain() {
    return !isDashed() && !isBold() && !isDoubled() && !isClosed() && !isFilled() && !isShaded() && !isArrowAtStart() && !isArrowAtEnd() &&
            !isHalfArrowAtStart() && !isHalfArrowAtEnd();
  }

  public void setPlain(boolean plain) {
    if (plain) {
      setDashed(false);
      setBold(false);
      setDoubled(false);
      setClosed(false);
      setFilled(false);
      setShaded(false);
      setArrowAtStart(false);
      setArrowAtEnd(false);
      setHalfArrowAtStart(false);
      setHalfArrowAtEnd(false);
    }
  }

  public CDLineType getLineType() {
    CDLineType lineType = new CDLineType();
    lineType.setDashed(isDashed());
    lineType.setBold(isBold());
    return lineType;
  }

  public CDFillType getFillType() {
    if (isFilled()) {
      return CDFillType.Solid;
    }
    if (isShaded()) {
      return CDFillType.Shaded;
    }
    return CDFillType.None;
  }

  public boolean isDashed() {
    return dashed;
  }

  public void setDashed(boolean dashed) {
    this.dashed = dashed;
  }

  public boolean isBold() {
    return bold;
  }

  public void setBold(boolean bold) {
    this.bold = bold;
  }

  public boolean isDoubled() {
    return doubled;
  }

  public void setDoubled(boolean doubled) {
    this.doubled = doubled;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  public boolean isFilled() {
    return filled;
  }

  public void setFilled(boolean filled) {
    this.filled = filled;
  }

  public boolean isShaded() {
    return shaded;
  }

  public void setShaded(boolean shaded) {
    this.shaded = shaded;
  }

  public boolean isArrowAtStart() {
    return arrowAtStart;
  }

  public void setArrowAtStart(boolean arrowAtStart) {
    this.arrowAtStart = arrowAtStart;
  }

  public boolean isArrowAtEnd() {
    return arrowAtEnd;
  }

  public void setArrowAtEnd(boolean arrowAtEnd) {
    this.arrowAtEnd = arrowAtEnd;
  }

  public boolean isHalfArrowAtStart() {
    return halfArrowAtStart;
  }

  public void setHalfArrowAtStart(boolean halfArrowAtStart) {
    this.halfArrowAtStart = halfArrowAtStart;
  }

  public boolean isHalfArrowAtEnd() {
    return halfArrowAtEnd;
  }

  public void setHalfArrowAtEnd(boolean halfArrowAtEnd) {
    this.halfArrowAtEnd = halfArrowAtEnd;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (arrowAtEnd ? 1231 : 1237);
    result = prime * result + (arrowAtStart ? 1231 : 1237);
    result = prime * result + (bold ? 1231 : 1237);
    result = prime * result + (closed ? 1231 : 1237);
    result = prime * result + (dashed ? 1231 : 1237);
    result = prime * result + (doubled ? 1231 : 1237);
    result = prime * result + (filled ? 1231 : 1237);
    result = prime * result + (halfArrowAtEnd ? 1231 : 1237);
    result = prime * result + (halfArrowAtStart ? 1231 : 1237);
    result = prime * result + (shaded ? 1231 : 1237);
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
    final CDSplineType other = (CDSplineType) obj;
    if (arrowAtEnd != other.arrowAtEnd) {
      return false;
    }
    if (arrowAtStart != other.arrowAtStart) {
      return false;
    }
    if (bold != other.bold) {
      return false;
    }
    if (closed != other.closed) {
      return false;
    }
    if (dashed != other.dashed) {
      return false;
    }
    if (doubled != other.doubled) {
      return false;
    }
    if (filled != other.filled) {
      return false;
    }
    if (halfArrowAtEnd != other.halfArrowAtEnd) {
      return false;
    }
    if (halfArrowAtStart != other.halfArrowAtStart) {
      return false;
    }
    if (shaded != other.shaded) {
      return false;
    }
    return true;
  }

}
