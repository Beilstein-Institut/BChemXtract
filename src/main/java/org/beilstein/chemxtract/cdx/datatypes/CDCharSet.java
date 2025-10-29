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

public enum CDCharSet {
  Unknown(null),
  EBCDICOEM(null),
  MSDOSUS("US-ASCII"),
  EBCDIC500V1(null),
  ArabicASMO708(null),
  ArabicASMO449P(null),
  ArabicTransparent(null),
  ArabicTransparentASMO(null),
  Greek437G("IBM437"),
  BalticOEM("IBM775"),
  MSDOSLatin1("IBM850"),
  MSDOSLatin2("IBM852"),
  IBMCyrillic("IBM855"),
  IBMTurkish("IBM857"),
  MSDOSPortuguese("IBM860"),
  MSDOSIcelandic("IBM861"),
  HebrewOEM("IBM862"),
  MSDOSCanadianFrench("IBM863"),
  ArabicOEM("IBM864"),
  MSDOSNordic("IBM865"),
  MSDOSRussian("IBM866"),
  IBMModernGreek("IBM869"),
  Thai("x-windows-874"),
  EBCDIC(null),
  Japanese("Shift_JIS"),
  /** PRC, Singapore */
  ChineseSimplified("GB2312"),
  Korean("ISO-2022-KR"),
  /** Taiwan, Hong Kong */
  ChineseTraditional("Big5"),
  UnicodeISO10646("UTF-8"),
  Win31EasternEuropean("windows-1250"),
  Win31Cyrillic("windows-1251"),
  Win31Latin1(/*"ISO-8859-1"*/ "windows-1252"),
  Win31Greek("ISO-8859-7"),
  Win31Turkish("ISO-8859-9"),
  Hebrew("windows-1255"),
  Arabic("windows-1256"),
  Baltic("windows-1257"),
  Vietnamese("windows-1258"),
  KoreanJohab(null),
  MacRoman("x-MacRoman"),
  MacJapanese(/*"x-MacJapanese"*/ "Shift_JIS"),
  MacTradChinese("x-IBM950"),
  MacKorean(null),
  MacArabic("x-MacArabic"),
  MacHebrew("x-MacHebrew"),
  MacGreek("x-MacGreek"),
  MacCyrillic("x-MacCyrillic"),
  MacReserved(null),
  MacDevanagari(null),
  MacGurmukhi(null),
  MacGujarati(null),
  MacOriya(null),
  MacBengali(null),
  MacTamil(null),
  MacTelugu(null),
  MacKannada(null),
  MacMalayalam(null),
  MacSinhalese(null),
  MacBurmese(null),
  MacKhmer(null),
  MacThai("x-MacThai"),
  MacLao(null),
  MacGeorgian(null),
  MacArmenian(null),
  MacSimpChinese(null),
  MacTibetan(null),
  MacMongolian(null),
  MacEthiopic(null),
  MacCentralEuroRoman("x-MacCentralEurope"),
  MacVietnamese(null),
  MacExtArabic(null),
  MacUninterpreted(null),
  MacIcelandic("x-MacIceland"),
  MacTurkish("x-MacTurkish");

  private final String charSet;

  private CDCharSet(String charSet) {
    this.charSet = charSet;
  }

  public String getCharSet() {
    return charSet;
  }
}
