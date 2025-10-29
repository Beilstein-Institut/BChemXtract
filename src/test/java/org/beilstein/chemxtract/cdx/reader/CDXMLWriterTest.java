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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CDXMLWriterTest {

  private static final String OUTPUT_DIR =
      System.getProperty("user.dir") + "/target/test-output/cdx";
  private File outputDir;

  @Before
  public void setUp() throws Exception {
    outputDir = new File(OUTPUT_DIR);
    outputDir.mkdirs();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testWriteCDXML() throws IOException {
    String fileName = "test_fixture.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    File out = new File(outputDir, "test_fixture.cdxml");
    FileOutputStream fos = new FileOutputStream(out);
    CDXMLWriter.writeDocument(document, fos);
    fos.flush();
    fos.close();
  }
}
