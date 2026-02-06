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
package org.beilstein.chemxtract.cdx;

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.datatypes.*;

/** Basic part of a substance, usually denoting a single atom. */
public class CDAtom extends CDObject {
  /** The abnormal valence restriction on the atom. */
  private boolean abnormalValenceAllowed = false;

  /** The geometry of the atom. */
  private CDAtomGeometry atomGeometry = CDAtomGeometry.Unknown;

  /** The atom number of the atom. */
  private String atomNumber;

  /** the collection of attached atoms, for multi-center atoms and variable attachment points. */
  private List<CDAtom> attachedAtoms;

  /** The type of the external attachment point, for atoms that are external attachment points. */
  private CDExternalConnectionType attachmentPointType = CDExternalConnectionType.Unspecified;

  /** The charge of the atom. */
  private int charge;

  /** The element number of the atom. */
  private int elementNumber = 6;

  /** The group number (associated with Or and And enhanced stereochemistry types) on the atom. */
  private int enhancedStereoGroupNumber;

  /** The type of enhanced stereochemistry on the atom. */
  private CDEnhancedStereoType enhancedStereoType;

  /** whether the atom has an H-dash indicator. */
  private boolean hDash = false;

  /** Whether the atom has an H-dot indicator. */
  private boolean hDot = false;

  /** The implicit hydrogen restriction on the atom. */
  private boolean implicitHydrogensAllowed = false;

  /** The isotope of the atom. */
  private int isotope;

  /** The isotopic abundance restriction on the atom. */
  private CDIsotopicAbundance isotopicAbundance = CDIsotopicAbundance.Unspecified;

  /** The label display type of the atom. */
  private CDLabelDisplay labelDisplay = CDLabelDisplay.Auto;

  /** The text of the atom's label. */
  private String labelText;

  /** The high end of the link count range, for link nodes. */
  private int linkCountHigh;

  /** The low end of the link count range, for link nodes. */
  private int linkCountLow;

  /** The type of the atom. */
  private CDNodeType nodeType = CDNodeType.Element;

  /** The number of implicit hydrogens on the atom. */
  private int numImplicitHydrogens;

  /** The radical of the atom. */
  private CDRadical radical = CDRadical.None;

  /** The reaction stereo restriction on the atom (feature of a query atom). */
  private CDReactionStereo reactionStereo = CDReactionStereo.Unspecified;

  /** The reaction change restriction on the atom (feature of a query atom). */
  private boolean restrictReactionChange = false;

  /** The ring bong limit of the atom (feature of a query atom). */
  private CDRingBondCount ringBondCount = CDRingBondCount.Unspecified;

  /** The absolute stereochemistry of the atom. */
  private CDAtomCIPType stereochemistry = CDAtomCIPType.Undetermined;

  /** The substitution restriction limit on the atom (feature of a query atom). */
  private int substituentCount;

  /** The type of restricted substitution on the atom (feature of a query atom). */
  private CDAtomSubstituentType substituentType = CDAtomSubstituentType.None;

  /** The translation restriction on the atom (feature related to queries/matching behavior). */
  private CDTranslation translation = CDTranslation.Equal;

  /** The unsaturation restriction on the atom (feature of a query atom). */
  private CDUnsaturation unsaturatedBonds = CDUnsaturation.Unspecified;

  /** The fragment of the superatom/nickname. */
  private List<CDFragment> fragments = new ArrayList<>();

  /** The label of the atom. */
  private CDText text;

  /** 2D coordinates. */
  private CDPoint2D position2D;

  /** 3D coordinates. */
  private CDPoint3D position3D;

  /** List of elements associated with this atom (feature of a query atom). */
  private CDElementList elementList;

  /**
   * The chemical formula. It is not disclosed by CambridgeSoft how this is encoded. Consequently,
   * the byte values are stored.
   */
  private byte[] formula;

  /** Important for retaining stereochemistry. */
  private List<CDBond> bondOrdering;

  /** A container object holding fragments that represent alternative substituents for a query. */
  private CDAltGroup altGroup;

  /** A list of generic nicknames. */
  private CDGenericList genericList;

  public List<CDFragment> getFragments() {
    return fragments;
  }

  public void setFragments(List<CDFragment> fragments) {
    this.fragments = fragments;
  }

  public CDText getText() {
    return text;
  }

  public void setText(CDText text) {
    this.text = text;
  }

  public CDPoint2D getPosition2D() {
    return position2D;
  }

  public void setPosition2D(CDPoint2D position2D) {
    this.position2D = position2D;
  }

  public CDPoint3D getPosition3D() {
    return position3D;
  }

  public void setPosition3D(CDPoint3D position3D) {
    this.position3D = position3D;
  }

  public CDNodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(CDNodeType nodeType) {
    this.nodeType = nodeType;
  }

  public CDLabelDisplay getLabelDisplay() {
    return labelDisplay;
  }

  public void setLabelDisplay(CDLabelDisplay labelDisplay) {
    this.labelDisplay = labelDisplay;
  }

  public int getElementNumber() {
    return elementNumber;
  }

  public void setElementNumber(int element) {
    elementNumber = element;
  }

  public CDElementList getElementList() {
    return elementList;
  }

  public void setElementList(CDElementList elementList) {
    this.elementList = elementList;
  }

  public byte[] getFormula() {
    return formula;
  }

  public void setFormula(byte[] formula) {
    this.formula = formula;
  }

  public int getIsotope() {
    return isotope;
  }

  public void setIsotope(int isotope) {
    this.isotope = isotope;
  }

  public int getCharge() {
    return charge;
  }

  public void setCharge(int charge) {
    this.charge = charge;
  }

  public CDRadical getRadical() {
    return radical;
  }

  public void setRadical(CDRadical radical) {
    this.radical = radical;
  }

  public boolean isImplicitHydrogensAllowed() {
    return implicitHydrogensAllowed;
  }

  public void setImplicitHydrogensAllowed(boolean implicitHydrogensAllowed) {
    this.implicitHydrogensAllowed = implicitHydrogensAllowed;
  }

  public CDRingBondCount getRingBondCount() {
    return ringBondCount;
  }

  public void setRingBondCount(CDRingBondCount ringBondCount) {
    this.ringBondCount = ringBondCount;
  }

  public CDUnsaturation getUnsaturatedBonds() {
    return unsaturatedBonds;
  }

  public void setUnsaturatedBonds(CDUnsaturation unsaturatedBonds) {
    this.unsaturatedBonds = unsaturatedBonds;
  }

  public boolean isRestrictReactionChange() {
    return restrictReactionChange;
  }

  public void setRestrictReactionChange(boolean restrictReactionChange) {
    this.restrictReactionChange = restrictReactionChange;
  }

  public CDReactionStereo getReactionStereo() {
    return reactionStereo;
  }

  public void setReactionStereo(CDReactionStereo reactionStereo) {
    this.reactionStereo = reactionStereo;
  }

  public boolean isAbnormalValenceAllowed() {
    return abnormalValenceAllowed;
  }

  public void setAbnormalValenceAllowed(boolean abnormalValenceAllowed) {
    this.abnormalValenceAllowed = abnormalValenceAllowed;
  }

  public int getNumImplicitHydrogens() {
    return numImplicitHydrogens;
  }

  public void setNumImplicitHydrogens(int numImplicitHydrogens) {
    this.numImplicitHydrogens = numImplicitHydrogens;
  }

  public boolean isHDot() {
    return hDot;
  }

  public void setHDot(boolean dot) {
    hDot = dot;
  }

  public boolean isHDash() {
    return hDash;
  }

  public void setHDash(boolean dash) {
    hDash = dash;
  }

  public CDAtomGeometry getAtomGeometry() {
    return atomGeometry;
  }

  public void setAtomGeometry(CDAtomGeometry atomGeometry) {
    this.atomGeometry = atomGeometry;
  }

  public List<CDBond> getBondOrdering() {
    return bondOrdering;
  }

  public void setBondOrdering(List<CDBond> bondOrdering) {
    this.bondOrdering = bondOrdering;
  }

  public List<CDAtom> getAttachedAtoms() {
    return attachedAtoms;
  }

  public void setAttachedAtoms(List<CDAtom> attachedAtoms) {
    this.attachedAtoms = attachedAtoms;
  }

  public String getLabelText() {
    return labelText;
  }

  public void setLabelText(String labelText) {
    this.labelText = labelText;
  }

  public CDAltGroup getAltGroup() {
    return altGroup;
  }

  public void setAltGroup(CDAltGroup altGroup) {
    this.altGroup = altGroup;
  }

  public int getSubstituentCount() {
    return substituentCount;
  }

  public void setSubstituentCount(int substituentCount) {
    this.substituentCount = substituentCount;
  }

  public CDAtomSubstituentType getSubstituentType() {
    return substituentType;
  }

  public void setSubstituentType(CDAtomSubstituentType substituentType) {
    this.substituentType = substituentType;
  }

  public CDAtomCIPType getStereochemistry() {
    return stereochemistry;
  }

  public void setStereochemistry(CDAtomCIPType stereochemistry) {
    this.stereochemistry = stereochemistry;
  }

  public CDTranslation getTranslation() {
    return translation;
  }

  public void setTranslation(CDTranslation translation) {
    this.translation = translation;
  }

  public String getAtomNumber() {
    return atomNumber;
  }

  public void setAtomNumber(String atomNumber) {
    this.atomNumber = atomNumber;
  }

  public int getLinkCountLow() {
    return linkCountLow;
  }

  public void setLinkCountLow(int linkCountLow) {
    this.linkCountLow = linkCountLow;
  }

  public int getLinkCountHigh() {
    return linkCountHigh;
  }

  public void setLinkCountHigh(int linkCountHigh) {
    this.linkCountHigh = linkCountHigh;
  }

  public CDIsotopicAbundance getIsotopicAbundance() {
    return isotopicAbundance;
  }

  public void setIsotopicAbundance(CDIsotopicAbundance isotopicAbundance) {
    this.isotopicAbundance = isotopicAbundance;
  }

  public CDExternalConnectionType getAttachmentPointType() {
    return attachmentPointType;
  }

  public void setAttachmentPointType(CDExternalConnectionType externalConnectionType) {
    attachmentPointType = externalConnectionType;
  }

  public CDGenericList getGenericList() {
    return genericList;
  }

  public void setGenericList(CDGenericList genericList) {
    this.genericList = genericList;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitAtom(this);
    for (CDFragment fragment : fragments) {
      if (fragment.getBounds() == null) {
        if (this.getBounds() != null) {
          fragment.setBounds(this.getBounds());
        } else if (this.text != null && this.text.getBounds() != null) {
          fragment.setBounds(this.text.getBounds());
        }
      }
      fragment.accept(visitor);
    }
    if (text != null) {
      text.accept(visitor);
    }
    super.accept(visitor);
  }

  public CDAtom() {
    // empty constructor
  }

  public CDAtom(CDAtom template) {
    // Primitive types and enums (direct assignments)
    this.abnormalValenceAllowed = template.abnormalValenceAllowed;
    this.atomGeometry = template.atomGeometry;
    this.attachmentPointType = template.attachmentPointType;
    this.charge = template.charge;
    this.elementNumber = template.elementNumber;
    this.enhancedStereoGroupNumber = template.enhancedStereoGroupNumber;
    this.enhancedStereoType = template.enhancedStereoType;
    this.hDash = template.hDash;
    this.hDot = template.hDot;
    this.implicitHydrogensAllowed = template.implicitHydrogensAllowed;
    this.isotope = template.isotope;
    this.isotopicAbundance = template.isotopicAbundance;
    this.labelDisplay = template.labelDisplay;
    this.linkCountHigh = template.linkCountHigh;
    this.linkCountLow = template.linkCountLow;
    this.nodeType = template.nodeType;
    this.numImplicitHydrogens = template.numImplicitHydrogens;
    this.radical = template.radical;
    this.reactionStereo = template.reactionStereo;
    this.restrictReactionChange = template.restrictReactionChange;
    this.ringBondCount = template.ringBondCount;
    this.stereochemistry = template.stereochemistry;
    this.substituentCount = template.substituentCount;
    this.substituentType = template.substituentType;
    this.translation = template.translation;
    this.unsaturatedBonds = template.unsaturatedBonds;

    // Strings (immutable, safe to copy directly)
    this.atomNumber = template.atomNumber;
    this.labelText = template.labelText;

    // lists
    if (template.fragments != null) {
      this.fragments = new ArrayList<>(template.fragments);
    }
    if (template.bondOrdering != null) {
      this.bondOrdering = new ArrayList<>(template.bondOrdering);
    }

    if (template.attachedAtoms != null) {
      this.attachedAtoms = new ArrayList<>(template.attachedAtoms);
    }

    // Objects (shallow or deep copy depending on whether they are mutable)
    this.text = (template.text != null) ? template.text : null; // Assuming CDText has a copy method
    this.position2D =
        (template.position2D != null)
            ? template.position2D
            : null; // Assuming CDPoint2D has a copy method
    this.position3D =
        (template.position3D != null)
            ? template.position3D
            : null; // Assuming CDPoint3D has a copy method
    this.elementList =
        (template.elementList != null)
            ? template.elementList
            : null; // Assuming CDElementList has a copy method
    this.altGroup =
        (template.altGroup != null)
            ? template.altGroup
            : null; // Assuming CDAltGroup has a copy method
    this.genericList =
        (template.genericList != null)
            ? template.genericList
            : null; // Assuming CDGenericList has a copy method

    // Byte arrays (deep copy)
    if (template.formula != null) {
      this.formula = template.formula.clone();
    }
  }
}
