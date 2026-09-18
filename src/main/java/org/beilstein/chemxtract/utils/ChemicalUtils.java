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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ShortestPaths;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.rinchi.RInChIGenerator;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class providing static methods for common chemical operations such as generating InChI,
 * SMILES, CXSMILES, and RInChI representations for molecules and reactions.
 */
public class ChemicalUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChemicalUtils.class);

  private ChemicalUtils() {
    // hide implicit public constructor
  }

  /**
   * Generates an InChI representation for the given AtomContainer.
   *
   * @param atomContainer AtomContainer for which the InChI representation is generated
   * @return InChIGenerator representing the InChI representation of the AtomContainer, returns null
   *     if InChI generation fails
   * @throws CDKException If InChI generation encounters an error or warning
   */
  public static InChIGenerator getInChI(IAtomContainer atomContainer) throws CDKException {
    InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
    StringBuilder options = new StringBuilder();
    InchiFlag[] opts = new InchiFlag[] {InchiFlag.Polymers, InchiFlag.NPZz};
    for (InchiFlag opt : opts) {
      options.append(" ").append(opt);
    }
    InChIGenerator gen = factory.getInChIGenerator(atomContainer, options.toString());
    InchiStatus status = gen.getStatus();
    if (status == InchiStatus.WARNING) {
      // InChI generated, but with warning message
      LOGGER.warn("InChI warning: {}", gen.getMessage());
    } else if (status != InchiStatus.SUCCESS) {
      // InChI generation failed
      CDKException exception =
          new CDKException("InChI failed: " + status.toString() + " [" + gen.getMessage() + "]");
      LOGGER.error("InChI generation failed", exception);
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
  public static String createAbsoluteSmiles(IAtomContainer atomContainer) throws CDKException {
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
    } catch (CDKException
        | NullPointerException
        | IllegalArgumentException
        | ArrayIndexOutOfBoundsException anException) {
      LOGGER.error("Reaction SMILES generation failed", anException);
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
   * Creates a CXSMILES with coordinates for the given AtomContainer. Only if all atoms have
   * coordinates SMILES will be returned otherwise null is returned.
   *
   * @param atomContainer AtomContainer for which to generate an absolute SMILES representation
   * @return CXSMILES with coordinates, if all atoms have coordinates, otherwise null.
   */
  public static String createExtendedSmiles(IAtomContainer atomContainer) throws CDKException {
    for (IAtom atom : atomContainer.atoms()) {
      if ((atom.getPoint2d() == null && atom.getPoint3d() == null)
          || hasDuplicateCoordinates(atomContainer)) {
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
   * @param flavor the CDK {@code SmiFlavor} bitmask controlling SMILES generation options
   * @return absolute SMILES string representing the structure of the AtomContainer
   */
  public static String createSmiles(IAtomContainer atomContainer, int flavor) throws CDKException {
    String smiles = null;
    // flag absolute generates a canonical SMILES with stereochemistry and atomic masses (isomers)
    SmilesGenerator smilesGen = new SmilesGenerator(flavor);
    try {
      try {
        smiles = smilesGen.create(atomContainer);
      } catch (CDKException _) {
        IAtomContainer clone = atomContainer.clone();
        Kekulization.kekulize(clone);
        smiles = smilesGen.create(clone);
        LOGGER.info("Kekulized structure: {}", smiles);
      }
    } catch (CDKException
        | NullPointerException
        | IllegalArgumentException
        | CloneNotSupportedException
        | IndexOutOfBoundsException
        | IllegalStateException anException) {
      // IndexOutOfBoundsException comes out of CDK's canonical numbering (InChINumbersTools and
      // Beam) on some organometallic structures. Like every other generator failure it is reported
      // as a CDKException, so a caller loses one structure instead of the whole document.
      LOGGER.error("SMILES generation failed", anException);
      throw new CDKException("Unable to generate SMILES.", anException);
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
    return findNearestResidueAtom(residueAtom, atomContainer, null);
  }

  /**
   * Finds the nearest residue atom to the given residueAtom within the provided AtomContainer,
   * optionally restricted to residues carrying a given label.
   *
   * @param residueAtom reference residue atom
   * @param atomContainer AtomContainer in which to search for the nearest residue atom.
   * @param label when not {@code null}, only pseudo-atoms with this label are considered; a caller
   *     pairing up two halves of one R-group must not pick up an unrelated residue
   * @return The nearest residue atom to the given residueAtom.
   */
  public static IAtom findNearestResidueAtom(
      IAtom residueAtom, IAtomContainer atomContainer, String label) {
    IAtom nearestResidueAtom = null;
    int dist = Integer.MAX_VALUE;
    ShortestPaths path = new ShortestPaths(atomContainer, residueAtom);
    for (IAtom atom : atomContainer.atoms()) {
      if (atom instanceof IPseudoAtom pseudoAtom
          && atom != residueAtom
          && (label == null || label.equals(pseudoAtom.getLabel()))) {
        int pathDist = path.distanceTo(atom);
        if (pathDist < dist) {
          dist = pathDist;
          nearestResidueAtom = atom;
        }
      }
    }
    return nearestResidueAtom;
  }

  /**
   * Parser used only to answer {@link #isValidSmiles}. Constructing a {@link SmilesParser} is far
   * more expensive than the parse itself — it initialises a CDK logging tool reflectively — and
   * validation is called once per candidate substituent, so the parser is kept rather than built
   * per call. {@link SmilesParser} carries per-parse state, hence one instance per thread.
   */
  private static final ThreadLocal<SmilesParser> VALIDATION_PARSER =
      ThreadLocal.withInitial(() -> new SmilesParser(SilentChemObjectBuilder.getInstance()));

  /**
   * A key identifying a structure among those one fragment produces.
   *
   * <p>The atoms, bonds and stereo descriptors written out in the container's own order, not a
   * canonical form. The duplicates this is here to catch are the same substituent combination
   * applied to different position-variation variants of one fragment, which the same code builds
   * the same way and so lays out identically; canonicalising them would cost as much as building
   * the substance the key is meant to avoid building.
   *
   * <p>The bargain this strikes is one-sided on purpose. Two containers sharing a key are the same
   * structure, since the key spells out the whole graph. Two that do not share one may still be the
   * same structure written differently, and are then built twice and merged by InChI at the end of
   * extraction as before — a missed saving, never a lost substance.
   *
   * @param container the structure to key
   * @return the key
   */
  public static String structureKey(IAtomContainer container) {
    StringBuilder key = new StringBuilder(container.getAtomCount() * 8);
    for (IAtom atom : container.atoms()) {
      key.append(atom.getSymbol())
          .append(':')
          .append(atom.getImplicitHydrogenCount())
          .append(':')
          .append(atom.getFormalCharge())
          .append(',');
    }
    // Moving a residue onto the position a legend names drops its bond and appends the new one, so
    // the bond order within the container depends on where the variant had drawn the residue.
    // Sorting removes that difference, which is exactly the one this key must see past.
    key.append('|').append(sorted(bondEntries(container)));
    key.append('|').append(sorted(stereoEntries(container)));
    return key.toString();
  }

  /** One entry per bond, endpoints in a fixed order so a bond reads the same from either end. */
  private static List<String> bondEntries(IAtomContainer container) {
    List<String> entries = new ArrayList<>(container.getBondCount());
    for (IBond bond : container.bonds()) {
      int begin = container.indexOf(bond.getBegin());
      int end = container.indexOf(bond.getEnd());
      entries.add(Math.min(begin, end) + "-" + Math.max(begin, end) + ":" + bond.getOrder());
    }
    return entries;
  }

  /** One entry per stereo element, its focus and carriers named by atom index. */
  private static List<String> stereoEntries(IAtomContainer container) {
    List<String> entries = new ArrayList<>();
    for (IStereoElement<?, ?> stereo : container.stereoElements()) {
      StringBuilder entry = new StringBuilder();
      entry.append(stereo.getConfigClass()).append(':').append(stereo.getConfigOrder()).append(':');
      if (stereo.getFocus() instanceof IAtom focus) {
        entry.append(container.indexOf(focus));
      }
      entry.append(':');
      for (Object carrier : stereo.getCarriers()) {
        entry.append(carrier instanceof IAtom atom ? container.indexOf(atom) : carrier).append('.');
      }
      entries.add(entry.toString());
    }
    return entries;
  }

  /** The entries joined in sorted order, so their order in the container does not reach the key. */
  private static String sorted(List<String> entries) {
    Collections.sort(entries);
    return String.join(",", entries);
  }

  /**
   * Validates whether a given string is a valid SMILES notation using the CDK {@link SmilesParser}.
   *
   * @param smiles the SMILES string to validate
   * @return {@code true} if the string is a valid SMILES, {@code false} otherwise
   */
  public static boolean isValidSmiles(String smiles) {
    try {
      VALIDATION_PARSER.get().parseSmiles(smiles);
      return true;
    } catch (Exception _) {
      return false;
    }
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
