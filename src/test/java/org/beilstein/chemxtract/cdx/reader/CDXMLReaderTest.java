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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDChemicalProperty;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDDocumentUtils;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CDXMLReaderTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testReadCDXML() throws IOException {
    String fileName = "test_fixture.cdxml";
    InputStream in = CDXMLReaderTest.class.getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXMLReader.readDocument(in);
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

  @Test
  public void readsAndWritesChemicalPropertyWithSeveralTypes() throws Exception {
    // ChemDraw writes a space-separated list when one property box shows several values.
    String cdxml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CDXML><page id=\"1\">"
            + "<chemicalproperty id=\"2\" ChemicalPropertyType=\"2 3 6\"/>"
            + "</page></CDXML>";

    CDDocument document =
        CDXMLReader.readDocument(new ByteArrayInputStream(cdxml.getBytes(StandardCharsets.UTF_8)));
    assertNotNull(document);
    assertThat(singleChemicalProperty(document).getTypes()).containsExactly(2, 3, 6);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CDXMLWriter.writeDocument(document, out);
    CDDocument reread = CDXMLReader.readDocument(new ByteArrayInputStream(out.toByteArray()));
    assertThat(singleChemicalProperty(reread).getTypes())
        .as("types survive a CDXML round trip")
        .containsExactly(2, 3, 6);
  }

  private static CDChemicalProperty singleChemicalProperty(CDDocument document) {
    List<CDChemicalProperty> properties = document.getPages().get(0).getChemicalProperties();
    assertThat(properties).hasSize(1);
    return properties.get(0);
  }
}
