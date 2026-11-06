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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.converter.FragmentConverter;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.smiles.SmiFlavor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Substituents drawn as R-group values: a legend line such as {@code a: R =} followed by a drawn
 * group whose free bond ends at a wavy line, the usual way of marking where a group attaches. The
 * wavy line is a fragment of its own, a single wavy bond between two unlabelled carbons, and the
 * group's bond ends on an unlabelled carbon lying on it. That end atom stands for the scaffold atom
 * the group is bonded to, not for a carbon of the group.
 */
final class DrawnRGroupValues {

  private static final Logger LOGGER = LoggerFactory.getLogger(DrawnRGroupValues.class);

  /** How close the group's end atom must lie to the wavy line, as a share of the line's length. */
  private static final double ON_THE_LINE = 0.4;

  /**
   * A drawn substituent and the wavy line its attachment end lies on.
   *
   * @param group the drawn group, end atom included
   * @param cut the wavy-line fragment
   * @param end the group's end atom on the wavy line, where the scaffold attaches
   */
  record Drawing(CDFragment group, CDFragment cut, CDAtom end) {}

  private DrawnRGroupValues() {}

  /**
   * Finds the drawn substituents among the page's fragments. A group qualifies when exactly one of
   * its atoms is an unlabelled terminal carbon lying on a wavy line, and each wavy line belongs to
   * one group at most.
   *
   * @param fragments the page's fragments
   * @return the drawings, in fragment order
   */
  static List<Drawing> find(List<CDFragment> fragments) {
    List<CDFragment> cuts = fragments.stream().filter(DrawnRGroupValues::isWavyLine).toList();
    if (cuts.isEmpty()) {
      return List.of();
    }
    Set<CDFragment> usedCuts = Collections.newSetFromMap(new IdentityHashMap<>());
    List<Drawing> drawings = new ArrayList<>();
    for (CDFragment fragment : fragments) {
      if (cuts.contains(fragment) || fragment.getAtoms().size() < 3) {
        continue;
      }
      Drawing found = null;
      for (CDAtom atom : fragment.getAtoms()) {
        if (!isBareCarbon(atom) || degree(fragment, atom) != 1) {
          continue;
        }
        for (CDFragment cut : cuts) {
          if (!usedCuts.contains(cut) && liesOn(atom, cut)) {
            if (found != null) {
              found = null; // two ends on wavy lines: not a single substituent
              break;
            }
            found = new Drawing(fragment, cut, atom);
          }
        }
      }
      if (found != null) {
        usedCuts.add(found.cut());
        drawings.add(found);
      }
    }
    return drawings;
  }

  /**
   * The drawing as substituent SMILES, its end atom written as the {@code *} the scaffold bonds to.
   *
   * @param drawing the drawn substituent
   * @param builder CDK object builder
   * @return the SMILES, or {@code null} if the group does not convert
   */
  static String smiles(Drawing drawing, IChemObjectBuilder builder) {
    try {
      IAtomContainer group = new FragmentConverter(builder).convert(drawing.group());
      // The converter keeps ChemDraw's coordinates with y flipped, which identifies the end atom.
      CDPoint2D end = drawing.end().getPosition2D();
      IAtom attachment = null;
      for (IAtom atom : group.atoms()) {
        Point2d point = atom.getPoint2d();
        if (point != null
            && Math.abs(point.x - end.getX()) < 1e-3
            && Math.abs(point.y + end.getY()) < 1e-3) {
          attachment = atom;
        }
      }
      if (attachment == null || group.getConnectedBondsCount(attachment) != 1) {
        return null;
      }
      IPseudoAtom star = builder.newInstance(IPseudoAtom.class, "*");
      star.setPoint2d(attachment.getPoint2d());
      star.setImplicitHydrogenCount(0);
      group.setAtom(group.indexOf(attachment), star);
      String smiles = ChemicalUtils.createSmiles(group, SmiFlavor.Canonical);
      return smiles != null && smiles.chars().filter(c -> c == '*').count() == 1 ? smiles : null;
    } catch (CDKException | RuntimeException e) {
      LOGGER.debug("Drawn substituent does not convert.", e);
      return null;
    }
  }

  /** A single wavy bond between two unlabelled carbons. */
  private static boolean isWavyLine(CDFragment fragment) {
    return fragment.getAtoms().size() == 2
        && fragment.getBonds().size() == 1
        && fragment.getBonds().get(0).getBondDisplay() == CDBondDisplay.Wavy
        && fragment.getAtoms().stream().allMatch(DrawnRGroupValues::isBareCarbon);
  }

  /** An element carbon drawn without a label. */
  private static boolean isBareCarbon(CDAtom atom) {
    return CDNodeType.Element.equals(atom.getNodeType())
        && atom.getElementNumber() == 6
        && (atom.getText() == null
            || atom.getText().getText() == null
            || atom.getText().getText().getText() == null
            || atom.getText().getText().getText().isBlank())
        && atom.getPosition2D() != null;
  }

  private static int degree(CDFragment fragment, CDAtom atom) {
    int degree = 0;
    for (CDBond bond : fragment.getBonds()) {
      if (bond.getBegin() == atom || bond.getEnd() == atom) {
        degree++;
      }
    }
    return degree;
  }

  /** Whether the atom lies on the wavy line, within 0.4 of the line's length. */
  private static boolean liesOn(CDAtom atom, CDFragment cut) {
    CDPoint2D p = atom.getPosition2D();
    CDPoint2D a = cut.getAtoms().get(0).getPosition2D();
    CDPoint2D b = cut.getAtoms().get(1).getPosition2D();
    double dx = b.getX() - a.getX();
    double dy = b.getY() - a.getY();
    double length = Math.hypot(dx, dy);
    if (length == 0) {
      return false;
    }
    double t = ((p.getX() - a.getX()) * dx + (p.getY() - a.getY()) * dy) / (length * length);
    t = Math.max(0, Math.min(1, t));
    double distance = Math.hypot(a.getX() + t * dx - p.getX(), a.getY() + t * dy - p.getY());
    return distance <= length * ON_THE_LINE;
  }
}
