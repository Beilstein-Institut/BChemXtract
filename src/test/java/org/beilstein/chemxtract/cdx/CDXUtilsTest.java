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
import org.beilstein.chemxtract.cdx.datatypes.CDArrowType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDoublePosition;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDBondReactionParticipation;
import org.beilstein.chemxtract.cdx.datatypes.CDBondTopology;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketType;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDExternalConnectionType;
import org.beilstein.chemxtract.cdx.datatypes.CDFillType;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDGraphicType;
import org.beilstein.chemxtract.cdx.datatypes.CDIsotopicAbundance;
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;
import org.beilstein.chemxtract.cdx.datatypes.CDLabelDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDLineType;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDOrbitalType;
import org.beilstein.chemxtract.cdx.datatypes.CDOvalType;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;
import org.beilstein.chemxtract.cdx.datatypes.CDRadical;
import org.beilstein.chemxtract.cdx.datatypes.CDReactionStereo;
import org.beilstein.chemxtract.cdx.datatypes.CDRectangleType;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.beilstein.chemxtract.cdx.datatypes.CDSymbolType;
import org.beilstein.chemxtract.cdx.datatypes.CDTranslation;
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

    property.setData(new byte[] {0x0 - 1});
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
    assertEquals(
        CDExternalConnectionType.Unspecified,
        CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(
        CDExternalConnectionType.Diamond, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(
        CDExternalConnectionType.Star, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(
        CDExternalConnectionType.PolymerBead,
        CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(
        CDExternalConnectionType.Wavy, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(
        CDExternalConnectionType.Residue, CDXUtils.readExternalConnectionTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(
        CDExternalConnectionType.Unspecified,
        CDXUtils.readExternalConnectionTypeProperty(property));
  }

  @Test
  public void testReadAbundanceProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDIsotopicAbundance.Unspecified, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDIsotopicAbundance.Any, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDIsotopicAbundance.Natural, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDIsotopicAbundance.Enriched, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDIsotopicAbundance.Deficient, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDIsotopicAbundance.Nonnatural, CDXUtils.readAbundanceProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDIsotopicAbundance.Unspecified, CDXUtils.readAbundanceProperty(property));
  }

  @Test
  public void testReadTranslationProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDTranslation.Equal, CDXUtils.readTranslationProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDTranslation.Broad, CDXUtils.readTranslationProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDTranslation.Narrow, CDXUtils.readTranslationProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDTranslation.Any, CDXUtils.readTranslationProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(null, CDXUtils.readTranslationProperty(property));
  }

  @Test
  public void testReadAtomCIPTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDAtomCIPType.Undetermined, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDAtomCIPType.None, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDAtomCIPType.R, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDAtomCIPType.S, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDAtomCIPType.PseudoR, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDAtomCIPType.PseudoS, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDAtomCIPType.Unspecified, CDXUtils.readAtomCIPTypeProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDAtomCIPType.Unspecified, CDXUtils.readAtomCIPTypeProperty(property));
  }

  @Test
  public void testReadAtomGeometryProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDAtomGeometry.Unknown, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDAtomGeometry.OneLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDAtomGeometry.Linear, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDAtomGeometry.Bent, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDAtomGeometry.TrigonalPlanar, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDAtomGeometry.TrigonalPyramidal, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDAtomGeometry.SquarePlanar, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDAtomGeometry.Tetrahedral, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDAtomGeometry.TrigonalBipyramidal, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x09});
    assertEquals(CDAtomGeometry.SquarePyramidal, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0a});
    assertEquals(CDAtomGeometry.FiveLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0b});
    assertEquals(CDAtomGeometry.Octahedral, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0c});
    assertEquals(CDAtomGeometry.SixLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0d});
    assertEquals(CDAtomGeometry.SevenLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0e});
    assertEquals(CDAtomGeometry.EightLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x0f});
    assertEquals(CDAtomGeometry.NineLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x10});
    assertEquals(CDAtomGeometry.TenLigand, CDXUtils.readAtomGeometryProperty(property));
    property.setData(new byte[] {0x11});
    assertEquals(CDAtomGeometry.Unknown, CDXUtils.readAtomGeometryProperty(property));
  }

  @Test
  public void testReadReactionStereoProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDReactionStereo.Unspecified, CDXUtils.readReactionStereoProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDReactionStereo.Inversion, CDXUtils.readReactionStereoProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDReactionStereo.Retention, CDXUtils.readReactionStereoProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDReactionStereo.Unspecified, CDXUtils.readReactionStereoProperty(property));
  }

  @Test
  public void testReadRadicalProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDRadical.None, CDXUtils.readRadicalProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDRadical.Singlet, CDXUtils.readRadicalProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDRadical.Doublet, CDXUtils.readRadicalProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDRadical.Triplet, CDXUtils.readRadicalProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDRadical.None, CDXUtils.readRadicalProperty(property));
  }

  @Test
  public void testReadLabelDisplayProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDLabelDisplay.Auto, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDLabelDisplay.Left, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDLabelDisplay.Center, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDLabelDisplay.Right, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDLabelDisplay.Above, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDLabelDisplay.Below, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDLabelDisplay.BestInitial, CDXUtils.readLabelDisplayProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDLabelDisplay.Auto, CDXUtils.readLabelDisplayProperty(property));
  }

  @Test
  public void testReadNodeTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(2);

    property.setData(new byte[] {0x00, 0x00});
    assertEquals(CDNodeType.Unspecified, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x01, 0x00});
    assertEquals(CDNodeType.Element, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x02, 0x00});
    assertEquals(CDNodeType.ElementList, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x03, 0x00});
    assertEquals(CDNodeType.ElementListNickname, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x04, 0x00});
    assertEquals(CDNodeType.Nickname, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x05, 0x00});
    assertEquals(CDNodeType.Fragment, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x06, 0x00});
    assertEquals(CDNodeType.Formula, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x07, 0x00});
    assertEquals(CDNodeType.GenericNickname, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x08, 0x00});
    assertEquals(CDNodeType.AnonymousAlternativeGroup, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x09, 0x00});
    assertEquals(CDNodeType.NamedAlternativeGroup, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x0a, 0x00});
    assertEquals(CDNodeType.MultiAttachment, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x0b, 0x00});
    assertEquals(CDNodeType.VariableAttachment, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x0c, 0x00});
    assertEquals(CDNodeType.ExternalConnectionPoint, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x0d, 0x00});
    assertEquals(CDNodeType.LinkNode, CDXUtils.readNodeTypeProperty(property));
    property.setData(new byte[] {0x0e, 0x00});
    assertEquals(CDNodeType.Unspecified, CDXUtils.readNodeTypeProperty(property));
  }

  @Test
  public void testReadBondReactionParticipationProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(
        CDBondReactionParticipation.Unspecified,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(
        CDBondReactionParticipation.ReactionCenter,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(
        CDBondReactionParticipation.MakeOrBreak,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(
        CDBondReactionParticipation.ChangeType,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(
        CDBondReactionParticipation.MakeAndChange,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(
        CDBondReactionParticipation.NotReactionCenter,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(
        CDBondReactionParticipation.NoChange,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(
        CDBondReactionParticipation.Unmapped,
        CDXUtils.readBondReactionParticipationProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(
        CDBondReactionParticipation.Unspecified,
        CDXUtils.readBondReactionParticipationProperty(property));
  }

  @Test
  public void testReadBondTopologyProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDBondTopology.Unspecified, CDXUtils.readBondTopologyProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDBondTopology.Ring, CDXUtils.readBondTopologyProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDBondTopology.Chain, CDXUtils.readBondTopologyProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDBondTopology.RingOrChain, CDXUtils.readBondTopologyProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDBondTopology.Unspecified, CDXUtils.readBondTopologyProperty(property));
  }

  @Test
  public void testReadBondDoublePositionProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(2);

    property.setData(new byte[] {0x00, 0x00});
    assertEquals(
        CDBondDoublePosition.AutoCenter, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x01, 0x00});
    assertEquals(CDBondDoublePosition.AutoRight, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x02, 0x00});
    assertEquals(CDBondDoublePosition.AutoLeft, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x00, 0x01});
    assertEquals(
        CDBondDoublePosition.UserCenter, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x01, 0x01});
    assertEquals(CDBondDoublePosition.UserRight, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x02, 0x01});
    assertEquals(CDBondDoublePosition.UserLeft, CDXUtils.readBondDoublePositionProperty(property));
    property.setData(new byte[] {0x03, 0x01});
    assertEquals(null, CDXUtils.readBondDoublePositionProperty(property));
  }

  @Test
  public void testReadBondDisplayProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDBondDisplay.Solid, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDBondDisplay.Dash, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDBondDisplay.Hash, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDBondDisplay.WedgedHashBegin, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDBondDisplay.WedgedHashEnd, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDBondDisplay.Bold, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDBondDisplay.WedgeBegin, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDBondDisplay.WedgeEnd, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDBondDisplay.Wavy, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x09});
    assertEquals(CDBondDisplay.HollowWedgeBegin, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0a});
    assertEquals(CDBondDisplay.HollowWedgeEnd, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0b});
    assertEquals(CDBondDisplay.WavyWedgeBegin, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0c});
    assertEquals(CDBondDisplay.WavyWedgeEnd, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0d});
    assertEquals(CDBondDisplay.Dot, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0e});
    assertEquals(CDBondDisplay.DashDot, CDXUtils.readBondDisplayProperty(property));
    property.setData(new byte[] {0x0f});
    assertEquals(CDBondDisplay.Solid, CDXUtils.readBondDisplayProperty(property));
  }

  @Test
  public void testReadBondOrdersProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(2);

    property.setData(new byte[] {0x01, 0x00});
    assertEquals(CDBondOrder.Single, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x02, 0x00});
    assertEquals(CDBondOrder.Double, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x04, 0x00});
    assertEquals(CDBondOrder.Triple, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x08, 0x00});
    assertEquals(CDBondOrder.Quadruple, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x10, 0x00});
    assertEquals(CDBondOrder.Quintuple, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x20, 0x00});
    assertEquals(CDBondOrder.Sextuple, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x40, 0x00});
    assertEquals(CDBondOrder.Half, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {(byte) 0x80, 0x00});
    assertEquals(CDBondOrder.OneHalf, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x01});
    assertEquals(CDBondOrder.TwoHalf, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x02});
    assertEquals(CDBondOrder.ThreeHalf, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x04});
    assertEquals(CDBondOrder.FourHalf, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x08});
    assertEquals(CDBondOrder.FiveHalf, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x10});
    assertEquals(CDBondOrder.Dative, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x20});
    assertEquals(CDBondOrder.Ionic, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x00, 0x40});
    assertEquals(CDBondOrder.Hydrogen, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {0x03, 0x00});
    assertEquals(CDBondOrder.SingleOrDouble, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {(byte) 0x81, 0x00});
    assertEquals(CDBondOrder.SingleOrAromatic, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {(byte) 0x82, 0x00});
    assertEquals(CDBondOrder.DoubleOrAromatic, CDXUtils.readBondOrdersProperty(property));
    property.setData(new byte[] {(byte) 0x84, 0x00});
    assertEquals(CDBondOrder.Single, CDXUtils.readBondOrdersProperty(property));
  }

  @Test
  public void testReadGraphicTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDGraphicType.Undefined, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDGraphicType.Line, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDGraphicType.Arc, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDGraphicType.Rectangle, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDGraphicType.Oval, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDGraphicType.Orbital, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDGraphicType.Bracket, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDGraphicType.Symbol, CDXUtils.readGraphicTypeProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDGraphicType.Undefined, CDXUtils.readGraphicTypeProperty(property));
  }

  @Test
  public void testReadLineTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);
    property.setData(new byte[] {0x00});
    CDLineType lt = CDXUtils.readLineTypeProperty(property);
    assertTrue(lt.isSolid());
    assertFalse(lt.isDashed());
    assertFalse(lt.isBold());
    assertFalse(lt.isWavy());

    property.setData(new byte[] {0x01});
    lt = CDXUtils.readLineTypeProperty(property);
    assertFalse(lt.isSolid());
    assertTrue(lt.isDashed());
    assertFalse(lt.isBold());
    assertFalse(lt.isWavy());

    property.setData(new byte[] {0x02});
    lt = CDXUtils.readLineTypeProperty(property);
    assertFalse(lt.isSolid());
    assertFalse(lt.isDashed());
    assertTrue(lt.isBold());
    assertFalse(lt.isWavy());

    property.setData(new byte[] {0x04});
    lt = CDXUtils.readLineTypeProperty(property);
    assertFalse(lt.isSolid());
    assertFalse(lt.isDashed());
    assertFalse(lt.isBold());
    assertTrue(lt.isWavy());

    property.setData(new byte[] {0x07});
    lt = CDXUtils.readLineTypeProperty(property);
    assertFalse(lt.isSolid());
    assertTrue(lt.isDashed());
    assertTrue(lt.isBold());
    assertTrue(lt.isWavy());
  }

  @Test
  public void testReadArrowTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDArrowType.NoHead, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDArrowType.HalfHead, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDArrowType.FullHead, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDArrowType.Resonance, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDArrowType.Equilibrium, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x10});
    assertEquals(CDArrowType.Hollow, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x20});
    assertEquals(CDArrowType.RetroSynthetic, CDXUtils.readArrowTypeProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDArrowType.NoHead, CDXUtils.readArrowTypeProperty(property));
  }

  @Test
  public void testReadRectangleTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);
    property.setData(new byte[] {0x0f});
    CDRectangleType rt = CDXUtils.readRectangleTypeProperty(property);
    assertTrue(rt.isRoundEdge());
    assertTrue(rt.isShadow());
    assertTrue(rt.isShaded());
    assertTrue(rt.isFilled());
    assertFalse(rt.isBold());
    assertFalse(rt.isDashed());
  }

  @Test
  public void testReadOvalTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);
    property.setData(new byte[] {0x0f});
    CDOvalType ot = CDXUtils.readOvalTypeProperty(property);
    assertTrue(ot.isCircle());
    assertTrue(ot.isShaded());
    assertTrue(ot.isFilled());
    assertTrue(ot.isDashed());
    assertFalse(ot.isBold());
    assertFalse(ot.isShadowed());
  }

  @Test
  public void testReadFillTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(2);

    property.setData(new byte[] {0x00, 0x00});
    assertEquals(CDFillType.Unspecified, CDXUtils.readFillTypeProperty(property));
    property.setData(new byte[] {0x01, 0x00});
    assertEquals(CDFillType.None, CDXUtils.readFillTypeProperty(property));
    property.setData(new byte[] {0x02, 0x00});
    assertEquals(CDFillType.Solid, CDXUtils.readFillTypeProperty(property));
    property.setData(new byte[] {0x03, 0x00});
    assertEquals(CDFillType.Shaded, CDXUtils.readFillTypeProperty(property));
    property.setData(new byte[] {0x04, 0x00});
    assertEquals(CDFillType.Faded, CDXUtils.readFillTypeProperty(property));
    property.setData(new byte[] {0x05, 0x00});
    assertEquals(CDFillType.Unspecified, CDXUtils.readFillTypeProperty(property));
  }

  @Test
  public void testReadOrbitalTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(2);

    property.setData(new byte[] {0x00, 0x00});
    assertEquals(CDOrbitalType.s, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x01, 0x00});
    assertEquals(CDOrbitalType.oval, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x02, 0x00});
    assertEquals(CDOrbitalType.lobe, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x03, 0x00});
    assertEquals(CDOrbitalType.p, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x04, 0x00});
    assertEquals(CDOrbitalType.hybridPlus, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x05, 0x00});
    assertEquals(CDOrbitalType.hybridMinus, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x06, 0x00});
    assertEquals(CDOrbitalType.dz2Plus, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x07, 0x00});
    assertEquals(CDOrbitalType.dz2Minus, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x08, 0x00});
    assertEquals(CDOrbitalType.dxy, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x00, 0x01});
    assertEquals(CDOrbitalType.sShaded, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x01, 0x01});
    assertEquals(CDOrbitalType.ovalShaded, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x02, 0x01});
    assertEquals(CDOrbitalType.lobeShaded, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x03, 0x01});
    assertEquals(CDOrbitalType.pShaded, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x00, 0x02});
    assertEquals(CDOrbitalType.sFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x01, 0x02});
    assertEquals(CDOrbitalType.ovalFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x02, 0x02});
    assertEquals(CDOrbitalType.lobeFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x03, 0x02});
    assertEquals(CDOrbitalType.pFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x04, 0x02});
    assertEquals(CDOrbitalType.hybridPlusFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x05, 0x02});
    assertEquals(CDOrbitalType.hybridMinusFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x06, 0x02});
    assertEquals(CDOrbitalType.dz2PlusFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x07, 0x02});
    assertEquals(CDOrbitalType.dz2MinusFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x08, 0x02});
    assertEquals(CDOrbitalType.dxyFilled, CDXUtils.readOrbitalTypeProperty(property));
    property.setData(new byte[] {0x09, 0x02});
    assertEquals(null, CDXUtils.readOrbitalTypeProperty(property));
  }

  @Test
  public void testReadBracketTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDBracketType.RoundPair, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDBracketType.SquarePair, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDBracketType.CurlyPair, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDBracketType.Square, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDBracketType.Curly, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDBracketType.Round, CDXUtils.readBracketTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(null, CDXUtils.readBracketTypeProperty(property));
  }

  @Test
  public void testReadSymbolTypeProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDSymbolType.LonePair, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDSymbolType.Electron, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDSymbolType.RadicalCation, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDSymbolType.RadicalAnion, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDSymbolType.CirclePlus, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDSymbolType.CircleMinus, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDSymbolType.Dagger, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDSymbolType.DoubleDagger, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDSymbolType.Plus, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x09});
    assertEquals(CDSymbolType.Minus, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x0a});
    assertEquals(CDSymbolType.Racemic, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x0b});
    assertEquals(CDSymbolType.Absolute, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x0c});
    assertEquals(CDSymbolType.Relative, CDXUtils.readSymbolTypeProperty(property));
    property.setData(new byte[] {0x0d});
    assertEquals(null, CDXUtils.readSymbolTypeProperty(property));
  }

  @Test
  public void testReadBracketUsageProperty() throws IOException {
    CDXProperty property = new CDXProperty();
    property.setLength(1);

    property.setData(new byte[] {0x00});
    assertEquals(CDBracketUsage.Unspecified, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x12});
    assertEquals(CDBracketUsage.Anypolymer, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x0d});
    assertEquals(CDBracketUsage.Component, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x06});
    assertEquals(CDBracketUsage.Copolymer, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x07});
    assertEquals(CDBracketUsage.CopolymerAlternating, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x09});
    assertEquals(CDBracketUsage.CopolymerBlock, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x08});
    assertEquals(CDBracketUsage.CopolymerRandom, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x11});
    assertEquals(CDBracketUsage.Generic, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x0b});
    assertEquals(CDBracketUsage.Graft, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x05});
    assertEquals(CDBracketUsage.Mer, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x0f});
    assertEquals(CDBracketUsage.MixtureOrdered, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x0e});
    assertEquals(CDBracketUsage.MixtureUnordered, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x0c});
    assertEquals(CDBracketUsage.Modification, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x04});
    assertEquals(CDBracketUsage.Monomer, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x10});
    assertEquals(CDBracketUsage.MultipleGroup, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x03});
    assertEquals(CDBracketUsage.SRU, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x01});
    assertEquals(CDBracketUsage.Unused1, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x02});
    assertEquals(CDBracketUsage.Unused2, CDXUtils.readBracketUsageProperty(property));
    property.setData(new byte[] {0x13});
    assertEquals(CDBracketUsage.Unspecified, CDXUtils.readBracketUsageProperty(property));
  }
}
