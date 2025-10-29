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

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;

/** Abstract class for all drawable ChemDraw objects. */
public abstract class CDObject {
  /** The object's object tags. */
  private List<CDObjectTag> objectTags = new ArrayList<>();

  /** The relative back-to-front ordering of the object. */
  private int zOrder;

  /** Whether chemical warnings are ignored for this object. */
  private boolean ignoreWarnings = false;

  /** The chemical warning string for this object. */
  private String chemicalWarning;

  /** Whether the ChemDraw object is visible. */
  private boolean visible = true;

  /** The rectangle bounding the object. */
  private CDRectangle bounds;

  /** The color of the ChemDraw object. */
  private CDColor color;

  /** The settings object. */
  private final CDSettings settings = new CDSettings();

  public List<CDObjectTag> getObjectTags() {
    return objectTags;
  }

  public void setObjectTags(List<CDObjectTag> objectTags) {
    this.objectTags = objectTags;
  }

  public int getZOrder() {
    return zOrder;
  }

  public void setZOrder(int order) {
    zOrder = order;
  }

  public boolean isIgnoreWarnings() {
    return ignoreWarnings;
  }

  public void setIgnoreWarnings(boolean ignoreWarnings) {
    this.ignoreWarnings = ignoreWarnings;
  }

  public String getChemicalWarning() {
    return chemicalWarning;
  }

  public void setChemicalWarning(String chemicalWarning) {
    this.chemicalWarning = chemicalWarning;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public CDRectangle getBounds() {
    return bounds;
  }

  public void setBounds(CDRectangle bounds) {
    this.bounds = bounds;
  }

  public CDColor getColor() {
    return color;
  }

  public void setColor(CDColor color) {
    this.color = color;
  }

  public CDSettings getSettings() {
    return settings;
  }

  public void accept(CDVisitor visitor) {
    for (CDObjectTag objectTag : objectTags) {
      objectTag.accept(visitor);
    }
  }
}
