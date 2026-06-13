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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.datatypes.CDCharSet;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDElementList;
import org.beilstein.chemxtract.cdx.datatypes.CDFont;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDGenericList;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint3D;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to store intermediate values during the reading of CDX files. This class is
 * only used by the {@link CDXReader}.
 */
public class CDXProperty {
  private static final Logger LOGGER = LoggerFactory.getLogger(CDXProperty.class);

  /** Fallback charset for CDX text whose charset is unknown or unsupported. */
  private static final Charset CDX_FALLBACK_CHARSET = Charset.forName("windows-1252");

  /** Tag of the property. */
  private int tag;

  /** Length of the property in the stream in bytes. */
  private int length;

  /** Content of the property. */
  private byte[] data;

  /** Position of the property in the stream. */
  private int position;

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getTag() {
    return tag;
  }

  public void setTag(int tag) {
    this.tag = tag;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public boolean getDataAsBoolean() throws IOException {
    if (length != 0 && length != 1) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size: 0 or 1 at "
              + getPositionAsString());
    }
    if (length == 0) {
      return true;
    }
    return CDXUtils.readUInt8(data, 0) > 0;
  }

  public int getDataAsUInt() throws IOException {
    if (length != 1 && length != 2 && length != 4) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size: 1, 2 or 4 at "
              + getPositionAsString());
    }

    if (length == 4) {
      return CDXUtils.readInt32(data, 0);
    }

    if (length == 2) {
      return CDXUtils.readUInt16(data, 0);
    }

    return CDXUtils.readUInt8(data, 0);
  }

  public int getDataAsInt() throws IOException {
    if (length != 1 && length != 2 && length != 4) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size: 1, 2 or 4 at "
              + getPositionAsString());
    }

    if (length == 4) {
      return CDXUtils.readInt32(data, 0);
    }

    if (length == 2) {
      return CDXUtils.readInt16(data, 0);
    }

    return CDXUtils.readInt8(data, 0);
  }

  public int getDataAsUInt8() throws IOException {
    checkPropSize(1);
    return CDXUtils.readUInt8(data, 0);
  }

  public int getDataAsInt8() throws IOException {
    checkPropSize(1);
    return CDXUtils.readInt8(data, 0);
  }

  public int getDataAsUInt16() throws IOException {
    checkPropSize(2);
    return CDXUtils.readUInt16(data, 0);
  }

  public int getDataAsInt16() throws IOException {
    checkPropSize(2);
    return CDXUtils.readInt16(data, 0);
  }

  public int getDataAsInt32() throws IOException {
    checkPropSize(4);
    return CDXUtils.readInt32(data, 0);
  }

  public long getDataAsInt64() throws IOException {
    checkPropSize(8);
    return CDXUtils.readInt64(data, 0);
  }

  public double getDataAsFloat64() throws IOException {
    return Double.longBitsToDouble(getDataAsInt64());
  }

  public double[] getDataAsFloat64Array() throws IOException {
    if (length % 8 != 0) {
      throw new IOException("Property size unexpected:" + length + " at " + getPositionAsString());
    }
    double[] array = new double[length / 8];
    for (int i = 0; i < length; i += 8) {
      array[i / 8] = CDXUtils.readFloat64(data, i);
    }
    return array;
  }

  public List<Integer> getDataAsInt16ListWithCounts() throws IOException {
    int count = CDXUtils.readUInt16(data, 0);
    if (count * 2 + 2 != length) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size:"
              + count * 2
              + 2
              + " at "
              + getPositionAsString());
    }
    List<Integer> array = new ArrayList<>(count);
    for (int i = 2; i < length; i += 2) {
      array.add((int) CDXUtils.readInt16(data, i));
    }
    return array;
  }

  public int[] getDataAsInt16Array() throws IOException {
    if (length % 2 != 0) {
      throw new IOException("Property size unexpected:" + length + " at " + getPositionAsString());
    }
    int[] array = new int[length / 2];
    for (int i = 0; i < length; i += 2) {
      array[i / 2] = CDXUtils.readInt16(data, i);
    }
    return array;
  }

  public float getDataAsCoordinate() throws IOException {
    if (length != 4) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size: 4 at "
              + getPositionAsString());
    }
    return readCoordinate(0);
  }

  public CDPoint2D getDataAsPoint2D() throws IOException {
    checkPropSize(8);
    CDPoint2D point2D = new CDPoint2D();
    // Y-coordinate first !!
    point2D.setY(readCoordinate(0));
    point2D.setX(readCoordinate(4));
    return point2D;
  }

  public CDPoint3D getDataAsPoint3D(boolean acendingOrder) throws IOException {
    checkPropSize(12);
    CDPoint3D point3D = new CDPoint3D();
    if (acendingOrder) {
      point3D.setX(readCoordinate(0));
      point3D.setY(readCoordinate(4));
      point3D.setZ(readCoordinate(8));
    } else {
      // Z-coordinate first, then Y and X !!
      point3D.setZ(readCoordinate(0));
      point3D.setY(readCoordinate(4));
      point3D.setX(readCoordinate(8));
    }
    return point3D;
  }

  public CDRectangle getDataAsRectangle() throws IOException {
    checkPropSize(16);
    CDRectangle rectangle = new CDRectangle();
    rectangle.setTop(readCoordinate(0));
    rectangle.setLeft(readCoordinate(4));
    rectangle.setBottom(readCoordinate(8));
    rectangle.setRight(readCoordinate(12));
    return rectangle;
  }

  public List<CDPoint2D> getDataAsPoint2DArray() throws IOException {
    int count = CDXUtils.readUInt16(data, 0);
    if (count * 8 + 2 != length) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size:"
              + count * 8
              + 2
              + " at "
              + getPositionAsString());
    }
    List<CDPoint2D> array = new ArrayList<>(count);
    for (int i = 2; i < length; i += 8) {
      array.add(readPoint2D(i));
    }
    return array;
  }

  public List<CDPoint3D> getDataAsPoint3DArray() throws IOException {
    int count = CDXUtils.readUInt16(data, 0);
    if (count * 12 + 2 != length) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size:"
              + count * 12
              + 2
              + " at "
              + getPositionAsString());
    }
    List<CDPoint3D> array = new ArrayList<>(count);
    for (int i = 2; i < length; i += 12) {
      array.add(readPoint3D(i));
    }
    return array;
  }

  private <T> T readObjectByRef(Class<T> clazz, int offset, RefManager refManager)
      throws IOException {
    int id = CDXUtils.readInt32(data, offset);
    return refManager.getObjectRef(id, clazz, CDXReader.RIGID);
  }

  public <T> T getDataAsObjectRef(Class<T> clazz, RefManager refManager) throws IOException {
    checkPropSize(4);
    return readObjectByRef(clazz, 0, refManager);
  }

  public <T> List<T> getDataAsObjectRefArray(Class<T> clazz, RefManager refManager)
      throws IOException {
    if (length % 4 != 0) {
      throw new IOException(
          "Cannot calculate array length for size of " + length + " at " + getPositionAsString());
    }
    List<T> array = new ArrayList<>(length / 4);
    for (int i = 0; i < length; i += 4) {
      T object = readObjectByRef(clazz, i, refManager);
      if (object != null) {
        array.add(object);
      }
    }
    return array;
  }

  public <T> List<T> getDataAsObjectRefArrayWithCounts(Class<T> clazz, RefManager refManager)
      throws IOException {
    int count = CDXUtils.readUInt16(data, 0);
    if (count * 4 + 2 != length) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size:"
              + count * 4
              + 2
              + " at "
              + getPositionAsString());
    }
    List<T> array = new ArrayList<>(count);
    for (int i = 2; i < length; i += 4) {
      T object = readObjectByRef(clazz, i, refManager);
      if (object != null) {
        array.add(object);
      }
    }
    return array;
  }

  public <K, V> Map<K, V> getDataObjectRefMap(
      Class<K> clazz1, Class<V> clazz2, RefManager refManager) throws IOException {
    if (length % 8 != 0) {
      throw new IOException(
          "Cannot calculate map length for size of " + length + " at " + getPositionAsString());
    }
    Map<K, V> map = new HashMap<>();
    for (int i = 0; i < length; i += 8) {
      K keyObject = readObjectByRef(clazz1, i, refManager);
      V valueObject = readObjectByRef(clazz2, i + 4, refManager);
      if (keyObject != null && valueObject != null) {
        map.put(keyObject, valueObject);
      }
    }
    return map;
  }

  public Date getDataAsDate() throws IOException {
    checkPropSize(14);
    int year = CDXUtils.readInt16(data, 0);
    int month = CDXUtils.readInt16(data, 2);
    int day = CDXUtils.readInt16(data, 4);
    int hour = CDXUtils.readInt16(data, 6);
    int minute = CDXUtils.readInt16(data, 8);
    int second = CDXUtils.readInt16(data, 10);
    // Match the legacy Date(year, month, day, h, m, s) semantics: year is offset from 1900,
    // month is 0-based — both already true for the values written by CDX.
    return new GregorianCalendar(year + 1900, month, day, hour, minute, second).getTime();
  }

  public String getDataAsString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append((char) CDXUtils.readUInt8(data, i));
    }
    return sb.toString();
  }

  public CDFontFace getDataAsFontFace() throws IOException {
    checkPropSize(2);
    return readFontFace(0);
  }

  public CDXFontStyle getDataAsFontStyle(Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors)
      throws IOException {
    checkPropSize(8);
    return readFontStyle(0, fonts, colors);
  }

  public CDFont getDataAsFontRef(Map<Integer, CDFont> fonts) throws IOException {
    checkPropSize(2);
    return readFontRef(0, fonts);
  }

  public CDColor getDataAsColorRef(Map<Integer, CDColor> colors) throws IOException {
    if (length != 2 && length != 4) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size: 2 or 4 at "
              + getPositionAsString());
    }

    return readColorRef(0, length, colors);
  }

  public CDStyledString getDataAsStyledString(
      Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors) throws IOException {
    return readStyledString(0, length, fonts, colors);
  }

  public String getDataAsUnstyledString(Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors)
      throws IOException {
    CDStyledString styledString = getDataAsStyledString(fonts, colors);
    if (styledString.getChunks().size() > 1) {
      throw new IOException(
          "String contains unexcpected count of style " + styledString.getChunks().size());
    }
    return styledString.getText();
  }

  public Map<String, Object> getDataAsRepresentsProperties(RefManager refManager)
      throws IOException {
    if (length % 6 != 0 && length >= 6) {
      throw new IOException("Property size unexpected:" + length);
    }

    Map<String, Object> properties = new HashMap<>();
    for (int i = 0; i < length; i += 6) {
      // read object id
      Object object = readObjectByRef(Object.class, i, refManager);
      if (object == null) {
        throw new IOException("Found null object as repesent value");
      }

      int tag = CDXUtils.readUInt16(data, i + 4);

      switch (tag) {
        case CDXConstants.CDXProp_Atom_Charge:
          properties.put("Charge", object);
          break;
        case CDXConstants.CDXProp_Atom_Radical:
          properties.put("Radical", object);
          break;
        default:
          throw new IOException(
              "Tag 0x"
                  + Integer.toHexString(tag)
                  + " not recognized at "
                  + i
                  + "(0x"
                  + Integer.toHexString(i)
                  + ")");
      }
    }
    return properties;
  }

  private void checkPropSize(int size) throws IOException {
    if (size != length) {
      throw new IOException(
          "Property size doesn't match, current size:"
              + length
              + " expected size:"
              + size
              + " at "
              + getPositionAsString());
    }
  }

  private float readCoordinate(int offset) {
    return CDXUtils.readFixedPoint(data, offset);
  }

  private CDPoint2D readPoint2D(int offset) {
    CDPoint2D point2D = new CDPoint2D();
    // Y-coordinate first !!
    point2D.setY(readCoordinate(offset + 0));
    point2D.setX(readCoordinate(offset + 4));
    return point2D;
  }

  private CDPoint3D readPoint3D(int offset) {
    CDPoint3D point3D = new CDPoint3D();
    // Z-coordinate first, then Y and X !!
    point3D.setZ(readCoordinate(offset + 0));
    point3D.setY(readCoordinate(offset + 4));
    point3D.setX(readCoordinate(offset + 8));
    return point3D;
  }

  private CDFont readFontRef(int offset, Map<Integer, CDFont> fonts) {
    int index = CDXUtils.readUInt16(data, offset);
    CDFont font = fonts.get(index);
    if (font == null) {
      LOGGER.warn("Font {}(0x{}) not found", index, Integer.toHexString(index));
      font = new CDFont();
      font.setName("Arial");
      font.setCharSet(CDCharSet.Win31Latin1);
    }
    return font;
  }

  public Map<Integer, CDFont> getDataAsFontTable() throws IOException {
    int count = CDXUtils.readUInt16(data, 2);
    int position = 4;
    Map<Integer, CDFont> fonts = new HashMap<>();
    for (int i = 0; i < count; i++) {
      int id = CDXUtils.readUInt16(data, position);
      position += 2;
      CDCharSet charSet = readCharSet(position);
      position += 2;
      int chars = CDXUtils.readUInt16(data, position);
      position += 2;
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < chars; j++) {
        sb.append((char) CDXUtils.readUInt8(data, position));
        position++;
      }
      CDFont font = new CDFont();
      font.setCharSet(charSet);
      font.setName(sb.toString());
      fonts.put(id, font);
    }
    return fonts;
  }

  private CDFontFace readFontFace(int offset) {
    return CDXUtils.convertIntToFontFace(CDXUtils.readUInt16(data, offset));
  }

  private CDXFontStyle readFontStyle(
      int offset, Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors) throws IOException {
    CDFont font = readFontRef(offset, fonts);
    CDFontFace fontType = readFontFace(offset + 2);
    float size = CDXUtils.readUInt16(data, offset + 4) / 20f;
    CDColor color = readColorRef(offset + 6, 2, colors);
    return new CDXFontStyle(font, size, fontType, color);
  }

  private CDColor readColorRef(int offset, int length, Map<Integer, CDColor> colors)
      throws IOException {
    int index = 0;
    if (length == 2) {
      index = CDXUtils.readUInt16(data, offset);
    } else if (length == 4) {
      index = (int) CDXUtils.readUInt32(data, offset);
    } else {
      LOGGER.warn("Size of color reference not supported: {}", length);
      return null;
    }

    CDColor color = colors.get(index);

    if (color == null) {
      throw new IOException("Color " + index + "(0x" + Integer.toHexString(index) + ") not found");
    }
    return color;
  }

  public Map<Integer, CDColor> getDataAsColorTable() throws IOException {
    int count = CDXUtils.readUInt16(data, 0);
    if (count * 6 + 2 != length) {
      throw new IOException(
          "Unexpected count of color entries "
              + count
              + " for length "
              + length
              + " at "
              + getPositionAsString());
    }
    Map<Integer, CDColor> colors = new HashMap<>();
    colors.put(0, CDColor.BLACK);
    colors.put(1, CDColor.WHITE);
    colors.put(2, CDColor.WHITE);
    colors.put(3, CDColor.BLACK);
    colors.put(4, CDColor.RED);
    colors.put(5, CDColor.YELLOW);
    colors.put(6, CDColor.GREEN);
    colors.put(7, CDColor.CYAN);
    colors.put(8, CDColor.BLUE);
    colors.put(9, CDColor.MAGENTA);

    int index = 2;
    for (int i = 0, position = 2; i < count; i++, position += 6) {
      int red = CDXUtils.readUInt16(data, position);
      int green = CDXUtils.readUInt16(data, position + 2);
      int blue = CDXUtils.readUInt16(data, position + 4);
      CDColor color = new CDColor();
      color.setRed(red / 65536f);
      color.setGreen(green / 65536f);
      color.setBlue(blue / 65536f);
      colors.put(index++, color);
    }
    return colors;
  }

  private CDCharSet readCharSet(int offset) throws IOException {
    int charSet = CDXUtils.readUInt16(data, offset);
    switch (charSet) {
      case CDXConstants.CDXCharSetUnknown:
        return CDCharSet.Unknown;
      case CDXConstants.CDXCharSetEBCDICOEM:
        return CDCharSet.EBCDICOEM;
      case CDXConstants.CDXCharSetMSDOSUS:
        return CDCharSet.MSDOSUS;
      case CDXConstants.CDXCharSetEBCDIC500V1:
        return CDCharSet.EBCDIC500V1;
      case CDXConstants.CDXCharSetArabicASMO708:
        return CDCharSet.ArabicASMO708;
      case CDXConstants.CDXCharSetArabicASMO449P:
        return CDCharSet.ArabicASMO449P;
      case CDXConstants.CDXCharSetArabicTransparent:
        return CDCharSet.ArabicTransparent;
      case CDXConstants.CDXCharSetArabicTransparentASMO:
        return CDCharSet.ArabicTransparentASMO;
      case CDXConstants.CDXCharSetGreek437G:
        return CDCharSet.Greek437G;
      case CDXConstants.CDXCharSetBalticOEM:
        return CDCharSet.BalticOEM;
      case CDXConstants.CDXCharSetMSDOSLatin1:
        return CDCharSet.MSDOSLatin1;
      case CDXConstants.CDXCharSetMSDOSLatin2:
        return CDCharSet.MSDOSLatin2;
      case CDXConstants.CDXCharSetIBMCyrillic:
        return CDCharSet.IBMCyrillic;
      case CDXConstants.CDXCharSetIBMTurkish:
        return CDCharSet.IBMTurkish;
      case CDXConstants.CDXCharSetMSDOSPortuguese:
        return CDCharSet.MSDOSPortuguese;
      case CDXConstants.CDXCharSetMSDOSIcelandic:
        return CDCharSet.MSDOSIcelandic;
      case CDXConstants.CDXCharSetHebrewOEM:
        return CDCharSet.HebrewOEM;
      case CDXConstants.CDXCharSetMSDOSCanadianFrench:
        return CDCharSet.MSDOSCanadianFrench;
      case CDXConstants.CDXCharSetArabicOEM:
        return CDCharSet.ArabicOEM;
      case CDXConstants.CDXCharSetMSDOSNordic:
        return CDCharSet.MSDOSNordic;
      case CDXConstants.CDXCharSetMSDOSRussian:
        return CDCharSet.MSDOSRussian;
      case CDXConstants.CDXCharSetIBMModernGreek:
        return CDCharSet.IBMModernGreek;
      case CDXConstants.CDXCharSetThai:
        return CDCharSet.Thai;
      case CDXConstants.CDXCharSetEBCDIC:
        return CDCharSet.EBCDIC;
      case CDXConstants.CDXCharSetJapanese:
        return CDCharSet.Japanese;
      case CDXConstants.CDXCharSetChineseSimplified:
        return CDCharSet.ChineseSimplified;
      case CDXConstants.CDXCharSetKorean:
        return CDCharSet.Korean;
      case CDXConstants.CDXCharSetChineseTraditional:
        return CDCharSet.ChineseTraditional;
      case CDXConstants.CDXCharSetUnicodeISO10646:
        return CDCharSet.UnicodeISO10646;
      case CDXConstants.CDXCharSetWin31EasternEuropean:
        return CDCharSet.Win31EasternEuropean;
      case CDXConstants.CDXCharSetWin31Cyrillic:
        return CDCharSet.Win31Cyrillic;
      case CDXConstants.CDXCharSetWin31Latin1:
        return CDCharSet.Win31Latin1;
      case CDXConstants.CDXCharSetWin31Greek:
        return CDCharSet.Win31Greek;
      case CDXConstants.CDXCharSetWin31Turkish:
        return CDCharSet.Win31Turkish;
      case CDXConstants.CDXCharSetHebrew:
        return CDCharSet.Hebrew;
      case CDXConstants.CDXCharSetArabic:
        return CDCharSet.Arabic;
      case CDXConstants.CDXCharSetBaltic:
        return CDCharSet.Baltic;
      case CDXConstants.CDXCharSetVietnamese:
        return CDCharSet.Vietnamese;
      case CDXConstants.CDXCharSetKoreanJohab:
        return CDCharSet.KoreanJohab;
      case CDXConstants.CDXCharSetMacRoman:
        return CDCharSet.MacRoman;
      case CDXConstants.CDXCharSetMacJapanese:
        return CDCharSet.MacJapanese;
      case CDXConstants.CDXCharSetMacTradChinese:
        return CDCharSet.MacTradChinese;
      case CDXConstants.CDXCharSetMacKorean:
        return CDCharSet.MacKorean;
      case CDXConstants.CDXCharSetMacArabic:
        return CDCharSet.MacArabic;
      case CDXConstants.CDXCharSetMacHebrew:
        return CDCharSet.MacHebrew;
      case CDXConstants.CDXCharSetMacGreek:
        return CDCharSet.MacGreek;
      case CDXConstants.CDXCharSetMacCyrillic:
        return CDCharSet.MacCyrillic;
      case CDXConstants.CDXCharSetMacReserved:
        return CDCharSet.MacReserved;
      case CDXConstants.CDXCharSetMacDevanagari:
        return CDCharSet.MacDevanagari;
      case CDXConstants.CDXCharSetMacGurmukhi:
        return CDCharSet.MacGurmukhi;
      case CDXConstants.CDXCharSetMacGujarati:
        return CDCharSet.MacGujarati;
      case CDXConstants.CDXCharSetMacOriya:
        return CDCharSet.MacOriya;
      case CDXConstants.CDXCharSetMacBengali:
        return CDCharSet.MacBengali;
      case CDXConstants.CDXCharSetMacTamil:
        return CDCharSet.MacTamil;
      case CDXConstants.CDXCharSetMacTelugu:
        return CDCharSet.MacTelugu;
      case CDXConstants.CDXCharSetMacKannada:
        return CDCharSet.MacKannada;
      case CDXConstants.CDXCharSetMacMalayalam:
        return CDCharSet.MacMalayalam;
      case CDXConstants.CDXCharSetMacSinhalese:
        return CDCharSet.MacSinhalese;
      case CDXConstants.CDXCharSetMacBurmese:
        return CDCharSet.MacBurmese;
      case CDXConstants.CDXCharSetMacKhmer:
        return CDCharSet.MacKhmer;
      case CDXConstants.CDXCharSetMacThai:
        return CDCharSet.MacThai;
      case CDXConstants.CDXCharSetMacLao:
        return CDCharSet.MacLao;
      case CDXConstants.CDXCharSetMacGeorgian:
        return CDCharSet.MacGeorgian;
      case CDXConstants.CDXCharSetMacArmenian:
        return CDCharSet.MacArmenian;
      case CDXConstants.CDXCharSetMacSimpChinese:
        return CDCharSet.MacSimpChinese;
      case CDXConstants.CDXCharSetMacTibetan:
        return CDCharSet.MacTibetan;
      case CDXConstants.CDXCharSetMacMongolian:
        return CDCharSet.MacMongolian;
      case CDXConstants.CDXCharSetMacEthiopic:
        return CDCharSet.MacEthiopic;
      case CDXConstants.CDXCharSetMacCentralEuroRoman:
        return CDCharSet.MacCentralEuroRoman;
      case CDXConstants.CDXCharSetMacVietnamese:
        return CDCharSet.MacVietnamese;
      case CDXConstants.CDXCharSetMacExtArabic:
        return CDCharSet.MacExtArabic;
      case CDXConstants.CDXCharSetMacUninterpreted:
        return CDCharSet.MacUninterpreted;
      case CDXConstants.CDXCharSetMacIcelandic:
        return CDCharSet.MacIcelandic;
      case CDXConstants.CDXCharSetMacTurkish:
        return CDCharSet.MacTurkish;
      default:
        break;
    }
    throw new IOException(
        "Charset 0x" + Integer.toHexString(charSet) + " no recognized at " + getPositionAsString());
  }

  private String getPositionAsString() {
    return position + "(0x" + Integer.toHexString(position) + ")";
  }

  public CDElementList getDataAsElementList() throws IOException {
    if (length % 2 != 0) {
      throw new IOException("Property size unexpected:" + length + " at " + getPositionAsString());
    }
    CDElementList elementList = new CDElementList();
    int count = CDXUtils.readInt16(data, 0);
    if (count < 0) {
      elementList.setExclusive(true);
    }
    for (int i = 0; i < length; i += 2) {
      elementList.addElement(CDXUtils.readUInt16(data, i));
    }
    return elementList;
  }

  public CDGenericList getDataAsGenericList(
      Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors) throws IOException {
    CDGenericList genericList = new CDGenericList();
    int count = CDXUtils.readInt16(data, 0);
    if (count < 0) {
      count = -count;
      genericList.setExclusive(true);
    }

    int position = 2;
    for (int i = 0; i < count; i++) {
      int stringLength = CDXUtils.readUInt16(data, position);
      CDStyledString string = readStyledString(position + 2, stringLength, fonts, colors);
      genericList.addElement(string.getText());
      position += 4 + string.getChunks().size() * 10 + string.getText().length();
    }
    return genericList;
  }

  public CDStyledString readStyledString(
      int offset, int length, Map<Integer, CDFont> fonts, Map<Integer, CDColor> colors)
      throws IOException {
    int position = offset;
    int styles = CDXUtils.readUInt16(data, position);
    position += 2;
    int[] starts = new int[styles];
    CDXFontStyle[] fontStyles = new CDXFontStyle[styles];
    for (int i = 0; i < styles; i++) {
      starts[i] = CDXUtils.readUInt16(data, position);
      position += 2;
      fontStyles[i] = readFontStyle(position, fonts, colors);
      position += 8;
    }
    byte[] text = copyOfRange(data, position, offset + length);

    // sort font styles by their starting index
    boolean changed;
    do {
      changed = false;
      for (int i = 1; i < styles; i++) {
        if (starts[i - 1] > starts[i]) {
          int start = starts[i - 1];
          starts[i - 1] = starts[i];
          starts[i] = start;
          CDXFontStyle fontStyle = fontStyles[i - 1];
          fontStyles[i - 1] = fontStyles[i];
          fontStyles[i] = fontStyle;
          changed = true;
        }
      }
    } while (changed);

    CDStyledString string = new CDStyledString();
    for (int i = 0; i < styles - 1; i++) {
      if (starts[i] != starts[i + 1]) {
        byte[] bytes = copyOfRange(text, starts[i], starts[i + 1]);
        CDCharSet charSet = fontStyles[i].getFont().getCharSet();
        String charSetName = charSet != null ? charSet.getCharSet() : null;
        Charset fallback = null;
        if (charSet == CDCharSet.Unknown) {
          fallback = CDX_FALLBACK_CHARSET;
        } else if (charSetName == null) {
          LOGGER.warn("Unsupported charset {}", charSet);
          fallback = CDX_FALLBACK_CHARSET;
        }
        try {
          string.addChunk(
              new CDStyledString.CDXChunk(
                  fontStyles[i].getFont(),
                  fontStyles[i].getSize(),
                  fontStyles[i].getFontType(),
                  fontStyles[i].getColor(),
                  fallback != null ? new String(bytes, fallback) : new String(bytes, charSetName)));
        } catch (UnsupportedEncodingException exception) {
          LOGGER.warn("Found unsupported encoding; text chunk discarded.", exception);
        }
      }
    }
    if (starts.length > 0) {
      int last = starts.length - 1;
      if (starts[last] < text.length) {
        byte[] bytes = copyOfRange(text, starts[last], text.length);
        CDCharSet charSet = fontStyles[last].getFont().getCharSet();

        String charSetName = charSet != null ? charSet.getCharSet() : null;
        Charset fallback = null;
        if (charSet == CDCharSet.Unknown) {
          fallback = CDX_FALLBACK_CHARSET;
        } else if (charSetName == null) {
          LOGGER.warn("Unsupported charset {}", charSet);
          fallback = CDX_FALLBACK_CHARSET;
        }
        try {
          string.addChunk(
              new CDStyledString.CDXChunk(
                  fontStyles[last].getFont(),
                  fontStyles[last].getSize(),
                  fontStyles[last].getFontType(),
                  fontStyles[last].getColor(),
                  fallback != null ? new String(bytes, fallback) : new String(bytes, charSetName)));
        } catch (UnsupportedEncodingException exception) {
          LOGGER.warn("Found unsupported encoding; text chunk discarded.", exception);
        }
      }
    } else {
      string.addChunk(
          new CDStyledString.CDXChunk(
              fonts.get(0),
              12,
              new CDFontFace(),
              colors.get(0),
              new String(text, CDX_FALLBACK_CHARSET)));
    }
    return string;
  }

  private byte[] copyOfRange(byte[] data, int start, int end) {
    if (start < 0 || end < 0 || start > end) {
      throw new IllegalArgumentException("Invalid positions start=" + start + " end=" + end);
    }
    byte[] copy = new byte[end - start];

    int length = Math.min(data.length - start, end - start);
    System.arraycopy(data, start, copy, 0, length);

    return copy;
  }
}
