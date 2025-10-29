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

import static org.beilstein.chemxtract.cdx.reader.CDXMLConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.*;
import org.beilstein.chemxtract.io.XMLEntityCatalog;
import org.beilstein.chemxtract.io.XMLObject;
import org.beilstein.chemxtract.io.XMLUtils;

/** Reader for ChemDraw CDXML files. */
public class CDXMLReader {
  private static final Log logger = LogFactory.getLog(CDXMLReader.class);

  private RefManager refManager = new RefManager();
  private Map<Integer, CDColor> colors = new HashMap<>();
  private Map<Integer, CDFont> fonts = new HashMap<>();

  protected static final boolean RIGID = false;

  private CDXMLReader() {}

  /**
   * This method reads a {@link CDDocument} from a {@link InputStream}.
   *
   * @param in {@link InputStream} from which the input are read
   * @return ChemDraw document instance
   * @throws IOException Occurs if the reader couldn't read the input from the {@link InputStream}
   */
  public static CDDocument readDocument(InputStream in) throws IOException {

    XMLEntityCatalog catalog = new XMLEntityCatalog();
    catalog.addSystemId(DTD, "org/beilstein/chemxtract/cdx/reader/cdxml.dtd");
    catalog.addSystemId(DTD2, "org/beilstein/chemxtract/cdx/reader/cdxml.dtd");

    XMLObject root = XMLUtils.parse(in, catalog, false);

    CDXMLReader reader = new CDXMLReader();
    CDDocument document = reader.createDocumentObject(root);
    reader.populateDocumentObject(root);
    return document;
  }

  private CDDocument createDocumentObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDDocument document = new CDDocument();
    handleReference(root, document);

    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Page)) {
        document.getPages().add(createPageObject(object));
      } else if (name.equals(CDXMLObj_TemplateGrid)) {
        document.setTemplateGrid(createTemplateGridObject(object));
      } else if (name.equals(CDXMLObj_ColorTable)) {
        createColorTableObject(object);
        // Color 2 & 3 are the standard foreground and background color
        document.getSettings().setColor(colors.get(3));
        document.getSettings().setBackgroundColor(colors.get(2));
      } else if (name.equals(CDXMLObj_FontTable)) {
        createFontTableObject(object);
      } else {
        handleMissingObject(object);
      }
    }
    return document;
  }

  private void createColorTableObject(XMLObject root) throws IOException {
    int index = 0;
    colors.put(index++, CDColor.BLACK);
    colors.put(index++, CDColor.WHITE);
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Color)) {
        colors.put(index++, createColorObject(object));
      } else {
        handleMissingObject(object);
      }
    }
  }

  private CDColor createColorObject(XMLObject root) throws IOException {
    CDColor color = new CDColor();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Red)) {
        color.setRed(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Green)) {
        color.setGreen(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Blue)) {
        color.setBlue(root.getAttributeAsFloat(name));
      } else {
        handleMissingAttribute(root, name);
      }
    }
    return color;
  }

  private void createFontTableObject(XMLObject root) throws IOException {
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Font)) {
        createFontObject(object);
      } else {
        handleMissingObject(object);
      }
    }
  }

  private void createFontObject(XMLObject root) throws IOException {
    CDFont font = new CDFont();
    int id = -1;

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        id = root.getAttributeAsInt(name);
      } else if (name.equals(CDXMLProp_Font_Name)) {
        font.setName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CharSet)) {
        font.setCharSet(CDXMLUtils.convertStringToCharSet(root.getAttribute(name)));
      } else {
        handleMissingAttribute(root, name);
      }
    }
    if (id < 0) {
      throw new IOException();
    }
    fonts.put(id, font);
  }

  private void populateDocumentObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDDocument document = (CDDocument) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_CreationUserName)) {
        document.setCreationUserName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CreationDate)) {
        document.setCreationDate(CDXMLUtils.convertStringToDate(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_CreationProgram)) {
        document.setCreationProgram(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_ModificationUserName)) {
        document.setModificationUserName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_ModificationDate)) {
        document.setModificationDate(CDXMLUtils.convertStringToDate(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ModificationProgram)) {
        document.setModificationProgram(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Name)) {
        document.setName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Comment)) {
        document.setComment(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        document.setBoundingBox(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_ShowQuery)) {
        document.getSettings().setShowAtomQuery(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowStereo)) {
        document.getSettings().setShowAtomStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowEnhancedStereo)) {
        document.getSettings().setShowAtomEnhancedStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowAtomNumber)) {
        document.getSettings().setShowAtomNumber(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Bond_ShowQuery)) {
        document.getSettings().setShowBondQuery(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Bond_ShowStereo)) {
        document.getSettings().setShowBondStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Bond_ShowRxn)) {
        document.getSettings().setShowBondReaction(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_LabelLineHeight)) {
        document
            .getSettings()
            .setLabelLineHeight(CDXMLUtils.convertStringToLineHeight(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_CaptionLineHeight)) {
        document
            .getSettings()
            .setCaptionLineHeight(CDXMLUtils.convertStringToLineHeight(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_InterpretChemically)) {
        document.getSettings().setInterpretChemically(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_MacPrintInfo)) {
        document.setMacPrintInfo(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_WinPrintInfo)) {
        document.setWinPrintInfo(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_PrintMargins)) {
        document.setPrintMargins(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ChainAngle)) {
        document.getSettings().setChainAngle(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BondSpacing)) {
        document.getSettings().setBondSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BondSpacingAbs)) {
        document.getSettings().setBondSpacingAbs(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BondLength)) {
        document.getSettings().setBondLength(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        document.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        document.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_MarginWidth)) {
        document.getSettings().setMarginWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        document.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionJustification)) {
        document
            .getSettings()
            .setCaptionJustification(
                CDXMLUtils.convertStringToTextJustification(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_FractionalWidths)) {
        document.setFractionalWidths(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Magnification)) {
        document.setMagnification(root.getAttributeAsFloat(name) / 10f);
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        document.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleFont)) {
        document.getSettings().setCaptionFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        document.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleSize)) {
        document.getSettings().setCaptionSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        document
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleFace)) {
        document
            .getSettings()
            .setCaptionFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleColor)) {
        document.getSettings().setLabelColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_CaptionStyleColor)) {
        document.getSettings().setCaptionColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_LabelJustification)) {
        document
            .getSettings()
            .setLabelJustification(
                CDXMLUtils.convertStringToTextJustification(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_FixInplaceExtent)) {
        document.setFixInPlaceExtent(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_FixInplaceGap)) {
        document.setFixInPlaceGap(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_CartridgeData)) {
        document.setCartridgeData(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Window_IsZoomed)) {
        document.setWindowIsZoomed(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Window_Position)) {
        document.setWindowPosition(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Window_Size)) {
        document.setWindowSize(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ShowTerminalCarbonLabels)) {
        document.getSettings().setShowTerminalCarbonLabels(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ShowNonTerminalCarbonLabels)) {
        document.getSettings().setShowNonTerminalCarbonLabels(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_HideImplicitHydrogens)) {
        document.getSettings().setHideImplicitHydrogens(root.getAttributeAsBoolean(name));
      } else {
        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDPage createPageObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDPage page = new CDPage();
    handleReference(root, page);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Group)) {
        page.getGroups().add(createGroupObject(object));
      } else if (name.equals(CDXMLObj_Fragment)) {
        page.getFragments().add(createFragmentObject(object));
      } else if (name.equals(CDXMLObj_Text)) {
        page.getTexts().add(createTextObject(object));
      } else if (name.equals(CDXMLObj_Graphic)) {
        page.getGraphics().add(createGraphicObject(object));
      } else if (name.equals(CDXMLObj_Arrow)) {
        page.getArrows().add(createArrowObject(object));
      } else if (name.equals(CDXMLObj_BracketedGroup)) {
        page.getBracketedGroups().add(createBracketedGroupObject(object));
      } else if (name.equals(CDXMLObj_Curve)) {
        page.getCurves().add(createSplineObject(object));
      } else if (name.equals(CDXMLObj_EmbeddedObject)) {
        page.getEmbeddedObjects().add(createEmbeddedObjectObject(object));
      } else if (name.equals(CDXMLObj_Table)) {
        page.getTables().add(createTableObject(object));
      } else if (name.equals(CDXMLObj_NamedAlternativeGroup)) {
        page.getNamedAlternativeGroups().add(createNamedAlternativeGroupObject(object));
      } else if (name.equals(CDXMLObj_ReactionScheme)) {
        page.getReactionSchemes().add(createReactionSchemeObject(object));
      } else if (name.equals(CDXMLObj_ReactionStep)) {
        page.getReactionSteps().add(createReactionStepObject(object));
      } else if (name.equals(CDXMLObj_Spectrum)) {
        page.getSpectra().add(createSpectrumObject(object));
      } else if (name.equals(CDXMLObj_Sequence)) {
        page.getSequences().add(createSequenceObject(object));
      } else if (name.equals(CDXMLObj_CrossReference)) {
        page.getCrossReferences().add(createCrossReferenceObject(object));
      } else if (name.equals(CDXMLObj_Border)) {
        page.getBorders().add(createBorderObject(object));
      } else if (name.equals(CDXMLObj_Geometry)) {
        page.getGeometries().add(createGeometryObject(object));
      } else if (name.equals(CDXMLObj_Constraint)) {
        page.getConstraints().add(createConstraintObject(object));
      } else if (name.equals(CDXMLObj_TLCPlate)) {
        page.getTLCPlates().add(createTLCPlateObject(object));
      } else if (name.equals(CDXMLObj_Splitter)) {
        page.getSplitters().add(createSplitterObject(object));
      } else if (name.equals(CDXMLObj_ChemicalProperty)) {
        page.getChemicalProperties().add(createChemicalPropertyObject(object));
      } else {

        // TODO arrow, bioshape

        handleMissingObject(object);
      }
    }
    return page;
  }

  private void populatePageObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDPage page = (CDPage) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        page.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        page.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_WidthPages)) {
        page.setWidthPages(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_HeightPages)) {
        page.setHeightPages(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_DrawingSpaceType)) {
        page.setDrawingSpaceType(
            CDXMLUtils.convertStringToDrawingSpaceType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Width)) {
        page.setWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Height)) {
        page.setHeight(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_PageOverlap)) {
        page.setPageOverlap(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Header)) {
        page.setHeader(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_HeaderPosition)) {
        page.setHeaderPosition(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Footer)) {
        page.setFooter(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_FooterPosition)) {
        page.setFooterPosition(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_PrintTrimMarks)) {
        page.setPrintTrimMarks(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_SplitterPositions)) {
        // ignore
      } else if (name.equals(CDXMLProp_PageDefinition)) {
        page.setPageDefinition(CDXMLUtils.convertStringToPageDefinition(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BoundsInParent)) {
        page.setBoundsInParent(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDFragment createFragmentObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDFragment fragment = new CDFragment();
    handleReference(root, fragment);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Node)) {
        fragment.getAtoms().add(createNodeObject(object));
      } else if (name.equals(CDXMLObj_Bond)) {
        fragment.getBonds().add(createBondObject(object));
      } else if (name.equals(CDXMLObj_Graphic)) {
        fragment.getGraphics().add(createGraphicObject(object));
      } else if (name.equals(CDXMLObj_Curve)) {
        fragment.getCurves().add(createSplineObject(object));
      } else if (name.equals(CDXMLObj_ObjectTag)) {
        fragment.getObjectTags().add(createObjectTagObject(object));
      } else if (name.equals(CDXMLObj_Text)) {
        fragment.getTexts().add(createTextObject(object));
      } else if (name.equals(CDXMLObj_ColoredMolecularArea)) {
        fragment.getColoredMolecularAreas().add(createColoredMolecularArea(object));
      } else {

        // regnum

        handleMissingObject(object);
      }
    }
    return fragment;
  }

  private void populateFragmentObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDFragment fragment = (CDFragment) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        fragment.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Mole_Racemic)) {
        fragment.setRacemic(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Mole_Absolute)) {
        fragment.setAbsolute(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Mole_Relative)) {
        fragment.setRelative(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Mole_Formula)) {
        fragment.setFormula(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Mole_Weight)) {
        fragment.setWeight(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_Frag_ConnectionOrder)) {
        fragment.setConnectionOrder(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_Frag_SequenceType)) {
        fragment.setSequenceType(CDXMLUtils.convertStringToSequenceType(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDAtom createNodeObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDAtom node = new CDAtom();
    handleReference(root, node);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Fragment)) {
        node.getFragments().add(createFragmentObject(object));
      } else if (name.equals(CDXMLObj_Text)) {
        if (node.getText() != null) {
          throw new IOException("Unexpected object");
        }
        node.setText(createTextObject(object));

      } else if (name.equals(CDXMLObj_ObjectTag)) {
        node.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return node;
  }

  private void populateNodeObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDAtom node = (CDAtom) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        node.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        node.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        node.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        node.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_2DPosition)) {
        node.setPosition2D(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_3DPosition)) {
        node.setPosition3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        node.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        node.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_HighlightColor)) {
        node.getSettings().setHighlightColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_Node_Type)) {
        node.setNodeType(CDXMLUtils.convertStringToNodeType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Node_LabelDisplay)) {
        node.setLabelDisplay(CDXMLUtils.convertStringToLabelDisplay(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Node_Element)) {
        node.setElementNumber(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_ElementList)) {
        node.setElementList(CDXMLUtils.convertStringToElementList(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_Formula)) {
        node.setFormula(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_Isotope)) {
        node.setIsotope(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_Charge)) {
        node.setCharge(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_Radical)) {
        node.setRadical(CDXMLUtils.convertStringToRadical(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_RestrictFreeSites)) {
        node.setSubstituentCount(root.getAttributeAsInt(name));
        node.setSubstituentType(CDAtomSubstituentType.FreeSites);
      } else if (name.equals(CDXMLProp_Atom_RestrictImplicitHydrogens)) {
        node.setImplicitHydrogensAllowed(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_RestrictRingBondCount)) {
        node.setRingBondCount(CDXMLUtils.convertStringToRingBondCount(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_RestrictUnsaturatedBonds)) {
        node.setUnsaturatedBonds(CDXMLUtils.convertStringToUnsaturation(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_RestrictRxnChange)) {
        node.setRestrictReactionChange(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_RestrictRxnStereo)) {
        node.setReactionStereo(CDXMLUtils.convertStringToReactionStereo(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_AbnormalValence)) {
        node.setAbnormalValenceAllowed(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_NumHydrogens)) {
        node.setNumImplicitHydrogens(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_HDot)) {
        node.setHDot(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_HDash)) {
        node.setHDash(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_Geometry)) {
        node.setAtomGeometry(CDXMLUtils.convertStringToAtomGeometry(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_BondOrdering)) {
        node.setBondOrdering(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), CDBond.class, refManager));
      } else if (name.equals(CDXMLProp_Node_Attachments)) {
        node.setAttachedAtoms(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_Atom_GenericNickname)) {
        node.setLabelText(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Atom_AltGroupID)) {
        node.setAltGroup(
            CDXMLUtils.convertStringToObjectRef(
                root.getAttribute(name), CDAltGroup.class, refManager));
      } else if (name.equals(CDXMLProp_Atom_RestrictSubstituentsUpTo)) {
        node.setSubstituentCount(root.getAttributeAsInt(name));
        node.setSubstituentType(CDAtomSubstituentType.SubstituentsUpTo);
      } else if (name.equals(CDXMLProp_Atom_RestrictSubstituentsExactly)) {
        node.setSubstituentCount(root.getAttributeAsInt(name));
        node.setSubstituentType(CDAtomSubstituentType.SubstituentsExactly);
      } else if (name.equals(CDXMLProp_Atom_CIPStereochemistry)) {
        node.setStereochemistry(CDXMLUtils.convertStringToAtomCIPType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_Translation)) {
        node.setTranslation(CDXMLUtils.convertStringToTranslation(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_AtomNumber)) {
        node.setAtomNumber(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Atom_ShowQuery)) {
        node.getSettings().setShowAtomQuery(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowStereo)) {
        node.getSettings().setShowAtomStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowEnhancedStereo)) {
        node.getSettings().setShowAtomEnhancedStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_ShowAtomNumber)) {
        node.getSettings().setShowAtomNumber(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Atom_LinkCountLow)) {
        node.setLinkCountLow(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_LinkCountHigh)) {
        node.setLinkCountHigh(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Atom_IsotopicAbundance)) {
        node.setIsotopicAbundance(CDXMLUtils.convertStringToAbundance(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_ExternalConnectionType)) {
        node.setAttachmentPointType(
            CDXMLUtils.convertStringToExternalConnectionType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Atom_GenericList)) {
        node.setGenericList(CDXMLUtils.getAttributeAsGenericList(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        node.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        node.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        node.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        node.getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_MarginWidth)) {
        node.getSettings().setMarginWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_ShowTerminalCarbonLabels)) {
        node.getSettings().setShowTerminalCarbonLabels(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ShowNonTerminalCarbonLabels)) {
        node.getSettings().setShowNonTerminalCarbonLabels(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_HideImplicitHydrogens)) {
        node.getSettings().setHideImplicitHydrogens(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_NeedsClean)) {
        // do nothing
      } else {
        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDBond createBondObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDBond bond = new CDBond();
    handleReference(root, bond);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        bond.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return bond;
  }

  private void populateBondObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDBond bond = (CDBond) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        bond.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        bond.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        bond.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        bond.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        bond.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        bond.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_HighlightColor)) {
        bond.getSettings().setHighlightColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_Bond_Order)) {
        bond.setBondOrder(CDXMLUtils.convertStringToBondOrder(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_Display)) {
        bond.setBondDisplay(CDXMLUtils.convertStringToBondDisplay(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_Display2)) {
        bond.setBondDisplay2(CDXMLUtils.convertStringToBondDisplay(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_DoublePosition)) {
        bond.setBondDoublePosition(
            CDXMLUtils.convertStringToBondDoublePosition(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_Begin)) {
        bond.setBegin(
            CDXMLUtils.convertStringToObjectRef(root.getAttribute(name), CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_Bond_End)) {
        bond.setEnd(
            CDXMLUtils.convertStringToObjectRef(root.getAttribute(name), CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_Bond_RestrictTopology)) {
        bond.setTopology(CDXMLUtils.convertStringToBondTopology(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_RestrictRxnParticipation)) {
        bond.setReactionParticipation(
            CDXMLUtils.convertStringToBondReactionParticipation(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_BeginAttach)) {
        bond.setBeginAttach(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Bond_EndAttach)) {
        bond.setEndAttach(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Bond_CIPStereochemistry)) {
        bond.setStereochemistry(CDXMLUtils.convertStringToBondCIPType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bond_BondOrdering)) {
        bond.setBondCircularOrdering(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), CDBond.class, refManager));
      } else if (name.equals(CDXMLProp_Bond_ShowQuery)) {
        bond.getSettings().setShowBondQuery(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Bond_ShowStereo)) {
        bond.getSettings().setShowBondStereo(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Bond_CrossingBonds)) {
        bond.setCrossingBonds(
            new HashSet<CDBond>(
                CDXMLUtils.convertStringToObjectRefList(
                    root.getAttribute(name), CDBond.class, refManager)));
      } else if (name.equals(CDXMLProp_Bond_ShowRxn)) {
        bond.getSettings().setShowBondReaction(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BondSpacing)) {
        bond.getSettings().setBondSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BondLength)) {
        bond.getSettings().setBondLength(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        bond.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        bond.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_MarginWidth)) {
        bond.getSettings().setMarginWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        bond.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        bond.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        bond.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        bond.getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_BondSpacingAbs)) {
        bond.getSettings().setBondSpacingAbs(root.getAttributeAsFloat(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDTemplateGrid createTemplateGridObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDTemplateGrid templateGrid = new CDTemplateGrid();
    handleReference(root, templateGrid);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return templateGrid;
  }

  private void populateTemplateGridObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDTemplateGrid templateGrid = (CDTemplateGrid) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_2DExtent)) {
        templateGrid.setExtent(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Template_PaneHeight)) {
        templateGrid.setPaneHeight(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Template_NumRows)) {
        templateGrid.setNumRows(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Template_NumColumns)) {
        templateGrid.setNumColumns(root.getAttributeAsInt(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDGroup createGroupObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDGroup group = new CDGroup();
    handleReference(root, group);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Group)) {
        group.getGroups().add(createGroupObject(object));
      } else if (name.equals(CDXMLObj_Fragment)) {
        group.getFragments().add(createFragmentObject(object));
      } else if (name.equals(CDXMLObj_Text)) {
        group.getCaptions().add(createTextObject(object));
      } else if (name.equals(CDXMLObj_Graphic)) {
        group.getGraphics().add(createGraphicObject(object));
      } else if (name.equals(CDXMLObj_Arrow)) {
        group.getArrows().add(createArrowObject(object));
      } else if (name.equals(CDXMLObj_Curve)) {
        group.getCurves().add(createSplineObject(object));
      } else if (name.equals(CDXMLObj_NamedAlternativeGroup)) {
        group.getNamedAlternativeGroups().add(createNamedAlternativeGroupObject(object));
      } else if (name.equals(CDXMLObj_ReactionStep)) {
        group.getReactionSteps().add(createReactionStepObject(object));
      } else if (name.equals(CDXMLObj_Spectrum)) {
        group.getSpectra().add(createSpectrumObject(object));
      } else if (name.equals(CDXMLObj_EmbeddedObject)) {
        group.getEmbeddedObjects().add(createEmbeddedObjectObject(object));
      } else if (name.equals(CDXMLObj_ObjectTag)) {
        group.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return group;
  }

  private void populateGroupObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDGroup group = (CDGroup) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        group.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Group_Integral)) {
        group.setIntegral(root.getAttributeAsBoolean(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDText createTextObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDText text = new CDText();
    handleReference(root, text);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        text.getObjectTags().add(createObjectTagObject(object));
      } else if (name.equals(CDXMLObj_String)) {
        CDStyledString string = text.getText();
        if (string == null) {
          string = new CDStyledString();
        }
        string.getChunks().addAll(createStyledString(object).getChunks());
        text.setText(string);
      } else {

        handleMissingObject(object);
      }
    }
    return text;
  }

  private void populateTextObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDText text = (CDText) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        text.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        text.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        text.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        text.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_2DPosition)) {
        text.setPosition2D(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        text.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_RotationAngle)) {
        text.setAngle(root.getAttributeAsLong(name) / 65536.0f);
      } else if (name.equals(CDXMLProp_Justification)) {
        text.setJustification(CDXMLUtils.convertStringToTextJustification(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LineHeight)) {
        text.setLineHeight(CDXMLUtils.convertStringToLineHeight(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_WordWrapWidth)) {
        text.setWrapWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineStarts)) {
        text.setLineStarts(CDXMLUtils.convertStringToIntList(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LabelAlignment)) {
        text.setLabelAlignment(CDXMLUtils.convertStringToLabelDisplay(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LabelLineHeight)) {
        text.getSettings()
            .setLabelLineHeight(CDXMLUtils.convertStringToLineHeight(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_CaptionLineHeight)) {
        text.getSettings()
            .setCaptionLineHeight(CDXMLUtils.convertStringToLineHeight(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_InterpretChemically)) {
        text.getSettings().setInterpretChemically(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_CaptionJustification)) {
        text.getSettings()
            .setCaptionJustification(
                CDXMLUtils.convertStringToTextJustification(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        text.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleFont)) {
        text.getSettings().setCaptionFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        text.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleSize)) {
        text.getSettings().setCaptionSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        text.getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleFace)) {
        text.getSettings()
            .setCaptionFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleColor)) {
        text.getSettings().setLabelColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_CaptionStyleColor)) {
        text.getSettings().setCaptionColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_LabelJustification)) {
        text.getSettings()
            .setLabelJustification(
                CDXMLUtils.convertStringToTextJustification(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        text.setColor(readColorAttribute(root, name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    if (root.hasAttribute(CDXMLProp_Justification)) {
      text.getSettings()
          .setLabelJustification(
              CDXMLUtils.convertStringToTextJustification(
                  root.getAttribute(CDXMLProp_Justification)));
    }

    populateChildren(root);
  }

  private CDGraphic createGraphicObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDGraphic graphic = new CDGraphic();
    handleReference(root, graphic);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        graphic.getObjectTags().add(createObjectTagObject(object));
      } else if (name.equals(CDXMLObj_Represent)) {
        createRepresent(object, graphic.getRepresents());
      } else {

        handleMissingObject(object);
      }
    }
    return graphic;
  }

  private void populateGraphicObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDGraphic graphic = (CDGraphic) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_SupercededBy)) {
        graphic.setSupersededBy(
            CDXMLUtils.convertStringToObjectRef(
                root.getAttribute(name), CDObject.class, refManager));
      } else if (name.equals(CDXMLProp_ZOrder)) {
        graphic.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        graphic.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        graphic.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        graphic.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        graphic.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Head3D)) {
        graphic.setHead3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Tail3D)) {
        graphic.setTail3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Center3D)) {
        graphic.setCenter3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        graphic.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        graphic.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        graphic.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        graphic.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        graphic.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleFont)) {
        graphic.getSettings().setCaptionFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleSize)) {
        graphic.getSettings().setCaptionSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleFace)) {
        graphic
            .getSettings()
            .setCaptionFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_Graphic_Type)) {
        graphic.setGraphicType(CDXMLUtils.convertStringToGraphicType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Line_Type)) {
        graphic.setLineType(CDXMLUtils.convertStringToLineType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Arrow_Type)) {
        graphic.setArrowType(CDXMLUtils.convertStringToArrowType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Rectangle_Type)) {
        graphic.setRectangleType(CDXMLUtils.convertStringToRectangleType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Oval_Type)) {
        graphic.setOvalType(CDXMLUtils.convertStringToOvalType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Orbital_Type)) {
        graphic.setOrbitalType(CDXMLUtils.convertStringToOrbitalType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Bracket_Type)) {
        graphic.setBracketType(CDXMLUtils.convertStringToBracketType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Symbol_Type)) {
        graphic.setSymbolType(CDXMLUtils.convertStringToSymbolType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Arrow_HeadSize)) {
        graphic.setArrowHeadSize(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_Arc_AngularSize)) {
        graphic.setArcAngularSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Bracket_LipSize)) {
        graphic.setBracketLipSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Bracket_Usage)) {
        graphic.setBracketUsage(CDXMLUtils.convertStringToBracketUsage(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Polymer_RepeatPattern)) {
        graphic.setPolymerRepeatPattern(
            CDXMLUtils.convertStringToPolymerRepeatPattern(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Polymer_FlipType)) {
        graphic.setPolymerFlipType(
            CDXMLUtils.convertStringToPolymerFlipType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_MajorAxisEnd3D)) {
        graphic.setMajorAxisEnd3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_MinorAxisEnd3D)) {
        graphic.setMinorAxisEnd3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Curve_FillType)) {
        graphic.setFillType(CDXMLUtils.convertStringToFillType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ShadowSize)) {
        graphic.setShadowSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CornerRadius)) {
        graphic.setCornerRadius(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_FadePercent)) {
        graphic.setFadePercent(root.getAttributeAsInt(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDColoredMolecularArea createColoredMolecularArea(XMLObject root) throws IOException {
    handleCreation(root);
    CDColoredMolecularArea area = new CDColoredMolecularArea();
    handleReference(root, area);
    return area;
  }

  private void populateColoredMolecularArea(XMLObject root) throws IOException {
    handlePopulation(root);
    CDColoredMolecularArea area = (CDColoredMolecularArea) root.getInstance();

    for (String name : root.getAttributes().keySet()) {

      if (name.equals(CDXMLProp_BackgroundColor)) {
        area.setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BasisObjects)) {
        area.setBasisObjects(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), CDBond.class, refManager));
      } else {
        handleMissingAttribute(root, name);
      }
    }
    populateChildren(root);
  }

  private CDArrow createArrowObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDArrow arrow = new CDArrow();
    handleReference(root, arrow);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        arrow.getObjectTags().add(createObjectTagObject(object));
      } else {
        handleMissingObject(object);
      }
    }
    return arrow;
  }

  private void populateArrowObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDArrow graphic = (CDArrow) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        graphic.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        graphic.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        graphic.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        graphic.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        graphic.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Head3D)) {
        graphic.setHead3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Tail3D)) {
        graphic.setTail3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Center3D)) {
        graphic.setCenter3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        graphic.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        graphic.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        graphic.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        graphic.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        graphic.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleFont)) {
        graphic.getSettings().setCaptionFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_CaptionStyleSize)) {
        graphic.getSettings().setCaptionSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_CaptionStyleFace)) {
        graphic
            .getSettings()
            .setCaptionFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_Line_Type)) {
        graphic.setLineType(CDXMLUtils.convertStringToLineType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Curve_FillType)) {
        graphic.setFillType(CDXMLUtils.convertStringToFillType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Arrow_HeadSize)) {
        graphic.setHeadSize(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_Arc_AngularSize)) {
        graphic.setAngularSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_ArrowHeadWidth)) {
        graphic.setHeadWidth(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_ArrowHeadCenterSize)) {
        graphic.setHeadCenterSize(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_ArrowEquilibriumRatio)) {
        graphic.setEquilibriumRatio(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_MajorAxisEnd3D)) {
        graphic.setMajorAxisEnd3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_MinorAxisEnd3D)) {
        graphic.setMinorAxisEnd3D(CDXMLUtils.convertStringToPoint3D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Dipole)) {
        graphic.setDipole(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ArrowHeadType)) {
        graphic.setArrowHeadType(CDXMLUtils.convertStringToArrowheadType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ArrowHeadHead)) {
        graphic.setArrowHeadPositionStart(
            CDXMLUtils.convertStringToArrowhead(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ArrowHeadTail)) {
        graphic.setArrowHeadPositionTail(
            CDXMLUtils.convertStringToArrowhead(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ArrowShaftSpacing)) {
        graphic.setShaftSpacing(root.getAttributeAsFloat(name) / 100f);
      } else if (name.equals(CDXMLProp_NoGo)) {
        graphic.setNoGoType(CDXMLUtils.convertStringToNoGoType(root.getAttribute(name)));
      } else if (name.equals("FadePercent")) {
        // empty
      } else {
        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDBracket createBracketedGroupObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDBracket bracketedGroup = new CDBracket();
    handleReference(root, bracketedGroup);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_BracketedGroup)) {
        bracketedGroup.getBrackets().add(createBracketedGroupObject(object));
      } else if (name.equals(CDXMLObj_BracketAttachment)) {
        bracketedGroup.getBracketAttachments().add(createBracketAttachmentObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return bracketedGroup;
  }

  private void populateBracketedGroupObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDBracket bracketedGroup = (CDBracket) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Bracket_Usage)) {
        bracketedGroup.setBracketUsage(
            CDXMLUtils.convertStringToBracketUsage(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Polymer_RepeatPattern)) {
        bracketedGroup.setPolymerRepeatPattern(
            CDXMLUtils.convertStringToPolymerRepeatPattern(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Polymer_FlipType)) {
        bracketedGroup.setPolymerFlipType(
            CDXMLUtils.convertStringToPolymerFlipType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BracketedObjects)) {
        bracketedGroup.setBracketedObjects(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_Bracket_RepeatCount)) {
        bracketedGroup.setRepeatCount(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_Bracket_ComponentOrder)) {
        bracketedGroup.setComponentOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Bracket_SRULabel)) {
        bracketedGroup.setSRULabel(root.getAttribute(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDBracketAttachment createBracketAttachmentObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDBracketAttachment bracketAttachment = new CDBracketAttachment();
    handleReference(root, bracketAttachment);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_CrossingBond)) {
        bracketAttachment.getCrossingBonds().add(createCrossingBondObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return bracketAttachment;
  }

  private void populateBracketAttachmentObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDBracketAttachment bracketAttachment = (CDBracketAttachment) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Bracket_GraphicID)) {
        bracketAttachment.setGraphic(
            CDXMLUtils.convertStringToObjectRef(
                root.getAttribute(name), CDGraphic.class, refManager));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDCrossingBond createCrossingBondObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDCrossingBond crossingBond = new CDCrossingBond();
    handleReference(root, crossingBond);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return crossingBond;
  }

  private void populateCrossingBondObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDCrossingBond crossingBond = (CDCrossingBond) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Bracket_BondID)) {
        crossingBond.setBond(
            CDXMLUtils.convertStringToObjectRef(root.getAttribute(name), CDBond.class, refManager));
      } else if (name.equals(CDXMLProp_Bracket_InnerAtomID)) {
        crossingBond.setInnerAtom(
            CDXMLUtils.convertStringToObjectRef(root.getAttribute(name), CDAtom.class, refManager));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDSplitter createSplitterObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDSplitter splitter = new CDSplitter();
    handleReference(root, splitter);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return splitter;
  }

  private void populateSplitterObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDSplitter splitter = (CDSplitter) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_2DPosition)) {
        splitter.setPosition2D(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_PageDefinition)) {
        splitter.setPageDefinition(
            CDXMLUtils.convertStringToPageDefinition(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDTLCPlate createTLCPlateObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDTLCPlate plate = new CDTLCPlate();
    handleReference(root, plate);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        plate.getObjectTags().add(createObjectTagObject(object));
      } else if (name.equals(CDXMLObj_TLCLane)) {
        plate.getLanes().add(createTLCLaneObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return plate;
  }

  private void populateTLCPlateObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDTLCPlate plate = (CDTLCPlate) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        plate.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        plate.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        plate.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_TopLeft)) {
        plate.setTopLeft(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_TopRight)) {
        plate.setTopRight(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BottomRight)) {
        plate.setBottomRight(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BottomLeft)) {
        plate.setBottomLeft(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        plate.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        plate.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        plate.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        plate.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_MarginWidth)) {
        plate.getSettings().setMarginWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        plate.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        plate.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        plate
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_TLC_OriginFraction)) {
        plate.setOriginFraction(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_TLC_SolventFrontFraction)) {
        plate.setSolventFrontFraction(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_TLC_ShowOrigin)) {
        plate.setShowOrigin(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_TLC_ShowSolventFront)) {
        plate.setShowSolventFront(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_TLC_ShowBorders)) {
        plate.setShowBorders(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Transparent)) {
        plate.setTransparent(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ShowSideTicks)) {
        plate.setShowSideTicks(root.getAttributeAsBoolean(name));
      } else {
        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDTLCLane createTLCLaneObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDTLCLane lane = new CDTLCLane();
    handleReference(root, lane);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        lane.getObjectTags().add(createObjectTagObject(object));
      } else if (name.equals(CDXMLObj_TLCSpot)) {
        lane.getSpots().add(createTLCSpotObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return lane;
  }

  private void populateTLCLaneObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDTLCLane lane = (CDTLCLane) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Visible)) {
        lane.setVisible(root.getAttributeAsBoolean(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDTLCSpot createTLCSpotObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDTLCSpot spot = new CDTLCSpot();
    handleReference(root, spot);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        spot.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return spot;
  }

  private void populateTLCSpotObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDTLCSpot spot = (CDTLCSpot) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Visible)) {
        spot.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Width)) {
        spot.setWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Height)) {
        spot.setHeight(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Curve_Type)) {
        spot.setCurveType(CDXUtils.convertIntToSplineType(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_TLC_Rf)) {
        spot.setRf(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_TLC_Tail)) {
        spot.setTail(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_TLC_ShowRf)) {
        spot.setShowRf(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        spot.setColor(readColorAttribute(root, name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDConstraint createConstraintObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDConstraint constraint = new CDConstraint();
    handleReference(root, constraint);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        constraint.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return constraint;
  }

  private void populateConstraintObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDConstraint constraint = (CDConstraint) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Name)) {
        constraint.setName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        constraint.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BondLength)) {
        constraint.getSettings().setBondLength(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        constraint.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        constraint.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        constraint.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        constraint.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        constraint
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleColor)) {
        constraint.getSettings().setLabelColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BasisObjects)) {
        constraint.setBasisObjects(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ConstraintType)) {
        constraint.setConstraintType(
            CDXMLUtils.convertStringToConstraintType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ConstraintMin)) {
        constraint.setMinRange(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_ConstraintMax)) {
        constraint.setMaxRange(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_IgnoreUnconnectedAtoms)) {
        constraint.setIgnoreUnconnectedAtoms(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_DihedralIsChiral)) {
        constraint.setDihedralIsChiral(root.getAttributeAsBoolean(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDGeometry createGeometryObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDGeometry geometry = new CDGeometry();
    handleReference(root, geometry);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        geometry.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return geometry;
  }

  private void populateGeometryObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDGeometry geometry = (CDGeometry) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Name)) {
        geometry.setName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        geometry.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BondLength)) {
        geometry.getSettings().setBondLength(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        geometry.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        geometry.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name))); // deprecated
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        geometry.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        geometry
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleColor)) {
        geometry.getSettings().setLabelColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_GeometricFeature)) {
        geometry.setGeometricType(
            CDXMLUtils.convertStringToGeometricFeature(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_RelationValue)) {
        geometry.setRelationValue(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_BasisObjects)) {
        geometry.setBasisObjects(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_PointIsDirected)) {
        geometry.setPointIsDirected(root.getAttributeAsBoolean(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDBorder createBorderObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDBorder border = new CDBorder();
    handleReference(root, border);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return border;
  }

  private void populateBorderObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDBorder border = (CDBorder) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        border.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        border.setWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_Side)) {
        border.setSide(CDXMLUtils.convertStringToSideType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Line_Type)) {
        border.setLineType(CDXMLUtils.convertStringToLineType(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDCrossReference createCrossReferenceObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDCrossReference crossReference = new CDCrossReference();
    handleReference(root, crossReference);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return crossReference;
  }

  private void populateCrossReferenceObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDCrossReference crossReference = (CDCrossReference) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_CrossReference_Container)) {
        crossReference.setContainer(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CrossReference_Document)) {
        crossReference.setDocument(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CrossReference_Identifier)) {
        crossReference.setIdentifier(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CrossReference_Sequence)) {
        crossReference.setSequence(root.getAttribute(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDSequence createSequenceObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDSequence sequence = new CDSequence();
    handleReference(root, sequence);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return sequence;
  }

  private void populateSequenceObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDSequence sequence = (CDSequence) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Sequence_Identifier)) {
        sequence.setIdentifier(root.getAttribute(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDSpectrum createSpectrumObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDSpectrum spectrum = new CDSpectrum();
    handleReference(root, spectrum);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        spectrum.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return spectrum;
  }

  private void populateSpectrumObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDSpectrum spectrum = (CDSpectrum) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        spectrum.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        spectrum.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        spectrum.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        spectrum.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        spectrum.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        spectrum.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        spectrum.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        spectrum.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        spectrum.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        spectrum.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        spectrum.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        spectrum
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_Spectrum_XSpacing)) {
        spectrum.setXSpacing(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_Spectrum_XLow)) {
        spectrum.setXLow(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_Spectrum_XType)) {
        spectrum.setXType(CDXMLUtils.convertStringToSpectrumXType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Spectrum_YType)) {
        spectrum.setYType(CDXMLUtils.convertStringToSpectrumYType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Spectrum_XAxisLabel)) {
        spectrum.setXAxisLabel(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Spectrum_YAxisLabel)) {
        spectrum.setYAxisLabel(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Spectrum_Class)) {
        spectrum.setSpectrumClass(CDXMLUtils.convertStringToSpectrumClass(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Spectrum_YLow)) {
        spectrum.setYLow(root.getAttributeAsDouble(name));
      } else if (name.equals(CDXMLProp_Spectrum_YScale)) {
        spectrum.setYLow(root.getAttributeAsDouble(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    String text = root.getTextsAsString();
    if (text != null) {
      List<Double> values = new ArrayList<>();
      for (String part : text.split("\\s+")) {
        if (part.length() > 0) {
          values.add(Double.parseDouble(part));
        }
      }
      double[] array = new double[values.size()];
      int index = 0;
      for (Double value : values) {
        array[index++] = value;
      }
      spectrum.setDataPoint(array);
    }

    populateChildren(root);
  }

  private CDReactionStep createReactionStepObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDReactionStep reactionStep = new CDReactionStep();
    handleReference(root, reactionStep);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return reactionStep;
  }

  private void populateReactionStepObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDReactionStep reactionStep = (CDReactionStep) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ReactionStep_Atom_Map)) {
        reactionStep.setAtomMap(
            CDXMLUtils.convertStringtoObjectRefMap(
                root.getAttribute(name), CDAtom.class, CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Reactants)) {
        reactionStep.setReactants(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Products)) {
        reactionStep.setProducts(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Plusses)) {
        reactionStep.setPlusses(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Arrows)) {
        reactionStep.setArrows(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_ObjectsAboveArrow)) {
        reactionStep.setObjectsAboveArrow(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_ObjectsBelowArrow)) {
        reactionStep.setObjectsBelowArrow(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Atom_Map_Manual)) {
        reactionStep.setAtomMapManual(
            CDXMLUtils.convertStringtoObjectRefMap(
                root.getAttribute(name), CDAtom.class, CDAtom.class, refManager));
      } else if (name.equals(CDXMLProp_ReactionStep_Atom_Map_Auto)) {
        reactionStep.setAtomMapAuto(
            CDXMLUtils.convertStringtoObjectRefMap(
                root.getAttribute(name), CDAtom.class, CDAtom.class, refManager));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDReactionScheme createReactionSchemeObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDReactionScheme reactionScheme = new CDReactionScheme();
    handleReference(root, reactionScheme);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ReactionStep)) {
        reactionScheme.getSteps().add(createReactionStepObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return reactionScheme;
  }

  private void populateReactionSchemeObject(XMLObject root) throws IOException {
    handlePopulation(root);

    for (String name : root.getAttributes().keySet()) {

      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDAltGroup createNamedAlternativeGroupObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDAltGroup namedAlternativeGroup = new CDAltGroup();
    handleReference(root, namedAlternativeGroup);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Group)) {
        namedAlternativeGroup.getGroups().add(createGroupObject(object));
      } else if (name.equals(CDXMLObj_Fragment)) {
        namedAlternativeGroup.getFragments().add(createFragmentObject(object));
      } else if (name.equals(CDXMLObj_Text)) {
        namedAlternativeGroup.getCaptions().add(createTextObject(object));
      } else if (name.equals(CDXMLObj_ObjectTag)) {
        namedAlternativeGroup.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return namedAlternativeGroup;
  }

  private void populateNamedAlternativeGroupObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDAltGroup namedAlternativeGroup = (CDAltGroup) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        namedAlternativeGroup.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        namedAlternativeGroup.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        namedAlternativeGroup.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        namedAlternativeGroup.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        namedAlternativeGroup.setBounds(
            CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        namedAlternativeGroup.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        namedAlternativeGroup.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_NamedAlternativeGroup_TextFrame)) {
        namedAlternativeGroup.setTextFrame(
            CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_NamedAlternativeGroup_GroupFrame)) {
        namedAlternativeGroup.setGroupFrame(
            CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_NamedAlternativeGroup_Valence)) {
        namedAlternativeGroup.setValence(root.getAttributeAsInt(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDTable createTableObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDTable table = new CDTable();
    handleReference(root, table);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Page)) {
        table.getPages().add(createPageObject(object));
      } else if (name.equals(CDXMLObj_ObjectTag)) {
        table.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return table;
  }

  private void populateTableObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDTable table = (CDTable) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        table.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        table.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        table.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        table.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        table.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BoldWidth)) {
        table.getSettings().setBoldWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        table.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_MarginWidth)) {
        table.getSettings().setMarginWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFont)) {
        table.getSettings().setLabelFont(fonts.get(root.getAttributeAsInt(name)));
      } else if (name.equals(CDXMLProp_LabelStyleSize)) {
        table.getSettings().setLabelSize(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_LabelStyleFace)) {
        table
            .getSettings()
            .setLabelFace(CDXUtils.convertIntToFontFace(root.getAttributeAsInt(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDPicture createEmbeddedObjectObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDPicture embeddedObject = new CDPicture();
    handleReference(root, embeddedObject);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        embeddedObject.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return embeddedObject;
  }

  private void populateEmbeddedObjectObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDPicture embeddedObject = (CDPicture) root.getInstance();

    byte[] compressedEnhancedMetafile = null;
    int uncompressedEnhancedMetafileSize = 0;
    byte[] compressedOLEObject = null;
    int uncompressedOLEObjectSize = 0;
    byte[] compressedWindowsMetafile = null;
    int uncompressedWindowsMetafileSize = 0;

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        embeddedObject.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        embeddedObject.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_RotationAngle)) {
        embeddedObject.setRotationAngle(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        embeddedObject.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        embeddedObject.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_Picture_Edition)) {
        embeddedObject.setPictureEdition(
            CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Picture_EditionAlias)) {
        embeddedObject.setPictureEditionAlias(
            CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_MacPICT)) {
        embeddedObject.setMacPICT(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_WindowsMetafile)) {
        embeddedObject.setWindowsMetafile(
            CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_OLEObject)) {
        embeddedObject.setOleObject(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_EnhancedMetafile)) {
        embeddedObject.setEnhancedMetafile(
            CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_CompressedWindowsMetafile)) {
        compressedWindowsMetafile = Base64.getMimeDecoder().decode(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CompressedOLEObject)) {
        compressedOLEObject = Base64.getMimeDecoder().decode(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_CompressedEnhancedMetafile)) {
        compressedEnhancedMetafile = Base64.getMimeDecoder().decode(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_UncompressedWindowsMetafileSize)) {
        uncompressedWindowsMetafileSize = root.getAttributeAsInt(name);
      } else if (name.equals(CDXMLProp_UncompressedOLEObjectSize)) {
        uncompressedOLEObjectSize = root.getAttributeAsInt(name);
      } else if (name.equals(CDXMLProp_UncompressedEnhancedMetafileSize)) {
        uncompressedEnhancedMetafileSize = root.getAttributeAsInt(name);
      } else if (name.equals(CDXMLProp_GIF)) {
        embeddedObject.setGif(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_TIFF)) {
        embeddedObject.setTiff(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_PNG)) {
        embeddedObject.setPng(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_JPEG)) {
        embeddedObject.setJpeg(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_BMP)) {
        embeddedObject.setBmp(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    if (compressedEnhancedMetafile != null && uncompressedEnhancedMetafileSize > 0) {
      try {
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressedEnhancedMetafile, 0, compressedEnhancedMetafile.length);
        byte[] result = new byte[uncompressedEnhancedMetafileSize];
        decompresser.inflate(result);
        decompresser.end();
        embeddedObject.setEnhancedMetafile(result);
      } catch (DataFormatException e) {
        logger.error("Cannot uncompress data", e);
      }
    }

    if (compressedOLEObject != null && uncompressedOLEObjectSize > 0) {
      try {
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressedOLEObject, 0, compressedOLEObject.length);
        byte[] result = new byte[uncompressedOLEObjectSize];
        decompresser.inflate(result);
        decompresser.end();
        embeddedObject.setOleObject(result);
      } catch (DataFormatException e) {
        logger.error("Cannot uncompress data", e);
      }
    }

    if (compressedWindowsMetafile != null && uncompressedWindowsMetafileSize > 0) {
      try {
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressedWindowsMetafile, 0, compressedWindowsMetafile.length);
        byte[] result = new byte[uncompressedWindowsMetafileSize];
        decompresser.inflate(result);
        decompresser.end();
        embeddedObject.setWindowsMetafile(result);
      } catch (DataFormatException e) {
        logger.error("Cannot uncompress data", e);
      }
    }

    // work-around to fix wrong EMFs, which are WMFs
    //    if (embeddedObject.getEnhancedMetafile() != null) {
    //      try {
    //        new EmfMetafile(new ByteArrayInputStream(embeddedObject.getEnhancedMetafile()));
    //      } catch (Exception e) {
    //        try {
    //          new WmfMetafile(new ByteArrayInputStream(embeddedObject.getEnhancedMetafile()));
    //          embeddedObject.setWindowsMetafile(embeddedObject.getEnhancedMetafile());
    //          embeddedObject.setEnhancedMetafile(null);
    //        } catch (Exception e2) {
    //          // do nothing
    //        }
    //      }
    //    }

    populateChildren(root);
  }

  private CDSpline createSplineObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDSpline curve = new CDSpline();
    handleReference(root, curve);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_ObjectTag)) {
        curve.getObjectTags().add(createObjectTagObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return curve;
  }

  private void populateSplineObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDSpline spline = (CDSpline) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_ZOrder)) {
        spline.setZOrder(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_IgnoreWarnings)) {
        spline.setIgnoreWarnings(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ChemicalWarning)) {
        spline.setChemicalWarning(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_Visible)) {
        spline.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_BoundingBox)) {
        spline.setBounds(CDXMLUtils.convertStringToRectangle(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ForegroundColor)) {
        spline.setColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_BackgroundColor)) {
        spline.getSettings().setBackgroundColor(readColorAttribute(root, name));
      } else if (name.equals(CDXMLProp_Curve_Type)) {
        CDSplineType curveType = CDXUtils.convertIntToSplineType(root.getAttributeAsInt(name));
        spline.setFillType(curveType.getFillType());
        spline.setLineType(curveType.getLineType());
        spline.setClosed(curveType.isClosed());
      } else if (name.equals(CDXMLProp_Curve_Points)) {
        spline.setPoints2D(CDXMLUtils.convertStringToPoint2DArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Curve_Points3D)) {
        spline.setPoints3D(CDXMLUtils.convertStringToPoint3DArray(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_LineWidth)) {
        spline.getSettings().setLineWidth(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_HashSpacing)) {
        spline.getSettings().setHashSpacing(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_ArrowHeadType)) {
        spline.setArrowHeadType(CDXMLUtils.convertStringToArrowheadType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ArrowHeadHead)) {
        spline.setArrowHeadPositionAtStart(
            CDXMLUtils.convertStringToArrowhead(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ArrowHeadTail)) {
        spline.setArrowHeadPositionAtEnd(
            CDXMLUtils.convertStringToArrowhead(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Closed)) {
        spline.setClosed(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Curve_FillType)) {
        spline.setFillType(CDXMLUtils.convertStringToFillType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_Line_Type)) {
        spline.setLineType(CDXMLUtils.convertStringToLineType(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    if (root.hasAttribute(CDXMLProp_Curve_Type)) {
      CDSplineType curveType =
          CDXUtils.convertIntToSplineType(root.getAttributeAsInt(CDXMLProp_Curve_Type));
      spline.setFillType(curveType.getFillType());
      spline.setLineType(curveType.getLineType());
      spline.setClosed(curveType.isClosed());
    }

    populateChildren(root);
  }

  private CDObjectTag createObjectTagObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDObjectTag objectTag = new CDObjectTag();
    handleReference(root, objectTag);

    // read content
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Text)) {
        objectTag.getTexts().add(createTextObject(object));
      } else {

        handleMissingObject(object);
      }
    }
    return objectTag;
  }

  private void populateObjectTagObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDObjectTag objectTag = (CDObjectTag) root.getInstance();

    // read first type of property
    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_ObjectTag_Type)) {
        objectTag.setObjectTagType(
            CDXMLUtils.convertStringToObjectTagType(root.getAttribute(name)));
      }
    }

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_Visible)) {
        objectTag.setVisible(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_Name)) {
        objectTag.setName(root.getAttribute(name));
      } else if (name.equals(CDXMLProp_ObjectTag_Type)) {
        objectTag.setObjectTagType(
            CDXMLUtils.convertStringToObjectTagType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_ObjectTag_Tracking)) {
        objectTag.setTracking(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ObjectTag_Persistent)) {
        objectTag.setPersistent(root.getAttributeAsBoolean(name));
      } else if (name.equals(CDXMLProp_ObjectTag_Value)) {
        switch (objectTag.getObjectTagType()) {
          case Long:
            objectTag.setValue(root.getAttributeAsLong(name));
            break;
          case Double:
            objectTag.setValue(root.getAttributeAsDouble(name));
            break;
          case String:
            objectTag.setValue(root.getAttribute(name));
            break;
          case Undefined:
            objectTag.setValue(CDXMLUtils.convertStringToByteArray(root.getAttribute(name)));
            break;

          default:
            throw new IOException();
        }
      } else if (name.equals(CDXMLProp_Positioning)) {
        objectTag.setPositioningType(
            CDXMLUtils.convertStringToPositioningType(root.getAttribute(name)));
      } else if (name.equals(CDXMLProp_PositioningAngle)) {
        objectTag.setPositioningAngle(root.getAttributeAsFloat(name));
      } else if (name.equals(CDXMLProp_PositioningOffset)) {
        objectTag.setPositioningOffset(CDXMLUtils.convertStringToPoint2D(root.getAttribute(name)));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private CDChemicalProperty createChemicalPropertyObject(XMLObject root) throws IOException {
    handleCreation(root);
    CDChemicalProperty chemicalProperty = new CDChemicalProperty();
    handleReference(root, chemicalProperty);

    // read content
    for (XMLObject object : root.getObjects()) {

      handleMissingObject(object);
    }
    return chemicalProperty;
  }

  private void populateChemicalPropertyObject(XMLObject root) throws IOException {
    handlePopulation(root);
    CDChemicalProperty chemicalProperty = (CDChemicalProperty) root.getInstance();

    for (String name : root.getAttributes().keySet()) {
      if (name.equals(CDXMLProp_Id)) {
        // ignore
      } else if (name.equals(CDXMLProp_BasisObjects)) {
        chemicalProperty.setBasisObjects(
            CDXMLUtils.convertStringToObjectRefList(
                root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ChemicalPropertyType)) {
        chemicalProperty.setType(root.getAttributeAsInt(name));
      } else if (name.equals(CDXMLProp_ChemicalPropertyDisplayID)) {
        chemicalProperty.setDisplay(
            CDXMLUtils.convertStringToObjectRef(root.getAttribute(name), Object.class, refManager));
      } else if (name.equals(CDXMLProp_ChemicalPropertyIsActive)) {
        chemicalProperty.setActive(root.getAttributeAsBoolean(name));
      } else {

        handleMissingAttribute(root, name);
      }
    }

    populateChildren(root);
  }

  private void populateChildren(XMLObject root) throws IOException {
    for (XMLObject object : root.getObjects()) {
      String name = object.getName();
      if (name.equals(CDXMLObj_Document)) {
        populateDocumentObject(object);
      } else if (name.equals(CDXMLObj_ColorTable)) {
        // nothing
      } else if (name.equals(CDXMLObj_FontTable)) {
        // nothing
      } else if (name.equals(CDXMLObj_Page)) {
        populatePageObject(object);
      } else if (name.equals(CDXMLObj_Group)) {
        populateGroupObject(object);
      } else if (name.equals(CDXMLObj_Fragment)) {
        populateFragmentObject(object);
      } else if (name.equals(CDXMLObj_Node)) {
        populateNodeObject(object);
      } else if (name.equals(CDXMLObj_Bond)) {
        populateBondObject(object);
      } else if (name.equals(CDXMLObj_Text)) {
        populateTextObject(object);
      } else if (name.equals(CDXMLObj_String)) {
        // nothing
      } else if (name.equals(CDXMLObj_Graphic)) {
        populateGraphicObject(object);
      } else if (name.equals(CDXMLObj_Arrow)) {
        populateArrowObject(object);
      } else if (name.equals(CDXMLObj_Represent)) {
        // nothing
      } else if (name.equals(CDXMLObj_Curve)) {
        populateSplineObject(object);
      } else if (name.equals(CDXMLObj_EmbeddedObject)) {
        populateEmbeddedObjectObject(object);
      } else if (name.equals(CDXMLObj_NamedAlternativeGroup)) {
        populateNamedAlternativeGroupObject(object);
      } else if (name.equals(CDXMLObj_TemplateGrid)) {
        populateTemplateGridObject(object);
      } else if (name.equals(CDXMLObj_ReactionScheme)) {
        populateReactionSchemeObject(object);
      } else if (name.equals(CDXMLObj_ReactionStep)) {
        populateReactionStepObject(object);
      } else if (name.equals(CDXMLObj_Spectrum)) {
        populateSpectrumObject(object);
      } else if (name.equals(CDXMLObj_ObjectTag)) {
        populateObjectTagObject(object);
      } else if (name.equals(CDXMLObj_Sequence)) {
        populateSequenceObject(object);
      } else if (name.equals(CDXMLObj_CrossReference)) {
        populateCrossReferenceObject(object);
      } else if (name.equals(CDXMLObj_Splitter)) {
        populateSplitterObject(object);
      } else if (name.equals(CDXMLObj_Table)) {
        populateTableObject(object);
      } else if (name.equals(CDXMLObj_BracketedGroup)) {
        populateBracketedGroupObject(object);
      } else if (name.equals(CDXMLObj_BracketAttachment)) {
        populateBracketAttachmentObject(object);
      } else if (name.equals(CDXMLObj_CrossingBond)) {
        populateCrossingBondObject(object);
      } else if (name.equals(CDXMLObj_Border)) {
        populateBorderObject(object);
      } else if (name.equals(CDXMLObj_Geometry)) {
        populateGeometryObject(object);
      } else if (name.equals(CDXMLObj_Constraint)) {
        populateConstraintObject(object);
      } else if (name.equals(CDXMLObj_TLCPlate)) {
        populateTLCPlateObject(object);
      } else if (name.equals(CDXMLObj_TLCLane)) {
        populateTLCLaneObject(object);
      } else if (name.equals(CDXMLObj_TLCSpot)) {
        populateTLCSpotObject(object);
      } else if (name.equals(CDXMLObj_ChemicalProperty)) {
        populateChemicalPropertyObject(object);
      } else if (name.equals(CDXMLObj_ColoredMolecularArea)) {
        populateColoredMolecularArea(object);
      } else {
        handleMissingObject(object);
      }
    }
  }

  private CDStyledString createStyledString(XMLObject object) throws IOException {
    CDFont font =
        object.hasAttribute(CDXMLProp_Font)
            ? fonts.get(object.getAttributeAsInt(CDXMLProp_Font))
            : null;
    float size =
        object.hasAttribute(CDXMLProp_FontSize)
            ? object.getAttributeAsFloat(CDXMLProp_FontSize)
            : 0;
    CDFontFace fontType =
        object.hasAttribute(CDXMLProp_FontFace)
            ? CDXUtils.convertIntToFontFace(object.getAttributeAsInt(CDXMLProp_FontFace))
            : new CDFontFace();
    CDColor color = readColorAttribute(object, CDXMLProp_ForegroundColor);

    CDStyledString string = new CDStyledString();
    string
        .getChunks()
        .add(new CDStyledString.CDXChunk(font, size, fontType, color, object.getTextsAsString()));
    return string;
  }

  private void createRepresent(XMLObject object, Map<String, Object> represents)
      throws IOException {
    String name = object.getAttribute(CDXMLProp_Attribute);
    Object value =
        CDXMLUtils.convertStringToObjectRef(
            object.getAttribute(CDXMLProp_Object), Object.class, refManager);
    if (value == null) {
      throw new IOException("Found null object as repesent value");
    }
    represents.put(name, value);
  }

  private CDColor readColorAttribute(XMLObject object, String name) throws IOException {
    if (object.hasAttribute(name)) {
      if (colors.containsKey(object.getAttributeAsInt(name))) {
        return colors.get(object.getAttributeAsInt(name));
      }
      logger.warn("Could not resolve color index to color: " + object.getAttributeAsInt(name));
    }
    return null;
  }

  private void handleReference(XMLObject root, Object reference) throws IOException {
    root.setInstance(reference);

    if (root.hasAttribute(CDXMLProp_Id)) {
      int id = root.getAttributeAsInt(CDXMLProp_Id);
      refManager.putObjectRef(id, reference);
    }
  }

  private void handleCreation(XMLObject object) {
    if (logger.isDebugEnabled()) {
      logger.debug("create " + object.getName() + " object at " + object.getLocation());
    }
  }

  private void handlePopulation(XMLObject object) {
    if (logger.isDebugEnabled()) {
      logger.debug("populate " + object.getName() + " object at " + object.getLocation());
    }
  }

  private void handleMissingObject(XMLObject object) throws IOException {
    String message =
        "Encountered unexpected element \'" + object.getName() + "\' at " + object.getLocation();

    logger.warn(message);
    if (object.getName().equals("annotation")) {
      return;
    }
    throw new IOException(message);
  }

  private void handleMissingAttribute(XMLObject object, String attribute) throws IOException {
    String message =
        "Encountered unexpected attribute \'"
            + attribute
            + "\' at element \'"
            + object.getName()
            + "\'"
            + " (value="
            + object.getAttributes().get(attribute)
            + ") at "
            + object.getLocation();

    if (RIGID) {
      throw new IOException(message);
    }
    logger.warn(message);
  }
}
