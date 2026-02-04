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
package org.beilstein.chemxtract.model;

import java.io.Serializable;
import java.util.*;
import org.openscience.cdk.interfaces.IAtomContainer;

/** A substance with structure and Inchi that could be extracted from a CDX file. */
public class BCXSubstance implements Serializable {

  private static final long serialVersionUID = -7849256075801495625L;

  /** Inchi */
  private String inchi;

  /** Business key */
  private String inchiKey;

  /** Canonical smiles */
  private String smiles;

  /** Extended smiles with coordinates */
  private String extendedSmiles;

  /** Generated IUPAC name */
  private String iupacName;

  /** Generated mol formula */
  private String molecularFormula;

  /** Aux info collected during Inchi generation */
  private String auxInfo;

  /**
   * The atom container being constructed or processed by this converter. Holds the chemical
   * structure representation including atoms, bonds, and associated properties.
   */
  private IAtomContainer atomContainer;

  /** The MDL V3000 format string representation of the chemical structure. */
  private String mdlv3000;

  /** The occurrences of the substance within the manuscript */
  private Set<BCXSubstanceOccurrence> occurrences = new HashSet<BCXSubstanceOccurrence>();

  /**
   * The abbreviations used in this substance. A map of String to String with the SMILES being the
   * key and the text label of the abbreviation being the value
   */
  private Map<String, String> abbreviations = new HashMap<String, String>();

  /** The total number of atoms in the structure that are part of an aromatic system. */
  private int aromaticAtomsCount;

  /** The total number of bonds in the structure that are classified as aromatic. */
  private int aromaticBondsCount;

  /** The total count of all atoms in the structure. */
  private int atomCount;

  /** The number of hydrogen bond acceptor sites in the structure. */
  private int hBondAcceptorCount;

  /** The number of hydrogen bond donor sites in the structure. */
  private int hBondDonorCount;

  /** The number of atoms involved in the largest continuous pi system of the structure. */
  private int largestPiSystemCount;

  /** The calculated molecular weight of the structure. */
  private double weight;

  /** The calculated monoisotopic mass of the structure. */
  private double exactMass;

  /** The predicted XLogP (partition coefficient) value for the structure. */
  private double xLogP;

  public BCXSubstance() {
    super();
  }

  public BCXSubstance(String inchiKey) {
    super();
    this.inchiKey = inchiKey;
  }

  public String getInchi() {
    return inchi;
  }

  public void setInchi(String inchi) {
    this.inchi = inchi;
  }

  public String getInchiKey() {
    return inchiKey;
  }

  public void setInchiKey(String inchiKey) {
    this.inchiKey = inchiKey;
  }

  public String getSmiles() {
    return smiles;
  }

  public void setSmiles(String smiles) {
    this.smiles = smiles;
  }

  public String getExtendedSmiles() {
    return extendedSmiles;
  }

  public void setExtendedSmiles(String extendedSmiles) {
    this.extendedSmiles = extendedSmiles;
  }

  public String getIupacName() {
    return iupacName;
  }

  public void setIupacName(String iupacName) {
    this.iupacName = iupacName;
  }

  public String getMolecularFormula() {
    return molecularFormula;
  }

  public void setMolecularFormula(String molecularFormula) {
    this.molecularFormula = molecularFormula;
  }

  public String getAuxInfo() {
    return auxInfo;
  }

  public void setAuxInfo(String auxInfo) {
    this.auxInfo = auxInfo;
  }

  public Set<BCXSubstanceOccurrence> getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(Set<BCXSubstanceOccurrence> occurrences) {
    this.occurrences = occurrences;
  }

  public void addOccurrence(BCXSubstanceOccurrence occurrence) {
    this.occurrences.add(occurrence);
  }

  public Map<String, String> getAbbreviations() {
    return abbreviations;
  }

  public void setAbbreviations(Map<String, String> abbreviations) {
    this.abbreviations = abbreviations;
  }

  public void addAbbreviation(String smiles, String label) {
    this.abbreviations.put(smiles, label);
  }

  public String getMdlv3000() {
    return mdlv3000;
  }

  public void setMdlv3000(String mdlv3000) {
    this.mdlv3000 = mdlv3000;
  }

  @Override
  public int hashCode() {
    return Objects.hash(inchiKey);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BCXSubstance other = (BCXSubstance) obj;
    return Objects.equals(inchiKey, other.inchiKey);
  }

  public IAtomContainer getAtomContainer() {
    return atomContainer;
  }

  public void setAtomContainer(IAtomContainer atomContainer) {
    this.atomContainer = atomContainer;
  }

  public int getAromaticAtomsCount() {
    return aromaticAtomsCount;
  }

  public void setAromaticAtomsCount(int aromaticAtomsCount) {
    this.aromaticAtomsCount = aromaticAtomsCount;
  }

  public int getAromaticBondsCount() {
    return aromaticBondsCount;
  }

  public void setAromaticBondsCount(int aromaticBondsCount) {
    this.aromaticBondsCount = aromaticBondsCount;
  }

  public int getAtomCount() {
    return atomCount;
  }

  public void setAtomCount(int atomCount) {
    this.atomCount = atomCount;
  }

  public int gethBondAcceptorCount() {
    return hBondAcceptorCount;
  }

  public void sethBondAcceptorCount(int hBondAcceptorCount) {
    this.hBondAcceptorCount = hBondAcceptorCount;
  }

  public int gethBondDonorCount() {
    return hBondDonorCount;
  }

  public void sethBondDonorCount(int hBondDonorCount) {
    this.hBondDonorCount = hBondDonorCount;
  }

  public int getLargestPiSystemCount() {
    return largestPiSystemCount;
  }

  public void setLargestPiSystemCount(int largestPiSystemCount) {
    this.largestPiSystemCount = largestPiSystemCount;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public double getExactMass() {
    return exactMass;
  }

  public void setExactMass(double exactMass) {
    this.exactMass = exactMass;
  }

  public double getxLogP() {
    return xLogP;
  }

  public void setxLogP(double xLogP) {
    this.xLogP = xLogP;
  }
}
