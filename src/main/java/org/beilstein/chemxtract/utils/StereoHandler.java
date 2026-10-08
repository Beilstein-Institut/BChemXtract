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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cheminf.SugarProjectionDetector;
import org.openscience.cdk.geometry.GeometryUtil;
import org.openscience.cdk.geometry.cip.CIPTool;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.stereo.Projection;
import org.openscience.cdk.stereo.StereoElementFactory;
import org.openscience.cdk.stereo.TetrahedralChirality;

/**
 * Utility class for setting stereochemistry in CDK {@link IAtomContainer} objects.
 *
 * <p>This class provides methods to extract, interpret, and set stereochemical elements, including
 * tetrahedral chirality and bond stereochemistry, based on 2D or 3D coordinates. It handles special
 * cases for sugar projections and adjusts stereochemistry for atoms with duplicate coordinates.
 */
public class StereoHandler {

  private StereoHandler() {
    // utility class, prevent instantiation
  }

  /**
   * Sets stereochemistry elements on the given {@link IAtomContainer} based on the provided mapping
   * between {@link CDAtom}/{@link CDBond} objects and CDK {@link IAtom}/{@link IBond} objects.
   *
   * <p>Stereo is perceived from wedges, from chair, Haworth and Fischer projections, and, where the
   * drawing leaves an atom undefined, from the CIP label ChemDraw stored on it. An atom with a wavy
   * bond gets no tetrahedral stereo.
   *
   * @param atomContainer the {@link IAtomContainer} to set stereochemistry on
   * @param bondMap mapping of {@link CDBond} to {@link IBond} used to read the drawn bond styles
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom} used for tetrahedral stereochemistry
   */
  public static void setStereo(
      IAtomContainer atomContainer, Map<CDBond, IBond> bondMap, Map<CDAtom, IAtom> atomMap) {
    Set<Projection> projections = flattenProjectionRings(atomContainer, bondMap);
    List<IStereoElement> elements =
        selectFactory(atomContainer)
            .interpretProjections(projections.toArray(Projection[]::new))
            .createAll();
    if (ChemicalUtils.hasDuplicateCoordinates(atomContainer) || elements.isEmpty()) {
      Set<Object> perceived =
          elements.stream().map(IStereoElement::getFocus).collect(Collectors.toSet());
      getTetrahedralStereoByCDAtomCIPType(atomContainer, atomMap).stream()
          .filter(element -> !perceived.contains(element.getFocus()))
          .forEach(elements::add);
    }
    filterWavyBonds(elements, bondMap);
    elements.forEach(atomContainer::addStereoElement);
  }

  /**
   * Finds chair and Haworth rings and draws the bonds at their atoms as plain bonds, returning the
   * projections to interpret. Their edges and substituents are drawn bold or wedged for
   * perspective, and CDK reads a projection only if every bond at its centres is plain; a single
   * wavy bond voids the whole ring, so wavy bonds are flattened too and their centres dropped by
   * {@link #filterWavyBonds}.
   *
   * <p>Every bond at a chair is reset, and a chair also enables Haworth and Fischer as before. A
   * Haworth-shaped outline alone is common in ordinary drawings, where a vertical substituent would
   * be misread as up or down, so a ring counts as Haworth only when an edge is drawn bold or
   * hashed, and it loses just its bold, hashed and wavy bonds so a real wedge is kept.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @param bondMap mapping of {@link CDBond} to {@link IBond} giving each bond's drawn style
   * @return the projections found
   */
  private static Set<Projection> flattenProjectionRings(
      IAtomContainer atomContainer, Map<CDBond, IBond> bondMap) {
    Set<IBond> perspective = bondsDrawn(bondMap, CDBondDisplay.Bold, CDBondDisplay.Hash);
    Set<IBond> wavy = bondsDrawn(bondMap, CDBondDisplay.Wavy);
    Set<Projection> projections = EnumSet.noneOf(Projection.class);
    SugarProjectionDetector detector = new SugarProjectionDetector(atomContainer);
    for (int[] ring : detector.findChairProjections()) {
      ringAtomBonds(atomContainer, ring).forEach(bond -> bond.setDisplay(IBond.Display.Solid));
      Collections.addAll(projections, Projection.Chair, Projection.Haworth, Projection.Fischer);
    }
    for (int[] ring : detector.findHaworthProjections()) {
      Set<IBond> bonds = ringAtomBonds(atomContainer, ring);
      Set<IAtom> atoms = new HashSet<>();
      for (int atom : ring) {
        atoms.add(atomContainer.getAtom(atom));
      }
      boolean frontEdge =
          bonds.stream()
              .anyMatch(
                  bond ->
                      perspective.contains(bond)
                          && atoms.contains(bond.getBegin())
                          && atoms.contains(bond.getEnd()));
      if (!frontEdge) {
        continue;
      }
      bonds.stream()
          .filter(bond -> perspective.contains(bond) || wavy.contains(bond))
          .forEach(bond -> bond.setDisplay(IBond.Display.Solid));
      projections.add(Projection.Haworth);
    }
    return projections;
  }

  /** The CDK bonds whose ChemDraw bond was drawn in one of the given styles. */
  private static Set<IBond> bondsDrawn(Map<CDBond, IBond> bondMap, CDBondDisplay... displays) {
    Set<CDBondDisplay> styles = Set.of(displays);
    return bondMap.entrySet().stream()
        .filter(entry -> styles.contains(entry.getKey().getBondDisplay()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toSet());
  }

  /** The bonds at the atoms of a ring, ring bonds and substituent bonds alike. */
  private static Set<IBond> ringAtomBonds(IAtomContainer atomContainer, int[] ring) {
    Set<IBond> bonds = new HashSet<>();
    for (int atom : ring) {
      atomContainer.getConnectedBondsList(atomContainer.getAtom(atom)).forEach(bonds::add);
    }
    return bonds;
  }

  /**
   * Selects an appropriate {@link StereoElementFactory} based on whether the atom container has 3D
   * or 2D coordinates.
   *
   * <p>The 3D factory is only usable when <em>every</em> atom carries 3D coordinates; ChemDraw
   * documents can mix atoms with and without a Z position, and the 3D factory dereferences the
   * missing points. Every atom placed by {@code AtomConverter} has a 2D position, so the 2D factory
   * is the safe choice for such mixed fragments.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @return a {@link StereoElementFactory} instance for 2D or 3D
   */
  private static StereoElementFactory selectFactory(IAtomContainer atomContainer) {
    return GeometryUtil.has3DCoordinates(atomContainer)
        ? StereoElementFactory.using3DCoordinates(atomContainer)
        : StereoElementFactory.using2DCoordinates(atomContainer);
  }

  /**
   * Removes stereochemical elements associated with wavy bonds from the provided list.
   *
   * @param stereoElements list of stereochemical elements to filter
   * @param bondMap mapping of {@link CDBond} to {@link IBond} used to identify wavy bonds
   */
  private static void filterWavyBonds(
      List<IStereoElement> stereoElements, Map<CDBond, IBond> bondMap) {
    List<IBond> wavyBonds =
        bondMap.entrySet().stream()
            .filter(entry -> entry.getKey().getBondDisplay() == CDBondDisplay.Wavy)
            .map(Map.Entry::getValue)
            .toList();
    List<IStereoElement> wavyElements =
        stereoElements.stream()
            .filter(
                element ->
                    element.getFocus() instanceof IAtom atom
                        && wavyBonds.stream()
                            .anyMatch(
                                bond -> bond.getBegin().equals(atom) || bond.getEnd().equals(atom)))
            .toList();
    stereoElements.removeAll(wavyElements);
  }

  /**
   * Generates tetrahedral chirality stereochemical elements for atoms with defined CIP type.
   *
   * <p>R/S depends on ligand priority, not ligand order, so each element is labelled with {@link
   * CIPTool} and inverted when that label disagrees with ChemDraw's.
   *
   * @param atomContainer the {@link IAtomContainer} containing the atoms
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom}
   * @return list of tetrahedral chirality stereo elements
   */
  private static List<IStereoElement> getTetrahedralStereoByCDAtomCIPType(
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap) {
    List<IStereoElement> stereoElements = new ArrayList<>();
    for (Map.Entry<CDAtom, IAtom> entry : atomMap.entrySet()) {
      CDAtom cdAtom = entry.getKey();
      IAtom atom = entry.getValue();

      if (!CDAtomGeometry.Tetrahedral.equals(cdAtom.getAtomGeometry())) {
        continue;
      }
      CDAtomCIPType cipType = cdAtom.getStereochemistry();
      if (cipType != CDAtomCIPType.R && cipType != CDAtomCIPType.S) {
        continue;
      }
      List<IAtom> ligands = new ArrayList<>(atomContainer.getConnectedAtomsList(atom));
      if (ligands.size() == 3) {
        ligands.add(atom); // implicit neighbour (H or lone pair)
      }
      if (ligands.size() != 4) {
        continue;
      }
      TetrahedralChirality chirality =
          new TetrahedralChirality(
              atom, ligands.toArray(IAtom[]::new), ITetrahedralChirality.Stereo.CLOCKWISE);
      CIPTool.CIP_CHIRALITY label = CIPTool.getCIPChirality(atomContainer, chirality);
      if (label == CIPTool.CIP_CHIRALITY.NONE) {
        continue; // not a stereocentre by CIP, e.g. two hydrogens
      }
      if (!label.name().equals(cipType.name())) {
        chirality.setStereo(ITetrahedralChirality.Stereo.ANTI_CLOCKWISE);
      }
      stereoElements.add(chirality);
    }
    return stereoElements;
  }
}
