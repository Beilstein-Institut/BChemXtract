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
package org.beilstein.chemxtract.cdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import junit.framework.TestCase;

import org.beilstein.chemxtract.cdx.datatypes.CDBondCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDExternalConnectionType;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.beilstein.chemxtract.cdx.datatypes.CDUnsaturation;
import org.beilstein.chemxtract.cdx.reader.CDXProperty;
import org.beilstein.chemxtract.cdx.reader.CDXUtils;
import org.junit.Test;

public class CDXUtilsTest extends TestCase {

  public void testFixedPoint() {
    byte[] bytes = new byte[] {0x66, (byte) 0xA6, 0x4C, 0x00};
    assertEquals(76.65f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] {(byte) 0xF9, 0x1E, 0x54, 0x00};
    assertEquals(84.12f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] {0x77, (byte) 0xB7, 0x74, 0x00};
    assertEquals(116.72f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] {0x0C, (byte) 0xFA, 0x7B, 0x00};
    assertEquals(123.98f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] {0x00, 0x00, (byte) 0x48, 0x00};
    assertEquals(72f, CDXUtils.readFixedPoint(bytes, 0), 0.1f);

    bytes = new byte[] {0x00, 0x00, (byte) 0x90, 0x00};
    assertEquals(144f, CDXUtils.readFixedPoint(bytes, 0), 0.1f);
  }

  public void testFloat64() {
    byte[] bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    assertEquals(0.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x24, 0x40};
    assertEquals(10.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE0, 0x6F, 0x40};
    assertEquals(255.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x70, 0x40};
    assertEquals(256.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x40, (byte) 0x8F, 0x40};
    assertEquals(1000.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x6A, (byte) 0xF8, 0x40};
    assertEquals(100000.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE0, 0x3F};
    assertEquals(0.5, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] {(byte) 0xFC, (byte) 0xA9, (byte) 0xF1, (byte) 0xD2, 0x4D, 0x62, 0x50, 0x3F};
    assertEquals(0.001, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes =
        new byte[] {
          (byte) 0xCD,
          (byte) 0xCC,
          (byte) 0xCC,
          (byte) 0xCC,
          (byte) 0xCC,
          (byte) 0xDC,
          0x5E,
          (byte) 0xC0
        };
    assertEquals(-123.45, CDXUtils.readFloat64(bytes, 0), 0.001);
  }

  public void testInt16() {
    byte[] bytes = new byte[] {0x00, 0x00};
    assertEquals(0, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] {0x0A, 0x00};
    assertEquals(10, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, 0x00};
    assertEquals(255, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] {0x00, 0x01};
    assertEquals(256, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xE8, 0x03};
    assertEquals(1000, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, (byte) 0xFF};
    assertEquals(-1, CDXUtils.readInt16(bytes, 0));
  }

  public void testInt32() {
    byte[] bytes = new byte[] {0x00, 0x00, 0x00, 0x00};
    assertEquals(0, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {0x0A, 0x00, 0x00, 0x00};
    assertEquals(10, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, 0x00, 0x00, 0x00};
    assertEquals(255, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {0x00, 0x01, 0x00, 0x00};
    assertEquals(256, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xE8, 0x03, 0x00, 0x00};
    assertEquals(1000, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xA0, (byte) 0x86, 0x01, 0x00};
    assertEquals(100000, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F};
    assertEquals(2147483647, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    assertEquals(-1L, CDXUtils.readInt32(bytes, 0));
  }

  public void testInt8() {
    byte[] bytes = new byte[] {0x00};
    assertEquals(0, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {0x0A};
    assertEquals(10, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {0x7F};
    assertEquals(127, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {(byte) 0xFD};
    assertEquals(-3, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {(byte) 0xFE};
    assertEquals(-2, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {(byte) 0xFF};
    assertEquals(-1, CDXUtils.readInt8(bytes, 0));
  }

  public void testUInt16() {
    byte[] bytes = new byte[] {0x00, 0x00};
    assertEquals(0, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] {0x0A, 0x00};
    assertEquals(10, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, 0x00};
    assertEquals(255, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] {0x00, 0x01};
    assertEquals(256, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xE8, 0x03};
    assertEquals(1000, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, (byte) 0xFF};
    assertEquals(65535, CDXUtils.readUInt16(bytes, 0));
  }

  public void testUInt32() {
    byte[] bytes = new byte[] {0x00, 0x00, 0x00, 0x00};
    assertEquals(0, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {0x0A, 0x00, 0x00, 0x00};
    assertEquals(10, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, 0x00, 0x00, 0x00};
    assertEquals(255, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {0x00, 0x01, 0x00, 0x00};
    assertEquals(256, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xE8, 0x03, 0x00, 0x00};
    assertEquals(1000, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xA0, (byte) 0x86, 0x01, 0x00};
    assertEquals(100000, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    assertEquals(4294967295L, CDXUtils.readUInt32(bytes, 0));
  }

  public void testUInt8() {
    byte[] bytes = new byte[] {0x00};
    assertEquals(0, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] {0x0A};
    assertEquals(10, CDXUtils.readUInt8(bytes, 0));

    bytes = new byte[] {0x7F};
    assertEquals(127, CDXUtils.readUInt8(bytes, 0));

    bytes = new byte[] {(byte) 0xFF};
    assertEquals(255, CDXUtils.readUInt8(bytes, 0));
  }

  public void testColorEquals() {
    CDColor expected = new CDColor(1f, 0.0f, 1f);
    CDColor actual = new CDColor(0.99998474f, 0.0f, 0.99998474f);

    assertEquals(expected.hashCode(), actual.hashCode());
    assertTrue(expected.equals(actual));

    actual = new CDColor(0.99998474f, 0.0f, 0.998f);

    assertFalse(expected.hashCode() == actual.hashCode());
    assertFalse(expected.equals(actual));
  }

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

  @Test
  public void testConvertCurveTypeToInt() {
    CDSplineType st = new CDSplineType();
    st.setArrowAtEnd(true);
    st.setArrowAtStart(true);
    st.setBold(true);
    st.setClosed(true);
    st.setDashed(true);
    st.setDoubled(true);
    st.setFilled(true);
    st.setShaded(true);
    st.setHalfArrowAtEnd(true);
    st.setHalfArrowAtStart(true);

    assertEquals(1023, CDXUtils.convertCurveTypeToInt(st));
    st.setPlain(true);
    assertTrue(st.isPlain());
    assertEquals(0, CDXUtils.convertCurveTypeToInt(st));
  }
  
  @Test
  public void testReadBondCIPTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDBondCIPType.Undetermined, CDXUtils.readBondCIPTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDBondCIPType.None, CDXUtils.readBondCIPTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDBondCIPType.E, CDXUtils.readBondCIPTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDBondCIPType.Z, CDXUtils.readBondCIPTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDBondCIPType.Undetermined, CDXUtils.readBondCIPTypeProperty(property));
  }
  
  @Test
  public void testReadTextJustificationProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x0-1});
    assertEquals(CDJustification.Right, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x00});
    assertEquals(CDJustification.Left, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDJustification.Center, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDJustification.Full, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDJustification.Above, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDJustification.Below, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDJustification.Auto, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDJustification.BestInitial, CDXUtils.readTextJustificationProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDJustification.Auto, CDXUtils.readTextJustificationProperty(property));
  }
  
  @Test
  public void testReadPageDefinitionProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDPageDefinition.Undefined, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDPageDefinition.Center, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDPageDefinition.TL4, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDPageDefinition.IDTerm, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDPageDefinition.FlushLeft, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDPageDefinition.FlushRight, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDPageDefinition.Reaction1, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDPageDefinition.Reaction2, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDPageDefinition.MulticolumnTL4, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x09});
    assertEquals(CDPageDefinition.MulticolumnNonTL4, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x0a});
    assertEquals(CDPageDefinition.UserDefined, CDXUtils.readPageDefinitionProperty(property));
    property.setData(new byte[] {0x0b});
    assertEquals(CDPageDefinition.Undefined, CDXUtils.readPageDefinitionProperty(property));

  }
  
  @Test
  public void testReadDrawingSpaceTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDDrawingSpaceType.Pages, CDXUtils.readDrawingSpaceTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDDrawingSpaceType.Poster, CDXUtils.readDrawingSpaceTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDDrawingSpaceType.Pages, CDXUtils.readDrawingSpaceTypeProperty(property));

  }
  
  @Test
  public void testReadUnsaturationProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);
    
    property.setData(new byte[] {0x00});
    assertEquals(CDUnsaturation.Unspecified, CDXUtils.readUnsaturationProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDUnsaturation.MustBeAbsent, CDXUtils.readUnsaturationProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDUnsaturation.MustBePresent, CDXUtils.readUnsaturationProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDUnsaturation.Unspecified, CDXUtils.readUnsaturationProperty(property));
  }
  
  @Test
  public void testReadExternalConnectionTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);
    
    property.setData(new byte[] {0x00});
    assertEquals(CDExternalConnectionType.Unspecified, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDExternalConnectionType.Diamond, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDExternalConnectionType.Star, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDExternalConnectionType.PolymerBead, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDExternalConnectionType.Wavy, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDExternalConnectionType.Residue, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDExternalConnectionType.Unspecified, CDXUtils.readExternalConnectionTypeProperty(property));
  }
}
