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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.*;
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
      handler.startDTD(CDXMLObj_Document, null, DTD);
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
    addAttribute(attributes, CDXMLProp_BoundingBox, document.getBoundingBox());
    addAttribute(attributes, CDXMLProp_MacPrintInfo, document.getMacPrintInfo());
    addAttribute(attributes, CDXMLProp_WinPrintInfo, document.getWinPrintInfo());
    addAttribute(attributes, CDXMLProp_PrintMargins, document.getPrintMargins());
    addAttribute(attributes, CDXMLProp_ChainAngle, document.getSettings().getChainAngle());
    addAttribute(attributes, CDXMLProp_BondSpacing, document.getSettings().getBondSpacing());
    if (document.getSettings().getBondSpacingAbs() > 0) {
      addAttribute(
          attributes, CDXMLProp_BondSpacingAbs, document.getSettings().getBondSpacingAbs());
    }
    addAttribute(attributes, CDXMLProp_BondLength, document.getSettings().getBondLength());
    addAttribute(attributes, CDXMLProp_BoldWidth, document.getSettings().getBoldWidth());
    addAttribute(attributes, CDXMLProp_LineWidth, document.getSettings().getLineWidth());
    addAttribute(attributes, CDXMLProp_MarginWidth, document.getSettings().getMarginWidth());
    addAttribute(attributes, CDXMLProp_HashSpacing, document.getSettings().getHashSpacing());
    addAttribute(attributes, CDXMLProp_LabelStyleFont, document.getSettings().getLabelFont());
    addAttribute(attributes, CDXMLProp_LabelStyleSize, document.getSettings().getLabelSize());
    addAttribute(attributes, CDXMLProp_LabelStyleFace, document.getSettings().getLabelFace());
    addAttribute(attributes, CDXMLProp_LabelStyleColor, document.getSettings().getLabelColor());
    addLineHeightAttribute(
        attributes, CDXMLProp_LabelLineHeight, document.getSettings().getLabelLineHeight());
    if (document.getSettings().getLabelJustification() != CDJustification.Left) {
      addAttribute(
          attributes, CDXMLProp_LabelJustification, document.getSettings().getLabelJustification());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFont, document.getSettings().getCaptionFont());
    addAttribute(attributes, CDXMLProp_CaptionStyleSize, document.getSettings().getCaptionSize());
    addAttribute(attributes, CDXMLProp_CaptionStyleFace, document.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLProp_CaptionStyleColor, document.getSettings().getCaptionColor());
    if (document.getSettings().getCaptionLineHeight() != CDSettings.LineHeight_Automatic) {
      addLineHeightAttribute(
          attributes, CDXMLProp_CaptionLineHeight, document.getSettings().getCaptionLineHeight());
    }
    if (document.getSettings().getCaptionJustification() != CDJustification.Left) {
      addAttribute(
          attributes,
          CDXMLProp_CaptionJustification,
          document.getSettings().getCaptionJustification());
    }
    if (document.isFractionalWidths()) {
      addAttribute(attributes, CDXMLProp_FractionalWidths, document.isFractionalWidths());
    }
    if (!document.getSettings().isInterpretChemically()) {
      addAttribute(
          attributes,
          CDXMLProp_InterpretChemically,
          document.getSettings().isInterpretChemically());
    }
    if (!document.getSettings().isShowAtomQuery()) {
      addAttribute(attributes, CDXMLProp_Atom_ShowQuery, document.getSettings().isShowAtomQuery());
    }
    if (document.getSettings().isShowAtomStereo()) {
      addAttribute(
          attributes, CDXMLProp_Atom_ShowStereo, document.getSettings().isShowAtomStereo());
    }
    if (!document.getSettings().isShowAtomEnhancedStereo()) {
      addAttribute(
          attributes,
          CDXMLProp_Atom_ShowEnhancedStereo,
          document.getSettings().isShowAtomEnhancedStereo());
    }
    if (document.getSettings().isShowAtomNumber()) {
      addAttribute(
          attributes, CDXMLProp_Atom_ShowAtomNumber, document.getSettings().isShowAtomNumber());
    }
    if (!document.getSettings().isShowBondQuery()) {
      addAttribute(attributes, CDXMLProp_Bond_ShowQuery, document.getSettings().isShowBondQuery());
    }
    if (document.getSettings().isShowBondStereo()) {
      addAttribute(
          attributes, CDXMLProp_Bond_ShowStereo, document.getSettings().isShowBondStereo());
    }
    if (document.getSettings().isShowBondReaction()) {
      addAttribute(attributes, CDXMLProp_Bond_ShowRxn, document.getSettings().isShowBondReaction());
    }
    if (document.getSettings().isShowTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLProp_ShowTerminalCarbonLabels,
          document.getSettings().isShowTerminalCarbonLabels());
    }
    if (document.getSettings().isShowNonTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLProp_ShowNonTerminalCarbonLabels,
          document.getSettings().isShowNonTerminalCarbonLabels());
    }
    if (document.getSettings().isHideImplicitHydrogens()) {
      addAttribute(
          attributes,
          CDXMLProp_HideImplicitHydrogens,
          document.getSettings().isHideImplicitHydrogens());
    }
    if (document.getMagnification() != 0.0) {
      addAttribute(attributes, CDXMLProp_Magnification, (int) (document.getMagnification() * 10f));
    }
    if (document.isWindowIsZoomed()) {
      addAttribute(attributes, CDXMLProp_Window_IsZoomed, document.isWindowIsZoomed());
    }
    addAttribute(attributes, CDXMLProp_Window_Position, document.getWindowPosition());
    addAttribute(attributes, CDXMLProp_Window_Size, document.getWindowSize());
    addAttribute(attributes, CDXMLProp_CreationUserName, document.getCreationUserName());
    addAttribute(attributes, CDXMLProp_CreationDate, document.getCreationDate());
    addAttribute(attributes, CDXMLProp_CreationProgram, document.getCreationProgram());
    addAttribute(attributes, CDXMLProp_ModificationUserName, document.getModificationUserName());
    addAttribute(attributes, CDXMLProp_ModificationDate, document.getModificationDate());
    addAttribute(attributes, CDXMLProp_ModificationProgram, document.getModificationProgram());
    addAttribute(attributes, CDXMLProp_Name, document.getName());
    addAttribute(attributes, CDXMLProp_Comment, document.getComment());
    addAttribute(attributes, CDXMLProp_CartridgeData, document.getCartridgeData());
    addAttribute(attributes, CDXMLProp_FixInplaceExtent, document.getFixInPlaceExtent());
    addAttribute(attributes, CDXMLProp_FixInplaceGap, document.getFixInPlaceGap());

    handler.startElement(NS, CDXMLObj_Document, CDXMLObj_Document, attributes);

    writeColorTable();
    writeFontTable();

    for (CDPage page : document.getPages()) {
      writePage(page);
    }
    if (document.getTemplateGrid() != null) {
      writeTemplateGrid(document.getTemplateGrid());
    }

    handler.endElement(NS, CDXMLObj_Document, CDXMLObj_Document);
  }

  private void writeColorTable() throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    handler.startElement(NS, CDXMLObj_ColorTable, CDXMLObj_ColorTable, attributes);
    List<Integer> indices = new ArrayList<>(colorsInverse.keySet());
    Collections.sort(indices);
    for (Integer index : indices) {
      if (index < 2) {
        continue;
      }

      CDColor color = colorsInverse.get(index);
      attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLProp_Red, color.getRed());
      addAttribute(attributes, CDXMLProp_Green, color.getGreen());
      addAttribute(attributes, CDXMLProp_Blue, color.getBlue());
      handler.startElement(NS, CDXMLObj_Color, CDXMLObj_Color, attributes);
      handler.endElement(NS, CDXMLObj_Color, CDXMLObj_Color);
    }
    handler.endElement(NS, CDXMLObj_ColorTable, CDXMLObj_ColorTable);
  }

  private void writeFontTable() throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    handler.startElement(NS, CDXMLObj_FontTable, CDXMLObj_FontTable, attributes);
    List<Integer> indices = new ArrayList<>(fontsInverse.keySet());
    Collections.sort(indices);
    for (Integer index : indices) {
      CDFont font = fontsInverse.get(index);

      attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLProp_Id, index);
      addAttribute(attributes, CDXMLProp_CharSet, font.getCharSet());
      addAttribute(attributes, CDXMLProp_Font_Name, font.getName());
      handler.startElement(NS, CDXMLObj_Font, CDXMLObj_Font, attributes);
      handler.endElement(NS, CDXMLObj_Font, CDXMLObj_Font);
    }
    handler.endElement(NS, CDXMLObj_FontTable, CDXMLObj_FontTable);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, page);
    addAttribute(attributes, CDXMLProp_BoundingBox, page.getBounds());
    addAttribute(attributes, CDXMLProp_BoundingBox, page.getBoundsInParent());
    addAttribute(attributes, CDXMLProp_BackgroundColor, page.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_WidthPages, page.getWidthPages());
    addAttribute(attributes, CDXMLProp_HeightPages, page.getHeightPages());
    if (page.isPrintTrimMarks()) {
      addAttribute(attributes, CDXMLProp_PrintTrimMarks, page.isPrintTrimMarks());
    }
    if (page.getWidth() > 0) {
      addAttribute(attributes, CDXMLProp_Width, page.getWidth());
    }
    if (page.getHeight() > 0) {
      addAttribute(attributes, CDXMLProp_Height, page.getHeight());
    }
    if (page.getPageOverlap() != 0) {
      addAttribute(attributes, CDXMLProp_PageOverlap, page.getPageOverlap());
    }
    addAttribute(attributes, CDXMLProp_Header, page.getHeader());
    addAttribute(attributes, CDXMLProp_HeaderPosition, page.getHeaderPosition());
    addAttribute(attributes, CDXMLProp_Footer, page.getFooter());
    addAttribute(attributes, CDXMLProp_FooterPosition, page.getFooterPosition());
    if (page.getDrawingSpaceType() != CDDrawingSpaceType.Pages) {
      addAttribute(attributes, CDXMLProp_DrawingSpaceType, page.getDrawingSpaceType());
    }
    if (page.getPageDefinition() != CDPageDefinition.Undefined) {
      addAttribute(attributes, CDXMLProp_PageDefinition, page.getPageDefinition());
    }

    handler.startElement(NS, CDXMLObj_Page, CDXMLObj_Page, attributes);

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

    handler.endElement(NS, CDXMLObj_Page, CDXMLObj_Page);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, group);
    addAttribute(attributes, CDXMLProp_BoundingBox, group.getBounds());
    if (group.isIntegral()) {
      addAttribute(attributes, CDXMLProp_Group_Integral, group.isIntegral());
    }

    handler.startElement(NS, CDXMLObj_Group, CDXMLObj_Group, attributes);

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

    handler.endElement(NS, CDXMLObj_Group, CDXMLObj_Group);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, fragment);
    addReferenceListAttribute(
        attributes, CDXMLProp_Frag_ConnectionOrder, fragment.getConnectionOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, fragment.getBounds());
    if (fragment.isRacemic()) {
      addAttribute(attributes, CDXMLProp_Mole_Racemic, fragment.isRacemic());
    }
    if (fragment.isAbsolute()) {
      addAttribute(attributes, CDXMLProp_Mole_Absolute, fragment.isAbsolute());
    }
    if (fragment.isRelative()) {
      addAttribute(attributes, CDXMLProp_Mole_Relative, fragment.isRelative());
    }
    addAttribute(attributes, CDXMLProp_Mole_Formula, fragment.getFormula());
    if (fragment.getWeight() > 0) {
      addAttribute(attributes, CDXMLProp_Mole_Weight, fragment.getWeight());
    }
    addAttribute(attributes, CDXMLProp_Frag_SequenceType, fragment.getSequenceType());

    handler.startElement(NS, CDXMLObj_Fragment, CDXMLObj_Fragment, attributes);

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
    handler.endElement(NS, CDXMLObj_Fragment, CDXMLObj_Fragment);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, text);
    addAttribute(attributes, CDXMLProp_2DPosition, text.getPosition2D());
    addAttribute(attributes, CDXMLProp_ForegroundColor, text.getColor());
    if (text.getAngle() != 0.0) {
      addAttribute(attributes, CDXMLProp_RotationAngle, (int) (text.getAngle() * 65536.0f));
    }
    addAttribute(attributes, CDXMLProp_ZOrder, text.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, text.getBounds());
    if (text.getJustification() != CDJustification.Left) {
      addAttribute(attributes, CDXMLProp_Justification, text.getJustification());
    }
    addLineHeightAttribute(attributes, CDXMLProp_LineHeight, text.getLineHeight());
    if (text.getWrapWidth() > 0) {
      addAttribute(attributes, CDXMLProp_WordWrapWidth, text.getWrapWidth());
    }
    addAttribute(attributes, CDXMLProp_LineStarts, text.getLineStarts());
    if (text.getLabelAlignment() != CDLabelDisplay.Auto) {
      addAttribute(attributes, CDXMLProp_LabelAlignment, text.getLabelAlignment());
    }
    if (text.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, text.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, text.getChemicalWarning());
    if (!text.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, text.isVisible());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFont, text.getSettings().getLabelFont());
    if (text.getSettings().getLabelSize() > 0) {
      addAttribute(attributes, CDXMLProp_LabelStyleSize, text.getSettings().getLabelSize());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFace, text.getSettings().getLabelFace());
    addAttribute(attributes, CDXMLProp_LabelStyleColor, text.getSettings().getLabelColor());
    if (text.getSettings().getLabelJustification() != CDJustification.Left) {
      addAttribute(
          attributes, CDXMLProp_LabelJustification, text.getSettings().getLabelJustification());
    }
    addLineHeightAttribute(
        attributes, CDXMLProp_LabelLineHeight, text.getSettings().getLabelLineHeight());
    addAttribute(attributes, CDXMLProp_CaptionStyleFont, text.getSettings().getCaptionFont());
    if (text.getSettings().getCaptionSize() > 0) {
      addAttribute(attributes, CDXMLProp_CaptionStyleSize, text.getSettings().getCaptionSize());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFace, text.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLProp_CaptionStyleColor, text.getSettings().getCaptionColor());
    if (text.getSettings().getCaptionLineHeight() != CDSettings.LineHeight_Automatic) {
      addAttribute(
          attributes, CDXMLProp_CaptionLineHeight, text.getSettings().getCaptionLineHeight());
    }
    if (text.getSettings().getCaptionJustification() != CDJustification.Left) {
      addAttribute(
          attributes, CDXMLProp_CaptionJustification, text.getSettings().getCaptionJustification());
    }
    if (!text.getSettings().isInterpretChemically()) {
      addAttribute(
          attributes, CDXMLProp_InterpretChemically, text.getSettings().isInterpretChemically());
    }

    handler.startElement(NS, CDXMLObj_Text, CDXMLObj_Text, attributes);

    if (text.getText() != null) {
      writeStyledString(text.getText());
    }

    for (CDObjectTag objectTag : text.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Text, CDXMLObj_Text);
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
      addAttribute(attributes, CDXMLProp_Font, chunk.getFont());
      addAttribute(attributes, CDXMLProp_FontSize, chunk.getFontSize());
      addAttribute(attributes, CDXMLProp_FontFace, chunk.getFontType());
      addAttribute(attributes, CDXMLProp_ForegroundColor, chunk.getColor());

      handler.startElement(NS, CDXMLObj_String, CDXMLObj_String, attributes);

      String string = chunk.getText();
      handler.characters(string.toCharArray(), 0, string.length());

      handler.endElement(NS, CDXMLObj_String, CDXMLObj_String);
    }
  }
  
  private void writeColoredMolecularArea(CDColoredMolecularArea area) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLProp_Id, area);
    addAttribute(attributes, CDXMLProp_BackgroundColor, area.getBackgroundColor());
    addReferenceListAttribute(attributes, CDXMLProp_BasisObjects, area.getBasisObjects());

    handler.startElement(NS, CDXMLObj_ColoredMolecularArea, CDXMLObj_ColoredMolecularArea, attributes);
    handler.endElement(NS, CDXMLObj_ColoredMolecularArea, CDXMLObj_ColoredMolecularArea);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, node);
    addAttribute(attributes, CDXMLProp_2DPosition, node.getPosition2D());
    addAttribute(attributes, CDXMLProp_3DPosition, node.getPosition3D());
    addAttribute(attributes, CDXMLProp_ForegroundColor, node.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, node.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_HighlightColor, node.getSettings().getHighlightColor());
    addAttribute(attributes, CDXMLProp_ZOrder, node.getZOrder());
    if (node.getElementNumber() != 6) {
      addAttribute(attributes, CDXMLProp_Node_Element, node.getElementNumber());
    }
    if (node.getLabelDisplay() != CDLabelDisplay.Auto) {
      addAttribute(attributes, CDXMLProp_Node_LabelDisplay, node.getLabelDisplay());
    }
    if (node.getNodeType() != CDNodeType.Element) {
      addAttribute(attributes, CDXMLProp_Node_Type, node.getNodeType());
    }
    addAttribute(attributes, CDXMLProp_Atom_ElementList, node.getElementList());
    addAttribute(attributes, CDXMLProp_Atom_GenericList, node.getGenericList());
    addAttribute(attributes, CDXMLProp_Atom_Formula, node.getFormula());
    if (node.getIsotope() > 0) {
      addAttribute(attributes, CDXMLProp_Atom_Isotope, node.getIsotope());
    }
    if (node.getCharge() != 0) {
      addAttribute(attributes, CDXMLProp_Atom_Charge, node.getCharge());
    }
    if (node.getRadical() != CDRadical.None) {
      addAttribute(attributes, CDXMLProp_Atom_Radical, node.getRadical());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.FreeSites
        && node.getSubstituentCount() != 0) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictFreeSites, node.getSubstituentCount());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.SubstituentsUpTo
        && node.getSubstituentCount() > 0) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictSubstituentsUpTo, node.getSubstituentCount());
    }
    if (node.getSubstituentType() == CDAtomSubstituentType.SubstituentsExactly
        && node.getSubstituentCount() > 0) {
      addAttribute(
          attributes, CDXMLProp_Atom_RestrictSubstituentsExactly, node.getSubstituentCount());
    }
    if (node.isImplicitHydrogensAllowed()) {
      addAttribute(
          attributes, CDXMLProp_Atom_RestrictImplicitHydrogens, node.isImplicitHydrogensAllowed());
    }
    if (node.getRingBondCount() != CDRingBondCount.Unspecified) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictRingBondCount, node.getRingBondCount());
    }
    if (node.getUnsaturatedBonds() != CDUnsaturation.Unspecified) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictUnsaturatedBonds, node.getUnsaturatedBonds());
    }
    if (node.isRestrictReactionChange()) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictRxnChange, node.isRestrictReactionChange());
    }
    if (node.getReactionStereo() != CDReactionStereo.Unspecified) {
      addAttribute(attributes, CDXMLProp_Atom_RestrictRxnStereo, node.getReactionStereo());
    }
    if (node.getTranslation() != CDTranslation.Equal) {
      addAttribute(attributes, CDXMLProp_Atom_Translation, node.getTranslation());
    }
    if (node.getIsotopicAbundance() != CDIsotopicAbundance.Unspecified) {
      addAttribute(attributes, CDXMLProp_Atom_IsotopicAbundance, node.getIsotopicAbundance());
    }
    if (node.getAttachmentPointType() != CDExternalConnectionType.Unspecified) {
      addAttribute(
          attributes, CDXMLProp_Atom_ExternalConnectionType, node.getAttachmentPointType());
    }
    if (node.isAbnormalValenceAllowed()) {
      addAttribute(attributes, CDXMLProp_Atom_AbnormalValence, node.isAbnormalValenceAllowed());
    }
    if (node.getNumImplicitHydrogens() > 0) {
      addAttribute(attributes, CDXMLProp_Atom_NumHydrogens, node.getNumImplicitHydrogens());
    }
    if (node.isHDot()) {
      addAttribute(attributes, CDXMLProp_Atom_HDot, node.isHDot());
    }
    if (node.isHDash()) {
      addAttribute(attributes, CDXMLProp_Atom_HDash, node.isHDash());
    }
    if (node.getAtomGeometry() != CDAtomGeometry.Unknown) {
      addAttribute(attributes, CDXMLProp_Atom_Geometry, node.getAtomGeometry());
    }
    addReferenceListAttribute(attributes, CDXMLProp_Atom_BondOrdering, node.getBondOrdering());
    addReferenceListAttribute(attributes, CDXMLProp_Node_Attachments, node.getAttachedAtoms());
    addAttribute(attributes, CDXMLProp_Atom_GenericNickname, node.getLabelText());
    addReferenceAttribute(attributes, CDXMLProp_Atom_AltGroupID, node.getAltGroup());
    if (node.getStereochemistry() != CDAtomCIPType.Undetermined) {
      addAttribute(attributes, CDXMLProp_Atom_CIPStereochemistry, node.getStereochemistry());
    }
    addAttribute(attributes, CDXMLProp_Atom_AtomNumber, node.getAtomNumber());
    if (node.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, node.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, node.getChemicalWarning());
    if (!node.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, node.isVisible());
    }
    if (!node.getSettings().isShowAtomQuery()) {
      addAttribute(attributes, CDXMLProp_Atom_ShowQuery, node.getSettings().isShowAtomQuery());
    }
    if (node.getSettings().isShowAtomStereo()) {
      addAttribute(attributes, CDXMLProp_Atom_ShowStereo, node.getSettings().isShowAtomStereo());
    }
    if (!node.getSettings().isShowAtomEnhancedStereo()) {
      addAttribute(
          attributes,
          CDXMLProp_Atom_ShowEnhancedStereo,
          node.getSettings().isShowAtomEnhancedStereo());
    }
    if (node.getSettings().isShowAtomNumber()) {
      addAttribute(
          attributes, CDXMLProp_Atom_ShowAtomNumber, node.getSettings().isShowAtomNumber());
    }
    if (node.getSettings().isShowTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLProp_ShowTerminalCarbonLabels,
          node.getSettings().isShowTerminalCarbonLabels());
    }
    if (node.getSettings().isShowNonTerminalCarbonLabels()) {
      addAttribute(
          attributes,
          CDXMLProp_ShowNonTerminalCarbonLabels,
          node.getSettings().isShowNonTerminalCarbonLabels());
    }
    if (node.getSettings().isHideImplicitHydrogens()) {
      addAttribute(
          attributes,
          CDXMLProp_HideImplicitHydrogens,
          node.getSettings().isHideImplicitHydrogens());
    }
    if (node.getSettings().getLineWidth() > 0) {
      addAttribute(attributes, CDXMLProp_LineWidth, node.getSettings().getLineWidth());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFont, node.getSettings().getLabelFont());
    if (node.getSettings().getLabelSize() > 0) {
      addAttribute(attributes, CDXMLProp_LabelStyleSize, node.getSettings().getLabelSize());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFace, node.getSettings().getLabelFace());
    if (node.getLinkCountLow() > 0) {
      addAttribute(attributes, CDXMLProp_Atom_LinkCountLow, node.getLinkCountLow());
    }
    if (node.getLinkCountHigh() > 0) {
      addAttribute(attributes, CDXMLProp_Atom_LinkCountHigh, node.getLinkCountHigh());
    }

    handler.startElement(NS, CDXMLObj_Node, CDXMLObj_Node, attributes);

    for (CDFragment fragment : node.getFragments()) {
      writeFragment(fragment);
    }
    if (node.getText() != null) {
      writeText(node.getText());
    }
    for (CDObjectTag objectTag : node.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Node, CDXMLObj_Node);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, bond);
    addAttribute(attributes, CDXMLProp_ForegroundColor, bond.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, bond.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_HighlightColor, bond.getSettings().getHighlightColor());
    addAttribute(attributes, CDXMLProp_ZOrder, bond.getZOrder());
    addBondOrderAttribute(attributes, CDXMLProp_Bond_Order, bond.getBondOrder());
    if (bond.getBondDisplay() != CDBondDisplay.Solid) {
      addAttribute(attributes, CDXMLProp_Bond_Display, bond.getBondDisplay());
    }
    if (bond.getBondDisplay2() != CDBondDisplay.Solid) {
      addAttribute(attributes, CDXMLProp_Bond_Display2, bond.getBondDisplay2());
    }
    addAttribute(attributes, CDXMLProp_Bond_DoublePosition, bond.getBondDoublePosition());
    addReferenceAttribute(attributes, CDXMLProp_Bond_Begin, bond.getBegin());
    addReferenceAttribute(attributes, CDXMLProp_Bond_End, bond.getEnd());
    if (bond.getTopology() != CDBondTopology.Unspecified) {
      addAttribute(attributes, CDXMLProp_Bond_RestrictTopology, bond.getTopology());
    }
    if (bond.getReactionParticipation() != CDBondReactionParticipation.Unspecified) {
      addAttribute(
          attributes, CDXMLProp_Bond_RestrictRxnParticipation, bond.getReactionParticipation());
    }
    if (bond.getBeginAttach() >= 0) {
      addAttribute(attributes, CDXMLProp_Bond_BeginAttach, bond.getBeginAttach());
    }
    if (bond.getEndAttach() >= 0) {
      addAttribute(attributes, CDXMLProp_Bond_EndAttach, bond.getEndAttach());
    }
    if (bond.getStereochemistry() != CDBondCIPType.Undetermined) {
      addAttribute(attributes, CDXMLProp_Bond_CIPStereochemistry, bond.getStereochemistry());
    }
    addReferenceListAttribute(
        attributes, CDXMLProp_Bond_BondOrdering, bond.getBondCircularOrdering());
    if (bond.getCrossingBonds() != null) {
      addReferenceListAttribute(
          attributes, CDXMLProp_Bond_CrossingBonds, new ArrayList<CDBond>(bond.getCrossingBonds()));
    }
    if (bond.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, bond.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, bond.getChemicalWarning());
    if (!bond.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, bond.isVisible());
    }
    if (!bond.getSettings().isShowBondQuery()) {
      addAttribute(attributes, CDXMLProp_Bond_ShowQuery, bond.getSettings().isShowBondQuery());
    }
    if (bond.getSettings().isShowBondStereo()) {
      addAttribute(attributes, CDXMLProp_Bond_ShowStereo, bond.getSettings().isShowBondStereo());
    }
    if (!bond.getSettings().isShowBondReaction()) {
      addAttribute(attributes, CDXMLProp_Bond_ShowRxn, bond.getSettings().isShowBondReaction());
    }
    if (bond.getSettings().getBondSpacing() > 0) {
      addAttribute(attributes, CDXMLProp_BondSpacing, bond.getSettings().getBondSpacing());
    }
    if (bond.getSettings().getBondSpacingAbs() > 0) {
      addAttribute(attributes, CDXMLProp_BondSpacingAbs, bond.getSettings().getBondSpacingAbs());
    }
    if (bond.getSettings().getBondLength() > 0) {
      addAttribute(attributes, CDXMLProp_BondLength, bond.getSettings().getBondLength());
    }
    if (bond.getSettings().getBoldWidth() > 0) {
      addAttribute(attributes, CDXMLProp_BoldWidth, bond.getSettings().getBoldWidth());
    }
    if (bond.getSettings().getLineWidth() > 0) {
      addAttribute(attributes, CDXMLProp_LineWidth, bond.getSettings().getLineWidth());
    }
    if (bond.getSettings().getMarginWidth() > 0) {
      addAttribute(attributes, CDXMLProp_MarginWidth, bond.getSettings().getMarginWidth());
    }
    if (bond.getSettings().getHashSpacing() > 0) {
      addAttribute(attributes, CDXMLProp_HashSpacing, bond.getSettings().getHashSpacing());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFont, bond.getSettings().getLabelFont());
    if (bond.getSettings().getLabelSize() > 0) {
      addAttribute(attributes, CDXMLProp_LabelStyleSize, bond.getSettings().getLabelSize());
    }
    addAttribute(attributes, CDXMLProp_LabelStyleFace, bond.getSettings().getLabelFace());

    handler.startElement(NS, CDXMLObj_Bond, CDXMLObj_Bond, attributes);

    for (CDObjectTag objectTag : bond.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Bond, CDXMLObj_Bond);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, graphic);
    addAttribute(attributes, CDXMLProp_ForegroundColor, graphic.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, graphic.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, graphic.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, graphic.getBounds());
    if (graphic.getGraphicType() != CDGraphicType.Undefined) {
      addAttribute(attributes, CDXMLProp_Graphic_Type, graphic.getGraphicType());
    }
    addAttribute(attributes, CDXMLProp_Line_Type, graphic.getLineType());
    addAttribute(attributes, CDXMLProp_Arrow_Type, graphic.getArrowType());
    addAttribute(attributes, CDXMLProp_Bracket_Type, graphic.getBracketType());
    addAttribute(attributes, CDXMLProp_Rectangle_Type, graphic.getRectangleType());
    addAttribute(attributes, CDXMLProp_Oval_Type, graphic.getOvalType());
    addAttribute(attributes, CDXMLProp_Orbital_Type, graphic.getOrbitalType());
    addAttribute(attributes, CDXMLProp_Symbol_Type, graphic.getSymbolType());
    if (graphic.getArrowHeadSize() != 0) {
      addAttribute(attributes, CDXMLProp_Arrow_HeadSize, graphic.getArrowHeadSize() * 100f);
    }
    if (graphic.getBracketLipSize() != 0) {
      addAttribute(attributes, CDXMLProp_Bracket_LipSize, graphic.getBracketLipSize());
    }
    if (graphic.getArcAngularSize() != 0) {
      addAttribute(attributes, CDXMLProp_Arc_AngularSize, graphic.getArcAngularSize());
    }
    if (graphic.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, graphic.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, graphic.getChemicalWarning());
    if (!graphic.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, graphic.isVisible());
    }
    if (graphic.getSettings().getBoldWidth() != 0) {
      addAttribute(attributes, CDXMLProp_BoldWidth, graphic.getSettings().getBoldWidth());
    }
    if (graphic.getSettings().getLineWidth() != 0) {
      addAttribute(attributes, CDXMLProp_LineWidth, graphic.getSettings().getLineWidth());
    }
    if (graphic.getSettings().getHashSpacing() != 0) {
      addAttribute(attributes, CDXMLProp_HashSpacing, graphic.getSettings().getHashSpacing());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFont, graphic.getSettings().getCaptionFont());
    if (graphic.getSettings().getCaptionSize() != 0) {
      addAttribute(attributes, CDXMLProp_CaptionStyleSize, graphic.getSettings().getCaptionSize());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFace, graphic.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLProp_BracketUsage, graphic.getBracketUsage());
    addAttribute(attributes, CDXMLProp_Polymer_RepeatPattern, graphic.getPolymerRepeatPattern());
    addAttribute(attributes, CDXMLProp_Polymer_FlipType, graphic.getPolymerFlipType());
    addAttribute(attributes, CDXMLProp_Head3D, graphic.getHead3D());
    addAttribute(attributes, CDXMLProp_Tail3D, graphic.getTail3D());
    addAttribute(attributes, CDXMLProp_Center3D, graphic.getTail3D());

    addAttribute(attributes, CDXMLProp_MajorAxisEnd3D, graphic.getMajorAxisEnd3D());
    addAttribute(attributes, CDXMLProp_MinorAxisEnd3D, graphic.getMinorAxisEnd3D());

    addAttribute(attributes, CDXMLProp_Curve_FillType, graphic.getFillType());
    if (graphic.getShadowSize() != 0) {
      addAttribute(attributes, CDXMLProp_ShadowSize, graphic.getShadowSize());
    }
    if (graphic.getCornerRadius() != 0) {
      addAttribute(attributes, CDXMLProp_CornerRadius, graphic.getCornerRadius());
    }
    if (graphic.getFadePercent() != 100) {
      addAttribute(attributes, CDXMLProp_FadePercent, graphic.getFadePercent());
    }

    handler.startElement(NS, CDXMLObj_Graphic, CDXMLObj_Graphic, attributes);

    for (CDObjectTag objectTag : graphic.getObjectTags()) {
      writeObjectTag(objectTag);
    }
    writeRepresents(graphic.getRepresents());

    handler.endElement(NS, CDXMLObj_Graphic, CDXMLObj_Graphic);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, arrow);
    addAttribute(attributes, CDXMLProp_ForegroundColor, arrow.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, arrow.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, arrow.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, arrow.getBounds());
    addAttribute(attributes, CDXMLProp_Line_Type, arrow.getLineType());
    addAttribute(attributes, CDXMLProp_Arrow_HeadSize, arrow.getHeadSize() * 100f);
    if (arrow.getAngularSize() != 0) {
      addAttribute(attributes, CDXMLProp_Arc_AngularSize, arrow.getAngularSize());
    }
    if (arrow.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, arrow.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, arrow.getChemicalWarning());
    if (!arrow.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, arrow.isVisible());
    }
    if (arrow.getSettings().getBoldWidth() != 0) {
      addAttribute(attributes, CDXMLProp_BoldWidth, arrow.getSettings().getBoldWidth());
    }
    if (arrow.getSettings().getLineWidth() != 0) {
      addAttribute(attributes, CDXMLProp_LineWidth, arrow.getSettings().getLineWidth());
    }
    if (arrow.getSettings().getHashSpacing() != 0) {
      addAttribute(attributes, CDXMLProp_HashSpacing, arrow.getSettings().getHashSpacing());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFont, arrow.getSettings().getCaptionFont());
    if (arrow.getSettings().getCaptionSize() != 0) {
      addAttribute(attributes, CDXMLProp_CaptionStyleSize, arrow.getSettings().getCaptionSize());
    }
    addAttribute(attributes, CDXMLProp_CaptionStyleFace, arrow.getSettings().getCaptionFace());
    addAttribute(attributes, CDXMLProp_Head3D, arrow.getHead3D());
    addAttribute(attributes, CDXMLProp_Tail3D, arrow.getTail3D());
    addAttribute(attributes, CDXMLProp_Center3D, arrow.getCenter3D());

    addAttribute(attributes, CDXMLProp_MajorAxisEnd3D, arrow.getMajorAxisEnd3D());
    addAttribute(attributes, CDXMLProp_MinorAxisEnd3D, arrow.getMinorAxisEnd3D());

    // arrow
    if (arrow.getHeadWidth() > 0) {
      addAttribute(attributes, CDXMLProp_ArrowHeadWidth, arrow.getHeadWidth() * 100f);
    }
    if (arrow.getHeadCenterSize() > 0) {
      addAttribute(attributes, CDXMLProp_ArrowHeadCenterSize, arrow.getHeadCenterSize() * 100f);
    }
    if (arrow.getEquilibriumRatio() > 0) {
      addAttribute(attributes, CDXMLProp_ArrowEquilibriumRatio, arrow.getEquilibriumRatio() * 100f);
    }
    if (arrow.isDipole()) {
      addAttribute(attributes, CDXMLProp_Dipole, arrow.isDipole());
    }
    addAttribute(attributes, CDXMLProp_ArrowHeadType, arrow.getArrowHeadType());
    addAttribute(attributes, CDXMLProp_ArrowHeadHead, arrow.getArrowHeadPositionStart());
    addAttribute(attributes, CDXMLProp_ArrowHeadTail, arrow.getArrowHeadPositionTail());
    if (arrow.getShaftSpacing() > 0) {
      addAttribute(attributes, CDXMLProp_ArrowShaftSpacing, arrow.getShaftSpacing() * 100f);
    }
    addAttribute(attributes, CDXMLProp_NoGo, arrow.getNoGoType());
    addAttribute(attributes, CDXMLProp_Curve_FillType, arrow.getFillType());

    handler.startElement(NS, CDXMLObj_Arrow, CDXMLObj_Arrow, attributes);

    for (CDObjectTag objectTag : arrow.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Arrow, CDXMLObj_Arrow);
  }

  private void writeRepresents(Map<String, Object> represents) throws SAXException, IOException {
    for (Entry<String, Object> represent : represents.entrySet()) {
      AttributesImpl attributes = new AttributesImpl();
      addAttribute(attributes, CDXMLProp_Attribute, represent.getKey());
      addReferenceAttribute(attributes, CDXMLProp_Object, represent.getValue());

      handler.startElement(NS, CDXMLObj_Represent, CDXMLObj_Represent, attributes);

      handler.endElement(NS, CDXMLObj_Represent, CDXMLObj_Represent);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, curve);
    addAttribute(attributes, CDXMLProp_ForegroundColor, curve.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, curve.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, curve.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, curve.getBounds());
    addAttribute(attributes, CDXMLProp_Curve_FillType, curve.getFillType());
    addAttribute(attributes, CDXMLProp_Line_Type, curve.getLineType());
    addPoint2DListAttribute(attributes, CDXMLProp_Curve_Points, curve.getPoints2D());
    addPoint3DListAttribute(attributes, CDXMLProp_Curve_Points3D, curve.getPoints3D());
    if (curve.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, curve.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, curve.getChemicalWarning());
    if (!curve.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, curve.isVisible());
    }
    addAttribute(attributes, CDXMLProp_ArrowHeadType, curve.getArrowHeadType());
    addAttribute(attributes, CDXMLProp_ArrowHeadHead, curve.getArrowHeadPositionAtStart());
    addAttribute(attributes, CDXMLProp_ArrowHeadTail, curve.getArrowHeadPositionAtStart());

    if (curve.isClosed()) {
      addAttribute(attributes, CDXMLProp_Closed, curve.isClosed());
    }
    if (curve.getSettings().getLineWidth() != 0) {
      addAttribute(attributes, CDXMLProp_LineWidth, curve.getSettings().getLineWidth());
    }
    if (curve.getSettings().getHashSpacing() != 0) {
      addAttribute(attributes, CDXMLProp_HashSpacing, curve.getSettings().getHashSpacing());
    }

    handler.startElement(NS, CDXMLObj_Curve, CDXMLObj_Curve, attributes);

    for (CDObjectTag objectTag : curve.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Curve, CDXMLObj_Curve);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, altGroup);
    addAttribute(attributes, CDXMLProp_ForegroundColor, altGroup.getColor());
    addAttribute(
        attributes, CDXMLProp_BackgroundColor, altGroup.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, altGroup.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, altGroup.getBounds());
    addAttribute(attributes, CDXMLProp_NamedAlternativeGroup_TextFrame, altGroup.getTextFrame());
    addAttribute(attributes, CDXMLProp_NamedAlternativeGroup_GroupFrame, altGroup.getGroupFrame());
    addAttribute(attributes, CDXMLProp_NamedAlternativeGroup_Valence, altGroup.getValence());
    if (altGroup.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, altGroup.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, altGroup.getChemicalWarning());
    if (!altGroup.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, altGroup.isVisible());
    }

    handler.startElement(NS, CDXMLObj_Curve, CDXMLObj_Curve, attributes);

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

    handler.endElement(NS, CDXMLObj_Curve, CDXMLObj_Curve);
  }

  private void collectReactionStep(CDReactionStep reactionStep) {
    collectReference(reactionStep);
  }

  private void writeReactionStep(CDReactionStep reactionStep) throws SAXException, IOException {
    if (reactionStep == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLProp_Id, reactionStep);
    addReferenceListAttribute(
        attributes, CDXMLProp_ReactionStep_Reactants, reactionStep.getReactants());
    addReferenceListAttribute(
        attributes, CDXMLProp_ReactionStep_Products, reactionStep.getProducts());
    addReferenceListAttribute(
        attributes, CDXMLProp_ReactionStep_Plusses, reactionStep.getPlusses());
    addReferenceListAttribute(attributes, CDXMLProp_ReactionStep_Arrows, reactionStep.getArrows());
    addReferenceListAttribute(
        attributes, CDXMLProp_ReactionStep_ObjectsAboveArrow, reactionStep.getObjectsAboveArrow());
    addReferenceListAttribute(
        attributes, CDXMLProp_ReactionStep_ObjectsBelowArrow, reactionStep.getObjectsBelowArrow());
    addReferenceMapAttribute(
        attributes, CDXMLProp_ReactionStep_Atom_Map, reactionStep.getAtomMap());
    addReferenceMapAttribute(
        attributes, CDXMLProp_ReactionStep_Atom_Map_Manual, reactionStep.getAtomMapManual());
    addReferenceMapAttribute(
        attributes, CDXMLProp_ReactionStep_Atom_Map_Auto, reactionStep.getAtomMapAuto());

    handler.startElement(NS, CDXMLObj_ReactionStep, CDXMLObj_ReactionStep, attributes);

    handler.endElement(NS, CDXMLObj_ReactionStep, CDXMLObj_ReactionStep);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, reactionScheme);

    handler.startElement(NS, CDXMLObj_ReactionScheme, CDXMLObj_ReactionScheme, attributes);

    for (CDReactionStep reactionStep : reactionScheme.getSteps()) {
      writeReactionStep(reactionStep);
    }

    handler.endElement(NS, CDXMLObj_ReactionScheme, CDXMLObj_ReactionScheme);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, geometry);
    addAttribute(attributes, CDXMLProp_ForegroundColor, geometry.getColor());
    addAttribute(attributes, CDXMLProp_Name, geometry.getName());
    addAttribute(attributes, CDXMLProp_LineWidth, geometry.getSettings().getLineWidth());
    if (geometry.getGeometricType() != CDGeometryType.Undefined) {
      addAttribute(attributes, CDXMLProp_GeometricFeature, geometry.getGeometricType());
    }
    addAttribute(attributes, CDXMLProp_RelationValue, geometry.getRelationValue());
    addReferenceListAttribute(attributes, CDXMLProp_BasisObjects, geometry.getBasisObjects());

    handler.startElement(NS, CDXMLObj_Geometry, CDXMLObj_Geometry, attributes);

    handler.endElement(NS, CDXMLObj_Geometry, CDXMLObj_Geometry);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, constraint);
    addAttribute(attributes, CDXMLProp_ForegroundColor, constraint.getColor());
    addAttribute(attributes, CDXMLProp_Name, constraint.getName());
    addAttribute(attributes, CDXMLProp_LineWidth, constraint.getSettings().getLineWidth());
    if (constraint.getConstraintType() != CDConstraintType.Undefined) {
      addAttribute(attributes, CDXMLProp_ConstraintType, constraint.getConstraintType());
    }
    addAttribute(attributes, CDXMLProp_ConstraintMin, constraint.getMinRange());
    addAttribute(attributes, CDXMLProp_ConstraintMax, constraint.getMaxRange());
    addAttribute(
        attributes, CDXMLProp_IgnoreUnconnectedAtoms, constraint.isIgnoreUnconnectedAtoms());
    if (constraint.isDihedralIsChiral()) {
      addAttribute(attributes, CDXMLProp_DihedralIsChiral, constraint.isDihedralIsChiral());
    }
    if (constraint.isPointIsDirected()) {
      addAttribute(attributes, CDXMLProp_PointIsDirected, constraint.isPointIsDirected());
    }

    handler.startElement(NS, CDXMLObj_Constraint, CDXMLObj_Constraint, attributes);

    handler.endElement(NS, CDXMLObj_Constraint, CDXMLObj_Constraint);
  }

  private void collectTemplateGrid(CDTemplateGrid templateGrid) {
    collectReference(templateGrid);
  }

  private void writeTemplateGrid(CDTemplateGrid templateGrid) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLProp_2DExtent, templateGrid.getExtent());
    addAttribute(attributes, CDXMLProp_Template_PaneHeight, templateGrid.getPaneHeight());
    addAttribute(attributes, CDXMLProp_Template_NumRows, templateGrid.getNumRows());
    addAttribute(attributes, CDXMLProp_Template_NumColumns, templateGrid.getNumColumns());

    handler.startElement(NS, CDXMLObj_TemplateGrid, CDXMLObj_TemplateGrid, attributes);

    handler.endElement(NS, CDXMLObj_TemplateGrid, CDXMLObj_TemplateGrid);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, spectrum);
    addAttribute(attributes, CDXMLProp_ForegroundColor, spectrum.getColor());
    addAttribute(
        attributes, CDXMLProp_BackgroundColor, spectrum.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, spectrum.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, spectrum.getBounds());
    addAttribute(attributes, CDXMLProp_Spectrum_XSpacing, spectrum.getXSpacing());
    addAttribute(attributes, CDXMLProp_Spectrum_XLow, spectrum.getXLow());
    if (spectrum.getXType() != CDSpectrumXType.Unknown) {
      addAttribute(attributes, CDXMLProp_Spectrum_XType, spectrum.getXType());
    }
    if (spectrum.getYType() != CDSpectrumYType.Unknown) {
      addAttribute(attributes, CDXMLProp_Spectrum_YType, spectrum.getYType());
    }
    if (spectrum.getSpectrumClass() != CDSpectrumClass.Unknown) {
      addAttribute(attributes, CDXMLProp_Spectrum_Class, spectrum.getSpectrumClass());
    }
    addAttribute(attributes, CDXMLProp_Spectrum_XAxisLabel, spectrum.getXAxisLabel());
    addAttribute(attributes, CDXMLProp_Spectrum_YAxisLabel, spectrum.getYAxisLabel());
    if (spectrum.getYLow() != 0.0) {
      addAttribute(attributes, CDXMLProp_Spectrum_YLow, spectrum.getYLow());
    }
    if (spectrum.getYScale() != 1.0) {
      addAttribute(attributes, CDXMLProp_Spectrum_YScale, spectrum.getYScale());
    }
    if (spectrum.isIgnoreWarnings()) {
      addAttribute(attributes, CDXMLProp_IgnoreWarnings, spectrum.isIgnoreWarnings());
    }
    addAttribute(attributes, CDXMLProp_ChemicalWarning, spectrum.getChemicalWarning());
    if (!spectrum.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, spectrum.isVisible());
    }
    addAttribute(attributes, CDXMLProp_BoldWidth, spectrum.getSettings().getBoldWidth());
    addAttribute(attributes, CDXMLProp_LineWidth, spectrum.getSettings().getLineWidth());
    addAttribute(attributes, CDXMLProp_LabelStyleFont, spectrum.getSettings().getLabelFont());
    addAttribute(attributes, CDXMLProp_LabelStyleSize, spectrum.getSettings().getLabelSize());
    addAttribute(attributes, CDXMLProp_LabelStyleFace, spectrum.getSettings().getLabelFace());

    handler.startElement(NS, CDXMLObj_Spectrum, CDXMLObj_Spectrum, attributes);

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

    handler.endElement(NS, CDXMLObj_Spectrum, CDXMLObj_Spectrum);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, embeddedObject);
    // SupersededBy CDATA #IMPLIED
    addAttribute(attributes, CDXMLProp_ForegroundColor, embeddedObject.getColor());
    addAttribute(
        attributes, CDXMLProp_BackgroundColor, embeddedObject.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, embeddedObject.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, embeddedObject.getBounds());
    addAttribute(attributes, CDXMLProp_RotationAngle, embeddedObject.getRotationAngle());
    addAttribute(attributes, CDXMLProp_Picture_Edition, embeddedObject.getPictureEdition());
    addAttribute(
        attributes, CDXMLProp_Picture_EditionAlias, embeddedObject.getPictureEditionAlias());
    addAttribute(attributes, CDXMLProp_MacPICT, embeddedObject.getMacPICT());

    if (embeddedObject.getEnhancedMetafile() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getEnhancedMetafile());
      addAttribute(
          attributes,
          CDXMLProp_CompressedEnhancedMetafile,
          Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes,
          CDXMLProp_UncompressedEnhancedMetafileSize,
          embeddedObject.getEnhancedMetafile().length);
    }
    if (embeddedObject.getOleObject() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getOleObject());
      addAttribute(
          attributes, CDXMLProp_CompressedOLEObject, Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes, CDXMLProp_UncompressedOLEObjectSize, embeddedObject.getOleObject().length);
    }
    if (embeddedObject.getWindowsMetafile() != null) {
      byte[] data = IOUtils.compress(embeddedObject.getWindowsMetafile());
      addAttribute(
          attributes,
          CDXMLProp_CompressedWindowsMetafile,
          Base64.getEncoder().encodeToString(data));
      addAttribute(
          attributes,
          CDXMLProp_UncompressedWindowsMetafileSize,
          embeddedObject.getWindowsMetafile().length);
    }

    addAttribute(attributes, CDXMLProp_GIF, embeddedObject.getGif());
    addAttribute(attributes, CDXMLProp_TIFF, embeddedObject.getTiff());
    addAttribute(attributes, CDXMLProp_PNG, embeddedObject.getPng());
    addAttribute(attributes, CDXMLProp_JPEG, embeddedObject.getJpeg());
    addAttribute(attributes, CDXMLProp_BMP, embeddedObject.getBmp());

    handler.startElement(NS, CDXMLObj_EmbeddedObject, CDXMLObj_EmbeddedObject, attributes);

    for (CDObjectTag objectTag : embeddedObject.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_EmbeddedObject, CDXMLObj_EmbeddedObject);
  }

  private void collectObjectTag(CDObjectTag objectTag) {
    collectReference(objectTag);

    for (CDText text : objectTag.getTexts()) {
      collectText(text);
    }
  }

  private void writeObjectTag(CDObjectTag objectTag) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLProp_Id, objectTag);
    if (!objectTag.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, objectTag.isVisible());
    }
    addAttribute(attributes, CDXMLProp_ObjectTag_Type, objectTag.getObjectTagType());
    addAttribute(attributes, CDXMLProp_Name, objectTag.getName());
    if (!objectTag.isTracking()) {
      addAttribute(attributes, CDXMLProp_ObjectTag_Tracking, objectTag.isTracking());
    }
    if (!objectTag.isPersistent()) {
      addAttribute(attributes, CDXMLProp_ObjectTag_Persistent, objectTag.isPersistent());
    }

    if (objectTag.getValue() instanceof String) {
      addAttribute(attributes, CDXMLProp_ObjectTag_Value, (String) objectTag.getValue());
    } else if (objectTag.getValue() instanceof Integer) {
      addAttribute(attributes, CDXMLProp_ObjectTag_Value, (Integer) objectTag.getValue());
    } else if (objectTag.getValue() instanceof Double) {
      addAttribute(attributes, CDXMLProp_ObjectTag_Value, (Double) objectTag.getValue());
    }

    if (objectTag.getPositioningType() != CDPositioningType.Auto) {
      addAttribute(attributes, CDXMLProp_Positioning, objectTag.getPositioningType());
    }
    if (objectTag.getPositioningAngle() != 0.0) {
      addAttribute(attributes, CDXMLProp_PositioningAngle, objectTag.getPositioningAngle());
    }
    addAttribute(attributes, CDXMLProp_PositioningOffset, objectTag.getPositioningOffset());

    handler.startElement(NS, CDXMLObj_ObjectTag, CDXMLObj_ObjectTag, attributes);

    for (CDText text : objectTag.getTexts()) {
      writeText(text);
    }

    handler.endElement(NS, CDXMLObj_ObjectTag, CDXMLObj_ObjectTag);
  }

  private void collectSequence(CDSequence sequence) {
    collectReference(sequence);
  }

  private void writeSequence(CDSequence sequence) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLProp_Sequence_Identifier, sequence.getIdentifier());

    handler.startElement(NS, CDXMLObj_Sequence, CDXMLObj_Sequence, attributes);
    handler.endElement(NS, CDXMLObj_Sequence, CDXMLObj_Sequence);
  }

  private void collectCrossReference(CDCrossReference crossReference) {
    collectReference(crossReference);
  }

  private void writeCrossReference(CDCrossReference crossReference) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLProp_CrossReference_Container, crossReference.getContainer());
    addAttribute(attributes, CDXMLProp_CrossReference_Document, crossReference.getDocument());
    addAttribute(attributes, CDXMLProp_CrossReference_Identifier, crossReference.getIdentifier());
    addAttribute(attributes, CDXMLProp_CrossReference_Sequence, crossReference.getSequence());

    handler.startElement(NS, CDXMLObj_CrossReference, CDXMLObj_CrossReference, attributes);

    handler.endElement(NS, CDXMLObj_CrossReference, CDXMLObj_CrossReference);
  }

  private void collectSplitter(CDSplitter splitter) {
    collectReference(splitter);
  }

  private void writeSplitter(CDSplitter splitter) throws SAXException, IOException {
    AttributesImpl attributes = new AttributesImpl();
    addAttribute(attributes, CDXMLProp_2DPosition, splitter.getPosition2D());
    if (splitter.getPageDefinition() != CDPageDefinition.Undefined) {
      addAttribute(attributes, CDXMLProp_PageDefinition, splitter.getPageDefinition());
    }

    handler.startElement(NS, CDXMLObj_Splitter, CDXMLObj_Splitter, attributes);

    handler.endElement(NS, CDXMLObj_Splitter, CDXMLObj_Splitter);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, table);
    if (!table.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, table.isVisible());
    }
    addAttribute(attributes, CDXMLProp_ForegroundColor, table.getColor());
    addAttribute(attributes, CDXMLProp_BackgroundColor, table.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, table.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, table.getBounds());
    addAttribute(attributes, CDXMLProp_BoldWidth, table.getSettings().getBoldWidth());
    addAttribute(attributes, CDXMLProp_LineWidth, table.getSettings().getLineWidth());
    addAttribute(attributes, CDXMLProp_LabelStyleFont, table.getSettings().getLabelFont());
    addAttribute(attributes, CDXMLProp_LabelStyleSize, table.getSettings().getLabelSize());
    addAttribute(attributes, CDXMLProp_LabelStyleFace, table.getSettings().getLabelFace());
    addAttribute(attributes, CDXMLProp_MarginWidth, table.getSettings().getMarginWidth());

    handler.startElement(NS, CDXMLObj_Table, CDXMLObj_Table, attributes);

    for (CDPage page : table.getPages()) {
      writePage(page);
    }
    for (CDObjectTag objectTag : table.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_Table, CDXMLObj_Table);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, tlcPlate);
    if (!tlcPlate.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, tlcPlate.isVisible());
    }
    addAttribute(attributes, CDXMLProp_ForegroundColor, tlcPlate.getColor());
    addAttribute(
        attributes, CDXMLProp_BackgroundColor, tlcPlate.getSettings().getBackgroundColor());
    addAttribute(attributes, CDXMLProp_ZOrder, tlcPlate.getZOrder());
    addAttribute(attributes, CDXMLProp_BoundingBox, tlcPlate.getBounds());
    addAttribute(attributes, CDXMLProp_BoldWidth, tlcPlate.getSettings().getBoldWidth());
    addAttribute(attributes, CDXMLProp_LineWidth, tlcPlate.getSettings().getLineWidth());
    addAttribute(attributes, CDXMLProp_LabelStyleFont, tlcPlate.getSettings().getLabelFont());
    addAttribute(attributes, CDXMLProp_LabelStyleSize, tlcPlate.getSettings().getLabelSize());
    addAttribute(attributes, CDXMLProp_LabelStyleFace, tlcPlate.getSettings().getLabelFace());
    addAttribute(attributes, CDXMLProp_MarginWidth, tlcPlate.getSettings().getMarginWidth());
    addAttribute(attributes, CDXMLProp_TopLeft, tlcPlate.getTopLeft());
    addAttribute(attributes, CDXMLProp_TopRight, tlcPlate.getTopRight());
    addAttribute(attributes, CDXMLProp_BottomRight, tlcPlate.getBottomRight());
    addAttribute(attributes, CDXMLProp_BottomLeft, tlcPlate.getBottomLeft());
    addAttribute(attributes, CDXMLProp_TLC_OriginFraction, tlcPlate.getOriginFraction());
    addAttribute(
        attributes, CDXMLProp_TLC_SolventFrontFraction, tlcPlate.getSolventFrontFraction());
    if (tlcPlate.isShowOrigin()) {
      addAttribute(attributes, CDXMLProp_TLC_ShowOrigin, tlcPlate.isShowOrigin());
    }
    if (tlcPlate.isShowSolventFront()) {
      addAttribute(attributes, CDXMLProp_TLC_ShowSolventFront, tlcPlate.isShowSolventFront());
    }
    if (tlcPlate.isShowBorders()) {
      addAttribute(attributes, CDXMLProp_TLC_ShowBorders, tlcPlate.isShowBorders());
    }
    if (tlcPlate.isShowSideTicks()) {
      addAttribute(attributes, CDXMLProp_ShowSideTicks, tlcPlate.isShowSideTicks());
    }
    if (tlcPlate.isTransparent()) {
      addAttribute(attributes, CDXMLProp_Transparent, tlcPlate.isTransparent());
    }

    handler.startElement(NS, CDXMLObj_TLCPlate, CDXMLObj_TLCPlate, attributes);

    for (CDTLCLane tlcLane : tlcPlate.getLanes()) {
      writeTLCLane(tlcLane);
    }
    for (CDObjectTag objectTag : tlcPlate.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_TLCPlate, CDXMLObj_TLCPlate);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, tlcLane);
    if (!tlcLane.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, tlcLane.isVisible());
    }

    handler.startElement(NS, CDXMLObj_TLCLane, CDXMLObj_TLCLane, attributes);

    for (CDTLCSpot tlcSpot : tlcLane.getSpots()) {
      writeTLCSpot(tlcSpot);
    }
    for (CDObjectTag objectTag : tlcLane.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_TLCLane, CDXMLObj_TLCLane);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, tlcSpot);
    if (!tlcSpot.isVisible()) {
      addAttribute(attributes, CDXMLProp_Visible, tlcSpot.isVisible());
    }
    addAttribute(attributes, CDXMLProp_ForegroundColor, tlcSpot.getColor());
    addAttribute(attributes, CDXMLProp_Width, tlcSpot.getWidth());
    addAttribute(attributes, CDXMLProp_Height, tlcSpot.getHeight());
    addAttribute(attributes, CDXMLProp_TLC_Tail, tlcSpot.getTail());
    addAttribute(attributes, CDXMLProp_TLC_Rf, tlcSpot.getRf());
    if (tlcSpot.isShowRf()) {
      addAttribute(attributes, CDXMLProp_TLC_ShowRf, tlcSpot.isShowRf());
    }
    addAttribute(attributes, CDXMLProp_Curve_Type, tlcSpot.getCurveType());

    handler.startElement(NS, CDXMLObj_TLCSpot, CDXMLObj_TLCSpot, attributes);

    for (CDObjectTag objectTag : tlcSpot.getObjectTags()) {
      writeObjectTag(objectTag);
    }

    handler.endElement(NS, CDXMLObj_TLCSpot, CDXMLObj_TLCSpot);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, bracketedGroup);
    addReferenceListAttribute(
        attributes, CDXMLProp_BracketedObjects, bracketedGroup.getBracketedObjects());
    addAttribute(attributes, CDXMLProp_Bracket_Usage, bracketedGroup.getBracketUsage());
    addAttribute(
        attributes, CDXMLProp_Polymer_RepeatPattern, bracketedGroup.getPolymerRepeatPattern());
    addAttribute(attributes, CDXMLProp_Polymer_FlipType, bracketedGroup.getPolymerFlipType());
    addAttribute(attributes, CDXMLProp_Bracket_RepeatCount, bracketedGroup.getRepeatCount());
    if (bracketedGroup.getComponentOrder() > 0) {
      addAttribute(
          attributes, CDXMLProp_Bracket_ComponentOrder, bracketedGroup.getComponentOrder());
    }
    addAttribute(attributes, CDXMLProp_Bracket_SRULabel, bracketedGroup.getSRULabel());

    handler.startElement(NS, CDXMLObj_BracketedGroup, CDXMLObj_BracketedGroup, attributes);

    for (CDBracket bracketedGroup2 : bracketedGroup.getBrackets()) {
      writeBracketedGroup(bracketedGroup2);
    }
    for (CDBracketAttachment bracketAttachment : bracketedGroup.getBracketAttachments()) {
      writeBracketAttachment(bracketAttachment);
    }

    handler.endElement(NS, CDXMLObj_BracketedGroup, CDXMLObj_BracketedGroup);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, bracketAttachment);
    addReferenceAttribute(attributes, CDXMLProp_Bracket_GraphicID, bracketAttachment.getGraphic());

    handler.startElement(NS, CDXMLObj_BracketAttachment, CDXMLObj_BracketAttachment, attributes);

    for (CDCrossingBond crossingBond : bracketAttachment.getCrossingBonds()) {
      writeCrossingBond(crossingBond);
    }

    handler.endElement(NS, CDXMLObj_BracketAttachment, CDXMLObj_BracketAttachment);
  }

  private void collectCrossingBond(CDCrossingBond crossingBond) {
    collectReference(crossingBond);
  }

  private void writeCrossingBond(CDCrossingBond crossingBond) throws SAXException, IOException {
    if (crossingBond == null) {
      return;
    }
    AttributesImpl attributes = new AttributesImpl();
    addReferenceAttribute(attributes, CDXMLProp_Id, crossingBond);
    addReferenceAttribute(attributes, CDXMLProp_Bracket_BondID, crossingBond.getBond());
    addReferenceAttribute(attributes, CDXMLProp_Bracket_InnerAtomID, crossingBond.getInnerAtom());

    handler.startElement(NS, CDXMLObj_CrossingBond, CDXMLObj_CrossingBond, attributes);

    handler.endElement(NS, CDXMLObj_CrossingBond, CDXMLObj_CrossingBond);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, border);
    addAttribute(attributes, CDXMLProp_Line_Type, border.getLineType());
    addAttribute(attributes, CDXMLProp_LineWidth, border.getWidth());
    addAttribute(attributes, CDXMLProp_ForegroundColor, border.getForegroundColor()); // TODO rename
    addAttribute(attributes, CDXMLProp_Side, border.getSide());

    handler.startElement(NS, CDXMLObj_Border, CDXMLObj_Border, attributes);

    handler.endElement(NS, CDXMLObj_Border, CDXMLObj_Border);
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
    addReferenceAttribute(attributes, CDXMLProp_Id, chemicalProperty);
    addAttribute(attributes, CDXMLProp_ChemicalPropertyType, chemicalProperty.getType());
    addReferenceAttribute(
        attributes, CDXMLProp_ChemicalPropertyDisplayID, chemicalProperty.getDisplay());
    if (chemicalProperty.isActive()) {
      addAttribute(attributes, CDXMLProp_ChemicalPropertyIsActive, chemicalProperty.isActive());
    }
    addReferenceListAttribute(
        attributes, CDXMLProp_BasisObjects, chemicalProperty.getBasisObjects());

    handler.startElement(NS, CDXMLObj_ChemicalProperty, CDXMLObj_ChemicalProperty, attributes);

    handler.endElement(NS, CDXMLObj_ChemicalProperty, CDXMLObj_ChemicalProperty);
  }

  // #######################################################
  // #######################################################

  private void addAttribute(AttributesImpl attributes, String name, boolean value) {
    attributes.addAttribute("", name, name, CDATA, value ? "yes" : "no");
  }

  private void addAttribute(AttributesImpl attributes, String name, int value) {
    attributes.addAttribute("", name, name, CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDElementList value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertElementListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGenericList value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertGenericListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, List<Integer> value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertIntListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, float value) {
    attributes.addAttribute("", name, name, CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, double value) {
    attributes.addAttribute("", name, name, CDATA, String.valueOf(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPoint2D value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertPoint2DToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPoint3D value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertPoint3DToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRectangle value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertRectangleToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, byte[] value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertByteArrayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, String value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, value);
  }

  private void addAttribute(AttributesImpl attributes, String name, Date value) {
    if (value == null) {
      return;
    }
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFont value)
      throws IOException {
    if (value == null) {
      return;
    }
    if (fonts.get(value) == null) {
      throw new IOException("Font wasn't collected in the first place");
    }
    attributes.addAttribute("", name, name, CDATA, String.valueOf(fonts.get(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDCharSet value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, String.valueOf(CDXMLUtils.convertCharSetToString(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDColor value)
      throws IOException {
    if (value == null) {
      return;
    }
    if (colors.get(value) == null) {
      throw new IOException("Color wasn't collected in the first place");
    }
    attributes.addAttribute("", name, name, CDATA, String.valueOf(colors.get(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFontFace value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, String.valueOf(CDXUtils.convertFontType(value)));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDJustification value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertTextJustificationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDDrawingSpaceType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertDrawingSpaceTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPageDefinition value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertPageDefinitionToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDLabelDisplay value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertLabelDisplayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDNodeType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertNodeTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRadical value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertRadicalToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRingBondCount value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertRingBondCountToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDUnsaturation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertUnsaturationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDReactionStereo value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertReactionStereoToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDTranslation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertTranslationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDIsotopicAbundance value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertAbundanceToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDExternalConnectionType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertExternalConnectionTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDAtomGeometry value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertAtomGeometryToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDAtomCIPType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertAtomCIPTypeToString(value));
  }

  private void addBondOrderAttribute(AttributesImpl attributes, String name, CDBondOrder value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBondOrderToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondDisplay value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBondDisplayToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondDoublePosition value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertBondDoublePositionToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondTopology value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBondTopologyToString(value));
  }

  private void addAttribute(
      AttributesImpl attributes, String name, CDBondReactionParticipation value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertBondReactionParticipationToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBondCIPType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBondCIPTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGraphicType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertGraphicTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDLineType value) {
    if (value == null || value.isSolid()) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertLineTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowType value)
      throws IOException {
    if (value == null || value == CDArrowType.NoHead) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertArrowTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBracketType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBracketTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDRectangleType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertRectangleTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDOvalType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertOvalTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDOrbitalType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertOrbitalTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSymbolType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSymbolTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDBracketUsage value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertBracketUsageToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPolymerRepeatPattern value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertPolymerRepeatPatternToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPolymerFlipType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertPolymerFlipTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSplineType value) {
    if (value == null || value.isPlain()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, String.valueOf(CDXUtils.convertCurveTypeToInt(value)));
  }

  private void addPoint2DListAttribute(
      AttributesImpl attributes, String name, List<CDPoint2D> value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertPoint2DListToString(value));
  }

  private void addPoint3DListAttribute(
      AttributesImpl attributes, String name, List<CDPoint3D> value) {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertPoint3DListToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDGeometryType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertGeometricFeatureToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDConstraintType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertConstraintTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumXType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSpectrumXTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumYType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSpectrumYTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSpectrumClass value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSpectrumClassToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDObjectTagType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertObjectTagTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDPositioningType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertPositioningTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSideType value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSideTypeToString(value));
  }

  private void addLineHeightAttribute(AttributesImpl attributes, String name, float value) {
    if (value == CDSettings.LineHeight_Variable) {
      // default
      return;
    }

    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertLineHeightToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDSequenceType value)
      throws IOException {
    if (value == null || value == CDSequenceType.Unknown) {
      // default
      return;
    }

    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertSequenceTypeToString(value));
  }

  private void addReferenceAttribute(AttributesImpl attributes, String name, Object value)
      throws IOException {
    if (value == null) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertObjectRefToString(value, references));
  }

  private void addReferenceListAttribute(AttributesImpl attributes, String name, List<?> value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertObjectRefList(value, references));
  }

  private void addReferenceMapAttribute(AttributesImpl attributes, String name, Map<?, ?> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    attributes.addAttribute(
        "", name, name, CDATA, CDXMLUtils.convertObjectRefMapToString(values, references));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowHeadType value)
      throws IOException {
    if (value == null || value == CDArrowHeadType.Solid) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertArrowheadTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDArrowHeadPositionType value)
      throws IOException {
    if (value == null || value == CDArrowHeadPositionType.Unspecified) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertArrowheadToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDFillType value)
      throws IOException {
    if (value == null || value == CDFillType.Unspecified) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertFillTypeToString(value));
  }

  private void addAttribute(AttributesImpl attributes, String name, CDNoGoType value)
      throws IOException {
    if (value == null || value == CDNoGoType.Unspecified) {
      return;
    }
    attributes.addAttribute("", name, name, CDATA, CDXMLUtils.convertNoGoTypeToString(value));
  }
}
