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
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * The drawn wedges decide a centre, as ChemDraw's own CIP labels do. One centre per structure came
 * out inverted: ChemDraw's stored label, RDKit, ChemScanner and Open Babel agree on the
 * configuration expected here, which is not taken from BChemXtract.
 */
class DrawnStereoPrecedenceTest {

  /**
   * At a ring fusion drawn in perspective, two bold bonds meet a real wedge. A bold bond was read
   * as a second wedge from the fusion carbon and inverted it; next to a real wedge it is now a
   * perspective edge.
   */
  @Test
  void boldPerspectiveBondsDoNotInvertAWedgedCentre() throws Exception {
    List<String> keys = inchiKeys("/cheminf/bugs/m25422595-i8.cdx");

    assertThat(keys)
        .as("each structure has the configuration ChemDraw labels")
        .contains(
            "BWMFRQKICHXLSH-FIRPSQKQSA-N",
            "GMCKLNZBDZBXCT-SJJKWINZSA-N",
            "HXDKFEOELJSYQO-WYIVGMSFSA-N",
            "LDIOLFBUTYGFNW-SJJKWINZSA-N",
            "ONEJFAKXFPBWBF-YNFYBLCGSA-N",
            "SHKUUMLOLTXIJX-YMMGPHOHSA-N",
            "YSUPXSXURADMDO-MOIDVZEBSA-N",
            "ZNXDVCGREHMNKV-XVEOUWLGSA-N");
    assertThat(keys)
        .as("no structure keeps the fusion carbon inverted by a bold bond")
        .doesNotContain(
            "BWMFRQKICHXLSH-SGJBMYIASA-N",
            "GMCKLNZBDZBXCT-NRWYEAIWSA-N",
            "HXDKFEOELJSYQO-GPWZLMPVSA-N",
            "LDIOLFBUTYGFNW-NRWYEAIWSA-N",
            "ONEJFAKXFPBWBF-CPDINLDASA-N",
            "SHKUUMLOLTXIJX-BETSAUPYSA-N",
            "YSUPXSXURADMDO-AUNJKCNLSA-N",
            "ZNXDVCGREHMNKV-GDVRDXLVSA-N");
  }

  private List<String> inchiKeys(String resource) throws Exception {
    InputStream in = DrawnStereoPrecedenceTest.class.getResourceAsStream(resource);
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();
    return new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
        .xtractUnique(document, new BCXSubstanceInfo(), false).stream()
            .map(BCXSubstance::getInchiKey)
            .toList();
  }
}
