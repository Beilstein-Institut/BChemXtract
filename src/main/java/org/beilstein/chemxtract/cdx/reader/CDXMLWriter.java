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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
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
import org.beilstein.chemxtract.cdx.CDObjectTag;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDPicture;
import org.beilstein.chemxtract.cdx.CDReactionScheme;
import org.beilstein.chemxtract.cdx.CDReactionStep;
import org.beilstein.chemxtract.cdx.CDRectangle;
import org.beilstein.chemxtract.cdx.CDSequence;
import org.beilstein.chemxtract.cdx.CDSettings;
import org.beilstein.chemxtract.cdx.CDSpectrum;
import org.beilstein.chemxtract.cdx.CDSpline;
import org.beilstein.chemxtract.cdx.CDSplitter;
import org.beilstein.chemxtract.cdx.CDTLCLane;
import org.beilstein.chemxtract.cdx.CDTLCPlate;
import org.beilstein.chemxtract.cdx.CDTLCSpot;
import org.beilstein.chemxtract.cdx.CDTable;
import org.beilstein.chemxtract.cdx.CDTemplateGrid;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadPositionType;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadType;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomSubstituentType;
import org.beilstein.chemxtract.cdx.datatypes.CDBondCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDoublePosition;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDBondReactionParticipation;
import org.beilstein.chemxtract.cdx.datatypes.CDBondTopology;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketType;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;
import org.beilstein.chemxtract.cdx.datatypes.CDCharSet;
import org.beilstein.chemxtract.cdx.datatypes.CDColor;
import org.beilstein.chemxtract.cdx.datatypes.CDConstraintType;
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDElementList;
import org.beilstein.chemxtract.cdx.datatypes.CDExternalConnectionType;
import org.beilstein.chemxtract.cdx.datatypes.CDFillType;
import org.beilstein.chemxtract.cdx.datatypes.CDFont;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
import org.beilstein.chemxtract.cdx.datatypes.CDGenericList;
import org.beilstein.chemxtract.cdx.datatypes.CDGeometryType;
import org.beilstein.chemxtract.cdx.datatypes.CDGraphicType;
import org.beilstein.chemxtract.cdx.datatypes.CDIsotopicAbundance;
import org.beilstein.chemxtract.cdx.datatypes.CDJustification;
import org.beilstein.chemxtract.cdx.datatypes.CDLabelDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDLineType;
import org.beilstein.chemxtract.cdx.datatypes.CDNoGoType;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDObjectTagType;
import org.beilstein.chemxtract.cdx.datatypes.CDOrbitalType;
import org.beilstein.chemxtract.cdx.datatypes.CDOvalType;
import org.beilstein.chemxtract.cdx.datatypes.CDPageDefinition;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint3D;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerFlipType;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerRepeatPattern;
import org.beilstein.chemxtract.cdx.datatypes.CDPositioningType;
import org.beilstein.chemxtract.cdx.datatypes.CDRadical;
import org.beilstein.chemxtract.cdx.datatypes.CDReactionStereo;
import org.beilstein.chemxtract.cdx.datatypes.CDRectangleType;
import org.beilstein.chemxtract.cdx.datatypes.CDRingBondCount;
import org.beilstein.chemxtract.cdx.datatypes.CDSequenceType;
import org.beilstein.chemxtract.cdx.datatypes.CDSideType;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumClass;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumXType;
import org.beilstein.chemxtract.cdx.datatypes.CDSpectrumYType;
import org.beilstein.chemxtract.cdx.datatypes.CDSplineType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.cdx.datatypes.CDSymbolType;
import org.beilstein.chemxtract.cdx.datatypes.CDTranslation;
import org.beilstein.chemxtract.cdx.datatypes.CDUnsaturation;
import org.beilstein.chemxtract.io.IOUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** Experimental writer for ChemDraw CDXML files. */
// TODO ColoredMolecularAreas
public class CDXMLWriter {
  private ContentHandler handler;

  private Map<Object, Integer> references = new LinkedHashMap<>();
  private Map<CDColor, Integer> colors = new LinkedHashMap<>();
  private Map<Integer, CDColor> colorsInverse = new LinkedHashMap<>();
  private Map<CDFont, Integer> fonts = new LinkedHashMap<>();
  private Map<Integer, CDFont> fontsInverse = new LinkedHashMap<>();

  private CDXMLWriter(ContentHandler handler) {
    this.handler = handler;

    colorsInverse.put(0, CDColor.BLACK);
    colors.put(CDColor.BLACK, 0);
    colorsInverse.put(1, CDColor.WHITE);
    colors.put(CDColor.WHITE, 1);
    colorsInverse.put(2, CDColor.WHITE);
    colors.put(CDColor.WHITE, 2);
    colorsInverse.put(3, CDColor.BLACK);
    colors.put(CDColor.BLACK, 3);
    colorsInverse.put(4, CDColor.RED);
    colors.put(CDColor.RED, 4);
    colorsInverse.put(5, CDColor.YELLOW);
    colors.put(CDColor.YELLOW, 5);
    colorsInverse.put(6, CDColor.GREEN);
    colors.put(CDColor.GREEN, 6);
    colorsInverse.put(7, CDColor.CYAN);
    colors.put(CDColor.CYAN, 7);
    colorsInverse.put(8, CDColor.BLUE);
    colors.put(CDColor.BLUE, 8);
    colorsInverse.put(9, CDColor.MAGENTA);
    colors.put(CDColor.MAGENTA, 9);

    collectColor(new CDColor(1, 1, 1));
    collectColor(new CDColor(0, 0, 0));
    collectColor(new CDColor(1, 0, 0));
    collectColor(new CDColor(1, 1, 0));
    collectColor(new CDColor(0, 1, 0));
    collectColor(new CDColor(0, 1, 1));
    collectColor(new CDColor(0, 0, 1));
    collectColor(new CDColor(1, 0, 1));
  }

  /**
   * This method writes the {@link CDDocument} into a {@link OutputStream}.
   *
   * @param document ChemDraw document, which should be written
   * @param out {@link OutputStream}, which retrieves the generated output
   * @return The generated output as text string
   * @throws IOException Occurs if the writer couldn't write the output into the {@link
   *     OutputStream}
   * @throws IOException Occurs if an exception occur during the generation of the output
   */
  public static String writeDocument(CDDocument document, OutputStream out)
      throws IOException, IOException {
    SAXTransformerFactory serializerfactory =
        (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    try {
      serializerfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (TransformerConfigurationException e) {
      throw new IOException("Could not create XML serializer", e);
    }

    Properties format = new Properties();
    format.put(OutputKeys.ENCODING, "UTF-8");
    format.put(OutputKeys.INDENT, "yes");
    format.put(OutputKeys.METHOD, "xml");

    TransformerHandler handler;
    try {
      handler = serializerfactory.newTransformerHandler();
    } catch (TransformerConfigurationException e) {
      throw new IOException("Could not create XML serializer", e);
    }
    handler.getTransformer().setOutputProperties(format);

    CharArrayWriter writer = new CharArrayWriter();
    handler.setResult(new StreamResult(writer));

    try {
      handler.startDocument();
      handler.startDTD(CDXMLConstants.CDXMLObj_Document, null, CDXMLConstants.DTD);
      handler.endDTD();

      CDXMLWriter documentWriter = new CDXMLWriter(handler);
      documentWriter.collectDocument(document);
      documentWriter.writeDocument(document);

      handler.endDocument();
    } catch (SAXException e) {
      throw new IOException("Could not serialize XML document", e);
    }

    String text = writer.toString();
    if (out != null) {
      IOUtils.writeText(out, text);
    }
    return text;
  }

  private void collectFont(CDFont font) {
    if (font == null) {
      return;
    }
    if (!fonts.keySet().contains(font)) {
      fontsInverse.put(fonts.keySet().size(), font);
      fonts.put(font, fonts.keySet().size());
    }
  }

  private void collectColor(CDColor color) {
    if (color == null) {
      return;
    }
    if (!colors.keySet().contains(color)) {
      colorsInverse.put(colors.keySet().size() + 2, color);
      colors.put(color, colors.keySet().size() + 2);
    }
  }

  private void collectReference(Object object) {
    if (object == null) {
      return;
    }
    if (!references.keySet().contains(object)) {
      references.put(object, references.keySet().size());
    }
  }

  private void collectDocument(CDDocument document) {
    collectReference(document);
    collectFont(document.getSettings().getLabelFont());
    collectColor(document.getSettings().getLabelColor());
    collectFont(document.getSettings().getCaptionFont());
    collectColor(document.getSettings().getCaptionColor());

    for (CDPage page : document.getPages()) {
      collectPage(page);
    }
    if (document.getTemplateGrid() != null) {
      collectTemplateGrid(document.getTemplateGrid());
    }
  }

  private void writeDocument(CDDocument document) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, document.getBoundingBox());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_MacPrintInfo, document.getMacPrintInfo());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_WinPrintInfo, document.getWinPrintInfo());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_PrintMargins, document.getPrintMargins());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ChainAngle, document.getSettings().getChainAngle());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BondSpacing, document.getSettings().getBondSpacing());
    if (document.getSettings().getBondSpacingAbs() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_BondSpacingAbs,
          document.getSettings().getBondSpacingAbs());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BondLength, document.getSettings().getBondLength());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BoldWidth, document.getSettings().getBoldWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, document.getSettings().getLineWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_MarginWidth, document.getSettings().getMarginWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_HashSpacing, document.getSettings().getHashSpacing());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, document.getSettings().getLabelFont());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, document.getSettings().getLabelSize());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, document.getSettings().getLabelFace());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_LabelStyleColor,
        document.getSettings().getLabelColor());
    addLineHeightAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_LabelLineHeight,
        document.getSettings().getLabelLineHeight());
    if (document.getSettings().getLabelJustification() != CDJustification.Left) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_LabelJustification,
          document.getSettings().getLabelJustification());
    }
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFont,
        document.getSettings().getCaptionFont());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleSize,
        document.getSettings().getCaptionSize());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFace,
        document.getSettings().getCaptionFace());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleColor,
        document.getSettings().getCaptionColor());
    if (document.getSettings().getCaptionLineHeight() != CDSettings.LineHeight_Automatic) {
      addLineHeightAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionLineHeight,
          document.getSettings().getCaptionLineHeight());
    }
    if (document.getSettings().getCaptionJustification() != CDJustification.Left) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionJustification,
          document.getSettings().getCaptionJustification());
    }
    if (document.isFractionalWidths()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_FractionalWidths, document.isFractionalWidths());
    }
    if (!document.getSettings().isInterpretChemically()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_InterpretChemically,
          document.getSettings().isInterpretChemically());
    }
    if (!document.getSettings().isShowAtomQuery()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowQuery,
          document.getSettings().isShowAtomQuery());
    }
    if (document.getSettings().isShowAtomStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowStereo,
          document.getSettings().isShowAtomStereo());
    }
    if (!document.getSettings().isShowAtomEnhancedStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowEnhancedStereo,
          document.getSettings().isShowAtomEnhancedStereo());
    }
    if (document.getSettings().isShowAtomNumber()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowAtomNumber,
          document.getSettings().isShowAtomNumber());
    }
    if (!document.getSettings().isShowBondQuery()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowQuery,
          document.getSettings().isShowBondQuery());
    }
    if (document.getSettings().isShowBondStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowStereo,
          document.getSettings().isShowBondStereo());
    }
    if (document.getSettings().isShowBondReaction()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowRxn,
          document.getSettings().isShowBondReaction());
    }
    if (document.getSettings().isShowTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ShowTerminalCarbonLabels,
          document.getSettings().isShowTerminalCarbonLabels());
    }
    if (document.getSettings().isShowNonTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ShowNonTerminalCarbonLabels,
          document.getSettings().isShowNonTerminalCarbonLabels());
    }
    if (document.getSettings().isHideImplicitHydrogens()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_HideImplicitHydrogens,
          document.getSettings().isHideImplicitHydrogens());
    }
    if (document.getMagnification() != 0.0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Magnification,
          (int) (document.getMagnification() * 10f));
    }
    if (document.isWindowIsZoomed()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Window_IsZoomed, document.isWindowIsZoomed());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Window_Position, document.getWindowPosition());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Window_Size, document.getWindowSize());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CreationUserName, document.getCreationUserName());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_CreationDate, document.getCreationDate());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CreationProgram, document.getCreationProgram());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ModificationUserName,
        document.getModificationUserName());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ModificationDate, document.getModificationDate());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ModificationProgram,
        document.getModificationProgram());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Name, document.getName());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Comment, document.getComment());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_CartridgeData, document.getCartridgeData());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_FixInplaceExtent, document.getFixInPlaceExtent());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_FixInplaceGap, document.getFixInPlaceGap());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Document,
        CDXMLConstants.CDXMLObj_Document,
        attributes);

    writeColorTable();
    writeFontTable();

    for (CDPage page : document.getPages()) {
      writePage(page);
    }
    if (document.getTemplateGrid() != null) {
      writeTemplateGrid(document.getTemplateGrid());
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Document, CDXMLConstants.CDXMLObj_Document);
  }

  private void writeColorTable() throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ColorTable,
        CDXMLConstants.CDXMLObj_ColorTable,
        attributes);
    List<Integer> indices = new ArrayList<>(colorsInverse.keySet());
    Collections.sort(indices);
    for (Integer index : indices) {
      if (index < 2) {
        continue;
      }

      CDColor color = colorsInverse.get(index);
      attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Red, color.getRed());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Green, color.getGreen());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Blue, color.getBlue());
      handler.startElement(
          CDXMLConstants.NS,
          CDXMLConstants.CDXMLObj_Color,
          CDXMLConstants.CDXMLObj_Color,
          attributes);
      handler.endElement(
          CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Color, CDXMLConstants.CDXMLObj_Color);
    }
    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_ColorTable, CDXMLConstants.CDXMLObj_ColorTable);
  }

  private void writeFontTable() throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_FontTable,
        CDXMLConstants.CDXMLObj_FontTable,
        attributes);
    List<Integer> indices = new ArrayList<>(fontsInverse.keySet());
    Collections.sort(indices);
    for (Integer index : indices) {
      CDFont font = fontsInverse.get(index);

      attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Id, index);
      addAttribute(attributes, CDXMLConstants.CDXMLProp_CharSet, font.getCharSet());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Font_Name, font.getName());
      handler.startElement(
          CDXMLConstants.NS,
          CDXMLConstants.CDXMLObj_Font,
          CDXMLConstants.CDXMLObj_Font,
          attributes);
      handler.endElement(
          CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Font, CDXMLConstants.CDXMLObj_Font);
    }
    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_FontTable, CDXMLConstants.CDXMLObj_FontTable);
  }

  private void collectPage(CDPage page) {
    collectReference(page);
    collectColor(page.getSettings().getBackgroundColor());

    for (CDText text : page.getTexts()) {
      collectText(text);
    }
    for (CDFragment fragment : page.getFragments()) {
      collectFragment(fragment);
    }
    for (CDGroup group : page.getGroups()) {
      collectGroup(group);
    }
    for (CDGraphic graphic : page.getGraphics()) {
      collectGraphic(graphic);
    }
    for (CDArrow arrow : page.getArrows()) {
      collectArrow(arrow);
    }
    for (CDAltGroup namedAlternativeGroup : page.getNamedAlternativeGroups()) {
      collectNamedAlternativeGroup(namedAlternativeGroup);
    }
    for (CDSpline curve : page.getCurves()) {
      collectCurve(curve);
    }
    for (CDReactionStep reactionStep : page.getReactionSteps()) {
      collectReactionStep(reactionStep);
    }
    for (CDReactionScheme reactionScheme : page.getReactionSchemes()) {
      collectReactionScheme(reactionScheme);
    }
    for (CDSpectrum spectrum : page.getSpectra()) {
      collectSpectrum(spectrum);
    }
    for (CDPicture embeddedObject : page.getEmbeddedObjects()) {
      collectEmbeddedObject(embeddedObject);
    }
    for (CDSequence sequence : page.getSequences()) {
      collectSequence(sequence);
    }
    for (CDCrossReference crossReference : page.getCrossReferences()) {
      collectCrossReference(crossReference);
    }
    for (CDSplitter splitter : page.getSplitters()) {
      collectSplitter(splitter);
    }
    for (CDTable table : page.getTables()) {
      collectTable(table);
    }
    for (CDBracket bracketedGroup : page.getBracketedGroups()) {
      collectBracketedGroup(bracketedGroup);
    }
    for (CDBorder border : page.getBorders()) {
      collectBorder(border);
    }
    for (CDGeometry geometry : page.getGeometries()) {
      collectGeometry(geometry);
    }
    for (CDConstraint constraint : page.getConstraints()) {
      collectConstraint(constraint);
    }
    for (CDTLCPlate tlcPlate : page.getTLCPlates()) {
      collectTLCPlate(tlcPlate);
    }
    for (CDChemicalProperty chemicalProperty : page.getChemicalProperties()) {
      collectChemicalProperty(chemicalProperty);
    }
  }

  private void writePage(CDPage page) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, page);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, page.getBounds());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, page.getBoundsInParent());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        page.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_WidthPages, page.getWidthPages());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_HeightPages, page.getHeightPages());
    if (page.isPrintTrimMarks()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_PrintTrimMarks, page.isPrintTrimMarks());
    }
    if (page.getWidth() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Width, page.getWidth());
    }
    if (page.getHeight() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Height, page.getHeight());
    }
    if (page.getPageOverlap() != 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_PageOverlap, page.getPageOverlap());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Header, page.getHeader());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_HeaderPosition, page.getHeaderPosition());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Footer, page.getFooter());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_FooterPosition, page.getFooterPosition());
    if (page.getDrawingSpaceType() != CDDrawingSpaceType.Pages) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_DrawingSpaceType, page.getDrawingSpaceType());
    }
    if (page.getPageDefinition() != CDPageDefinition.Undefined) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_PageDefinition, page.getPageDefinition());
    }

    handler.startElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Page, CDXMLConstants.CDXMLObj_Page, attributes);

    for (CDFragment fragment : page.getFragments()) {
      writeFragment(fragment);
    }
    for (CDGroup group : page.getGroups()) {
      writeGroup(group);
    }
    for (CDText text : page.getTexts()) {
      writeText(text);
    }
    for (CDGraphic graphic : page.getGraphics()) {
      writeGraphic(graphic);
    }
    for (CDArrow arrow : page.getArrows()) {
      writeArrow(arrow);
    }
    for (CDAltGroup namedAlternativeGroup : page.getNamedAlternativeGroups()) {
      writeNamedAlternativeGroup(namedAlternativeGroup);
    }
    for (CDSpline curve : page.getCurves()) {
      writeCurve(curve);
    }
    for (CDReactionStep reactionStep : page.getReactionSteps()) {
      writeReactionStep(reactionStep);
    }
    for (CDReactionScheme reactionScheme : page.getReactionSchemes()) {
      writeReactionScheme(reactionScheme);
    }
    for (CDSpectrum spectrum : page.getSpectra()) {
      writeSpectrum(spectrum);
    }
    for (CDPicture embeddedObject : page.getEmbeddedObjects()) {
      writeEmbeddedObject(embeddedObject);
    }
    for (CDSequence sequence : page.getSequences()) {
      writeSequence(sequence);
    }
    for (CDCrossReference crossReference : page.getCrossReferences()) {
      writeCrossReference(crossReference);
    }
    for (CDSplitter splitter : page.getSplitters()) {
      writeSplitter(splitter);
    }
    for (CDTable table : page.getTables()) {
      writeTable(table);
    }
    for (CDBracket bracketedGroup : page.getBracketedGroups()) {
      writeBracketedGroup(bracketedGroup);
    }
    for (CDBorder border : page.getBorders()) {
      writeBorder(border);
    }
    for (CDGeometry geometry : page.getGeometries()) {
      writeGeometry(geometry);
    }
    for (CDConstraint constraint : page.getConstraints()) {
      writeConstraint(constraint);
    }
    for (CDTLCPlate tlcPlate : page.getTLCPlates()) {
      writeTLCPlate(tlcPlate);
    }
    for (CDChemicalProperty chemicalProperty : page.getChemicalProperties()) {
      writeChemicalProperty(chemicalProperty);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Page, CDXMLConstants.CDXMLObj_Page);
  }

  private void collectGroup(CDGroup group) {
    collectReference(group);

    for (CDText text : group.getCaptions()) {
      collectText(text);
    }
    for (CDFragment fragment : group.getFragments()) {
      collectFragment(fragment);
    }
    for (CDGroup group2 : group.getGroups()) {
      collectGroup(group2);
    }
    for (CDGraphic graphic : group.getGraphics()) {
      collectGraphic(graphic);
    }
    for (CDArrow arrow : group.getArrows()) {
      collectArrow(arrow);
    }
    for (CDAltGroup namedAlternativeGroup : group.getNamedAlternativeGroups()) {
      collectNamedAlternativeGroup(namedAlternativeGroup);
    }
    for (CDSpline curve : group.getCurves()) {
      collectCurve(curve);
    }
    for (CDReactionStep reactionStep : group.getReactionSteps()) {
      collectReactionStep(reactionStep);
    }
    for (CDSpectrum spectrum : group.getSpectra()) {
      collectSpectrum(spectrum);
    }
    for (CDPicture embeddedObject : group.getEmbeddedObjects()) {
      collectEmbeddedObject(embeddedObject);
    }
    for (CDObjectTag objectTag : group.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeGroup(CDGroup group) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, group);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, group.getBounds());
    if (group.isIntegral()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Group_Integral, group.isIntegral());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Group,
        CDXMLConstants.CDXMLObj_Group,
        attributes);

    for (CDText text : group.getCaptions()) {
      writeText(text);
    }
    for (CDFragment fragment : group.getFragments()) {
      writeFragment(fragment);
    }
    for (CDGroup group2 : group.getGroups()) {
      writeGroup(group2);
    }
    for (CDGraphic graphic : group.getGraphics()) {
      writeGraphic(graphic);
    }
    for (CDArrow arrow : group.getArrows()) {
      writeArrow(arrow);
    }
    for (CDAltGroup namedAlternativeGroup : group.getNamedAlternativeGroups()) {
      writeNamedAlternativeGroup(namedAlternativeGroup);
    }
    for (CDSpline curve : group.getCurves()) {
      writeCurve(curve);
    }
    for (CDReactionStep reactionStep : group.getReactionSteps()) {
      writeReactionStep(reactionStep);
    }
    for (CDSpectrum spectrum : group.getSpectra()) {
      writeSpectrum(spectrum);
    }
    for (CDPicture embeddedObject : group.getEmbeddedObjects()) {
      writeEmbeddedObject(embeddedObject);
    }
    for (CDObjectTag objectTag : group.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Group, CDXMLConstants.CDXMLObj_Group);
  }

  private void collectFragment(CDFragment fragment) {
    collectReference(fragment);

    for (CDAtom node : fragment.getAtoms()) {
      collectNode(node);
    }
    for (CDBond bond : fragment.getBonds()) {
      collectBond(bond);
    }
    for (CDGraphic graphic : fragment.getGraphics()) {
      collectGraphic(graphic);
    }
    for (CDArrow arrow : fragment.getArrows()) {
      collectArrow(arrow);
    }
    for (CDSpline curve : fragment.getCurves()) {
      collectCurve(curve);
    }
    for (CDObjectTag objectTag : fragment.getObjectTags()) {
      collectObjectTag(objectTag);
    }
    for (CDText text : fragment.getTexts()) {
      collectText(text);
    }
    for (CDColoredMolecularArea area : fragment.getColoredMolecularAreas()) {
      collectColoredMolecularArea(area);
    }
  }

  private void writeFragment(CDFragment fragment) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, fragment);
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_Frag_ConnectionOrder, fragment.getConnectionOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, fragment.getBounds());
    if (fragment.isRacemic()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Mole_Racemic, fragment.isRacemic());
    }
    if (fragment.isAbsolute()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Mole_Absolute, fragment.isAbsolute());
    }
    if (fragment.isRelative()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Mole_Relative, fragment.isRelative());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Mole_Formula, fragment.getFormula());
    if (fragment.getWeight() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Mole_Weight, fragment.getWeight());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Frag_SequenceType, fragment.getSequenceType());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Fragment,
        CDXMLConstants.CDXMLObj_Fragment,
        attributes);

    for (CDAtom node : fragment.getAtoms()) {
      writeNode(node);
    }
    for (CDBond bond : fragment.getBonds()) {
      writeBond(bond);
    }
    for (CDGraphic graphic : fragment.getGraphics()) {
      writeGraphic(graphic);
    }
    for (CDArrow arrow : fragment.getArrows()) {
      writeArrow(arrow);
    }
    for (CDSpline curve : fragment.getCurves()) {
      writeCurve(curve);
    }
    for (CDObjectTag objectTag : fragment.getObjectTags()) {
      writeObjectTag(objectTag);
    }
    for (CDText text : fragment.getTexts()) {
      writeText(text);
    }
    for (CDColoredMolecularArea area : fragment.getColoredMolecularAreas()) {
      writeColoredMolecularArea(area);
    }
    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Fragment, CDXMLConstants.CDXMLObj_Fragment);
  }

  private void collectText(CDText text) {
    collectReference(text);
    collectColor(text.getColor());
    collectFont(text.getSettings().getLabelFont());
    collectColor(text.getSettings().getLabelColor());
    collectFont(text.getSettings().getCaptionFont());
    collectColor(text.getSettings().getCaptionColor());

    if (text.getText() != null) {
      collectStyledString(text.getText());
    }

    for (CDObjectTag objectTag : text.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeText(CDText text) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, text);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_2DPosition, text.getPosition2D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, text.getColor());
    if (text.getAngle() != 0.0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_RotationAngle, (int) (text.getAngle() * 65536.0f));
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, text.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, text.getBounds());
    if (text.getJustification() != CDJustification.Left) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Justification, text.getJustification());
    }
    addLineHeightAttribute(attributes, CDXMLConstants.CDXMLProp_LineHeight, text.getLineHeight());
    if (text.getWrapWidth() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_WordWrapWidth, text.getWrapWidth());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_LineStarts, text.getLineStarts());
    if (text.getLabelAlignment() != CDLabelDisplay.Auto) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_LabelAlignment, text.getLabelAlignment());
    }
    if (text.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, text.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, text.getChemicalWarning());
    if (!text.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, text.isVisible());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, text.getSettings().getLabelFont());
    if (text.getSettings().getLabelSize() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, text.getSettings().getLabelSize());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, text.getSettings().getLabelFace());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleColor, text.getSettings().getLabelColor());
    if (text.getSettings().getLabelJustification() != CDJustification.Left) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_LabelJustification,
          text.getSettings().getLabelJustification());
    }
    addLineHeightAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_LabelLineHeight,
        text.getSettings().getLabelLineHeight());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CaptionStyleFont, text.getSettings().getCaptionFont());
    if (text.getSettings().getCaptionSize() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionStyleSize,
          text.getSettings().getCaptionSize());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CaptionStyleFace, text.getSettings().getCaptionFace());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleColor,
        text.getSettings().getCaptionColor());
    if (text.getSettings().getCaptionLineHeight() != CDSettings.LineHeight_Automatic) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionLineHeight,
          text.getSettings().getCaptionLineHeight());
    }
    if (text.getSettings().getCaptionJustification() != CDJustification.Left) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionJustification,
          text.getSettings().getCaptionJustification());
    }
    if (!text.getSettings().isInterpretChemically()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_InterpretChemically,
          text.getSettings().isInterpretChemically());
    }

    handler.startElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Text, CDXMLConstants.CDXMLObj_Text, attributes);

    if (text.getText() != null) {
      writeStyledString(text.getText());
    }

    for (CDObjectTag objectTag : text.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Text, CDXMLConstants.CDXMLObj_Text);
  }

  private void collectStyledString(CDStyledString text) {
    for (CDStyledString.CDXChunk chunk : text.getChunks()) {
      collectFont(chunk.getFont());
      collectColor(chunk.getColor());
    }
  }

  private void writeStyledString(CDStyledString text) throws SAXException, IOException {
    for (CDStyledString.CDXChunk chunk : text.getChunks()) {
      AttributesImpl attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Font, chunk.getFont());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_FontSize, chunk.getFontSize());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_FontFace, chunk.getFontType());
      addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, chunk.getColor());

      handler.startElement(
          CDXMLConstants.NS,
          CDXMLConstants.CDXMLObj_String,
          CDXMLConstants.CDXMLObj_String,
          attributes);

      String string = chunk.getText();
      handler.characters(string.toCharArray(), 0, string.length());

      handler.endElement(
          CDXMLConstants.NS, CDXMLConstants.CDXMLObj_String, CDXMLConstants.CDXMLObj_String);
    }
  }

  private void writeColoredMolecularArea(CDColoredMolecularArea area)
      throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, area);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BackgroundColor, area.getBackgroundColor());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_BasisObjects, area.getBasisObjects());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ColoredMolecularArea,
        CDXMLConstants.CDXMLObj_ColoredMolecularArea,
        attributes);
    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ColoredMolecularArea,
        CDXMLConstants.CDXMLObj_ColoredMolecularArea);
  }

  private void collectColoredMolecularArea(CDColoredMolecularArea area) {
    collectReference(area);
    collectColor(area.getBackgroundColor());
  }

  private void collectNode(CDAtom node) {
    collectReference(node);
    collectColor(node.getColor());
    collectColor(node.getSettings().getBackgroundColor());
    collectColor(node.getSettings().getHighlightColor());
    collectFont(node.getSettings().getLabelFont());

    if (node.getText() != null) {
      collectText(node.getText());
    }
    for (CDFragment fragment : node.getFragments()) {
      collectFragment(fragment);
    }
    for (CDObjectTag objectTag : node.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeNode(CDAtom node) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, node);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_2DPosition, node.getPosition2D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_3DPosition, node.getPosition3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, node.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        node.getSettings().getBackgroundColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_HighlightColor,
        node.getSettings().getHighlightColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, node.getZOrder());
    if (node.getElementNumber() != 6) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Node_Element, node.getElementNumber());
    }
    if (node.getLabelDisplay() != CDLabelDisplay.Auto) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Node_LabelDisplay, node.getLabelDisplay());
    }
    if (node.getNodeType() != CDNodeType.Element) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Node_Type, node.getNodeType());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_ElementList, node.getElementList());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_GenericList, node.getGenericList());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Formula, node.getFormula());
    if (node.getIsotope() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Isotope, node.getIsotope());
    }
    if (node.getCharge() != 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Charge, node.getCharge());
    }
    if (node.getRadical() != CDRadical.None) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Radical, node.getRadical());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.FreeSites
        && node.getSubstituentCount() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_RestrictFreeSites, node.getSubstituentCount());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.SubstituentsUpTo
        && node.getSubstituentCount() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_RestrictSubstituentsUpTo,
          node.getSubstituentCount());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.SubstituentsExactly
        && node.getSubstituentCount() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_RestrictSubstituentsExactly,
          node.getSubstituentCount());
    }
    if (node.isImplicitHydrogensAllowed()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_RestrictImplicitHydrogens,
          node.isImplicitHydrogensAllowed());
    }
    if (node.getRingBondCount() != CDRingBondCount.Unspecified) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_RestrictRingBondCount, node.getRingBondCount());
    }
    if (node.getUnsaturatedBonds() != CDUnsaturation.Unspecified) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_RestrictUnsaturatedBonds,
          node.getUnsaturatedBonds());
    }
    if (node.isRestrictReactionChange()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_RestrictRxnChange,
          node.isRestrictReactionChange());
    }
    if (node.getReactionStereo() != CDReactionStereo.Unspecified) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_RestrictRxnStereo, node.getReactionStereo());
    }
    if (node.getTranslation() != CDTranslation.Equal) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Translation, node.getTranslation());
    }
    if (node.getIsotopicAbundance() != CDIsotopicAbundance.Unspecified) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_IsotopicAbundance, node.getIsotopicAbundance());
    }
    if (node.getAttachmentPointType() != CDExternalConnectionType.Unspecified) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ExternalConnectionType,
          node.getAttachmentPointType());
    }
    if (node.isAbnormalValenceAllowed()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_AbnormalValence,
          node.isAbnormalValenceAllowed());
    }
    if (node.getNumImplicitHydrogens() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_NumHydrogens, node.getNumImplicitHydrogens());
    }
    if (node.isHDot()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_HDot, node.isHDot());
    }
    if (node.isHDash()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_HDash, node.isHDash());
    }
    if (node.getAtomGeometry() != CDAtomGeometry.Unknown) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_Geometry, node.getAtomGeometry());
    }
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_Atom_BondOrdering, node.getBondOrdering());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_Node_Attachments, node.getAttachedAtoms());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_GenericNickname, node.getLabelText());
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_AltGroupID, node.getAltGroup());
    if (node.getStereochemistry() != CDAtomCIPType.Undetermined) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_CIPStereochemistry, node.getStereochemistry());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_AtomNumber, node.getAtomNumber());
    if (node.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, node.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, node.getChemicalWarning());
    if (!node.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, node.isVisible());
    }
    if (!node.getSettings().isShowAtomQuery()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowQuery,
          node.getSettings().isShowAtomQuery());
    }
    if (node.getSettings().isShowAtomStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowStereo,
          node.getSettings().isShowAtomStereo());
    }
    if (!node.getSettings().isShowAtomEnhancedStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowEnhancedStereo,
          node.getSettings().isShowAtomEnhancedStereo());
    }
    if (node.getSettings().isShowAtomNumber()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Atom_ShowAtomNumber,
          node.getSettings().isShowAtomNumber());
    }
    if (node.getSettings().isShowTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ShowTerminalCarbonLabels,
          node.getSettings().isShowTerminalCarbonLabels());
    }
    if (node.getSettings().isShowNonTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ShowNonTerminalCarbonLabels,
          node.getSettings().isShowNonTerminalCarbonLabels());
    }
    if (node.getSettings().isHideImplicitHydrogens()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_HideImplicitHydrogens,
          node.getSettings().isHideImplicitHydrogens());
    }
    if (node.getSettings().getLineWidth() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LineWidth, node.getSettings().getLineWidth());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, node.getSettings().getLabelFont());
    if (node.getSettings().getLabelSize() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, node.getSettings().getLabelSize());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, node.getSettings().getLabelFace());
    if (node.getLinkCountLow() > 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Atom_LinkCountLow, node.getLinkCountLow());
    }
    if (node.getLinkCountHigh() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Atom_LinkCountHigh, node.getLinkCountHigh());
    }

    handler.startElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Node, CDXMLConstants.CDXMLObj_Node, attributes);

    for (CDFragment fragment : node.getFragments()) {
      writeFragment(fragment);
    }
    if (node.getText() != null) {
      writeText(node.getText());
    }
    for (CDObjectTag objectTag : node.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Node, CDXMLConstants.CDXMLObj_Node);
  }

  private void collectBond(CDBond bond) {
    collectReference(bond);
    collectColor(bond.getColor());
    collectColor(bond.getSettings().getBackgroundColor());
    collectColor(bond.getSettings().getHighlightColor());
    collectFont(bond.getSettings().getLabelFont());

    for (CDObjectTag objectTag : bond.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeBond(CDBond bond) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, bond);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, bond.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        bond.getSettings().getBackgroundColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_HighlightColor,
        bond.getSettings().getHighlightColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, bond.getZOrder());
    addBondOrderAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_Order, bond.getBondOrder());
    if (bond.getBondDisplay() != CDBondDisplay.Solid) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_Display, bond.getBondDisplay());
    }
    if (bond.getBondDisplay2() != CDBondDisplay.Solid) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_Display2, bond.getBondDisplay2());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bond_DoublePosition, bond.getBondDoublePosition());
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_Begin, bond.getBegin());
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_End, bond.getEnd());
    if (bond.getTopology() != CDBondTopology.Unspecified) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_RestrictTopology, bond.getTopology());
    }
    if (bond.getReactionParticipation() != CDBondReactionParticipation.Unspecified) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_RestrictRxnParticipation,
          bond.getReactionParticipation());
    }
    if (bond.getBeginAttach() >= 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_BeginAttach, bond.getBeginAttach());
    }
    if (bond.getEndAttach() >= 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Bond_EndAttach, bond.getEndAttach());
    }
    if (bond.getStereochemistry() != CDBondCIPType.Undetermined) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Bond_CIPStereochemistry, bond.getStereochemistry());
    }
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bond_BondOrdering, bond.getBondCircularOrdering());
    if (bond.getCrossingBonds() != null) {
      addReferenceListAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_CrossingBonds,
          new ArrayList<CDBond>(bond.getCrossingBonds()));
    }
    if (bond.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, bond.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, bond.getChemicalWarning());
    if (!bond.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, bond.isVisible());
    }
    if (!bond.getSettings().isShowBondQuery()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowQuery,
          bond.getSettings().isShowBondQuery());
    }
    if (bond.getSettings().isShowBondStereo()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowStereo,
          bond.getSettings().isShowBondStereo());
    }
    if (!bond.getSettings().isShowBondReaction()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bond_ShowRxn,
          bond.getSettings().isShowBondReaction());
    }
    if (bond.getSettings().getBondSpacing() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_BondSpacing, bond.getSettings().getBondSpacing());
    }
    if (bond.getSettings().getBondSpacingAbs() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_BondSpacingAbs,
          bond.getSettings().getBondSpacingAbs());
    }
    if (bond.getSettings().getBondLength() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_BondLength, bond.getSettings().getBondLength());
    }
    if (bond.getSettings().getBoldWidth() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_BoldWidth, bond.getSettings().getBoldWidth());
    }
    if (bond.getSettings().getLineWidth() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LineWidth, bond.getSettings().getLineWidth());
    }
    if (bond.getSettings().getMarginWidth() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_MarginWidth, bond.getSettings().getMarginWidth());
    }
    if (bond.getSettings().getHashSpacing() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_HashSpacing, bond.getSettings().getHashSpacing());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, bond.getSettings().getLabelFont());
    if (bond.getSettings().getLabelSize() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, bond.getSettings().getLabelSize());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, bond.getSettings().getLabelFace());

    handler.startElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Bond, CDXMLConstants.CDXMLObj_Bond, attributes);

    for (CDObjectTag objectTag : bond.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Bond, CDXMLConstants.CDXMLObj_Bond);
  }

  private void collectGraphic(CDGraphic graphic) {
    collectReference(graphic);
    collectColor(graphic.getColor());
    collectColor(graphic.getSettings().getBackgroundColor());
    collectFont(graphic.getSettings().getCaptionFont());

    for (CDObjectTag objectTag : graphic.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeGraphic(CDGraphic graphic) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, graphic);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, graphic.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        graphic.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, graphic.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, graphic.getBounds());
    if (graphic.getGraphicType() != CDGraphicType.Undefined) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Graphic_Type, graphic.getGraphicType());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Line_Type, graphic.getLineType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Arrow_Type, graphic.getArrowType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Bracket_Type, graphic.getBracketType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Rectangle_Type, graphic.getRectangleType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Oval_Type, graphic.getOvalType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Orbital_Type, graphic.getOrbitalType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Symbol_Type, graphic.getSymbolType());
    if (graphic.getArrowHeadSize() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Arrow_HeadSize, graphic.getArrowHeadSize() * 100f);
    }
    if (graphic.getBracketLipSize() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Bracket_LipSize, graphic.getBracketLipSize());
    }
    if (graphic.getArcAngularSize() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Arc_AngularSize, graphic.getArcAngularSize());
    }
    if (graphic.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, graphic.isIgnoreWarnings());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, graphic.getChemicalWarning());
    if (!graphic.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, graphic.isVisible());
    }
    if (graphic.getSettings().getBoldWidth() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_BoldWidth, graphic.getSettings().getBoldWidth());
    }
    if (graphic.getSettings().getLineWidth() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LineWidth, graphic.getSettings().getLineWidth());
    }
    if (graphic.getSettings().getHashSpacing() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_HashSpacing, graphic.getSettings().getHashSpacing());
    }
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFont,
        graphic.getSettings().getCaptionFont());
    if (graphic.getSettings().getCaptionSize() != 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionStyleSize,
          graphic.getSettings().getCaptionSize());
    }
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFace,
        graphic.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BracketUsage, graphic.getBracketUsage());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_Polymer_RepeatPattern,
        graphic.getPolymerRepeatPattern());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Polymer_FlipType, graphic.getPolymerFlipType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Head3D, graphic.getHead3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Tail3D, graphic.getTail3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Center3D, graphic.getTail3D());

    addAttribute(attributes, CDXMLConstants.CDXMLProp_MajorAxisEnd3D, graphic.getMajorAxisEnd3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_MinorAxisEnd3D, graphic.getMinorAxisEnd3D());

    addAttribute(attributes, CDXMLConstants.CDXMLProp_Curve_FillType, graphic.getFillType());
    if (graphic.getShadowSize() != 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_ShadowSize, graphic.getShadowSize());
    }
    if (graphic.getCornerRadius() != 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_CornerRadius, graphic.getCornerRadius());
    }
    if (graphic.getFadePercent() != 100) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_FadePercent, graphic.getFadePercent());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Graphic,
        CDXMLConstants.CDXMLObj_Graphic,
        attributes);

    for (CDObjectTag objectTag : graphic.getObjectTags()) {
      writeObjectTag(objectTag);
    }
    writeRepresents(graphic.getRepresents());

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Graphic, CDXMLConstants.CDXMLObj_Graphic);
  }

  private void collectArrow(CDArrow arrow) {
    collectReference(arrow);
    collectColor(arrow.getColor());
    collectColor(arrow.getSettings().getBackgroundColor());
    collectFont(arrow.getSettings().getCaptionFont());

    for (CDObjectTag objectTag : arrow.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeArrow(CDArrow arrow) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, arrow);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, arrow.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        arrow.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, arrow.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, arrow.getBounds());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Line_Type, arrow.getLineType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Arrow_HeadSize, arrow.getHeadSize() * 100f);
    if (arrow.getAngularSize() != 0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Arc_AngularSize, arrow.getAngularSize());
    }
    if (arrow.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, arrow.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, arrow.getChemicalWarning());
    if (!arrow.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, arrow.isVisible());
    }
    if (arrow.getSettings().getBoldWidth() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_BoldWidth, arrow.getSettings().getBoldWidth());
    }
    if (arrow.getSettings().getLineWidth() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LineWidth, arrow.getSettings().getLineWidth());
    }
    if (arrow.getSettings().getHashSpacing() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_HashSpacing, arrow.getSettings().getHashSpacing());
    }
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFont,
        arrow.getSettings().getCaptionFont());
    if (arrow.getSettings().getCaptionSize() != 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CaptionStyleSize,
          arrow.getSettings().getCaptionSize());
    }
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CaptionStyleFace,
        arrow.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Head3D, arrow.getHead3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Tail3D, arrow.getTail3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Center3D, arrow.getCenter3D());

    addAttribute(attributes, CDXMLConstants.CDXMLProp_MajorAxisEnd3D, arrow.getMajorAxisEnd3D());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_MinorAxisEnd3D, arrow.getMinorAxisEnd3D());

    // arrow
    if (arrow.getHeadWidth() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ArrowHeadWidth, arrow.getHeadWidth() * 100f);
    }
    if (arrow.getHeadCenterSize() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ArrowHeadCenterSize,
          arrow.getHeadCenterSize() * 100f);
    }
    if (arrow.getEquilibriumRatio() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ArrowEquilibriumRatio,
          arrow.getEquilibriumRatio() * 100f);
    }
    if (arrow.isDipole()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Dipole, arrow.isDipole());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ArrowHeadType, arrow.getArrowHeadType());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ArrowHeadHead, arrow.getArrowHeadPositionStart());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ArrowHeadTail, arrow.getArrowHeadPositionTail());
    if (arrow.getShaftSpacing() > 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ArrowShaftSpacing, arrow.getShaftSpacing() * 100f);
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_NoGo, arrow.getNoGoType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Curve_FillType, arrow.getFillType());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Arrow,
        CDXMLConstants.CDXMLObj_Arrow,
        attributes);

    for (CDObjectTag objectTag : arrow.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Arrow, CDXMLConstants.CDXMLObj_Arrow);
  }

  private void writeRepresents(Map<String, Object> represents) throws SAXException, IOException {
    for (Entry<String, Object> represent : represents.entrySet()) {
      AttributesImpl attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Attribute, represent.getKey());
      addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Object, represent.getValue());

      handler.startElement(
          CDXMLConstants.NS,
          CDXMLConstants.CDXMLObj_Represent,
          CDXMLConstants.CDXMLObj_Represent,
          attributes);

      handler.endElement(
          CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Represent, CDXMLConstants.CDXMLObj_Represent);
    }
  }

  private void collectCurve(CDSpline curve) {
    collectReference(curve);
    collectColor(curve.getColor());
    collectColor(curve.getSettings().getBackgroundColor());

    for (CDObjectTag objectTag : curve.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeCurve(CDSpline curve) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, curve);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, curve.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        curve.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, curve.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, curve.getBounds());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Curve_FillType, curve.getFillType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Line_Type, curve.getLineType());
    addPoint2DListAttribute(attributes, CDXMLConstants.CDXMLProp_Curve_Points, curve.getPoints2D());
    addPoint3DListAttribute(
        attributes, CDXMLConstants.CDXMLProp_Curve_Points3D, curve.getPoints3D());
    if (curve.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, curve.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, curve.getChemicalWarning());
    if (!curve.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, curve.isVisible());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ArrowHeadType, curve.getArrowHeadType());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ArrowHeadHead, curve.getArrowHeadPositionAtStart());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ArrowHeadTail, curve.getArrowHeadPositionAtStart());

    if (curve.isClosed()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Closed, curve.isClosed());
    }
    if (curve.getSettings().getLineWidth() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_LineWidth, curve.getSettings().getLineWidth());
    }
    if (curve.getSettings().getHashSpacing() != 0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_HashSpacing, curve.getSettings().getHashSpacing());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Curve,
        CDXMLConstants.CDXMLObj_Curve,
        attributes);

    for (CDObjectTag objectTag : curve.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Curve, CDXMLConstants.CDXMLObj_Curve);
  }

  private void collectNamedAlternativeGroup(CDAltGroup altGroup) {
    collectReference(altGroup);
    collectColor(altGroup.getColor());
    collectColor(altGroup.getSettings().getBackgroundColor());

    for (CDObjectTag objectTag : altGroup.getObjectTags()) {
      collectObjectTag(objectTag);
    }
    for (CDText text : altGroup.getCaptions()) {
      collectText(text);
    }
    for (CDFragment fragment : altGroup.getFragments()) {
      collectFragment(fragment);
    }
    for (CDGroup group : altGroup.getGroups()) {
      collectGroup(group);
    }
  }

  private void writeNamedAlternativeGroup(CDAltGroup altGroup) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, altGroup);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, altGroup.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        altGroup.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, altGroup.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, altGroup.getBounds());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_NamedAlternativeGroup_TextFrame,
        altGroup.getTextFrame());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_NamedAlternativeGroup_GroupFrame,
        altGroup.getGroupFrame());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_NamedAlternativeGroup_Valence, altGroup.getValence());
    if (altGroup.isIgnoreWarnings()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, altGroup.isIgnoreWarnings());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, altGroup.getChemicalWarning());
    if (!altGroup.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, altGroup.isVisible());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Curve,
        CDXMLConstants.CDXMLObj_Curve,
        attributes);

    for (CDObjectTag objectTag : altGroup.getObjectTags()) {
      writeObjectTag(objectTag);
    }
    for (CDText text : altGroup.getCaptions()) {
      writeText(text);
    }
    for (CDFragment fragment : altGroup.getFragments()) {
      writeFragment(fragment);
    }
    for (CDGroup group : altGroup.getGroups()) {
      writeGroup(group);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Curve, CDXMLConstants.CDXMLObj_Curve);
  }

  private void collectReactionStep(CDReactionStep reactionStep) {
    collectReference(reactionStep);
  }

  private void writeReactionStep(CDReactionStep reactionStep) throws SAXException, IOException {
    if (reactionStep == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, reactionStep);
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_ReactionStep_Reactants, reactionStep.getReactants());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_ReactionStep_Products, reactionStep.getProducts());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_ReactionStep_Plusses, reactionStep.getPlusses());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_ReactionStep_Arrows, reactionStep.getArrows());
    addReferenceListAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ReactionStep_ObjectsAboveArrow,
        reactionStep.getObjectsAboveArrow());
    addReferenceListAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ReactionStep_ObjectsBelowArrow,
        reactionStep.getObjectsBelowArrow());
    addReferenceMapAttribute(
        attributes, CDXMLConstants.CDXMLProp_ReactionStep_Atom_Map, reactionStep.getAtomMap());
    addReferenceMapAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ReactionStep_Atom_Map_Manual,
        reactionStep.getAtomMapManual());
    addReferenceMapAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ReactionStep_Atom_Map_Auto,
        reactionStep.getAtomMapAuto());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ReactionStep,
        CDXMLConstants.CDXMLObj_ReactionStep,
        attributes);

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ReactionStep,
        CDXMLConstants.CDXMLObj_ReactionStep);
  }

  private void collectReactionScheme(CDReactionScheme reactionScheme) {
    collectReference(reactionScheme);

    for (CDReactionStep reactionStep : reactionScheme.getSteps()) {
      collectReactionStep(reactionStep);
    }
  }

  private void writeReactionScheme(CDReactionScheme reactionScheme)
      throws SAXException, IOException {
    if (reactionScheme == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, reactionScheme);

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ReactionScheme,
        CDXMLConstants.CDXMLObj_ReactionScheme,
        attributes);

    for (CDReactionStep reactionStep : reactionScheme.getSteps()) {
      writeReactionStep(reactionStep);
    }

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ReactionScheme,
        CDXMLConstants.CDXMLObj_ReactionScheme);
  }

  private void collectGeometry(CDGeometry geometry) {
    collectReference(geometry);
    collectColor(geometry.getColor());
  }

  private void writeGeometry(CDGeometry geometry) throws SAXException, IOException {
    if (geometry == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, geometry);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, geometry.getColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Name, geometry.getName());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, geometry.getSettings().getLineWidth());
    if (geometry.getGeometricType() != CDGeometryType.Undefined) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_GeometricFeature, geometry.getGeometricType());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_RelationValue, geometry.getRelationValue());
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_BasisObjects, geometry.getBasisObjects());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Geometry,
        CDXMLConstants.CDXMLObj_Geometry,
        attributes);

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Geometry, CDXMLConstants.CDXMLObj_Geometry);
  }

  private void collectConstraint(CDConstraint constraint) {
    collectReference(constraint);
    collectColor(constraint.getColor());
  }

  private void writeConstraint(CDConstraint constraint) throws SAXException, IOException {
    if (constraint == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, constraint);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, constraint.getColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Name, constraint.getName());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, constraint.getSettings().getLineWidth());
    if (constraint.getConstraintType() != CDConstraintType.Undefined) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ConstraintType, constraint.getConstraintType());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ConstraintMin, constraint.getMinRange());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ConstraintMax, constraint.getMaxRange());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_IgnoreUnconnectedAtoms,
        constraint.isIgnoreUnconnectedAtoms());
    if (constraint.isDihedralIsChiral()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_DihedralIsChiral, constraint.isDihedralIsChiral());
    }
    if (constraint.isPointIsDirected()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_PointIsDirected, constraint.isPointIsDirected());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Constraint,
        CDXMLConstants.CDXMLObj_Constraint,
        attributes);

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Constraint, CDXMLConstants.CDXMLObj_Constraint);
  }

  private void collectTemplateGrid(CDTemplateGrid templateGrid) {
    collectReference(templateGrid);
  }

  private void writeTemplateGrid(CDTemplateGrid templateGrid) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLConstants.CDXMLProp_2DExtent, templateGrid.getExtent());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Template_PaneHeight, templateGrid.getPaneHeight());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Template_NumRows, templateGrid.getNumRows());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Template_NumColumns, templateGrid.getNumColumns());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_TemplateGrid,
        CDXMLConstants.CDXMLObj_TemplateGrid,
        attributes);

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_TemplateGrid,
        CDXMLConstants.CDXMLObj_TemplateGrid);
  }

  private void collectSpectrum(CDSpectrum spectrum) {
    collectReference(spectrum);
    collectColor(spectrum.getColor());
    collectColor(spectrum.getSettings().getBackgroundColor());
    collectFont(spectrum.getSettings().getLabelFont());

    for (CDObjectTag objectTag : spectrum.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeSpectrum(CDSpectrum spectrum) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, spectrum);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, spectrum.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        spectrum.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, spectrum.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, spectrum.getBounds());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_XSpacing, spectrum.getXSpacing());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_XLow, spectrum.getXLow());
    if (spectrum.getXType() != CDSpectrumXType.Unknown) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_XType, spectrum.getXType());
    }
    if (spectrum.getYType() != CDSpectrumYType.Unknown) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_YType, spectrum.getYType());
    }
    if (spectrum.getSpectrumClass() != CDSpectrumClass.Unknown) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Spectrum_Class, spectrum.getSpectrumClass());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Spectrum_XAxisLabel, spectrum.getXAxisLabel());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Spectrum_YAxisLabel, spectrum.getYAxisLabel());
    if (spectrum.getYLow() != 0.0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_YLow, spectrum.getYLow());
    }
    if (spectrum.getYScale() != 1.0) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Spectrum_YScale, spectrum.getYScale());
    }
    if (spectrum.isIgnoreWarnings()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_IgnoreWarnings, spectrum.isIgnoreWarnings());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ChemicalWarning, spectrum.getChemicalWarning());
    if (!spectrum.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, spectrum.isVisible());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BoldWidth, spectrum.getSettings().getBoldWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, spectrum.getSettings().getLineWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, spectrum.getSettings().getLabelFont());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, spectrum.getSettings().getLabelSize());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, spectrum.getSettings().getLabelFace());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Spectrum,
        CDXMLConstants.CDXMLObj_Spectrum,
        attributes);

    for (CDObjectTag objectTag : spectrum.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    StringBuilder sb = new StringBuilder();
    for (double value : spectrum.getDataPoint()) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(value);
    }

    handler.characters(sb.toString().toCharArray(), 0, sb.length());

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Spectrum, CDXMLConstants.CDXMLObj_Spectrum);
  }

  private void collectEmbeddedObject(CDPicture embeddedObject) {
    collectReference(embeddedObject);
    collectColor(embeddedObject.getColor());
    collectColor(embeddedObject.getSettings().getBackgroundColor());

    for (CDObjectTag objectTag : embeddedObject.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeEmbeddedObject(CDPicture embeddedObject) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, embeddedObject);
    // SupersededBy CDATA #IMPLIED
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, embeddedObject.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        embeddedObject.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, embeddedObject.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, embeddedObject.getBounds());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_RotationAngle, embeddedObject.getRotationAngle());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Picture_Edition, embeddedObject.getPictureEdition());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_Picture_EditionAlias,
        embeddedObject.getPictureEditionAlias());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_MacPICT, embeddedObject.getMacPICT());

    if (embeddedObject.getEnhancedMetafile() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getEnhancedMetafile());
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CompressedEnhancedMetafile,
          Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_UncompressedEnhancedMetafileSize,
          embeddedObject.getEnhancedMetafile().length);
    }
    if (embeddedObject.getOleObject() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getOleObject());
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CompressedOLEObject,
          Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_UncompressedOLEObjectSize,
          embeddedObject.getOleObject().length);
    }
    if (embeddedObject.getWindowsMetafile() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getWindowsMetafile());
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_CompressedWindowsMetafile,
          Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_UncompressedWindowsMetafileSize,
          embeddedObject.getWindowsMetafile().length);
    }

    addAttribute(attributes, CDXMLConstants.CDXMLProp_GIF, embeddedObject.getGif());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_TIFF, embeddedObject.getTiff());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_PNG, embeddedObject.getPng());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_JPEG, embeddedObject.getJpeg());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BMP, embeddedObject.getBmp());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_EmbeddedObject,
        CDXMLConstants.CDXMLObj_EmbeddedObject,
        attributes);

    for (CDObjectTag objectTag : embeddedObject.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_EmbeddedObject,
        CDXMLConstants.CDXMLObj_EmbeddedObject);
  }

  private void collectObjectTag(CDObjectTag objectTag) {
    collectReference(objectTag);

    for (CDText text : objectTag.getTexts()) {
      collectText(text);
    }
  }

  private void writeObjectTag(CDObjectTag objectTag) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, objectTag);
    if (!objectTag.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, objectTag.isVisible());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ObjectTag_Type, objectTag.getObjectTagType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Name, objectTag.getName());
    if (!objectTag.isTracking()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_ObjectTag_Tracking, objectTag.isTracking());
    }
    if (!objectTag.isPersistent()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ObjectTag_Persistent, objectTag.isPersistent());
    }

    if (objectTag.getValue() instanceof String) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ObjectTag_Value, (String) objectTag.getValue());
    } else if (objectTag.getValue() instanceof Integer) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ObjectTag_Value, (Integer) objectTag.getValue());
    } else if (objectTag.getValue() instanceof Double) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_ObjectTag_Value, (Double) objectTag.getValue());
    }

    if (objectTag.getPositioningType() != CDPositioningType.Auto) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_Positioning, objectTag.getPositioningType());
    }
    if (objectTag.getPositioningAngle() != 0.0) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_PositioningAngle, objectTag.getPositioningAngle());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_PositioningOffset, objectTag.getPositioningOffset());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ObjectTag,
        CDXMLConstants.CDXMLObj_ObjectTag,
        attributes);

    for (CDText text : objectTag.getTexts()) {
      writeText(text);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_ObjectTag, CDXMLConstants.CDXMLObj_ObjectTag);
  }

  private void collectSequence(CDSequence sequence) {
    collectReference(sequence);
  }

  private void writeSequence(CDSequence sequence) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Sequence_Identifier, sequence.getIdentifier());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Sequence,
        CDXMLConstants.CDXMLObj_Sequence,
        attributes);
    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Sequence, CDXMLConstants.CDXMLObj_Sequence);
  }

  private void collectCrossReference(CDCrossReference crossReference) {
    collectReference(crossReference);
  }

  private void writeCrossReference(CDCrossReference crossReference) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CrossReference_Container,
        crossReference.getContainer());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CrossReference_Document, crossReference.getDocument());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_CrossReference_Identifier,
        crossReference.getIdentifier());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_CrossReference_Sequence, crossReference.getSequence());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_CrossReference,
        CDXMLConstants.CDXMLObj_CrossReference,
        attributes);

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_CrossReference,
        CDXMLConstants.CDXMLObj_CrossReference);
  }

  private void collectSplitter(CDSplitter splitter) {
    collectReference(splitter);
  }

  private void writeSplitter(CDSplitter splitter) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLConstants.CDXMLProp_2DPosition, splitter.getPosition2D());
    if (splitter.getPageDefinition() != CDPageDefinition.Undefined) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_PageDefinition, splitter.getPageDefinition());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Splitter,
        CDXMLConstants.CDXMLObj_Splitter,
        attributes);

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Splitter, CDXMLConstants.CDXMLObj_Splitter);
  }

  private void collectTable(CDTable table) {
    collectReference(table);
    collectColor(table.getColor());
    collectColor(table.getSettings().getBackgroundColor());
    collectFont(table.getSettings().getLabelFont());

    for (CDPage page : table.getPages()) {
      collectPage(page);
    }
    for (CDObjectTag objectTag : table.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeTable(CDTable table) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, table);
    if (!table.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, table.isVisible());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, table.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        table.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, table.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, table.getBounds());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BoldWidth, table.getSettings().getBoldWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, table.getSettings().getLineWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, table.getSettings().getLabelFont());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, table.getSettings().getLabelSize());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, table.getSettings().getLabelFace());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_MarginWidth, table.getSettings().getMarginWidth());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Table,
        CDXMLConstants.CDXMLObj_Table,
        attributes);

    for (CDPage page : table.getPages()) {
      writePage(page);
    }
    for (CDObjectTag objectTag : table.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Table, CDXMLConstants.CDXMLObj_Table);
  }

  private void collectTLCPlate(CDTLCPlate tlcPlate) {
    collectReference(tlcPlate);
    collectColor(tlcPlate.getColor());
    collectColor(tlcPlate.getSettings().getBackgroundColor());
    collectFont(tlcPlate.getSettings().getLabelFont());

    for (CDTLCLane tlcLane : tlcPlate.getLanes()) {
      collectTLCLane(tlcLane);
    }
    for (CDObjectTag objectTag : tlcPlate.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeTLCPlate(CDTLCPlate tlcPlate) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, tlcPlate);
    if (!tlcPlate.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, tlcPlate.isVisible());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, tlcPlate.getColor());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BackgroundColor,
        tlcPlate.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ZOrder, tlcPlate.getZOrder());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BoundingBox, tlcPlate.getBounds());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_BoldWidth, tlcPlate.getSettings().getBoldWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LineWidth, tlcPlate.getSettings().getLineWidth());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFont, tlcPlate.getSettings().getLabelFont());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleSize, tlcPlate.getSettings().getLabelSize());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_LabelStyleFace, tlcPlate.getSettings().getLabelFace());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_MarginWidth, tlcPlate.getSettings().getMarginWidth());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_TopLeft, tlcPlate.getTopLeft());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_TopRight, tlcPlate.getTopRight());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BottomRight, tlcPlate.getBottomRight());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_BottomLeft, tlcPlate.getBottomLeft());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_TLC_OriginFraction, tlcPlate.getOriginFraction());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_TLC_SolventFrontFraction,
        tlcPlate.getSolventFrontFraction());
    if (tlcPlate.isShowOrigin()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_TLC_ShowOrigin, tlcPlate.isShowOrigin());
    }
    if (tlcPlate.isShowSolventFront()) {
      addAttribute(
          attributes, CDXMLConstants.CDXMLProp_TLC_ShowSolventFront, tlcPlate.isShowSolventFront());
    }
    if (tlcPlate.isShowBorders()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_TLC_ShowBorders, tlcPlate.isShowBorders());
    }
    if (tlcPlate.isShowSideTicks()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_ShowSideTicks, tlcPlate.isShowSideTicks());
    }
    if (tlcPlate.isTransparent()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Transparent, tlcPlate.isTransparent());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_TLCPlate,
        CDXMLConstants.CDXMLObj_TLCPlate,
        attributes);

    for (CDTLCLane tlcLane : tlcPlate.getLanes()) {
      writeTLCLane(tlcLane);
    }
    for (CDObjectTag objectTag : tlcPlate.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_TLCPlate, CDXMLConstants.CDXMLObj_TLCPlate);
  }

  private void collectTLCLane(CDTLCLane tlcLane) {
    collectReference(tlcLane);

    for (CDTLCSpot tlcSpot : tlcLane.getSpots()) {
      collectTLCSpot(tlcSpot);
    }
    for (CDObjectTag objectTag : tlcLane.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeTLCLane(CDTLCLane tlcLane) throws SAXException, IOException {
    if (tlcLane == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, tlcLane);
    if (!tlcLane.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, tlcLane.isVisible());
    }

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_TLCLane,
        CDXMLConstants.CDXMLObj_TLCLane,
        attributes);

    for (CDTLCSpot tlcSpot : tlcLane.getSpots()) {
      writeTLCSpot(tlcSpot);
    }
    for (CDObjectTag objectTag : tlcLane.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_TLCLane, CDXMLConstants.CDXMLObj_TLCLane);
  }

  private void collectTLCSpot(CDTLCSpot tlcSpot) {
    collectReference(tlcSpot);
    collectColor(tlcSpot.getColor());

    for (CDObjectTag objectTag : tlcSpot.getObjectTags()) {
      collectObjectTag(objectTag);
    }
  }

  private void writeTLCSpot(CDTLCSpot tlcSpot) throws SAXException, IOException {
    if (tlcSpot == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, tlcSpot);
    if (!tlcSpot.isVisible()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_Visible, tlcSpot.isVisible());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_ForegroundColor, tlcSpot.getColor());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Width, tlcSpot.getWidth());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Height, tlcSpot.getHeight());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_TLC_Tail, tlcSpot.getTail());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_TLC_Rf, tlcSpot.getRf());
    if (tlcSpot.isShowRf()) {
      addAttribute(attributes, CDXMLConstants.CDXMLProp_TLC_ShowRf, tlcSpot.isShowRf());
    }
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Curve_Type, tlcSpot.getCurveType());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_TLCSpot,
        CDXMLConstants.CDXMLObj_TLCSpot,
        attributes);

    for (CDObjectTag objectTag : tlcSpot.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_TLCSpot, CDXMLConstants.CDXMLObj_TLCSpot);
  }

  private void collectBracketedGroup(CDBracket bracketedGroup) {
    collectReference(bracketedGroup);

    for (CDBracket bracketedGroup2 : bracketedGroup.getBrackets()) {
      collectBracketedGroup(bracketedGroup2);
    }
    for (CDBracketAttachment bracketAttachment : bracketedGroup.getBracketAttachments()) {
      collectBracketAttachment(bracketAttachment);
    }
  }

  private void writeBracketedGroup(CDBracket bracketedGroup) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, bracketedGroup);
    addReferenceListAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_BracketedObjects,
        bracketedGroup.getBracketedObjects());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_Usage, bracketedGroup.getBracketUsage());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_Polymer_RepeatPattern,
        bracketedGroup.getPolymerRepeatPattern());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Polymer_FlipType, bracketedGroup.getPolymerFlipType());
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_RepeatCount, bracketedGroup.getRepeatCount());
    if (bracketedGroup.getComponentOrder() > 0) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_Bracket_ComponentOrder,
          bracketedGroup.getComponentOrder());
    }
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_SRULabel, bracketedGroup.getSRULabel());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_BracketedGroup,
        CDXMLConstants.CDXMLObj_BracketedGroup,
        attributes);

    for (CDBracket bracketedGroup2 : bracketedGroup.getBrackets()) {
      writeBracketedGroup(bracketedGroup2);
    }
    for (CDBracketAttachment bracketAttachment : bracketedGroup.getBracketAttachments()) {
      writeBracketAttachment(bracketAttachment);
    }

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_BracketedGroup,
        CDXMLConstants.CDXMLObj_BracketedGroup);
  }

  private void collectBracketAttachment(CDBracketAttachment bracketAttachment) {
    collectReference(bracketAttachment);

    for (CDCrossingBond crossingBond : bracketAttachment.getCrossingBonds()) {
      collectCrossingBond(crossingBond);
    }
  }

  private void writeBracketAttachment(CDBracketAttachment bracketAttachment)
      throws SAXException, IOException {
    if (bracketAttachment == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, bracketAttachment);
    addReferenceAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_GraphicID, bracketAttachment.getGraphic());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_BracketAttachment,
        CDXMLConstants.CDXMLObj_BracketAttachment,
        attributes);

    for (CDCrossingBond crossingBond : bracketAttachment.getCrossingBonds()) {
      writeCrossingBond(crossingBond);
    }

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_BracketAttachment,
        CDXMLConstants.CDXMLObj_BracketAttachment);
  }

  private void collectCrossingBond(CDCrossingBond crossingBond) {
    collectReference(crossingBond);
  }

  private void writeCrossingBond(CDCrossingBond crossingBond) throws SAXException, IOException {
    if (crossingBond == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, crossingBond);
    addReferenceAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_BondID, crossingBond.getBond());
    addReferenceAttribute(
        attributes, CDXMLConstants.CDXMLProp_Bracket_InnerAtomID, crossingBond.getInnerAtom());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_CrossingBond,
        CDXMLConstants.CDXMLObj_CrossingBond,
        attributes);

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_CrossingBond,
        CDXMLConstants.CDXMLObj_CrossingBond);
  }

  private void collectBorder(CDBorder border) {
    collectReference(border);
    collectColor(border.getForegroundColor());
  }

  private void writeBorder(CDBorder border) throws SAXException, IOException {
    if (border == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, border);
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Line_Type, border.getLineType());
    addAttribute(attributes, CDXMLConstants.CDXMLProp_LineWidth, border.getWidth());
    addAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ForegroundColor,
        border.getForegroundColor()); // TODO rename
    addAttribute(attributes, CDXMLConstants.CDXMLProp_Side, border.getSide());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_Border,
        CDXMLConstants.CDXMLObj_Border,
        attributes);

    handler.endElement(
        CDXMLConstants.NS, CDXMLConstants.CDXMLObj_Border, CDXMLConstants.CDXMLObj_Border);
  }

  private void collectChemicalProperty(CDChemicalProperty chemicalProperty) {
    collectReference(chemicalProperty);
  }

  private void writeChemicalProperty(CDChemicalProperty chemicalProperty)
      throws SAXException, IOException {
    if (chemicalProperty == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLConstants.CDXMLProp_Id, chemicalProperty);
    addAttribute(
        attributes, CDXMLConstants.CDXMLProp_ChemicalPropertyType, chemicalProperty.getType());
    addReferenceAttribute(
        attributes,
        CDXMLConstants.CDXMLProp_ChemicalPropertyDisplayID,
        chemicalProperty.getDisplay());
    if (chemicalProperty.isActive()) {
      addAttribute(
          attributes,
          CDXMLConstants.CDXMLProp_ChemicalPropertyIsActive,
          chemicalProperty.isActive());
    }
    addReferenceListAttribute(
        attributes, CDXMLConstants.CDXMLProp_BasisObjects, chemicalProperty.getBasisObjects());

    handler.startElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ChemicalProperty,
        CDXMLConstants.CDXMLObj_ChemicalProperty,
        attributes);

    handler.endElement(
        CDXMLConstants.NS,
        CDXMLConstants.CDXMLObj_ChemicalProperty,
        CDXMLConstants.CDXMLObj_ChemicalProperty);
  }

  // #######################################################
  // #######################################################

  private void addAttribute(AttributesImpl attributes, String name, boolean value) {
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, value ? "yes" : "no");
  }

  private void addAttribute(AttributesImpl attributes, String name, int value) {
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDElementList value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertElementListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGenericList value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertGenericListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, List<Integer> value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertIntListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, float value) {
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, double value) {
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPoint2D value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPoint2DToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPoint3D value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPoint3DToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRectangle value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertRectangleToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, byte[] value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertByteArrayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, String value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, value);
  }

  // CDXML date serialization is intentionally a no-op for parity with current behavior;
  // route through the String overload so the parameters are referenced.
  private void addAttribute(AttributesImpl attributes, String name, Date value) {
    if (value == null) {
      return;
    }
    addAttribute(attributes, name, (String) null);
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFont value)
      throws IOException {
    if (value == null) {
      return;
    }
    if (fonts.get(value) == null) {
      throw new IOException("Font wasn't collected in the first place");
    }
    attributes.addAttribute("", name, name, CDXMLConstants.CDATA, String.valueOf(fonts.get(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDCharSet value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        String.valueOf(CDXMLUtils.convertCharSetToString(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDColor value)
      throws IOException {
    if (value == null) {
      return;
    }
    if (colors.get(value) == null) {
      throw new IOException("Color wasn't collected in the first place");
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, String.valueOf(colors.get(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFontFace value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, String.valueOf(CDXUtils.convertFontType(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDJustification value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertTextJustificationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDDrawingSpaceType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertDrawingSpaceTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPageDefinition value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPageDefinitionToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDLabelDisplay value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertLabelDisplayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDNodeType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertNodeTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRadical value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertRadicalToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRingBondCount value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertRingBondCountToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDUnsaturation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertUnsaturationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDReactionStereo value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertReactionStereoToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDTranslation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertTranslationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDIsotopicAbundance value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertAbundanceToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDExternalConnectionType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        CDXMLUtils.convertExternalConnectionTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDAtomGeometry value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertAtomGeometryToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDAtomCIPType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertAtomCIPTypeToString(value));
  }

  private void addBondOrderAttribute(AttributesImpl attributes, String name, CDBondOrder value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBondOrderToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondDisplay value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBondDisplayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondDoublePosition value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBondDoublePositionToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondTopology value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBondTopologyToString(value));
  }

  private void addAttribute(
      AttributesImpl attributes, String name, CDBondReactionParticipation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        CDXMLUtils.convertBondReactionParticipationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondCIPType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBondCIPTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGraphicType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertGraphicTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDLineType value) {
    if (value == null || value.isSolid()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertLineTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowType value)
      throws IOException {
    if (value == null || value == CDArrowType.NoHead) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertArrowTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBracketType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBracketTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRectangleType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertRectangleTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDOvalType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertOvalTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDOrbitalType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertOrbitalTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSymbolType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSymbolTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBracketUsage value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertBracketUsageToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPolymerRepeatPattern value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        CDXMLUtils.convertPolymerRepeatPatternToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPolymerFlipType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPolymerFlipTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSplineType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        String.valueOf(CDXUtils.convertCurveTypeToInt(value)));
  }

  private void addPoint2DListAttribute(
      AttributesImpl attributes, String name, List<CDPoint2D> value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPoint2DListToString(value));
  }

  private void addPoint3DListAttribute(
      AttributesImpl attributes, String name, List<CDPoint3D> value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPoint3DListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGeometryType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertGeometricFeatureToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDConstraintType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertConstraintTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumXType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSpectrumXTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumYType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSpectrumYTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumClass value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSpectrumClassToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDObjectTagType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertObjectTagTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPositioningType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertPositioningTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSideType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSideTypeToString(value));
  }

  private void addLineHeightAttribute(AttributesImpl attributes, String name, float value) {
    if (value == CDSettings.LineHeight_Variable) {
      // default
      return;
    }

    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertLineHeightToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSequenceType value)
      throws IOException {
    if (value == null || value == CDSequenceType.Unknown) {
      // default
      return;
    }

    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertSequenceTypeToString(value));
  }

  private void addReferenceAttribute(AttributesImpl attributes, String name, Object value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        CDXMLUtils.convertObjectRefToString(value, references));
  }

  private void addReferenceListAttribute(AttributesImpl attributes, String name, List<?> value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertObjectRefList(value, references));
  }

  private void addReferenceMapAttribute(AttributesImpl attributes, String name, Map<?, ?> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    attributes.addAttribute(
        "",
        name,
        name,
        CDXMLConstants.CDATA,
        CDXMLUtils.convertObjectRefMapToString(values, references));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowHeadType value)
      throws IOException {
    if (value == null || value == CDArrowHeadType.Solid) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertArrowheadTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowHeadPositionType value)
      throws IOException {
    if (value == null || value == CDArrowHeadPositionType.Unspecified) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertArrowheadToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFillType value)
      throws IOException {
    if (value == null || value == CDFillType.Unspecified) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertFillTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDNoGoType value)
      throws IOException {
    if (value == null || value == CDNoGoType.Unspecified) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDXMLConstants.CDATA, CDXMLUtils.convertNoGoTypeToString(value));
  }
}
