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
package org.beilstein.chemxtract.cdx.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to store chunks of text with different text styles.
 */
public class CDStyledString {
  /** Text chunks. */
  private List<CDXChunk> chunks = new ArrayList<>();

  public List<CDXChunk> getChunks() {
    return chunks;
  }

  public void setChunks(List<CDXChunk> chunks) {
    this.chunks = chunks;
  }

  public String getText() {
    StringBuilder sb = new StringBuilder();
    for (CDXChunk chunk : getChunks()) {
      sb.append(chunk.getText());
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "StyledString:\"" + getText() + "\"";
  }

  /**
   * This class stores a chunk of text and additional style attributes.
   */
  public static class CDXChunk {
    /** Text font. */
    private final CDFont font;
    /** Font size. */
    private final float fontSize;
    /** Font face. */
    private final CDFontFace fontType;
    /** Text color. */
    private final CDColor color;
    /** Text. */
    private final String text;

    public CDXChunk(CDFont font, float fontSize, CDFontFace fontType, CDColor color, String text) {
      this.font = font;
      this.fontSize = fontSize;
      this.fontType = fontType;
      this.color = color;
      this.text = text;
    }

    public CDFont getFont() {
      return font;
    }

    public float getFontSize() {
      return fontSize;
    }

    public CDFontFace getFontType() {
      return fontType;
    }

    public CDColor getColor() {
      return color;
    }

    public String getText() {
      return text;
    }
  }
}