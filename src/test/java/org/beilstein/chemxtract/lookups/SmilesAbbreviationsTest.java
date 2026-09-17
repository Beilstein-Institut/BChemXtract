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
package org.beilstein.chemxtract.lookups;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Locant-prefixed aryl abbreviations, and the two spellings chemists write them in (issue #165).
 */
class SmilesAbbreviationsTest {

  private final SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());

  /**
   * How far round the ring the substituent sits from the attachment point: 1 is ortho, 2 meta, 3
   * para. Checks the entry means what its name says, which a hand-written SMILES easily gets wrong.
   */
  private int ringSeparation(String abbreviation) throws Exception {
    IAtomContainer molecule = parser.parseSmiles(SmilesAbbreviations.get(abbreviation));
    List<Integer> ring = new ArrayList<>();
    for (IAtomContainer candidate : Cycles.mcb(molecule).toRingSet().atomContainers()) {
      if (candidate.getAtomCount() == 6) {
        ring.clear();
        for (IAtom atom : candidate.atoms()) {
          ring.add(molecule.indexOf(atom));
        }
      }
    }
    assertThat(ring).as("%s must contain a benzene ring", abbreviation).hasSize(6);

    int attachment = -1;
    int substituted = -1;
    for (int index : ring) {
      for (IAtom neighbour : molecule.getConnectedAtomsList(molecule.getAtom(index))) {
        if (ring.contains(molecule.indexOf(neighbour))) {
          continue;
        }
        if (neighbour instanceof IPseudoAtom) {
          attachment = index;
        } else {
          substituted = index;
        }
      }
    }
    assertThat(attachment).as("%s must have an attachment point", abbreviation).isNotNegative();
    assertThat(substituted).as("%s must carry a substituent", abbreviation).isNotNegative();

    int separation = Math.abs(ring.indexOf(attachment) - ring.indexOf(substituted));
    return Math.min(separation, 6 - separation);
  }

  @ParameterizedTest(name = "{0} is {1} bonds from the attachment")
  @CsvSource({
    "2-MeC6H4, 1",
    "3-OMeC6H4, 2",
    "3-FC6H4, 2",
    "4-FC6H4, 3",
    "4-t-BuC6H4, 3",
    "4-ClC6H4, 3",
    "p-MeC6H4, 3",
  })
  void putsTheSubstituentWhereTheLocantSaysItIs(String abbreviation, int expected)
      throws Exception {
    assertThat(ringSeparation(abbreviation))
        .as("%s names position %s of its own ring", abbreviation, expected + 1)
        .isEqualTo(expected);
  }

  @Test
  void readsANumberedLocantAndItsOrthoMetaParaSpellingAsTheSameGroup() throws Exception {
    // p-MeC6H4 is in the table and 4-MeC6H4 is not; both must resolve, to the same group.
    assertThat(SmilesAbbreviations.contains("4-MeC6H4")).isTrue();
    assertThat(SmilesAbbreviations.get("4-MeC6H4"))
        .as("the two spellings name one group")
        .isEqualTo(SmilesAbbreviations.get("p-MeC6H4"));

    // and the other way round: 4-ClC6H4 is in the table, p-ClC6H4 is not.
    assertThat(SmilesAbbreviations.contains("p-ClC6H4")).isTrue();
    assertThat(SmilesAbbreviations.get("p-ClC6H4")).isEqualTo(SmilesAbbreviations.get("4-ClC6H4"));
  }

  @Test
  void rewritesTheLocantWhereverItAppearsAndLeavesOtherPrefixesAlone() throws Exception {
    // The rewrite is not restricted to benzenes: o-py is a spelling no one writes, but it reads
    // the way it was meant and finds the table's 2-py rather than missing.
    assertThat(SmilesAbbreviations.get("o-py")).isEqualTo(SmilesAbbreviations.get("2-py"));

    // A leading letter that is not a locant must not be rewritten.
    assertThat(SmilesAbbreviations.get("t-Bu")).as("t- is tert-, not a locant").isNotNull();
    assertThat(SmilesAbbreviations.get("t-Bu")).isEqualTo(SmilesAbbreviations.get("tBu"));

    assertThat(SmilesAbbreviations.contains("Ph")).isTrue();
    assertThat(SmilesAbbreviations.contains("definitely-not-an-abbreviation")).isFalse();
  }
}
