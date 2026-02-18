package org.beilstein.chemxtract.cdx.reader;

import static org.junit.Assert.*;

import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.junit.Before;
import org.junit.Test;

public class CDXUtilsTests {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testConvertFontType() {
    CDFontFace ff = new CDFontFace();
    ff.setBold(true);
    ff.setFormula(true);
    ff.setItalic(true);
    ff.setOutline(true);
    ff.setPlain(true);
    ff.setShadow(true);
    ff.setSubscript(true);
    ff.setSuperscript(true);
    ff.setUnderline(true);
    
    int c = CDXUtils.convertFontType(ff);
    assertEquals(84, c);
  }
  
  @Test
  public void testConvertIntToFontFace() {
    CDFontFace ff = CDXUtils.convertIntToFontFace(127);
    assertTrue(ff.isBold());
    assertTrue(ff.isItalic());
    assertTrue(ff.isUnderline());
    assertTrue(ff.isOutline());
    assertTrue(ff.isShadow());
  }
  
  @Test
  public void testConvertIntToSplineType() {
    CDSplineType st = CDXUtils.convertIntToSplineType(1023);
    assertTrue(st.isClosed());
    assertTrue(st.isDashed());
    assertTrue(st.isBold());
    assertTrue(st.isArrowAtEnd());
    assertTrue(st.isArrowAtStart());
    assertTrue(st.isHalfArrowAtEnd());
    assertTrue(st.isHalfArrowAtStart());
    assertTrue(st.isFilled());
    assertTrue(st.isShaded());
    assertTrue(st.isDoubled());
  }

}
