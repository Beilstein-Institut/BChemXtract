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
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;
import org.beilstein.chemxtract.cdx.datatypes.CDLabelDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;

/**
 * A block of potentially styled text. Has no chemical meaning if appearing standalone and outside
 * an atom.
 */
public class CDText extends CDObject {
  /** The rotation angle of the caption, in degrees. */
  private float angle;

  /** The justification of the caption. */
  private CDJustification justification = CDJustification.Left;

  /** The line height of the caption, in points. */
  private float lineHeight = CDSettings.LineHeight_Variable;

  /** The wrap width of the caption, in points. */
  private float wrapWidth;

  private CDPoint2D position2D;
  private CDStyledString text;
  private List<Integer> lineStarts = new ArrayList<>();
  private CDLabelDisplay labelAlignment = CDLabelDisplay.Auto;

  public CDPoint2D getPosition2D() {
    return position2D;
  }

  public void setPosition2D(CDPoint2D position2D) {
    this.position2D = position2D;
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }

  public CDStyledString getText() {
    return text;
  }

  public void setText(CDStyledString text) {
    this.text = text;
  }

  public CDJustification getJustification() {
    return justification;
  }

  public void setJustification(CDJustification justification) {
    this.justification = justification;
  }

  public float getLineHeight() {
    return lineHeight;
  }

  public void setLineHeight(float lineHeight) {
    this.lineHeight = lineHeight;
  }

  public float getWrapWidth() {
    return wrapWidth;
  }

  public void setWrapWidth(float wrapWidth) {
    this.wrapWidth = wrapWidth;
  }

  public List<Integer> getLineStarts() {
    return lineStarts;
  }

  public void setLineStarts(List<Integer> lineStarts) {
    this.lineStarts = lineStarts;
  }

  public CDLabelDisplay getLabelAlignment() {
    return labelAlignment;
  }

  public void setLabelAlignment(CDLabelDisplay labelAlignment) {
    this.labelAlignment = labelAlignment;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitText(this);
    super.accept(visitor);
  }
}
