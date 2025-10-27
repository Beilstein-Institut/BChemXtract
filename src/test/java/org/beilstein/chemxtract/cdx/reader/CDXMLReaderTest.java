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
import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDTLCLane;
import org.beilstein.chemxtract.cdx.CDTLCPlate;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDDocumentUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CDXMLReaderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testReadCDXML() throws IOException {
    String fileName = "test_fixture.cdxml";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXMLReader.readDocument(in);
    assertNotNull(document);

    List<CDFragment> fragments = CDDocumentUtils.getListOfFragments(document);
    assertThat(fragments.size()).isEqualTo(14);

    List<CDReactionStep> steps = CDDocumentUtils.getListOfReactionSteps(document);
    assertThat(steps.size()).isEqualTo(4);

    Map<String,String> residues = CDDocumentUtils.getResidues(document);
    assertThat(residues.size()).isEqualTo(0);

    List<CDText> texts = CDDocumentUtils.getTexts(document);
    assertThat(texts.size()).isEqualTo(8);

    List<CDBracket> brackets = CDDocumentUtils.getBrackets(document);
    assertThat(brackets.size()).isEqualTo(3);
  }

}
