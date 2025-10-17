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

import org.beilstein.chemxtract.cdx.datatypes.CDGeometryType;

import java.util.List;

/**
 * A geometry relationship between several objects.
 */
public class CDGeometry extends CDObject {
  /** The objects used to define this geometry. */
  private List<Object> basisObjects;
  /** The type of the geometry. */
  private CDGeometryType geometricType = CDGeometryType.Undefined;
  /** The the value (if any) associated with this geometry. */
  private double relationValue;

  private String name;
  private boolean pointIsDirected;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CDGeometryType getGeometricType() {
    return geometricType;
  }

  public void setGeometricType(CDGeometryType geometricType) {
    this.geometricType = geometricType;
  }

  public double getRelationValue() {
    return relationValue;
  }

  public void setRelationValue(double relationValue) {
    this.relationValue = relationValue;
  }

  public List<Object> getBasisObjects() {
    return basisObjects;
  }

  public void setBasisObjects(List<Object> basisObjects) {
    this.basisObjects = basisObjects;
  }

  public boolean isPointIsDirected() {
    return pointIsDirected;
  }

  public void setPointIsDirected(boolean pointIsDirected) {
    this.pointIsDirected = pointIsDirected;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitGeometry(this);
    super.accept(visitor);
  }

}
