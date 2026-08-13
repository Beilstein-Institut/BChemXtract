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

import java.util.ArrayList;
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
    // The generator lays out at a fixed internal bond length (DEFAULT_BOND_LENGTH); ChemDraw
    // scaffolds usually sit at a very different scale. Laying out in the scaffold's scale makes the
    // generator place grafted atoms far too small, so the scaffold is first scaled into the
    // generator's native scale, then the whole structure is scaled back afterwards. The scaffold
    // ends up at its exact original coordinates (scaled down then up by the inverse), while the
    // grafted atoms inherit the matching scale.
    double toNative = targetBondLength > 0.0 ? DEFAULT_BOND_LENGTH / targetBondLength : 1.0;
    Point2d pivot = centroid(fixedAtoms);

    // Snapshot every coordinate so a failed or partial layout leaves the container exactly as it
    // was, never in an intermediate (nulled-out or half-scaled) state.
    Map<IAtom, Point2d> originalPoints = new HashMap<>();
    for (IAtom atom : container.atoms()) {
      originalPoints.put(atom, atom.getPoint2d());
    }

    // Clear collapsed coordinates so the generator treats them as unplaced.
    for (IAtom atom : freeAtoms) {
      atom.setPoint2d(null);
      atom.setPoint3d(null);
    }
    // Move the fixed scaffold into the generator's native bond-length scale.
    scaleAbout(fixedAtoms, pivot, toNative);

    Set<IBond> fixedBonds = new HashSet<>();
    for (IBond bond : container.bonds()) {
      if (fixedAtoms.contains(bond.getBegin()) && fixedAtoms.contains(bond.getEnd())) {
        fixedBonds.add(bond);
      }
    }

    try {
      sdg.setMolecule(container, false, fixedAtoms, fixedBonds);
      sdg.generateCoordinates();
      // Map the whole structure back to the scaffold's original scale: the fixed atoms return to
      // their exact starting positions, the newly placed atoms come along at the matching size.
      List<IAtom> placed = new ArrayList<>();
      for (IAtom atom : container.atoms()) {
        if (atom.getPoint2d() != null) {
          placed.add(atom);
        }
      }
      scaleAbout(placed, pivot, 1.0 / toNative);
      LOGGER.debug("Laid out {} expanded abbreviation atom(s).", freeAtoms.size());
    } catch (CDKException | RuntimeException e) {
      for (Map.Entry<IAtom, Point2d> entry : originalPoints.entrySet()) {
        entry.getKey().setPoint2d(entry.getValue());
      }
      throw e;
    }
  }

  /** Uniformly scales the given atoms about {@code pivot} by {@code factor} (pivot stays fixed). */
  private static void scaleAbout(Iterable<IAtom> atoms, Point2d pivot, double factor) {
    for (IAtom atom : atoms) {
      Point2d p = atom.getPoint2d();
      if (p == null) {
        continue;
      }
      atom.setPoint2d(
          new Point2d(pivot.x + (p.x - pivot.x) * factor, pivot.y + (p.y - pivot.y) * factor));
    }
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
