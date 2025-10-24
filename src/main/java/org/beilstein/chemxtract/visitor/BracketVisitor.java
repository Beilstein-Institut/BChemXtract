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

import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for traversing a ChemDraw page and collecting multiple-atom brackets.
 */
public class BracketVisitor extends CDVisitor {

  private final List<CDBracket> multipleGroups;

  /**
   * Constructs a {@code BracketVisitor} and immediately traverses the given page
   * to collect all relevant multiple-group brackets.
   *
   * @param page the {@link CDPage} to traverse
   */
  public BracketVisitor(CDPage page) {
    multipleGroups = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDBracket} node during traversal.
   * <p>
   * If the bracket represents a multiple group (usage is {@link CDBracketUsage#MultipleGroup})
   * and the bracketed objects contain atoms ({@link CDAtom}), it is added to the internal
   * list of multiple groups.
   * </p>
   *
   * @param bracket the {@link CDBracket} node being visited
   */
  @Override
  public void visitBracketedGroup(CDBracket bracket) {
    if (CDBracketUsage.MultipleGroup.equals(bracket.getBracketUsage()) &&
            bracket.getBracketedObjects() != null &&
            !bracket.getBracketedObjects().isEmpty() &&
            bracket.getBracketedObjects().get(0) instanceof CDAtom
    ) {
      multipleGroups.add(bracket);
    }
  }

  /**
   * Returns the list of multiple-group brackets collected during traversal.
   *
   * @return list of {@link CDBracket} objects representing multiple-atom groups
   */
  public List<CDBracket> getMultipleGroups() {
    return multipleGroups;
  }
}
