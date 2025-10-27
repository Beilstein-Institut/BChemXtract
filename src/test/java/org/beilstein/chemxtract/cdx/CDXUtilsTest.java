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

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.reader.CDXUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

public class CDXUtilsTest extends TestCase {

  public void testFixedPoint() {
    byte[] bytes = new byte[] { 0x66, (byte) 0xA6, 0x4C, 0x00 };
    assertEquals(76.65f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] { (byte) 0xF9, 0x1E, 0x54, 0x00 };
    assertEquals(84.12f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] { 0x77, (byte) 0xB7, 0x74, 0x00 };
    assertEquals(116.72f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] { 0x0C, (byte) 0xFA, 0x7B, 0x00 };
    assertEquals(123.98f, CDXUtils.readFixedPoint(bytes, 0), 0.01f);

    bytes = new byte[] { 0x00, 0x00, (byte) 0x48, 0x00 };
    assertEquals(72f, CDXUtils.readFixedPoint(bytes, 0), 0.1f);

    bytes = new byte[] { 0x00, 0x00, (byte) 0x90, 0x00 };
    assertEquals(144f, CDXUtils.readFixedPoint(bytes, 0), 0.1f);
  }

  public void testFloat64() {
    byte[] bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    assertEquals(0.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x24, 0x40 };
    assertEquals(10.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE0, 0x6F, 0x40 };
    assertEquals(255.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x70, 0x40 };
    assertEquals(256.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, (byte) 0x8F, 0x40 };
    assertEquals(1000.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x6A, (byte) 0xF8, 0x40 };
    assertEquals(100000.0, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xE0, 0x3F };
    assertEquals(0.5, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { (byte) 0xFC, (byte) 0xA9, (byte) 0xF1, (byte) 0xD2, 0x4D, 0x62, 0x50, 0x3F };
    assertEquals(0.001, CDXUtils.readFloat64(bytes, 0), 0.001);

    bytes = new byte[] { (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xDC, 0x5E, (byte) 0xC0 };
    assertEquals(-123.45, CDXUtils.readFloat64(bytes, 0), 0.001);
  }

  public void testInt16() {
    byte[] bytes = new byte[] { 0x00, 0x00 };
    assertEquals(0, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] { 0x0A, 0x00 };
    assertEquals(10, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, 0x00 };
    assertEquals(255, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] { 0x00, 0x01 };
    assertEquals(256, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xE8, 0x03 };
    assertEquals(1000, CDXUtils.readInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, (byte) 0xFF };
    assertEquals(-1, CDXUtils.readInt16(bytes, 0));
  }

  public void testInt32() {
    byte[] bytes = new byte[] { 0x00, 0x00, 0x00, 0x00 };
    assertEquals(0, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { 0x0A, 0x00, 0x00, 0x00 };
    assertEquals(10, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, 0x00, 0x00, 0x00 };
    assertEquals(255, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { 0x00, 0x01, 0x00, 0x00 };
    assertEquals(256, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xE8, 0x03, 0x00, 0x00 };
    assertEquals(1000, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xA0, (byte) 0x86, 0x01, 0x00 };
    assertEquals(100000, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F };
    assertEquals(2147483647, CDXUtils.readInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
    assertEquals(-1L, CDXUtils.readInt32(bytes, 0));
  }

  public void testInt8() {
    byte[] bytes = new byte[] { 0x00 };
    assertEquals(0, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { 0x0A };
    assertEquals(10, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { 0x7F };
    assertEquals(127, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { (byte) 0xFD };
    assertEquals(-3, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { (byte) 0xFE };
    assertEquals(-2, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { (byte) 0xFF };
    assertEquals(-1, CDXUtils.readInt8(bytes, 0));
  }

  public void testUInt16() {
    byte[] bytes = new byte[] { 0x00, 0x00 };
    assertEquals(0, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] { 0x0A, 0x00 };
    assertEquals(10, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, 0x00 };
    assertEquals(255, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] { 0x00, 0x01 };
    assertEquals(256, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xE8, 0x03 };
    assertEquals(1000, CDXUtils.readUInt16(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, (byte) 0xFF };
    assertEquals(65535, CDXUtils.readUInt16(bytes, 0));
  }

  public void testUInt32() {
    byte[] bytes = new byte[] { 0x00, 0x00, 0x00, 0x00 };
    assertEquals(0, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { 0x0A, 0x00, 0x00, 0x00 };
    assertEquals(10, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, 0x00, 0x00, 0x00 };
    assertEquals(255, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { 0x00, 0x01, 0x00, 0x00 };
    assertEquals(256, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xE8, 0x03, 0x00, 0x00 };
    assertEquals(1000, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xA0, (byte) 0x86, 0x01, 0x00 };
    assertEquals(100000, CDXUtils.readUInt32(bytes, 0));

    bytes = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
    assertEquals(4294967295L, CDXUtils.readUInt32(bytes, 0));
  }

  public void testUInt8() {
    byte[] bytes = new byte[] { 0x00 };
    assertEquals(0, CDXUtils.readInt8(bytes, 0));

    bytes = new byte[] { 0x0A };
    assertEquals(10, CDXUtils.readUInt8(bytes, 0));

    bytes = new byte[] { 0x7F };
    assertEquals(127, CDXUtils.readUInt8(bytes, 0));

    bytes = new byte[] { (byte) 0xFF };
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

}
