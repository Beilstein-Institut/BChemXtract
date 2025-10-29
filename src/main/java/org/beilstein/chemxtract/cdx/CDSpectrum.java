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

import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumClass;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumXType;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumYType;

/** This object stores an NMR, MS, IR or other types of spectrum. */
public class CDSpectrum extends CDObject {
  private double xSpacing;
  private double xLow;
  private CDSpectrumXType xType = CDSpectrumXType.Unknown;
  private CDSpectrumYType yType = CDSpectrumYType.Unknown;
  private String xAxisLabel;
  private String yAxisLabel;
  private double[] dataPoint;
  private CDSpectrumClass spectrumClass = CDSpectrumClass.Unknown;
  private double yLow = 1;
  private double yScale = 1;

  public double getXSpacing() {
    return xSpacing;
  }

  public void setXSpacing(double spacing) {
    xSpacing = spacing;
  }

  public double getXLow() {
    return xLow;
  }

  public void setXLow(double low) {
    xLow = low;
  }

  public CDSpectrumXType getXType() {
    return xType;
  }

  public void setXType(CDSpectrumXType type) {
    xType = type;
  }

  public CDSpectrumYType getYType() {
    return yType;
  }

  public void setYType(CDSpectrumYType type) {
    yType = type;
  }

  public String getXAxisLabel() {
    return xAxisLabel;
  }

  public void setXAxisLabel(String axisLabel) {
    xAxisLabel = axisLabel;
  }

  public String getYAxisLabel() {
    return yAxisLabel;
  }

  public void setYAxisLabel(String axisLabel) {
    yAxisLabel = axisLabel;
  }

  public double[] getDataPoint() {
    return dataPoint;
  }

  public void setDataPoint(double[] dataPoint) {
    this.dataPoint = dataPoint;
  }

  public CDSpectrumClass getSpectrumClass() {
    return spectrumClass;
  }

  public void setSpectrumClass(CDSpectrumClass spectrumClass) {
    this.spectrumClass = spectrumClass;
  }

  public double getYLow() {
    return yLow;
  }

  public void setYLow(double low) {
    yLow = low;
  }

  public double getYScale() {
    return yScale;
  }

  public void setYScale(double scale) {
    yScale = scale;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitSpectrum(this);
    super.accept(visitor);
  }
}
