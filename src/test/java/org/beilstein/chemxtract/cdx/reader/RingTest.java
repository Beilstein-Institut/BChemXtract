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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDGroup;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDSpline;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RingTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testRingsCDXML() throws IOException {
    String fileName = "testcase-ringe-simple.cdxml";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXMLReader.readDocument(in);
    assertNotNull(document);
    CDPage page = document.getPages().get(0);
    assertNotNull(page);
    CDGroup group = page.getGroups().get(0);
    assertNotNull(group);
    CDFragment frag = group.getFragments().get(0);
    assertNotNull(frag);
    CDSpline curve = frag.getCurves().get(0);
    assertNotNull(curve);
    assertTrue(curve.isClosed());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CDXMLWriter.writeDocument(document, baos);
    byte[] buf = baos.toByteArray();

    // roundtrip
    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    document = CDXMLReader.readDocument(bais);
    assertNotNull(document);
    page = document.getPages().get(0);
    assertNotNull(page);
    group = page.getGroups().get(0);
    assertNotNull(group);
    frag = group.getFragments().get(0);
    assertNotNull(frag);
    curve = frag.getCurves().get(0);
    assertNotNull(curve);
    assertTrue(curve.isClosed());
  }

  @Test
  public void testRingsCDX() throws IOException {
    String fileName = "testcase-ringe-simple.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);
    CDPage page = document.getPages().get(0);
    assertNotNull(page);
    CDGroup group = page.getGroups().get(0);
    assertNotNull(group);
    CDFragment frag = group.getFragments().get(0);
    assertNotNull(frag);
    CDSpline curve = frag.getCurves().get(0);
    assertNotNull(curve);
    assertTrue(curve.isClosed());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CDXMLWriter.writeDocument(document, baos);
    byte[] buf = baos.toByteArray();

    // roundtrip
    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    document = CDXMLReader.readDocument(bais);
    assertNotNull(document);
    page = document.getPages().get(0);
    assertNotNull(page);
    group = page.getGroups().get(0);
    assertNotNull(group);
    frag = group.getFragments().get(0);
    assertNotNull(frag);
    curve = frag.getCurves().get(0);
    assertNotNull(curve);
    assertTrue(curve.isClosed());
  }
}
