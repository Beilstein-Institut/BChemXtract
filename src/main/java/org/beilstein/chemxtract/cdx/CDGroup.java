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

/**
 * An object for grouping several other objects.
 */
public class CDGroup extends CDObject {
  /** The alternative groups collection of this group. */
  private List<CDAltGroup> altGroups = new ArrayList<>();
  /** the captions collection of this group. */
  private List<CDText> captions = new ArrayList<>();
  /** Whether the group must be selected as an entire unit. */
  private boolean integral = false;
  /** The groups collection of this group. */
  private List<CDGroup> groups = new ArrayList<>();

  private List<CDFragment> fragments = new ArrayList<>();
  private List<CDGraphic> graphics = new ArrayList<>();
  private List<CDSpline> curves = new ArrayList<>();
  private List<CDReactionStep> reactionSteps = new ArrayList<>();
  private List<CDSpectrum> spectra = new ArrayList<>();
  private List<CDPicture> embeddedObjects = new ArrayList<>();
  private List<CDArrow> arrows = new ArrayList<>();

  public List<CDGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<CDGroup> groups) {
    this.groups = groups;
  }

  public List<CDFragment> getFragments() {
    return fragments;
  }

  public void setFragments(List<CDFragment> fragments) {
    this.fragments = fragments;
  }

  public List<CDText> getCaptions() {
    return captions;
  }

  public void setCaptions(List<CDText> captions) {
    this.captions = captions;
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

  public List<CDAltGroup> getNamedAlternativeGroups() {
    return altGroups;
  }

  public void setNamedAlternativeGroups(List<CDAltGroup> namedAlternativeGroups) {
    altGroups = namedAlternativeGroups;
  }

  public List<CDReactionStep> getReactionSteps() {
    return reactionSteps;
  }

  public void setReactionSteps(List<CDReactionStep> reactionSteps) {
    this.reactionSteps = reactionSteps;
  }

  public List<CDSpectrum> getSpectra() {
    return spectra;
  }

  public void setSpectra(List<CDSpectrum> spectra) {
    this.spectra = spectra;
  }

  public List<CDPicture> getEmbeddedObjects() {
    return embeddedObjects;
  }

  public void setEmbeddedObjects(List<CDPicture> embeddedObjects) {
    this.embeddedObjects = embeddedObjects;
  }

  public List<CDArrow> getArrows() {
    return arrows;
  }

  public void setArrows(List<CDArrow> arrows) {
    this.arrows = arrows;
  }

  public boolean isIntegral() {
    return integral;
  }

  public void setIntegral(boolean integral) {
    this.integral = integral;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitGroup(this);
    for (CDGroup group : groups) {
      group.accept(visitor);
    }
    for (CDFragment fragment : fragments) {
      fragment.accept(visitor);
    }
    for (CDGraphic graphic : graphics) {
      graphic.accept(visitor);
    }
    for (CDSpline curve : curves) {
      curve.accept(visitor);
    }
    for (CDText text : captions) {
      text.accept(visitor);
    }
    for (CDArrow arrow : arrows) {
      arrow.accept(visitor);
    }
    for (CDAltGroup namedAlternativeGroup : altGroups) {
      namedAlternativeGroup.accept(visitor);
    }
    for (CDSpectrum spectrum : spectra) {
      spectrum.accept(visitor);
    }
    for (CDPicture embeddedObject : embeddedObjects) {
      embeddedObject.accept(visitor);
    }
    super.accept(visitor);
  }

}
