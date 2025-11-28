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

import java.io.IOException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.lookups.UnwantedAbbreviations;

/** Visitor class for traversing a ChemDraw fragment and collecting atom-related information. */
public class AtomVisitor extends CDVisitor {

  /** A list containing all {@link CDAtom} instances collected during traversal of the fragment. */
  private final List<CDAtom> atoms;

  /** A map associating nickname strings with their corresponding {@link CDFragment} definitions. */
  private final Map<String, CDFragment> nicknames; // map for nicknames

  /** A set containing all abbreviation strings encountered during fragment traversal. */
  private final Set<String> abbreviations;

  /** A set of {@link CDAtom} instances that should be skipped during processing. */
  private final Set<CDAtom> skip;

  /** Logger instance for this class, used for diagnostic and error reporting. */
  private static final Log logger = LogFactory.getLog(AtomVisitor.class);

  /**
   * Flag indicating whether to process atoms in raw unfiltered, untreated mode. When {@code true},
   * atoms are processed without additional transformations or interpretations.
   */
  private boolean rawMode;

  /**
   * Constructs an {@code AtomVisitor} and immediately traverses the provided fragment to collect
   * atoms, nicknames, and abbreviations. Uses default processing mode (raw = false).
   *
   * @param fragment the {@link CDFragment} to traverse
   */
  public AtomVisitor(CDFragment fragment) {
    this(fragment, false);
  }

  /**
   * Constructs an {@code AtomVisitor} with configurable raw processing mode and immediately
   * traverses the provided fragment to collect atoms, nicknames, and abbreviations.
   *
   * @param fragment the {@link CDFragment} to traverse
   * @param rawMode {@code true} to enable raw processing unfiltered, untreated mode, {@code false}
   *     for standard processing
   */
  public AtomVisitor(CDFragment fragment, boolean rawMode) {
    atoms = new ArrayList<>();
    nicknames = new HashMap<>();
    abbreviations = new HashSet<>();
    skip = new HashSet<>();
    this.rawMode = rawMode;
    fragment.accept(this);
  }

  /**
   * Visits a {@link CDAtom} node during the fragment traversal.
   *
   * @param node the {@link CDAtom} node being visited
   */
  @Override
  public void visitAtom(CDAtom node) {
    if (rawMode) {
      atoms.add(node);
    } else {
      // Extract primary node text, maybe null
      String nickname =
          Optional.ofNullable(node.getText())
              .map(CDText::getText)
              .map(CDStyledString::getText)
              .orElseGet(node::getLabelText);

      // Collect internal fragment and its nickname
      if (!node.getFragments().isEmpty()) {
        CDFragment frag = node.getFragments().get(0);
        if (!isUnwantedAbbreviation(nickname)) {
          nicknames.putIfAbsent(nickname, frag);
        } else {
          skip.addAll(frag.getAtoms());
          abbreviations.add(nickname);
          atoms.add(node);
        }
      }
      // collect abbreviations that could not be handled by ChemDraw
      else if ((!CDNodeType.Element.equals(node.getNodeType()) && nickname != null)
          || isUnwantedAbbreviation(nickname)) {
        abbreviations.add(nickname);
        atoms.add(node);
      }
      // Collect atoms
      if (CDNodeType.Element.equals(node.getNodeType()) && !skip.contains(node)) {
        atoms.add(node);
      }
    }
  }

  /**
   * Checks if the given nickname is present in the unwanted abbreviations list.
   *
   * @param nickname the abbreviation nickname to check (may be {@code null})
   * @return {@code true} if nickname is found in unwanted abbreviations list, {@code false}
   *     otherwise (including when I/O errors occur)
   * @see UnwantedAbbreviations#contains(String)
   */
  private boolean isUnwantedAbbreviation(String nickname) {
    try {
      return UnwantedAbbreviations.contains(nickname);
    } catch (IOException e) {
      logger.error("Unable to load unwanted abbreviations: " + e.getMessage());
    }
    return false;
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
