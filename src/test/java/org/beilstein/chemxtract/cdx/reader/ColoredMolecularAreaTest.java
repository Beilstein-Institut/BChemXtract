package org.beilstein.chemxtract.cdx.reader;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDColoredMolecularArea;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDGroup;
import org.beilstein.chemxtract.cdx.CDPage;
import org.junit.Before;
import org.junit.Test;

public class ColoredMolecularAreaTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testColoredMolecularArea() throws Exception {
    
    String fileName = "colored_molecular_area.cdxml";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXMLReader.readDocument(in);
    assertNotNull(document);
    CDPage page = document.getPages().get(0);
    assertNotNull(page);
    CDFragment frag = page.getFragments().get(0);
    assertNotNull(frag);
    List<CDColoredMolecularArea> areas = frag.getColoredMolecularAreas();
    assertEquals(4, areas.size());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CDXMLWriter.writeDocument(document, baos);
    byte[] buf = baos.toByteArray();
    
    // roundtrip
    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    document = CDXMLReader.readDocument(bais);
    assertNotNull(document);
    page = document.getPages().get(0);
    assertNotNull(page);
    frag = page.getFragments().get(0);
    assertNotNull(frag);
    areas = frag.getColoredMolecularAreas();
    assertEquals(4, areas.size());

  }

}
