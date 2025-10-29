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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.utils.Definitions;

/** Visitor class for traversing a ChemDraw page and extracting R-group definitions from text. */
public class TextVisitor extends CDVisitor {

  private final Map<String, List<String>> rgroups;

  /**
   * Constructs a {@code TextVisitor} and traverses the given page to collect R-group definitions.
   *
   * @param page the {@link CDPage} to traverse for text containing R-group definitions
   */
  public TextVisitor(CDPage page) {
    rgroups = new HashMap<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDText} node and extracts any R-group definitions it contains. The extracted
   * R-groups are added to the internal map.
   *
   * @param cdText the {@link CDText} node being visited
   */
  @Override
  public void visitText(CDText cdText) {
    if (cdText == null || cdText.getText() == null || cdText.getText().getText() == null) {
      return;
    }
    Map<String, List<String>> results = extractRGroups(cdText.getText().getText());
    for (Map.Entry<String, List<String>> entry : results.entrySet()) {
      rgroups.putIfAbsent(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Parses a string to extract R-group definitions in the form "R = ..." or "X = ...".
   *
   * <p>Supports both enumerated forms "(a) H, (b) F, ..." and simple comma-separated lists.
   *
   * @param input the string to parse for R-group definitions
   * @return a map of R-group identifiers to their possible substituents
   */
  // TODO needs to be extended for better recognition
  private Map<String, List<String>> extractRGroups(String input) {

    Map<String, List<String>> result = new LinkedHashMap<>();

    // Match something like "R = ..." or "X = ..."
    Matcher mainMatcher = Definitions.RGROUP_PATTERN.matcher(input);

    if (mainMatcher.find()) {
      String identifier = mainMatcher.group(1); // e.g., "R"
      String rhs = mainMatcher.group(2).trim(); // e.g., "(a) H, (b) F, (c) –OCH2O–" or "CH3, Cl"

      List<String> abbreviations = new ArrayList<>();

      // Case 1: enumerated (a) H, (b) F, ...
      Pattern enumerated = Pattern.compile("\\([a-zA-Z]\\)\\s*([^,]+)");
      Matcher enumMatcher = enumerated.matcher(rhs);

      while (enumMatcher.find()) {
        abbreviations.add(enumMatcher.group(1).trim());
      }

      // Case 2: plain comma-separated list if no (a)/(b)/(c) found
      if (abbreviations.isEmpty()) {
        for (String part : rhs.split("\\s*,\\s*")) {
          if (!part.isEmpty()) abbreviations.add(part.trim());
        }
      }

      result.put(identifier, abbreviations);
    }

    return result;
  }

  /**
   * Returns the map of extracted R-groups.
   *
   * @return a map where keys are R-group labels and values are lists of substituents
   */
  public Map<String, List<String>> getRgroups() {
    return rgroups;
  }
}
