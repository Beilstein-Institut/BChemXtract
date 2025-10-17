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

import io.github.dan2097.jnainchi.InchiFlag;
import io.github.dan2097.jnainchi.InchiStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.rinchi.RInChIGenerator;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class providing static methods for common chemical operations
 * such as generating InChI, SMILES, CXSMILES, and RInChI representations
 * for molecules and reactions.
 */
public class ChemicalUtils {

  private static final Log logger = LogFactory.getLog(ChemicalUtils.class);

  private ChemicalUtils() {
    // hide implicit public constructor
  }

  /**
   * Generates an InChI representation for the given AtomContainer.
   *
   * @param atomContainer AtomContainer for which the InChI representation is generated
   * @return InChIGenerator representing the InChI representation of the AtomContainer, returns null if
   *          InChI generation fails
   * @throws CDKException If InChI generation encounters an error or warning
   */
  public static InChIGenerator getInChI(IAtomContainer atomContainer) throws CDKException {
    InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    StringBuilder options = new StringBuilder();
    InchiFlag[] opts = new InchiFlag[] { InchiFlag.Polymers, InchiFlag.NPZz };
    for (InchiFlag opt : opts) {
      options.append(" ").append(opt);
    }
    InChIGenerator gen = factory.getInChIGenerator(atomContainer, options.toString());
    InchiStatus status = gen.getStatus();
    if (status == InchiStatus.WARNING) {
      // InChI generated, but with warning message
      logger.warn("InChI warning: " + gen.getMessage());
    } else if (status != InchiStatus.SUCCESS) {
      // InChI generation failed
      CDKException exception = new CDKException("InChI failed: " + status.toString() + " [" + gen.getMessage() + "]");
      logger.error(exception);
      throw exception;
    }
    return gen;
  }

  /**
   * Creates an absolute SMILES representation for the given AtomContainer.
   *
   * @param atomContainer AtomContainer for which to generate an absolute SMILES representation
   * @return absolute SMILES string representing the structure of the AtomContainer
   */
  public static String createAbsoluteSmiles(IAtomContainer atomContainer) {
    return createSmiles(atomContainer, SmiFlavor.Absolute);
  }

  /**
   * Creates a reaction SMILES with the given flavor.
   *
   * @param reaction the reaction to convert
   * @param flavor SMILES flavor (e.g., SmiFlavor.Absolute)
   * @return SMILES string for the reaction, or null if conversion fails
   */
  public static String createReactionSmiles(IReaction reaction, int flavor) {
    String smiles = null;
    // flag absolute generates a canonical SMILES with stereochemistry and atomic masses (isomers)
    SmilesGenerator smilesGen = new SmilesGenerator(flavor);
    try {
     smiles = smilesGen.create(reaction);
    } catch (CDKException | NullPointerException | IllegalArgumentException anException) {
      logger.error(anException.getMessage());
    }
    return smiles;
  }

  /**
   * Creates an absolute reaction SMILES representation including stereochemistry.
   *
   * @param reaction the reaction to convert
   * @return Absolute reaction SMILES string
   */
  public static String createAbsoluteReactionSmiles(IReaction reaction) {
    return createReactionSmiles(reaction, SmiFlavor.Absolute);
  }

  /**
   * Creates a CXSMILES with coordinates for the given AtomContainer.
   * Only if all atoms have coordinates SMILES will be returned otherwise null is returned.
   *
   * @param atomContainer AtomContainer for which to generate an absolute SMILES representation
   * @return CXSMILES with coordinates, if all atoms have coordinates, otherwise null.
   */
  public static String createExtendedSmiles(IAtomContainer atomContainer){
    for (IAtom atom : atomContainer.atoms()) {
      if((atom.getPoint2d() == null && atom.getPoint3d() == null) ||
              hasDuplicateCoordinates(atomContainer)
      ){
        return null;
      }
    }
    return createSmiles(atomContainer, SmiFlavor.CxSmilesWithCoords);
  }

  /**
   * Checks whether the AtomContainer contains duplicate 2D or 3D coordinates.
   *
   * @param container AtomContainer to check
   * @return {@code true} if duplicates exist, {@code false} otherwise
   */
  public static boolean hasDuplicateCoordinates(IAtomContainer container) {
    Set<String> seen = new HashSet<>();
    for (IAtom atom : container.atoms()) {
      Point3d p3 = atom.getPoint3d();
      Point2d p2 = atom.getPoint2d();
      String key;
      if (p3 != null) {
        key = String.format("3D:%.6f,%.6f,%.6f", p3.x, p3.y, p3.z);
      } else if (p2 != null) {
        key = String.format("2D:%.6f,%.6f", p2.x, p2.y);
      } else {
        continue;
      }
      if (!seen.add(key)) {
        return true; // duplicate found
      }
    }
    return false; // all unique
  }

  /**
   * Creates an SMILES representation for the given AtomContainer with the given SmiFlavor.
   *
   * @param atomContainer AtomContainer for which to generate an absolute SMILES representation
   * @return absolute SMILES string representing the structure of the AtomContainer
   */
  public static String createSmiles(IAtomContainer atomContainer, int flavor) {
    String smiles = null;
    // flag absolute generates a canonical SMILES with stereochemistry and atomic masses (isomers)
    SmilesGenerator smilesGen = new SmilesGenerator(flavor);
    try {
      try {
        smiles = smilesGen.create(atomContainer);
      } catch (CDKException exception) {
        IAtomContainer clone = atomContainer.clone();
        Kekulization.kekulize(clone);
        smiles = smilesGen.create(clone);
        logger.info("Kekulized structure: " + smiles);
      }
    } catch (CDKException | NullPointerException | IllegalArgumentException | CloneNotSupportedException anException) {
      logger.error(anException + "; molecule name: " + atomContainer.getID(), anException);
    }
    return smiles;
  }

  /**
   * Finds the nearest residue atom to the given residueAtom within the provided AtomContainer.
   *
   * @param residueAtom reference residue atom
   * @param atomContainer AtomContainer in which to search for the nearest residue atom.
   * @return The nearest residue atom to the given residueAtom.
   */
  public static IAtom findNearestResidueAtom(IAtom residueAtom, IAtomContainer atomContainer) {
    IAtom nearestResidueAtom = null;
    int dist = Integer.MAX_VALUE;
    ShortestPaths path = new ShortestPaths(atomContainer, residueAtom);
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom && atom != residueAtom) {
        int pathDist = path.distanceTo(atom);
        if (pathDist < dist) {
          dist = pathDist;
          nearestResidueAtom = atom;
        }
      }
    }
    return nearestResidueAtom;
  }

  public static String getRInChI(IReaction reaction) {
    RInChIGenerator generator = new RInChIGenerator().generate(reaction);
    return generator.getRInChI();
  }

  public static String getLongRInChIKey(IReaction reaction) {
    RInChIGenerator generator = new RInChIGenerator().generate(reaction);
    return generator.getLongRInChIKey();
  }

  public static String getShortRInChIKey(IReaction reaction) {
    RInChIGenerator generator = new RInChIGenerator().generate(reaction);
    return generator.getShortRInChIKey();
  }

  public static String getWebRInChIKey(IReaction reaction) {
    RInChIGenerator generator = new RInChIGenerator().generate(reaction);
    return generator.getWebRInChIKey();
  }

  public static String getRAuxInfo(IReaction reaction) {
    RInChIGenerator generator = new RInChIGenerator().generate(reaction);
    return generator.getAuxInfo();
  }
}
