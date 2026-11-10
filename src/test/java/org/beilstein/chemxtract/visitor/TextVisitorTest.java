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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.jupiter.api.Test;

/** Unit tests for R-group text parsing in {@link TextVisitor}. */
public class TextVisitorTest {

  private static CDPage pageWithTexts(String... texts) {
    CDPage page = new CDPage();
    for (String s : texts) {
      CDStyledString styled = new CDStyledString();
      styled.addChunk(new CDStyledString.CDXChunk(null, 10f, null, null, s));
      CDText text = new CDText();
      text.setText(styled);
      page.addText(text);
    }
    return page;
  }

  @Test
  public void parsesPlainCommaList() {
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R = CH3, Cl")).getRgroups();
    assertEquals(List.of("CH3", "Cl"), r.get("R"));
  }

  @Test
  public void splitsSemicolonSeparatedList() {
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R = Cl; Br")).getRgroups();
    assertEquals(List.of("Cl", "Br"), r.get("R"));
  }

  @Test
  public void stripsLeadingSchemeNumbering() {
    // Numbered scheme items must not leak "13:" into the substituent list.
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("13: R = Cl; Br")).getRgroups();
    assertEquals(List.of("Cl", "Br"), r.get("R"));
  }

  @Test
  public void stripsInterspersedSchemeNumbering() {
    // "R = H, 5: R1 = OMe" — the "5:" belongs to the next scheme item and must not become a
    // substituent of R (the leak observed in m48800432-2).
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R = H, 5: R1 = OMe")).getRgroups();
    assertEquals(List.of("H"), r.get("R"), "scheme number must not leak into R");
    assertEquals(List.of("OMe"), r.get("R1"));
  }

  @Test
  public void mergesRepeatedLabelWithinOneNode() {
    // A numbered list in one text node repeats the same label per row; all values belong to it.
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("4: R = H\r5: R = CH3\r6: R = OCH3")).getRgroups();
    assertEquals(List.of("H", "CH3", "OCH3"), r.get("R"), "repeated R rows must be merged");
  }

  @Test
  public void stripsTrailingAnnotationFromSubstituent() {
    // "R = H Keq 1" glues an equilibrium annotation onto the substituent; the substituent is H.
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R = H Keq    1")).getRgroups();
    assertEquals(List.of("H"), r.get("R"), "annotation after the substituent must be dropped");
  }

  @Test
  public void joinsWrappedContinuationLines() {
    // A long value list soft-wraps to a second line without repeating "R ="; both lines belong to
    // R.
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("R = 4-CH3Ph, 2-OCH3Ph\r4-ClPh, 4-BrPh")).getRgroups();
    assertEquals(List.of("4-CH3Ph", "2-OCH3Ph", "4-ClPh", "4-BrPh"), r.get("R"));
  }

  @Test
  public void stripsYieldPercentages() {
    // Reaction yields ("; 84%") are annotations, not substituents.
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("3b: R = H; 84%\r3c: R = Me; 74%")).getRgroups();
    assertEquals(List.of("H", "Me"), r.get("R"), "yields must not leak into substituents");
  }

  @Test
  public void yieldAnnotationsDoNotBreakTupleTable() {
    // Each row carries a trailing "; NN%" yield; the (R1,R2) pairing must still be recognised.
    RGroupDefinitionBlock block =
        new TextVisitor(pageWithTexts("2s: R1 = CN, R2 = H; 61%\r2t: R1 = CN, R2 = Br; 47%"))
            .getBlocks()
            .get(0);
    assertTrue(block.definitions().isEmpty(), "yield-annotated rows are still a correlated table");
    assertEquals(1, block.correlatedGroups().size());
    CorrelatedGroup group = block.correlatedGroups().get(0);
    assertEquals(List.of("R1", "R2"), group.labels());
    assertEquals(
        List.of(Map.of("R1", "CN", "R2", "H"), Map.of("R1", "CN", "R2", "Br")), group.tuples());
  }

  @Test
  public void compoundNumberAfterAColonDoesNotLeaveTheColonOnTheValue() {
    // Rows that end "value: number (yield)" put the colon straight after the last value
    // (m28144482-i5); the values are CO2Me and H, not "CO2Me:" and "H:".
    RGroupDefinitionBlock block =
        new TextVisitor(
                pageWithTexts(
                    "R1 = R2 = H, R3 = CO2Me: 12k (72%)\rR1 = Me, R2 = R3 = H: 12a (73%)"))
            .getBlocks()
            .get(0);
    assertEquals(1, block.correlatedGroups().size());
    assertEquals(
        List.of(
            Map.of("R1", "H", "R2", "H", "R3", "CO2Me"), Map.of("R1", "Me", "R2", "H", "R3", "H")),
        block.correlatedGroups().get(0).tuples());
  }

  @Test
  public void positionalTableParsedAsCorrelatedGroup() {
    TextVisitor visitor =
        new TextVisitor(pageWithTexts("R1 = R2 = H\rR1 = F, R2 = H\rR1 = H, R2 = F"));
    List<RGroupDefinitionBlock> blocks = visitor.getBlocks();
    assertEquals(1, blocks.size());

    RGroupDefinitionBlock block = blocks.get(0);
    assertEquals(
        List.of(), List.copyOf(block.definitions().keySet()), "R1/R2 must not be independent");
    assertEquals(1, block.correlatedGroups().size());

    CorrelatedGroup group = block.correlatedGroups().get(0);
    assertEquals(List.of("R1", "R2"), group.labels());
    assertEquals(3, group.tuples().size(), "exactly the three table rows");
    assertTrue(group.tuples().contains(Map.of("R1", "H", "R2", "H")));
    assertTrue(group.tuples().contains(Map.of("R1", "F", "R2", "H")));
    assertTrue(group.tuples().contains(Map.of("R1", "H", "R2", "F")));
    assertFalse(
        group.tuples().contains(Map.of("R1", "F", "R2", "F")), "the (F,F) corner is not a row");
  }

  @Test
  public void multipleTuplesOnOneLineParsedAsCorrelatedGroup() {
    // A positional table whose rows sit on ONE physical line, each row prefixed by a compound
    // number and separated by ';': "10 X = .., Y = ..; 11 X = .., Y = ..; ...". Repeated X/Y heads
    // mark the row boundaries.
    RGroupDefinitionBlock block =
        new TextVisitor(
                pageWithTexts("10 X = PhCONMe, Y = N; 11 X = PhCONH, Y = C; 12 X = PhCOO, Y = N"))
            .getBlocks()
            .get(0);

    assertTrue(block.definitions().isEmpty(), "X/Y must be a correlated table, not independent");
    assertEquals(1, block.correlatedGroups().size());

    CorrelatedGroup group = block.correlatedGroups().get(0);
    assertEquals(List.of("X", "Y"), group.labels());
    assertEquals(
        List.of(
            Map.of("X", "PhCONMe", "Y", "N"),
            Map.of("X", "PhCONH", "Y", "C"),
            Map.of("X", "PhCOO", "Y", "N")),
        group.tuples(),
        "each ';'-separated row is its own tuple, in order");
  }

  @Test
  public void independentListNotTreatedAsCorrelated() {
    RGroupDefinitionBlock block = new TextVisitor(pageWithTexts("R = H, CH3")).getBlocks().get(0);
    assertTrue(block.correlatedGroups().isEmpty(), "a single-label list is not a table");
    assertEquals(List.of("H", "CH3"), block.definitions().get("R"));
  }

  @Test
  public void recognisesPrimedLabelAsDistinctFromUnprimed() {
    // "R = Troc, R' = DTBS" defines two distinct residues; R' must not be swallowed as a value of
    // R.
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("R = Troc, R' = DTBS")).getRgroups();
    assertEquals(List.of("Troc"), r.get("R"), "R must not absorb R' or DTBS");
    assertEquals(List.of("DTBS"), r.get("R'"), "R' must be its own label");
  }

  @Test
  public void resolvesChainedEqualityAcrossPrimedLabel() {
    // "R = R' = Ac" means both R and R' are Ac.
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R = R' = Ac")).getRgroups();
    assertEquals(List.of("Ac"), r.get("R"), "R must inherit the chained value");
    assertEquals(List.of("Ac"), r.get("R'"), "R' must be resolved, not treated as R's value");
  }

  @Test
  public void doubleAndTriplePrimedLabelsAreTableLabels() {
    // m28732236-i2: R', R'' and R''' are three labels, and "R', R'' = H" gives one value to both.
    RGroupDefinitionBlock block =
        new TextVisitor(
                pageWithTexts(
                    "4vv R', R'' = H, 36%\r4ww R' = Br, R'' =  H, 37%\r"
                        + "4y R' = OH, R'' = NEt2, R''' = H, 35%"))
            .getBlocks()
            .get(0);
    assertTrue(block.definitions().isEmpty(), "R'/R''/R''' rows are table rows");
    assertEquals(2, block.correlatedGroups().size());
    assertEquals(
        List.of(Map.of("R'", "H", "R''", "H"), Map.of("R'", "Br", "R''", "H")),
        block.correlatedGroups().get(0).tuples());
    assertEquals(
        List.of(Map.of("R'", "OH", "R''", "NEt2", "R'''", "H")),
        block.correlatedGroups().get(1).tuples());
  }

  @Test
  public void labelListGivesItsValueToEveryLabel() {
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R1, R2, R3 = Me")).getRgroups();
    assertEquals(List.of("Me"), r.get("R1"), "R1 must get the shared value");
    assertEquals(List.of("Me"), r.get("R2"), "R2 must get the shared value");
    assertEquals(List.of("Me"), r.get("R3"), "R3 must get the shared value");
  }

  @Test
  public void resolvesChainedEqualityToAllLabels() {
    // "R1 = R2 = H" means both R1 and R2 are H.
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R1 = R2 = H")).getRgroups();
    assertEquals(List.of("H"), r.get("R1"), "R1 must inherit the chained value");
    assertEquals(List.of("H"), r.get("R2"), "R2 must be resolved");
  }

  @Test
  public void keepsLocantSetTogether() {
    // "5,7-Me2" names one disubstituted value, not the scheme number 5 followed by "7-Me2": the
    // comma inside a locant set is not a list separator (#166).
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("R6 = H, 7-Me, 5,7-Me2")).getRgroups();
    assertEquals(List.of("H", "7-Me", "5,7-Me2"), r.get("R6"), "the locant set must stay whole");
  }

  @Test
  public void keepsThreeMemberedLocantSetTogether() {
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("R = H, 3,4,5-(OMe)3")).getRgroups();
    assertEquals(List.of("H", "3,4,5-(OMe)3"), r.get("R"));
  }

  /**
   * A legend line whose value is a drawn structure ("a: R =" beside it) defines the label with no
   * values, so the scaffold beside it is known to have its own legend; the page-wide union gains
   * nothing from it.
   */
  @Test
  public void labelGivenOnlyAsDrawingIsDefinedWithoutValues() {
    TextVisitor visitor = new TextVisitor(pageWithTexts("a: R ="));
    assertEquals(1, visitor.getBlocks().size());
    assertEquals(Map.of("R", List.of()), visitor.getBlocks().get(0).definitions());
    assertFalse(visitor.getRgroups().containsKey("R"), "an empty definition adds nothing");
  }

  /** The letters legend lines open with are kept in order, so lettered columns can be matched. */
  @Test
  public void recordsTheLettersLegendLinesOpenWith() {
    assertEquals(
        "abc",
        new TextVisitor(pageWithTexts("a: R = H\rb: R = Me\r(c) R = Et"))
            .getBlocks()
            .get(0)
            .entryLetters());
    assertEquals("", new TextVisitor(pageWithTexts("R = H, Me")).getBlocks().get(0).entryLetters());
  }

  /**
   * A lettered line names one compound, so ring substituents listed on it at different positions
   * ("p: R = 2-CH3, 5-NO2") are one value, not two alternatives.
   */
  @Test
  public void ringSubstituentsOnALetteredLineAreOneValue() {
    Map<String, List<String>> r =
        new TextVisitor(pageWithTexts("o: R = 2,4,6-CH3\rp: R = 2-CH3, 5-NO2")).getRgroups();
    assertEquals(List.of("2,4,6-CH3", "2-CH3; 5-NO2"), r.get("R"));
    assertEquals(
        List.of("H", "2-NO2; 4-Cl"),
        new TextVisitor(pageWithTexts("30a: R = H\r30f: R = 2-NO2, 4-Cl")).getRgroups().get("R"),
        "a line numbered with its compound (30f, m50167957-8) names one compound too");
  }

  /** Without a letter, or when not every value names a ring position, a list stays a list. */
  @Test
  public void otherListsStayAlternatives() {
    assertEquals(
        List.of("2-CH3", "5-NO2"),
        new TextVisitor(pageWithTexts("R = 2-CH3, 5-NO2")).getRgroups().get("R"));
    assertEquals(
        List.of("H", "4-Me"),
        new TextVisitor(pageWithTexts("a: R = H, 4-Me")).getRgroups().get("R"));
  }

  /**
   * The compound numbers legend lines open with are recorded, and a line naming a range of a series
   * ("40a–m: R1 = H") records the value it fixes for that series, as in 40/41 of {@code
   * cheminf/bugs/markush/m50167957-9}.
   */
  @Test
  public void recordsCompoundNumbersAndWhatASeriesRangeFixes() {
    RGroupDefinitionBlock caption =
        new TextVisitor(pageWithTexts("40a–m: R1 = H\r41a–e: R1 = OMe")).getBlocks().get(0);
    assertEquals(Set.of("40", "41"), caption.compoundNumbers());
    assertEquals(
        Map.of("40", Map.of("R1", "H"), "41", Map.of("R1", "OMe")), caption.seriesAssignments());

    RGroupDefinitionBlock legend =
        new TextVisitor(pageWithTexts("36a: Ar = 4-OHC6H4\r36b: Ar = 4-MeOC6H4"))
            .getBlocks()
            .get(0);
    assertEquals(Set.of("36"), legend.compoundNumbers());
    assertTrue(legend.seriesAssignments().isEmpty(), "single compounds fix nothing per series");
    assertTrue(
        new TextVisitor(pageWithTexts("R = H, Me")).getBlocks().get(0).compoundNumbers().isEmpty());
  }

  /**
   * A line of entry letters under a row of drawings is read with each letter's estimated place,
   * also after the definition that holds for its entries; letters out of sequence are not.
   */
  @Test
  public void readsLetterRowsUnderDrawings() {
    CDPage page = pageWithTexts("   a        b        c", "R = NO2   g   h", "a  c", "R = H");
    page.getTexts().forEach(text -> text.setBounds(new CDRectangle()));
    page.getTexts().get(0).getBounds().setRight(100);
    page.getTexts().get(1).getBounds().setRight(80);
    List<TextVisitor.LetterRow> rows = new TextVisitor(page).getLetterRows();

    assertEquals(2, rows.size(), "a c skips b, R = H has no letters");
    List<TextVisitor.Letter> first = rows.get(0).letters();
    assertEquals(
        "abc",
        first.stream().map(letter -> String.valueOf(letter.letter())).reduce("", String::concat));
    assertTrue(first.get(0).x() < first.get(1).x() && first.get(1).x() < first.get(2).x());
    assertTrue(first.get(2).x() > 90 && first.get(2).x() < 100, "c ends the text");
    assertEquals(
        "gh",
        rows.get(1).letters().stream()
            .map(letter -> String.valueOf(letter.letter()))
            .reduce("", String::concat));
  }

  /** A ring table gives one value per lettered ring and line; a line that does not is no table. */
  @Test
  public void readsRingTables() {
    CDPage page =
        pageWithTexts(
            "   A ring        B ring\r20a: 3-triazolyl     3,4-diOMe\r20c: 4-triazolyl     2,3-diOMe",
            "   A ring        B ring\r21a: 3-triazolyl");
    page.getTexts().forEach(text -> text.setBounds(new CDRectangle()));
    List<TextVisitor.RingTable> tables = new TextVisitor(page).getRingTables();

    assertEquals(1, tables.size(), "a line with one value for two rings is no table");
    assertEquals(List.of("A", "B"), tables.get(0).rings());
    assertEquals(
        List.of(
            new TextVisitor.RingRow("20a", List.of("3-triazolyl", "3,4-diOMe")),
            new TextVisitor.RingRow("20c", List.of("4-triazolyl", "2,3-diOMe"))),
        tables.get(0).rows());
  }

  /** A repeat count in a numbered or lettered line is the label n, with R or on its own. */
  @Test
  public void readsRepeatCountsOfLegendLines() {
    List<CorrelatedGroup> withR =
        new TextVisitor(pageWithTexts("26a: n = 1, R = H\r26g: n = 2, R = F"))
            .getBlocks()
            .get(0)
            .correlatedGroups();
    assertEquals(
        List.of(Map.of("R", "H", "n", "1"), Map.of("R", "F", "n", "2")), withR.get(0).tuples());
    List<CorrelatedGroup> alone =
        new TextVisitor(pageWithTexts("32a: n = 6\r32b: n = 4"))
            .getBlocks()
            .get(0)
            .correlatedGroups();
    assertEquals(List.of(Map.of("n", "6"), Map.of("n", "4")), alone.get(0).tuples());
    assertTrue(
        new TextVisitor(pageWithTexts("yield, n = 3")).getBlocks().isEmpty(),
        "an n outside a legend line is no entry");
  }

  /** A line naming one compound by number and letter ("64a: R1 = ...") counts its letter. */
  @Test
  public void numberedEntriesCountTheirLetters() {
    assertEquals(
        "ab",
        new TextVisitor(pageWithTexts("64a: R1 = H\r64b: R1 = Me"))
            .getBlocks()
            .get(0)
            .entryLetters());
    assertEquals(
        "",
        new TextVisitor(pageWithTexts("40a–m: R1 = H\r41a–e: R1 = OMe"))
            .getBlocks()
            .get(0)
            .entryLetters(),
        "a range names no single entry");
  }

  /**
   * A text that is a compound number alone is the caption of a drawn structure ("43", "42a–l"); an
   * atom's label is part of its drawing, whatever it reads.
   */
  @Test
  public void readsCaptionsButNotAtomLabels() {
    CDPage page = pageWithTexts("43", "42a–l", "43a: R = H");
    for (CDText text : page.getTexts()) {
      text.setBounds(new CDRectangle());
    }
    CDText label = page.getTexts().get(0);
    CDAtom atom = new CDAtom();
    CDText atomLabel = new CDText();
    atomLabel.setText(label.getText());
    atomLabel.setBounds(new CDRectangle());
    atom.setText(atomLabel);
    CDFragment fragment = new CDFragment();
    fragment.addAtom(atom);
    page.addFragment(fragment);

    assertEquals(
        List.of("43", "42"),
        new TextVisitor(page).getCaptions().stream().map(TextVisitor.Caption::number).toList());
  }

  /**
   * A ring formula with room for more substituents than its own takes the ring substituents written
   * before it: "4-OH, 3-MeOC6H3" is one 4-hydroxy-3-methoxyphenyl (36c of {@code
   * cheminf/bugs/markush/m50167957-9}). A full C6H4 ring takes none.
   */
  @Test
  public void ringFormulaKeepsItsSubstituentsTogether() {
    assertEquals(
        List.of("4-OHC6H4", "4-OH, 3-MeOC6H3"),
        new TextVisitor(pageWithTexts("36a: Ar = 4-OHC6H4\r36c: Ar = 4-OH, 3-MeOC6H3"))
            .getRgroups()
            .get("Ar"));
    assertEquals(
        List.of("2-OMe, 4-CHO-C6H3", "2,4-Me2C6H3"),
        new TextVisitor(pageWithTexts("R = 2-OMe,4-CHO-C6H3, 2,4-Me2C6H3")).getRgroups().get("R"));
    assertEquals(
        List.of("4-OH", "4-MeOC6H4"),
        new TextVisitor(pageWithTexts("R = 4-OH, 4-MeOC6H4")).getRgroups().get("R"));
  }
}
