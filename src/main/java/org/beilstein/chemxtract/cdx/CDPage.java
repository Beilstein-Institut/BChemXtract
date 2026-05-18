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
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;

/** The drawing canvas that contains all other objects of the drawing. */
public class CDPage extends CDObject {
  private List<CDGroup> groups = new ArrayList<>();
  private List<CDFragment> fragments = new ArrayList<>();
  private List<CDText> texts = new ArrayList<>();
  private List<CDGraphic> graphics = new ArrayList<>();
  private List<CDBracket> bracketedGroups = new ArrayList<>();
  private List<CDSpline> curves = new ArrayList<>();
  private List<CDPicture> embeddedObjects = new ArrayList<>();
  private List<CDTable> tables = new ArrayList<>();
  private List<CDAltGroup> namedAlternativeGroups = new ArrayList<>();
  private List<CDReactionScheme> reactionSchemes = new ArrayList<>();
  private List<CDReactionStep> reactionSteps = new ArrayList<>();
  private List<CDSpectrum> spectra = new ArrayList<>();
  private List<CDSequence> sequences = new ArrayList<>();
  private List<CDCrossReference> crossReferences = new ArrayList<>();
  private List<CDBorder> borders = new ArrayList<>();
  private List<CDGeometry> geometries = new ArrayList<>();
  private List<CDConstraint> constraints = new ArrayList<>();
  private List<CDTLCPlate> tLCPlates = new ArrayList<>();
  private List<CDSplitter> splitters = new ArrayList<>();
  private List<CDChemicalProperty> chemicalProperties = new ArrayList<>();
  private List<CDArrow> arrows = new ArrayList<>();

  private int widthPages = 1;
  private int heightPages = 1;
  private CDDrawingSpaceType drawingSpaceType = CDDrawingSpaceType.Pages;
  private float width;
  private float height;
  private float pageOverlap;
  private String header;
  private float headerPosition = 36.0f;
  private String footer;
  private float footerPosition = 36.0f;
  private boolean printTrimMarks = true;
  private CDPageDefinition pageDefinition = CDPageDefinition.Undefined;
  private CDRectangle boundsInParent;

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

  public List<CDText> getTexts() {
    return Collections.unmodifiableList(texts);
  }

  public void setTexts(List<CDText> texts) {
    this.texts = texts == null ? new ArrayList<>() : new ArrayList<>(texts);
  }

  public void addText(CDText text) {
    this.texts.add(text);
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

  public List<CDBracket> getBracketedGroups() {
    return Collections.unmodifiableList(bracketedGroups);
  }

  public void setBracketedGroups(List<CDBracket> bracketedGroups) {
    this.bracketedGroups =
        bracketedGroups == null ? new ArrayList<>() : new ArrayList<>(bracketedGroups);
  }

  public void addBracketedGroup(CDBracket bracketedGroup) {
    this.bracketedGroups.add(bracketedGroup);
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

  public List<CDTable> getTables() {
    return Collections.unmodifiableList(tables);
  }

  public void setTables(List<CDTable> tables) {
    this.tables = tables == null ? new ArrayList<>() : new ArrayList<>(tables);
  }

  public void addTable(CDTable table) {
    this.tables.add(table);
  }

  public List<CDAltGroup> getNamedAlternativeGroups() {
    return Collections.unmodifiableList(namedAlternativeGroups);
  }

  public void setNamedAlternativeGroups(List<CDAltGroup> namedAlternativeGroups) {
    this.namedAlternativeGroups =
        namedAlternativeGroups == null
            ? new ArrayList<>()
            : new ArrayList<>(namedAlternativeGroups);
  }

  public void addNamedAlternativeGroup(CDAltGroup namedAlternativeGroup) {
    this.namedAlternativeGroups.add(namedAlternativeGroup);
  }

  public List<CDReactionScheme> getReactionSchemes() {
    return Collections.unmodifiableList(reactionSchemes);
  }

  public void setReactionSchemes(List<CDReactionScheme> reactionSchemes) {
    this.reactionSchemes =
        reactionSchemes == null ? new ArrayList<>() : new ArrayList<>(reactionSchemes);
  }

  public void addReactionScheme(CDReactionScheme reactionScheme) {
    this.reactionSchemes.add(reactionScheme);
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

  public List<CDSequence> getSequences() {
    return Collections.unmodifiableList(sequences);
  }

  public void setSequences(List<CDSequence> sequences) {
    this.sequences = sequences == null ? new ArrayList<>() : new ArrayList<>(sequences);
  }

  public void addSequence(CDSequence sequence) {
    this.sequences.add(sequence);
  }

  public List<CDCrossReference> getCrossReferences() {
    return Collections.unmodifiableList(crossReferences);
  }

  public void setCrossReferences(List<CDCrossReference> crossReferences) {
    this.crossReferences =
        crossReferences == null ? new ArrayList<>() : new ArrayList<>(crossReferences);
  }

  public void addCrossReference(CDCrossReference crossReference) {
    this.crossReferences.add(crossReference);
  }

  public List<CDBorder> getBorders() {
    return Collections.unmodifiableList(borders);
  }

  public void setBorders(List<CDBorder> borders) {
    this.borders = borders == null ? new ArrayList<>() : new ArrayList<>(borders);
  }

  public void addBorder(CDBorder border) {
    this.borders.add(border);
  }

  public List<CDGeometry> getGeometries() {
    return Collections.unmodifiableList(geometries);
  }

  public void setGeometries(List<CDGeometry> geometries) {
    this.geometries = geometries == null ? new ArrayList<>() : new ArrayList<>(geometries);
  }

  public void addGeometry(CDGeometry geometry) {
    this.geometries.add(geometry);
  }

  public List<CDConstraint> getConstraints() {
    return Collections.unmodifiableList(constraints);
  }

  public void setConstraints(List<CDConstraint> constraints) {
    this.constraints = constraints == null ? new ArrayList<>() : new ArrayList<>(constraints);
  }

  public void addConstraint(CDConstraint constraint) {
    this.constraints.add(constraint);
  }

  public List<CDTLCPlate> getTLCPlates() {
    return Collections.unmodifiableList(tLCPlates);
  }

  public void setTLCPlates(List<CDTLCPlate> plates) {
    tLCPlates = plates == null ? new ArrayList<>() : new ArrayList<>(plates);
  }

  public void addTLCPlate(CDTLCPlate plate) {
    this.tLCPlates.add(plate);
  }

  public List<CDSplitter> getSplitters() {
    return Collections.unmodifiableList(splitters);
  }

  public void setSplitters(List<CDSplitter> splitters) {
    this.splitters = splitters == null ? new ArrayList<>() : new ArrayList<>(splitters);
  }

  public void addSplitter(CDSplitter splitter) {
    this.splitters.add(splitter);
  }

  public List<CDChemicalProperty> getChemicalProperties() {
    return Collections.unmodifiableList(chemicalProperties);
  }

  public void setChemicalProperties(List<CDChemicalProperty> chemicalProperties) {
    this.chemicalProperties =
        chemicalProperties == null ? new ArrayList<>() : new ArrayList<>(chemicalProperties);
  }

  public void addChemicalProperty(CDChemicalProperty chemicalProperty) {
    this.chemicalProperties.add(chemicalProperty);
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

  public int getWidthPages() {
    return widthPages;
  }

  public void setWidthPages(int widthPages) {
    this.widthPages = widthPages;
  }

  public int getHeightPages() {
    return heightPages;
  }

  public void setHeightPages(int heightPages) {
    this.heightPages = heightPages;
  }

  public CDDrawingSpaceType getDrawingSpaceType() {
    return drawingSpaceType;
  }

  public void setDrawingSpaceType(CDDrawingSpaceType drawingSpaceType) {
    this.drawingSpaceType = drawingSpaceType;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    this.width = width;
  }

  public float getHeight() {
    return height;
  }

  public void setHeight(float height) {
    this.height = height;
  }

  public float getPageOverlap() {
    return pageOverlap;
  }

  public void setPageOverlap(float pageOverlap) {
    this.pageOverlap = pageOverlap;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public float getHeaderPosition() {
    return headerPosition;
  }

  public void setHeaderPosition(float headerPosition) {
    this.headerPosition = headerPosition;
  }

  public String getFooter() {
    return footer;
  }

  public void setFooter(String footer) {
    this.footer = footer;
  }

  public float getFooterPosition() {
    return footerPosition;
  }

  public void setFooterPosition(float footerPosition) {
    this.footerPosition = footerPosition;
  }

  public boolean isPrintTrimMarks() {
    return printTrimMarks;
  }

  public void setPrintTrimMarks(boolean printTrimMarks) {
    this.printTrimMarks = printTrimMarks;
  }

  public CDPageDefinition getPageDefinition() {
    return pageDefinition;
  }

  public void setPageDefinition(CDPageDefinition pageDefinition) {
    this.pageDefinition = pageDefinition;
  }

  public CDRectangle getBoundsInParent() {
    return boundsInParent;
  }

  public void setBoundsInParent(CDRectangle boundsInParent) {
    this.boundsInParent = boundsInParent;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitPage(this);
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
    for (CDBracket bracketedGroup : bracketedGroups) {
      bracketedGroup.accept(visitor);
    }
    for (CDText text : texts) {
      text.accept(visitor);
    }
    for (CDArrow arrow : arrows) {
      arrow.accept(visitor);
    }
    for (CDAltGroup namedAlternativeGroup : namedAlternativeGroups) {
      namedAlternativeGroup.accept(visitor);
    }
    for (CDSpectrum spectrum : spectra) {
      spectrum.accept(visitor);
    }
    for (CDPicture embeddedObject : embeddedObjects) {
      embeddedObject.accept(visitor);
    }
    for (CDTable table : tables) {
      table.accept(visitor);
    }
    for (CDGeometry geometry : geometries) {
      geometry.accept(visitor);
    }
    for (CDConstraint constraint : constraints) {
      constraint.accept(visitor);
    }
    for (CDTLCPlate tlcPlate : tLCPlates) {
      tlcPlate.accept(visitor);
    }
    for (CDReactionScheme reactionScheme : reactionSchemes) {
      reactionScheme.accept(visitor);
    }
  }
}
