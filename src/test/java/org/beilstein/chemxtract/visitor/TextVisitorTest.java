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
import org.beilstein.chemxtract.cdx.CDPage;
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
  public void independentListNotTreatedAsCorrelated() {
    RGroupDefinitionBlock block = new TextVisitor(pageWithTexts("R = H, CH3")).getBlocks().get(0);
    assertTrue(block.correlatedGroups().isEmpty(), "a single-label list is not a table");
    assertEquals(List.of("H", "CH3"), block.definitions().get("R"));
  }

  @Test
  public void resolvesChainedEqualityToAllLabels() {
    // "R1 = R2 = H" means both R1 and R2 are H.
    Map<String, List<String>> r = new TextVisitor(pageWithTexts("R1 = R2 = H")).getRgroups();
    assertEquals(List.of("H"), r.get("R1"), "R1 must inherit the chained value");
    assertEquals(List.of("H"), r.get("R2"), "R2 must be resolved");
  }
}
