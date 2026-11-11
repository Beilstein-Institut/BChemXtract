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
package org.beilstein.chemxtract.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.beilstein.chemxtract.visitor.TextVisitor.RingRow;
import org.junit.jupiter.api.Test;

class RingLetterTableTest {

  private static final List<RingRow> ROWS =
      List.of(
          new RingRow("20a", List.of("3-triazolyl", "3,4-diOMe")),
          new RingRow("20b", List.of("4-triazolyl", "4-OMe")),
          new RingRow("20c", List.of("3-imidazolyl", "3,4,5-triOMe")));

  /** A column naming one group, or the drawn group's label, places the drawn group. */
  @Test
  void columnNamingTheDrawnGroupFits() {
    assertTrue(RingLetterTable.namesOneGroup(ROWS.subList(0, 2), 0, ""), "triazolyl in each row");
    assertTrue(RingLetterTable.namesOneGroup(ROWS, 1, "OMe"), "OMe is the drawn label");
  }

  /**
   * A column naming two groups ({@code 3-triazolyl}, {@code 3-imidazolyl}) would put the drawn
   * triazolyl where the legend names an imidazolyl; the table does not fit.
   */
  @Test
  void columnNamingTwoGroupsDoesNotFit() {
    assertFalse(RingLetterTable.namesOneGroup(ROWS, 0, ""), "triazolyl and imidazolyl");
  }
}
