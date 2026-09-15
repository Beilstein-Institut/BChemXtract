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
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Issue #144: four of the six C6H4F labels in this drawing are stored as {@code Unspecified} nodes
 * without a chemical warning, while the other two are {@code Fragment} nodes. {@code BondVisitor}
 * recognised only the latter as abbreviations and dropped the bond attaching an {@code Unspecified}
 * one to the skeleton, which left the pseudoatom unbonded and made abbreviation resubstitution
 * throw {@link java.util.NoSuchElementException}.
 */
class UnwantedAbbreviationAttachmentTest {

  @Test
  void unwantedAbbreviationNodesKeepTheirAttachmentBondRegardlessOfNodeType() throws Exception {
    InputStream in =
        UnwantedAbbreviationAttachmentTest.class.getResourceAsStream(
            "/cheminf/bugs/m6217264-i2.cdx");
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    // Extraction used to abort with NoSuchElementException on the unbonded abbreviation.
    List<BCXSubstance> substances = xtractor.xtractUnique(document, new BCXSubstanceInfo(), true);

    assertThat(substances)
        .as("the tri(4-fluorophenyl) phosphole must be extracted with its abbreviations expanded")
        .extracting(BCXSubstance::getInchiKey)
        .contains("KXGUQTUFVTWUKG-UHFFFAOYSA-N");
  }

  /**
   * Same drawing, second defect: its four phosphorus centres carry the atom-numbering labels P1 to
   * P4, which ChemDraw read as the formulas P&#8321; to P&#8324; and expanded into nested fragments
   * of 1, 2, 3 and 4 phosphorus atoms. That turned four drawn phosphorus atoms into ten, joined by
   * P-P bonds the drawing never showed.
   */
  @Test
  void numberedAtomLabelsAreNotExpandedAsFormulas() throws Exception {
    InputStream in =
        UnwantedAbbreviationAttachmentTest.class.getResourceAsStream(
            "/cheminf/bugs/m6217264-i2.cdx");
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtractUnique(document, new BCXSubstanceInfo(), true);

    BCXSubstance cage =
        substances.stream()
            .filter(s -> "CXHYVNABHAFARV-WEGGDKQKSA-N".equals(s.getInchiKey()))
            .findFirst()
            .orElse(null);
    assertThat(cage).as("the tetraphosphorus cage must be extracted").isNotNull();
    assertThat(countPhosphorus(cage))
        .as("P1..P4 label the four drawn phosphorus atoms, they are not P1+P2+P3+P4 = 10")
        .isEqualTo(4);
  }

  private static long countPhosphorus(BCXSubstance substance) {
    long phosphorus = 0;
    for (IAtom atom : substance.getAtomContainer().atoms()) {
      if ("P".equals(atom.getSymbol())) {
        phosphorus++;
      }
    }
    return phosphorus;
  }
}
