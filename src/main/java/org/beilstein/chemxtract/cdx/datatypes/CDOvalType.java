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
 * The type of a graphic object that represents a circle or ellipse.
 */
public class CDOvalType {
  private boolean circle = false;
  private boolean shaded = false;
  private boolean filled = false;
  private boolean dashed = false;
  private boolean bold = false;
  private boolean shadowed = false;

  public boolean isPlain() {
    return !isCircle() && !isShaded() && !isFilled() && !isDashed() && !isBold() && !isShadowed();
  }

  public void setPlain(boolean plain) {
    if (plain) {
      setCircle(false);
      setShaded(false);
      setFilled(false);
      setDashed(false);
      setBold(false);
      setShadowed(false);
    }
  }

  public boolean isCircle() {
    return circle;
  }

  public void setCircle(boolean circle) {
    this.circle = circle;
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

  public boolean isShadowed() {
    return shadowed;
  }

  public void setShadowed(boolean shadowed) {
    this.shadowed = shadowed;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (bold ? 1231 : 1237);
    result = prime * result + (circle ? 1231 : 1237);
    result = prime * result + (dashed ? 1231 : 1237);
    result = prime * result + (filled ? 1231 : 1237);
    result = prime * result + (shaded ? 1231 : 1237);
    result = prime * result + (shadowed ? 1231 : 1237);
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
    final CDOvalType other = (CDOvalType) obj;
    if (bold != other.bold) {
      return false;
    }
    if (circle != other.circle) {
      return false;
    }
    if (dashed != other.dashed) {
      return false;
    }
    if (filled != other.filled) {
      return false;
    }
    if (shaded != other.shaded) {
      return false;
    }
    if (shadowed != other.shadowed) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("OvalType[");
    sb.append("bold=");
    sb.append(isBold());
    sb.append(",circle=");
    sb.append(isCircle());
    sb.append(",dashed=");
    sb.append(isDashed());
    sb.append(",filled=");
    sb.append(isFilled());
    sb.append(",shaped=");
    sb.append(isShaded());
    sb.append(",shadowed=");
    sb.append(isShadowed());
    sb.append("]");
    return sb.toString();
  }

}
