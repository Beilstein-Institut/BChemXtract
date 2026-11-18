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
package org.beilstein.chemxtract.cdx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CDBond}. */
public class CDBondTest {

  /**
   * The copy keeps the crossing bonds in the order the original holds them. Position-variation
   * candidates are collected in that order, so a reordered copy changes which of two equivalent
   * ring atoms a substituent lands on.
   */
  @Test
  public void copyKeepsCrossingBondOrder() {
    Set<CDBond> crossing = new LinkedHashSet<>();
    for (int i = 0; i < 32; i++) {
      crossing.add(new CDBond());
    }
    CDBond bond = new CDBond();
    bond.setCrossingBonds(crossing);

    assertThat(new CDBond(bond).getCrossingBonds())
        .as("copied crossing bonds keep the original order")
        .containsExactlyElementsOf(crossing);
  }
}
