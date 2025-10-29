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

import java.util.*;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cheminf.SugarRings;
import org.openscience.cdk.interfaces.*;
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
    List<IStereoElement> stereoElements = getStereoElements(atomContainer, atomMap);
    filterWavyBonds(stereoElements, bondMap);
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
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap) {
    return SugarRings.containsSugarRings(atomContainer)
        ? extractSugarStereoElements(atomContainer)
        : extractNonSugarStereoElements(atomContainer, atomMap);
  }

  /**
   * Extracts stereochemical elements specifically for sugar-containing molecules.
   *
   * @param atomContainer the {@link IAtomContainer} containing sugar rings
   * @return list of stereochemical elements
   */
  private static List<IStereoElement> extractSugarStereoElements(IAtomContainer atomContainer) {
    return selectFactory(atomContainer)
        .interpretProjections(Projection.Chair, Projection.Fischer, Projection.Haworth)
        .createAll();
  }

  /**
   * Extracts stereochemical elements for non-sugar molecules.
   *
   * <p>Sets bond stereo from display types if necessary and determines tetrahedral chirality from
   * {@link CDAtom} CIP types if coordinates are duplicated or stereo elements are empty.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom} for tetrahedral stereochemistry
   * @return list of stereochemical elements
   */
  private static List<IStereoElement> extractNonSugarStereoElements(
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap) {
    setBondStereoByDisplayType(atomContainer); // TODO can be removed when using cdk v2.12 or higher
    List<IStereoElement> elements = selectFactory(atomContainer).createAll();
    if (ChemicalUtils.hasDuplicateCoordinates(atomContainer) || elements.isEmpty()) {
      //      return
      elements.addAll(setTetrahedralStereoByCDAtomCIPType(atomContainer, atomMap));
    }
    return elements;
  }

  /**
   * Selects an appropriate {@link StereoElementFactory} based on whether the atom container has 3D
   * or 2D coordinates.
   *
   * @param atomContainer the {@link IAtomContainer} to analyze
   * @return a {@link StereoElementFactory} instance for 2D or 3D
   */
  private static StereoElementFactory selectFactory(IAtomContainer atomContainer) {
    return (atomContainer.getAtom(0).getPoint3d() != null)
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
                    (element.getFocus() instanceof IAtom atom
                        && wavyBonds.stream()
                            .anyMatch(
                                bond ->
                                    bond.getBegin().equals(atom) || bond.getEnd().equals(atom))))
            .toList();
    stereoElements.removeAll(wavyElements);
  }

  /**
   * Generates tetrahedral chirality stereochemical elements for atoms with defined CIP type.
   *
   * @param atomContainer the {@link IAtomContainer} containing the atoms
   * @param atomMap mapping of {@link CDAtom} to {@link IAtom}
   * @return list of tetrahedral chirality stereo elements
   */
  private static List<IStereoElement> setTetrahedralStereoByCDAtomCIPType(
      IAtomContainer atomContainer, Map<CDAtom, IAtom> atomMap) {
    List<IStereoElement> stereoElements = new ArrayList<>();
    for (Map.Entry<CDAtom, IAtom> entry : atomMap.entrySet()) {
      CDAtom cdAtom = entry.getKey();
      IAtom atom = entry.getValue();

      if (!CDAtomGeometry.Tetrahedral.equals(cdAtom.getAtomGeometry())) continue;
      CDAtomCIPType cipType = cdAtom.getStereochemistry();
      ITetrahedralChirality.Stereo stereo;
      if (cipType == CDAtomCIPType.R) {
        stereo = ITetrahedralChirality.Stereo.CLOCKWISE;
      } else if (cipType == CDAtomCIPType.S) {
        stereo = ITetrahedralChirality.Stereo.ANTI_CLOCKWISE;
      } else {
        continue;
      }
      int nNbrs = 0;
      IAtom[] ligands = new IAtom[4];
      int idxOfH = -1;
      for (IAtom ligand : atomContainer.getConnectedAtomsList(atom)) {
        if (nNbrs == 4) {
          continue; // too many ligands
        }
        if (ligand.getAtomicNumber() == IElement.H) {
          if (idxOfH >= 0) {
            continue; // too many hydrogens
          }
          idxOfH = nNbrs;
        }
        ligands[nNbrs++] = ligand;
      }
      // incorrect number of neighbours?
      if (nNbrs < 3 || nNbrs < 4 && idxOfH >= 0) {
        continue;
      }
      // implicit neighbour (H or lone-pair)
      if (nNbrs == 3) {
        ligands[nNbrs++] = atom;
      }
      if (nNbrs != 4) {
        continue;
      }
      // H is always at back, even if explicit! At least this seems to be the case.
      // we adjust the winding as needed which is when the explict H is in slot
      // 0 or 2 (odd number of swaps to get to index 3)
      if (idxOfH == 0 || idxOfH == 2) {
        stereo = stereo.invert();
      }

      TetrahedralChirality chirality = new TetrahedralChirality(atom, ligands, stereo);
      stereoElements.add(chirality);
    }
    return stereoElements;
  }

  /**
   * Sets bond stereochemistry in an {@link IAtomContainer} based on CDK bond display types.
   *
   * @param atomContainer the {@link IAtomContainer} whose bonds will be updated
   */
  private static void setBondStereoByDisplayType(IAtomContainer atomContainer) {
    for (IBond bond : atomContainer.bonds()) {
      if (bond.getDisplay() == null) {
        bond.setStereo(IBond.Stereo.NONE);
      }
      switch (bond.getDisplay()) {
        case WedgeBegin, Bold -> bond.setStereo(IBond.Stereo.UP);
        case WedgedHashBegin, Hash -> bond.setStereo(IBond.Stereo.DOWN);
        case WedgeEnd -> bond.setStereo(IBond.Stereo.UP_INVERTED);
        case WedgedHashEnd -> bond.setStereo(IBond.Stereo.DOWN_INVERTED);
        case Wavy -> bond.setStereo(IBond.Stereo.UP_OR_DOWN);
        default -> bond.setStereo(IBond.Stereo.NONE);
      }
    }
  }
}
