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

public class BCXReactionInfo implements Serializable {

  @Serial private static final long serialVersionUID = 3345067415014408831L;

  /** The number of found reaction steps in the document, regardless if they are rubbish or not. */
  private int noReactionSteps;

  /** The actual number of extracted reactions that have passed the sanitizing rules. */
  private int noValidReactions;

  public BCXReactionInfo() {
    super();
  }

  public BCXReactionInfo(int noReactionSteps, int noValidReactions) {
    super();
    this.noReactionSteps = noReactionSteps;
    this.noValidReactions = noValidReactions;
  }

  public int getNoReactionSteps() {
    return noReactionSteps;
  }

  public void setNoReactionSteps(int noReactionSteps) {
    this.noReactionSteps = noReactionSteps;
  }

  public int getNoValidReactions() {
    return noValidReactions;
  }

  public void setNoValidReactions(int noValidReactions) {
    this.noValidReactions = noValidReactions;
  }

  @Override
  public String toString() {
    return "BCXReactionInfo [noReactionSteps="
        + noReactionSteps
        + ", noValidReactions="
        + noValidReactions
        + "]";
  }
}
