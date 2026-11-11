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
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.smiles.SmiFlavor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Substituents drawn as R-group values: a legend line such as {@code a: R =} followed by a drawn
 * group whose free bond ends at a wavy line, the usual way of marking where a group attaches. The
 * wavy line is a single wavy bond between two unlabelled carbons, and the group's bond ends on an
 * unlabelled carbon lying on it. That end atom stands for the scaffold atom the group is bonded to,
 * not for a carbon of the group. The wavy line is usually a fragment of its own; when the author
 * grouped it with the group, it is the fragment's one wavy bond standing apart from the rest.
 */
final class DrawnRGroupValues {

  private static final Logger LOGGER = LoggerFactory.getLogger(DrawnRGroupValues.class);

  /** How close the group's end atom must lie to the wavy line, as a share of the line's length. */
  private static final double ON_THE_LINE = 0.4;

  /**
   * A drawn substituent and the wavy line its attachment end lies on.
   *
   * @param group the drawn group, end atom included
   * @param cut the wavy line
   * @param end the group's end atom on the wavy line, where the scaffold attaches
   * @param drawn the page fragments the drawing consists of: the group's and the wavy line's, or
   *     the one fragment holding both
   */
  record Drawing(CDFragment group, CDFragment cut, CDAtom end, Set<CDFragment> drawn) {}

  /**
   * A group or wavy line, and the page fragment it was drawn in.
   *
   * @param fragment the group or wavy line on its own
   * @param source the page fragment
   */
  private record Part(CDFragment fragment, CDFragment source) {}

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
    List<Part> cuts = new ArrayList<>();
    List<Part> groups = new ArrayList<>();
    for (CDFragment fragment : fragments) {
      if (isWavyLine(fragment)) {
        cuts.add(new Part(fragment, fragment));
        continue;
      }
      CDBond wavy = separateWavyBond(fragment);
      if (wavy == null) {
        groups.add(new Part(fragment, fragment));
        continue;
      }
      List<CDAtom> line = List.of(wavy.getBegin(), wavy.getEnd());
      cuts.add(new Part(part(line, List.of(wavy)), fragment));
      List<CDAtom> rest = new ArrayList<>(fragment.getAtoms());
      rest.removeAll(line);
      List<CDBond> restBonds = new ArrayList<>(fragment.getBonds());
      restBonds.remove(wavy);
      groups.add(new Part(part(rest, restBonds), fragment));
    }
    if (cuts.isEmpty()) {
      return List.of();
    }
    Set<Part> usedCuts = Collections.newSetFromMap(new IdentityHashMap<>());
    List<Drawing> drawings = new ArrayList<>();
    for (Part group : groups) {
      CDFragment fragment = group.fragment();
      if (fragment.getAtoms().size() < 3) {
        continue;
      }
      Drawing found = null;
      Part foundCut = null;
      int ends = 0;
      for (CDAtom atom : fragment.getAtoms()) {
        if (!isEnd(atom) || AttachmentHandler.incidentBonds(fragment, atom).size() != 1) {
          continue;
        }
        for (Part cut : cuts) {
          if (!usedCuts.contains(cut) && liesOn(atom, cut.fragment())) {
            ends++;
            Set<CDFragment> drawn = Collections.newSetFromMap(new IdentityHashMap<>());
            drawn.add(group.source());
            drawn.add(cut.source());
            found = new Drawing(fragment, cut.fragment(), atom, drawn);
            foundCut = cut;
          }
        }
      }
      // A group with more than one end on a wavy line is not a single substituent.
      if (ends == 1) {
        usedCuts.add(foundCut);
        drawings.add(found);
      }
    }
    return drawings;
  }

  /**
   * The fragment's wavy bond that stands apart from the rest of it: a single wavy bond between two
   * unlabelled carbons bonded to nothing else, beside a drawing of at least three atoms.
   *
   * @return the bond, or {@code null} if the fragment has none or more than one
   */
  private static CDBond separateWavyBond(CDFragment fragment) {
    CDBond found = null;
    for (CDBond bond : fragment.getBonds()) {
      if (bond.getBondDisplay() == CDBondDisplay.Wavy
          && isBareCarbon(bond.getBegin())
          && isBareCarbon(bond.getEnd())
          && AttachmentHandler.incidentBonds(fragment, bond.getBegin()).size() == 1
          && AttachmentHandler.incidentBonds(fragment, bond.getEnd()).size() == 1) {
        if (found != null) {
          return null;
        }
        found = bond;
      }
    }
    return found != null && fragment.getAtoms().size() >= 5 ? found : null;
  }

  /** A fragment of the given atoms and bonds, taken from a larger one. */
  private static CDFragment part(List<CDAtom> atoms, List<CDBond> bonds) {
    CDFragment part = new CDFragment();
    part.addAllAtoms(atoms);
    bonds.forEach(part::addBond);
    return part;
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
      FragmentConverter converter = new FragmentConverter(builder);
      IAtomContainer group = converter.convert(drawing.group());
      IPseudoAtom star = builder.newInstance(IPseudoAtom.class, "*");
      star.setImplicitHydrogenCount(0);
      IAtom attachment = converter.convertedAtom(group, drawing.end());
      if (attachment != null) {
        if (group.getConnectedBondsCount(attachment) != 1) {
          return null;
        }
        star.setPoint2d(attachment.getPoint2d());
        group.setAtom(group.indexOf(attachment), star);
      } else {
        // The converter leaves out an attachment point node and its bond: bond the * to the atom
        // the node was bonded to.
        IAtom bonded = converter.convertedAtom(group, neighbour(drawing.group(), drawing.end()));
        if (bonded == null) {
          return null;
        }
        star.setPoint2d(drawing.end().getPosition2D() == null ? null : flipped(drawing.end()));
        group.addAtom(star);
        group.addBond(group.indexOf(bonded), group.indexOf(star), IBond.Order.SINGLE);
        Integer hydrogens = bonded.getImplicitHydrogenCount();
        if (hydrogens == null || hydrogens < 1) {
          return null;
        }
        bonded.setImplicitHydrogenCount(hydrogens - 1);
      }
      String smiles = ChemicalUtils.createSmiles(group, SmiFlavor.Canonical);
      return smiles != null && smiles.chars().filter(c -> c == '*').count() == 1 ? smiles : null;
    } catch (CDKException | RuntimeException e) {
      LOGGER.debug("Drawn substituent does not convert.", e);
      return null;
    }
  }

  private static Point2d flipped(CDAtom atom) {
    CDPoint2D position = atom.getPosition2D();
    return new Point2d(position.getX(), -position.getY());
  }

  /** The one atom bonded to the given one in the fragment, or {@code null}. */
  private static CDAtom neighbour(CDFragment fragment, CDAtom atom) {
    for (CDBond bond : fragment.getBonds()) {
      if (bond.getBegin() == atom) {
        return bond.getEnd();
      }
      if (bond.getEnd() == atom) {
        return bond.getBegin();
      }
    }
    return null;
  }

  /** The x of the middle of the drawn group, between its leftmost and rightmost atoms. */
  static double centreX(Drawing drawing) {
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for (CDAtom atom : drawing.group().getAtoms()) {
      if (atom.getPosition2D() != null) {
        min = Math.min(min, atom.getPosition2D().getX());
        max = Math.max(max, atom.getPosition2D().getX());
      }
    }
    return (min + max) / 2;
  }

  /** The y of the drawn group's lowest atom. */
  static double bottom(Drawing drawing) {
    double max = -Double.MAX_VALUE;
    for (CDAtom atom : drawing.group().getAtoms()) {
      if (atom.getPosition2D() != null) {
        max = Math.max(max, atom.getPosition2D().getY());
      }
    }
    return max;
  }

  /** A single wavy bond between two unlabelled carbons. */
  private static boolean isWavyLine(CDFragment fragment) {
    return fragment.getAtoms().size() == 2
        && fragment.getBonds().size() == 1
        && fragment.getBonds().get(0).getBondDisplay() == CDBondDisplay.Wavy
        && fragment.getAtoms().stream().allMatch(DrawnRGroupValues::isBareCarbon);
  }

  /**
   * Whether the atom can be the group's end on a wavy line: an unlabelled carbon, or an attachment
   * point node drawn there instead.
   */
  private static boolean isEnd(CDAtom atom) {
    return isBareCarbon(atom)
        || (CDNodeType.VariableAttachment.equals(atom.getNodeType())
            && atom.getPosition2D() != null);
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
