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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.utils.Definitions;

/** Visitor class for traversing a ChemDraw page and extracting R-group definitions from text. */
public class TextVisitor extends CDVisitor {

  private final Map<String, List<String>> rgroups;
  private final List<RGroupDefinitionBlock> blocks;

  /**
   * Constructs a {@code TextVisitor} and traverses the given page to collect R-group definitions.
   *
   * @param page the {@link CDPage} to traverse for text containing R-group definitions
   */
  public TextVisitor(CDPage page) {
    rgroups = new LinkedHashMap<>();
    blocks = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDText} node and extracts any R-group definitions it contains. Each text node
   * that yields definitions becomes its own {@link RGroupDefinitionBlock} (tagged with the text's
   * position) so callers can scope definitions to the scaffold they belong to.
   *
   * @param cdText the {@link CDText} node being visited
   */
  @Override
  public void visitText(CDText cdText) {
    if (cdText == null || cdText.getText() == null || cdText.getText().getText() == null) {
      return;
    }
    ParsedDefinitions parsed = extractDefinitions(cdText.getText().getText());
    if (parsed.independent().isEmpty() && parsed.correlatedGroups().isEmpty()) {
      return;
    }
    blocks.add(
        new RGroupDefinitionBlock(
            cdText.getBounds(), parsed.independent(), parsed.correlatedGroups()));
    // Keep a page-wide union as a fallback; merge rather than drop on label collisions. Correlated
    // values are flattened into the union too so the fallback/guard still see them (the union path
    // over-enumerates by design; the scoped path preserves the tuples).
    for (Map.Entry<String, List<String>> entry : parsed.independent().entrySet()) {
      mergeSubstituents(rgroups, entry.getKey(), entry.getValue());
    }
    for (CorrelatedGroup group : parsed.correlatedGroups()) {
      for (Map<String, String> tuple : group.tuples()) {
        tuple.forEach((label, value) -> mergeSubstituents(rgroups, label, List.of(value)));
      }
    }
  }

  /** Independent per-label lists plus correlated (positional-table) groups from one text node. */
  private record ParsedDefinitions(
      Map<String, List<String>> independent, List<CorrelatedGroup> correlatedGroups) {}

  /**
   * Adds substituents to the list for a label, creating the list if needed and skipping duplicates.
   *
   * @param target the map to merge into
   * @param label the R-group label
   * @param substituents the substituents to add
   */
  private static void mergeSubstituents(
      Map<String, List<String>> target, String label, List<String> substituents) {
    List<String> existing = target.computeIfAbsent(label, key -> new ArrayList<>());
    for (String substituent : substituents) {
      if (!existing.contains(substituent)) {
        existing.add(substituent);
      }
    }
  }

  /**
   * Parses one text node into independent per-label definitions and correlated (positional-table)
   * groups. Each line (rows are separated by carriage returns / newlines) is parsed on its own: a
   * line that assigns two or more labels each a single value is a correlated tuple (a table row);
   * anything else contributes to the independent per-label lists.
   *
   * @param input the raw text of a node
   * @return the parsed definitions
   */
  // TODO needs to be extended for better recognition
  private ParsedDefinitions extractDefinitions(String input) {
    Map<String, List<String>> independent = new LinkedHashMap<>();
    // Correlated rows keyed by their (sorted) label set, so rows of the same table accumulate.
    Map<List<String>, List<Map<String, String>>> tables = new LinkedHashMap<>();

    for (String row : toLogicalRows(input)) {
      Map<String, List<String>> assignments = parseRow(row);
      if (assignments.isEmpty()) {
        continue;
      }
      boolean tupleRow =
          assignments.size() >= 2
              && assignments.values().stream().allMatch(values -> values.size() == 1);
      if (tupleRow) {
        List<String> key = new ArrayList<>(assignments.keySet());
        key.sort(null);
        Map<String, String> tuple = new LinkedHashMap<>();
        assignments.forEach((label, values) -> tuple.put(label, values.get(0)));
        tables.computeIfAbsent(key, k -> new ArrayList<>()).add(tuple);
      } else {
        assignments.forEach((label, values) -> mergeSubstituents(independent, label, values));
      }
    }

    List<CorrelatedGroup> groups = new ArrayList<>();
    tables.forEach((labels, tuples) -> groups.add(new CorrelatedGroup(labels, tuples)));
    return new ParsedDefinitions(independent, groups);
  }

  /**
   * Groups the physical lines of a text node into logical rows. A line that introduces a {@code
   * label =} assignment starts a new row; a line without one is a wrapped continuation of the
   * previous row's value list (ChemDraw soft-wraps long substituent lists) and is appended to it.
   *
   * @param input the raw text of a node
   * @return the logical rows, continuation lines already merged
   */
  private List<String> toLogicalRows(String input) {
    List<String> rows = new ArrayList<>();
    for (String line : input.split("[\\r\\n]+")) {
      String trimmed = line.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      boolean startsAssignment = Definitions.RGROUP_ASSIGN_PATTERN.matcher(trimmed).find();
      if (startsAssignment || rows.isEmpty()) {
        rows.add(trimmed);
      } else {
        int last = rows.size() - 1;
        rows.set(last, rows.get(last) + ", " + trimmed);
      }
    }
    return rows;
  }

  /**
   * Parses a single line into its {@code label = values} assignments, in order. Handles multiple
   * assignments per line ("R1 = F, R2 = H"), chained equality ("R1 = R2 = H"), and comma/semicolon
   * value lists.
   *
   * @param row a single line of definition text
   * @return ordered map of label to its parsed substituent values
   */
  private Map<String, List<String>> parseRow(String row) {
    Map<String, List<String>> result = new LinkedHashMap<>();

    // Locate every "label =" head, then treat the text up to the next head (or end of line) as its
    // right-hand side so one assignment cannot swallow the next.
    Matcher assignMatcher = Definitions.RGROUP_ASSIGN_PATTERN.matcher(row);
    List<String> identifiers = new ArrayList<>();
    List<Integer> rhsStarts = new ArrayList<>();
    List<Integer> headStarts = new ArrayList<>();
    while (assignMatcher.find()) {
      identifiers.add(assignMatcher.group(1));
      rhsStarts.add(assignMatcher.end());
      headStarts.add(assignMatcher.start());
    }

    List<String> chainedLabels = new ArrayList<>();
    for (int i = 0; i < identifiers.size(); i++) {
      int from = rhsStarts.get(i);
      int to = (i + 1 < identifiers.size()) ? headStarts.get(i + 1) : row.length();
      String rhs = row.substring(from, to).replaceAll("^[\\s,;]+|[\\s,;]+$", "");
      List<String> abbreviations = parseSubstituents(rhs);
      if (abbreviations.isEmpty()) {
        // An empty right-hand side is a chained assignment, e.g. "R1 = R2 = H": defer this label
        // until the shared value is found.
        chainedLabels.add(identifiers.get(i));
        continue;
      }
      mergeSubstituents(result, identifiers.get(i), abbreviations);
      for (String chained : chainedLabels) {
        mergeSubstituents(result, chained, abbreviations);
      }
      chainedLabels.clear();
    }

    return result;
  }

  /**
   * Splits the right-hand side of an R-group definition into individual substituents. Supports the
   * enumerated form "(a) H, (b) F, ..." and plain lists separated by commas or semicolons.
   *
   * @param rhs the right-hand side text of a single "label = ..." definition
   * @return the list of substituent tokens, empty if none were found
   */
  private List<String> parseSubstituents(String rhs) {
    List<String> abbreviations = new ArrayList<>();

    // Case 1: enumerated (a) H, (b) F, ...
    Pattern enumerated = Pattern.compile("\\([a-zA-Z]\\)\\s*([^,;]+)");
    Matcher enumMatcher = enumerated.matcher(rhs);
    while (enumMatcher.find()) {
      abbreviations.add(enumMatcher.group(1).trim());
    }

    // Case 2: plain comma- or semicolon-separated list if no (a)/(b)/(c) found
    if (abbreviations.isEmpty()) {
      for (String rawPart : rhs.split("\\s*[,;]\\s*")) {
        String part = rawPart.trim();
        // A substituent/abbreviation is a single token; drop any trailing annotation glued on with
        // whitespace, e.g. "H Keq 1" -> "H".
        if (!part.isEmpty()) {
          part = part.split("\\s+")[0];
        }
        // Drop leftover scheme-item numbers (e.g. "5:") that are not substituents.
        if (!part.isEmpty() && !part.matches("\\d+:?")) {
          abbreviations.add(part);
        }
      }
    }

    return abbreviations;
  }

  /**
   * Returns the page-wide union of extracted R-groups (definitions from all text nodes merged).
   * Useful as a fallback when definitions cannot be scoped to a specific scaffold.
   *
   * @return a map where keys are R-group labels and values are lists of substituents
   */
  public Map<String, List<String>> getRgroups() {
    return rgroups;
  }

  /**
   * Returns the per-text-node definition blocks, each tagged with the position of its source text.
   *
   * @return the list of extracted definition blocks
   */
  public List<RGroupDefinitionBlock> getBlocks() {
    return blocks;
  }
}
