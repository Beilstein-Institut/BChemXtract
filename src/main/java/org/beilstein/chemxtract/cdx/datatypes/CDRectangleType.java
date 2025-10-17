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
 * The type of a rectangle object.
 */
public class CDRectangleType {

  private boolean roundEdge = false;
  private boolean shadow = false;
  private boolean shaded = false;
  private boolean filled = false;
  private boolean dashed = false;
  private boolean bold = false;

  public boolean isPlain() {
    return !isRoundEdge() && !isShadow() && !isShaded() && !isFilled() && !isDashed() && !isBold();
  }

  public void setPlain(boolean plain) {
    if (plain) {
      setRoundEdge(false);
      setShadow(false);
      setShaded(false);
      setFilled(false);
      setDashed(false);
      setBold(false);
    }
  }

  public boolean isRoundEdge() {
    return roundEdge;
  }

  public void setRoundEdge(boolean roundEdge) {
    this.roundEdge = roundEdge;
  }

  public boolean isShadow() {
    return shadow;
  }

  public void setShadow(boolean shadow) {
    this.shadow = shadow;
  }

  public boolean isShaded() {
    return shaded;
  }

  public void setShaded(boolean shaded) {
    this.shaded = shaded;
  }

  public boolean isFilled() {
    return filled;
  }

  public void setFilled(boolean filled) {
    this.filled = filled;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (bold ? 1231 : 1237);
    result = prime * result + (dashed ? 1231 : 1237);
    result = prime * result + (filled ? 1231 : 1237);
    result = prime * result + (roundEdge ? 1231 : 1237);
    result = prime * result + (shaded ? 1231 : 1237);
    result = prime * result + (shadow ? 1231 : 1237);
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
    final CDRectangleType other = (CDRectangleType) obj;
    if (bold != other.bold) {
      return false;
    }
    if (dashed != other.dashed) {
      return false;
    }
    if (filled != other.filled) {
      return false;
    }
    if (roundEdge != other.roundEdge) {
      return false;
    }
    if (shaded != other.shaded) {
      return false;
    }
    if (shadow != other.shadow) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("RectangleType[");
    sb.append("bold=");
    sb.append(isBold());
    sb.append(",dashed=");
    sb.append(isDashed());
    sb.append(",filled=");
    sb.append(isFilled());
    sb.append(",round-edge=");
    sb.append(isRoundEdge());
    sb.append(",shaped=");
    sb.append(isShaded());
    sb.append(",shadow=");
    sb.append(isShadow());
    sb.append("]");
    return sb.toString();
  }

}
