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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDObjectTag;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.utils.Definitions;

/** Visitor class for traversing a ChemDraw page and extracting R-group definitions from text. */
public class TextVisitor extends CDVisitor {

  /** A ring position, or the several a locant set names: the {@code 5,7} of {@code 5,7-Me2}. */
  private static final String LOCANT_SET = "\\d{1,2}(?:,\\d{1,2})*";

  /** A ring substituent named by number: {@code 2-CH3}, {@code 3,5-Me2}. */
  private static final Pattern RING_SUBSTITUENT = Pattern.compile("(" + LOCANT_SET + ")-.+");

  /**
   * A ring substituent ending in the formula of a benzene ring with fewer than four hydrogens,
   * which therefore carries more substituents than this one: the {@code 3-MeOC6H3} of {@code 4-OH,
   * 3-MeOC6H3}. Group 2 is the ring's hydrogen count.
   */
  private static final Pattern RING_FORMULA = Pattern.compile("(" + LOCANT_SET + ")-.*?C6H([0-3])");

  /**
   * What several ring substituents of one compound are joined with into one value. A list separator
   * never survives value splitting, so the joined value cannot be mistaken for anything a legend
   * wrote.
   */
  public static final String SUBSTITUENT_JOINER = "; ";

  /**
   * The letter a legend line opens with when it names its compound by letter: {@code a: R = H},
   * {@code (b) R = Me}, {@code c) R = Et}.
   */
  private static final Pattern ENTRY_LETTER = Pattern.compile("^\\(?([a-z])\\)?\\s*[:.)]\\s*\\S");

  /**
   * The letter of a legend line that names one compound by number and letter: the {@code a} of
   * {@code 64a: R1 = ...}. A line naming a range ({@code 40a–m}) has none.
   */
  private static final Pattern NUMBERED_ENTRY_LETTER =
      Pattern.compile("^\\(?\\d{1,4}([a-z])\\)?\\s*[:.)]\\s*\\S");

  /**
   * The compound number a legend line opens with: the {@code 36} of {@code 36a: Ar = ...}, or of a
   * line naming a range of the series, {@code 40a–m: R1 = H}. Group 2 is the range's last letter.
   */
  private static final Pattern COMPOUND_NUMBER =
      Pattern.compile(
          "^\\(?(\\d{1,4})(?:[a-z](?:\\s*[–-]\\s*(?:\\d{1,4})?([a-z]))?)?\\)?\\s*[:.)]\\s*\\S");

  /**
   * A text that is a compound number alone, the caption under a drawn structure: {@code 43}, {@code
   * 42a–l}, {@code (12)}. Group 1 is the number.
   */
  /**
   * The run of single letters a {@link LetterRow} ends with, each set apart by spaces; group 1
   * starts at its first letter.
   */
  private static final Pattern LETTER_RUN = Pattern.compile("(?:^|\\s)([a-z](?:\\s+[a-z])+)\\s*$");

  /** The label of a bracket's repeat count, as legend lines give it: {@code 26g: n = 2, R = H}. */
  public static final String REPEAT = "n";

  /** A repeat count {@code n = 2} in a legend line; group 1 is the count. */
  private static final Pattern REPEAT_COUNT =
      Pattern.compile("(?:^|[\\s,;:(])n\\s*=\\s*(\\d{1,2})(?![\\d.,–-]*\\d)");

  /** One ring letter of a {@link RingTable} header: {@code A ring}, {@code B-ring}. */
  private static final Pattern RING_LETTER = Pattern.compile("\\b([A-Z])(?:\\s*-\\s*|\\s+)ring\\b");

  /** A {@link RingTable} header: ring letters and nothing else. */
  private static final Pattern RING_HEADER =
      Pattern.compile("\\s*[A-Z](?:\\s*-\\s*|\\s+)ring(?:\\s+[A-Z](?:\\s*-\\s*|\\s+)ring)*\\s*");

  /** A compound line of a {@link RingTable}: {@code 20a: 3-triazolyl 3,4-diOMe}. */
  private static final Pattern RING_ROW =
      Pattern.compile("\\(?(\\d{1,4}[a-z]?)\\)?\\s*[:.)]\\s*(\\S.*)");

  private static final Pattern CAPTION =
      Pattern.compile("^\\(?(\\d{1,4})(?:[a-z](?:\\s*[–-]\\s*(?:\\d{1,4})?[a-z])?)?\\)?$");

  /**
   * A compound number standing alone as the caption of a drawn structure.
   *
   * @param bounds the bounding box of the caption text
   * @param number the compound number, without letters, e.g. {@code 42} for {@code 42a–l}
   */
  public record Caption(CDRectangle bounds, String number) {}

  /**
   * One line of entry letters set out under a row of drawings, e.g. {@code a b c}, possibly after
   * the definition that holds for its entries ({@code R = NO2 g h i}).
   *
   * @param bounds the bounding box of the text
   * @param letters each letter with the estimated x of its centre, from left to right
   */
  public record LetterRow(CDRectangle bounds, List<Letter> letters) {}

  /**
   * A letter of a {@link LetterRow}.
   *
   * @param letter the entry letter
   * @param x the estimated x of its centre on the page
   */
  public record Letter(char letter, double x) {}

  /**
   * A table naming, per compound, where each lettered ring of the scaffold carries its variable
   * group: a header of ring letters ({@code A ring B ring}) and one line per compound giving a
   * value per ring ({@code 20a: 3-triazolyl 3,4-diOMe}).
   *
   * @param bounds the bounding box of the text
   * @param rings the ring letters, in column order
   * @param rows the compound lines, in order
   */
  public record RingTable(CDRectangle bounds, List<String> rings, List<RingRow> rows) {}

  /**
   * A compound line of a {@link RingTable}.
   *
   * @param entry the compound, e.g. {@code 20a}
   * @param values one value per ring, in column order
   */
  public record RingRow(String entry, List<String> values) {}

  private final List<RingTable> ringTables = new ArrayList<>();

  private final Map<String, List<String>> rgroups;
  private final List<LetterRow> letterRows = new ArrayList<>();
  private final List<RGroupDefinitionBlock> blocks;
  private final List<Caption> captions;

  /** Texts that belong to a drawing rather than the page: atom labels and object tags. */
  private final Set<CDText> drawingTexts = Collections.newSetFromMap(new IdentityHashMap<>());

  /**
   * Constructs a {@code TextVisitor} and traverses the given page to collect R-group definitions.
   *
   * @param page the {@link CDPage} to traverse for text containing R-group definitions
   */
  public TextVisitor(CDPage page) {
    rgroups = new LinkedHashMap<>();
    blocks = new ArrayList<>();
    captions = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Notes the atom's label as part of its drawing: a number on an atom is no caption. An atom is
   * visited before its label.
   *
   * @param atom the atom being visited
   */
  @Override
  public void visitAtom(CDAtom atom) {
    if (atom.getText() != null) {
      drawingTexts.add(atom.getText());
    }
  }

  /**
   * Notes the tag's texts as part of the drawing, e.g. the repeat count of a bracket.
   *
   * @param tag the object tag being visited
   */
  @Override
  public void visitObjectTag(CDObjectTag tag) {
    drawingTexts.addAll(tag.getTexts());
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
    LetterRow row = letterRow(cdText);
    if (row != null) {
      letterRows.add(row);
    }
    RingTable ringTable = ringTable(cdText);
    if (ringTable != null) {
      ringTables.add(ringTable);
      return;
    }
    Matcher caption = CAPTION.matcher(cdText.getText().getText().strip());
    if (caption.matches()) {
      if (cdText.getBounds() != null && !drawingTexts.contains(cdText)) {
        captions.add(new Caption(cdText.getBounds(), caption.group(1)));
      }
      return;
    }
    ParsedDefinitions parsed = extractDefinitions(cdText.getText().getText());
    if (parsed.independent().isEmpty() && parsed.correlatedGroups().isEmpty()) {
      return;
    }
    blocks.add(
        new RGroupDefinitionBlock(
            cdText.getBounds(),
            parsed.independent(),
            parsed.correlatedGroups(),
            lineHeight(cdText),
            entryLetters(cdText.getText().getText()),
            compoundNumbers(cdText.getText().getText()),
            parsed.seriesAssignments()));
    // Keep a page-wide union as a fallback; merge rather than drop on label collisions. Correlated
    // values are flattened into the union too so the fallback/guard still see them (the union path
    // over-enumerates by design; the scoped path preserves the tuples). A label defined with no
    // values adds nothing to it.
    for (Map.Entry<String, List<String>> entry : parsed.independent().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        mergeSubstituents(rgroups, entry.getKey(), entry.getValue());
      }
    }
    for (CorrelatedGroup group : parsed.correlatedGroups()) {
      for (Map<String, String> tuple : group.tuples()) {
        tuple.forEach((label, value) -> mergeSubstituents(rgroups, label, List.of(value)));
      }
    }
  }

  /**
   * Reads a single-line text ending in two or more consecutive entry letters set apart by spaces.
   * Where each letter sits is estimated from the advance widths of the characters before it, scaled
   * to the text's width: the page gives the bounds of a text, not of its characters.
   */
  private static LetterRow letterRow(CDText cdText) {
    String text = cdText.getText().getText();
    if (cdText.getBounds() == null || text.contains("\r") || text.contains("\n")) {
      return null;
    }
    Matcher matcher = LETTER_RUN.matcher(text);
    if (!matcher.find()) {
      return null;
    }
    List<Integer> positions = new ArrayList<>();
    for (int i = matcher.start(1); i < text.length(); i++) {
      if (!Character.isWhitespace(text.charAt(i))) {
        positions.add(i);
      }
    }
    for (int i = 1; i < positions.size(); i++) {
      if (text.charAt(positions.get(i)) != text.charAt(positions.get(i - 1)) + 1) {
        return null;
      }
    }
    double total = advance(text, text.length());
    CDRectangle bounds = cdText.getBounds();
    double width = bounds.getRight() - bounds.getLeft();
    if (total <= 0 || width <= 0) {
      return null;
    }
    List<Letter> letters = new ArrayList<>();
    for (int position : positions) {
      double centre =
          advance(text, position) + advance(text.substring(position, position + 1), 1) / 2;
      letters.add(new Letter(text.charAt(position), bounds.getLeft() + width * centre / total));
    }
    return new LetterRow(bounds, List.copyOf(letters));
  }

  /**
   * Reads a {@link RingTable}: a header line of ring letters followed by compound lines with one
   * value per ring, the columns set apart by two or more spaces.
   */
  private static RingTable ringTable(CDText cdText) {
    if (cdText.getBounds() == null) {
      return null;
    }
    List<String> lines =
        Arrays.stream(cdText.getText().getText().split("\\r\\n|\\r|\\n"))
            .filter(line -> !line.isBlank())
            .toList();
    if (lines.size() < 2 || !RING_HEADER.matcher(lines.get(0)).matches()) {
      return null;
    }
    List<String> rings = new ArrayList<>();
    Matcher letter = RING_LETTER.matcher(lines.get(0));
    while (letter.find()) {
      rings.add(letter.group(1));
    }
    List<RingRow> rows = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      Matcher matcher = RING_ROW.matcher(line.strip());
      if (!matcher.matches()) {
        return null;
      }
      List<String> values = List.of(matcher.group(2).strip().split("\\s{2,}"));
      if (values.size() != rings.size()) {
        return null;
      }
      rows.add(new RingRow(matcher.group(1), values));
    }
    return new RingTable(cdText.getBounds(), List.copyOf(rings), List.copyOf(rows));
  }

  /**
   * The estimated advance width of the first {@code end} characters, in ems: a space is narrow, a
   * capital wide, anything else average. Only the proportions matter.
   */
  private static double advance(String text, int end) {
    double advance = 0;
    for (int i = 0; i < end; i++) {
      char c = text.charAt(i);
      advance += Character.isWhitespace(c) ? 0.278 : Character.isUpperCase(c) ? 0.667 : 0.556;
    }
    return advance;
  }

  /** Height of one line of the text: its bounds' height over its number of lines. */
  private static float lineHeight(CDText cdText) {
    if (cdText.getBounds() == null) {
      return 0;
    }
    int lines = cdText.getText().getText().strip().split("\\r\\n|\\r|\\n", -1).length;
    return Math.abs(cdText.getBounds().getHeight()) / lines;
  }

  /**
   * The letters the text's lines open with, in order, also after a compound number ({@code 64a:});
   * empty if none is lettered.
   */
  private static String entryLetters(String text) {
    StringBuilder letters = new StringBuilder();
    for (String line : text.split("[\\r\\n]+")) {
      Matcher matcher = ENTRY_LETTER.matcher(line.strip());
      if (!matcher.find()) {
        matcher = NUMBERED_ENTRY_LETTER.matcher(line.strip());
        if (!matcher.find()) {
          continue;
        }
      }
      letters.append(matcher.group(1));
    }
    return letters.toString();
  }

  /** The compound numbers the text's lines open with, in order of first appearance. */
  private static Set<String> compoundNumbers(String text) {
    Set<String> numbers = new LinkedHashSet<>();
    for (String line : text.split("[\\r\\n]+")) {
      Matcher matcher = COMPOUND_NUMBER.matcher(line.strip());
      if (matcher.find()) {
        numbers.add(matcher.group(1));
      }
    }
    return numbers;
  }

  /**
   * Independent per-label lists, correlated (positional-table) groups, and the values lines naming
   * a range of a series assign to it, from one text node.
   */
  private record ParsedDefinitions(
      Map<String, List<String>> independent,
      List<CorrelatedGroup> correlatedGroups,
      Map<String, Map<String, String>> seriesAssignments) {}

  /** One {@code label = values} assignment, kept in source order (repeats preserved). */
  private record Assignment(String label, List<String> values) {}

  /**
   * Adds substituents to the list for a label, creating the list if needed and skipping duplicates.
   *
   * @param target the map to merge into
   * @param label the R-group label
   * @param substituents the substituents to add
   */
  private static void mergeSubstituents(
      Map<String, List<String>> target, String label, List<String> substituents) {
    List<String> existing = target.computeIfAbsent(label, _ -> new ArrayList<>());
    for (String substituent : substituents) {
      if (!existing.contains(substituent)) {
        existing.add(substituent);
      }
    }
  }

  /**
   * Parses one text node into independent per-label definitions and correlated (positional-table)
   * groups. Assignments are read in source order and partitioned into row-tuples: a label that
   * repeats starts a new tuple, so several rows sharing one physical line — e.g. {@code "10 X = a,
   * Y = b; 11 X = c, Y = d"} — are recognised as distinct tuples of the same table. A row that
   * resolves to two or more labels, each with a single value, is a correlated tuple; anything else
   * (single-label lists, multi-valued labels) contributes to the independent per-label lists.
   *
   * @param input the raw text of a node
   * @return the parsed definitions
   */
  private ParsedDefinitions extractDefinitions(String input) {
    Map<String, List<String>> independent = new LinkedHashMap<>();
    // Correlated rows keyed by their (sorted) label set, so rows of the same table accumulate.
    Map<List<String>, List<Map<String, String>>> tables = new LinkedHashMap<>();
    Map<String, Map<String, String>> series = new LinkedHashMap<>();

    for (String row : toLogicalRows(input)) {
      List<Assignment> assignments =
          parseAssignments(row).stream().map(TextVisitor::oneRing).toList();
      if (ENTRY_LETTER.matcher(row).find() || COMPOUND_NUMBER.matcher(row).find()) {
        // "26g: n = 2, R = H": the repeat count of the scaffold's bracket varies with the entry.
        // Lines giving only n run on into one row, so each n found is an entry of its own.
        Matcher repeat = REPEAT_COUNT.matcher(row);
        List<Assignment> counts = new ArrayList<>();
        while (repeat.find()) {
          counts.add(new Assignment(REPEAT, List.of(repeat.group(1))));
        }
        if (!counts.isEmpty()
            && assignments.stream().noneMatch(assignment -> assignment.label().equals(REPEAT))) {
          assignments = new ArrayList<>(assignments);
          assignments.addAll(counts);
        }
      }
      Matcher number = COMPOUND_NUMBER.matcher(row);
      if (number.find()
          && number.group(2) != null
          && !assignments.isEmpty()
          && assignments.stream().allMatch(assignment -> assignment.values().size() == 1)) {
        // "40a–m: R1 = H": every compound of the series 40 has R1 = H.
        Map<String, String> fixed =
            series.computeIfAbsent(number.group(1), _ -> new LinkedHashMap<>());
        assignments.forEach(
            assignment -> fixed.put(assignment.label(), assignment.values().getFirst()));
      }
      if (ENTRY_LETTER.matcher(row).find()) {
        assignments = assignments.stream().map(TextVisitor::oneCompound).toList();
      }
      if (assignments.isEmpty()) {
        continue;
      }

      List<Map<String, List<String>>> tuples = partitionIntoTuples(assignments);
      boolean allSingleValued =
          tuples.stream().allMatch(t -> t.values().stream().allMatch(v -> v.size() == 1));
      boolean correlated =
          allSingleValued
              && (tuples.size() >= 2 || (tuples.size() == 1 && tuples.getFirst().size() >= 2));

      if (correlated) {
        for (Map<String, List<String>> tuple : tuples) {
          List<String> key = new ArrayList<>(tuple.keySet());
          key.sort(null);
          Map<String, String> row2 = new LinkedHashMap<>();
          tuple.forEach((label, values) -> row2.put(label, values.getFirst()));
          tables.computeIfAbsent(key, _ -> new ArrayList<>()).add(row2);
        }
      } else {
        for (Assignment assignment : assignments) {
          mergeSubstituents(independent, assignment.label(), assignment.values());
        }
      }
    }

    List<CorrelatedGroup> groups = new ArrayList<>();
    tables.forEach((labels, tuples) -> groups.add(new CorrelatedGroup(labels, tuples)));
    return new ParsedDefinitions(independent, groups, series);
  }

  /**
   * Rejoins the substituents of one benzene ring that the value split separated. A value whose ring
   * formula has fewer hydrogens than its own substituents leave ({@code 3-MeOC6H3} has room for one
   * more) takes as many of the ring substituents just before it: {@code 4-OH, 3-MeOC6H3} is one
   * 4-hydroxy-3-methoxyphenyl, not two values.
   */
  private static Assignment oneRing(Assignment assignment) {
    List<String> values = new ArrayList<>();
    for (String value : assignment.values()) {
      Matcher formula = RING_FORMULA.matcher(value);
      if (formula.matches()) {
        // The pattern admits a single digit 0-3 only.
        int others =
            5
                - Character.digit(formula.group(2).charAt(0), 10)
                - formula.group(1).split(",").length;
        int from = values.size() - others;
        if (others > 0
            && from >= 0
            && values.subList(from, values.size()).stream()
                .allMatch(
                    other -> RING_SUBSTITUENT.matcher(other).matches() && !other.contains("C6H"))) {
          List<String> parts = new ArrayList<>(values.subList(from, values.size()));
          parts.add(value);
          values.subList(from, values.size()).clear();
          values.add(String.join(", ", parts));
          continue;
        }
      }
      values.add(value);
    }
    return values.equals(assignment.values())
        ? assignment
        : new Assignment(assignment.label(), List.copyOf(values));
  }

  /**
   * The values of a lettered line, which names one compound: ring substituents listed at different
   * positions ({@code 2-CH3, 5-NO2}) are all on that compound, so they make one value. Any other
   * list is left as alternatives.
   */
  private static Assignment oneCompound(Assignment assignment) {
    List<String> values = assignment.values();
    if (values.size() < 2) {
      return assignment;
    }
    Set<String> positions = new HashSet<>();
    for (String value : values) {
      Matcher matcher = RING_SUBSTITUENT.matcher(value);
      if (!matcher.matches()) {
        return assignment;
      }
      for (String position : matcher.group(1).split(",")) {
        if (!positions.add(position)) {
          return assignment;
        }
      }
    }
    return new Assignment(assignment.label(), List.of(String.join(SUBSTITUENT_JOINER, values)));
  }

  /**
   * Splits an ordered assignment list into row-tuples, starting a new tuple whenever a label is
   * seen that the current tuple already holds. This turns a repeated {@code label = ...} pattern
   * (multiple table rows glued onto one line) into the individual rows.
   *
   * @param assignments the assignments of one logical row, in source order
   * @return the tuples, each mapping its labels to their (possibly multi-)value lists
   */
  private List<Map<String, List<String>>> partitionIntoTuples(List<Assignment> assignments) {
    List<Map<String, List<String>>> tuples = new ArrayList<>();
    Map<String, List<String>> current = new LinkedHashMap<>();
    for (Assignment assignment : assignments) {
      if (current.containsKey(assignment.label())) {
        tuples.add(current);
        current = new LinkedHashMap<>();
      }
      current.put(assignment.label(), assignment.values());
    }
    if (!current.isEmpty()) {
      tuples.add(current);
    }
    return tuples;
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
   * Parses a single line into its {@code label = values} assignments, in source order, preserving
   * repeated labels (so several table rows on one line survive as separate assignments). Handles
   * multiple assignments per line ("R1 = F, R2 = H"), chained equality ("R1 = R2 = H"), label lists
   * sharing one value ("R1, R2 = H"), and comma/semicolon value lists.
   *
   * @param row a single line of definition text
   * @return the assignments, in order, one entry per {@code label =} head
   */
  private List<Assignment> parseAssignments(String row) {
    List<Assignment> result = new ArrayList<>();

    // A label list sharing one value ("R1, R2 = H") is the chained form "R1 = R2 = H".
    row = Definitions.RGROUP_LABEL_LIST_PATTERN.matcher(row).replaceAll("$1 = ");

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
      result.add(new Assignment(identifiers.get(i), abbreviations));
      for (String chained : chainedLabels) {
        result.add(new Assignment(chained, abbreviations));
      }
      chainedLabels.clear();
    }

    // A line whose only assignment has nothing after it ("a: R =") gives the value as a drawn
    // structure. The label is still defined there, with no values the text can supply.
    if (result.isEmpty()) {
      for (String label : chainedLabels) {
        result.add(new Assignment(label, List.of()));
      }
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
      for (String rawPart : splitValues(rhs)) {
        String part = rawPart.trim();
        // A leaked reaction condition of the form "label = value" (e.g. a temperature
        // "T = 250 °C" or "t = 30 min") is not a substituent. Its label is one the assignment
        // parser does not recognise as an R-group, so it arrives glued onto a value list; drop the
        // whole part rather than keeping its label token as a bogus substituent.
        if (part.matches("[^=\\s]+\\s*=.*")) {
          continue;
        }
        // A substituent/abbreviation is a single token; drop any trailing annotation glued on with
        // whitespace, e.g. "H Keq 1" -> "H".
        if (!part.isEmpty()) {
          part = part.split("\\s+")[0];
        }
        // A row that names its compound after a colon ("R3 = CO2Me: 12k") leaves the colon on the
        // last value; no substituent ends in one.
        part = part.replaceFirst(":+$", "");
        // Drop leftover scheme-item numbers ("5:") and reaction yields ("84%") that are not
        // substituents.
        if (!part.isEmpty() && !part.matches("\\d+[:%]?")) {
          abbreviations.add(part);
        }
      }
    }

    return abbreviations;
  }

  /**
   * Splits a value list on its separators, keeping a locant set together: the comma of {@code
   * 5,7-Me2} names a second ring position for one value, it does not start another one. Such a
   * comma is one with nothing but a bare number in front of it and a number that carries its own
   * substituent behind it — a list separator always has a whole value on at least one side.
   *
   * @param rhs the right-hand side text of a single "label = ..." definition
   * @return the value tokens, locant sets unsplit
   */
  private static List<String> splitValues(String rhs) {
    List<String> values = new ArrayList<>();
    for (String part : rhs.split("\\s*[,;]\\s*")) {
      values.add(part);
      // A set of three or more locants ("3,4,5-(OMe)3") arrives one number at a time, so keep
      // folding the bare number in front of the value until there is none left.
      while (values.size() > 1
          && values.get(values.size() - 2).matches(LOCANT_SET)
          && values.getLast().matches(LOCANT_SET + "-.+")) {
        String merged = values.get(values.size() - 2) + "," + values.removeLast();
        values.set(values.size() - 1, merged);
      }
    }
    return values;
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

  /**
   * Returns the compound numbers that stand alone on the page, the captions of drawn structures.
   *
   * @return the captions, in page order
   */
  public List<Caption> getCaptions() {
    return captions;
  }

  /**
   * Returns the lines of entry letters set out under rows of drawings.
   *
   * @return the letter rows, in page order
   */
  public List<LetterRow> getLetterRows() {
    return letterRows;
  }

  /**
   * Returns the tables naming where each lettered ring carries its variable group.
   *
   * @return the ring tables, in page order
   */
  public List<RingTable> getRingTables() {
    return ringTables;
  }
}
