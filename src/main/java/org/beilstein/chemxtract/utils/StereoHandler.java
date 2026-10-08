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
 * cases for sugars and adjusts stereochemistry for atoms with duplicate coordinates. Wavy bonds are
 * filtered out when adding stereo elements to the container.
 */
public class StereoHandler {

  private StereoHandler() {
    // utility class, prevent instantiation
  }

  /**
   * Sets stereochemistry elements on the given {@link IAtomContainer} based on the provided mapping
   * between {@link CDAtom}/{@link CDBond} objects and CDK {@link IAtom}/{@link IBond} objects.
   *
   * <p>Sugar stereochemistry is handled differently from non-sugar stereochemistry. Wavy bonds are
   * filtered out from the generated stereo elements.
   *
   * @param atomContainer the {@link IAtomContainer} to set stereochemistry on
   * @param bondMap mapping of {@link CDBond} to {@link IBond} used to identify wavy bonds
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom} used for tetrahedral stereochemistry
   */
  public static void setStereo(
      IAtomContainer atomContainer, Map<CDBond, IBond> bondMap, Map<CDAtom, IAtom> atomMap) {
    List<IStereoElement> stereoElements = getStereoElements(atomContainer, atomMap, bondMap);
    //
    stereoElements.forEach(atomContainer::addStereoElement);
  }

  /**
   * Determines and returns all stereochemical elements for the given atom container. Handles sugars
   * differently from non-sugar structures.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom} for tetrahedral stereochemistry
   * @return list of stereochemical elements
   */
  private static List<IStereoElement> getStereoElements(
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap, Map<CDBond, IBond> bondMap) {
    SugarProjectionDetector detector = new SugarProjectionDetector(atomContainer);
    return detector.containsChairProjections()
        ? extractSugarStereoElements(atomContainer, bondMap)
        : extractNonSugarStereoElements(atomContainer, atomMap);
  }

  /**
   * Extracts stereochemical elements specifically for sugar-containing molecules.
   *
   * @param atomContainer the {@link IAtomContainer} containing sugar rings
   * @return list of stereochemical elements
   */
  private static List<IStereoElement> extractSugarStereoElements(
      IAtomContainer atomContainer, Map<CDBond, IBond> bondMap) {
    removeBondDisplay(atomContainer);
    List<IStereoElement> elements =
        selectFactory(atomContainer)
            .interpretProjections(Projection.Chair, Projection.Fischer, Projection.Haworth)
            .createAll();
    filterWavyBonds(elements, bondMap);
    return elements;
  }

  /**
   * Resets all bond display styles within the given atom container to {@link IBond.Display#Solid}.
   *
   * @param atomContainer the {@link IAtomContainer} whose bonds are to be normalised; must not be
   *     {@code null}
   */
  private static void removeBondDisplay(IAtomContainer atomContainer) {
    atomContainer.bonds().forEach(b -> b.setDisplay(IBond.Display.Solid));
  }

  /**
   * Extracts stereochemical elements for non-sugar molecules.
   *
   * <p>Sets bond stereo from display types if necessary and determines tetrahedral chirality from
   * {@link CDAtom} CIP types if coordinates are duplicated or stereo elements are empty. CIP types
   * only fill in atoms that have no perceived stereo element.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom} for tetrahedral stereochemistry
   * @return list of stereochemical elements
   */
  private static List<IStereoElement> extractNonSugarStereoElements(
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap) {
    List<IStereoElement> elements = selectFactory(atomContainer).createAll();
    if (ChemicalUtils.hasDuplicateCoordinates(atomContainer) || elements.isEmpty()) {
      Set<Object> perceived =
          elements.stream().map(IStereoElement::getFocus).collect(Collectors.toSet());
      getTetrahedralStereoByCDAtomCIPType(atomContainer, atomMap).stream()
          .filter(element -> !perceived.contains(element.getFocus()))
          .forEach(elements::add);
    }
    return elements;
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
