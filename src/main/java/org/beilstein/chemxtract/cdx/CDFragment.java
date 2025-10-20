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

import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDSequenceType;
import org.beilstein.chemxtract.utils.Definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A container with chemical semantics. Usually represents a 
 * chemical substance and contains atoms and bonds.
 */
public class CDFragment extends CDObject {
  private List<CDAtom> atoms = new ArrayList<>();
  private List<CDBond> bonds = new ArrayList<>();
  private List<CDGraphic> graphics = new ArrayList<>();
  private List<CDSpline> curves = new ArrayList<>();
  private List<CDText> texts = new ArrayList<>();
  private List<CDArrow> arrows = new ArrayList<>();
  private List<CDColoredMolecularArea> coloredMolecularAreas = new ArrayList<>();
  /** Indicates whether the fragment is a racemic mixture. */
  private boolean racemic = false;
  /** Indicates whether the stereochemistry is absolute. */
  private boolean absolute = false;
  /** Indicates whether the stereochemistry is relative. */
  private boolean relative = false;
  /** The chemical formula. It is not disclosed by CambridgeSoft how this is encoded. Consequently,
   the byte values are stored. */
  private byte[] formula;
  /** Molecular weight. */
  private double weight;
  /** An ordered list of attachment points within a fragment. */
  private List<CDAtom> connectionOrder = new ArrayList<>();
  /** This property is not documented in the CambridgeSoft documentation; its meaning is currently, unkown. */
  private CDSequenceType sequenceType = CDSequenceType.Unknown;

  public List<CDAtom> getAtoms() {
    return atoms;
  }

  public void setAtoms(List<CDAtom> atoms) {
    this.atoms = atoms;
  }

  public List<CDBond> getBonds() {
    return bonds;
  }

  public void setBonds(List<CDBond> bonds) {
    this.bonds = bonds;
  }

  public List<CDGraphic> getGraphics() {
    return graphics;
  }

  public void setGraphics(List<CDGraphic> graphics) {
    this.graphics = graphics;
  }

  public List<CDSpline> getCurves() {
    return curves;
  }

  public void setCurves(List<CDSpline> curves) {
    this.curves = curves;
  }

  public List<CDText> getTexts() {
    return texts;
  }

  public void setTexts(List<CDText> texts) {
    this.texts = texts;
  }

  public List<CDArrow> getArrows() {
    return arrows;
  }

  public void setArrows(List<CDArrow> arrows) {
    this.arrows = arrows;
  }

  public List<CDColoredMolecularArea> getColoredMolecularAreas() {
    return coloredMolecularAreas;
  }

  public void setColoredMolecularAreas(List<CDColoredMolecularArea> coloredMolecularAreas) {
    this.coloredMolecularAreas = coloredMolecularAreas;
  }

  public boolean isRacemic() {
    return racemic;
  }

  public void setRacemic(boolean racemic) {
    this.racemic = racemic;
  }

  public boolean isAbsolute() {
    return absolute;
  }

  public void setAbsolute(boolean absolute) {
    this.absolute = absolute;
  }

  public boolean isRelative() {
    return relative;
  }

  public void setRelative(boolean relative) {
    this.relative = relative;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public List<CDAtom> getConnectionOrder() {
    return connectionOrder;
  }

  public void setConnectionOrder(List<CDAtom> connectionOrder) {
    this.connectionOrder = connectionOrder;
  }

  public byte[] getFormula() {
    return formula;
  }

  public void setFormula(byte[] formula) {
    this.formula = formula;
  }

  public CDSequenceType getSequenceType() {
    return sequenceType;
  }

  public void setSequenceType(CDSequenceType sequenceType) {
    this.sequenceType = sequenceType;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitFragment(this);
    for (CDAtom node : atoms) {
      node.accept(visitor);
    }
    for (CDBond bond : bonds) {
      bond.accept(visitor);
    }
    for (CDGraphic graphic : graphics) {
      graphic.accept(visitor);
    }
    for (CDSpline spline : curves) {
      spline.accept(visitor);
    }
    for (CDText text : texts) {
      text.accept(visitor);
    }
    for (CDArrow arrow : arrows) {
      arrow.accept(visitor);
    }
    super.accept(visitor);
  }

  public boolean hasRGroup() {
    return atoms.stream()
            .map(a -> a.getText() != null ? a.getText().getText().getText() : null)
            .filter(Objects::nonNull)
            .anyMatch(text -> Definitions.RGROUP_LABEL_PATTERN.matcher(text).find());
  }

  public List<String> getRGroups() {
    return atoms.stream()
            .map(a -> a.getText() != null ? a.getText().getText().getText() : null)
            .filter(Objects::nonNull)
            .filter(text -> Definitions.RGROUP_PATTERN.matcher(text).find())
            .toList();
  }

  /**
   * Checks if fragment is valid.
   *
   * @return {@code true} if fragment is valid (i.e., contains at least two atoms), otherwise {@code false}.
   */
  public boolean isValid() {
    return this.atoms.size() >= 2 ||
            this.atoms.stream().anyMatch(a ->
                    (a.getNodeType() == CDNodeType.Element ||
                      a.getNodeType() == CDNodeType.GenericNickname ||
                      a.getNodeType() == CDNodeType.Fragment ||
                      a.getNodeType() == CDNodeType.Nickname ||
                      a.getNodeType() == CDNodeType.AnonymousAlternativeGroup) &&
                            a.getChemicalWarning() == null
            );
  }
}
