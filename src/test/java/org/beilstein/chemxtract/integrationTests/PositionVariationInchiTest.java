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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * These documents draw substituents across a ring (position variation) whose R-groups have no
 * definition on the page. The enumerated placements were emitted with their R atoms still in place,
 * so they carried SMILES but no InChI, and {@code xtractUnique}, which deduplicates by InChI,
 * collapsed all of them into one arbitrary survivor. A structure that cannot be given an InChI is
 * now skipped like any other unresolved R-group structure.
 */
class PositionVariationInchiTest {

  @ParameterizedTest
  @ValueSource(strings = {"m50848776-i4.cdx", "m50848776-i6.cdx", "m50966958-i14.cdx"})
  void everySubstanceHasAnInchi(String file) throws Exception {
    InputStream in =
        PositionVariationInchiTest.class.getResourceAsStream("/cheminf/bugs/InChI_null/" + file);
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();

    List<BCXSubstance> substances =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .xtract(document, new BCXSubstanceInfo(), false);

    assertThat(substances)
        .as("the page's resolved structures must still be extracted")
        .isNotEmpty();
    assertThat(substances)
        .as("no substance may lack an InChI")
        .allSatisfy(s -> assertThat(s.getInchi()).isNotBlank());
  }
}
