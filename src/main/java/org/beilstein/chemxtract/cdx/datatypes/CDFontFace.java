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

/** Font face definition. This class hold the various style attributes for text. */
public class CDFontFace {
  /** Bond text style attribute. */
  private boolean bold = false;

  /** Italic text style attribute. */
  private boolean italic = false;

  /** Underline text style attribute. */
  private boolean underline = false;

  /** Outline text style attribute. */
  private boolean outline = false;

  /** Shadow text style attribute. */
  private boolean shadow = false;

  /** Subscript text style attribute. */
  private boolean subscript = false;

  /** Superscript text style attribute. */
  private boolean superscript = false;

  /** Formula text style attribute. */
  private boolean formula = false;

  /**
   * Return true, if the font face has no additional style attributes.
   *
   * @return true, if the font face has no additional style attributes
   */
  public boolean isPlain() {
    return !isBold()
        && !isItalic()
        && !isUnderline()
        && !isOutline()
        && !isShadow()
        && !isSubscript()
        && !isSuperscript()
        && !isFormula();
  }

  /**
   * Remove all additional style attributes.
   *
   * @param plain True, if all additional style attributes should be removed
   */
  public void setPlain(boolean plain) {
    if (plain) {
      setBold(false);
      setItalic(false);
      setUnderline(false);
      setOutline(false);
      setShadow(false);
      setSubscript(false);
      setSuperscript(false);
      setFormula(false);
    }
  }

  public boolean isBold() {
    return bold;
  }

  public void setBold(boolean bold) {
    this.bold = bold;
  }

  public boolean isItalic() {
    return italic;
  }

  public void setItalic(boolean italic) {
    this.italic = italic;
  }

  public boolean isUnderline() {
    return underline;
  }

  public void setUnderline(boolean underline) {
    this.underline = underline;
  }

  public boolean isOutline() {
    return outline;
  }

  public void setOutline(boolean outline) {
    this.outline = outline;
  }

  public boolean isShadow() {
    return shadow;
  }

  public void setShadow(boolean shadow) {
    this.shadow = shadow;
  }

  public boolean isSubscript() {
    return subscript;
  }

  public void setSubscript(boolean subscript) {
    this.subscript = subscript;
    if (subscript) {
      setSuperscript(false);
      setFormula(false);
    }
  }

  public boolean isSuperscript() {
    return superscript;
  }

  public void setSuperscript(boolean superscript) {
    this.superscript = superscript;
    if (superscript) {
      setSubscript(false);
      setFormula(false);
    }
  }

  public boolean isFormula() {
    return formula;
  }

  public void setFormula(boolean formula) {
    this.formula = formula;
    if (formula) {
      setSubscript(false);
      setSuperscript(false);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (bold ? 1231 : 1237);
    result = prime * result + (formula ? 1231 : 1237);
    result = prime * result + (italic ? 1231 : 1237);
    result = prime * result + (outline ? 1231 : 1237);
    result = prime * result + (shadow ? 1231 : 1237);
    result = prime * result + (subscript ? 1231 : 1237);
    result = prime * result + (superscript ? 1231 : 1237);
    result = prime * result + (underline ? 1231 : 1237);
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
    final CDFontFace other = (CDFontFace) obj;
    if (bold != other.bold) {
      return false;
    }
    if (formula != other.formula) {
      return false;
    }
    if (italic != other.italic) {
      return false;
    }
    if (outline != other.outline) {
      return false;
    }
    if (shadow != other.shadow) {
      return false;
    }
    if (subscript != other.subscript) {
      return false;
    }
    if (superscript != other.superscript) {
      return false;
    }
    if (underline != other.underline) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("FontType[");
    sb.append("bold=");
    sb.append(isBold());
    sb.append(",italic=");
    sb.append(isItalic());
    sb.append(",underline=");
    sb.append(isUnderline());
    sb.append(",outline=");
    sb.append(isOutline());
    sb.append(",shadow=");
    sb.append(isShadow());
    sb.append(",subscript=");
    sb.append(isSubscript());
    sb.append(",superscript=");
    sb.append(isSuperscript());
    sb.append(",formula=");
    sb.append(isFormula());
    sb.append("]");
    return sb.toString();
  }
}
