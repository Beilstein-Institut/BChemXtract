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
import java.util.Collections;
import java.util.List;

/** An object for grouping several other objects. */
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
    return Collections.unmodifiableList(groups);
  }

  public void setGroups(List<CDGroup> groups) {
    this.groups = groups == null ? new ArrayList<>() : new ArrayList<>(groups);
  }

  public void addGroup(CDGroup group) {
    this.groups.add(group);
  }

  public List<CDFragment> getFragments() {
    return Collections.unmodifiableList(fragments);
  }

  public void setFragments(List<CDFragment> fragments) {
    this.fragments = fragments == null ? new ArrayList<>() : new ArrayList<>(fragments);
  }

  public void addFragment(CDFragment fragment) {
    this.fragments.add(fragment);
  }

  public List<CDText> getCaptions() {
    return Collections.unmodifiableList(captions);
  }

  public void setCaptions(List<CDText> captions) {
    this.captions = captions == null ? new ArrayList<>() : new ArrayList<>(captions);
  }

  public void addCaption(CDText caption) {
    this.captions.add(caption);
  }

  public List<CDGraphic> getGraphics() {
    return Collections.unmodifiableList(graphics);
  }

  public void setGraphics(List<CDGraphic> graphics) {
    this.graphics = graphics == null ? new ArrayList<>() : new ArrayList<>(graphics);
  }

  public void addGraphic(CDGraphic graphic) {
    this.graphics.add(graphic);
  }

  public List<CDSpline> getCurves() {
    return Collections.unmodifiableList(curves);
  }

  public void setCurves(List<CDSpline> curves) {
    this.curves = curves == null ? new ArrayList<>() : new ArrayList<>(curves);
  }

  public void addCurve(CDSpline curve) {
    this.curves.add(curve);
  }

  public List<CDAltGroup> getNamedAlternativeGroups() {
    return Collections.unmodifiableList(altGroups);
  }

  public void setNamedAlternativeGroups(List<CDAltGroup> namedAlternativeGroups) {
    altGroups =
        namedAlternativeGroups == null
            ? new ArrayList<>()
            : new ArrayList<>(namedAlternativeGroups);
  }

  public void addNamedAlternativeGroup(CDAltGroup namedAlternativeGroup) {
    this.altGroups.add(namedAlternativeGroup);
  }

  public List<CDReactionStep> getReactionSteps() {
    return Collections.unmodifiableList(reactionSteps);
  }

  public void setReactionSteps(List<CDReactionStep> reactionSteps) {
    this.reactionSteps = reactionSteps == null ? new ArrayList<>() : new ArrayList<>(reactionSteps);
  }

  public void addReactionStep(CDReactionStep reactionStep) {
    this.reactionSteps.add(reactionStep);
  }

  public List<CDSpectrum> getSpectra() {
    return Collections.unmodifiableList(spectra);
  }

  public void setSpectra(List<CDSpectrum> spectra) {
    this.spectra = spectra == null ? new ArrayList<>() : new ArrayList<>(spectra);
  }

  public void addSpectrum(CDSpectrum spectrum) {
    this.spectra.add(spectrum);
  }

  public List<CDPicture> getEmbeddedObjects() {
    return Collections.unmodifiableList(embeddedObjects);
  }

  public void setEmbeddedObjects(List<CDPicture> embeddedObjects) {
    this.embeddedObjects =
        embeddedObjects == null ? new ArrayList<>() : new ArrayList<>(embeddedObjects);
  }

  public void addEmbeddedObject(CDPicture embeddedObject) {
    this.embeddedObjects.add(embeddedObject);
  }

  public List<CDArrow> getArrows() {
    return Collections.unmodifiableList(arrows);
  }

  public void setArrows(List<CDArrow> arrows) {
    this.arrows = arrows == null ? new ArrayList<>() : new ArrayList<>(arrows);
  }

  public void addArrow(CDArrow arrow) {
    this.arrows.add(arrow);
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
