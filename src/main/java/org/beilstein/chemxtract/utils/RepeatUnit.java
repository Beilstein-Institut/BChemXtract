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
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDBracketAttachment;
import org.beilstein.chemxtract.cdx.CDCrossingBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;

/**
 * A bracketed atom whose repeat count the drawing leaves open ({@code n}) and a legend states per
 * entry ({@code 26g: n = 2}). The atom is drawn once, between the two bonds crossing its bracket;
 * {@link #repeat(int)} puts further copies of it into the chain.
 */
public final class RepeatUnit {

  private final CDFragment fragment;
  private final CDAtom unit;
  private CDAtom last;
  private CDBond exit;
  private int count = 1;

  private RepeatUnit(CDFragment fragment, CDAtom unit, CDBond exit) {
    this.fragment = fragment;
    this.unit = unit;
    this.last = unit;
    this.exit = exit;
  }

  /**
   * Finds the fragment's open repeat unit: a bracket with no repeat count around a single atom of
   * the fragment, crossed by two of that atom's bonds.
   *
   * @param fragment the fragment
   * @param page the page holding the brackets
   * @return the repeat unit, or {@code null} if the fragment has none or more than one
   */
  public static RepeatUnit find(CDFragment fragment, CDPage page) {
    RepeatUnit found = null;
    for (CDBracket bracket : page.getBracketedGroups()) {
      if (bracket.getRepeatCount() != 0 || bracket.getBracketedObjects().size() != 1) {
        continue;
      }
      if (!(bracket.getBracketedObjects().get(0) instanceof CDAtom atom)
          || !fragment.getAtoms().contains(atom)) {
        continue;
      }
      List<CDBond> crossing = new ArrayList<>();
      for (CDBracketAttachment attachment : bracket.getBracketAttachments()) {
        for (CDCrossingBond crossingBond : attachment.getCrossingBonds()) {
          CDBond bond = crossingBond.getBond();
          if (bond != null
              && fragment.getBonds().contains(bond)
              && (bond.getBegin() == atom || bond.getEnd() == atom)
              && !crossing.contains(bond)) {
            crossing.add(bond);
          }
        }
      }
      if (crossing.size() != 2) {
        continue;
      }
      if (found != null) {
        return null;
      }
      found = new RepeatUnit(fragment, atom, crossing.get(1));
    }
    return found;
  }

  /**
   * Repeats the unit until it occurs {@code times} times in the chain. Copies only add up: a
   * smaller count than already reached leaves the chain as it is.
   *
   * @param times how often the unit occurs, at least 1
   * @return {@code true} if the chain now holds the unit that often
   */
  public boolean repeat(int times) {
    while (count < times) {
      CDAtom copy = new CDAtom(unit);
      fragment.addAtom(copy);
      CDBond link = new CDBond(exit);
      link.setCrossingBonds(null);
      link.setBegin(last);
      link.setEnd(copy);
      fragment.addBond(link);
      if (exit.getBegin() == last) {
        exit.setBegin(copy);
      } else {
        exit.setEnd(copy);
      }
      last = copy;
      count++;
    }
    return count == times;
  }
}
