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
package org.beilstein.chemxtract.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Roman numerals numbering the steps or intermediates of a scheme ({@code III}, {@code IV}), typed
 * with the structure tool. ChemDraw keeps such a numeral as a label node, often with its letters
 * also lying on it as bonded atoms (iodine, vanadium); neither is a compound.
 */
public final class RomanNumeralLabels {

  private static final Logger LOGGER = LoggerFactory.getLogger(RomanNumeralLabels.class);

  /** A Roman numeral of up to a few letters. */
  private static final Pattern NUMERAL = Pattern.compile("[IVX]{1,6}");

  private RomanNumeralLabels() {}

  /**
   * The fragments without the Roman numerals among them: a single node labelled with a numeral (but
   * a lone {@code I}, which may be iodine, or {@code X}, an R-group label), and atoms each labelled
   * with a numeral's letter that lie on a numeral label and together hold its letters.
   *
   * @param fragments the page's fragments
   * @param page the page, for its texts
   * @return the fragments that remain
   */
  public static List<CDFragment> without(List<CDFragment> fragments, CDPage page) {
    List<CDText> numerals = new ArrayList<>();
    for (CDText text : page.getTexts()) {
      if (isNumeral(text)) {
        numerals.add(text);
      }
    }
    for (CDFragment fragment : fragments) {
      if (fragment.getAtoms().size() == 1 && isNumeral(fragment.getAtoms().get(0).getText())) {
        numerals.add(fragment.getAtoms().get(0).getText());
      }
    }
    if (numerals.isEmpty()) {
      return fragments;
    }
    List<CDFragment> kept = new ArrayList<>();
    for (CDFragment fragment : fragments) {
      if (isNumeralNode(fragment) || spellsNumeral(fragment, numerals)) {
        LOGGER.info("Not a compound: a Roman numeral drawn with the structure tool.");
      } else {
        kept.add(fragment);
      }
    }
    return kept;
  }

  private static boolean isNumeralNode(CDFragment fragment) {
    if (fragment.getAtoms().size() != 1) {
      return false;
    }
    CDText text = fragment.getAtoms().get(0).getText();
    // A lone I may be iodine, and a lone X is an R-group label.
    return isNumeral(text) && !label(text).equals("I") && !label(text).equals("X");
  }

  /**
   * Whether the fragment's atoms each carry a numeral's letter and together spell a numeral on
   * them.
   */
  private static boolean spellsNumeral(CDFragment fragment, List<CDText> numerals) {
    if (fragment.getAtoms().size() < 2) {
      return false;
    }
    StringBuilder letters = new StringBuilder();
    double left = Double.MAX_VALUE;
    double top = Double.MAX_VALUE;
    double right = -Double.MAX_VALUE;
    double bottom = -Double.MAX_VALUE;
    for (CDAtom atom : fragment.getAtoms()) {
      CDText text = atom.getText();
      if (text == null || text.getBounds() == null || !NUMERAL.matcher(label(text)).matches()) {
        return false;
      }
      letters.append(label(text));
      CDRectangle bounds = text.getBounds();
      left = Math.min(left, bounds.getLeft());
      top = Math.min(top, bounds.getTop());
      right = Math.max(right, bounds.getRight());
      bottom = Math.max(bottom, bounds.getBottom());
    }
    String spelled = sorted(letters.toString());
    for (CDText numeral : numerals) {
      CDRectangle bounds = numeral.getBounds();
      if (bounds != null
          && bounds.getLeft() < right
          && left < bounds.getRight()
          && bounds.getTop() < bottom
          && top < bounds.getBottom()
          && sorted(label(numeral)).equals(spelled)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isNumeral(CDText text) {
    return text != null && NUMERAL.matcher(label(text)).matches();
  }

  private static String label(CDText text) {
    return text == null || text.getText() == null ? "" : text.getText().getText().strip();
  }

  private static String sorted(String text) {
    return text.chars()
        .sorted()
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
