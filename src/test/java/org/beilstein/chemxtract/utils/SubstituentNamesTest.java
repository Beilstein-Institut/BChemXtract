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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SubstituentNames}. */
public class SubstituentNamesTest {

  /** A prefix names its group; a multiplier must match the number of positions. */
  @Test
  public void prefixNamesItsGroup() {
    assertEquals("*OC", SubstituentNames.group("methoxy", 1));
    assertEquals("Cl", SubstituentNames.group("chloro", 1));
    assertEquals("C", SubstituentNames.group("methyl", 1));
    assertEquals("*OC", SubstituentNames.group("dimethoxy", 2));
    assertEquals("Br", SubstituentNames.group("tribromo", 3));
    assertNull(SubstituentNames.group("dimethoxy", 1), "di on one position");
    assertNull(SubstituentNames.group("methoxy", 2), "no multiplier on two positions");
    assertNull(SubstituentNames.group("ethoxy", 1), "not a known prefix");
  }

  /** A named phenyl or benzyl group carries each prefix at its positions. */
  @Test
  public void namedRingCarriesItsPrefixes() {
    assertEquals(
        new SubstituentNames.Ring(true, Map.of(4, "Cl")), SubstituentNames.ring("4-chlorobenzyl"));
    assertEquals(
        new SubstituentNames.Ring(true, Map.of(3, "Cl", 4, "Cl")),
        SubstituentNames.ring("3,4-dichlorobenzyl"));
    assertEquals(
        new SubstituentNames.Ring(false, Map.of(3, "C", 4, "Cl")),
        SubstituentNames.ring("4-chloro-3-methylphenyl"));
    assertEquals(
        new SubstituentNames.Ring(false, Map.of(3, "*OC", 4, "*OC", 5, "*OC")),
        SubstituentNames.ring("3,4,5-trimethoxyphenyl"));
    assertEquals(new SubstituentNames.Ring(false, Map.of()), SubstituentNames.ring("phenyl"));
  }

  /** Names that do not add up are not read rather than guessed. */
  @Test
  public void inconsistentNamesAreNotRead() {
    assertNull(SubstituentNames.ring("2,4-chlorophenyl"), "two locants, no di");
    assertNull(SubstituentNames.ring("7-chlorophenyl"), "no position 7 on a phenyl");
    assertNull(SubstituentNames.ring("4-chloro-4-methylphenyl"), "one position named twice");
    assertNull(SubstituentNames.ring("4-ethoxyphenyl"), "not a known prefix");
    assertNull(SubstituentNames.ring("naphthyl"), "not a phenyl or benzyl");
  }
}
