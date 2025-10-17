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

import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * The drawing canvas that contains all other objects of the drawing.
 */
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

  public List<CDText> getTexts() {
    return texts;
  }

  public void setTexts(List<CDText> texts) {
    this.texts = texts;
  }

  public List<CDGraphic> getGraphics() {
    return graphics;
  }

  public void setGraphics(List<CDGraphic> graphics) {
    this.graphics = graphics;
  }

  public List<CDBracket> getBracketedGroups() {
    return bracketedGroups;
  }

  public void setBracketedGroups(List<CDBracket> bracketedGroups) {
    this.bracketedGroups = bracketedGroups;
  }

  public List<CDSpline> getCurves() {
    return curves;
  }

  public void setCurves(List<CDSpline> curves) {
    this.curves = curves;
  }

  public List<CDPicture> getEmbeddedObjects() {
    return embeddedObjects;
  }

  public void setEmbeddedObjects(List<CDPicture> embeddedObjects) {
    this.embeddedObjects = embeddedObjects;
  }

  public List<CDTable> getTables() {
    return tables;
  }

  public void setTables(List<CDTable> tables) {
    this.tables = tables;
  }

  public List<CDAltGroup> getNamedAlternativeGroups() {
    return namedAlternativeGroups;
  }

  public void setNamedAlternativeGroups(List<CDAltGroup> namedAlternativeGroups) {
    this.namedAlternativeGroups = namedAlternativeGroups;
  }

  public List<CDReactionScheme> getReactionSchemes() {
    return reactionSchemes;
  }

  public void setReactionSchemes(List<CDReactionScheme> reactionSchemes) {
    this.reactionSchemes = reactionSchemes;
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

  public List<CDSequence> getSequences() {
    return sequences;
  }

  public void setSequences(List<CDSequence> sequences) {
    this.sequences = sequences;
  }

  public List<CDCrossReference> getCrossReferences() {
    return crossReferences;
  }

  public void setCrossReferences(List<CDCrossReference> crossReferences) {
    this.crossReferences = crossReferences;
  }

  public List<CDBorder> getBorders() {
    return borders;
  }

  public void setBorders(List<CDBorder> borders) {
    this.borders = borders;
  }

  public List<CDGeometry> getGeometries() {
    return geometries;
  }

  public void setGeometries(List<CDGeometry> geometries) {
    this.geometries = geometries;
  }

  public List<CDConstraint> getConstraints() {
    return constraints;
  }

  public void setConstraints(List<CDConstraint> constraints) {
    this.constraints = constraints;
  }

  public List<CDTLCPlate> getTLCPlates() {
    return tLCPlates;
  }

  public void setTLCPlates(List<CDTLCPlate> plates) {
    tLCPlates = plates;
  }

  public List<CDSplitter> getSplitters() {
    return splitters;
  }

  public void setSplitters(List<CDSplitter> splitters) {
    this.splitters = splitters;
  }

  public List<CDChemicalProperty> getChemicalProperties() {
    return chemicalProperties;
  }

  public void setChemicalProperties(List<CDChemicalProperty> chemicalProperties) {
    this.chemicalProperties = chemicalProperties;
  }

  public List<CDArrow> getArrows() {
    return arrows;
  }

  public void setArrows(List<CDArrow> arrows) {
    this.arrows = arrows;
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
