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

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Two C100H54 structures, each joining two polycyclic cages (37 rings). In the para isomer the cage
 * centres carry only ChemDraw R/S labels, so {@code StereoHandler} ranks their ligands by CIP
 * priority. CDK's legacy {@code CIPTool} walked every path through the cages and never returned;
 * the centres labeller gives up after a node budget, leaving those centres unassigned.
 */
class CipLabelCageTest {

  private static List<BCXSubstance> xtract(String fixture) throws Exception {
    InputStream in = CipLabelCageTest.class.getResourceAsStream("/cheminf/bugs/" + fixture);
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();
    return new SubstanceXtractor().xtractUnique(document, new BCXSubstanceInfo(), false);
  }

  @Test
  @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void cageWithLabelOnlyCentresExtracts() throws Exception {
    assertThat(xtract("m20970122-5.cdx"))
        .extracting(BCXSubstance::getInchiKey)
        .as("para isomer without stereo, meta isomer with its wedged centres")
        .containsExactlyInAnyOrder("VFVOCIPZIXHOIP-UHFFFAOYSA-N", "XPMUPRKPPAUJEY-YLMDTDHTSA-N");
  }

  /** Fullerene-silsesquioxane hybrids with azide, triazole and copper-triazole linkers. */
  @Test
  @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void fullereneSilsesquioxaneCagesExtract() throws Exception {
    assertThat(xtract("m17773228-i16.cdx"))
        .extracting(BCXSubstance::getInchiKey)
        .as("three cage products, tripropargylamine and copper(I) iodide")
        .containsExactlyInAnyOrder(
            "UGFQJILFBOOEJM-UHFFFAOYSA-N",
            "BXCRLDPNHKHLIH-UHFFFAOYSA-N",
            "AFZQPSDAKNWVSX-UHFFFAOYSA-N",
            "ZHOBJWVNWMQMLF-UHFFFAOYSA-N",
            "LSXDOTMGLUJQCM-UHFFFAOYSA-M");
  }
}
