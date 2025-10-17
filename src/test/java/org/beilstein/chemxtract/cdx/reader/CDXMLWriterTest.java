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

  private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/target/test-output/cdx";
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
