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

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Substituents written as names rather than formulas or abbreviations: {@code methoxy}, {@code
 * 4-chlorobenzyl}, {@code 3,4-dichlorophenyl}, {@code 4-chloro-3-methylphenyl}, {@code allyl}. Only
 * a fixed set of prefixes is known, on a phenyl or benzyl parent; anything else is not read, so a
 * name is either resolved exactly or left alone.
 */
final class SubstituentNames {

  /**
   * The known prefixes and the SMILES of the group each names, in the form a phenyl substituent
   * takes: a lone terminal atom, or a group with one attachment point.
   */
  private static final Map<String, String> PREFIXES =
      Map.of(
          "fluoro", "F",
          "chloro", "Cl",
          "bromo", "Br",
          "iodo", "I",
          "methyl", "C",
          "methoxy", "*OC",
          "hydroxy", "*O",
          "nitro", "*[N+](=O)[O-]");

  /** Groups named as a whole, not as a prefix, and the SMILES of each with its attachment. */
  private static final Map<String, String> GROUPS = Map.of("allyl", "*CC=C", "propargyl", "*CC#C");

  private static final String PREFIX = "(fluoro|chloro|bromo|iodo|methyl|methoxy|hydroxy|nitro)";

  /** A multiplied prefix: {@code dimethoxy}, {@code trichloro}. */
  private static final Pattern MULTIPLIED = Pattern.compile("(di|tri)?" + PREFIX);

  /**
   * One substituent of a named ring: its locants, multiplier and prefix, e.g. {@code 3,4-dichloro}.
   */
  private static final Pattern RING_PREFIX =
      Pattern.compile("(\\d(?:,\\d)*)-(di|tri)?" + PREFIX + "-?");

  /** A named ring: substituent prefixes, then the parent. */
  private static final Pattern RING_NAME =
      Pattern.compile("((?:\\d(?:,\\d)*-(?:di|tri)?" + PREFIX + "-?)*)(phenyl|benzyl)");

  /**
   * A named phenyl or benzyl group.
   *
   * @param benzyl whether the ring is attached through a CH2
   * @param substituents the SMILES of each substituent by ring position, 2 to 6
   */
  record Ring(boolean benzyl, Map<Integer, String> substituents) {}

  private SubstituentNames() {}

  /**
   * The group a prefix names, as many times as there are locants: {@code methoxy} for one, {@code
   * dimethoxy} for two, {@code trimethoxy} for three. A group named as a whole ({@code allyl},
   * {@code propargyl}) is read for one position.
   *
   * @param name the prefix as written
   * @param count how many positions it is put on
   * @return the group's SMILES, or {@code null} if the name is not a known prefix or its multiplier
   *     does not match the count
   */
  static String group(String name, int count) {
    if (GROUPS.containsKey(name)) {
      return count == 1 ? GROUPS.get(name) : null;
    }
    Matcher matcher = MULTIPLIED.matcher(name);
    if (!matcher.matches() || multiplier(matcher.group(1)) != count) {
      return null;
    }
    return PREFIXES.get(matcher.group(2));
  }

  /**
   * Reads a named phenyl or benzyl group, e.g. {@code 3,4-dichlorobenzyl}: each prefix at its
   * positions, the attachment at position 1.
   *
   * @param name the value as written
   * @return the ring, or {@code null} if the name is not one, a multiplier does not match its
   *     locants, or a position is named twice or outside 2 to 6
   */
  static Ring ring(String name) {
    Matcher matcher = RING_NAME.matcher(name);
    if (!matcher.matches()) {
      return null;
    }
    Map<Integer, String> substituents = new TreeMap<>();
    Matcher prefix = RING_PREFIX.matcher(matcher.group(1));
    int end = 0;
    while (prefix.find()) {
      if (prefix.start() != end) {
        return null;
      }
      end = prefix.end();
      String[] locants = prefix.group(1).split(",");
      if (multiplier(prefix.group(2)) != locants.length) {
        return null;
      }
      for (String locant : locants) {
        int position = Character.digit(locant.charAt(0), 10); // locants are single digits
        if (position < 2
            || position > 6
            || substituents.put(position, PREFIXES.get(prefix.group(3))) != null) {
          return null;
        }
      }
    }
    if (end != matcher.group(1).length()) {
      return null;
    }
    return new Ring(matcher.group(matcher.groupCount()).equals("benzyl"), substituents);
  }

  /**
   * How often a multiplier puts its group on the ring: {@code di} twice, {@code tri} three times,
   * none once.
   */
  static int multiplier(String multiplier) {
    if (multiplier == null) {
      return 1;
    }
    return multiplier.equals("di") ? 2 : 3;
  }
}
