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
package org.beilstein.chemxtract.cheminf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.utils.Definitions;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates 2D coordinates for atoms of expanded ChemDraw abbreviations. When an abbreviation is
 * resolved into an explicit substructure, all of its atoms are stamped with the single coordinate
 * of the connection point, collapsing them onto one point. This class runs CDK partial layout with
 * the already-positioned scaffold held fixed, so only the collapsed atoms are placed, preserving
 * the original ChemDraw layout of the rest of the structure.
 *
 * <p>Must be invoked only after stereochemistry has been perceived, because the collapsed
 * coordinate is what gives scaffold stereocentres a valid wedge direction during perception.
 *
 * <p>Only the atoms explicitly passed in are laid out; the caller is responsible for supplying
 * exactly the atoms produced by abbreviation resubstitution so unrelated collapsed-coordinate
 * structures (e.g. ChemDraw "Multiple Group" sgroups) are never touched.
 */
public final class AbbreviationLayout {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbbreviationLayout.class);
  private static final double DEFAULT_BOND_LENGTH = 1.5;

  private AbbreviationLayout() {
    // static utility
  }

  /**
   * Spreads out the given expanded-abbreviation atoms (all collapsed onto their connection point by
   * {@code FragmentConverter.resubstituteAbbreviation}) using CDK partial 2D layout, keeping every
   * other validly-positioned scaffold atom fixed so the original ChemDraw layout is preserved.
   * No-op when {@code freeAtoms} is empty or the container exceeds {@link
   * Definitions#MAX_ATOM_COUNT}.
   *
   * <p>Must be invoked only after stereochemistry has been perceived: the collapsed coordinate is
   * what gives scaffold stereocentres a valid wedge direction during perception.
   *
   * @param container the atom container to lay out; modified in place
   * @param freeAtoms the atoms produced by abbreviation resubstitution (the atoms to place)
   * @throws CDKException if coordinate generation fails
   */
  public static void layoutExpandedAbbreviations(IAtomContainer container, Set<IAtom> freeAtoms)
      throws CDKException {
    if (container == null || freeAtoms == null || freeAtoms.isEmpty()) {
      return;
    }
    if (container.getAtomCount() > Definitions.MAX_ATOM_COUNT) {
      return;
    }

    Set<IAtom> fixedAtoms = new HashSet<>();
    for (IAtom atom : container.atoms()) {
      if (!freeAtoms.contains(atom) && atom.getPoint2d() != null) {
        fixedAtoms.add(atom);
      }
    }

    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    if (fixedAtoms.isEmpty()) {
      // Nothing to anchor to: lay the whole structure out fresh.
      sdg.generateCoordinates(container);
      return;
    }

    double targetBondLength = averageFixedBondLength(container, fixedAtoms);

    // Snapshot the collapsed coordinates so they can be restored verbatim if layout fails
    // partway through: a failed attempt must leave the container exactly as it was, never in an
    // intermediate (nulled-out) state that would silently change downstream chemistry.
    Map<IAtom, Point2d> originalPoints = new HashMap<>();
    for (IAtom atom : freeAtoms) {
      originalPoints.put(atom, atom.getPoint2d());
    }

    // Clear collapsed coordinates so the generator treats them as unplaced.
    for (IAtom atom : freeAtoms) {
      atom.setPoint2d(null);
      atom.setPoint3d(null);
    }

    Set<IBond> fixedBonds = new HashSet<>();
    for (IBond bond : container.bonds()) {
      if (fixedAtoms.contains(bond.getBegin()) && fixedAtoms.contains(bond.getEnd())) {
        fixedBonds.add(bond);
      }
    }

    try {
      // CDK 2.12's StructureDiagramGenerator.setBondLength(double) always throws
      // UnsupportedOperationException (bond length is fixed internally at 1.5; rescaling is meant
      // to happen post-layout via GeometryUtil.scaleMolecule). That whole-molecule helper would
      // also shift the fixed scaffold atoms, so instead we rescale only the newly-placed atoms
      // afterwards, anchored on their attachment point(s) into the fixed scaffold.
      sdg.setMolecule(container, false, fixedAtoms, fixedBonds);
      sdg.generateCoordinates();
      rescaleFreeAtoms(container, freeAtoms, fixedAtoms, targetBondLength);
      LOGGER.debug("Laid out {} expanded abbreviation atom(s).", freeAtoms.size());
    } catch (CDKException | RuntimeException e) {
      for (Map.Entry<IAtom, Point2d> entry : originalPoints.entrySet()) {
        entry.getKey().setPoint2d(entry.getValue());
      }
      throw e;
    }
  }

  /**
   * Scales each connected group of newly-placed atoms so its bond lengths approximate {@code
   * targetBondLength}, anchored on the fixed scaffold atom(s) it attaches to. Because this is a
   * uniform scaling about a fixed centre, every pairwise distance within a group scales by the same
   * factor, so the anchor atom itself never moves.
   */
  private static void rescaleFreeAtoms(
      IAtomContainer container,
      Set<IAtom> freeAtoms,
      Set<IAtom> fixedAtoms,
      double targetBondLength) {
    double generatedBondLength = averageBondLengthTouching(container, freeAtoms);
    if (generatedBondLength <= 0.0) {
      return;
    }
    double factor = targetBondLength / generatedBondLength;

    Set<IAtom> visited = new HashSet<>();
    for (IAtom start : freeAtoms) {
      if (!visited.add(start)) {
        continue;
      }
      List<IAtom> component = new ArrayList<>();
      Set<IAtom> anchors = new HashSet<>();
      Deque<IAtom> queue = new ArrayDeque<>();
      queue.add(start);
      component.add(start);
      while (!queue.isEmpty()) {
        IAtom current = queue.poll();
        for (IBond bond : container.getConnectedBondsList(current)) {
          IAtom other = bond.getOther(current);
          if (fixedAtoms.contains(other)) {
            anchors.add(other);
          } else if (freeAtoms.contains(other) && visited.add(other)) {
            component.add(other);
            queue.add(other);
          }
        }
      }
      if (anchors.isEmpty()) {
        continue;
      }
      Point2d anchor = centroid(anchors);
      for (IAtom atom : component) {
        Point2d p = atom.getPoint2d();
        if (p == null) {
          continue;
        }
        atom.setPoint2d(
            new Point2d(
                anchor.x + (p.x - anchor.x) * factor, anchor.y + (p.y - anchor.y) * factor));
      }
    }
  }

  /** Average length of bonds that touch at least one free (newly-placed) atom. */
  private static double averageBondLengthTouching(IAtomContainer container, Set<IAtom> freeAtoms) {
    double sum = 0.0;
    int count = 0;
    for (IBond bond : container.bonds()) {
      IAtom a = bond.getBegin();
      IAtom b = bond.getEnd();
      if ((freeAtoms.contains(a) || freeAtoms.contains(b))
          && a.getPoint2d() != null
          && b.getPoint2d() != null) {
        sum += a.getPoint2d().distance(b.getPoint2d());
        count++;
      }
    }
    return count == 0 ? 0.0 : sum / count;
  }

  /** Centroid of the 2D coordinates of the given atoms. */
  private static Point2d centroid(Set<IAtom> atoms) {
    double sumX = 0.0;
    double sumY = 0.0;
    int count = 0;
    for (IAtom atom : atoms) {
      Point2d p = atom.getPoint2d();
      if (p != null) {
        sumX += p.x;
        sumY += p.y;
        count++;
      }
    }
    return count == 0 ? new Point2d(0.0, 0.0) : new Point2d(sumX / count, sumY / count);
  }

  /** Average bond length over bonds whose endpoints are both fixed (both carry coordinates). */
  private static double averageFixedBondLength(IAtomContainer container, Set<IAtom> fixedAtoms) {
    double sum = 0.0;
    int count = 0;
    for (IBond bond : container.bonds()) {
      IAtom a = bond.getBegin();
      IAtom b = bond.getEnd();
      if (fixedAtoms.contains(a)
          && fixedAtoms.contains(b)
          && a.getPoint2d() != null
          && b.getPoint2d() != null) {
        sum += a.getPoint2d().distance(b.getPoint2d());
        count++;
      }
    }
    if (count == 0 || sum <= 0.0) {
      return DEFAULT_BOND_LENGTH;
    }
    return sum / count;
  }
}
