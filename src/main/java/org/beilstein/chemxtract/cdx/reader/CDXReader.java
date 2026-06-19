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
package org.beilstein.chemxtract.cdx.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.DataFormatException;
import org.beilstein.chemxtract.cdx.CDAltGroup;
import org.beilstein.chemxtract.cdx.CDArrow;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDBorder;
import org.beilstein.chemxtract.cdx.CDBracket;
import org.beilstein.chemxtract.cdx.CDBracketAttachment;
import org.beilstein.chemxtract.cdx.CDChemicalProperty;
import org.beilstein.chemxtract.cdx.CDColoredMolecularArea;
import org.beilstein.chemxtract.cdx.CDConstraint;
import org.beilstein.chemxtract.cdx.CDCrossReference;
import org.beilstein.chemxtract.cdx.CDCrossingBond;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDGeometry;
import org.beilstein.chemxtract.cdx.CDGraphic;
import org.beilstein.chemxtract.cdx.CDGroup;
import org.beilstein.chemxtract.cdx.CDObject;
import org.beilstein.chemxtract.cdx.CDObjectTag;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDPicture;
import org.beilstein.chemxtract.cdx.CDReactionScheme;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDSequence;
import org.beilstein.chemxtract.cdx.CDSpectrum;
import org.beilstein.chemxtract.cdx.CDSpline;
import org.beilstein.chemxtract.cdx.CDSplitter;
import org.beilstein.chemxtract.cdx.CDTLCLane;
import org.beilstein.chemxtract.cdx.CDTLCPlate;
import org.beilstein.chemxtract.cdx.CDTLCSpot;
import org.beilstein.chemxtract.cdx.CDTable;
import org.beilstein.chemxtract.cdx.CDTemplateGrid;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomSubstituentType;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDFont;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.beilstein.chemxtract.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader for ChemDraw CDX files. Converts a binary file into an in-memory tree of model objects.
 */
public class CDXReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(CDXReader.class);

  private RefManager refManager = new RefManager();
  private Map<Integer, CDColor> colors = new HashMap<>();
  private Map<Integer, CDFont> fonts = new HashMap<>();

  protected static final boolean RIGID = false;

  private CDXReader() {}

  /**
   * This method reads a {@link CDDocument} from a {@link InputStream}.
   *
   * @param in {@link InputStream} from which the input are read
   * @return ChemDraw document instance
   * @throws IOException Occurs if the reader couldn't read the input from the {@link InputStream}
   * @throws IOException Occurs if an exception occur during the generation of the instance
   */
  public static CDDocument readDocument(InputStream in) throws IOException, IOException {
    byte[] bytes = IOUtils.readBytes(in);
    CDXReader reader = new CDXReader();
    LOGGER.debug("Create object tree");
    CDXObject object = CDXUtils.readCDXDocument(bytes, new int[] {0});

    LOGGER.debug("Create model tree");
    CDDocument document = reader.createDocumentObject(object);

    LOGGER.debug("Populate model tree");
    reader.populateDocumentObject(object);

    LOGGER.debug("Finished reading document");
    return document;
  }

  private CDDocument createDocumentObject(CDXObject root) throws IOException {
    handleCreation("document", root);
    CDDocument document = new CDDocument();

    handleReference(root, document);

    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Page:
          document.addPage(createPageObject(object));
          break;
        case CDXConstants.CDXObj_TemplateGrid:
          if (document.getTemplateGrid() != null) {
            throw new IOException("Multiple instances of template grid not allowed");
          }
          document.setTemplateGrid(createTemplateGridObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }

    return document;
  }

  private void populateDocumentObject(CDXObject root) throws IOException {
    handlePopulation("document", root);
    CDDocument document = (CDDocument) root.getInstance();

    // first color table
    for (CDXProperty property : root.getProperties()) {
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ColorTable:
          colors = property.getDataAsColorTable();
          // Color 2 & 3 are the standard foreground and background color
          document.getSettings().setColor(colors.get(3));
          document.getSettings().setBackgroundColor(colors.get(2));
          break;
        default:
          break;
      }
    }

    // second font table
    for (CDXProperty property : root.getProperties()) {
      switch (property.getTag()) {
        case CDXConstants.CDXProp_FontTable:
          fonts = property.getDataAsFontTable();
          break;
        default:
          break;
      }
    }

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_CreationUserName:
          document.setCreationUserName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_CreationDate:
          document.setCreationDate(property.getDataAsDate());
          break;
        case CDXConstants.CDXProp_CreationProgram:
          document.setCreationProgram(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_ModificationUserName:
          document.setModificationUserName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_ModificationDate:
          document.setModificationDate(property.getDataAsDate());
          break;
        case CDXConstants.CDXProp_ModificationProgram:
          document.setModificationProgram(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Name:
          document.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Comment:
          document.setComment(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_FontTable:
          // ignore here
          break;
        case CDXConstants.CDXProp_BoundingBox:
          document.setBoundingBox(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ColorTable:
          // ignore here
          break;
        case CDXConstants.CDXProp_Atom_ShowQuery:
          document.getSettings().setShowAtomQuery(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_ShowStereo:
          document.getSettings().setShowAtomStereo(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_ShowAtomNumber:
          document.getSettings().setShowAtomNumber(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Bond_ShowQuery:
          document.getSettings().setShowBondQuery(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Bond_ShowStereo:
          document.getSettings().setShowBondStereo(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Bond_ShowRxn:
          document.getSettings().setShowBondReaction(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_LabelLineHeight:
          document.getSettings().setLabelLineHeight(CDXUtils.readLineHeight(property));
          break;
        case CDXConstants.CDXProp_CaptionLineHeight:
          document.getSettings().setCaptionLineHeight(CDXUtils.readLineHeight(property));
          break;
        case CDXConstants.CDXProp_InterpretChemically:
          document.getSettings().setInterpretChemically(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_MacPrintInfo:
          document.setMacPrintInfo(property.getData());
          break;
        case CDXConstants.CDXProp_WinPrintInfo:
          document.setWinPrintInfo(property.getData());
          break;
        case CDXConstants.CDXProp_PrintMargins:
          document.setPrintMargins(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ChainAngle:
          document.getSettings().setChainAngle(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_BondSpacing:
          document.getSettings().setBondSpacing(property.getDataAsInt16() / 10f);
          break;
        case CDXConstants.CDXProp_BondSpacingAbs:
          document.getSettings().setBondSpacingAbs(Math.max(property.getDataAsCoordinate(), 0f));
          break;
        case CDXConstants.CDXProp_BondLength:
          document.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_BoldWidth:
          document.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          document.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_MarginWidth:
          document.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          document.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          document.getSettings().setLabelFont(fontStyle.getFont());
          document.getSettings().setLabelSize(fontStyle.getSize());
          document.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionStyle:
          fontStyle = property.getDataAsFontStyle(fonts, colors);
          document.getSettings().setCaptionFont(fontStyle.getFont());
          document.getSettings().setCaptionSize(fontStyle.getSize());
          document.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionJustification:
          document
              .getSettings()
              .setCaptionJustification(CDXUtils.readTextJustificationProperty(property));
          break;
        case CDXConstants.CDXProp_FractionalWidths:
          document.setFractionalWidths(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Magnification:
          document.setMagnification(property.getDataAsInt16() / 10f);
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          document.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_CaptionStyleFont:
          document.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          document.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_CaptionStyleSize:
          document.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          document.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_CaptionStyleFace:
          document.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_LabelStyleColor:
          document.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_CaptionStyleColor:
          document.getSettings().setCaptionColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_LabelJustification:
          document
              .getSettings()
              .setLabelJustification(CDXUtils.readTextJustificationProperty(property));
          break;
        case CDXConstants.CDXProp_FixInplaceExtent:
          document.setFixInPlaceExtent(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_FixInplaceGap:
          document.setFixInPlaceGap(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_CartridgeData:
          document.setCartridgeData(property.getData());
          break;
        case CDXConstants.CDXProp_Window_IsZoomed:
          document.setWindowIsZoomed(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Window_Position:
          document.setWindowPosition(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_Window_Size:
          document.setWindowSize(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_ShowTerminalCarbonLabels:
          document.getSettings().setShowTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ShowNonTerminalCarbonLabels:
          document.getSettings().setShowNonTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_HideImplicitHydrogens:
          document.getSettings().setHideImplicitHydrogens(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_ShowEnhancedStereo:
          document.getSettings().setShowAtomEnhancedStereo(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDPage createPageObject(CDXObject root) throws IOException {
    handleCreation("page", root);
    CDPage page = new CDPage();
    handleReference(root, page);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Group:
          page.addGroup(createGroupObject(object));
          break;
        case CDXConstants.CDXObj_Fragment:
          page.addFragment(createFragmentObject(object));
          break;
        case CDXConstants.CDXObj_Text:
          page.addText(createTextObject(object));
          break;
        case CDXConstants.CDXObj_Graphic:
          page.addGraphic(createGraphicObject(object));
          break;
        case CDXConstants.CDXObj_BracketedGroup:
          page.addBracketedGroup(createBracketedGroupObject(object));
          break;
        case CDXConstants.CDXObj_Curve:
          page.addCurve(createSplineObject(object));
          break;
        case CDXConstants.CDXObj_EmbeddedObject:
          page.addEmbeddedObject(createEmbeddedObjectObject(object));
          break;
        case CDXConstants.CDXObj_Table:
          page.addTable(createTableObject(object));
          break;
        case CDXConstants.CDXObj_NamedAlternativeGroup:
          page.addNamedAlternativeGroup(createNamedAlternativeGroupObject(object));
          break;
        case CDXConstants.CDXObj_ReactionScheme:
          page.addReactionScheme(createReactionSchemeObject(object));
          break;
        case CDXConstants.CDXObj_ReactionStep:
          page.addReactionStep(createReactionStepObject(object));
          break;
        case CDXConstants.CDXObj_Spectrum:
          page.addSpectrum(createSpectrumObject(object));
          break;
        case CDXConstants.CDXObj_Sequence:
          page.addSequence(createSequenceObject(object));
          break;
        case CDXConstants.CDXObj_CrossReference:
          page.addCrossReference(createCrossReferenceObject(object));
          break;
        case CDXConstants.CDXObj_Border:
          page.addBorder(createBorderObject(object));
          break;
        case CDXConstants.CDXObj_Geometry:
          page.addGeometry(createGeometryObject(object));
          break;
        case CDXConstants.CDXObj_Constraint:
          page.addConstraint(createConstraintObject(object));
          break;
        case CDXConstants.CDXObj_TLCPlate:
          page.addTLCPlate(createTLCPlateObject(object));
          break;
        case CDXConstants.CDXObj_Splitter:
          page.addSplitter(createSplitterObject(object));
          break;
        case CDXConstants.CDXObj_ChemicalProperty:
          page.addChemicalProperty(createChemicalPropertyObject(object));
          break;

        case CDXConstants.CDXObj_Arrow:
          page.addArrow(createArrowObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return page;
  }

  private void populatePageObject(CDXObject root) throws IOException {
    handlePopulation("page", root);
    CDPage page = (CDPage) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_BoundingBox:
          page.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          page.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_WidthPages:
          page.setWidthPages(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_HeightPages:
          page.setHeightPages(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_DrawingSpaceType:
          page.setDrawingSpaceType(CDXUtils.readDrawingSpaceTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Width:
          page.setWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Height:
          page.setHeight(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_PageOverlap:
          page.setPageOverlap(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Header:
          page.setHeader(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_HeaderPosition:
          page.setHeaderPosition(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Footer:
          page.setFooter(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_FooterPosition:
          page.setFooterPosition(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_PrintTrimMarks:
          page.setPrintTrimMarks(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_SplitterPositions:
          break;
        case CDXConstants.CDXProp_PageDefinition:
          page.setPageDefinition(CDXUtils.readPageDefinitionProperty(property));
          break;
        case CDXConstants.CDXProp_BoundsInParent:
          page.setBoundsInParent(property.getDataAsRectangle());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDFragment createFragmentObject(CDXObject root) throws IOException {
    handleCreation("fragment", root);
    CDFragment fragment = new CDFragment();
    handleReference(root, fragment);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Node:
          fragment.addAtom(createNodeObject(object));
          break;
        case CDXConstants.CDXObj_Bond:
          fragment.addBond(createBondObject(object));
          break;
        case CDXConstants.CDXObj_Graphic:
          fragment.addGraphic(createGraphicObject(object));
          break;
        case CDXConstants.CDXObj_Curve:
          fragment.addCurve(createSplineObject(object));
          break;
        case CDXConstants.CDXObj_ObjectTag:
          fragment.addObjectTag(createObjectTagObject(object));
          break;

        case CDXConstants.CDXObj_Text:
          fragment.addText(createTextObject(object));
          break;
        case CDXConstants.CDXObj_Arrow:
          fragment.addArrow(createArrowObject(object));
          break;
        case CDXConstants.CDXObj_ColoredMolecularArea:
          fragment.addColoredMolecularArea(createColoredMolecularArea(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return fragment;
  }

  private void populateFragmentObject(CDXObject root) throws IOException {
    handlePopulation("fragment", root);
    CDFragment fragment = (CDFragment) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      switch (property.getTag()) {
        case CDXConstants.CDXProp_BoundingBox:
          fragment.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_Mole_Racemic:
          fragment.setRacemic(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Mole_Absolute:
          fragment.setAbsolute(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Mole_Relative:
          fragment.setRelative(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Mole_Formula:
          fragment.setFormula(property.getData());
          break;
        case CDXConstants.CDXProp_Mole_Weight:
          fragment.setWeight(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_Frag_ConnectionOrder:
          fragment.setConnectionOrder(property.getDataAsObjectRefArray(CDAtom.class, refManager));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDAtom createNodeObject(CDXObject root) throws IOException {
    handleCreation("node", root);
    CDAtom node = new CDAtom();
    handleReference(root, node);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Fragment:
          node.addFragment(createFragmentObject(object));
          break;
        case CDXConstants.CDXObj_Text:
          if (node.getText() != null) {
            throw new IOException("Unexpected object");
          }
          node.setText(createTextObject(object));
          break;
        case CDXConstants.CDXObj_ObjectTag:
          node.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return node;
  }

  private void populateNodeObject(CDXObject root) throws IOException {
    handlePopulation("node", root);
    CDAtom node = (CDAtom) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          node.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          node.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          node.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          node.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_2DPosition:
          node.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_3DPosition:
          node.setPosition3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          node.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          node.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_HighlightColor:
          node.getSettings().setHighlightColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_Node_Type:
          node.setNodeType(CDXUtils.readNodeTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Node_LabelDisplay:
          node.setLabelDisplay(CDXUtils.readLabelDisplayProperty(property));
          break;
        case CDXConstants.CDXProp_Node_Element:
          node.setElementNumber(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Atom_ElementList:
          node.setElementList(property.getDataAsElementList());
          break;
        case CDXConstants.CDXProp_Atom_Formula:
          node.setFormula(property.getData());
          break;
        case CDXConstants.CDXProp_Atom_Isotope:
          node.setIsotope(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Atom_Charge:
          node.setCharge(property.getDataAsInt());
          break;
        case CDXConstants.CDXProp_Atom_Radical:
          node.setRadical(CDXUtils.readRadicalProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_RestrictFreeSites:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.FreeSites);
          break;
        case CDXConstants.CDXProp_Atom_RestrictImplicitHydrogens:
          node.setImplicitHydrogensAllowed(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_RestrictRingBondCount:
          node.setRingBondCount(CDXUtils.readRingBondCountProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_RestrictUnsaturatedBonds:
          node.setUnsaturatedBonds(CDXUtils.readUnsaturationProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_RestrictRxnChange:
          node.setRestrictReactionChange(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_RestrictRxnStereo:
          node.setReactionStereo(CDXUtils.readReactionStereoProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_AbnormalValence:
          node.setAbnormalValenceAllowed(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_NumHydrogens:
          node.setNumImplicitHydrogens(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Atom_HDot:
          node.setHDot(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_HDash:
          node.setHDash(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_Geometry:
          node.setAtomGeometry(CDXUtils.readAtomGeometryProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_BondOrdering:
          node.setBondOrdering(property.getDataAsObjectRefArray(CDBond.class, refManager));
          break;
        case CDXConstants.CDXProp_Node_Attachments:
          node.setAttachedAtoms(
              property.getDataAsObjectRefArrayWithCounts(CDAtom.class, refManager));
          break;
        case CDXConstants.CDXProp_Atom_GenericNickname:
          node.setLabelText(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Atom_AltGroupID:
          node.setAltGroup(property.getDataAsObjectRef(CDAltGroup.class, refManager));
          break;
        case CDXConstants.CDXProp_Atom_RestrictSubstituentsUpTo:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.SubstituentsUpTo);
          break;
        case CDXConstants.CDXProp_Atom_RestrictSubstituentsExactly:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.SubstituentsExactly);
          break;
        case CDXConstants.CDXProp_Atom_CIPStereochemistry:
          node.setStereochemistry(CDXUtils.readAtomCIPTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_Translation:
          node.setTranslation(CDXUtils.readTranslationProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_AtomNumber:
          node.setAtomNumber(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Atom_ShowQuery:
          node.getSettings().setShowAtomQuery(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_ShowStereo:
          node.getSettings().setShowAtomStereo(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_ShowAtomNumber:
          node.getSettings().setShowAtomNumber(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Atom_LinkCountLow:
          node.setLinkCountLow(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Atom_LinkCountHigh:
          node.setLinkCountHigh(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Atom_IsotopicAbundance:
          node.setIsotopicAbundance(CDXUtils.readAbundanceProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_ExternalConnectionType:
          node.setAttachmentPointType(CDXUtils.readExternalConnectionTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Atom_GenericList:
          node.setGenericList(property.getDataAsGenericList(fonts, colors));
          break;
        case CDXConstants.CDXProp_LineWidth:
          node.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          node.getSettings().setLabelFont(fontStyle.getFont());
          node.getSettings().setLabelSize(fontStyle.getSize());
          node.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          node.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          node.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          node.getSettings().setLabelFace(property.getDataAsFontFace());
          break;

        case CDXConstants.CDXProp_MarginWidth:
          node.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;

        case CDXConstants.CDXProp_ShowTerminalCarbonLabels:
          node.getSettings().setShowTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ShowNonTerminalCarbonLabels:
          node.getSettings().setShowNonTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_HideImplicitHydrogens:
          node.getSettings().setHideImplicitHydrogens(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);

    if (node.getNodeType() == CDNodeType.GenericNickname
        && node.getLabelText() == null
        && node.getText() != null) {
      node.setLabelText(node.getText().getText().getText());
    }
  }

  private CDBond createBondObject(CDXObject root) throws IOException {
    handleCreation("bond", root);
    CDBond bond = new CDBond();
    handleReference(root, bond);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          bond.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return bond;
  }

  private void populateBondObject(CDXObject root) throws IOException {
    handlePopulation("bond", root);
    CDBond bond = (CDBond) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          bond.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          bond.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          bond.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          bond.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          bond.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          bond.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_HighlightColor:
          bond.getSettings().setHighlightColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_Bond_Order:
          bond.setBondOrder(CDXUtils.readBondOrdersProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_Display:
          bond.setBondDisplay(CDXUtils.readBondDisplayProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_Display2:
          bond.setBondDisplay2(CDXUtils.readBondDisplayProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_DoublePosition:
          bond.setBondDoublePosition(CDXUtils.readBondDoublePositionProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_Begin:
          bond.setBegin(property.getDataAsObjectRef(CDAtom.class, refManager));
          break;
        case CDXConstants.CDXProp_Bond_End:
          bond.setEnd(property.getDataAsObjectRef(CDAtom.class, refManager));
          break;
        case CDXConstants.CDXProp_Bond_RestrictTopology:
          bond.setTopology(CDXUtils.readBondTopologyProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_RestrictRxnParticipation:
          bond.setReactionParticipation(CDXUtils.readBondReactionParticipationProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_BeginAttach:
          bond.setBeginAttach(property.getDataAsUInt8());
          break;
        case CDXConstants.CDXProp_Bond_EndAttach:
          bond.setEndAttach(property.getDataAsUInt8());
          break;
        case CDXConstants.CDXProp_Bond_CIPStereochemistry:
          bond.setStereochemistry(CDXUtils.readBondCIPTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Bond_BondOrdering:
          bond.setBondCircularOrdering(property.getDataAsObjectRefArray(CDBond.class, refManager));
          break;
        case CDXConstants.CDXProp_Bond_ShowQuery:
          bond.getSettings().setShowBondQuery(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Bond_ShowStereo:
          bond.getSettings().setShowBondStereo(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Bond_CrossingBonds:
          bond.setCrossingBonds(
              new HashSet<CDBond>(property.getDataAsObjectRefArray(CDBond.class, refManager)));
          break;
        case CDXConstants.CDXProp_Bond_ShowRxn:
          bond.getSettings().setShowBondReaction(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BondSpacing:
          bond.getSettings().setBondSpacing(property.getDataAsInt16() / 10f);
          break;
        case CDXConstants.CDXProp_BondLength:
          bond.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_BoldWidth:
          bond.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          bond.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_MarginWidth:
          bond.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          bond.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          bond.getSettings().setLabelFont(fontStyle.getFont());
          bond.getSettings().setLabelSize(fontStyle.getSize());
          bond.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          bond.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          bond.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          bond.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_BondSpacingAbs:
          bond.getSettings().setBondSpacingAbs(property.getDataAsCoordinate());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDTemplateGrid createTemplateGridObject(CDXObject root) throws IOException {
    handleCreation("template grid", root);
    CDTemplateGrid templateGrid = new CDTemplateGrid();
    handleReference(root, templateGrid);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return templateGrid;
  }

  private void populateTemplateGridObject(CDXObject root) throws IOException {
    handlePopulation("template grid", root);
    CDTemplateGrid templateGrid = (CDTemplateGrid) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_2DExtent:
          templateGrid.setExtent(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_Template_PaneHeight:
          templateGrid.setPaneHeight(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Template_NumRows:
          templateGrid.setNumRows(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Template_NumColumns:
          templateGrid.setNumColumns(property.getDataAsInt16());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDGroup createGroupObject(CDXObject root) throws IOException {
    handleCreation("group", root);
    CDGroup group = new CDGroup();
    handleReference(root, group);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Group:
          group.addGroup(createGroupObject(object));
          break;
        case CDXConstants.CDXObj_Fragment:
          group.addFragment(createFragmentObject(object));
          break;
        case CDXConstants.CDXObj_Text:
          group.addCaption(createTextObject(object));
          break;
        case CDXConstants.CDXObj_Graphic:
          group.addGraphic(createGraphicObject(object));
          break;
        case CDXConstants.CDXObj_Curve:
          group.addCurve(createSplineObject(object));
          break;
        case CDXConstants.CDXObj_NamedAlternativeGroup:
          group.addNamedAlternativeGroup(createNamedAlternativeGroupObject(object));
          break;
        case CDXConstants.CDXObj_ReactionStep:
          group.addReactionStep(createReactionStepObject(object));
          break;
        case CDXConstants.CDXObj_Spectrum:
          group.addSpectrum(createSpectrumObject(object));
          break;
        case CDXConstants.CDXObj_EmbeddedObject:
          group.addEmbeddedObject(createEmbeddedObjectObject(object));
          break;
        case CDXConstants.CDXObj_ObjectTag:
          group.addObjectTag(createObjectTagObject(object));
          break;

        case CDXConstants.CDXObj_Arrow:
          group.addArrow(createArrowObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return group;
  }

  private void populateGroupObject(CDXObject root) throws IOException {
    handlePopulation("group", root);
    CDGroup group = (CDGroup) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_BoundingBox:
          group.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_Group_Integral:
          group.setIntegral(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDText createTextObject(CDXObject root) throws IOException {
    handleCreation("text", root);
    CDText text = new CDText();
    handleReference(root, text);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          text.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return text;
  }

  private void populateTextObject(CDXObject root) throws IOException {
    handlePopulation("text", root);
    CDText text = (CDText) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          text.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          text.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          text.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          text.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_2DPosition:
          text.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          text.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_RotationAngle:
          text.setAngle(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Text:
          text.setText(property.getDataAsStyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Justification:
          text.setJustification(CDXUtils.readTextJustificationProperty(property));
          text.getSettings()
              .setLabelJustification(CDXUtils.readTextJustificationProperty(property));
          break;
        case CDXConstants.CDXProp_LineHeight:
          text.setLineHeight(CDXUtils.readLineHeight(property));
          break;
        case CDXConstants.CDXProp_WordWrapWidth:
          text.setWrapWidth(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LineStarts:
          text.setLineStarts(property.getDataAsInt16ListWithCounts());
          break;
        case CDXConstants.CDXProp_LabelAlignment:
          text.setLabelAlignment(CDXUtils.readLabelDisplayProperty(property));
          break;
        case CDXConstants.CDXProp_LabelLineHeight:
          text.getSettings().setLabelLineHeight(CDXUtils.readLineHeight(property));
          break;
        case CDXConstants.CDXProp_CaptionLineHeight:
          text.getSettings().setCaptionLineHeight(CDXUtils.readLineHeight(property));
          break;
        case CDXConstants.CDXProp_InterpretChemically:
          text.getSettings().setInterpretChemically(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          text.getSettings().setLabelFont(fontStyle.getFont());
          text.getSettings().setLabelSize(fontStyle.getSize());
          text.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionStyle:
          fontStyle = property.getDataAsFontStyle(fonts, colors);
          text.getSettings().setCaptionFont(fontStyle.getFont());
          text.getSettings().setCaptionSize(fontStyle.getSize());
          text.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionJustification:
          text.getSettings()
              .setCaptionJustification(CDXUtils.readTextJustificationProperty(property));
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          text.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_CaptionStyleFont:
          text.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          text.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_CaptionStyleSize:
          text.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          text.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_CaptionStyleFace:
          text.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_LabelStyleColor:
          text.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_CaptionStyleColor:
          text.getSettings().setCaptionColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_LabelJustification:
          text.setJustification(CDXUtils.readTextJustificationProperty(property));
          text.getSettings()
              .setLabelJustification(CDXUtils.readTextJustificationProperty(property));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          text.setColor(property.getDataAsColorRef(colors));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDGraphic createGraphicObject(CDXObject root) throws IOException {
    handleCreation("graphic", root);
    CDGraphic graphic = new CDGraphic();
    handleReference(root, graphic);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          graphic.addObjectTag(createObjectTagObject(object));
          break;
        default:
          handleMissingTag(object);
      }
    }
    return graphic;
  }

  private void populateGraphicObject(CDXObject root) throws IOException {
    handlePopulation("graphic", root);
    CDGraphic graphic = (CDGraphic) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_RepresentsProperty:
          graphic.setRepresents(property.getDataAsRepresentsProperties(refManager));
          break;
        case CDXConstants.CDXProp_ZOrder:
          graphic.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          graphic.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          graphic.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          graphic.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_SupersededBy:
          graphic.setSupersededBy(property.getDataAsObjectRef(CDObject.class, refManager));
          break;
        case CDXConstants.CDXProp_BoundingBox:
          graphic.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_3DHead:
          graphic.setHead3D(property.getDataAsPoint3D(false));
          break;
        case CDXConstants.CDXProp_3DTail:
          graphic.setTail3D(property.getDataAsPoint3D(false));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          graphic.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          graphic.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BoldWidth:
          graphic.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          graphic.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          graphic.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_CaptionStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          graphic.getSettings().setCaptionFont(fontStyle.getFont());
          graphic.getSettings().setCaptionSize(fontStyle.getSize());
          graphic.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionStyleFont:
          graphic.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_CaptionStyleSize:
          graphic.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_CaptionStyleFace:
          graphic.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_Graphic_Type:
          graphic.setGraphicType(CDXUtils.readGraphicTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Line_Type:
          graphic.setLineType(CDXUtils.readLineTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Arrow_Type:
          graphic.setArrowType(CDXUtils.readArrowTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Rectangle_Type:
          graphic.setRectangleType(CDXUtils.readRectangleTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Oval_Type:
          graphic.setOvalType(CDXUtils.readOvalTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Orbital_Type:
          graphic.setOrbitalType(CDXUtils.readOrbitalTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Bracket_Type:
          graphic.setBracketType(CDXUtils.readBracketTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Symbol_Type:
          graphic.setSymbolType(CDXUtils.readSymbolTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Arrow_HeadSize:
          graphic.setArrowHeadSize(property.getDataAsInt16() / 100f);
          break;
        case CDXConstants.CDXProp_Arc_AngularSize:
          graphic.setArcAngularSize(property.getDataAsInt16() / 10f);
          break;
        case CDXConstants.CDXProp_Bracket_LipSize:
          graphic.setBracketLipSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Bracket_Usage:
          graphic.setBracketUsage(CDXUtils.readBracketUsageProperty(property));
          break;
        case CDXConstants.CDXProp_Polymer_RepeatPattern:
          graphic.setPolymerRepeatPattern(CDXUtils.readPolymerRepeatPatternProperty(property));
          break;
        case CDXConstants.CDXProp_Polymer_FlipType:
          graphic.setPolymerFlipType(CDXUtils.readPolymerFlipTypeProperty(property));
          break;

        case CDXConstants.CDXProp_Curve_FillType:
          graphic.setFillType(CDXUtils.readFillTypeProperty(property));
          break;
        case CDXConstants.CDXProp_ShadowSize:
          graphic.setShadowSize(property.getDataAsUInt16());
          break;
        case CDXConstants.CDXProp_CornerRadius:
          graphic.setCornerRadius(property.getDataAsUInt16());
          break;
        case CDXConstants.CDXProp_3DCenter:
          graphic.setCenter3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_MajorAxisEnd3D:
          graphic.setMajorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_MinorAxisEnd3D:
          graphic.setMinorAxisEnd3D(property.getDataAsPoint3D(true));
          break;

        case CDXConstants.CDXProp_FadePercent:
          graphic.setFadePercent(property.getDataAsUInt16());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDColoredMolecularArea createColoredMolecularArea(CDXObject root) throws IOException {
    handleCreation("colored molecular area", root);
    CDColoredMolecularArea area = new CDColoredMolecularArea();
    handleReference(root, area);
    return area;
  }

  private void populateColoredMolecularArea(CDXObject root) throws IOException {
    handlePopulation("colored molecular area", root);
    CDColoredMolecularArea area = (CDColoredMolecularArea) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_BackgroundColor:
          area.setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BasisObjects:
          area.setBasisObjects(property.getDataAsObjectRefArray(CDBond.class, refManager));
          break;
        default:
          handleMissingTag(property);
      }
    }
    populateChildren(root);
  }

  private CDArrow createArrowObject(CDXObject root) throws IOException {
    handleCreation("graphic", root);
    CDArrow arrow = new CDArrow();
    handleReference(root, arrow);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          arrow.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return arrow;
  }

  private void populateArrowObject(CDXObject root) throws IOException {
    handlePopulation("arrow", root);
    CDArrow arrow = (CDArrow) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          arrow.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          arrow.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          arrow.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          arrow.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          arrow.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_3DHead:
          arrow.setHead3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_3DTail:
          arrow.setTail3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          arrow.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          arrow.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BoldWidth:
          arrow.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          arrow.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          arrow.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_CaptionStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          arrow.getSettings().setCaptionFont(fontStyle.getFont());
          arrow.getSettings().setCaptionSize(fontStyle.getSize());
          arrow.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_CaptionStyleFont:
          arrow.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_CaptionStyleSize:
          arrow.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_CaptionStyleFace:
          arrow.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_Line_Type:
          arrow.setLineType(CDXUtils.readLineTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Arrow_HeadSize:
          arrow.setHeadSize(property.getDataAsInt16() / 100f);
          break;
        case CDXConstants.CDXProp_Arc_AngularSize:
          arrow.setAngularSize(property.getDataAsInt16() / 10f);
          break;

        case CDXConstants.CDXProp_Curve_ArrowheadType:
          arrow.setArrowHeadType(CDXUtils.readArrowheadTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadHead:
          arrow.setArrowHeadPositionStart(CDXUtils.readArrowheadProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadTail:
          arrow.setArrowHeadPositionTail(CDXUtils.readArrowheadProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadCenterSize:
          arrow.setHeadCenterSize(property.getDataAsUInt16() / 100f);
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadWidth:
          arrow.setHeadWidth(property.getDataAsUInt16() / 100f);
          break;
        case CDXConstants.CDXProp_3DCenter:
          arrow.setCenter3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_MajorAxisEnd3D:
          arrow.setMajorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_MinorAxisEnd3D:
          arrow.setMinorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXConstants.CDXProp_Arrow_NoGo:
          arrow.setNoGoType(CDXUtils.readNoGoProperty(property));
          break;
        case CDXConstants.CDXProp_Arrow_ShaftSpacing:
          arrow.setShaftSpacing(property.getDataAsUInt16() / 100f);
          break;
        case CDXConstants.CDXProp_Curve_FillType:
          arrow.setFillType(CDXUtils.readFillTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Arrow_Dipole:
          arrow.setDipole(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Arrow_EquilibriumRatio:
          arrow.setEquilibriumRatio(property.getDataAsUInt16() / 100f);
          break;

        case 0x303:
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDBracket createBracketedGroupObject(CDXObject root) throws IOException {
    handleCreation("bracketed group", root);
    CDBracket bracketedGroup = new CDBracket();
    handleReference(root, bracketedGroup);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_BracketedGroup:
          bracketedGroup.addBracket(createBracketedGroupObject(object));
          break;
        case CDXConstants.CDXObj_BracketAttachment:
          bracketedGroup.addBracketAttachment(createBracketAttachmentObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return bracketedGroup;
  }

  private void populateBracketedGroupObject(CDXObject root) throws IOException {
    handlePopulation("bracketed group", root);
    CDBracket bracketedGroup = (CDBracket) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Bracket_Usage:
          bracketedGroup.setBracketUsage(CDXUtils.readBracketUsageProperty(property));
          break;
        case CDXConstants.CDXProp_Polymer_RepeatPattern:
          bracketedGroup.setPolymerRepeatPattern(
              CDXUtils.readPolymerRepeatPatternProperty(property));
          break;
        case CDXConstants.CDXProp_Polymer_FlipType:
          bracketedGroup.setPolymerFlipType(CDXUtils.readPolymerFlipTypeProperty(property));
          break;
        case CDXConstants.CDXProp_BracketedObjects:
          bracketedGroup.setBracketedObjects(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_Bracket_RepeatCount:
          bracketedGroup.setRepeatCount(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_Bracket_ComponentOrder:
          bracketedGroup.setComponentOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Bracket_SRULabel:
          bracketedGroup.setSRULabel(property.getDataAsUnstyledString(fonts, colors));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDBracketAttachment createBracketAttachmentObject(CDXObject root) throws IOException {
    handleCreation("bracket attachment", root);
    CDBracketAttachment bracketAttachment = new CDBracketAttachment();
    handleReference(root, bracketAttachment);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_CrossingBond:
          bracketAttachment.addCrossingBond(createCrossingBondObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return bracketAttachment;
  }

  private void populateBracketAttachmentObject(CDXObject root) throws IOException {
    handlePopulation("document", root);
    CDBracketAttachment bracketAttachment = (CDBracketAttachment) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Bracket_GraphicID:
          bracketAttachment.setGraphic(property.getDataAsObjectRef(CDGraphic.class, refManager));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDCrossingBond createCrossingBondObject(CDXObject root) throws IOException {
    handleCreation("crossing bond", root);
    CDCrossingBond crossingBond = new CDCrossingBond();
    handleReference(root, crossingBond);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return crossingBond;
  }

  private void populateCrossingBondObject(CDXObject root) throws IOException {
    handlePopulation("crossing bond", root);
    CDCrossingBond crossingBond = (CDCrossingBond) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Bracket_BondID:
          crossingBond.setBond(property.getDataAsObjectRef(CDBond.class, refManager));
          break;
        case CDXConstants.CDXProp_Bracket_InnerAtomID:
          crossingBond.setInnerAtom(property.getDataAsObjectRef(CDAtom.class, refManager));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDSplitter createSplitterObject(CDXObject root) throws IOException {
    handleCreation("splitter", root);
    CDSplitter splitter = new CDSplitter();
    handleReference(root, splitter);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return splitter;
  }

  private void populateSplitterObject(CDXObject root) throws IOException {
    handlePopulation("splitter", root);
    CDSplitter splitter = (CDSplitter) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_2DPosition:
          splitter.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_PageDefinition:
          splitter.setPageDefinition(CDXUtils.readPageDefinitionProperty(property));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDTLCPlate createTLCPlateObject(CDXObject root) throws IOException {
    handleCreation("tlc plate", root);
    CDTLCPlate plate = new CDTLCPlate();
    handleReference(root, plate);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          plate.addObjectTag(createObjectTagObject(object));
          break;
        case CDXConstants.CDXObj_TLCLane:
          plate.addLane(createTLCLaneObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return plate;
  }

  private void populateTLCPlateObject(CDXObject root) throws IOException {
    handlePopulation("tlc plate", root);
    CDTLCPlate plate = (CDTLCPlate) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          plate.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Visible:
          plate.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          plate.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_TopLeft:
          plate.setTopLeft(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_TopRight:
          plate.setTopRight(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_BottomRight:
          plate.setBottomRight(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_BottomLeft:
          plate.setBottomLeft(property.getDataAsPoint2D());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          plate.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          plate.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BoldWidth:
          plate.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          plate.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_MarginWidth:
          plate.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          plate.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          plate.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          plate.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_TLC_OriginFraction:
          plate.setOriginFraction(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_TLC_SolventFrontFraction:
          plate.setSolventFrontFraction(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_TLC_ShowOrigin:
          plate.setShowOrigin(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_TLC_ShowSolventFront:
          plate.setShowSolventFront(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_TLC_ShowBorders:
          plate.setShowBorders(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDTLCLane createTLCLaneObject(CDXObject root) throws IOException {
    handleCreation("tlc lane", root);
    CDTLCLane lane = new CDTLCLane();
    handleReference(root, lane);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          lane.addObjectTag(createObjectTagObject(object));
          break;
        case CDXConstants.CDXObj_TLCSpot:
          lane.addSpot(createTLCSpotObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return lane;
  }

  private void populateTLCLaneObject(CDXObject root) throws IOException {
    handlePopulation("tlc lane", root);
    CDTLCLane lane = (CDTLCLane) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Visible:
          lane.setVisible(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDTLCSpot createTLCSpotObject(CDXObject root) throws IOException {
    handleCreation("tlc spot", root);
    CDTLCSpot spot = new CDTLCSpot();
    handleReference(root, spot);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          spot.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return spot;
  }

  private void populateTLCSpotObject(CDXObject root) throws IOException {
    handlePopulation("tlc spot", root);
    CDTLCSpot spot = (CDTLCSpot) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Visible:
          spot.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Width:
          spot.setWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Height:
          spot.setHeight(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Curve_Type:
          spot.setCurveType(CDXUtils.readCurveTypeProperty(property));
          break;
        case CDXConstants.CDXProp_TLC_Rf:
          spot.setRf(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_TLC_Tail:
          spot.setTail(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_TLC_ShowRf:
          spot.setShowRf(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          spot.setColor(property.getDataAsColorRef(colors));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDConstraint createConstraintObject(CDXObject root) throws IOException {
    handleCreation("constraint", root);
    CDConstraint constraint = new CDConstraint();
    handleReference(root, constraint);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          constraint.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return constraint;
  }

  private void populateConstraintObject(CDXObject root) throws IOException {
    handlePopulation("constraint", root);
    CDConstraint constraint = (CDConstraint) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Name:
          constraint.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          constraint.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BondLength:
          constraint.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          constraint.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          constraint.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          constraint.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          constraint.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          constraint.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_LabelStyleColor:
          constraint.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BasisObjects:
          constraint.setBasisObjects(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ConstraintType:
          constraint.setConstraintType(CDXUtils.readConstraintTypeProperty(property));
          break;
        case CDXConstants.CDXProp_ConstraintMin:
          constraint.setMinRange(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_ConstraintMax:
          constraint.setMaxRange(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_IgnoreUnconnectedAtoms:
          constraint.setIgnoreUnconnectedAtoms(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_DihedralIsChiral:
          constraint.setDihedralIsChiral(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDGeometry createGeometryObject(CDXObject root) throws IOException {
    handleCreation("geometry", root);
    CDGeometry geometry = new CDGeometry();
    handleReference(root, geometry);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          geometry.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return geometry;
  }

  private void populateGeometryObject(CDXObject root) throws IOException {
    handlePopulation("geometry", root);
    CDGeometry geometry = (CDGeometry) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Name:
          geometry.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          geometry.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BondLength:
          geometry.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          geometry.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          geometry.getSettings().setLabelFont(property.getDataAsFontRef(fonts)); // deprecated
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          geometry.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          geometry.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_LabelStyleColor:
          geometry.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_GeometricFeature:
          geometry.setGeometricType(CDXUtils.readGeometricFeatureProperty(property));
          break;
        case CDXConstants.CDXProp_RelationValue:
          geometry.setRelationValue(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_BasisObjects:
          geometry.setBasisObjects(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_PointIsDirected:
          geometry.setPointIsDirected(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDBorder createBorderObject(CDXObject root) throws IOException {
    handleCreation("border", root);
    CDBorder border = new CDBorder();
    handleReference(root, border);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return border;
  }

  private void populateBorderObject(CDXObject root) throws IOException {
    handlePopulation("border", root);
    CDBorder border = (CDBorder) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ForegroundColor:
          border.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_LineWidth:
          border.setWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_Side:
          border.setSide(CDXUtils.readSideTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Line_Type:
          border.setLineType(CDXUtils.readLineTypeProperty(property));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDCrossReference createCrossReferenceObject(CDXObject root) throws IOException {
    handleCreation("cross reference", root);
    CDCrossReference crossReference = new CDCrossReference();
    handleReference(root, crossReference);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return crossReference;
  }

  private void populateCrossReferenceObject(CDXObject root) throws IOException {
    handlePopulation("document", root);
    CDCrossReference crossReference = (CDCrossReference) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_CrossReference_Container:
          crossReference.setContainer(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_CrossReference_Document:
          crossReference.setDocument(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_CrossReference_Identifier:
          crossReference.setIdentifier(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_CrossReference_Sequence:
          crossReference.setSequence(property.getDataAsUnstyledString(fonts, colors));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDSequence createSequenceObject(CDXObject root) throws IOException {
    handleCreation("sequence", root);
    CDSequence sequence = new CDSequence();
    handleReference(root, sequence);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return sequence;
  }

  private void populateSequenceObject(CDXObject root) throws IOException {
    handlePopulation("sequence", root);
    CDSequence sequence = (CDSequence) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Sequence_Identifier:
          sequence.setIdentifier(property.getDataAsUnstyledString(fonts, colors));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDSpectrum createSpectrumObject(CDXObject root) throws IOException {
    handleCreation("spectrum", root);
    CDSpectrum spectrum = new CDSpectrum();
    handleReference(root, spectrum);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          spectrum.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return spectrum;
  }

  private void populateSpectrumObject(CDXObject root) throws IOException {
    handlePopulation("spectrum", root);
    CDSpectrum spectrum = (CDSpectrum) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          spectrum.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          spectrum.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          spectrum.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          spectrum.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          spectrum.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          spectrum.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          spectrum.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BoldWidth:
          spectrum.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          spectrum.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          spectrum.getSettings().setLabelFont(fontStyle.getFont());
          spectrum.getSettings().setLabelSize(fontStyle.getSize());
          spectrum.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          spectrum.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          spectrum.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          spectrum.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXConstants.CDXProp_Spectrum_XSpacing:
          spectrum.setXSpacing(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_Spectrum_XLow:
          spectrum.setXLow(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_Spectrum_XType:
          spectrum.setXType(CDXUtils.readSpectrumXTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Spectrum_YType:
          spectrum.setYType(CDXUtils.readSpectrumYTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Spectrum_XAxisLabel:
          spectrum.setXAxisLabel(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Spectrum_YAxisLabel:
          spectrum.setYAxisLabel(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Spectrum_DataPoint:
          spectrum.setDataPoint(property.getDataAsFloat64Array());
          break;
        case CDXConstants.CDXProp_Spectrum_Class:
          spectrum.setSpectrumClass(CDXUtils.readSpectrumClassProperty(property));
          break;
        case CDXConstants.CDXProp_Spectrum_YLow:
          spectrum.setYLow(property.getDataAsFloat64());
          break;
        case CDXConstants.CDXProp_Spectrum_YScale:
          spectrum.setYLow(property.getDataAsFloat64());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDReactionStep createReactionStepObject(CDXObject root) throws IOException {
    handleCreation("reaction step", root);
    CDReactionStep reactionStep = new CDReactionStep();
    handleReference(root, reactionStep);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return reactionStep;
  }

  private void populateReactionStepObject(CDXObject root) throws IOException {
    handlePopulation("reaction step", root);
    CDReactionStep reactionStep = (CDReactionStep) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ReactionStep_Atom_Map:
          reactionStep.setAtomMap(
              property.getDataObjectRefMap(CDAtom.class, CDAtom.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Reactants:
          reactionStep.setReactants(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Products:
          reactionStep.setProducts(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Plusses:
          reactionStep.setPlusses(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Arrows:
          reactionStep.setArrows(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_ObjectsAboveArrow:
          reactionStep.setObjectsAboveArrow(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_ObjectsBelowArrow:
          reactionStep.setObjectsBelowArrow(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Atom_Map_Manual:
          reactionStep.setAtomMapManual(
              property.getDataObjectRefMap(CDAtom.class, CDAtom.class, refManager));
          break;
        case CDXConstants.CDXProp_ReactionStep_Atom_Map_Auto:
          reactionStep.setAtomMapAuto(
              property.getDataObjectRefMap(CDAtom.class, CDAtom.class, refManager));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDReactionScheme createReactionSchemeObject(CDXObject root) throws IOException {
    handleCreation("reaction scheme", root);
    CDReactionScheme reactionScheme = new CDReactionScheme();
    handleReference(root, reactionScheme);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ReactionStep:
          reactionScheme.addStep(createReactionStepObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return reactionScheme;
  }

  private void populateReactionSchemeObject(CDXObject root) throws IOException {
    handlePopulation("reaction scheme", root);

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDAltGroup createNamedAlternativeGroupObject(CDXObject root) throws IOException {
    handleCreation("names alternative group", root);
    CDAltGroup namedAlternativeGroup = new CDAltGroup();
    handleReference(root, namedAlternativeGroup);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Group:
          namedAlternativeGroup.addGroup(createGroupObject(object));
          break;
        case CDXConstants.CDXObj_Fragment:
          namedAlternativeGroup.addFragment(createFragmentObject(object));
          break;
        case CDXConstants.CDXObj_Text:
          namedAlternativeGroup.addCaption(createTextObject(object));
          break;
        case CDXConstants.CDXObj_ObjectTag:
          namedAlternativeGroup.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return namedAlternativeGroup;
  }

  private void populateNamedAlternativeGroupObject(CDXObject root) throws IOException {
    handlePopulation("named alternative group", root);
    CDAltGroup namedAlternativeGroup = (CDAltGroup) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          namedAlternativeGroup.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          namedAlternativeGroup.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          namedAlternativeGroup.setChemicalWarning(property.getDataAsString());
          break;
        case CDXConstants.CDXProp_Visible:
          namedAlternativeGroup.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          namedAlternativeGroup.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          namedAlternativeGroup.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          namedAlternativeGroup
              .getSettings()
              .setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_NamedAlternativeGroup_TextFrame:
          namedAlternativeGroup.setTextFrame(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_NamedAlternativeGroup_GroupFrame:
          namedAlternativeGroup.setGroupFrame(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_NamedAlternativeGroup_Valence:
          namedAlternativeGroup.setValence(property.getDataAsInt16());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDTable createTableObject(CDXObject root) throws IOException {
    handleCreation("table", root);
    CDTable table = new CDTable();
    handleReference(root, table);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Page:
          table.addPage(createPageObject(object));
          break;
        case CDXConstants.CDXObj_ObjectTag:
          table.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return table;
  }

  private void populateTableObject(CDXObject root) throws IOException {
    handlePopulation("table", root);
    CDTable table = (CDTable) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          table.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_Visible:
          table.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          table.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          table.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          table.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BoldWidth:
          table.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LineWidth:
          table.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_MarginWidth:
          table.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_LabelStyleFont:
          table.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXConstants.CDXProp_LabelStyleSize:
          table.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_LabelStyleFace:
          table.getSettings().setLabelFace(property.getDataAsFontFace());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDPicture createEmbeddedObjectObject(CDXObject root) throws IOException {
    handleCreation("embedded object", root);
    CDPicture picture = new CDPicture();
    handleReference(root, picture);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          picture.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return picture;
  }

  private void populateEmbeddedObjectObject(CDXObject root) throws IOException {
    handlePopulation("embedded object", root);
    CDPicture picture = (CDPicture) root.getInstance();

    byte[] compressedEnhancedMetafile = null;
    int uncompressedEnhancedMetafileSize = 0;
    byte[] compressedOLEObject = null;
    int uncompressedOLEObjectSize = 0;
    byte[] compressedWindowsMetafile = null;
    int uncompressedWindowsMetafileSize = 0;

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          picture.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          picture.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_RotationAngle:
          picture.setRotationAngle(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          picture.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          picture.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_Picture_Edition:
          picture.setPictureEdition(property.getData());
          break;
        case CDXConstants.CDXProp_Picture_EditionAlias:
          picture.setPictureEditionAlias(property.getData());
          break;
        case CDXConstants.CDXProp_MacPICT:
          picture.setMacPICT(property.getData());
          break;
        case CDXConstants.CDXProp_WindowsMetafile:
          picture.setWindowsMetafile(property.getData());
          break;
        case CDXConstants.CDXProp_OLEObject:
          picture.setOleObject(property.getData());
          break;
        case CDXConstants.CDXProp_EnhancedMetafile:
          picture.setEnhancedMetafile(property.getData());
          break;

        case CDXConstants.CDXProp_CompressedWindowsMetafile:
          compressedWindowsMetafile = property.getData();
          break;
        case CDXConstants.CDXProp_CompressedOLEObject:
          compressedOLEObject = property.getData();
          break;
        case CDXConstants.CDXProp_CompressedEnhancedMetafile:
          compressedEnhancedMetafile = property.getData();
          break;

        case CDXConstants.CDXProp_UncompressedWindowsMetafileSize:
          uncompressedWindowsMetafileSize = property.getDataAsInt();
          break;
        case CDXConstants.CDXProp_UncompressedOLEObjectSize:
          uncompressedOLEObjectSize = property.getDataAsInt();
          break;
        case CDXConstants.CDXProp_UncompressedEnhancedMetafileSize:
          uncompressedEnhancedMetafileSize = property.getDataAsInt();
          break;

        case CDXConstants.CDXProp_GIF:
          picture.setGif(property.getData());
          break;
        case CDXConstants.CDXProp_TIFF:
          picture.setTiff(property.getData());
          break;
        case CDXConstants.CDXProp_PNG:
          picture.setPng(property.getData());
          break;
        case CDXConstants.CDXProp_JPEG:
          picture.setJpeg(property.getData());
          break;
        case CDXConstants.CDXProp_BMP:
          picture.setBmp(property.getData());
          break;

        default:
          handleMissingTag(property);
      }
    }

    if (compressedEnhancedMetafile != null && uncompressedEnhancedMetafileSize > 0) {
      try {
        picture.setEnhancedMetafile(IOUtils.uncompress(compressedEnhancedMetafile));
      } catch (DataFormatException e) {
        LOGGER.error("Cannot uncompress data", e);
      }
    }

    if (compressedOLEObject != null && uncompressedOLEObjectSize > 0) {
      try {
        picture.setOleObject(IOUtils.uncompress(compressedOLEObject));
      } catch (DataFormatException e) {
        LOGGER.error("Cannot uncompress data", e);
      }
    }

    if (compressedWindowsMetafile != null && uncompressedWindowsMetafileSize > 0) {
      try {
        picture.setWindowsMetafile(IOUtils.uncompress(compressedWindowsMetafile));
      } catch (DataFormatException e) {
        LOGGER.error("Cannot uncompress data", e);
      }
    }

    // work-around to fix wrong EMFs, which are WMFs
    //    if (picture.getEnhancedMetafile() != null) {
    //      try {
    //        new EmfMetafile(new ByteArrayInputStream(picture.getEnhancedMetafile()));
    //      } catch (Exception e) {
    //        try {
    //          new WmfMetafile(new ByteArrayInputStream(picture.getEnhancedMetafile()));
    //          picture.setWindowsMetafile(picture.getEnhancedMetafile());
    //          picture.setEnhancedMetafile(null);
    //        } catch (Exception e2) {
    //          // do nothing
    //        }
    //      }
    //    }

    populateChildren(root);
  }

  private CDSpline createSplineObject(CDXObject root) throws IOException {
    handleCreation("curve", root);
    CDSpline spline = new CDSpline();
    handleReference(root, spline);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_ObjectTag:
          spline.addObjectTag(createObjectTagObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return spline;
  }

  private void populateSplineObject(CDXObject root) throws IOException {
    handlePopulation("curve", root);
    CDSpline spline = (CDSpline) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ZOrder:
          spline.setZOrder(property.getDataAsInt16());
          break;
        case CDXConstants.CDXProp_IgnoreWarnings:
          spline.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ChemicalWarning:
          spline.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXConstants.CDXProp_Visible:
          spline.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_BoundingBox:
          spline.setBounds(property.getDataAsRectangle());
          break;
        case CDXConstants.CDXProp_ForegroundColor:
          spline.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_BackgroundColor:
          spline.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXConstants.CDXProp_Curve_Type:
          CDSplineType curveType = CDXUtils.readCurveTypeProperty(property);
          spline.setFillType(curveType.getFillType());
          spline.setLineType(curveType.getLineType());
          spline.setClosed(curveType.isClosed());
          break;
        case CDXConstants.CDXProp_Curve_Points:
          spline.setPoints2D(property.getDataAsPoint2DArray());
          break;
        case CDXConstants.CDXProp_Curve_Points3D:
          spline.setPoints3D(property.getDataAsPoint3DArray());
          break;

        case CDXConstants.CDXProp_LineWidth:
          spline.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_HashSpacing:
          spline.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_BoldWidth:
          spline.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;

        case CDXConstants.CDXProp_Curve_ArrowheadType:
          spline.setArrowHeadType(CDXUtils.readArrowheadTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadHead:
          spline.setArrowHeadPositionAtStart(CDXUtils.readArrowheadProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_ArrowheadTail:
          spline.setArrowHeadPositionAtEnd(CDXUtils.readArrowheadProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_Closed:
          spline.setClosed(property.getDataAsBoolean());
          break;

        case CDXConstants.CDXProp_Line_Type:
          spline.setLineType(CDXUtils.readLineTypeProperty(property));
          break;
        case CDXConstants.CDXProp_Curve_FillType:
          spline.setFillType(CDXUtils.readFillTypeProperty(property));
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDObjectTag createObjectTagObject(CDXObject root) throws IOException {
    handleCreation("object tag", root);
    CDObjectTag objectTag = new CDObjectTag();
    handleReference(root, objectTag);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Text:
          objectTag.addText(createTextObject(object));
          break;

        default:
          handleMissingTag(object);
      }
    }
    return objectTag;
  }

  private void populateObjectTagObject(CDXObject root) throws IOException {
    handlePopulation("object tag", root);
    CDObjectTag objectTag = (CDObjectTag) root.getInstance();

    // read first type of property
    for (CDXProperty property : root.getProperties()) {
      switch (property.getTag()) {
        case CDXConstants.CDXProp_ObjectTag_Type:
          objectTag.setObjectTagType(CDXUtils.readObjectTagTypeProperty(property));
          break;
        default:
          break;
      }
    }

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_Visible:
          objectTag.setVisible(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_Name:
          objectTag.setName(property.getDataAsString());
          break;
        case CDXConstants.CDXProp_ObjectTag_Type:
          objectTag.setObjectTagType(CDXUtils.readObjectTagTypeProperty(property));
          break;
        case CDXConstants.CDXProp_ObjectTag_Tracking:
          objectTag.setTracking(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ObjectTag_Persistent:
          objectTag.setPersistent(property.getDataAsBoolean());
          break;
        case CDXConstants.CDXProp_ObjectTag_Value:
          switch (objectTag.getObjectTagType()) {
            case Long:
              objectTag.setValue(property.getDataAsInt32());
              break;
            case Double:
              objectTag.setValue(property.getDataAsFloat64());
              break;
            case String:
              objectTag.setValue(property.getDataAsUnstyledString(fonts, colors));
              break;
            case Undefined:
              objectTag.setValue(property.getData());
              break;
            default:
              throw new IOException();
          }
          break;
        case CDXConstants.CDXProp_Positioning:
          objectTag.setPositioningType(CDXUtils.readPositioningTypeProperty(property));
          break;
        case CDXConstants.CDXProp_PositioningAngle:
          objectTag.setPositioningAngle(property.getDataAsCoordinate());
          break;
        case CDXConstants.CDXProp_PositioningOffset:
          objectTag.setPositioningOffset(property.getDataAsPoint2D());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private CDChemicalProperty createChemicalPropertyObject(CDXObject root) throws IOException {
    handleCreation("chemical property", root);
    CDChemicalProperty chemicalProperty = new CDChemicalProperty();
    handleReference(root, chemicalProperty);

    // read content
    for (CDXObject object : root.getObjects()) {
      switch (object.getTag()) {
        default:
          handleMissingTag(object);
      }
    }
    return chemicalProperty;
  }

  private void populateChemicalPropertyObject(CDXObject root) throws IOException {
    handlePopulation("chemical property", root);
    CDChemicalProperty chemicalProperty = (CDChemicalProperty) root.getInstance();

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXConstants.CDXProp_BasisObjects:
          chemicalProperty.setBasisObjects(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ChemicalPropertyType:
          chemicalProperty.setType(property.getDataAsInt32());
          break;
        case CDXConstants.CDXProp_ChemicalPropertyDisplayID:
          chemicalProperty.setDisplay(property.getDataAsObjectRef(Object.class, refManager));
          break;
        case CDXConstants.CDXProp_ChemicalPropertyIsActive:
          chemicalProperty.setActive(property.getDataAsBoolean());
          break;

        default:
          handleMissingTag(property);
      }
    }

    populateChildren(root);
  }

  private void populateChildren(CDXObject root) throws IOException {
    for (CDXObject object : root.getObjects()) {
      if (object.getInstance() == null) {
        LOGGER.warn(
            "Omit object with tag 0x{} not recognized at {}",
            Integer.toHexString(object.getTag()),
            CDXUtils.getPositionAsString(object));
        continue;
      }
      switch (object.getTag()) {
        case CDXConstants.CDXObj_Document:
          populateDocumentObject(object);
          break;
        case CDXConstants.CDXObj_Page:
          populatePageObject(object);
          break;
        case CDXConstants.CDXObj_Group:
          populateGroupObject(object);
          break;
        case CDXConstants.CDXObj_Fragment:
          populateFragmentObject(object);
          break;
        case CDXConstants.CDXObj_Node:
          populateNodeObject(object);
          break;
        case CDXConstants.CDXObj_Bond:
          populateBondObject(object);
          break;
        case CDXConstants.CDXObj_Text:
          populateTextObject(object);
          break;
        case CDXConstants.CDXObj_Graphic:
          populateGraphicObject(object);
          break;
        case CDXConstants.CDXObj_Curve:
          populateSplineObject(object);
          break;
        case CDXConstants.CDXObj_EmbeddedObject:
          populateEmbeddedObjectObject(object);
          break;
        case CDXConstants.CDXObj_NamedAlternativeGroup:
          populateNamedAlternativeGroupObject(object);
          break;
        case CDXConstants.CDXObj_TemplateGrid:
          populateTemplateGridObject(object);
          break;
        case CDXConstants.CDXObj_ReactionScheme:
          populateReactionSchemeObject(object);
          break;
        case CDXConstants.CDXObj_ReactionStep:
          populateReactionStepObject(object);
          break;
        case CDXConstants.CDXObj_Spectrum:
          populateSpectrumObject(object);
          break;
        case CDXConstants.CDXObj_ObjectTag:
          populateObjectTagObject(object);
          break;
        case CDXConstants.CDXObj_Sequence:
          populateSequenceObject(object);
          break;
        case CDXConstants.CDXObj_CrossReference:
          populateCrossReferenceObject(object);
          break;
        case CDXConstants.CDXObj_Splitter:
          populateSplitterObject(object);
          break;
        case CDXConstants.CDXObj_Table:
          populateTableObject(object);
          break;
        case CDXConstants.CDXObj_BracketedGroup:
          populateBracketedGroupObject(object);
          break;
        case CDXConstants.CDXObj_BracketAttachment:
          populateBracketAttachmentObject(object);
          break;
        case CDXConstants.CDXObj_CrossingBond:
          populateCrossingBondObject(object);
          break;
        case CDXConstants.CDXObj_Border:
          populateBorderObject(object);
          break;
        case CDXConstants.CDXObj_Geometry:
          populateGeometryObject(object);
          break;
        case CDXConstants.CDXObj_Constraint:
          populateConstraintObject(object);
          break;
        case CDXConstants.CDXObj_TLCPlate:
          populateTLCPlateObject(object);
          break;
        case CDXConstants.CDXObj_TLCLane:
          populateTLCLaneObject(object);
          break;
        case CDXConstants.CDXObj_TLCSpot:
          populateTLCSpotObject(object);
          break;
        case CDXConstants.CDXObj_ChemicalProperty:
          populateChemicalPropertyObject(object);
          break;

        case CDXConstants.CDXObj_Arrow:
          populateArrowObject(object);
          break;

        case CDXConstants.CDXObj_ColoredMolecularArea:
          populateColoredMolecularArea(object);
          break;

        default:
          handleMissingTag(object);
      }
    }
  }

  private void handleReference(CDXObject root, Object reference) {
    root.setInstance(reference);
    refManager.putObjectRef(root.getId(), reference);
  }

  private void handleCreation(String name, CDXObject object) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "create {} object with id {}(0x{}) at {}",
          name,
          object.getId(),
          Integer.toHexString(object.getId()),
          CDXUtils.getPositionAsString(object));
    }
  }

  private void handlePopulation(String name, CDXObject object) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "populate {} object with id {}(0x{}) at {}",
          name,
          object.getId(),
          Integer.toHexString(object.getId()),
          CDXUtils.getPositionAsString(object));
    }
  }

  private void handleProperty(CDXProperty property) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "handle tag {} (0x{}) at {} with length {}(0x{})",
          property.getTag(),
          Integer.toHexString(property.getTag()),
          CDXUtils.getPositionAsString(property),
          property.getLength(),
          Integer.toHexString(property.getLength()));
    }
  }

  private void handleMissingTag(CDXObject object) throws IOException {
    String message =
        "Object with tag 0x"
            + Integer.toHexString(object.getTag())
            + " not recognized at "
            + CDXUtils.getPositionAsString(object);
    if (RIGID) {
      throw new IOException(message);
    }
    LOGGER.warn(message);
  }

  private void handleMissingTag(CDXProperty property) throws IOException {
    String message =
        "Property with tag 0x"
            + Integer.toHexString(property.getTag())
            + " and with length "
            + property.getLength()
            + " not recognized at "
            + CDXUtils.getPositionAsString(property);
    if (RIGID) {
      throw new IOException(message);
    }
    LOGGER.warn(message);
  }
}
