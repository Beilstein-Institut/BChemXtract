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
package org.beilstein.chemxtract.cdx.reader;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDDocumentUtils;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CDXReaderTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testReadCDX() throws Exception {
    String fileName = "test_fixture.cdx";
    InputStream in = CDXReaderTest.class.getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    List<CDFragment> fragments = CDDocumentUtils.getListOfFragments(document);
    assertThat(fragments.size()).isEqualTo(14);

    List<CDReactionStep> steps = CDDocumentUtils.getListOfReactionSteps(document);
    assertThat(steps.size()).isEqualTo(4);

    Map<String, String> residues = CDDocumentUtils.getResidues(document);
    assertThat(residues.size()).isEqualTo(0);

    List<CDText> texts = CDDocumentUtils.getTexts(document);
    assertThat(texts.size()).isEqualTo(8);

    List<CDBracket> brackets = CDDocumentUtils.getBrackets(document);
    assertThat(brackets.size()).isEqualTo(3);
  }

  /**
   * Crossing bonds come back in the same order on every read. Position-variation candidates are
   * collected in this order, so an order that changes between reads changes which of two equivalent
   * ring atoms a substituent lands on, and with it the SMILES.
   */
  @Test
  public void crossingBondsKeepTheirOrderAcrossReads() throws Exception {
    List<String> first = crossingBondOrder();
    assertThat(first).as("the fixture must carry crossing bonds").isNotEmpty();
    for (int read = 2; read <= 5; read++) {
      assertThat(crossingBondOrder()).as("crossing-bond order on read %d", read).isEqualTo(first);
    }
  }

  private static List<String> crossingBondOrder() throws IOException {
    try (InputStream in =
        CDXReaderTest.class.getResourceAsStream("/cheminf/bugs/m8881674-i73.cdx")) {
      assertThat(in).as("fixture must be on the classpath").isNotNull();
      CDDocument document = CDXReader.readDocument(in);
      assertThat(document).as("document must parse").isNotNull();
      List<String> order = new ArrayList<>();
      for (CDFragment fragment : CDDocumentUtils.getListOfFragments(document)) {
        for (CDBond bond : fragment.getBonds()) {
          if (bond.getCrossingBonds() == null) {
            continue;
          }
          for (CDBond crossed : bond.getCrossingBonds()) {
            order.add(crossed.getBegin().getPosition2D() + "-" + crossed.getEnd().getPosition2D());
          }
        }
      }
      return order;
    }
  }
}
