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

import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents a Thin Layer Chromatography plate and consists of severyal TLC lanes.
 */
public class CDTLCPlate extends CDObject {
  /** The bottom-left corner of TLC plate (possibly in a rotated frame). */
  private CDPoint2D bottomLeft;
  /** The bottom-right corner of TLC plate (possibly in a rotated frame). */
  private CDPoint2D bottomRight;
  /** The lanes contained within this TLC plate. */
  private List<CDTLCLane> lanes = new ArrayList<>();
  /**
   * the positioning of the origin, as a decimal fraction of the distance from the bottom to the top
   * of the TLC plate.
   */
  private double originFraction;
  /** Whether the borders of the TLC Plate will be drawn. */
  private boolean showBorders = false;
  /** Whether the origin will be drawn. */
  private boolean showOrigin = false;
  /** Whether tick marks will be drawn at the sides of the TLC plate. */
  private boolean showSideTicks = false;
  /** Whether the solvent front will be drawn. */
  private boolean showSolventFront = false;
  /**
   * The positioning of the solvent front, as a decimal fraction of the distance from the bottom to
   * the top of the TLC plate.
   */
  private double solventFrontFraction;
  /** The top-left corner of the TLC plate (possibly in a rotated frame). */
  private CDPoint2D topLeft;
  /** The top-right corner of TLC plate (possibly in a rotated frame). */
  private CDPoint2D topRight;
  /** Whether the background of the TLC Plate will be omitted. */
  private boolean transparent = false;

  public boolean isTransparent() {
    return transparent;
  }

  public void setTransparent(boolean transparent) {
    this.transparent = transparent;
  }

  public boolean isShowSideTicks() {
    return showSideTicks;
  }

  public void setShowSideTicks(boolean showSideTicks) {
    this.showSideTicks = showSideTicks;
  }

  public List<CDTLCLane> getLanes() {
    return lanes;
  }

  public void setLanes(List<CDTLCLane> lanes) {
    this.lanes = lanes;
  }

  public CDPoint2D getTopLeft() {
    return topLeft;
  }

  public void setTopLeft(CDPoint2D topLeft) {
    this.topLeft = topLeft;
  }

  public CDPoint2D getTopRight() {
    return topRight;
  }

  public void setTopRight(CDPoint2D topRight) {
    this.topRight = topRight;
  }

  public CDPoint2D getBottomRight() {
    return bottomRight;
  }

  public void setBottomRight(CDPoint2D bottomRight) {
    this.bottomRight = bottomRight;
  }

  public CDPoint2D getBottomLeft() {
    return bottomLeft;
  }

  public void setBottomLeft(CDPoint2D bottomLeft) {
    this.bottomLeft = bottomLeft;
  }

  public double getOriginFraction() {
    return originFraction;
  }

  public void setOriginFraction(double originFraction) {
    this.originFraction = originFraction;
  }

  public double getSolventFrontFraction() {
    return solventFrontFraction;
  }

  public void setSolventFrontFraction(double solventFrontFraction) {
    this.solventFrontFraction = solventFrontFraction;
  }

  public boolean isShowOrigin() {
    return showOrigin;
  }

  public void setShowOrigin(boolean showOrigin) {
    this.showOrigin = showOrigin;
  }

  public boolean isShowSolventFront() {
    return showSolventFront;
  }

  public void setShowSolventFront(boolean showSolventFront) {
    this.showSolventFront = showSolventFront;
  }

  public boolean isShowBorders() {
    return showBorders;
  }

  public void setShowBorders(boolean showBorders) {
    this.showBorders = showBorders;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitTLCPlate(this);
    super.accept(visitor);
  }
}
