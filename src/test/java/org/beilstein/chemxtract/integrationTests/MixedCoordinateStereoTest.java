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
 * Issue #146: {@code StereoHandler.selectFactory} decided between the 2D and the 3D {@link
 * org.openscience.cdk.stereo.StereoElementFactory} from the first atom alone. This document has a
 * fragment where only 13 of 28 atoms carry a Z position, the first of them among them, so the 3D
 * factory was chosen and threw a {@link NullPointerException} out of {@code
 * createExtendedTetrahedral} on the first atom without 3D coordinates, aborting the whole document.
 *
 * <p>The factory is now picked only when every atom has 3D coordinates; every atom placed by {@code
 * AtomConverter} has a 2D position, so mixed fragments use the 2D factory and their allene stereo
 * is still perceived.
 */
class MixedCoordinateStereoTest {

  @Test
  void documentWithPartial3DCoordinatesExtracts() throws Exception {
    InputStream in =
        MixedCoordinateStereoTest.class.getResourceAsStream("/cheminf/bugs/m22693636-i38.cdx");
    assertThat(in).as("fixture must be on the classpath").isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as("document must parse").isNotNull();

    List<BCXSubstance> substances =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .xtractUnique(document, new BCXSubstanceInfo(), true);

    // Six, not the four this expected when it was written: issue #165 made the locant-prefixed
    // aryls resolve, and this page's 4-BrC6H4 groups now graft instead of leaving their structures
    // with an unresolved R-group. The three keys below are what this test is actually about.
    assertThat(substances).as("every structure on the page must survive").hasSize(6);
    assertThat(substances)
        .as("the allene stereochemistry of the mixed-coordinate fragments must be perceived")
        .extracting(BCXSubstance::getInchiKey)
        .contains(
            "REPJKDCMLXUTEI-RBSBEOHCSA-N",
            "GIODLUBQGIBAIW-FKEHTHLJSA-N",
            "VFBGGYOYGOJEQA-YMGMXPECSA-N");
  }
}
