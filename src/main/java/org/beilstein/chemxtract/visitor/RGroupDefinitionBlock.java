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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.beilstein.chemxtract.cdx.CDRectangle;

/**
 * A single block of R-group definitions extracted from one text node, together with the position of
 * that text on the page. The position lets callers associate a block with the scaffold it belongs
 * to when several scaffolds on the same page reuse the same R-group labels.
 *
 * <p>A label may be defined with no values: a legend line such as {@code a: R =} whose value is a
 * drawn structure rather than text. Such a block still claims the label for the scaffold it sits
 * next to.
 *
 * @param bounds the bounding box of the source text ({@code null} if the text had no position)
 * @param definitions independent per-label substituent lists (each label varies on its own)
 * @param correlatedGroups positional tables whose labels vary together as fixed row-tuples
 * @param lineHeight height of one line of the source text: its bounds' height over its number of
 *     lines ({@code 0} if the text had no position)
 * @param entryLetters the letters the text's lines open with, in order, e.g. {@code "abc"} for a
 *     legend of lines {@code a: R = ...}, {@code b: R = ...}, {@code c: R = ...}, or {@code 64a: R1
 *     = ...}, {@code 64b: R1 = ...}; empty if its lines are not lettered
 * @param compoundNumbers the compound numbers the text's lines open with, e.g. {@code 36} for a
 *     legend of lines {@code 36a: Ar = ...}, {@code 36b: Ar = ...}; empty if no line opens with one
 * @param seriesAssignments per compound number, the single values a line naming a range of that
 *     series assigns, e.g. {@code 40 -> {R1 = H}} for {@code 40a–m: R1 = H}; the series' other
 *     labels are listed by the block numbered {@code 40}
 */
public record RGroupDefinitionBlock(
    CDRectangle bounds,
    Map<String, List<String>> definitions,
    List<CorrelatedGroup> correlatedGroups,
    float lineHeight,
    String entryLetters,
    Set<String> compoundNumbers,
    Map<String, Map<String, String>> seriesAssignments) {

  /**
   * A block whose lines name no compound numbers.
   *
   * @param bounds the bounding box of the source text
   * @param definitions independent per-label substituent lists
   * @param correlatedGroups positional tables whose labels vary together
   * @param lineHeight height of one line of the source text
   * @param entryLetters the letters the text's lines open with
   */
  public RGroupDefinitionBlock(
      CDRectangle bounds,
      Map<String, List<String>> definitions,
      List<CorrelatedGroup> correlatedGroups,
      float lineHeight,
      String entryLetters) {
    this(bounds, definitions, correlatedGroups, lineHeight, entryLetters, Set.of(), Map.of());
  }

  /**
   * A block whose lines are not lettered.
   *
   * @param bounds the bounding box of the source text
   * @param definitions independent per-label substituent lists
   * @param correlatedGroups positional tables whose labels vary together
   * @param lineHeight height of one line of the source text
   */
  public RGroupDefinitionBlock(
      CDRectangle bounds,
      Map<String, List<String>> definitions,
      List<CorrelatedGroup> correlatedGroups,
      float lineHeight) {
    this(bounds, definitions, correlatedGroups, lineHeight, "");
  }
}
