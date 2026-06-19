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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to build the "Visitor" pattern for {@link CDObject}s. For creating specific
 * visitors, implement a subclass, override the method you are interested in and pass the object to
 * the accept method of {@link CDPage}.
 *
 * <p>Methods on this base class are intentional no-ops: they exist to provide a default for
 * subclasses that only override a subset of the visitor protocol. Each method delegates to {@link
 * #visitDefault(Object)} so subclasses may also override that single hook to react to every
 * otherwise-unhandled visit.
 */
public class CDVisitor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CDVisitor.class);

  /**
   * Default callback invoked from every unoverridden {@code visitXxx} method. The base
   * implementation only emits a TRACE-level log of the visited object; subclasses may override it
   * to handle the otherwise-unhandled cases.
   *
   * @param object the visited object
   */
  protected void visitDefault(Object object) {
    LOGGER.trace("Unhandled visit: {}", object);
  }

  public void visitDocument(CDDocument document) {
    visitDefault(document);
  }

  public void visitPage(CDPage page) {
    visitDefault(page);
  }

  public void visitGroup(CDGroup group) {
    visitDefault(group);
  }

  public void visitFragment(CDFragment fragment) {
    visitDefault(fragment);
  }

  public void visitAtom(CDAtom node) {
    visitDefault(node);
  }

  public void visitBond(CDBond bond) {
    visitDefault(bond);
  }

  public void visitText(CDText text) {
    visitDefault(text);
  }

  public void visitGraphic(CDGraphic graphic) {
    visitDefault(graphic);
  }

  public void visitBracketedGroup(CDBracket bracketedGroup) {
    visitDefault(bracketedGroup);
  }

  public void visitBracketAttachment(CDBracketAttachment bracketAttachment) {
    visitDefault(bracketAttachment);
  }

  public void visitCrossingBond(CDCrossingBond crossingBond) {
    visitDefault(crossingBond);
  }

  public void visitCurve(CDSpline curve) {
    visitDefault(curve);
  }

  public void visitEmbeddedObject(CDPicture embeddedObject) {
    visitDefault(embeddedObject);
  }

  public void visitTable(CDTable table) {
    visitDefault(table);
  }

  public void visitNamedAlternativeGroup(CDAltGroup namedAlternativeGroup) {
    visitDefault(namedAlternativeGroup);
  }

  public void visitTemplateGrid(CDTemplateGrid templateGrid) {
    visitDefault(templateGrid);
  }

  public void visitReactionScheme(CDReactionScheme reactionScheme) {
    visitDefault(reactionScheme);
  }

  public void visitReactionStep(CDReactionStep reactionStep) {
    visitDefault(reactionStep);
  }

  public void visitSpectrum(CDSpectrum spectrum) {
    visitDefault(spectrum);
  }

  public void visitObjectTag(CDObjectTag objectTag) {
    visitDefault(objectTag);
  }

  public void visitSequence(CDSequence sequence) {
    visitDefault(sequence);
  }

  public void visitCrossReference(CDCrossReference crossReference) {
    visitDefault(crossReference);
  }

  public void visitBorder(CDBorder border) {
    visitDefault(border);
  }

  public void visitGeometry(CDGeometry geometry) {
    visitDefault(geometry);
  }

  public void visitConstraint(CDConstraint constraint) {
    visitDefault(constraint);
  }

  public void visitTLCPlate(CDTLCPlate tlcPlate) {
    visitDefault(tlcPlate);
  }

  public void visitTLCLane(CDTLCLane tlcLane) {
    visitDefault(tlcLane);
  }

  public void visitTLCSpot(CDTLCSpot tlcSpot) {
    visitDefault(tlcSpot);
  }

  public void visitSplitter(CDSplitter splitter) {
    visitDefault(splitter);
  }

  public void visitChemicalProperty(CDChemicalProperty chemicalProperty) {
    visitDefault(chemicalProperty);
  }

  public void visitColor(CDColor color) {
    visitDefault(color);
  }

  public void visitFont(CDFont font) {
    visitDefault(font);
  }

  public void visitArrow(CDArrow arrow) {
    visitDefault(arrow);
  }
}
