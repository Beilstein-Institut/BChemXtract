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

import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;

import java.util.*;

/**
 * Visitor class for traversing a ChemDraw fragment and collecting atom-related information.
 */
public class AtomVisitor extends CDVisitor {

  private final List<CDAtom> atoms;
  private final Map<String, CDFragment> nicknames; // map for nicknames
  private final Set<String> abbreviations;

  /**
   * Constructs an {@code AtomVisitor} and immediately traverses the provided fragment
   * to collect atoms, nicknames, and abbreviations.
   *
   * @param fragment the {@link CDFragment} to traverse
   */
  public AtomVisitor(CDFragment fragment) {
    atoms = new ArrayList<>();
    nicknames = new HashMap<>();
    abbreviations = new HashSet<>();
    fragment.accept(this);
  }

  /**
   * Visits a {@link CDAtom} node during the fragment traversal.
   *
   * @param node the {@link CDAtom} node being visited
   */
  @Override
  public void visitAtom(CDAtom node) {
    // Collect internal fragment and its nickname
    if (!node.getFragments().isEmpty()) {
      String nickname = node.getText().getText().getText();
      CDFragment frag = node.getFragments().get(0);
      nicknames.putIfAbsent(nickname, frag);
    }
    // collect abbreviations that could not be handled by ChemDraw
    else if (!CDNodeType.Element.equals(node.getNodeType())
            && (node.getText() != null || node.getLabelText() != null)
    ) {
      String text = Optional.ofNullable(node.getText())
              .map(CDText::getText)
              .map(CDStyledString::getText)
              .orElseGet(node::getLabelText
              );
      abbreviations.add(text);
      atoms.add(node);
    }
    // Collect atoms
    if (CDNodeType.Element.equals(node.getNodeType())
    ) {
      atoms.add(node);
    }
  }

  /**
   * Returns the list of {@link CDAtom} objects collected during the visit.
   *
   * @return list of collected atoms
   */
  public List<CDAtom> getAtoms() {
    return atoms;
  }

  /**
   * Returns a mapping of fragment nicknames to their corresponding {@link CDFragment} objects.
   *
   * @return map of nickname strings to internal fragments
   */
  public Map<String, CDFragment> getNicknames() {
    return nicknames;
  }

  /**
   * Returns a set of abbreviation strings that were collected during the fragment traversal.
   *
   * @return set of abbreviation strings
   */
  public Set<String> getAbbreviations() {
    return abbreviations;
  }
}
