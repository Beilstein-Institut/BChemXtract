package org.beilstein.chemxtract.cdx.reader;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.CDDocumentUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class CDXReaderTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testReadCDX() throws Exception {
    String fileName = "test_fixture.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
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
