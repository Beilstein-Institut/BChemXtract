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
package org.beilstein.chemxtract.visitor;

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDVisitor;

/** Visitor class for traversing a ChemDraw page and collecting reaction steps. */
public class ReactionStepVisitor extends CDVisitor {

  private final List<CDReactionStep> reactionSteps;

  /**
   * Constructs a {@code ReactionStepVisitor} and traverses the given page to collect all reaction
   * steps.
   *
   * @param page the {@link CDPage} to traverse for reaction steps
   */
  public ReactionStepVisitor(CDPage page) {
    reactionSteps = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDReactionStep} node during traversal and adds it to the internal list of
   * reaction steps.
   *
   * @param reactionStep the {@link CDReactionStep} being visited
   */
  @Override
  public void visitReactionStep(CDReactionStep reactionStep) {
    reactionSteps.add(reactionStep);
  }

  /**
   * Returns the list of all reaction steps visited on the page.
   *
   * @return a list of {@link CDReactionStep} objects
   */
  public List<CDReactionStep> getReactionSteps() {
    return this.reactionSteps;
  }
}
