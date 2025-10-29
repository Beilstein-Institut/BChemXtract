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

/**
 * This class is used to build the "Visitor" pattern for {@link CDObject}s. For creating specific
 * visitors, implement a subclass, override the method you are interested in and pass the object to
 * the accept method of {@link CDPage}.
 */
public class CDVisitor {
  public void visitDocument(CDDocument document) {
    // to be implemented in subclasses
  }

  public void visitPage(CDPage page) {
    // to be implemented in subclasses
  }

  public void visitGroup(CDGroup group) {
    // to be implemented in subclasses
  }

  public void visitFragment(CDFragment fragment) {
    // to be implemented in subclasses
  }

  public void visitAtom(CDAtom node) {
    // to be implemented in subclasses
  }

  public void visitBond(CDBond bond) {
    // to be implemented in subclasses
  }

  public void visitText(CDText text) {
    // to be implemented in subclasses
  }

  public void visitGraphic(CDGraphic graphic) {
    // to be implemented in subclasses
  }

  public void visitBracketedGroup(CDBracket bracketedGroup) {
    // to be implemented in subclasses
  }

  public void visitBracketAttachment(CDBracketAttachment bracketAttachment) {
    // to be implemented in subclasses
  }

  public void visitCrossingBond(CDCrossingBond crossingBond) {
    // to be implemented in subclasses
  }

  public void visitCurve(CDSpline curve) {
    // to be implemented in subclasses
  }

  public void visitEmbeddedObject(CDPicture embeddedObject) {
    // to be implemented in subclasses
  }

  public void visitTable(CDTable table) {
    // to be implemented in subclasses
  }

  public void visitNamedAlternativeGroup(CDAltGroup namedAlternativeGroup) {
    // to be implemented in subclasses
  }

  public void visitTemplateGrid(CDTemplateGrid templateGrid) {
    // to be implemented in subclasses
  }

  public void visitReactionScheme(CDReactionScheme reactionScheme) {
    // to be implemented in subclasses
  }

  public void visitReactionStep(CDReactionStep reactionStep) {
    // to be implemented in subclasses
  }

  public void visitSpectrum(CDSpectrum spectrum) {
    // to be implemented in subclasses
  }

  public void visitObjectTag(CDObjectTag objectTag) {
    // to be implemented in subclasses
  }

  public void visitSequence(CDSequence sequence) {
    // to be implemented in subclasses
  }

  public void visitCrossReference(CDCrossReference crossReference) {
    // to be implemented in subclasses
  }

  public void visitBorder(CDBorder border) {
    // to be implemented in subclasses
  }

  public void visitGeometry(CDGeometry geometry) {
    // to be implemented in subclasses
  }

  public void visitConstraint(CDConstraint constraint) {
    // to be implemented in subclasses
  }

  public void visitTLCPlate(CDTLCPlate tlcPlate) {
    // to be implemented in subclasses
  }

  public void visitTLCLane(CDTLCLane tlcLane) {
    // to be implemented in subclasses
  }

  public void visitTLCSpot(CDTLCSpot tlcSpot) {
    // to be implemented in subclasses
  }

  public void visitSplitter(CDSplitter splitter) {
    // to be implemented in subclasses
  }

  public void visitChemicalProperty(CDChemicalProperty chemicalProperty) {
    // to be implemented in subclasses
  }

  public void visitColor(CDColor color) {
    // to be implemented in subclasses
  }

  public void visitFont(CDFont font) {
    // to be implemented in subclasses
  }

  public void visitArrow(CDArrow arrow) {
    // to be implemented in subclasses
  }
}
