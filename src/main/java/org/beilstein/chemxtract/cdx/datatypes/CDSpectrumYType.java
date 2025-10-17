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
package org.beilstein.chemxtract.cdx.datatypes;

/**
 * The type of units the Y-axis represents.
 */
public enum CDSpectrumYType{
  /** The axis type is unknown. Not recommended. */
  Unknown,
  /**
   * Axis is in absorbance units, and consequently has a baseline of 0.0 with
   * peaks pointing up. Only for IR spectra.
   */
  Absorbance,
  /**
   * Axis is in transmittance units. The baseline is at 1.0 and peaks points
   * down to a value of 0.0 being no transmission. Only for IR spectra.
   */
  Transmittance,
  /**
   * Axis is in transmittance units*100. The baseline is at 100% and peaks
   * points down to a value of 0.0 being no transmission. Only for IR spectra.
   */
  PercentTransmittance,
  /** Axis is some other type. */
  Other,
  /**
   * Axis is unscaled -- essentially, the absolute values have no meaning and
   * only relative values matter.
   */
  ArbitraryUnits;
}
