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

import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for traversing a ChemDraw page and collecting fragments.
 */
public class FragmentVisitor extends CDVisitor {

  private final List<CDFragment> allFragments;
  private final List<CDFragment> fragments;

  /**
   * Constructs a {@code FragmentVisitor} and traverses the given page to collect
   * fragments.
   *
   * @param page the {@link CDPage} to traverse for fragments
   */
  public FragmentVisitor(CDPage page) {
    fragments = new ArrayList<>();
    allFragments = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDFragment} node during traversal.
   * <p>
   * Fragments without external connection points are added to both
   * {@link #fragments} and {@link #allFragments}, while fragments with external
   * connection points are only added to {@link #allFragments}.
   * </p>
   *
   * @param fragment the {@link CDFragment} being visited
   */
  @Override
  public void visitFragment(CDFragment fragment){
    if (fragment.getAtoms().stream().noneMatch(cdAtom -> CDNodeType.ExternalConnectionPoint.equals(cdAtom.getNodeType()))) {
      fragments.add(fragment);
    }
    allFragments.add(fragment);
  }

  /**
   * Returns the list of fragments that do not contain external connection points.
   *
   * @return list of complete {@link CDFragment} objects
   */
  public List<CDFragment> getFragments() {
    return fragments;
  }

  /**
   * Returns the list of all fragments visited, including those with external
   * connection points.
   *
   * @return list of all {@link CDFragment} objects visited
   */
  public List<CDFragment> getAllFragments() {
    return allFragments;
  }
}
