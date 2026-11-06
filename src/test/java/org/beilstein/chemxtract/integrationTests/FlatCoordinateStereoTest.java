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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * ChemDraw stores a flat 3D position (z = -0.0005 for every atom) next to the 2D drawings of this
 * document. The 3D stereo factory read a configuration from that planar noise instead of the
 * wedges: three wedged spiro-oxindoles came out with their SMILES inverted against ChemDraw's R/S
 * labels and against their own InChI, and an unwedged one gained a centre the drawing does not
 * define.
 *
 * <p>A fifth drawing stores 3D positions for its indanone half only; every other atom sits at z =
 * 0. Those coordinates contradict the wedges at the indanone spiro centre and invent a
 * configuration for the unwedged CH next to it.
 */
class FlatCoordinateStereoTest {

  private static List<BCXSubstance> substances;

  @BeforeAll
  static void extract() throws Exception {
    InputStream in =
        FlatCoordinateStereoTest.class.getResourceAsStream("/cheminf/bugs/m43214419-1.cdx");
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();
    substances =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .xtractUnique(document, new BCXSubstanceInfo(), false);
  }

  @Test
  void wedgedCentresKeepTheirConfiguration() {
    assertThat(substances)
        .as("the three wedged spiro-oxindoles keep ChemDraw's R/S labels")
        .extracting(BCXSubstance::getInchiKey)
        .contains(
            "HBHCBIIRYVIJGE-DZGCQCFKSA-N",
            "RVOLLKGLJIUGLG-ZDUSSCGKSA-N",
            "PNYGHERLKSFRMH-LBPRGKRZSA-N");
  }

  @Test
  void smilesDescribesTheSameStereoAsTheInchi() throws Exception {
    SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    for (BCXSubstance substance : substances) {
      String fromSmiles =
          factory.getInChIGenerator(parser.parseSmiles(substance.getSmiles())).getInchiKey();
      assertThat(fromSmiles)
          .as("the InChIKey of the SMILES %s", substance.getSmiles())
          .isEqualTo(substance.getInchiKey());
    }
  }

  @Test
  void unwedgedCentreStaysUndefined() {
    assertThat(substances)
        .as("the spiro centre drawn without a wedge has no configuration")
        .extracting(BCXSubstance::getInchiKey)
        .contains("SKLDVYDFQCXARY-UHFFFAOYSA-N")
        .doesNotContain("SKLDVYDFQCXARY-FQEVSTJZSA-N");
  }

  @Test
  void wedgesWinOverPartialThreeDimensionalCoordinates() {
    assertThat(substances)
        .as("both wedged spiro centres are S and the unwedged CH stays undefined")
        .extracting(BCXSubstance::getInchiKey)
        .contains("UTUGTUIULXVUOF-TWAOCZIPSA-N")
        .doesNotContain("UTUGTUIULXVUOF-YMSRPXOMSA-N");
  }
}
