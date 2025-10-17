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
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents a spot on a TLC plate.
 */
public class CDTLCSpot {

  /** The color of the spot. */
  private CDColor color;
  /** The height of the spot, in points. */
  private float height;
  /** The retention factor (Rf) of the spot. */
  private double rf;
  /** Whether the retention factor (Rf) of the spot is displayed. */
  private boolean showRf = false;
  /** The height of the spot's tail, in points. */
  private float tail;
  /** Whether the spot is visible. */
  private boolean visible = true;
  /** The width of the spot, in points. */
  private float width;

  private List<CDObjectTag> objectTags = new ArrayList<>();
  private CDSplineType curveType;

  public List<CDObjectTag> getObjectTags() {
    return objectTags;
  }

  public void setObjectTags(List<CDObjectTag> objectTags) {
    this.objectTags = objectTags;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    this.width = width;
  }

  public float getHeight() {
    return height;
  }

  public void setHeight(float height) {
    this.height = height;
  }

  public CDSplineType getCurveType() {
    return curveType;
  }

  public void setCurveType(CDSplineType curveType) {
    this.curveType = curveType;
  }

  public double getRf() {
    return rf;
  }

  public void setRf(double rf) {
    this.rf = rf;
  }

  public float getTail() {
    return tail;
  }

  public void setTail(float tail) {
    this.tail = tail;
  }

  public boolean isShowRf() {
    return showRf;
  }

  public void setShowRf(boolean showRf) {
    this.showRf = showRf;
  }

  public CDColor getColor() {
    return color;
  }

  public void setColor(CDColor color) {
    this.color = color;
  }

}
