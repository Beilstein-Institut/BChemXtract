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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAltGroup;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDGroup;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDSettings;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadPositionType;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadType;
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
import org.beilstein.chemxtract.cdx.datatypes.CDConstraintType;
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDExternalConnectionType;
import org.beilstein.chemxtract.cdx.datatypes.CDFillType;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDGeometryType;
import org.beilstein.chemxtract.cdx.datatypes.CDGraphicType;
import org.beilstein.chemxtract.cdx.datatypes.CDIsotopicAbundance;
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;
import org.beilstein.chemxtract.cdx.datatypes.CDLabelDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDLineType;
import org.beilstein.chemxtract.cdx.datatypes.CDNoGoType;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDObjectTagType;
import org.beilstein.chemxtract.cdx.datatypes.CDOrbitalType;
import org.beilstein.chemxtract.cdx.datatypes.CDOvalType;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerFlipType;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerRepeatPattern;
import org.beilstein.chemxtract.cdx.datatypes.CDPositioningType;
import org.beilstein.chemxtract.cdx.datatypes.CDRadical;
import org.beilstein.chemxtract.cdx.datatypes.CDReactionStereo;
import org.beilstein.chemxtract.cdx.datatypes.CDRectangleType;
import org.beilstein.chemxtract.cdx.datatypes.CDRingBondCount;
import org.beilstein.chemxtract.cdx.datatypes.CDSideType;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumClass;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumXType;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumYType;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.cdx.datatypes.CDSymbolType;
import org.beilstein.chemxtract.cdx.datatypes.CDTranslation;
import org.beilstein.chemxtract.cdx.datatypes.CDUnsaturation;
import org.beilstein.chemxtract.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a set of static helper methods to do binary conversions and convert CDX
 * constant values to Java enums.
 */
public class CDXUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CDXUtils.class);

  public static boolean isCDX(byte[] bytes) {
    return IOUtils.startsWithBytes(bytes, CDXConstants.getCdxSignature());
  }

  /**
   * The main entry for reading a binary CDX document.
   *
   * @param bytes the raw CDX document bytes
   * @param position single-element cursor holding the current read offset into {@code bytes}
   * @return the root {@link CDXObject} parsed from the document
   * @throws IOException If header is not recognized.
   */
  public static CDXObject readCDXDocument(byte[] bytes, int[] position) throws IOException {
    // read header string
    for (byte element : CDXConstants.getCdxSignature()) {
      if (bytes[position[0]++] != element) {
        throw new IOException("Header not recognized");
      }
    }

    // read reserved bytes for backward compatibility
    position[0] += 4;

    // read reserved bytes
    position[0] += 10;
    // position += 16;

    int tag = readUInt16(bytes, position[0]);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read root tag={} position={}",
          Integer.toHexString(tag),
          Integer.toHexString(position[0]));
    }
    position[0] += 2;

    return readCDXObject(tag, bytes, position);
  }

  private static CDXObject readCDXObject(int rootTag, byte[] bytes, int[] position)
      throws IOException {
    // read object id
    int id = readInt32(bytes, position[0]);
    position[0] += 4;

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read object with tag 0x{} and  id {}(0x{}) at {}(0x{})",
          Integer.toHexString(rootTag),
          id,
          Integer.toHexString(id),
          position[0] - 6,
          Integer.toHexString(position[0] - 6));
    }

    CDXObject object = new CDXObject();
    object.setTag(rootTag);
    object.setId(id);
    object.setPosition(position[0] - 6);

    // read content
    while (position[0] < bytes.length) {
      int tag = readUInt16(bytes, position[0]);
      position[0] += 2;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "read tag=0x{} at {}(0x{})",
            Integer.toHexString(tag),
            position[0],
            Integer.toHexString(position[0]));
      }
      if (tag == CDXConstants.CDXProp_EndObject) {
        break;
      } else if (tag >= CDXConstants.CDXTag_Object) {
        CDXObject object2 = readCDXObject(tag, bytes, position);
        object2.setTag(tag);
        object.addObject(object2);
      } else {
        CDXProperty property = readCDXProperty(tag, bytes, position);
        object.addProperty(property);
      }
    }
    return object;
  }

  private static CDXProperty readCDXProperty(int tag, byte[] bytes, int[] position)
      throws IOException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read property with tag 0x{} at {}(0x{})",
          Integer.toHexString(tag),
          position[0] - 2,
          Integer.toHexString(position[0] - 2));
    }

    CDXProperty property = new CDXProperty();
    property.setTag(tag);
    property.setPosition(position[0] - 2);

    // read property length
    int length = readUInt16(bytes, position[0]);
    position[0] += 2;
    if (length == 0xFFFF) {
      length = readInt32(bytes, position[0]);
      position[0] += 4;
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("property length={}", length);
    }
    property.setLength(length);

    if (position[0] + length >= bytes.length) {
      throw new IOException(
          "Property size doesn't fit into the remaining data at " + getPositionAsString(property));
    }

    // read data
    byte[] data = new byte[length];
    System.arraycopy(bytes, position[0], data, 0, length);
    position[0] += length;
    property.setData(data);

    return property;
  }

  public static int readUInt8(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff;
  }

  public static int readInt8(byte[] bytes, int offset) {
    return bytes[offset + 0];
  }

  public static short readInt16(byte[] bytes, int offset) {
    short a = (short) (bytes[offset + 0] & (short) 0xff);
    short b = (short) (bytes[offset + 1] << 8);

    return (short) (a | b);
  }

  public static int readUInt16(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff | (bytes[offset + 1] & 0xff) << 8;
  }

  public static int readInt32(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff
        | (bytes[offset + 1] & 0xff) << 8
        | (bytes[offset + 2] & 0xff) << 16
        | (bytes[offset + 3] & 0xff) << 24;
  }

  public static long readUInt32(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0x00000000000000ffL
        | (bytes[offset + 1] & 0x00000000000000ffL) << 8
        | (bytes[offset + 2] & 0x00000000000000ffL) << 16
        | (bytes[offset + 3] & 0x00000000000000ffL) << 24;
  }

  public static long readInt64(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0x00000000000000ffL
        | (bytes[offset + 1] & 0x00000000000000ffL) << 8
        | (bytes[offset + 2] & 0x00000000000000ffL) << 16
        | (bytes[offset + 3] & 0x00000000000000ffL) << 24
        | (bytes[offset + 4] & 0x00000000000000ffL) << 32
        | (bytes[offset + 5] & 0x00000000000000ffL) << 40
        | (bytes[offset + 6] & 0x00000000000000ffL) << 48
        | (bytes[offset + 7] & 0x00000000000000ffL) << 56;
  }

  public static double readFloat64(byte[] bytes, int offset) {
    return Double.longBitsToDouble(readInt64(bytes, offset));
  }

  public static float readFixedPoint(byte[] bytes, int offset) {
    return readInt32(bytes, offset) / 65536f;
  }

  public static CDFontFace convertIntToFontFace(int type) {
    CDFontFace fontType = new CDFontFace();
    if ((type & CDXConstants.CDXFontFace_Bold) != 0) {
      fontType.setBold(true);
    }
    if ((type & CDXConstants.CDXFontFace_Italic) != 0) {
      fontType.setItalic(true);
    }
    if ((type & CDXConstants.CDXFontFace_Underline) != 0) {
      fontType.setUnderline(true);
    }
    if ((type & CDXConstants.CDXFontFace_Outline) != 0) {
      fontType.setOutline(true);
    }
    if ((type & CDXConstants.CDXFontFace_Shadow) != 0) {
      fontType.setShadow(true);
    }

    // special handling for formula
    if ((type & CDXConstants.CDXFontFace_Formula) == CDXConstants.CDXFontFace_Formula) {
      fontType.setFormula(true);
    } else if ((type & CDXConstants.CDXFontFace_Subscript) != 0) {
      fontType.setSubscript(true);
    } else if ((type & CDXConstants.CDXFontFace_Superscript) != 0) {
      fontType.setSuperscript(true);
    }
    return fontType;
  }

  public static int convertFontType(CDFontFace fontType) {
    int value = 0;
    if (fontType.isBold()) {
      value |= CDXConstants.CDXFontFace_Bold;
    }
    if (fontType.isItalic()) {
      value |= CDXConstants.CDXFontFace_Italic;
    }
    if (fontType.isUnderline()) {
      value |= CDXConstants.CDXFontFace_Underline;
    }
    if (fontType.isOutline()) {
      value |= CDXConstants.CDXFontFace_Outline;
    }
    if (fontType.isShadow()) {
      value |= CDXConstants.CDXFontFace_Shadow;
    }

    // special handling for formula
    if (fontType.isFormula()) {
      value |= CDXConstants.CDXFontFace_Formula;
    } else if (fontType.isSubscript()) {
      value |= CDXConstants.CDXFontFace_Subscript;
    } else if (fontType.isSuperscript()) {
      value |= CDXConstants.CDXFontFace_Superscript;
    }
    return value;
  }

  public static CDSplineType convertIntToSplineType(int value) {
    CDSplineType curveType = new CDSplineType();
    if ((value & CDXConstants.CDXCurveType_Closed) != 0) {
      curveType.setClosed(true);
    }
    if ((value & CDXConstants.CDXCurveType_Dashed) != 0) {
      curveType.setDashed(true);
    }
    if ((value & CDXConstants.CDXCurveType_Bold) != 0) {
      curveType.setBold(true);
    }
    if ((value & CDXConstants.CDXCurveType_ArrowAtEnd) != 0) {
      curveType.setArrowAtEnd(true);
    }
    if ((value & CDXConstants.CDXCurveType_ArrowAtStart) != 0) {
      curveType.setArrowAtStart(true);
    }
    if ((value & CDXConstants.CDXCurveType_HalfArrowAtEnd) != 0) {
      curveType.setHalfArrowAtEnd(true);
    }
    if ((value & CDXConstants.CDXCurveType_HalfArrowAtStart) != 0) {
      curveType.setHalfArrowAtStart(true);
    }
    if ((value & CDXConstants.CDXCurveType_Filled) != 0) {
      curveType.setFilled(true);
    }
    if ((value & CDXConstants.CDXCurveType_Shaded) != 0) {
      curveType.setShaded(true);
    }
    if ((value & CDXConstants.CDXCurveType_Doubled) != 0) {
      curveType.setDoubled(true);
    }
    return curveType;
  }

  public static int convertCurveTypeToInt(CDSplineType curveType) {
    int value = 0;
    if (curveType.isClosed()) {
      value |= CDXConstants.CDXCurveType_Closed;
    }
    if (curveType.isDashed()) {
      value |= CDXConstants.CDXCurveType_Dashed;
    }
    if (curveType.isBold()) {
      value |= CDXConstants.CDXCurveType_Bold;
    }
    if (curveType.isArrowAtEnd()) {
      value |= CDXConstants.CDXCurveType_ArrowAtEnd;
    }
    if (curveType.isArrowAtStart()) {
      value |= CDXConstants.CDXCurveType_ArrowAtStart;
    }
    if (curveType.isHalfArrowAtEnd()) {
      value |= CDXConstants.CDXCurveType_HalfArrowAtEnd;
    }
    if (curveType.isHalfArrowAtStart()) {
      value |= CDXConstants.CDXCurveType_HalfArrowAtStart;
    }
    if (curveType.isFilled()) {
      value |= CDXConstants.CDXCurveType_Filled;
    }
    if (curveType.isShaded()) {
      value |= CDXConstants.CDXCurveType_Shaded;
    }
    if (curveType.isDoubled()) {
      value |= CDXConstants.CDXCurveType_Doubled;
    }
    return value;
  }

  public static CDBondCIPType readBondCIPTypeProperty(CDXProperty property) throws IOException {
    int type = property.getDataAsUInt8();
    switch (type) {
      case CDXConstants.CDXCIPBond_Undetermined:
        return CDBondCIPType.Undetermined;
      case CDXConstants.CDXCIPBond_None:
        return CDBondCIPType.None;
      case CDXConstants.CDXCIPBond_E:
        return CDBondCIPType.E;
      case CDXConstants.CDXCIPBond_Z:
        return CDBondCIPType.Z;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond CIP type 0x"
            + Integer.toHexString(type)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondCIPType.Undetermined;
  }

  public static CDJustification readTextJustificationProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsInt8();
    switch (value) {
      case CDXConstants.CDXTextJustification_Right:
        return CDJustification.Right;
      case CDXConstants.CDXTextJustification_Left:
        return CDJustification.Left;
      case CDXConstants.CDXTextJustification_Center:
        return CDJustification.Center;
      case CDXConstants.CDXTextJustification_Full:
        return CDJustification.Full;
      case CDXConstants.CDXTextJustification_Above:
        return CDJustification.Above;
      case CDXConstants.CDXTextJustification_Below:
        return CDJustification.Below;
      case CDXConstants.CDXTextJustification_Auto:
        return CDJustification.Auto;
      case CDXConstants.CDXTextJustification_BestInitial:
        return CDJustification.BestInitial;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Text justification 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDJustification.Auto;
  }

  public static CDPageDefinition readPageDefinitionProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXPageDefinition_Undefined:
        return CDPageDefinition.Undefined;
      case CDXConstants.CDXPageDefinition_Center:
        return CDPageDefinition.Center;
      case CDXConstants.CDXPageDefinition_TL4:
        return CDPageDefinition.TL4;
      case CDXConstants.CDXPageDefinition_IDTerm:
        return CDPageDefinition.IDTerm;
      case CDXConstants.CDXPageDefinition_FlushLeft:
        return CDPageDefinition.FlushLeft;
      case CDXConstants.CDXPageDefinition_FlushRight:
        return CDPageDefinition.FlushRight;
      case CDXConstants.CDXPageDefinition_Reaction1:
        return CDPageDefinition.Reaction1;
      case CDXConstants.CDXPageDefinition_Reaction2:
        return CDPageDefinition.Reaction2;
      case CDXConstants.CDXPageDefinition_MulticolumnTL4:
        return CDPageDefinition.MulticolumnTL4;
      case CDXConstants.CDXPageDefinition_MulticolumnNonTL4:
        return CDPageDefinition.MulticolumnNonTL4;
      case CDXConstants.CDXPageDefinition_UserDefined:
        return CDPageDefinition.UserDefined;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Page definition 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPageDefinition.Undefined;
  }

  public static CDDrawingSpaceType readDrawingSpaceTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXDrawingSpace_Pages:
        return CDDrawingSpaceType.Pages;
      case CDXConstants.CDXDrawingSpace_Poster:
        return CDDrawingSpaceType.Poster;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Drawing space type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDDrawingSpaceType.Pages;
  }

  public static CDUnsaturation readUnsaturationProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXUnsaturation_Unspecified:
        return CDUnsaturation.Unspecified;
      case CDXConstants.CDXUnsaturation_MustBeAbsent:
        return CDUnsaturation.MustBeAbsent;
      case CDXConstants.CDXUnsaturation_MustBePresent:
        return CDUnsaturation.MustBePresent;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Unsaturation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDUnsaturation.Unspecified;
  }

  public static CDExternalConnectionType readExternalConnectionTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXExternalConnection_Unspecified:
        return CDExternalConnectionType.Unspecified;
      case CDXConstants.CDXExternalConnection_Diamond:
        return CDExternalConnectionType.Diamond;
      case CDXConstants.CDXExternalConnection_Star:
        return CDExternalConnectionType.Star;
      case CDXConstants.CDXExternalConnection_PolymerBead:
        return CDExternalConnectionType.PolymerBead;
      case CDXConstants.CDXExternalConnection_Wavy:
        return CDExternalConnectionType.Wavy;
      case CDXConstants.CDXExternalConnection_Residue:
        return CDExternalConnectionType.Residue;
      default:
        break;
    }
    handleUnrecognizedValue(
        "External connection type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDExternalConnectionType.Unspecified;
  }

  public static CDIsotopicAbundance readAbundanceProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXAbundance_Unspecified:
        return CDIsotopicAbundance.Unspecified;
      case CDXConstants.CDXAbundance_Any:
        return CDIsotopicAbundance.Any;
      case CDXConstants.CDXAbundance_Natural:
        return CDIsotopicAbundance.Natural;
      case CDXConstants.CDXAbundance_Enriched:
        return CDIsotopicAbundance.Enriched;
      case CDXConstants.CDXAbundance_Deficient:
        return CDIsotopicAbundance.Deficient;
      case CDXConstants.CDXAbundance_Nonnatural:
        return CDIsotopicAbundance.Nonnatural;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Abundance 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDIsotopicAbundance.Unspecified;
  }

  public static CDTranslation readTranslationProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXTranslation_Equal:
        return CDTranslation.Equal;
      case CDXConstants.CDXTranslation_Broad:
        return CDTranslation.Broad;
      case CDXConstants.CDXTranslation_Narrow:
        return CDTranslation.Narrow;
      case CDXConstants.CDXTranslation_Any:
        return CDTranslation.Any;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Translation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDAtomCIPType readAtomCIPTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXCIPAtom_Undetermined:
        return CDAtomCIPType.Undetermined;
      case CDXConstants.CDXCIPAtom_None:
        return CDAtomCIPType.None;
      case CDXConstants.CDXCIPAtom_R:
        return CDAtomCIPType.R;
      case CDXConstants.CDXCIPAtom_S:
        return CDAtomCIPType.S;
      case CDXConstants.CDXCIPAtom_r:
        return CDAtomCIPType.PseudoR;
      case CDXConstants.CDXCIPAtom_s:
        return CDAtomCIPType.PseudoS;
      case CDXConstants.CDXCIPAtom_Unspecified:
        return CDAtomCIPType.Unspecified;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Atom CIP type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDAtomCIPType.Unspecified;
  }

  public static CDAtomGeometry readAtomGeometryProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXAtomGeometry_Unknown:
        return CDAtomGeometry.Unknown;
      case CDXConstants.CDXAtomGeometry_1Ligand:
        return CDAtomGeometry.OneLigand;
      case CDXConstants.CDXAtomGeometry_Linear:
        return CDAtomGeometry.Linear;
      case CDXConstants.CDXAtomGeometry_Bent:
        return CDAtomGeometry.Bent;
      case CDXConstants.CDXAtomGeometry_TrigonalPlanar:
        return CDAtomGeometry.TrigonalPlanar;
      case CDXConstants.CDXAtomGeometry_TrigonalPyramidal:
        return CDAtomGeometry.TrigonalPyramidal;
      case CDXConstants.CDXAtomGeometry_SquarePlanar:
        return CDAtomGeometry.SquarePlanar;
      case CDXConstants.CDXAtomGeometry_Tetrahedral:
        return CDAtomGeometry.Tetrahedral;
      case CDXConstants.CDXAtomGeometry_TrigonalBipyramidal:
        return CDAtomGeometry.TrigonalBipyramidal;
      case CDXConstants.CDXAtomGeometry_SquarePyramidal:
        return CDAtomGeometry.SquarePyramidal;
      case CDXConstants.CDXAtomGeometry_5Ligand:
        return CDAtomGeometry.FiveLigand;
      case CDXConstants.CDXAtomGeometry_Octahedral:
        return CDAtomGeometry.Octahedral;
      case CDXConstants.CDXAtomGeometry_6Ligand:
        return CDAtomGeometry.SixLigand;
      case CDXConstants.CDXAtomGeometry_7Ligand:
        return CDAtomGeometry.SevenLigand;
      case CDXConstants.CDXAtomGeometry_8Ligand:
        return CDAtomGeometry.EightLigand;
      case CDXConstants.CDXAtomGeometry_9Ligand:
        return CDAtomGeometry.NineLigand;
      case CDXConstants.CDXAtomGeometry_10Ligand:
        return CDAtomGeometry.TenLigand;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Atom geometry 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDAtomGeometry.Unknown;
  }

  public static CDReactionStereo readReactionStereoProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXReactionStereo_Unspecified:
        return CDReactionStereo.Unspecified;
      case CDXConstants.CDXReactionStereo_Inversion:
        return CDReactionStereo.Inversion;
      case CDXConstants.CDXReactionStereo_Retention:
        return CDReactionStereo.Retention;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Reaction stereo 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDReactionStereo.Unspecified;
  }

  public static CDRadical readRadicalProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXRadical_None:
        return CDRadical.None;
      case CDXConstants.CDXRadical_Singlet:
        return CDRadical.Singlet;
      case CDXConstants.CDXRadical_Doublet:
        return CDRadical.Doublet;
      case CDXConstants.CDXRadical_Triplet:
        return CDRadical.Triplet;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Radical 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDRadical.None;
  }

  public static int[] readElementListProperty(CDXProperty property) throws IOException {
    return property.getDataAsInt16Array();
  }

  public static CDLabelDisplay readLabelDisplayProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXLabelDisplay_Auto:
        return CDLabelDisplay.Auto;
      case CDXConstants.CDXLabelDisplay_Left:
        return CDLabelDisplay.Left;
      case CDXConstants.CDXLabelDisplay_Center:
        return CDLabelDisplay.Center;
      case CDXConstants.CDXLabelDisplay_Right:
        return CDLabelDisplay.Right;
      case CDXConstants.CDXLabelDisplay_Above:
        return CDLabelDisplay.Above;
      case CDXConstants.CDXLabelDisplay_Below:
        return CDLabelDisplay.Below;
      case CDXConstants.CDXLabelDisplay_BestInitial:
        return CDLabelDisplay.BestInitial;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Radical 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDLabelDisplay.Auto;
  }

  public static CDNodeType readNodeTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXNodeType_Unspecified:
        return CDNodeType.Unspecified;
      case CDXConstants.CDXNodeType_Element:
        return CDNodeType.Element;
      case CDXConstants.CDXNodeType_ElementList:
        return CDNodeType.ElementList;
      case CDXConstants.CDXNodeType_ElementListNickname:
        return CDNodeType.ElementListNickname;
      case CDXConstants.CDXNodeType_Nickname:
        return CDNodeType.Nickname;
      case CDXConstants.CDXNodeType_Fragment:
        return CDNodeType.Fragment;
      case CDXConstants.CDXNodeType_Formula:
        return CDNodeType.Formula;
      case CDXConstants.CDXNodeType_GenericNickname:
        return CDNodeType.GenericNickname;
      case CDXConstants.CDXNodeType_AnonymousAlternativeGroup:
        return CDNodeType.AnonymousAlternativeGroup;
      case CDXConstants.CDXNodeType_NamedAlternativeGroup:
        return CDNodeType.NamedAlternativeGroup;
      case CDXConstants.CDXNodeType_MultiAttachment:
        return CDNodeType.MultiAttachment;
      case CDXConstants.CDXNodeType_VariableAttachment:
        return CDNodeType.VariableAttachment;
      case CDXConstants.CDXNodeType_ExternalConnectionPoint:
        return CDNodeType.ExternalConnectionPoint;
      case CDXConstants.CDXNodeType_LinkNode:
        return CDNodeType.LinkNode;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Node type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDNodeType.Unspecified;
  }

  public static CDBondReactionParticipation readBondReactionParticipationProperty(
      CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXBondReactionParticipation_Unspecified:
        return CDBondReactionParticipation.Unspecified;
      case CDXConstants.CDXBondReactionParticipation_ReactionCenter:
        return CDBondReactionParticipation.ReactionCenter;
      case CDXConstants.CDXBondReactionParticipation_MakeOrBreak:
        return CDBondReactionParticipation.MakeOrBreak;
      case CDXConstants.CDXBondReactionParticipation_ChangeType:
        return CDBondReactionParticipation.ChangeType;
      case CDXConstants.CDXBondReactionParticipation_MakeAndChange:
        return CDBondReactionParticipation.MakeAndChange;
      case CDXConstants.CDXBondReactionParticipation_NotReactionCenter:
        return CDBondReactionParticipation.NotReactionCenter;
      case CDXConstants.CDXBondReactionParticipation_NoChange:
        return CDBondReactionParticipation.NoChange;
      case CDXConstants.CDXBondReactionParticipation_Unmapped:
        return CDBondReactionParticipation.Unmapped;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond reaction participation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondReactionParticipation.Unspecified;
  }

  public static CDBondTopology readBondTopologyProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXBondTopology_Unspecified:
        return CDBondTopology.Unspecified;
      case CDXConstants.CDXBondTopology_Ring:
        return CDBondTopology.Ring;
      case CDXConstants.CDXBondTopology_Chain:
        return CDBondTopology.Chain;
      case CDXConstants.CDXBondTopology_RingOrChain:
        return CDBondTopology.RingOrChain;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond topology 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondTopology.Unspecified;
  }

  public static CDBondDoublePosition readBondDoublePositionProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXBondDoublePosition_AutoCenter:
        return CDBondDoublePosition.AutoCenter;
      case CDXConstants.CDXBondDoublePosition_AutoRight:
        return CDBondDoublePosition.AutoRight;
      case CDXConstants.CDXBondDoublePosition_AutoLeft:
        return CDBondDoublePosition.AutoLeft;
      case CDXConstants.CDXBondDoublePosition_UserCenter:
        return CDBondDoublePosition.UserCenter;
      case CDXConstants.CDXBondDoublePosition_UserRight:
        return CDBondDoublePosition.UserRight;
      case CDXConstants.CDXBondDoublePosition_UserLeft:
        return CDBondDoublePosition.UserLeft;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond double position 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBondDisplay readBondDisplayProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXBondDisplay_Solid:
        return CDBondDisplay.Solid;
      case CDXConstants.CDXBondDisplay_Dash:
        return CDBondDisplay.Dash;
      case CDXConstants.CDXBondDisplay_Hash:
        return CDBondDisplay.Hash;
      case CDXConstants.CDXBondDisplay_WedgedHashBegin:
        return CDBondDisplay.WedgedHashBegin;
      case CDXConstants.CDXBondDisplay_WedgedHashEnd:
        return CDBondDisplay.WedgedHashEnd;
      case CDXConstants.CDXBondDisplay_Bold:
        return CDBondDisplay.Bold;
      case CDXConstants.CDXBondDisplay_WedgeBegin:
        return CDBondDisplay.WedgeBegin;
      case CDXConstants.CDXBondDisplay_WedgeEnd:
        return CDBondDisplay.WedgeEnd;
      case CDXConstants.CDXBondDisplay_Wavy:
        return CDBondDisplay.Wavy;
      case CDXConstants.CDXBondDisplay_HollowWedgeBegin:
        return CDBondDisplay.HollowWedgeBegin;
      case CDXConstants.CDXBondDisplay_HollowWedgeEnd:
        return CDBondDisplay.HollowWedgeEnd;
      case CDXConstants.CDXBondDisplay_WavyWedgeBegin:
        return CDBondDisplay.WavyWedgeBegin;
      case CDXConstants.CDXBondDisplay_WavyWedgeEnd:
        return CDBondDisplay.WavyWedgeEnd;
      case CDXConstants.CDXBondDisplay_Dot:
        return CDBondDisplay.Dot;
      case CDXConstants.CDXBondDisplay_DashDot:
        return CDBondDisplay.DashDot;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond display 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondDisplay.Solid;
  }

  public static CDBondOrder readBondOrdersProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXBondOrder_Single:
        return CDBondOrder.Single;
      case CDXConstants.CDXBondOrder_Double:
        return CDBondOrder.Double;
      case CDXConstants.CDXBondOrder_Triple:
        return CDBondOrder.Triple;
      case CDXConstants.CDXBondOrder_Quadruple:
        return CDBondOrder.Quadruple;
      case CDXConstants.CDXBondOrder_Quintuple:
        return CDBondOrder.Quintuple;
      case CDXConstants.CDXBondOrder_Sextuple:
        return CDBondOrder.Sextuple;
      case CDXConstants.CDXBondOrder_Half:
        return CDBondOrder.Half;
      case CDXConstants.CDXBondOrder_OneHalf:
        return CDBondOrder.OneHalf;
      case CDXConstants.CDXBondOrder_TwoHalf:
        return CDBondOrder.TwoHalf;
      case CDXConstants.CDXBondOrder_ThreeHalf:
        return CDBondOrder.ThreeHalf;
      case CDXConstants.CDXBondOrder_FourHalf:
        return CDBondOrder.FourHalf;
      case CDXConstants.CDXBondOrder_FiveHalf:
        return CDBondOrder.FiveHalf;
      case CDXConstants.CDXBondOrder_Dative:
        return CDBondOrder.Dative;
      case CDXConstants.CDXBondOrder_Ionic:
        return CDBondOrder.Ionic;
      case CDXConstants.CDXBondOrder_Hydrogen:
        return CDBondOrder.Hydrogen;
      case CDXConstants.CDXBondOrder_ThreeCenter:
        return CDBondOrder.ThreeCenter;
      case CDXConstants.CDXBondOrder_SingleOrDouble:
        return CDBondOrder.SingleOrDouble;
      case CDXConstants.CDXBondOrder_SingleOrAromatic:
        return CDBondOrder.SingleOrAromatic;
      case CDXConstants.CDXBondOrder_DoubleOrAromatic:
        return CDBondOrder.DoubleOrAromatic;
      case CDXConstants.CDXBondOrder_Any:
        return CDBondOrder.Any;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bonder order 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondOrder.Single;
  }

  public static CDGraphicType readGraphicTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXGraphicType_Undefined:
        return CDGraphicType.Undefined;
      case CDXConstants.CDXGraphicType_Line:
        return CDGraphicType.Line;
      case CDXConstants.CDXGraphicType_Arc:
        return CDGraphicType.Arc;
      case CDXConstants.CDXGraphicType_Rectangle:
        return CDGraphicType.Rectangle;
      case CDXConstants.CDXGraphicType_Oval:
        return CDGraphicType.Oval;
      case CDXConstants.CDXGraphicType_Orbital:
        return CDGraphicType.Orbital;
      case CDXConstants.CDXGraphicType_Bracket:
        return CDGraphicType.Bracket;
      case CDXConstants.CDXGraphicType_Symbol:
        return CDGraphicType.Symbol;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Graphic type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDGraphicType.Undefined;
  }

  public static CDLineType readLineTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    // Bug: Combinations of line types are not supported by CDXML
    CDLineType lineType = new CDLineType();
    if ((value & CDXConstants.CDXLineType_Dashed) == CDXConstants.CDXLineType_Dashed) {
      lineType.setDashed(true);
    }
    if ((value & CDXConstants.CDXLineType_Bold) == CDXConstants.CDXLineType_Bold) {
      lineType.setBold(true);
    }
    if ((value & CDXConstants.CDXLineType_Wavy) == CDXConstants.CDXLineType_Wavy) {
      lineType.setWavy(true);
    }
    return lineType;
  }

  public static CDArrowType readArrowTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXArrowType_NoHead:
        return CDArrowType.NoHead;
      case CDXConstants.CDXArrowType_HalfHead:
        return CDArrowType.HalfHead;
      case CDXConstants.CDXArrowType_FullHead:
        return CDArrowType.FullHead;
      case CDXConstants.CDXArrowType_Resonance:
        return CDArrowType.Resonance;
      case CDXConstants.CDXArrowType_Equilibrium:
        return CDArrowType.Equilibrium;
      case CDXConstants.CDXArrowType_Hollow:
        return CDArrowType.Hollow;
      case CDXConstants.CDXArrowType_RetroSynthetic:
        return CDArrowType.RetroSynthetic;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowType.NoHead;
  }

  public static CDRectangleType readRectangleTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    CDRectangleType rectangleType = new CDRectangleType();
    if ((value & CDXConstants.CDXRectangleType_RoundEdge) != 0) {
      rectangleType.setRoundEdge(true);
    }
    if ((value & CDXConstants.CDXRectangleType_Shadow) != 0) {
      rectangleType.setShadow(true);
    }
    if ((value & CDXConstants.CDXRectangleType_Shaded) != 0) {
      rectangleType.setShaded(true);
    }
    if ((value & CDXConstants.CDXRectangleType_Filled) != 0) {
      rectangleType.setFilled(true);
    }
    if ((value & CDXConstants.CDXRectangleType_Dashed) != 0) {
      rectangleType.setDashed(true);
    }
    if ((value & CDXConstants.CDXRectangleType_Bold) != 0) {
      rectangleType.setBold(true);
    }
    return rectangleType;
  }

  public static CDOvalType readOvalTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    CDOvalType ovalType = new CDOvalType();
    if ((value & CDXConstants.CDXOvalType_Circle) != 0) {
      ovalType.setCircle(true);
    }
    if ((value & CDXConstants.CDXOvalType_Shaded) != 0) {
      ovalType.setShaded(true);
    }
    if ((value & CDXConstants.CDXOvalType_Filled) != 0) {
      ovalType.setFilled(true);
    }
    if ((value & CDXConstants.CDXOvalType_Dashed) != 0) {
      ovalType.setDashed(true);
    }
    if ((value & CDXConstants.CDXOvalType_Bold) != 0) {
      ovalType.setBold(true);
    }
    if ((value & CDXConstants.CDXOvalType_Shadowed) != 0) {
      ovalType.setShadowed(true);
    }
    return ovalType;
  }

  public static CDSplineType readCurveTypeProperty(CDXProperty property) throws IOException {
    return convertIntToSplineType(property.getDataAsInt16());
  }

  public static CDFillType readFillTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXConstants.CDXFillType_Unspecified:
        return CDFillType.Unspecified;
      case CDXConstants.CDXFillType_None:
        return CDFillType.None;
      case CDXConstants.CDXFillType_Solid:
        return CDFillType.Solid;
      case CDXConstants.CDXFillType_Shaded:
        return CDFillType.Shaded;
      case CDXConstants.CDXFillType_Faded:
        return CDFillType.Faded;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Fill type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDFillType.Unspecified;
  }

  public static CDOrbitalType readOrbitalTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXOrbitalType_s:
        return CDOrbitalType.s;
      case CDXConstants.CDXOrbitalType_oval:
        return CDOrbitalType.oval;
      case CDXConstants.CDXOrbitalType_lobe:
        return CDOrbitalType.lobe;
      case CDXConstants.CDXOrbitalType_p:
        return CDOrbitalType.p;
      case CDXConstants.CDXOrbitalType_hybridPlus:
        return CDOrbitalType.hybridPlus;
      case CDXConstants.CDXOrbitalType_hybridMinus:
        return CDOrbitalType.hybridMinus;
      case CDXConstants.CDXOrbitalType_dz2Plus:
        return CDOrbitalType.dz2Plus;
      case CDXConstants.CDXOrbitalType_dz2Minus:
        return CDOrbitalType.dz2Minus;
      case CDXConstants.CDXOrbitalType_dxy:
        return CDOrbitalType.dxy;
      case CDXConstants.CDXOrbitalType_sShaded:
        return CDOrbitalType.sShaded;
      case CDXConstants.CDXOrbitalType_ovalShaded:
        return CDOrbitalType.ovalShaded;
      case CDXConstants.CDXOrbitalType_lobeShaded:
        return CDOrbitalType.lobeShaded;
      case CDXConstants.CDXOrbitalType_pShaded:
        return CDOrbitalType.pShaded;
      case CDXConstants.CDXOrbitalType_sFilled:
        return CDOrbitalType.sFilled;
      case CDXConstants.CDXOrbitalType_ovalFilled:
        return CDOrbitalType.ovalFilled;
      case CDXConstants.CDXOrbitalType_lobeFilled:
        return CDOrbitalType.lobeFilled;
      case CDXConstants.CDXOrbitalType_pFilled:
        return CDOrbitalType.pFilled;
      case CDXConstants.CDXOrbitalType_hybridPlusFilled:
        return CDOrbitalType.hybridPlusFilled;
      case CDXConstants.CDXOrbitalType_hybridMinusFilled:
        return CDOrbitalType.hybridMinusFilled;
      case CDXConstants.CDXOrbitalType_dz2PlusFilled:
        return CDOrbitalType.dz2PlusFilled;
      case CDXConstants.CDXOrbitalType_dz2MinusFilled:
        return CDOrbitalType.dz2MinusFilled;
      case CDXConstants.CDXOrbitalType_dxyFilled:
        return CDOrbitalType.dxyFilled;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Orbital type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBracketType readBracketTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXBracketType_RoundPair:
        return CDBracketType.RoundPair;
      case CDXConstants.CDXBracketType_SquarePair:
        return CDBracketType.SquarePair;
      case CDXConstants.CDXBracketType_CurlyPair:
        return CDBracketType.CurlyPair;
      case CDXConstants.CDXBracketType_Square:
        return CDBracketType.Square;
      case CDXConstants.CDXBracketType_Curly:
        return CDBracketType.Curly;
      case CDXConstants.CDXBracketType_Round:
        return CDBracketType.Round;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bracket type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDSymbolType readSymbolTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXSymbolType_LonePair:
        return CDSymbolType.LonePair;
      case CDXConstants.CDXSymbolType_Electron:
        return CDSymbolType.Electron;
      case CDXConstants.CDXSymbolType_RadicalCation:
        return CDSymbolType.RadicalCation;
      case CDXConstants.CDXSymbolType_RadicalAnion:
        return CDSymbolType.RadicalAnion;
      case CDXConstants.CDXSymbolType_CirclePlus:
        return CDSymbolType.CirclePlus;
      case CDXConstants.CDXSymbolType_CircleMinus:
        return CDSymbolType.CircleMinus;
      case CDXConstants.CDXSymbolType_Dagger:
        return CDSymbolType.Dagger;
      case CDXConstants.CDXSymbolType_DoubleDagger:
        return CDSymbolType.DoubleDagger;
      case CDXConstants.CDXSymbolType_Plus:
        return CDSymbolType.Plus;
      case CDXConstants.CDXSymbolType_Minus:
        return CDSymbolType.Minus;
      case CDXConstants.CDXSymbolType_Racemic:
        return CDSymbolType.Racemic;
      case CDXConstants.CDXSymbolType_Absolute:
        return CDSymbolType.Absolute;
      case CDXConstants.CDXSymbolType_Relative:
        return CDSymbolType.Relative;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Symbol type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBracketUsage readBracketUsageProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXBracketUsage_Unspecified:
        return CDBracketUsage.Unspecified;
      case CDXConstants.CDXBracketUsage_Anypolymer:
        return CDBracketUsage.Anypolymer;
      case CDXConstants.CDXBracketUsage_Component:
        return CDBracketUsage.Component;
      case CDXConstants.CDXBracketUsage_Copolymer:
        return CDBracketUsage.Copolymer;
      case CDXConstants.CDXBracketUsage_CopolymerAlternating:
        return CDBracketUsage.CopolymerAlternating;
      case CDXConstants.CDXBracketUsage_CopolymerBlock:
        return CDBracketUsage.CopolymerBlock;
      case CDXConstants.CDXBracketUsage_CopolymerRandom:
        return CDBracketUsage.CopolymerRandom;
      case CDXConstants.CDXBracketUsage_Crosslink:
        return CDBracketUsage.Crosslink;
      case CDXConstants.CDXBracketUsage_Generic:
        return CDBracketUsage.Generic;
      case CDXConstants.CDXBracketUsage_Graft:
        return CDBracketUsage.Graft;
      case CDXConstants.CDXBracketUsage_Mer:
        return CDBracketUsage.Mer;
      case CDXConstants.CDXBracketUsage_MixtureOrdered:
        return CDBracketUsage.MixtureOrdered;
      case CDXConstants.CDXBracketUsage_MixtureUnordered:
        return CDBracketUsage.MixtureUnordered;
      case CDXConstants.CDXBracketUsage_Modification:
        return CDBracketUsage.Modification;
      case CDXConstants.CDXBracketUsage_Monomer:
        return CDBracketUsage.Monomer;
      case CDXConstants.CDXBracketUsage_MultipleGroup:
        return CDBracketUsage.MultipleGroup;
      case CDXConstants.CDXBracketUsage_SRU:
        return CDBracketUsage.SRU;
      case CDXConstants.CDXBracketUsage_Unused1:
        return CDBracketUsage.Unused1;
      case CDXConstants.CDXBracketUsage_Unused2:
        return CDBracketUsage.Unused2;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bracket usage 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBracketUsage.Unspecified;
  }

  public static CDPolymerRepeatPattern readPolymerRepeatPatternProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXPolymerRepeatPattern_HeadToTail:
        return CDPolymerRepeatPattern.HeadToTail;
      case CDXConstants.CDXPolymerRepeatPattern_HeadToHead:
        return CDPolymerRepeatPattern.HeadToHead;
      case CDXConstants.CDXPolymerRepeatPattern_EitherUnknown:
        return CDPolymerRepeatPattern.EitherUnknown;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Polymer repeat pattern 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDPolymerFlipType readPolymerFlipTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXConstants.CDXPolymerFlipType_Unspecified:
        return CDPolymerFlipType.Unspecified;
      case CDXConstants.CDXPolymerFlipType_NoFlip:
        return CDPolymerFlipType.NoFlip;
      case CDXConstants.CDXPolymerFlipType_Flip:
        return CDPolymerFlipType.Flip;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Polymer flip type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPolymerFlipType.Unspecified;
  }

  public static CDGeometryType readGeometricFeatureProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXGeometricFeature_Undefined:
        return CDGeometryType.Undefined;
      case CDXConstants.CDXGeometricFeature_PointFromPointPointDistance:
        return CDGeometryType.PointFromPointPointDistance;
      case CDXConstants.CDXGeometricFeature_PointFromPointPointPercentage:
        return CDGeometryType.PointFromPointPointPercentage;
      case CDXConstants.CDXGeometricFeature_PointFromPointNormalDistance:
        return CDGeometryType.PointFromPointNormalDistance;
      case CDXConstants.CDXGeometricFeature_LineFromPoints:
        return CDGeometryType.LineFromPoints;
      case CDXConstants.CDXGeometricFeature_PlaneFromPoints:
        return CDGeometryType.PlaneFromPoints;
      case CDXConstants.CDXGeometricFeature_PlaneFromPointLine:
        return CDGeometryType.PlaneFromPointLine;
      case CDXConstants.CDXGeometricFeature_CentroidFromPoints:
        return CDGeometryType.CentroidFromPoints;
      case CDXConstants.CDXGeometricFeature_NormalFromPointPlane:
        return CDGeometryType.NormalFromPointPlane;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Geometric feature 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDGeometryType.Undefined;
  }

  public static CDConstraintType readConstraintTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXConstraintType_Undefined:
        return CDConstraintType.Undefined;
      case CDXConstants.CDXConstraintType_Distance:
        return CDConstraintType.Distance;
      case CDXConstants.CDXConstraintType_Angle:
        return CDConstraintType.Angle;
      case CDXConstants.CDXConstraintType_ExclusionSphere:
        return CDConstraintType.ExclusionSphere;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Constraint type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDConstraintType.Undefined;
  }

  public static CDSideType readSideTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXConstants.CDXSideType_Undefined:
        return CDSideType.Undefined;
      case CDXConstants.CDXSideType_Top:
        return CDSideType.Top;
      case CDXConstants.CDXSideType_Left:
        return CDSideType.Left;
      case CDXConstants.CDXSideType_Bottom:
        return CDSideType.Bottom;
      case CDXConstants.CDXSideType_Right:
        return CDSideType.Right;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Side type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSideType.Undefined;
  }

  public static CDObjectTagType readObjectTagTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXObjectTagType_Undefined:
        return CDObjectTagType.Undefined;
      case CDXConstants.CDXObjectTagType_Double:
        return CDObjectTagType.Double;
      case CDXConstants.CDXObjectTagType_Long:
        return CDObjectTagType.Long;
      case CDXConstants.CDXObjectTagType_String:
        return CDObjectTagType.String;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Object tag type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDObjectTagType.Undefined;
  }

  public static CDPositioningType readPositioningTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXPositioningType_Auto:
        return CDPositioningType.Auto;
      case CDXConstants.CDXPositioningType_Angle:
        return CDPositioningType.Angle;
      case CDXConstants.CDXPositioningType_Offset:
        return CDPositioningType.Offset;
      case CDXConstants.CDXPositioningType_Absolute:
        return CDPositioningType.Absolute;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Positioning type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPositioningType.Auto;
  }

  public static CDSpectrumClass readSpectrumClassProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXSpectrumClass_Unknown:
        return CDSpectrumClass.Unknown;
      case CDXConstants.CDXSpectrumClass_Chromatogram:
        return CDSpectrumClass.Chromatogram;
      case CDXConstants.CDXSpectrumClass_Infrared:
        return CDSpectrumClass.Infrared;
      case CDXConstants.CDXSpectrumClass_UVVis:
        return CDSpectrumClass.UVVis;
      case CDXConstants.CDXSpectrumClass_XRayDiffraction:
        return CDSpectrumClass.XRayDiffraction;
      case CDXConstants.CDXSpectrumClass_MassSpectrum:
        return CDSpectrumClass.MassSpectrum;
      case CDXConstants.CDXSpectrumClass_NMR:
        return CDSpectrumClass.NMR;
      case CDXConstants.CDXSpectrumClass_Raman:
        return CDSpectrumClass.Raman;
      case CDXConstants.CDXSpectrumClass_Fluorescence:
        return CDSpectrumClass.Fluorescence;
      case CDXConstants.CDXSpectrumClass_Atomic:
        return CDSpectrumClass.Atomic;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum class 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumClass.Unknown;
  }

  public static CDSpectrumXType readSpectrumXTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXSpectrumXType_Unknown:
        return CDSpectrumXType.Unknown;
      case CDXConstants.CDXSpectrumXType_Wavenumbers:
        return CDSpectrumXType.Wavenumbers;
      case CDXConstants.CDXSpectrumXType_Microns:
        return CDSpectrumXType.Microns;
      case CDXConstants.CDXSpectrumXType_Hertz:
        return CDSpectrumXType.Hertz;
      case CDXConstants.CDXSpectrumXType_MassUnits:
        return CDSpectrumXType.MassUnits;
      case CDXConstants.CDXSpectrumXType_PartsPerMillion:
        return CDSpectrumXType.PartsPerMillion;
      case CDXConstants.CDXSpectrumXType_Other:
        return CDSpectrumXType.Other;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum x type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumXType.Unknown;
  }

  public static CDSpectrumYType readSpectrumYTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXConstants.CDXSpectrumYType_Unknown:
        return CDSpectrumYType.Unknown;
      case CDXConstants.CDXSpectrumYType_Absorbance:
        return CDSpectrumYType.Absorbance;
      case CDXConstants.CDXSpectrumYType_Transmittance:
        return CDSpectrumYType.Transmittance;
      case CDXConstants.CDXSpectrumYType_PercentTransmittance:
        return CDSpectrumYType.PercentTransmittance;
      case CDXConstants.CDXSpectrumYType_Other:
        return CDSpectrumYType.Other;
      case CDXConstants.CDXSpectrumYType_ArbitraryUnits:
        return CDSpectrumYType.ArbitraryUnits;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum y type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumYType.Unknown;
  }

  public static CDRingBondCount readRingBondCountProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt8();
    switch (value) {
      case CDXConstants.CDXRingBondCount_Unspecified:
        return CDRingBondCount.Unspecified;
      case CDXConstants.CDXRingBondCount_NoRingBonds:
        return CDRingBondCount.Unspecified;
      case CDXConstants.CDXRingBondCount_AsDrawn:
        return CDRingBondCount.Unspecified;
      case CDXConstants.CDXRingBondCount_SimpleRing:
        return CDRingBondCount.Unspecified;
      case CDXConstants.CDXRingBondCount_Fusion:
        return CDRingBondCount.Unspecified;
      case CDXConstants.CDXRingBondCount_SpiroOrHigher:
        return CDRingBondCount.Unspecified;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Ring bond count 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDRingBondCount.Unspecified;
  }

  public static float readLineHeight(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    if (value == 0) {
      return CDSettings.LineHeight_Variable;
    }
    if (value == 1) {
      return CDSettings.LineHeight_Automatic;
    }
    return value / 20f;
  }

  public static CDArrowHeadType readArrowheadTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXConstants.CDXArrowheadType_Solid:
        return CDArrowHeadType.Solid;
      case CDXConstants.CDXArrowheadType_Hollow:
        return CDArrowHeadType.Hollow;
      case CDXConstants.CDXArrowheadType_Angle:
        return CDArrowHeadType.Angle;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow head type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowHeadType.Solid;
  }

  public static CDArrowHeadPositionType readArrowheadProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXConstants.CDXArrowhead_Unspecified:
        return CDArrowHeadPositionType.Unspecified;
      case CDXConstants.CDXArrowhead_None:
        return CDArrowHeadPositionType.None;
      case CDXConstants.CDXArrowhead_Full:
        return CDArrowHeadPositionType.Full;
      case CDXConstants.CDXArrowhead_HalfLeft:
        return CDArrowHeadPositionType.HalfLeft;
      case CDXConstants.CDXArrowhead_HalfRight:
        return CDArrowHeadPositionType.HalfRight;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow head 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowHeadPositionType.Unspecified;
  }

  public static CDNoGoType readNoGoProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstants.CDXArrowNoGo_Unspecified:
        return CDNoGoType.Unspecified;
      case CDXConstants.CDXArrowNoGo_None:
        return CDNoGoType.None;
      case CDXConstants.CDXArrowNoGo_Cross:
        return CDNoGoType.Cross;
      case CDXConstants.CDXArrowNoGo_Hash:
        return CDNoGoType.Hash;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow no go 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDNoGoType.Unspecified;
  }

  public static String getPositionAsString(CDXObject object) {
    return object.getPosition() + "(0x" + Integer.toHexString(object.getPosition()) + ")";
  }

  public static String getPositionAsString(CDXProperty property) {
    return property.getPosition() + "(0x" + Integer.toHexString(property.getPosition()) + ")";
  }

  public static String dumpProperty(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for (byte b : data) {
      String hex = Integer.toHexString(b & 0xff);
      if (hex.length() == 1) {
        sb.append("0");
      }
      sb.append(hex);
      sb.append(" ");
    }
    return sb.toString();
  }

  private static void handleUnrecognizedValue(String message) throws IOException {
    if (CDXReader.RIGID) {
      throw new IOException(message);
    }
    LOGGER.warn(message);
  }

  public static boolean containsLineWrapBug(CDDocument doc) {
    // search for bug
    if (doc.getPages() == null || doc.getPages().isEmpty()) {
      return false;
    }
    boolean foundBug = false;
    for (CDPage page : doc.getPages()) {
      foundBug |= containsLineWrapBug(page);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDPage page) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(page.getTexts());
    for (CDGroup g : page.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDAltGroup ag : page.getNamedAlternativeGroups()) {
      foundBug |= containsLineWrapBug(ag);
    }
    for (CDFragment f : page.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDGroup group) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(group.getCaptions());
    for (CDGroup g : group.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDAltGroup ag : group.getNamedAlternativeGroups()) {
      foundBug |= containsLineWrapBug(ag);
    }
    for (CDFragment f : group.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDAltGroup altgroup) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(altgroup.getCaptions());
    for (CDGroup g : altgroup.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDFragment f : altgroup.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDFragment fragment) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(fragment.getTexts());
    return foundBug;
  }

  static boolean containsLineWrapBug(List<CDText> texts) {
    for (CDText text : texts) {
      List<Integer> definedLineStarts = text.getLineStarts();
      CDStyledString css = text.getText();
      if (css == null) {
        continue;
      }
      String t = css.getText();
      if (t == null) {
        continue;
      }

      if (containsLineWrapBugCharacter(t)) {
        List<Integer> calculatedLineStarts = getCalculatedLineStarts(t, (char) 13);
        for (int i : calculatedLineStarts) {
          if (!definedLineStarts.contains(i)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static void fixLineWrapBug(CDDocument doc) {
    // search for bug
    if (doc.getPages() == null || doc.getPages().isEmpty()) {
      return;
    }
    for (CDPage page : doc.getPages()) {
      fixLineWrapBug(page);
    }
  }

  static void fixLineWrapBug(CDPage page) {
    fixLineWrapBug(page.getTexts());
    for (CDGroup group : page.getGroups()) {
      fixLineWrapBug(group);
    }
    for (CDAltGroup altGroup : page.getNamedAlternativeGroups()) {
      fixLineWrapBug(altGroup);
    }
    for (CDFragment fragment : page.getFragments()) {
      fixLineWrapBug(fragment);
    }
  }

  static void fixLineWrapBug(CDGroup group) {
    fixLineWrapBug(group.getCaptions());
    for (CDGroup g : group.getGroups()) {
      fixLineWrapBug(g);
    }
    for (CDAltGroup ag : group.getNamedAlternativeGroups()) {
      fixLineWrapBug(ag);
    }
    for (CDFragment f : group.getFragments()) {
      fixLineWrapBug(f);
    }
  }

  static void fixLineWrapBug(CDAltGroup altgroup) {
    fixLineWrapBug(altgroup.getCaptions());
    for (CDGroup g : altgroup.getGroups()) {
      fixLineWrapBug(g);
    }
    for (CDFragment f : altgroup.getFragments()) {
      fixLineWrapBug(f);
    }
  }

  static void fixLineWrapBug(CDFragment fragment) {
    fixLineWrapBug(fragment.getTexts());
  }

  static void fixLineWrapBug(List<CDText> texts) {
    for (CDText text : texts) {
      List<Integer> definedLineStarts = text.getLineStarts();
      CDStyledString css = text.getText();
      if (css == null) {
        continue;
      }
      String t = css.getText();
      if (t == null) {
        continue;
      }

      //      for (int i=0; i < t.length(); i++) {
      //        System.out.println(i + ": " + t.charAt(i) + " (" + (int)t.charAt(i) + "/" +
      // Integer.toHexString((int)t.charAt(i)) + ")");
      //      }

      if (containsLineWrapBugCharacter(t)) {
        t += (char) 13;
        List<Integer> calculatedLineStarts = getCalculatedLineStarts(t, (char) 13);
        boolean erroneous = false;
        for (int i : calculatedLineStarts) {
          if (!definedLineStarts.contains(i)) {
            erroneous = true;
          }
        }
        if (erroneous) {
          text.setLineStarts(calculatedLineStarts);
        }
      }
    }
  }

  public static List<Integer> getCalculatedLineStarts(String s, char c) {
    List<Integer> result = new ArrayList<Integer>();
    while (s.lastIndexOf(c) > -1) {
      int i = s.lastIndexOf(c);
      s = s.substring(0, i);
      result.add(0, i + 1);
    }
    return result;
  }

  public static boolean containsLineWrapBugCharacter(String t) {
    return t.contains("\u00B0")
        || // degree sign
        t.contains("\u00B7")
        || // middle dot
        t.contains("\u00D7")
        || // multiplication sign
        t.contains("\u00AE")
        || // rightwards arrow (actually registered)
        t.contains("\u00B3")
        || // less-than or equal to (actually superscript three)
        t.contains("\u00A3")
        || // greater-than or equal to (actually pound sign)
        t.contains("\u00A9")
        || // copyright
        t.contains("\u00D4")
        || // trademark (actually latin capital letter O with circumflex
        t.contains("\u00B9")
        || // not equals (actually superscript one)
        t.contains("\u00BB")
        || // almost equal to (actually right-pointing double angle quotation mark)
        t.contains("\u00C5"); // latin capital letter A with ring above
  }
}
