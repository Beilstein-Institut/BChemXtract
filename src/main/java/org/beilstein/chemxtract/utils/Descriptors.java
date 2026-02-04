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

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.*;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 * Utility class providing static methods to calculate molecular qsar descriptors.
 */
public class Descriptors {

  // Pre-instantiate descriptors to avoid overhead on every method call
  private static final IMolecularDescriptor AROMATIC_ATOMS = new AromaticAtomsCountDescriptor();
  private static final IMolecularDescriptor AROMATIC_BONDS = new AromaticBondsCountDescriptor();
  private static final IMolecularDescriptor ATOM_COUNT = new AtomCountDescriptor();
  private static final IMolecularDescriptor HBOND_ACCEPTOR = new HBondAcceptorCountDescriptor();
  private static final IMolecularDescriptor HBOND_DONOR = new HBondDonorCountDescriptor();
  private static final IMolecularDescriptor LARGEST_PI_SYSTEM = new LargestPiSystemDescriptor();
  private static final IMolecularDescriptor WEIGHT = new WeightDescriptor();
  private static final IMolecularDescriptor XLOGP = new XLogPDescriptor();

  private Descriptors() {
    // hide implicit public constructor
  }

  /**
   * Calculates the number of aromatic atoms in the given atom container.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect for aromatic bonds
   * @return the total number of aromatic bonds found
   */
  public static int getAromaticAtomsCount(IAtomContainer atomContainer) {
    return ((IntegerResult) AROMATIC_ATOMS.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the number of aromatic bonds in the given atom container.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect for aromatic bonds
   * @return the total number of aromatic bonds found
   */
  public static int getAromaticBondsCount(IAtomContainer atomContainer) {
    return ((IntegerResult) AROMATIC_BONDS.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the total number of atoms in the given atom container.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the total count of atoms
   */
  public static int getAtomCount(IAtomContainer atomContainer) {
    return ((IntegerResult) ATOM_COUNT.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the number of hydrogen bond acceptors in the molecule.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the number of H-bond acceptor sites
   */
  public static int getHBondAcceptorCount(IAtomContainer atomContainer) {
    return ((IntegerResult) HBOND_ACCEPTOR.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the number of hydrogen bond donors in the molecule.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the number of H-bond donor sites
   */
  public static int getHBondDonorCount(IAtomContainer atomContainer) {
    return ((IntegerResult) HBOND_DONOR.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the size (number of atoms) of the largest pi system in the molecule.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the number of atoms involved in the largest pi system
   */
  public static int getLargestPiSystemCount(IAtomContainer atomContainer) {
    return ((IntegerResult) LARGEST_PI_SYSTEM.calculate(atomContainer).getValue()).intValue();
  }

  /**
   * Calculates the molecular weight of the atom container.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the molecular weight
   */
  public static double getWeight(IAtomContainer atomContainer) {
    return ((DoubleResult) WEIGHT.calculate(atomContainer).getValue()).doubleValue();
  }

  /**
   * Calculates the monoisotopic mass of the atom container.
   *
   * <p>This method uses {@link AtomContainerManipulator#getMass} with the
   * {@code MonoIsotopic} flag.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the exact monoisotopic mass
   */
  public static double getExactMass(IAtomContainer atomContainer) {
    return AtomContainerManipulator.getMass(atomContainer, AtomContainerManipulator.MonoIsotopic);
  }

  /**
   * Calculates the XLogP (prediction of logP) value for the molecule.
   *
   * @param atomContainer the {@link IAtomContainer} to inspect
   * @return the calculated XLogP value
   */
  public static double getXlogP(IAtomContainer atomContainer) {
    return ((DoubleResult) XLOGP.calculate(atomContainer).getValue()).doubleValue();
  }
}
