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

import java.util.HashMap;
import java.util.Map;
import org.beilstein.chemxtract.cdx.datatypes.*;

/**
 * This object represents a graphical object without chemical meaning, like a line, spline, circle
 * or box.
 */
public class CDGraphic extends CDObject {
  /** The overall type of the graphic. */
  private CDGraphicType graphicType = CDGraphicType.Undefined;

  /** The line type of the graphics that are lines. */
  private CDLineType lineType = new CDLineType();

  /** The end point of the major axis of the object. */
  private CDPoint3D majorAxisEnd3D;

  /** The end point of the minor axis of the object. */
  private CDPoint3D minorAxisEnd3D;

  /** The orbital type of the graphics that are orbital. */
  private CDOrbitalType orbitalType;

  /** The oval type of the graphics that are ovals. */
  private CDOvalType ovalType = new CDOvalType();

  /** The rectangle type of the graphics that are rectangles. */
  private CDRectangleType rectangleType = new CDRectangleType();

  private Map<String, Object> represents = new HashMap<>();
  private CDObject supersededBy = null;
  private CDPoint3D head3D;
  private CDPoint3D tail3D;
  private CDPoint3D center3D;
  private CDArrowType arrowType = CDArrowType.NoHead;
  private CDBracketType bracketType;
  private CDSymbolType symbolType;
  private float arrowHeadSize;
  private float arcAngularSize;
  private float bracketLipSize;
  private CDBracketUsage bracketUsage;
  private CDPolymerRepeatPattern polymerRepeatPattern;
  private CDPolymerFlipType polymerFlipType;

  private int fadePercent = 100;

  // curve
  private CDFillType fillType;

  /** The size of the object's shadow. */
  private float shadowSize;

  /** The radius of the rounded corner of a rounded rectangle. */
  private float cornerRadius;

  public Map<String, Object> getRepresents() {
    return represents;
  }

  public void setRepresents(Map<String, Object> represents) {
    this.represents = represents;
  }

  public CDObject getSupersededBy() {
    return supersededBy;
  }

  public void setSupersededBy(CDObject supersededBy) {
    this.supersededBy = supersededBy;
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

  public CDGraphicType getGraphicType() {
    return graphicType;
  }

  public void setGraphicType(CDGraphicType graphicType) {
    this.graphicType = graphicType;
  }

  public CDLineType getLineType() {
    return lineType;
  }

  public void setLineType(CDLineType lineType) {
    this.lineType = lineType;
  }

  public CDArrowType getArrowType() {
    return arrowType;
  }

  public void setArrowType(CDArrowType arrowType) {
    this.arrowType = arrowType;
  }

  public CDOrbitalType getOrbitalType() {
    return orbitalType;
  }

  public void setOrbitalType(CDOrbitalType orbitalType) {
    this.orbitalType = orbitalType;
  }

  public CDBracketType getBracketType() {
    return bracketType;
  }

  public void setBracketType(CDBracketType bracketType) {
    this.bracketType = bracketType;
  }

  public CDSymbolType getSymbolType() {
    return symbolType;
  }

  public void setSymbolType(CDSymbolType symbolType) {
    this.symbolType = symbolType;
  }

  public float getArrowHeadSize() {
    return arrowHeadSize;
  }

  public void setArrowHeadSize(float arrowHeadSize) {
    this.arrowHeadSize = arrowHeadSize;
  }

  public float getArcAngularSize() {
    return arcAngularSize;
  }

  public void setArcAngularSize(float arcAngularSize) {
    this.arcAngularSize = arcAngularSize;
  }

  public float getBracketLipSize() {
    return bracketLipSize;
  }

  public void setBracketLipSize(float bracketLipSize) {
    this.bracketLipSize = bracketLipSize;
  }

  public CDBracketUsage getBracketUsage() {
    return bracketUsage;
  }

  public void setBracketUsage(CDBracketUsage bracketUsage) {
    this.bracketUsage = bracketUsage;
  }

  public CDPolymerRepeatPattern getPolymerRepeatPattern() {
    return polymerRepeatPattern;
  }

  public void setPolymerRepeatPattern(CDPolymerRepeatPattern polymerRepeatPattern) {
    this.polymerRepeatPattern = polymerRepeatPattern;
  }

  public CDPolymerFlipType getPolymerFlipType() {
    return polymerFlipType;
  }

  public void setPolymerFlipType(CDPolymerFlipType polymerFlipType) {
    this.polymerFlipType = polymerFlipType;
  }

  public CDOvalType getOvalType() {
    return ovalType;
  }

  public void setOvalType(CDOvalType ovalType) {
    this.ovalType = ovalType;
  }

  public CDRectangleType getRectangleType() {
    return rectangleType;
  }

  public void setRectangleType(CDRectangleType rectangleType) {
    this.rectangleType = rectangleType;
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

  public CDFillType getFillType() {
    return fillType;
  }

  public void setFillType(CDFillType fillType) {
    this.fillType = fillType;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitGraphic(this);
    super.accept(visitor);
  }

  public float getShadowSize() {
    return shadowSize;
  }

  public void setShadowSize(float shadowSize) {
    this.shadowSize = shadowSize;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public void setCornerRadius(float cornerRadius) {
    this.cornerRadius = cornerRadius;
  }

  public int getFadePercent() {
    return fadePercent;
  }

  public void setFadePercent(int fadePercent) {
    this.fadePercent = fadePercent;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Graphic(").append(getGraphicType()).append(")");
    return sb.toString();
  }
}
