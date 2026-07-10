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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.utils.ChemicalUtils;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

class AbbreviationLayoutIntegrationTest {

  // Baseline captured from the current extraction (see plan Task 3, Step 2). Layout must not change
  // it.
  private static final String BASELINE_INCHIKEY = "AFPHTEQTJZKQAQ-UHFFFAOYSA-N";

  @Test
  void expandedAbbreviationCoordinatesAreLaidOutWithoutChangingChemistry() throws Exception {
    InputStream in =
        AbbreviationLayoutIntegrationTest.class.getResourceAsStream(
            "/integrationTests/Sgroups_Abbreviations.cdx");
    assertNotNull(in, "fixture must be on the classpath");
    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document, "document must parse");

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
    assertFalse(substances.isEmpty(), "at least one substance must be extracted");

    BCXSubstance first = substances.get(0);

    // Chemistry is unchanged by coordinate layout.
    assertEquals(BASELINE_INCHIKEY, first.getInchiKey(), "InChIKey must be unchanged");

    // Coordinates are no longer collapsed.
    assertFalse(
        ChemicalUtils.hasDuplicateCoordinates(first.getAtomContainer()),
        "expanded abbreviation atoms must have distinct coordinates");

    // Extended SMILES now carries a coordinate block (CXSMILES uses the "|...|" suffix).
    assertTrue(
        first.getExtendedSmiles().contains("|"),
        "extended SMILES should include a coordinate block");
  }
}
