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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class MarkushTest {

  @Test
  public void testResidues() throws IOException {
    String fileName = "complex_rgroups.cdx";
    InputStream in = MarkushTest.class.getResourceAsStream("/integrationTests/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info, true);
    for (BCXSubstance substance : substances) {
      IO.println("extracted: " + substance.getMolecularFormula());
    }
  }

  @Test
  public void testMarkush_Ar_X_Y() throws IOException {
    String fileName = "Markush_Ar_X_Y.cdx";
    InputStream in = MarkushTest.class.getResourceAsStream("/integrationTests/markush/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance()).setUnrestrictedMarkush(true);
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info, true);
    assertEquals(9, substances.size());
  }

  @Test
  public void testMarkush_R_R1() throws IOException, CDKException {
    String fileName = "Markush_R_R1.cdx";
    InputStream in = MarkushTest.class.getResourceAsStream("/integrationTests/markush/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance()).setUnrestrictedMarkush(true);
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info, true);
    assertEquals(14, substances.size());
  }

  /**
   * The product scaffolds carry two R-groups that each list several substituents ({@code Y} and
   * {@code Ar}; {@code R} and {@code R1}), so by default their cartesian product is not expanded.
   * The reactant scaffolds carry one varying R-group each and are expanded either way.
   */
  @ParameterizedTest
  @CsvSource({"Markush_Ar_X_Y.cdx, 5", "Markush_R_R1.cdx, 6"})
  public void twoVaryingRGroupsAreNotExpanded(String fileName, int restricted) throws IOException {
    List<BCXSubstance> unrestrictedSubstances = xtract(fileName, true);
    List<BCXSubstance> restrictedSubstances = xtract(fileName, false);

    assertEquals(restricted, restrictedSubstances.size(), "only the one-R scaffolds expand");
    assertTrue(
        unrestrictedSubstances.stream()
            .map(BCXSubstance::getInchiKey)
            .toList()
            .containsAll(restrictedSubstances.stream().map(BCXSubstance::getInchiKey).toList()),
        "restricting drops structures, it never adds any");
  }

  private static List<BCXSubstance> xtract(String fileName, boolean unrestricted)
      throws IOException {
    InputStream in = MarkushTest.class.getResourceAsStream("/integrationTests/markush/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    SubstanceXtractor xtractor =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .setUnrestrictedMarkush(unrestricted);
    return xtractor.xtractUnique(document, new BCXSubstanceInfo(), true);
  }

  /**
   * A legend giving aryl substituents by ring position (3b–3o, R = H, p-OMe, ..., o-Br) expands the
   * aryl scaffold it describes. It is not applied to the R on sulfur in the reaction scheme above
   * it, where only its R = H entry would graft (mantis 11158, 22-9-i2).
   */
  @Test
  public void positionalLegendIsNotAppliedToAnOffRingRGroup() throws IOException {
    List<BCXSubstance> substances = xtract("Markush_positional_off_ring_R.cdx", false);
    List<String> keys = substances.stream().map(BCXSubstance::getInchiKey).toList();

    assertEquals(
        14, substances.stream().filter(BCXSubstance::isMarkush).count(), "3b–3o expand the aryl");
    assertThat(keys)
        .as("the scheme's R on sulfur is not replaced by H")
        .doesNotContain("AAUKAELUJMWJIL-UHFFFAOYSA-N", "RZQOAUROAHKPRB-UHFFFAOYSA-N");
    assertEquals(19, substances.size(), "3b–3s and MeOH");
  }
}
