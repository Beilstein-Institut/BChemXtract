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
package org.beilstein.chemxtract.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * A minimal statistics object about substance extraction. May exist in the scope of one CDX scheme.
 */
public class BCXSubstanceInfo implements Serializable {

  @Serial private static final long serialVersionUID = 3821837888603010882L;

  /** Number of fragments in CDX */
  private int noFragments;

  /** Number of extractable fragments with Inchi */
  private int noInchis;

  /** Number of distinct Inchis, i.e. substances */
  private int noSubstances;

  public BCXSubstanceInfo() {
    super();
  }

  public BCXSubstanceInfo(int noFragments, int noInchis, int noSubstances) {
    super();
    this.noFragments = noFragments;
    this.noInchis = noInchis;
    this.noSubstances = noSubstances;
  }

  public int getNoFragments() {
    return noFragments;
  }

  public void setNoFragments(int noFragments) {
    this.noFragments = noFragments;
  }

  public int getNoInchis() {
    return noInchis;
  }

  public void setNoInchis(int noInchis) {
    this.noInchis = noInchis;
  }

  public int getNoSubstances() {
    return noSubstances;
  }

  public void setNoSubstances(int noSubstances) {
    this.noSubstances = noSubstances;
  }

  @Override
  public String toString() {
    return "BCXSubstanceInfo [noFragments="
        + noFragments
        + ", noInchis="
        + noInchis
        + ", noSubstances="
        + noSubstances
        + "]";
  }
}
