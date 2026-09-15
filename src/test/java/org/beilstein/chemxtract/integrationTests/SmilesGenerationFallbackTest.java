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
 * Issue #145: absolute and isomeric SMILES need CDK's InChI-based canonical numbering, which throws
 * {@code ArrayIndexOutOfBoundsException} out of {@code InChINumbersTools} and Beam on some
 * organometallic structures. The exception is a {@link RuntimeException}, so it escaped the
 * per-fragment {@code CDKException} handling in {@code SubstanceXtractor.xtract} and aborted the
 * whole document.
 *
 * <p>{@code ChemicalUtils.createSmiles} now reports it as a {@code CDKException} like every other
 * generator failure, and the extractor falls back to the canonical flavour, which uses graph
 * invariants rather than InChI numbering.
 *
 * <p>The underlying CDK defect is fixed in 2.13, so on that version these documents already
 * extract; the fallback is what keeps them working on CDK 2.12 and guards the path against any
 * future numbering failure.
 */
class SmilesGenerationFallbackTest {

  private List<BCXSubstance> xtract(String fixture) throws Exception {
    InputStream in =
        SmilesGenerationFallbackTest.class.getResourceAsStream("/cheminf/bugs/" + fixture);
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    return xtractor.xtractUnique(document, new BCXSubstanceInfo(), true);
  }

  @Test
  void rheniumCarbonylComplexesAllExtract() throws Exception {
    List<BCXSubstance> substances = xtract("m12743226-i45.cdx");

    assertThat(substances).as("every structure on the page must survive").hasSize(18);
    assertThat(substances)
        .as("the decacarbonyl dirhenium must be among them")
        .extracting(BCXSubstance::getInchiKey)
        .contains("ICPLACDRIJNXEM-UHFFFAOYSA-N");
    assertThat(substances).allSatisfy(s -> assertThat(s.getSmiles()).isNotNull());
    assertThat(substances).allSatisfy(s -> assertThat(s.getInchiKey()).isNotNull());
  }

  @Test
  void largePeptideExtracts() throws Exception {
    List<BCXSubstance> substances = xtract("m20441376-1.cdx");

    assertThat(substances).hasSize(1);
    BCXSubstance peptide = substances.get(0);
    assertThat(peptide.getInchiKey()).isEqualTo("WIBFOGMDBPAPMD-XALDNWMSSA-N");
    assertThat(peptide.getAtomContainer().getAtomCount()).isEqualTo(364);
    assertThat(peptide.getSmiles()).as("a SMILES must be produced").isNotNull();
  }
}
