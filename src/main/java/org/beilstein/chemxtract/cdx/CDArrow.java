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

import org.beilstein.chemxtract.cdx.datatypes.*;

/** A line or an arrow with or without arrowheads on one or both ends. */
public class CDArrow extends CDObject {
  /** The line type of the arrow. */
  private CDLineType lineType = new CDLineType();

  /** The size of the arrowhead, as a multiple of the line width, for arrows with arrowheads. */
  private float headSize;

  /**
   * The half-width of the arrowhead, as a multiple of the line width, for arrows with arrowheads.
   */
  private float headWidth;

  /**
   * The size of the center of the arrowhead, as a multiple of the line width, for arrows with
   * arrowheads.
   */
  private float headCenterSize;

  /** The type of the arrowhead, for arrows with arrowheads. */
  private CDArrowHeadType arrowHeadType = CDArrowHeadType.Solid;

  /** The position of the arrowhead at the start of the arrow. */
  private CDArrowHeadPositionType arrowHeadPositionStart = CDArrowHeadPositionType.Unspecified;

  /** The position of the arrowhead at the tail of the arrow. */
  private CDArrowHeadPositionType arrowHeadPositionTail = CDArrowHeadPositionType.Unspecified;

  /**
   * The width of the space between a multiple-component arrow shaft, as in an equilibrium arrow.
   */
  private float shaftSpacing;

  /**
   * The ratio of the length of the left component of an equilibrium arrow (viewed from the end to
   * the start) to the right component.
   */
  private float equilibriumRatio;

  /** The angular size of the arrow, for arrows that are arcs. */
  private float angularSize;

  /** The type of the no-go (crossed-through or hashed-out) arrow. */
  private CDNoGoType noGoType;

  // TODO
  private boolean dipole = false;

  private CDPoint3D head3D;
  private CDPoint3D tail3D;
  private CDPoint3D center3D;
  private CDPoint3D majorAxisEnd3D;
  private CDPoint3D minorAxisEnd3D;

  /** The fill type of the spline. */
  private CDFillType fillType = CDFillType.Unspecified;

  public CDLineType getLineType() {
    return lineType;
  }

  public void setLineType(CDLineType lineType) {
    this.lineType = lineType;
  }

  public float getHeadSize() {
    return headSize;
  }

  public void setHeadSize(float arrowHeadSize) {
    headSize = arrowHeadSize;
  }

  public float getHeadWidth() {
    return headWidth;
  }

  public void setHeadWidth(float arrowheadWidth) {
    headWidth = arrowheadWidth;
  }

  public float getHeadCenterSize() {
    return headCenterSize;
  }

  public void setHeadCenterSize(float arrowheadCenterSize) {
    headCenterSize = arrowheadCenterSize;
  }

  public CDArrowHeadType getArrowHeadType() {
    return arrowHeadType;
  }

  public void setArrowHeadType(CDArrowHeadType arrowheadType) {
    arrowHeadType = arrowheadType;
  }

  public CDArrowHeadPositionType getArrowHeadPositionStart() {
    return arrowHeadPositionStart;
  }

  public void setArrowHeadPositionStart(CDArrowHeadPositionType arrowheadHead) {
    arrowHeadPositionStart = arrowheadHead;
  }

  public CDArrowHeadPositionType getArrowHeadPositionTail() {
    return arrowHeadPositionTail;
  }

  public void setArrowHeadPositionTail(CDArrowHeadPositionType arrowheadTail) {
    arrowHeadPositionTail = arrowheadTail;
  }

  public float getShaftSpacing() {
    return shaftSpacing;
  }

  public void setShaftSpacing(float arrowShaftSpacing) {
    shaftSpacing = arrowShaftSpacing;
  }

  public float getEquilibriumRatio() {
    return equilibriumRatio;
  }

  public void setEquilibriumRatio(float arrowEquilibriumRatio) {
    equilibriumRatio = arrowEquilibriumRatio;
  }

  public float getAngularSize() {
    return angularSize;
  }

  public void setAngularSize(float arcAngularSize) {
    angularSize = arcAngularSize;
  }

  public boolean isDipole() {
    return dipole;
  }

  public void setDipole(boolean dipole) {
    this.dipole = dipole;
  }

  public CDPoint3D getHead3D() {
    return head3D;
  }

  public void setHead3D(CDPoint3D head3D) {
    this.head3D = head3D;
  }

  public CDPoint3D getTail3D() {
    return tail3D;
  }

  public void setTail3D(CDPoint3D tail3D) {
    this.tail3D = tail3D;
  }

  public CDPoint3D getCenter3D() {
    return center3D;
  }

  public void setCenter3D(CDPoint3D center3D) {
    this.center3D = center3D;
  }

  public CDPoint3D getMajorAxisEnd3D() {
    return majorAxisEnd3D;
  }

  public void setMajorAxisEnd3D(CDPoint3D majorAxisEnd3D) {
    this.majorAxisEnd3D = majorAxisEnd3D;
  }

  public CDPoint3D getMinorAxisEnd3D() {
    return minorAxisEnd3D;
  }

  public void setMinorAxisEnd3D(CDPoint3D minorAxisEnd3D) {
    this.minorAxisEnd3D = minorAxisEnd3D;
  }

  public CDNoGoType getNoGoType() {
    return noGoType;
  }

  public void setNoGoType(CDNoGoType noGoType) {
    this.noGoType = noGoType;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitArrow(this);
    super.accept(visitor);
  }

  public CDFillType getFillType() {
    return fillType;
  }

  public void setFillType(CDFillType fillType) {
    this.fillType = fillType;
  }
}
