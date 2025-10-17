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

import org.beilstein.chemxtract.cdx.datatypes.CDObjectTagType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDPositioningType;

import java.util.ArrayList;
import java.util.List;

/**
 * A named property that may be assigned to any ChemDraw object.
 */
public class CDObjectTag extends CDObject {
  /** The type of the object tag. */
  private CDObjectTagType objectTagType;
  /** Whether the tag will be preserved when the document is saved. */
  private boolean persistent = true;
  /**
   * The angle (in radians) at which this tag is positioned (if positioned by angle).
   */
  private float positioningAngle;
  /**
   * The offset by which this tag is positioned (if positioned by offset or absolute).
   */
  private CDPoint2D positioningOffset;
  /** A description of how the tag is positioned relative to its owner. */
  private CDPositioningType positioningType = CDPositioningType.Auto;
  /**
   * Whether placing the mouse over the object tag will cause other objects with matching tags also
   * to become highlighted.
   */
  private boolean tracking = true;

  private List<CDText> texts = new ArrayList<>();
  private String name;
  private Object value;

  public List<CDText> getTexts() {
    return texts;
  }

  public void setTexts(List<CDText> texts) {
    this.texts = texts;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CDObjectTagType getObjectTagType() {
    return objectTagType;
  }

  public void setObjectTagType(CDObjectTagType objectTagType) {
    this.objectTagType = objectTagType;
  }

  public boolean isTracking() {
    return tracking;
  }

  public void setTracking(boolean tracking) {
    this.tracking = tracking;
  }

  public boolean isPersistent() {
    return persistent;
  }

  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public CDPositioningType getPositioningType() {
    return positioningType;
  }

  public void setPositioningType(CDPositioningType positioningType) {
    this.positioningType = positioningType;
  }

  public float getPositioningAngle() {
    return positioningAngle;
  }

  public void setPositioningAngle(float positioningAngle) {
    this.positioningAngle = positioningAngle;
  }

  public CDPoint2D getPositioningOffset() {
    return positioningOffset;
  }

  public void setPositioningOffset(CDPoint2D positioningOffset) {
    this.positioningOffset = positioningOffset;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitObjectTag(this);
    for (CDText text : texts) {
      text.accept(visitor);
    }
  }

}
