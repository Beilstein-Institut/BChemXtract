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

import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDFont;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;

/** This class holds the information about font style of CDX files. */
public class CDXFontStyle {
  /** Font. */
  private CDFont font;

  /** Font size. */
  private float size;

  /** Font face. */
  private CDFontFace fontType;

  /** Font color. */
  private CDColor color;

  public CDXFontStyle(CDFont font, float size, CDFontFace fontType, CDColor color) {
    this.font = font;
    this.size = size;
    this.fontType = fontType;
    this.color = color;
  }

  public CDFont getFont() {
    return font;
  }

  public float getSize() {
    return size;
  }

  public CDFontFace getFontType() {
    return fontType;
  }

  public CDColor getColor() {
    return color;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + ((font == null) ? 0 : font.hashCode());
    result = prime * result + ((fontType == null) ? 0 : fontType.hashCode());
    result = prime * result + Float.floatToIntBits(size);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CDXFontStyle other = (CDXFontStyle) obj;
    if (color == null) {
      if (other.color != null) {
        return false;
      }
    } else if (!color.equals(other.color)) {
      return false;
    }
    if (font == null) {
      if (other.font != null) {
        return false;
      }
    } else if (!font.equals(other.font)) {
      return false;
    }
    if (fontType == null) {
      if (other.fontType != null) {
        return false;
      }
    } else if (!fontType.equals(other.fontType)) {
      return false;
    }
    if (Float.floatToIntBits(size) != Float.floatToIntBits(other.size)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FontStyle[");
    sb.append("font=");
    sb.append(getFont());
    sb.append(",size=");
    sb.append(getSize());
    sb.append(",fonttype=");
    sb.append(getFontType());
    sb.append(",color=");
    sb.append(getColor());
    sb.append("]");
    return sb.toString();
  }
}
