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

import org.beilstein.chemxtract.cdx.datatypes.CDConstraintType;

import java.util.List;

/**
 * A constraint that describes an angle or distance between one or more objects.
 */
public class CDConstraint extends CDObject {
  /** The objects used to define this constraint. */
  private List<Object> basisObjects;
  /** The type of the constraint. */
  private CDConstraintType constraintType = CDConstraintType.Undefined;
  /** The maximum value of this constraint range. */
  private double maxRange;
  /** The minimum value of this constraint range. */
  private double minRange;

  private String name;
  private boolean ignoreUnconnectedAtoms = false;
  private boolean dihedralIsChiral = false;
  private boolean pointIsDirected = false;

  public boolean isPointIsDirected() {
    return pointIsDirected;
  }

  public void setPointIsDirected(boolean pointIsDirected) {
    this.pointIsDirected = pointIsDirected;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Object> getBasisObjects() {
    return basisObjects;
  }

  public void setBasisObjects(List<Object> basisObjects) {
    this.basisObjects = basisObjects;
  }

  public CDConstraintType getConstraintType() {
    return constraintType;
  }

  public void setConstraintType(CDConstraintType constraintType) {
    this.constraintType = constraintType;
  }

  public double getMinRange() {
    return minRange;
  }

  public void setMinRange(double minRange) {
    this.minRange = minRange;
  }

  public double getMaxRange() {
    return maxRange;
  }

  public void setMaxRange(double maxRange) {
    this.maxRange = maxRange;
  }

  public boolean isIgnoreUnconnectedAtoms() {
    return ignoreUnconnectedAtoms;
  }

  public void setIgnoreUnconnectedAtoms(boolean ignoreUnconnectedAtoms) {
    this.ignoreUnconnectedAtoms = ignoreUnconnectedAtoms;
  }

  public boolean isDihedralIsChiral() {
    return dihedralIsChiral;
  }

  public void setDihedralIsChiral(boolean dihedralIsChiral) {
    this.dihedralIsChiral = dihedralIsChiral;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitConstraint(this);
    super.accept(visitor);
  }

}
