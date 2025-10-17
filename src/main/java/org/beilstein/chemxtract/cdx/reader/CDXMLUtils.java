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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDSettings;
import org.beilstein.chemxtract.cdx.datatypes.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.beilstein.chemxtract.cdx.reader.CDXMLConstants.*;

/**
 * This holds various methods to convert values for the CDXML file format.
 */
public class CDXMLUtils {
  private static final Log logger = LogFactory.getLog(CDXMLUtils.class);

  public static String convertTextJustificationToString(CDJustification value) throws IOException {
    return convertEnumToString(value, CDXMLTextJustification);
  }

  public static CDJustification convertStringToTextJustification(String value) throws IOException {
    return (CDJustification) convertStringToEnum(value, CDXMLTextJustification);
  }

  public static String convertDrawingSpaceTypeToString(CDDrawingSpaceType value) throws IOException {
    return convertEnumToString(value, CDXMLDrawingSpaceType);
  }

  public static CDDrawingSpaceType convertStringToDrawingSpaceType(String value) throws IOException {
    return (CDDrawingSpaceType) convertStringToEnum(value, CDXMLDrawingSpaceType);
  }

  public static String convertPageDefinitionToString(CDPageDefinition value) throws IOException {
    return convertEnumToString(value, CDXMLPageDefinition);
  }

  public static CDPageDefinition convertStringToPageDefinition(String value) throws IOException {
    return (CDPageDefinition) convertStringToEnum(value, CDXMLPageDefinition);
  }

  public static String convertLabelDisplayToString(CDLabelDisplay value) throws IOException {
    return convertEnumToString(value, CDXMLLabelDisplay);
  }

  public static CDLabelDisplay convertStringToLabelDisplay(String value) throws IOException {
    return (CDLabelDisplay) convertStringToEnum(value, CDXMLLabelDisplay);
  }

  public static String convertNodeTypeToString(CDNodeType value) throws IOException {
    return convertEnumToString(value, CDXMLNodeType);
  }

  public static CDNodeType convertStringToNodeType(String value) throws IOException {
    return (CDNodeType) convertStringToEnum(value, CDXMLNodeType);
  }

  public static String convertRadicalToString(CDRadical value) throws IOException {
    return convertEnumToString(value, CDXMLRadical);
  }

  public static CDRadical convertStringToRadical(String value) throws IOException {
    return (CDRadical) convertStringToEnum(value, CDXMLRadical);
  }

  public static String convertRingBondCountToString(CDRingBondCount value) throws IOException {
    return convertEnumToString(value, CDXMLRingBondCount);
  }

  public static CDRingBondCount convertStringToRingBondCount(String value) throws IOException {
    return (CDRingBondCount) convertStringToEnum(value, CDXMLRingBondCount);
  }

  public static String convertUnsaturationToString(CDUnsaturation value) throws IOException {
    return convertEnumToString(value, CDXMLUnsaturation);
  }

  public static CDUnsaturation convertStringToUnsaturation(String value) throws IOException {
    return (CDUnsaturation) convertStringToEnum(value, CDXMLUnsaturation);
  }

  public static String convertReactionStereoToString(CDReactionStereo value) throws IOException {
    return convertEnumToString(value, CDXMLReactionStereo);
  }

  public static CDReactionStereo convertStringToReactionStereo(String value) throws IOException {
    return (CDReactionStereo) convertStringToEnum(value, CDXMLReactionStereo);
  }

  public static String convertTranslationToString(CDTranslation value) throws IOException {
    return convertEnumToString(value, CDXMLTranslation);
  }

  public static CDTranslation convertStringToTranslation(String value) throws IOException {
    return (CDTranslation) convertStringToEnum(value, CDXMLTranslation);
  }

  public static String convertAbundanceToString(CDIsotopicAbundance value) throws IOException {
    return convertEnumToString(value, CDXMLAbundance);
  }

  public static CDIsotopicAbundance convertStringToAbundance(String value) throws IOException {
    return (CDIsotopicAbundance) convertStringToEnum(value, CDXMLAbundance);
  }

  public static String convertExternalConnectionTypeToString(CDExternalConnectionType value) throws IOException {
    return convertEnumToString(value, CDXMLExternalConnectionType);
  }

  public static CDExternalConnectionType convertStringToExternalConnectionType(String value) throws IOException {
    return (CDExternalConnectionType) convertStringToEnum(value, CDXMLExternalConnectionType);
  }

  public static String convertAtomGeometryToString(CDAtomGeometry value) throws IOException {
    return convertEnumToString(value, CDXMLAtomGeometry);
  }

  public static CDAtomGeometry convertStringToAtomGeometry(String value) throws IOException {
    return (CDAtomGeometry) convertStringToEnum(value, CDXMLAtomGeometry);
  }

  public static String convertAtomCIPTypeToString(CDAtomCIPType value) throws IOException {
    return convertEnumToString(value, CDXMLAtomCIPType);
  }

  public static CDAtomCIPType convertStringToAtomCIPType(String value) throws IOException {
    return (CDAtomCIPType) convertStringToEnum(value, CDXMLAtomCIPType);
  }

  public static String convertBondOrderToString(CDBondOrder value) throws IOException {
    return convertEnumToString(value, CDXMLBondOrder);
  }

  public static CDBondOrder convertStringToBondOrder(String value) throws IOException {
    return (CDBondOrder) convertStringToEnum(value, CDXMLBondOrder);
  }

  public static String convertBondDisplayToString(CDBondDisplay value) throws IOException {
    return convertEnumToString(value, CDXMLBondDisplay);
  }

  public static CDBondDisplay convertStringToBondDisplay(String value) throws IOException {
    return (CDBondDisplay) convertStringToEnum(value, CDXMLBondDisplay);
  }

  public static String convertBondDoublePositionToString(CDBondDoublePosition value) throws IOException {
    return convertEnumToString(value, CDXMLBondDoublePosition);
  }

  public static CDBondDoublePosition convertStringToBondDoublePosition(String value) throws IOException {
    return (CDBondDoublePosition) convertStringToEnum(value, CDXMLBondDoublePosition);
  }

  public static String convertBondTopologyToString(CDBondTopology value) throws IOException {
    return convertEnumToString(value, CDXMLBondTopology);
  }

  public static CDBondTopology convertStringToBondTopology(String value) throws IOException {
    return (CDBondTopology) convertStringToEnum(value, CDXMLBondTopology);
  }

  public static String convertBondReactionParticipationToString(CDBondReactionParticipation value) throws IOException {
    return convertEnumToString(value, CDXMLBondReactionParticipation);
  }

  public static CDBondReactionParticipation convertStringToBondReactionParticipation(String value) throws IOException {
    return (CDBondReactionParticipation) convertStringToEnum(value, CDXMLBondReactionParticipation);
  }

  public static String convertBondCIPTypeToString(CDBondCIPType value) throws IOException {
    return convertEnumToString(value, CDXMLBondCIPType);
  }

  public static CDBondCIPType convertStringToBondCIPType(String value) throws IOException {
    return (CDBondCIPType) convertStringToEnum(value, CDXMLBondCIPType);
  }

  public static String convertGraphicTypeToString(CDGraphicType value) throws IOException {
    return convertEnumToString(value, CDXMLGraphicType);
  }

  public static CDGraphicType convertStringToGraphicType(String value) throws IOException {
    return (CDGraphicType) convertStringToEnum(value, CDXMLGraphicType);
  }

  public static String convertLineTypeToString(CDLineType value) {
    StringBuilder sb = new StringBuilder();
    if (value.isDashed()) {
      sb.append(CDXLineType_Dashed);
    }
    if (value.isBold()) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(CDXLineType_Bold);
    }
    if (value.isWavy()) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(CDXLineType_Wavy);
    }
    if (sb.length() > 0) {
      return sb.toString();
    }
    return CDXLineType_Solid;
  }

  public static CDLineType convertStringToLineType(String value) {
    CDLineType lineType = new CDLineType();
    if (value.indexOf(CDXLineType_Dashed) >= 0) {
      lineType.setDashed(true);
    } else if (value.indexOf(CDXLineType_Bold) >= 0) {
      lineType.setBold(true);
    } else if (value.indexOf(CDXLineType_Wavy) >= 0) {
      lineType.setWavy(true);
    }
    return lineType;
  }

  public static String convertArrowTypeToString(CDArrowType value) throws IOException {
    return convertEnumToString(value, CDXMLArrowType);
  }

  public static CDArrowType convertStringToArrowType(String value) throws IOException {
    return (CDArrowType) convertStringToEnum(value, CDXMLArrowType);
  }

  public static String convertBracketTypeToString(CDBracketType value) throws IOException {
    return convertEnumToString(value, CDXMLBracketType);
  }

  public static CDBracketType convertStringToBracketType(String value) throws IOException {
    return (CDBracketType) convertStringToEnum(value, CDXMLBracketType);
  }

  public static String convertRectangleTypeToString(CDRectangleType value) {
    StringBuilder sb = new StringBuilder();
    if (value.isPlain()) {
      sb.append(CDXRectangleType_Plain);
    }
    if (value.isRoundEdge()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_RoundEdge);
    }
    if (value.isShadow()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_Shadow);
    }
    if (value.isShaded()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_Shaded);
    }
    if (value.isFilled()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_Filled);
    }
    if (value.isDashed()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_Dashed);
    }
    if (value.isBold()) {
      appendSeparator(sb);
      sb.append(CDXRectangleType_Bold);
    }
    return sb.toString();
  }

  public static CDRectangleType convertStringToRectangleType(String value) throws IOException {
    StringTokenizer st = new StringTokenizer(value, " ");
    CDRectangleType result = new CDRectangleType();
    while (st.hasMoreTokens()) {
      String element = st.nextToken();
      if (element.equals(CDXRectangleType_Plain)) {
        result.setPlain(true);
      } else if (element.equals(CDXRectangleType_RoundEdge)) {
        result.setRoundEdge(true);
      } else if (element.equals(CDXRectangleType_Shadow)) {
        result.setShadow(true);
      } else if (element.equals(CDXRectangleType_Shaded)) {
        result.setShaded(true);
      } else if (element.equals(CDXRectangleType_Filled)) {
        result.setFilled(true);
      } else if (element.equals(CDXRectangleType_Dashed)) {
        result.setDashed(true);
      } else if (element.equals(CDXRectangleType_Bold)) {
        result.setBold(true);
      } else {
        throw new IOException("Rectangle type \"" + element + "\" not recognized");
      }
    }
    return result;
  }

  public static String convertOvalTypeToString(CDOvalType value) {
    StringBuilder sb = new StringBuilder();
    if (value.isCircle()) {
      sb.append(CDXMLOvalType_Circle);
    }
    if (value.isShaded()) {
      appendSeparator(sb);
      sb.append(CDXMLOvalType_Shaded);
    }
    if (value.isFilled()) {
      appendSeparator(sb);
      sb.append(CDXMLOvalType_Filled);
    }
    if (value.isDashed()) {
      appendSeparator(sb);
      sb.append(CDXMLOvalType_Dashed);
    }
    if (value.isBold()) {
      appendSeparator(sb);
      sb.append(CDXMLOvalType_Bold);
    }
    if (value.isShadowed()) {
      appendSeparator(sb);
      sb.append(CDXMLOvalType_Shadowed);
    }
    return sb.toString();
  }

  public static CDOvalType convertStringToOvalType(String value) throws IOException {
    StringTokenizer st = new StringTokenizer(value, " ");
    CDOvalType result = new CDOvalType();
    while (st.hasMoreTokens()) {
      String element = st.nextToken();
      if (element.equals(CDXMLOvalType_Circle)) {
        result.setCircle(true);
      } else if (element.equals(CDXMLOvalType_Shaded)) {
        result.setShaded(true);
      } else if (element.equals(CDXMLOvalType_Filled)) {
        result.setFilled(true);
      } else if (element.equals(CDXMLOvalType_Dashed)) {
        result.setDashed(true);
      } else if (element.equals(CDXMLOvalType_Bold)) {
        result.setBold(true);
      } else if (element.equals(CDXMLOvalType_Shadowed)) {
        result.setShadowed(true);
      } else {
        throw new IOException("Oval type \"" + element + "\" not recognized");
      }
    }
    return result;
  }

  public static String convertOrbitalTypeToString(CDOrbitalType value) throws IOException {
    return convertEnumToString(value, CDXMLOrbitalType);
  }

  public static CDOrbitalType convertStringToOrbitalType(String value) throws IOException {
    return (CDOrbitalType) convertStringToEnum(value, CDXMLOrbitalType);
  }

  public static String convertSymbolTypeToString(CDSymbolType value) throws IOException {
    return convertEnumToString(value, CDXMLSymbolType);
  }

  public static CDSymbolType convertStringToSymbolType(String value) throws IOException {
    return (CDSymbolType) convertStringToEnum(value, CDXMLSymbolType);
  }

  public static String convertBracketUsageToString(CDBracketUsage value) throws IOException {
    return convertEnumToString(value, CDXMLBracketUsage);
  }

  public static CDBracketUsage convertStringToBracketUsage(String value) throws IOException {
    return (CDBracketUsage) convertStringToEnum(value, CDXMLBracketUsage);
  }

  public static String convertPolymerRepeatPatternToString(CDPolymerRepeatPattern value) throws IOException {
    return convertEnumToString(value, CDXMLPolymerRepeatPattern);
  }

  public static CDPolymerRepeatPattern convertStringToPolymerRepeatPattern(String value) throws IOException {
    return (CDPolymerRepeatPattern) convertStringToEnum(value, CDXMLPolymerRepeatPattern);
  }

  public static String convertPolymerFlipTypeToString(CDPolymerFlipType value) throws IOException {
    return convertEnumToString(value, CDXMLPolymerFlipType);
  }

  public static CDPolymerFlipType convertStringToPolymerFlipType(String value) throws IOException {
    return (CDPolymerFlipType) convertStringToEnum(value, CDXMLPolymerFlipType);
  }

  public static String convertGeometricFeatureToString(CDGeometryType value) throws IOException {
    return convertEnumToString(value, CDXMLGeometricFeature);
  }

  public static CDGeometryType convertStringToGeometricFeature(String value) throws IOException {
    return (CDGeometryType) convertStringToEnum(value, CDXMLGeometricFeature);
  }

  public static String convertConstraintTypeToString(CDConstraintType value) throws IOException {
    return convertEnumToString(value, CDXMLConstraintType);
  }

  public static CDConstraintType convertStringToConstraintType(String value) throws IOException {
    return (CDConstraintType) convertStringToEnum(value, CDXMLConstraintType);
  }

  public static String convertSpectrumXTypeToString(CDSpectrumXType value) throws IOException {
    return convertEnumToString(value, CDXMLSpectrumXType);
  }

  public static CDSpectrumXType convertStringToSpectrumXType(String value) throws IOException {
    return (CDSpectrumXType) convertStringToEnum(value, CDXMLSpectrumXType);
  }

  public static String convertSpectrumYTypeToString(CDSpectrumYType value) throws IOException {
    return convertEnumToString(value, CDXMLSpectrumYType);
  }

  public static CDSpectrumYType convertStringToSpectrumYType(String value) throws IOException {
    return (CDSpectrumYType) convertStringToEnum(value, CDXMLSpectrumYType);
  }

  public static String convertSpectrumClassToString(CDSpectrumClass value) throws IOException {
    return convertEnumToString(value, CDXMLSpectrumClass);
  }

  public static CDSpectrumClass convertStringToSpectrumClass(String value) throws IOException {
    return (CDSpectrumClass) convertStringToEnum(value, CDXMLSpectrumClass);
  }

  public static String convertObjectTagTypeToString(CDObjectTagType value) throws IOException {
    return convertEnumToString(value, CDXMLObjectTagType);
  }

  public static CDObjectTagType convertStringToObjectTagType(String value) throws IOException {
    return (CDObjectTagType) convertStringToEnum(value, CDXMLObjectTagType);
  }

  public static String convertPositioningTypeToString(CDPositioningType value) throws IOException {
    return convertEnumToString(value, CDXMLPositioningType);
  }

  public static CDPositioningType convertStringToPositioningType(String value) throws IOException {
    return (CDPositioningType) convertStringToEnum(value, CDXMLPositioningType);
  }

  public static String convertSideTypeToString(CDSideType value) throws IOException {
    return convertEnumToString(value, CDXMLSideType);
  }

  public static CDSideType convertStringToSideType(String value) throws IOException {
    return (CDSideType) convertStringToEnum(value, CDXMLSideType);
  }

  public static String convertSequenceTypeToString(CDSequenceType value) throws IOException {
    return convertEnumToString(value, CDXMLSequenceType);
  }

  public static CDSequenceType convertStringToSequenceType(String value) throws IOException {
    return (CDSequenceType) convertStringToEnum(value, CDXMLSequenceType);
  }

  public static String convertCharSetToString(CDCharSet value) throws IOException {
    return convertEnumToString(value, CDXMLCharSet);
  }

  public static CDCharSet convertStringToCharSet(String value) throws IOException {
    return (CDCharSet) convertStringToEnum(value, CDXMLCharSet);
  }

  private static String convertEnumToString(Enum<?> value, Object[][] table) throws IOException {
    for (Object[] entry : table) {
      if (entry[0] == value) {
        return (String) entry[1];
      }
    }
    throw new IOException("Cannot convert enum " + value + " to string");
  }

  @SuppressWarnings("unchecked")
  private static Enum convertStringToEnum(String value, Object[][] table) throws IOException {
    for (Object[] entry : table) {
      if (entry[1].equals(value)) {
        return (Enum<?>) entry[0];
      }
    }
    throw new IOException("Value not recognized: " + value);
  }

  public static String convertByteArrayToString(byte[] value) {
    StringBuilder sb = new StringBuilder();
    for (byte b : value) {
      String string = Integer.toHexString(b & 0xff);
      if (string.length() > 2) {
        string = string.substring(0, 2);
      } else if (string.length() < 2) {
        string = "00".substring(string.length()) + string;
      }
      sb.append(string);
    }
    return sb.toString();
  }

  public static byte[] convertStringToByteArray(String value) {
    byte[] array = new byte[value.length() / 2];
    for (int offset = 0; offset < value.length(); offset += 2) {
      array[offset / 2] = (byte) Integer.parseInt(value.substring(offset, offset + 2), 16);
    }
    return array;
  }

  public static String convertIntListToString(List<Integer> value) {
    StringBuilder sb = new StringBuilder();
    for (int integer : value) {
      appendSeparator(sb);
      sb.append(integer);
    }
    return sb.toString();
  }

  public static List<Integer> convertStringToIntList(String value) {
    List<String> stringList = convertStringToStringList(value);
    List<Integer> list = new ArrayList<>(stringList.size());
    for (String element : stringList) {
      list.add(Integer.parseInt(element));
    }
    return list;
  }

  private static List<String> convertStringToStringList(String value) {
    List<String> list = new ArrayList<>(Arrays.asList(value.split(" ")));
    // remove empty elements
    for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
      String string = iterator.next();
      if (string == null || string.length() == 0) {
        iterator.remove();
      }
    }
    return list;
  }

  public static String convertRectangleToString(CDRectangle value) {
    return value.getLeft() + " " + value.getTop() + " " + value.getRight() + " " + value.getBottom();
  }

  public static CDRectangle convertStringToRectangle(String value) {
    List<String> list = convertStringToStringList(value);
    CDRectangle rectangle = new CDRectangle();
    rectangle.setLeft(Float.parseFloat(list.get(0)));
    rectangle.setTop(Float.parseFloat(list.get(1)));
    rectangle.setRight(Float.parseFloat(list.get(2)));
    rectangle.setBottom(Float.parseFloat(list.get(3)));
    return rectangle;
  }

  public static String convertPoint2DToString(CDPoint2D value) {
    return value.getX() + " " + value.getY();
  }

  public static CDPoint2D convertStringToPoint2D(String value) {
    List<String> list = convertStringToStringList(value);
    CDPoint2D point = new CDPoint2D();
    point.setX(Float.parseFloat(list.get(0)));
    point.setY(Float.parseFloat(list.get(1)));
    return point;
  }

  public static String convertPoint2DListToString(List<CDPoint2D> value) {
    StringBuilder sb = new StringBuilder();
    for (CDPoint2D element : value) {
      appendSeparator(sb);
      sb.append(convertPoint2DToString(element));
    }
    return sb.toString();
  }

  public static List<CDPoint2D> convertStringToPoint2DArray(String value) {
    List<String> list = convertStringToStringList(value);
    int count = list.size() / 2;
    List<CDPoint2D> pointList = new ArrayList<>(count);
    for (int i = 0, offset = 0; i < count; i++, offset += 2) {
      CDPoint2D point = new CDPoint2D();
      point.setX(Float.parseFloat(list.get(offset)));
      point.setY(Float.parseFloat(list.get(offset + 1)));
      pointList.add(point);
    }
    return pointList;
  }

  public static String convertPoint3DToString(CDPoint3D value) {
    return value.getX() + " " + value.getY() + " " + value.getZ();
  }

  public static CDPoint3D convertStringToPoint3D(String value) {
    List<String> list = convertStringToStringList(value);
    CDPoint3D point = new CDPoint3D();
    point.setX(Float.parseFloat(list.get(0)));
    point.setY(Float.parseFloat(list.get(1)));
    point.setZ(Float.parseFloat(list.get(2)));
    return point;
  }

  public static String convertPoint3DListToString(List<CDPoint3D> value) {
    StringBuilder sb = new StringBuilder();
    for (CDPoint3D element : value) {
      appendSeparator(sb);
      sb.append(convertPoint3DToString(element));
    }
    return sb.toString();
  }

  public static List<CDPoint3D> convertStringToPoint3DArray(String value) {
    List<String> list = convertStringToStringList(value);
    int count = list.size() / 3;
    List<CDPoint3D> pointList = new ArrayList<>(count);
    for (int i = 0, offset = 0; i < count; i++, offset += 3) {
      CDPoint3D point = new CDPoint3D();
      point.setX(Float.parseFloat(list.get(offset)));
      point.setY(Float.parseFloat(list.get(offset + 1)));
      point.setZ(Float.parseFloat(list.get(offset + 2)));
      pointList.add(point);
    }
    return pointList;
  }

  public static String convertElementListToString(CDElementList value) {
    StringBuilder sb = new StringBuilder();
    if (value.isExclusive()) {
      sb.append("NOT");
    }
    for (int element : value.getElements()) {
      appendSeparator(sb);
      sb.append(element);
    }
    return sb.toString();
  }

  public static CDElementList convertStringToElementList(String value) {
    List<String> stringList = convertStringToStringList(value);
    CDElementList list = new CDElementList();
    if (!stringList.isEmpty() && stringList.get(0).equalsIgnoreCase("NOT")) {
      list.setExclusive(true);
      stringList.remove(0);
    }
    for (String element : stringList) {
      list.getElements().add(Integer.parseInt(element));
    }
    return list;
  }

  public static String convertGenericListToString(CDGenericList value) {
    StringBuilder sb = new StringBuilder();
    if (value.isExclusive()) {
      sb.append("NOT");
    }
    for (String element : value.getElements()) {
      appendSeparator(sb);
      sb.append(element);
    }
    return sb.toString();
  }

  public static CDGenericList getAttributeAsGenericList(String value) {
    List<String> stringList = convertStringToStringList(value);
    CDGenericList list = new CDGenericList();
    if (!stringList.isEmpty() && stringList.get(0).equalsIgnoreCase("NOT")) {
      list.setExclusive(true);
      stringList.remove(0);
    }
    list.getElements().addAll(stringList);
    return list;
  }

  private static void appendSeparator(StringBuilder sb) {
    if (sb.length() > 0) {
      sb.append(" ");
    }
  }

  public static String convertDateToString(Date value) {
    DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
    return dateFormat.format(value);
  }

  public static Date convertStringToDate(String value) {
    DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
    try {
      return dateFormat.parse(value);
    } catch (ParseException e) {
      logger.warn("Cannot convert date " + value, e);
      return null;
    }
  }

  public static String convertLineHeightToString(float value) {
    if (value == CDSettings.LineHeight_Variable) {
      // default
      return "variable";
    } else if (value == CDSettings.LineHeight_Automatic) {
      return "auto";
    } else {
      return String.valueOf(value);
    }
  }

  public static float convertStringToLineHeight(String value) {
    if (value.equals("variable")) {
      return CDSettings.LineHeight_Variable;
    }
    if (value.equals("auto")) {
      return CDSettings.LineHeight_Automatic;
    }
    return (int) Float.parseFloat(value);
  }

  public static String convertObjectRefToString(Object value, Map<Object,Integer> references) throws IOException {
    if (references.get(value) == null) {
      throw new IOException("Reference wasn't collected in the first place");
    }
    return String.valueOf(references.get(value));
  }

  @SuppressWarnings("unchecked")
  public static <T> T convertStringToObjectRef(String value, Class<T> clazz, RefManager refManager) throws IOException {
    return refManager.getObjectRef(Integer.parseInt(value), clazz, CDXMLReader.RIGID);
  }

  public static String convertObjectRefList(List<?> value, Map<Object,Integer> references) {
    StringBuilder sb = new StringBuilder();
    for (Object element : value) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(references.get(element) != null ? references.get(element) : "0");
    }
    return sb.toString();
  }

  public static <T> List<T> convertStringToObjectRefList(String value, Class<T> clazz, RefManager refManager) throws IOException {
    List<Integer> intList = convertStringToIntList(value);
    List<T> array = new ArrayList<>(intList.size());
    for (Integer ref : intList) {
      T object = refManager.getObjectRef(ref, clazz, CDXMLReader.RIGID);
      if (object != null) {
        array.add(object);
      }
    }
    return array;
  }

  public static String convertObjectRefMapToString(Map<?,?> values, Map<Object,Integer> references) {
    StringBuilder sb = new StringBuilder();
    for (Object key : values.keySet()) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(references.get(key) != null ? references.get(key) : "0");
      sb.append(" ");
      sb.append(references.get(values.get(key)) != null ? references.get(values.get(key)) : "0");
    }
    return sb.toString();
  }

  public static <K,V> Map<K,V> convertStringtoObjectRefMap(String value, Class<K> clazz1, Class<V> clazz2, RefManager refManager)
    throws IOException {
    List<Integer> intList = convertStringToIntList(value);
    if (intList.size() % 2 != 0) {
      throw new IOException("Cannot calculate map length for size of " + intList.size());
    }
    Map<K,V> map = new HashMap<>();
    for (int i = 0; i < intList.size(); i += 2) {
      K keyObject = refManager.getObjectRef(intList.get(i), clazz1, CDXMLReader.RIGID);
      V valueObject = refManager.getObjectRef(intList.get(i + 1), clazz2, CDXMLReader.RIGID);
      if (keyObject != null && valueObject != null) {
        map.put(keyObject, valueObject);
      }
    }
    return map;
  }

  public static String convertArrowheadTypeToString(CDArrowHeadType value) throws IOException {
    return convertEnumToString(value, CDXMLArrowheadType);
  }

  public static CDArrowHeadType convertStringToArrowheadType(String value) throws IOException {
    return (CDArrowHeadType) convertStringToEnum(value, CDXMLArrowheadType);
  }

  public static String convertArrowheadToString(CDArrowHeadPositionType value) throws IOException {
    return convertEnumToString(value, CDXMLArrowhead);
  }

  public static CDArrowHeadPositionType convertStringToArrowhead(String value) throws IOException {
    return (CDArrowHeadPositionType) convertStringToEnum(value, CDXMLArrowhead);
  }

  public static String convertFillTypeToString(CDFillType value) throws IOException {
    return convertEnumToString(value, CDXMLFillType);
  }

  public static CDFillType convertStringToFillType(String value) throws IOException {
    return (CDFillType) convertStringToEnum(value, CDXMLFillType);
  }

  public static String convertNoGoTypeToString(CDNoGoType value) throws IOException {
    return convertEnumToString(value, CDXMLNoGoType);
  }

  public static CDNoGoType convertStringToNoGoType(String value) throws IOException {
    return (CDNoGoType) convertStringToEnum(value, CDXMLNoGoType);
  }

}