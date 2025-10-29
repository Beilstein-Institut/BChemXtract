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

import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDFont;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;

/**
 * This class holds all common display attributes for a {@link CDObject}. By extracting this
 * informations into a separate object the settings can be easily transferred to other objects.
 */
public class CDSettings {
  public static final float LineHeight_Variable = -1;
  public static final float LineHeight_Automatic = -2;

  /** The color of the objects or the foreground color of the document. */
  private CDColor color;

  /** The background color of the document (not applicable to objects). */
  private CDColor backgroundColor;

  /** The highlight color of the document (not applicable to objects). */
  private CDColor highlightColor;

  /** The width of any line segment in points. */
  private float lineWidth;

  /** The width of a bold or wedge bond in points. */
  private float boldWidth;

  /** The default bond length in points. */
  private float bondLength;

  /** The default bond spacing in percentage of the bond length. */
  private float bondSpacing;

  /** The absolute distance between segments of a multiple bond. */
  private float bondSpacingAbs;

  /** The default chain angle in degrees. */
  private float chainAngle;

  /** The spacing between hash lines in points. */
  private float hashSpacing;

  /** The amount of space surrounding all atom labels in points. */
  private float marginWidth;

  /** The absolute distance between segments of a multiple bond. */
  private CDColor captionColor;

  /** The default font face for captions (non-atom-label text objects). */
  private CDFontFace captionFace = new CDFontFace();

  /** The default font style for captions (non-atom-label text objects). */
  private CDFont captionFont;

  /** The font size of captions. */
  private float captionSize;

  /** The justification used for captions. */
  private CDJustification captionJustification = CDJustification.Left;

  /** The line height used for captions. */
  private float captionLineHeight = LineHeight_Automatic;

  /** Whether text objects should be interpreted as chemically-meaningful where possible. */
  private boolean interpretChemically = true;

  /** The default color for atom labels. */
  private CDColor labelColor;

  /** The default font style for atom labels. */
  private CDFontFace labelFace = new CDFontFace();

  /** The font used for atom labels. */
  private CDFont labelFont;

  /** The font size of atom labels. */
  private float labelSize;

  /** The justification used for atom labels. */
  private CDJustification labelJustification = CDJustification.Left;

  /** The line height used for atom labels. */
  private float labelLineHeight = LineHeight_Variable;

  /**
   * Whether atoms will display associated tags representing the atoms' enhanced stereochemical
   * configuration.
   */
  private boolean showAtomEnhancedStereo = true;

  /** Whether atoms will display associated tags representing the atoms' atom numbers. */
  private boolean showAtomNumber = false;

  /** Whether atoms will display associated tags representing the atoms' query properties. */
  private boolean showAtomQuery = true;

  /**
   * Whether atoms will display associated tags representing the atoms' absolute CIP stereochemical
   * configurations.
   */
  private boolean showAtomStereo = false;

  /** Whether bonds will display associated tags representing the bonds' query properties. */
  private boolean showBondQuery = true;

  /** Whether bonds will display associated tags representing the bonds' reaction-change status. */
  private boolean showBondReaction = true;

  /**
   * Whether bonds will display associated tags representing the bonds' absolute CIP stereochemical
   * configurations.
   */
  private boolean showBondStereo = false;

  /**
   * Whether non-terminal carbons (carbons with more than one bond) should display a text label with
   * the element symbol and appropriate hydrogens.
   */
  private boolean showNonTerminalCarbonLabels = false;

  /**
   * Whether terminal carbons (carbons with zero or one bond) should display a text label with the
   * element symbol and appropriate hydrogens.
   */
  private boolean showTerminalCarbonLabels = false;

  /**
   * Whether implicit hydrogens should be displayed on otherwise-atomic atom labels (NH2 versus N).
   */
  private boolean hideImplicitHydrogens = false;

  public CDColor getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(CDColor backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public float getBoldWidth() {
    return boldWidth;
  }

  public void setBoldWidth(float boldWidth) {
    this.boldWidth = boldWidth;
  }

  public float getBondLength() {
    return bondLength;
  }

  public void setBondLength(float bondLength) {
    this.bondLength = bondLength;
  }

  public float getBondSpacing() {
    return bondSpacing;
  }

  public void setBondSpacing(float bondSpacing) {
    this.bondSpacing = bondSpacing;
  }

  public CDColor getCaptionColor() {
    return captionColor;
  }

  public void setCaptionColor(CDColor captionColor) {
    this.captionColor = captionColor;
  }

  public CDFontFace getCaptionFace() {
    return captionFace;
  }

  public void setCaptionFace(CDFontFace captionFace) {
    this.captionFace = captionFace;
  }

  public CDFont getCaptionFont() {
    return captionFont;
  }

  public void setCaptionFont(CDFont captionFont) {
    this.captionFont = captionFont;
  }

  public CDJustification getCaptionJustification() {
    return captionJustification;
  }

  public void setCaptionJustification(CDJustification captionJustification) {
    this.captionJustification = captionJustification;
  }

  public float getCaptionLineHeight() {
    return captionLineHeight;
  }

  public void setCaptionLineHeight(float captionLineHeight) {
    this.captionLineHeight = captionLineHeight;
  }

  public float getCaptionSize() {
    return captionSize;
  }

  public void setCaptionSize(float captionSize) {
    this.captionSize = captionSize;
  }

  public float getChainAngle() {
    return chainAngle;
  }

  public void setChainAngle(float chainAngle) {
    this.chainAngle = chainAngle;
  }

  public CDColor getColor() {
    return color;
  }

  public void setColor(CDColor color) {
    this.color = color;
  }

  public float getHashSpacing() {
    return hashSpacing;
  }

  public void setHashSpacing(float hashSpacing) {
    this.hashSpacing = hashSpacing;
  }

  public CDColor getHighlightColor() {
    return highlightColor;
  }

  public void setHighlightColor(CDColor highlightColor) {
    this.highlightColor = highlightColor;
  }

  public boolean isHideImplicitHydrogens() {
    return hideImplicitHydrogens;
  }

  public void setHideImplicitHydrogens(boolean hideImplicitHydrogens) {
    this.hideImplicitHydrogens = hideImplicitHydrogens;
  }

  public boolean isInterpretChemically() {
    return interpretChemically;
  }

  public void setInterpretChemically(boolean interpretChemically) {
    this.interpretChemically = interpretChemically;
  }

  public CDColor getLabelColor() {
    return labelColor;
  }

  public void setLabelColor(CDColor labelColor) {
    this.labelColor = labelColor;
  }

  public CDFontFace getLabelFace() {
    return labelFace;
  }

  public void setLabelFace(CDFontFace labelFace) {
    this.labelFace = labelFace;
  }

  public CDFont getLabelFont() {
    return labelFont;
  }

  public void setLabelFont(CDFont labelFont) {
    this.labelFont = labelFont;
  }

  public CDJustification getLabelJustification() {
    return labelJustification;
  }

  public void setLabelJustification(CDJustification labelJustification) {
    this.labelJustification = labelJustification;
  }

  public float getLabelLineHeight() {
    return labelLineHeight;
  }

  public void setLabelLineHeight(float labelLineHeight) {
    this.labelLineHeight = labelLineHeight;
  }

  public float getLabelSize() {
    return labelSize;
  }

  public void setLabelSize(float labelSize) {
    this.labelSize = labelSize;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public void setLineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
  }

  public float getMarginWidth() {
    return marginWidth;
  }

  public void setMarginWidth(float marginWidth) {
    this.marginWidth = marginWidth;
  }

  public boolean isShowAtomEnhancedStereo() {
    return showAtomEnhancedStereo;
  }

  public void setShowAtomEnhancedStereo(boolean showAtomEnhancedStereo) {
    this.showAtomEnhancedStereo = showAtomEnhancedStereo;
  }

  public boolean isShowAtomNumber() {
    return showAtomNumber;
  }

  public void setShowAtomNumber(boolean showAtomNumber) {
    this.showAtomNumber = showAtomNumber;
  }

  public boolean isShowAtomQuery() {
    return showAtomQuery;
  }

  public void setShowAtomQuery(boolean showAtomQuery) {
    this.showAtomQuery = showAtomQuery;
  }

  public boolean isShowAtomStereo() {
    return showAtomStereo;
  }

  public void setShowAtomStereo(boolean showAtomStereo) {
    this.showAtomStereo = showAtomStereo;
  }

  public boolean isShowBondQuery() {
    return showBondQuery;
  }

  public void setShowBondQuery(boolean showBondQuery) {
    this.showBondQuery = showBondQuery;
  }

  public boolean isShowBondReaction() {
    return showBondReaction;
  }

  public void setShowBondReaction(boolean showBondReaction) {
    this.showBondReaction = showBondReaction;
  }

  public boolean isShowBondStereo() {
    return showBondStereo;
  }

  public void setShowBondStereo(boolean showBondStereo) {
    this.showBondStereo = showBondStereo;
  }

  public boolean isShowNonTerminalCarbonLabels() {
    return showNonTerminalCarbonLabels;
  }

  public void setShowNonTerminalCarbonLabels(boolean showNonTerminalCarbonLabels) {
    this.showNonTerminalCarbonLabels = showNonTerminalCarbonLabels;
  }

  public boolean isShowTerminalCarbonLabels() {
    return showTerminalCarbonLabels;
  }

  public void setShowTerminalCarbonLabels(boolean showTerminalCarbonLabels) {
    this.showTerminalCarbonLabels = showTerminalCarbonLabels;
  }

  public float getBondSpacingAbs() {
    return bondSpacingAbs;
  }

  public void setBondSpacingAbs(float bondSpacingAbs) {
    this.bondSpacingAbs = bondSpacingAbs;
  }
}
