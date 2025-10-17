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

import java.util.List;

/**
 * This object represents a Bezier curve.
 */
public class CDSpline extends CDObject {
  /** The position of the arrowhead at the end of the spline. */
  private CDArrowHeadPositionType arrowHeadPositionAtEnd = CDArrowHeadPositionType.Unspecified;
  /** The position of the arrowhead at the start of the spline. */
  private CDArrowHeadPositionType arrowHeadPositionAtStart = CDArrowHeadPositionType.Unspecified;
  /** The type of the arrowhead, for splines with arrowheads. */
  private CDArrowHeadType arrowHeadType = CDArrowHeadType.Solid;
  /** The fill type of the spline. */
  private CDFillType fillType = CDFillType.Unspecified;
  /** The line type of the spline. */
  private CDLineType lineType = new CDLineType();
  /** The points used to describe the spline. */
  private List<CDPoint2D> points2D;
  private List<CDPoint3D> points3D;

  private boolean closed = false;

  public CDFillType getFillType() {
    return fillType;
  }

  public void setFillType(CDFillType fillType) {
    this.fillType = fillType;
  }

  public CDLineType getLineType() {
    return lineType;
  }

  public void setLineType(CDLineType lineType) {
    this.lineType = lineType;
  }

  public List<CDPoint2D> getPoints2D() {
    return points2D;
  }

  public void setPoints2D(List<CDPoint2D> points2D) {
    this.points2D = points2D;
  }

  public List<CDPoint3D> getPoints3D() {
    return points3D;
  }

  public void setPoints3D(List<CDPoint3D> points3D) {
    this.points3D = points3D;
  }

  public CDArrowHeadType getArrowHeadType() {
    return arrowHeadType;
  }

  public void setArrowHeadType(CDArrowHeadType arrowHeadType) {
    this.arrowHeadType = arrowHeadType;
  }

  public CDArrowHeadPositionType getArrowHeadPositionAtStart() {
    return arrowHeadPositionAtStart;
  }

  public void setArrowHeadPositionAtStart(CDArrowHeadPositionType arrowHeadPositionAtStart) {
    this.arrowHeadPositionAtStart = arrowHeadPositionAtStart;
  }

  public CDArrowHeadPositionType getArrowHeadPositionAtEnd() {
    return arrowHeadPositionAtEnd;
  }

  public void setArrowHeadPositionAtEnd(CDArrowHeadPositionType arrowHeadPositionAtEnd) {
    this.arrowHeadPositionAtEnd = arrowHeadPositionAtEnd;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitCurve(this);
    super.accept(visitor);
  }

}
