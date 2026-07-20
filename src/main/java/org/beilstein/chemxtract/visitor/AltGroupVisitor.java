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
import org.beilstein.chemxtract.cdx.CDAltGroup;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDVisitor;

/**
 * Visitor that collects every {@link CDAltGroup} (ChemDraw {@code NamedAlternativeGroup}) reachable
 * from a page, including those nested inside groups. These are the structural R-group definitions,
 * as opposed to the free-text definitions gathered by {@link TextVisitor}.
 */
public class AltGroupVisitor extends CDVisitor {

  private final List<CDAltGroup> altGroups = new ArrayList<>();

  /**
   * Constructs an {@code AltGroupVisitor} and traverses the given page to collect alternative
   * groups.
   *
   * @param page the {@link CDPage} to traverse
   */
  public AltGroupVisitor(CDPage page) {
    page.accept(this);
  }

  @Override
  public void visitNamedAlternativeGroup(CDAltGroup namedAlternativeGroup) {
    altGroups.add(namedAlternativeGroup);
  }

  /**
   * Returns the collected alternative groups.
   *
   * @return list of {@link CDAltGroup} objects found on the page
   */
  public List<CDAltGroup> getAltGroups() {
    return altGroups;
  }
}
