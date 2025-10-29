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

/** ChemDraw rectangle. */
public class CDRectangle {
  /** The top edge of the rectangle. */
  private float top;

  /** The left edge of the rectangle. */
  private float left;

  /** The bottom edge of the rectangle. */
  private float bottom;

  /** The right edge of the rectangle. */
  private float right;

  public float getTop() {
    return top;
  }

  public void setTop(float top) {
    this.top = top;
  }

  public float getLeft() {
    return left;
  }

  public void setLeft(float left) {
    this.left = left;
  }

  public float getBottom() {
    return bottom;
  }

  public void setBottom(float bottom) {
    this.bottom = bottom;
  }

  public float getRight() {
    return right;
  }

  public void setRight(float right) {
    this.right = right;
  }

  @Override
  public String toString() {
    return "Rectangle[top=" + top + " left=" + left + " bottom=" + bottom + " right=" + right + "]";
  }

  public float getMinX() {
    return Math.min(getLeft(), getRight());
  }

  public float getMaxX() {
    return Math.max(getLeft(), getRight());
  }

  public float getMinY() {
    return Math.min(getTop(), getBottom());
  }

  public float getMaxY() {
    return Math.max(getTop(), getBottom());
  }

  public float getWidth() {
    return Math.abs(getRight() - getLeft());
  }

  public float getHeight() {
    return Math.abs(getTop() - getBottom());
  }

  public float getCenterX() {
    return (getLeft() + getRight()) / 2f;
  }

  public float getCenterY() {
    return (getTop() + getBottom()) / 2f;
  }
}
