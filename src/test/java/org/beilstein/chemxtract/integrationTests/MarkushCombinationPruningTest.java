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
package org.beilstein.chemxtract.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Issue #142: Markush expansion of this drawing took about 84 seconds, against one second with
 * Markush off.
 *
 * <p>The drawing holds four structures, but its product scaffold carries six R-group labels of six
 * or seven substituents each — 49,392 assignments — and its position-variation attachments expand
 * that scaffold into sixteen variants, so 792,065 assignments were built in all. All but 65 were
 * dropped again because one of their values did not resolve, after each had been cloned and
 * partially substituted.
 *
 * <p>Enumeration now walks the product depth-first and abandons a prefix as soon as one of its
 * values fails, which discards those subtrees whole. The substances extracted are unchanged; only
 * the work done to reach them differs.
 */
class MarkushCombinationPruningTest {

  /**
   * The substances this drawing currently yields. They are fewer than the drawing describes: every
   * substituent naming a fused-ring position (R2 = 5-OMe, R5 = 7-F, ...) fails to resolve, which is
   * issue #164, and the locant-prefixed aryls of R1 fail too, which is #165. Fixing either will
   * deliberately change these keys — this test pins current behaviour so that the pruning above can
   * be shown not to change it, not because eight is the right answer.
   */
  private static final List<String> EXPECTED_INCHI_KEYS =
      List.of(
          "IURYWTLWTJMFLI-GWHBCOKCSA-N",
          "LLSIEQLHMGACNA-UHFFFAOYSA-N",
          "NFHQGPKXWICMMI-XHIZWQFQSA-N",
          "PORVOXZIKMDGML-DXQCBLCSSA-N",
          "TZMAFVABXKVNAC-UHFFFAOYSA-N",
          "UQNXHNASBPBCCD-UHFFFAOYSA-N",
          "UROJGXRWYOYUJW-UHFFFAOYSA-N",
          "VMHVOKGEGWVGKW-UHFFFAOYSA-N");

  private List<BCXSubstance> xtract(String fixture) throws Exception {
    InputStream in =
        MarkushCombinationPruningTest.class.getResourceAsStream("/cheminf/bugs/" + fixture);
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    return xtractor.xtractUnique(document, new BCXSubstanceInfo(), true);
  }

  @Test
  void prunedEnumerationYieldsTheSameSubstances() throws Exception {
    List<BCXSubstance> substances = xtract("m31594334-i40.cdx");

    assertThat(substances)
        .as("pruning must not change which structures are extracted")
        .extracting(BCXSubstance::getInchiKey)
        .containsExactlyInAnyOrderElementsOf(EXPECTED_INCHI_KEYS);
  }

  /**
   * Guards the enumeration against regressing to a full cartesian product. The margin is wide on
   * purpose: the extraction takes about a second once pruned and took about 84 before, so any bound
   * between the two catches the regression without being sensitive to how loaded the machine
   * running it happens to be.
   */
  @Test
  void markushExpansionDoesNotEnumerateTheWholeProduct() {
    assertTimeoutPreemptively(
        Duration.ofSeconds(60),
        () ->
            assertThat(xtract("m31594334-i40.cdx"))
                .as("expansion must finish without building every assignment")
                .hasSize(EXPECTED_INCHI_KEYS.size()));
  }
}
