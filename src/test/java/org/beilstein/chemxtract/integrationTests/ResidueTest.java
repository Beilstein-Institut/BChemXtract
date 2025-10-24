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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class ResidueTest {

  @Test
  public void testResidues() throws IOException {
    String fileName = "complex_rgroups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info, true);
    for (BCXSubstance substance : substances) {
      System.out.println("extracted: " + substance.getMolecularFormula());
    }

  }

}
