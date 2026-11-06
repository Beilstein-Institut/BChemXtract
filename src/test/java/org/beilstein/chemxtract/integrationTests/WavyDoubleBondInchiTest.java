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
 * Each alkene on this page has a wavy bond at its double bond, ChemDraw's mark for "either E or Z".
 * The SMILES left the configuration open, but the InChI read one from the drawn geometry, so the
 * two outputs described different molecules. The InChI now leaves it open too.
 */
class WavyDoubleBondInchiTest {

  @Test
  void wavyBondsLeaveTheDoubleBondConfigurationOutOfTheInchi() throws Exception {
    InputStream in =
        WavyDoubleBondInchiTest.class.getResourceAsStream("/cheminf/bugs/m30172992-3.cdx");
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();

    List<String> keys =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .xtractUnique(document, new BCXSubstanceInfo(), false).stream()
                .map(BCXSubstance::getInchiKey)
                .toList();

    assertThat(keys)
        .as("the wavy double bonds carry no configuration")
        .containsExactlyInAnyOrder(
            "FPVXOLZRNGEMNO-UHFFFAOYSA-N",
            "WWQYRFHPXLNMFN-UHFFFAOYSA-N",
            "POKSPSWIUWLBHS-UHFFFAOYSA-N",
            "JCDZLXCQTJNIPA-UHFFFAOYSA-N",
            "YVLINXSOIKTHIB-UHFFFAOYSA-N",
            "TXJVEJLZQFHTSS-UHFFFAOYSA-N");
  }
}
