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

import org.beilstein.chemxtract.cdx.datatypes.*;

/** Constants defined by the CDXML file format Specification. */
public class CDXMLConstants {
  static final String DTD = "http://www.cambridgesoft.com/xml/cdxml.dtd";
  static final String DTD2 = "https://static.chemistry.revvitycloud.com/cdxml/CDXML.dtd";
  static final String NS = "";
  static final String CDATA = "CDATA";

  static final String CDXMLObj_Document = "CDXML";
  static final String CDXMLObj_ColorTable = "colortable";
  static final String CDXMLObj_Color = "color";
  static final String CDXMLObj_FontTable = "fonttable";
  static final String CDXMLObj_Font = "font";
  static final String CDXMLObj_Page = "page";
  static final String CDXMLObj_Group = "group";
  static final String CDXMLObj_Fragment = "fragment";
  static final String CDXMLObj_Text = "t";
  static final String CDXMLObj_String = "s";
  static final String CDXMLObj_Node = "n";
  static final String CDXMLObj_Bond = "b";
  static final String CDXMLObj_Graphic = "graphic";
  static final String CDXMLObj_Arrow = "arrow";
  static final String CDXMLObj_Curve = "curve";
  static final String CDXMLObj_NamedAlternativeGroup = "altgroup";
  static final String CDXMLObj_ReactionStep = "step";
  static final String CDXMLObj_ReactionScheme = "scheme";
  static final String CDXMLObj_Geometry = "geometry";
  static final String CDXMLObj_Constraint = "constraint";
  static final String CDXMLObj_TemplateGrid = "templateGrid";
  static final String CDXMLObj_Spectrum = "spectrum";
  static final String CDXMLObj_EmbeddedObject = "embeddedobject";
  static final String CDXMLObj_Represent = "represent";
  static final String CDXMLObj_ObjectTag = "objecttag";
  static final String CDXMLObj_Sequence = "sequence";
  static final String CDXMLObj_CrossReference = "crossreference";
  static final String CDXMLObj_RegistryNumber = "regnum";
  static final String CDXMLObj_Splitter = "splitter";
  static final String CDXMLObj_Table = "table";
  static final String CDXMLObj_TLCPlate = "tlcplate";
  static final String CDXMLObj_TLCLane = "tlclane";
  static final String CDXMLObj_TLCSpot = "tlcspot";
  static final String CDXMLObj_BracketedGroup = "bracketedgroup";
  static final String CDXMLObj_BracketAttachment = "bracketattachment";
  static final String CDXMLObj_CrossingBond = "crossingbond";
  static final String CDXMLObj_Border = "border";
  static final String CDXMLObj_ChemicalProperty = "chemicalproperty";
  static final String CDXMLObj_Bioshape = "bioshape";
  static final String CDXMLObj_ColoredMolecularArea = "ColoredMolecularArea";

  static final String CDXMLProp_Id = "id";
  static final String CDXMLProp_BoundingBox = "BoundingBox";
  static final String CDXMLProp_MacPrintInfo = "MacPrintInfo";
  static final String CDXMLProp_WinPrintInfo = "WinPrintInfo";
  static final String CDXMLProp_PrintMargins = "PrintMargins";
  static final String CDXMLProp_ChainAngle = "ChainAngle";
  static final String CDXMLProp_BondSpacing = "BondSpacing";
  static final String CDXMLProp_BondLength = "BondLength";
  static final String CDXMLProp_BoldWidth = "BoldWidth";
  static final String CDXMLProp_LineWidth = "LineWidth";
  static final String CDXMLProp_MarginWidth = "MarginWidth";
  static final String CDXMLProp_HashSpacing = "HashSpacing";
  static final String CDXMLProp_LabelStyleFont = "LabelFont";
  static final String CDXMLProp_LabelStyleSize = "LabelSize";
  static final String CDXMLProp_LabelStyleFace = "LabelFace";
  static final String CDXMLProp_LabelStyleColor = "LabelColor";
  static final String CDXMLProp_LabelLineHeight = "LabelLineHeight";
  static final String CDXMLProp_LabelJustification = "LabelJustification";
  static final String CDXMLProp_CaptionStyleFont = "CaptionFont";
  static final String CDXMLProp_CaptionStyleSize = "CaptionSize";
  static final String CDXMLProp_CaptionStyleFace = "CaptionFace";
  static final String CDXMLProp_CaptionStyleColor = "CaptionColor";
  static final String CDXMLProp_CaptionLineHeight = "CaptionLineHeight";
  static final String CDXMLProp_CaptionJustification = "CaptionJustification";
  static final String CDXMLProp_FractionalWidths = "FractionalWidths";
  static final String CDXMLProp_InterpretChemically = "InterpretChemically";
  static final String CDXMLProp_Atom_ShowQuery = "ShowAtomQuery";
  static final String CDXMLProp_Atom_ShowStereo = "ShowAtomStereo";
  static final String CDXMLProp_Atom_ShowEnhancedStereo = "ShowAtomEnhancedStereo";
  static final String CDXMLProp_Atom_ShowAtomNumber = "ShowAtomNumber";
  static final String CDXMLProp_Bond_ShowQuery = "ShowBondQuery";
  static final String CDXMLProp_Bond_ShowStereo = "ShowBondStereo";
  static final String CDXMLProp_Bond_ShowRxn = "ShowBondRxn";
  static final String CDXMLProp_ShowTerminalCarbonLabels = "ShowTerminalCarbonLabels";
  static final String CDXMLProp_ShowNonTerminalCarbonLabels = "ShowNonTerminalCarbonLabels";
  static final String CDXMLProp_HideImplicitHydrogens = "HideImplicitHydrogens";
  static final String CDXMLProp_Magnification = "Magnification";
  static final String CDXMLProp_Window_IsZoomed = "WindowIsZoomed";
  static final String CDXMLProp_Window_Position = "WindowPosition";
  static final String CDXMLProp_Window_Size = "WindowSize";
  static final String CDXMLProp_CreationUserName = "CreationUserName";
  static final String CDXMLProp_CreationDate = "CreationDate";
  static final String CDXMLProp_CreationProgram = "CreationProgram";
  static final String CDXMLProp_ModificationUserName = "ModificationUserName";
  static final String CDXMLProp_ModificationDate = "ModificationDate";
  static final String CDXMLProp_ModificationProgram = "ModificationProgram";
  static final String CDXMLProp_Name = "Name";
  static final String CDXMLProp_Comment = "Comment";
  static final String CDXMLProp_CartridgeData = "CartridgeData";
  static final String CDXMLProp_FixInplaceExtent = "FixInPlaceExtent";
  static final String CDXMLProp_FixInplaceGap = "FixInPlaceGap";
  static final String CDXMLProp_Red = "r";
  static final String CDXMLProp_Green = "g";
  static final String CDXMLProp_Blue = "b";
  static final String CDXMLProp_Font_Name = "name";
  static final String CDXMLProp_CharSet = "charset";
  static final String CDXMLProp_BoundsInParent = "BoundsInParent";
  static final String CDXMLProp_BackgroundColor = "bgcolor";
  static final String CDXMLProp_HighlightColor = "highlightColor";
  static final String CDXMLProp_WidthPages = "WidthPages";
  static final String CDXMLProp_HeightPages = "HeightPages";
  static final String CDXMLProp_PrintTrimMarks = "PrintTrimMarks";
  static final String CDXMLProp_Width = "Width";
  static final String CDXMLProp_Height = "Height";
  static final String CDXMLProp_PageOverlap = "PageOverlap";
  static final String CDXMLProp_Header = "Header";
  static final String CDXMLProp_HeaderPosition = "HeaderPosition";
  static final String CDXMLProp_Footer = "Footer";
  static final String CDXMLProp_FooterPosition = "FooterPosition";
  static final String CDXMLProp_DrawingSpaceType = "DrawingSpace";
  static final String CDXMLProp_SplitterPositions = "SplitterPositions";
  static final String CDXMLProp_PageDefinition = "PageDefinition";
  static final String CDXMLProp_Group_Integral = "Integral";
  static final String CDXMLProp_Frag_ConnectionOrder = "ConnectionOrder";
  static final String CDXMLProp_Frag_SequenceType = "SequenceType";
  static final String CDXMLProp_Mole_Racemic = "Racemic";
  static final String CDXMLProp_Mole_Absolute = "Absolute";
  static final String CDXMLProp_Mole_Relative = "Relative";
  static final String CDXMLProp_Mole_Formula = "Formula";
  static final String CDXMLProp_Mole_Weight = "Weight";
  static final String CDXMLProp_2DPosition = "p";
  static final String CDXMLProp_SupercededBy = "SupersededBy";
  static final String CDXMLProp_ForegroundColor = "color";
  static final String CDXMLProp_RotationAngle = "RotationAngle";
  static final String CDXMLProp_ZOrder = "Z";
  static final String CDXMLProp_Justification = "Justification";
  static final String CDXMLProp_LineHeight = "LineHeight";
  static final String CDXMLProp_WordWrapWidth = "WordWrapWidth";
  static final String CDXMLProp_LineStarts = "LineStarts";
  static final String CDXMLProp_LabelAlignment = "LabelAlignment";
  static final String CDXMLProp_IgnoreWarnings = "IgnoreWarnings";
  static final String CDXMLProp_ChemicalWarning = "Warning";
  static final String CDXMLProp_Visible = "Visible";
  static final String CDXMLProp_Font = "font";
  static final String CDXMLProp_FontFace = "face";
  static final String CDXMLProp_FontSize = "size";
  static final String CDXMLProp_NeedsClean = "NeedsClean";

  static final String CDXMLProp_3DPosition = "xyz";
  static final String CDXMLProp_Node_Element = "Element";
  static final String CDXMLProp_Node_LabelDisplay = "LabelDisplay";
  static final String CDXMLProp_Node_Type = "NodeType";
  static final String CDXMLProp_Atom_ElementList = "ElementList";
  static final String CDXMLProp_Atom_Formula = "Formula";
  static final String CDXMLProp_Atom_GenericList = "GenericList";
  static final String CDXMLProp_Atom_Isotope = "Isotope";
  static final String CDXMLProp_Atom_Charge = "Charge";
  static final String CDXMLProp_Atom_Radical = "Radical";
  static final String CDXMLProp_Atom_RestrictFreeSites = "FreeSites";
  static final String CDXMLProp_Atom_RestrictImplicitHydrogens = "ImplicitHydrogens";
  static final String CDXMLProp_Atom_RestrictRingBondCount = "RingBondCount";
  static final String CDXMLProp_Atom_RestrictUnsaturatedBonds = "UnsaturatedBonds";
  static final String CDXMLProp_Atom_RestrictRxnChange = "RxnChange";
  static final String CDXMLProp_Atom_RestrictRxnStereo = "RxnStereo";
  static final String CDXMLProp_Atom_Translation = "Translation";
  static final String CDXMLProp_Atom_IsotopicAbundance = "IsotopicAbundance";
  static final String CDXMLProp_Atom_ExternalConnectionType = "ExternalConnectionType";
  static final String CDXMLProp_Atom_AbnormalValence = "AbnormalValence";
  static final String CDXMLProp_Atom_NumHydrogens = "NumHydrogens";
  static final String CDXMLProp_Atom_HDot = "HDot";
  static final String CDXMLProp_Atom_HDash = "HDash";
  static final String CDXMLProp_Atom_Geometry = "Geometry";
  static final String CDXMLProp_Atom_BondOrdering = "BondOrdering";
  static final String CDXMLProp_Node_Attachments = "Attachments";
  static final String CDXMLProp_Atom_GenericNickname = "GenericNickname";
  static final String CDXMLProp_Atom_AltGroupID = "AltGroupID";
  static final String CDXMLProp_Atom_RestrictSubstituentsUpTo = "SubstituentsUpTo";
  static final String CDXMLProp_Atom_RestrictSubstituentsExactly = "SubstituentsExactly";
  static final String CDXMLProp_Atom_CIPStereochemistry = "AS";
  static final String CDXMLProp_Atom_AtomNumber = "AtomNumber";
  static final String CDXMLProp_Atom_LinkCountLow = "LinkCountLow";
  static final String CDXMLProp_Atom_LinkCountHigh = "LinkCountHigh";

  static final String CDXMLProp_Bond_Order = "Order";
  static final String CDXMLProp_Bond_Display = "Display";
  static final String CDXMLProp_Bond_Display2 = "Display2";
  static final String CDXMLProp_Bond_DoublePosition = "DoublePosition";
  static final String CDXMLProp_Bond_Begin = "B";
  static final String CDXMLProp_Bond_End = "E";
  static final String CDXMLProp_Bond_RestrictTopology = "Topology";
  static final String CDXMLProp_Bond_RestrictRxnParticipation = "RxnParticipation";
  static final String CDXMLProp_Bond_BeginAttach = "BeginAttach";
  static final String CDXMLProp_Bond_EndAttach = "EndAttach";
  static final String CDXMLProp_Bond_CIPStereochemistry = "BS";
  static final String CDXMLProp_Bond_BondOrdering = "BondCircularOrdering";
  static final String CDXMLProp_Bond_CrossingBonds = "CrossingBonds";
  static final String CDXMLProp_BondSpacingAbs = "BondSpacingAbs";
  static final String CDXMLProp_CrossingBondsS = "CrossingBondss";

  static final String CDXMLProp_Graphic_Type = "GraphicType";
  static final String CDXMLProp_Line_Type = "LineType";
  static final String CDXMLProp_Arrow_Type = "ArrowType";
  static final String CDXMLProp_Bracket_Type = "BracketType";
  static final String CDXMLProp_Rectangle_Type = "RectangleType";
  static final String CDXMLProp_Oval_Type = "OvalType";
  static final String CDXMLProp_Orbital_Type = "OrbitalType";
  static final String CDXMLProp_Symbol_Type = "SymbolType";
  static final String CDXMLProp_FrameType = "FrameType";
  static final String CDXMLProp_Arrow_HeadSize = "HeadSize";
  static final String CDXMLProp_Bracket_LipSize = "LipSize";
  static final String CDXMLProp_Arc_AngularSize = "AngularSize";
  static final String CDXMLProp_BracketUsage = "BracketUsage";
  static final String CDXMLProp_Polymer_RepeatPattern = "PolymerRepeatPattern";
  static final String CDXMLProp_Polymer_FlipType = "PolymerFlipType";
  static final String CDXMLProp_Head3D = "Head3D";
  static final String CDXMLProp_Tail3D = "Tail3D";
  static final String CDXMLProp_Center3D = "Center3D";
  static final String CDXMLProp_MajorAxisEnd3D = "MajorAxisEnd3D";
  static final String CDXMLProp_MinorAxisEnd3D = "MinorAxisEnd3D";
  static final String CDXMLProp_ShadowSize = "ShadowSize";
  static final String CDXMLProp_CornerRadius = "CornerRadius";
  static final String CDXMLProp_FadePercent = "FadePercent";

  static final String CDXMLProp_ArrowHeadType = "ArrowheadType";
  static final String CDXMLProp_ArrowHeadHead = "ArrowheadHead";
  static final String CDXMLProp_ArrowHeadTail = "ArrowheadTail";
  static final String CDXMLProp_ArrowHeadCenterSize = "ArrowheadCenterSize";
  static final String CDXMLProp_ArrowHeadWidth = "ArrowheadWidth";
  static final String CDXMLProp_ArrowShaftSpacing = "ArrowShaftSpacing";
  static final String CDXMLProp_ArrowEquilibriumRatio = "ArrowEquilibriumRatio";
  static final String CDXMLProp_NoGo = "NoGo";
  static final String CDXMLProp_Dipole = "Dipole";

  static final String CDXMLProp_Curve_Type = "CurveType";
  static final String CDXMLProp_Curve_Points = "CurvePoints";
  static final String CDXMLProp_Curve_Points3D = "CurvePoints3D";
  static final String CDXMLProp_Curve_FillType = "FillType";
  static final String CDXMLProp_HeaderCenterSize = "HeadCenterSize";
  static final String CDXMLProp_HeadWidth = "HeadWidth";
  static final String CDXMLProp_CurveSpacing = "CurveSpacing";
  static final String CDXMLProp_Closed = "Closed";

  static final String CDXMLProp_NamedAlternativeGroup_TextFrame = "TextFrame";
  static final String CDXMLProp_NamedAlternativeGroup_GroupFrame = "GroupFrame";
  static final String CDXMLProp_NamedAlternativeGroup_Valence = "Valence";

  static final String CDXMLProp_ReactionStep_Reactants = "ReactionStepReactants";
  static final String CDXMLProp_ReactionStep_Products = "ReactionStepProducts";
  static final String CDXMLProp_ReactionStep_Plusses = "ReactionStepPlusses";
  static final String CDXMLProp_ReactionStep_Arrows = "ReactionStepArrows";
  static final String CDXMLProp_ReactionStep_ObjectsAboveArrow = "ReactionStepObjectsAboveArrow";
  static final String CDXMLProp_ReactionStep_ObjectsBelowArrow = "ReactionStepObjectsBelowArrow";
  static final String CDXMLProp_ReactionStep_Atom_Map = "ReactionStepAtomMap";
  static final String CDXMLProp_ReactionStep_Atom_Map_Manual = "ReactionStepAtomMapManual";
  static final String CDXMLProp_ReactionStep_Atom_Map_Auto = "ReactionStepAtomMapAuto";

  static final String CDXMLProp_GeometricFeature = "GeometricFeature";
  static final String CDXMLProp_RelationValue = "RelationValue";
  static final String CDXMLProp_BasisObjects = "BasisObjects";

  static final String CDXMLProp_ConstraintType = "ConstraintType";
  static final String CDXMLProp_ConstraintMin = "ConstraintMin";
  static final String CDXMLProp_ConstraintMax = "ConstraintMax";
  static final String CDXMLProp_IgnoreUnconnectedAtoms = "IgnoreUnconnectedAtoms";
  static final String CDXMLProp_DihedralIsChiral = "DihedralIsChiral";
  static final String CDXMLProp_PointIsDirected = "PointIsDirected";

  static final String CDXMLProp_2DExtent = "extent";
  static final String CDXMLProp_Template_PaneHeight = "PaneHeight";
  static final String CDXMLProp_Template_NumRows = "NumRows";
  static final String CDXMLProp_Template_NumColumns = "NumColumns";

  static final String CDXMLProp_Spectrum_XSpacing = "XSpacing";
  static final String CDXMLProp_Spectrum_XLow = "XLow";
  static final String CDXMLProp_Spectrum_XType = "XType";
  static final String CDXMLProp_Spectrum_YType = "YType";
  static final String CDXMLProp_Spectrum_Class = "Class";
  static final String CDXMLProp_Spectrum_XAxisLabel = "XAxisLabel";
  static final String CDXMLProp_Spectrum_YAxisLabel = "YAxisLabel";
  static final String CDXMLProp_Spectrum_YLow = "YLow";
  static final String CDXMLProp_Spectrum_YScale = "YScale";

  static final String CDXMLProp_Picture_Edition = "Edition";
  static final String CDXMLProp_Picture_EditionAlias = "EditionAlias";
  static final String CDXMLProp_MacPICT = "MacPICT";
  static final String CDXMLProp_WindowsMetafile = "WindowsMetafile";
  static final String CDXMLProp_OLEObject = "OLEObject";
  static final String CDXMLProp_EnhancedMetafile = "EnhancedMetafile";
  static final String CDXMLProp_CompressedWindowsMetafile = "CompressedWindowsMetafile";
  static final String CDXMLProp_CompressedOLEObject = "CompressedOLEObject";
  static final String CDXMLProp_CompressedEnhancedMetafile = "CompressedEnhancedMetafile";
  static final String CDXMLProp_UncompressedWindowsMetafileSize = "UncompressedWindowsMetafileSize";
  static final String CDXMLProp_UncompressedOLEObjectSize = "UncompressedOLEObjectSize";
  static final String CDXMLProp_UncompressedEnhancedMetafileSize =
      "UncompressedEnhancedMetafileSize";
  static final String CDXMLProp_GIF = "GIF";
  static final String CDXMLProp_TIFF = "TIFF";
  static final String CDXMLProp_PNG = "PNG";
  static final String CDXMLProp_JPEG = "JPEG";
  static final String CDXMLProp_BMP = "BMP";

  static final String CDXMLProp_Attribute = "attribute";
  static final String CDXMLProp_Object = "object";

  static final String CDXMLProp_ObjectTag_Type = "TagType";
  static final String CDXMLProp_DisplayName = "DisplayName";
  static final String CDXMLProp_ObjectTag_Tracking = "Tracking";
  static final String CDXMLProp_ObjectTag_Persistent = "Persistent";
  static final String CDXMLProp_ObjectTag_Value = "Value";
  static final String CDXMLProp_Positioning = "PositioningType";
  static final String CDXMLProp_PositioningAngle = "PositioningAngle";
  static final String CDXMLProp_PositioningOffset = "PositioningOffset";

  static final String CDXMLProp_Sequence_Identifier = "SequenceIdentifier";

  static final String CDXMLProp_CrossReference_Container = "CrossReferenceContainer";
  static final String CDXMLProp_CrossReference_Document = "CrossReferenceDocument";
  static final String CDXMLProp_CrossReference_Identifier = "CrossReferenceIdentifier";
  static final String CDXMLProp_CrossReference_Sequence = "CrossReferenceSequence";

  static final String CDXMLProp_RegistryNumber = "RegistryNumber";
  static final String CDXMLProp_RegistryAuthority = "RegistryAuthority";

  static final String CDXMLProp_TopLeft = "TopLeft";
  static final String CDXMLProp_TopRight = "TopRight";
  static final String CDXMLProp_BottomRight = "BottomRight";
  static final String CDXMLProp_BottomLeft = "BottomLeft";
  static final String CDXMLProp_TLC_OriginFraction = "OriginFraction";
  static final String CDXMLProp_TLC_SolventFrontFraction = "SolventFrontFraction";
  static final String CDXMLProp_TLC_ShowOrigin = "ShowOrigin";
  static final String CDXMLProp_TLC_ShowSolventFront = "ShowSolventFront";
  static final String CDXMLProp_TLC_ShowBorders = "ShowBorders";
  static final String CDXMLProp_ShowSideTicks = "ShowSideTicks";
  static final String CDXMLProp_Transparent = "Transparent";

  static final String CDXMLProp_TLC_Tail = "Tail";
  static final String CDXMLProp_TLC_Rf = "Rf";
  static final String CDXMLProp_TLC_ShowRf = "ShowRf";

  static final String CDXMLProp_Bracket_Usage = "BracketUsage";
  static final String CDXMLProp_BracketedObjects = "BracketedObjectIDs";
  static final String CDXMLProp_Bracket_RepeatCount = "RepeatCount";
  static final String CDXMLProp_Bracket_ComponentOrder = "ComponentOrder";
  static final String CDXMLProp_Bracket_SRULabel = "SRULabel";

  static final String CDXMLProp_Bracket_GraphicID = "GraphicID";

  static final String CDXMLProp_Bracket_BondID = "BondID";
  static final String CDXMLProp_Bracket_InnerAtomID = "InnerAtomID";

  static final String CDXMLProp_Side = "Side";

  static final String CDXMLProp_ChemicalPropertyType = "ChemicalPropertyType";
  static final String CDXMLProp_ChemicalPropertyDisplayID = "ChemicalPropertyDisplayID";
  static final String CDXMLProp_ChemicalPropertyIsActive = "ChemicalPropertyIsActive";

  static final String CDXMLProp_BioShapeType = "BioShapeType";
  static final String CDXMLProp_EnzymeWidth = "EnzymeWidth";
  static final String CDXMLProp_EnzymeHeight = "EnzymeHeight";
  static final String CDXMLProp_NeckWidth = "NeckWidth";
  static final String CDXMLProp_NeckHeight = "NeckHeight";
  static final String CDXMLProp_CylinderWidth = "CylinderWidth";
  static final String CDXMLProp_CylinderHeight = "CylinderHeight";
  static final String CDXMLProp_CylinderDistance = "CylinderDistance";
  static final String CDXMLProp_PipeWidth = "PipeWidth";
  static final String CDXMLProp_HelixProteinExtra = "HelixProteinExtra";
  static final String CDXMLProp_ImmunoglobinHeight = "ImmunoglobinHeight";
  static final String CDXMLProp_ImmunoglobinWidth = "ImmunoglobinWidth";
  static final String CDXMLProp_MembraneElementSize = "MembraneElementSize";
  static final String CDXMLProp_MembraneMajorAxisSize = "MembraneMajorAxisSize";
  static final String CDXMLProp_MembraneMinorAxisSize = "MembraneMinorAxisSize";
  static final String CDXMLProp_MembraneStartAngle = "MembraneStartAngle";
  static final String CDXMLProp_MembraneEndAngle = "MembraneEndAngle";
  static final String CDXMLProp_DNAWaveLength = "DNAWaveLength";
  static final String CDXMLProp_DNAWaveWidth = "DNAWaveWidth";
  static final String CDXMLProp_DNAWaveOffset = "DNAWaveOffset";
  static final String CDXMLProp_DNAWaveHeight = "DNAWaveHeight";
  static final String CDXMLProp_GProteinUpperHeight = "GproteinUpperHeight";
  static final String CDXMLProp_GProteinLowerHeight = "GproteinLowerHeight";
  static final String CDXMLProp_GolgiLength = "GolgiLength";
  static final String CDXMLProp_GolgiHeight = "GolgiHeight";
  static final String CDXMLProp_GolgiWidth = "GolgiWidth";

  // enum CDXOvalType

  /** Circle */
  static final String CDXMLOvalType_Circle = "Circle";

  /** Shaded */
  static final String CDXMLOvalType_Shaded = "Shaded";

  /** Filled */
  static final String CDXMLOvalType_Filled = "Filled";

  /** Dashed */
  static final String CDXMLOvalType_Dashed = "Dashed";

  /** Bold */
  static final String CDXMLOvalType_Bold = "Bold";

  /** Shadowed */
  static final String CDXMLOvalType_Shadowed = "Shadowed";

  // enum CDXRectangleType

  /** Plain rectangle. */
  static final String CDXRectangleType_Plain = "Plain";

  /** Round-edge rectangle. */
  static final String CDXRectangleType_RoundEdge = "RoundEdge";

  /** Shadow rectangle. */
  static final String CDXRectangleType_Shadow = "Shadow";

  /** Shaded rectangle. */
  static final String CDXRectangleType_Shaded = "Shaded";

  /** Filled rectangle. */
  static final String CDXRectangleType_Filled = "Filled";

  /** Dashed rectangle. */
  static final String CDXRectangleType_Dashed = "Dashed";

  /** Bold rectangle. */
  static final String CDXRectangleType_Bold = "Bold";

  /** Solid line type */
  static final String CDXLineType_Solid = "Solid";

  /** Dashed line type */
  static final String CDXLineType_Dashed = "Dashed";

  /** Bold line type */
  static final String CDXLineType_Bold = "Bold";

  /** Wavy line type */
  static final String CDXLineType_Wavy = "Wavy";

  static final Object[][] CDXMLTextJustification =
      new Object[][] {
        {CDJustification.Right, "Right"},
        {CDJustification.Left, "Left"},
        {CDJustification.Center, "Center"},
        {CDJustification.Full, "Full"},
        {CDJustification.Above, "Above"},
        {CDJustification.Below, "Below"},
        {CDJustification.Auto, "Auto"},
        {CDJustification.BestInitial, "Best"}
      };

  static final Object[][] CDXMLDrawingSpaceType =
      new Object[][] {{CDDrawingSpaceType.Pages, "pages"}, {CDDrawingSpaceType.Poster, "poster"}};

  static final Object[][] CDXMLPageDefinition =
      new Object[][] {
        {CDPageDefinition.Undefined, "Undefined"},
        {CDPageDefinition.Center, "Center"},
        {CDPageDefinition.TL4, "TL4"},
        {CDPageDefinition.IDTerm, "IDTerm"},
        {CDPageDefinition.FlushLeft, "FlushLeft"},
        {CDPageDefinition.FlushRight, "FlushRight"},
        {CDPageDefinition.Reaction1, "Reaction1"},
        {CDPageDefinition.Reaction2, "Reaction2"},
        {CDPageDefinition.MulticolumnTL4, "MulticolumnTL4"},
        {CDPageDefinition.MulticolumnNonTL4, "MulticolumnNonTL4"},
        {CDPageDefinition.UserDefined, "UserDefined"}
      };

  static final Object[][] CDXMLLabelDisplay =
      new Object[][] {
        {CDLabelDisplay.Auto, "Auto"},
        {CDLabelDisplay.Left, "Left"},
        {CDLabelDisplay.Center, "Center"},
        {CDLabelDisplay.Right, "Right"},
        {CDLabelDisplay.Above, "Above"},
        {CDLabelDisplay.Below, "Below"},
        {CDLabelDisplay.BestInitial, "Best"}
      };

  static final Object[][] CDXMLNodeType =
      new Object[][] {
        {CDNodeType.Unspecified, "Unspecified"},
        {CDNodeType.Element, "Element"},
        {CDNodeType.ElementList, "ElementList"},
        {CDNodeType.ElementListNickname, "ElementListNickname"},
        {CDNodeType.Nickname, "Nickname"},
        {CDNodeType.Fragment, "Fragment"},
        {CDNodeType.Formula, "Formula"},
        {CDNodeType.GenericNickname, "GenericNickname"},
        {CDNodeType.AnonymousAlternativeGroup, "AnonymousAlternativeGroup"},
        {CDNodeType.NamedAlternativeGroup, "NamedAlternativeGroup"},
        {CDNodeType.MultiAttachment, "MultiAttachment"},
        {CDNodeType.VariableAttachment, "VariableAttachment"},
        {CDNodeType.ExternalConnectionPoint, "ExternalConnectionPoint"},
        {CDNodeType.LinkNode, null /* TODO check */}
      };

  static final Object[][] CDXMLRadical =
      new Object[][] {
        {CDRadical.None, "None"},
        {CDRadical.Singlet, "Singlet"},
        {CDRadical.Doublet, "Doublet"},
        {CDRadical.Triplet, "Triplet"}
      };

  static final Object[][] CDXMLRingBondCount =
      new Object[][] {
        {CDRingBondCount.Unspecified, "Unspecified"},
        {CDRingBondCount.NoRingBonds, "NoRingBonds"},
        {CDRingBondCount.AsDrawn, "AsDrawn"},
        {CDRingBondCount.SimpleRing, "SimpleRing"},
        {CDRingBondCount.Fusion, "Fusion"},
        {CDRingBondCount.SpiroOrHigher, "SpiroOrHigher"}
      };

  static final Object[][] CDXMLUnsaturation =
      new Object[][] {
        {CDUnsaturation.Unspecified, "Unspecified"},
        {CDUnsaturation.MustBeAbsent, "MustBeAbsent"},
        {CDUnsaturation.MustBePresent, "MustBePresent"}
      };

  static final Object[][] CDXMLReactionStereo =
      new Object[][] {
        {CDReactionStereo.Unspecified, "Unspecified"},
        {CDReactionStereo.Inversion, "Inversion"},
        {CDReactionStereo.Retention, "Retention"}
      };

  static final Object[][] CDXMLTranslation =
      new Object[][] {
        {CDTranslation.Equal, "Equal"},
        {CDTranslation.Broad, "Broad"},
        {CDTranslation.Narrow, "Narrow"},
        {CDTranslation.Any, "Any"}
      };

  static final Object[][] CDXMLAbundance =
      new Object[][] {
        {CDIsotopicAbundance.Unspecified, "Unspecified"},
        {CDIsotopicAbundance.Any, "Any"},
        {CDIsotopicAbundance.Natural, "Natural"},
        {CDIsotopicAbundance.Enriched, "Enriched"},
        {CDIsotopicAbundance.Deficient, "Deficient"},
        {CDIsotopicAbundance.Nonnatural, "Nonnatural"}
      };

  static final Object[][] CDXMLExternalConnectionType =
      new Object[][] {
        {CDExternalConnectionType.Unspecified, "Unspecified"},
        {CDExternalConnectionType.Diamond, "Diamond"},
        {CDExternalConnectionType.Star, "Star"},
        {CDExternalConnectionType.PolymerBead, "PolymerBead"},
        {CDExternalConnectionType.Wavy, "Wavy"},
        {CDExternalConnectionType.Residue, "Residue"}
      };

  static final Object[][] CDXMLAtomGeometry =
      new Object[][] {
        {CDAtomGeometry.Unknown, "Unknown"},
        {CDAtomGeometry.OneLigand, "1"},
        {CDAtomGeometry.Linear, "Linear"},
        {CDAtomGeometry.Bent, "Bent"},
        {CDAtomGeometry.TrigonalPlanar, "TrigonalPlanar"},
        {CDAtomGeometry.TrigonalPyramidal, "TrigonalPyramidal"},
        {CDAtomGeometry.SquarePlanar, "SquarePlanar"},
        {CDAtomGeometry.Tetrahedral, "Tetrahedral"},
        {CDAtomGeometry.TrigonalBipyramidal, "TrigonalBipyramidal"},
        {CDAtomGeometry.SquarePyramidal, "SquarePyramidal"},
        {CDAtomGeometry.FiveLigand, "5"},
        {CDAtomGeometry.Octahedral, "Octahedral"},
        {CDAtomGeometry.SixLigand, "6"},
        {CDAtomGeometry.SevenLigand, "7"},
        {CDAtomGeometry.EightLigand, "8"},
        {CDAtomGeometry.NineLigand, "9"},
        {CDAtomGeometry.TenLigand, "10"}
      };

  static final Object[][] CDXMLAtomCIPType =
      new Object[][] {
        {CDAtomCIPType.Undetermined, "U"},
        {CDAtomCIPType.None, "N"},
        {CDAtomCIPType.R, "R"},
        {CDAtomCIPType.S, "S"},
        {CDAtomCIPType.PseudoR, "r"},
        {CDAtomCIPType.PseudoS, "s"},
        {CDAtomCIPType.Unspecified, "u"}
      };

  static final Object[][] CDXMLBondOrder =
      new Object[][] {
        {CDBondOrder.Single, "1"},
        {CDBondOrder.Double, "2"},
        {CDBondOrder.Triple, "3"},
        {CDBondOrder.Quadruple, "4"},
        {CDBondOrder.Quintuple, "5"},
        {CDBondOrder.Sextuple, "6"},
        {CDBondOrder.Half, "0.5"},
        {CDBondOrder.OneHalf, "1.5"},
        {CDBondOrder.TwoHalf, "2.5"},
        {CDBondOrder.ThreeHalf, "3.5"},
        {CDBondOrder.FourHalf, "4.5"},
        {CDBondOrder.FiveHalf, "5.5"},
        {CDBondOrder.Dative, "dative"},
        {CDBondOrder.Ionic, "ionic"},
        {CDBondOrder.Hydrogen, "hydrogen"},
        {CDBondOrder.ThreeCenter, "threecenter"},
        {CDBondOrder.SingleOrDouble, "1 2"},
        {CDBondOrder.SingleOrAromatic, "1 1.5"},
        {CDBondOrder.DoubleOrAromatic, "2 1.5"},
        {CDBondOrder.Any, "any"}
      };

  static final Object[][] CDXMLBondDisplay =
      new Object[][] {
        {CDBondDisplay.Solid, "Solid"},
        {CDBondDisplay.Dash, "Dash"},
        {CDBondDisplay.Hash, "Hash"},
        {CDBondDisplay.WedgedHashBegin, "WedgedHashBegin"},
        {CDBondDisplay.WedgedHashEnd, "WedgedHashEnd"},
        {CDBondDisplay.Bold, "Bold"},
        {CDBondDisplay.WedgeBegin, "WedgeBegin"},
        {CDBondDisplay.WedgeEnd, "WedgeEnd"},
        {CDBondDisplay.Wavy, "Wavy"},
        {CDBondDisplay.HollowWedgeBegin, "HollowWedgeBegin"},
        {CDBondDisplay.HollowWedgeEnd, "HollowWedgeEnd"},
        {CDBondDisplay.WavyWedgeBegin, "WavyWedgeBegin"},
        {CDBondDisplay.WavyWedgeEnd, "WavyWedgeEnd"},
        {CDBondDisplay.Dot, "Dot"},
        {CDBondDisplay.DashDot, "DashDot"}
      };

  static final Object[][] CDXMLBondDoublePosition =
      new Object[][] {
        {CDBondDoublePosition.AutoCenter, "Center"},
        {CDBondDoublePosition.AutoRight, "Right"},
        {CDBondDoublePosition.AutoLeft, "Left"},
        {CDBondDoublePosition.UserCenter, "Center"},
        {CDBondDoublePosition.UserRight, "Right"},
        {CDBondDoublePosition.UserLeft, "Left"}
      };

  static final Object[][] CDXMLBondTopology =
      new Object[][] {
        {CDBondTopology.Unspecified, "Unspecified"},
        {CDBondTopology.Ring, "Ring"},
        {CDBondTopology.Chain, "Chain"},
        {CDBondTopology.RingOrChain, "RingOrChain"}
      };

  static final Object[][] CDXMLBondReactionParticipation =
      new Object[][] {
        {CDBondReactionParticipation.Unspecified, "Unspecified"},
        {CDBondReactionParticipation.ReactionCenter, "ReactionCenter"},
        {CDBondReactionParticipation.MakeOrBreak, "MakeOrBreak"},
        {CDBondReactionParticipation.ChangeType, "ChangeType"},
        {CDBondReactionParticipation.MakeAndChange, "MakeAndChange"},
        {CDBondReactionParticipation.NotReactionCenter, "NotReactionCenter"},
        {CDBondReactionParticipation.NoChange, "NoChange"},
        {CDBondReactionParticipation.Unmapped, "Unmapped"}
      };

  static final Object[][] CDXMLBondCIPType =
      new Object[][] {
        {CDBondCIPType.Undetermined, "U"},
        {CDBondCIPType.None, "N"},
        {CDBondCIPType.E, "E"},
        {CDBondCIPType.Z, "Z"}
      };

  static final Object[][] CDXMLGraphicType =
      new Object[][] {
        {CDGraphicType.Undefined, "Undefined"},
        {CDGraphicType.Line, "Line"},
        {CDGraphicType.Arc, "Arc"},
        {CDGraphicType.Rectangle, "Rectangle"},
        {CDGraphicType.Oval, "Oval"},
        {CDGraphicType.Orbital, "Orbital"},
        {CDGraphicType.Bracket, "Bracket"},
        {CDGraphicType.Symbol, "Symbol"}
      };

  static final Object[][] CDXMLArrowType =
      new Object[][] {
        {CDArrowType.NoHead, "NoHead"},
        {CDArrowType.HalfHead, "HalfHead"},
        {CDArrowType.FullHead, "FullHead"},
        {CDArrowType.Resonance, "Resonance"},
        {CDArrowType.Equilibrium, "Equilibrium"},
        {CDArrowType.Hollow, "Hollow"},
        {CDArrowType.RetroSynthetic, "RetroSynthetic"},
        {CDArrowType.NoGo, "NoGo"},
        {CDArrowType.Dipole, "Dipole"}
      };

  static final Object[][] CDXMLBracketType =
      new Object[][] {
        {CDBracketType.RoundPair, "RoundPair"},
        {CDBracketType.SquarePair, "SquarePair"},
        {CDBracketType.CurlyPair, "CurlyPair"},
        {CDBracketType.Square, "Square"},
        {CDBracketType.Curly, "Curly"},
        {CDBracketType.Round, "Round"}
      };

  static final Object[][] CDXMLOrbitalType =
      new Object[][] {
        {CDOrbitalType.s, "s"},
        {CDOrbitalType.oval, "oval"},
        {CDOrbitalType.lobe, "lobe"},
        {CDOrbitalType.p, "p"},
        {CDOrbitalType.hybridPlus, "hybridPlus"},
        {CDOrbitalType.hybridMinus, "hybridMinus"},
        {CDOrbitalType.dz2Plus, "dz2Plus"},
        {CDOrbitalType.dz2Minus, "dz2Minus"},
        {CDOrbitalType.dxy, "dxy"},
        {CDOrbitalType.sShaded, "sShaded"},
        {CDOrbitalType.ovalShaded, "ovalShaded"},
        {CDOrbitalType.lobeShaded, "lobeShaded"},
        {CDOrbitalType.pShaded, "pShaded"},
        {CDOrbitalType.sFilled, "sFilled"},
        {CDOrbitalType.ovalFilled, "ovalFilled"},
        {CDOrbitalType.lobeFilled, "lobeFilled"},
        {CDOrbitalType.pFilled, "pFilled"},
        {CDOrbitalType.hybridPlusFilled, "hybridPlusFilled"},
        {CDOrbitalType.hybridMinusFilled, "hybridMinusFilled"},
        {CDOrbitalType.dz2PlusFilled, "dz2PlusFilled"},
        {CDOrbitalType.dz2MinusFilled, "dz2MinusFilled"},
        {CDOrbitalType.dxyFilled, "dxyFilled"}
      };

  static final Object[][] CDXMLSymbolType =
      new Object[][] {
        {CDSymbolType.LonePair, "LonePair"},
        {CDSymbolType.Electron, "Electron"},
        {CDSymbolType.RadicalCation, "RadicalCation"},
        {CDSymbolType.RadicalAnion, "RadicalAnion"},
        {CDSymbolType.CirclePlus, "CirclePlus"},
        {CDSymbolType.CircleMinus, "CircleMinus"},
        {CDSymbolType.Dagger, "Dagger"},
        {CDSymbolType.DoubleDagger, "DoubleDagger"},
        {CDSymbolType.Plus, "Plus"},
        {CDSymbolType.Minus, "Minus"},
        {CDSymbolType.Racemic, "Racemic"},
        {CDSymbolType.Absolute, "Absolute"},
        {CDSymbolType.Relative, "Relative"},
        {CDSymbolType.LonePairBar, "LonePairBar"}
      };

  static final Object[][] CDXMLBracketUsage =
      new Object[][] {
        {CDBracketUsage.Unspecified, "Unspecified"},
        {CDBracketUsage.Anypolymer, "Anypolymer"},
        {CDBracketUsage.Component, "Component"},
        {CDBracketUsage.Copolymer, "Copolymer"},
        {CDBracketUsage.CopolymerAlternating, "CopolymerAlternating"},
        {CDBracketUsage.CopolymerBlock, "CopolymerBlock"},
        {CDBracketUsage.CopolymerRandom, "CopolymerRandom"},
        {CDBracketUsage.Crosslink, "Crosslink"},
        {CDBracketUsage.Generic, "Generic"},
        {CDBracketUsage.Graft, "Graft"},
        {CDBracketUsage.Mer, "Mer"},
        {CDBracketUsage.MixtureOrdered, "MixtureOrdered"},
        {CDBracketUsage.MixtureUnordered, "MixtureUnordered"},
        {CDBracketUsage.Modification, "Modification"},
        {CDBracketUsage.Monomer, "Monomer"},
        {CDBracketUsage.MultipleGroup, "MultipleGroup"},
        {CDBracketUsage.SRU, "SRU"},
        {CDBracketUsage.Unused1, null},
        {CDBracketUsage.Unused2, null}
      };

  static final Object[][] CDXMLPolymerRepeatPattern =
      new Object[][] {
        {CDPolymerRepeatPattern.HeadToTail, "HeadToTail"},
        {CDPolymerRepeatPattern.HeadToHead, "HeadToHead"},
        {CDPolymerRepeatPattern.EitherUnknown, "EitherUnknown"}
      };

  static final Object[][] CDXMLPolymerFlipType =
      new Object[][] {
        {CDPolymerFlipType.Unspecified, "Unspecified"},
        {CDPolymerFlipType.NoFlip, "NoFlip"},
        {CDPolymerFlipType.Flip, "Flip"}
      };

  static final Object[][] CDXMLGeometricFeature =
      new Object[][] {
        {CDGeometryType.Undefined, "Unknown"},
        {CDGeometryType.PointFromPointPointDistance, "PointFromPointPointDistance"},
        {CDGeometryType.PointFromPointPointPercentage, "PointFromPointPointPercentage"},
        {CDGeometryType.PointFromPointNormalDistance, "PointFromPointNormalDistance"},
        {CDGeometryType.LineFromPoints, "LineFromPoints"},
        {CDGeometryType.PlaneFromPoints, "PlaneFromPoints"},
        {CDGeometryType.PlaneFromPointLine, "PlaneFromPointLine"},
        {CDGeometryType.CentroidFromPoints, "CentroidFromPoints"},
        {CDGeometryType.NormalFromPointPlane, "NormalFromPointPlane"}
      };

  static final Object[][] CDXMLConstraintType =
      new Object[][] {
        {CDConstraintType.Undefined, "Unknown"},
        {CDConstraintType.Distance, "Distance"},
        {CDConstraintType.Angle, "Angle"},
        {CDConstraintType.ExclusionSphere, "ExclusionSphere"}
      };

  static final Object[][] CDXMLSpectrumXType =
      new Object[][] {
        {CDSpectrumXType.Unknown, "Unknown"},
        {CDSpectrumXType.Wavenumbers, "Wavenumbers"},
        {CDSpectrumXType.Microns, "Microns"},
        {CDSpectrumXType.Hertz, "Hertz"},
        {CDSpectrumXType.MassUnits, "MassUnits"},
        {CDSpectrumXType.PartsPerMillion, "PartsPerMillion"},
        {CDSpectrumXType.Other, "Other"}
      };

  static final Object[][] CDXMLSpectrumYType =
      new Object[][] {
        {CDSpectrumYType.Unknown, "Unknown"},
        {CDSpectrumYType.Absorbance, "Absorbance"},
        {CDSpectrumYType.Transmittance, "Transmittance"},
        {CDSpectrumYType.PercentTransmittance, "PercentTransmittance"},
        {CDSpectrumYType.Other, "Other"},
        {CDSpectrumYType.ArbitraryUnits, "ArbitraryUnits"}
      };

  static final Object[][] CDXMLSpectrumClass =
      new Object[][] {
        {CDSpectrumClass.Unknown, "Unknown"},
        {CDSpectrumClass.Chromatogram, "Chromatogram"},
        {CDSpectrumClass.Infrared, "Infrared"},
        {CDSpectrumClass.UVVis, "UVVis"},
        {CDSpectrumClass.XRayDiffraction, "XRayDiffraction"},
        {CDSpectrumClass.MassSpectrum, "MassSpectrum"},
        {CDSpectrumClass.NMR, "NMR"},
        {CDSpectrumClass.Raman, "Raman"},
        {CDSpectrumClass.Fluorescence, "Fluorescence"},
        {CDSpectrumClass.Atomic, "Atomic"}
      };

  static final Object[][] CDXMLObjectTagType =
      new Object[][] {
        {CDObjectTagType.Undefined, "Unknown"},
        {CDObjectTagType.Double, "Double"},
        {CDObjectTagType.Long, "Long"},
        {CDObjectTagType.String, "String"}
      };

  static final Object[][] CDXMLPositioningType =
      new Object[][] {
        {CDPositioningType.Auto, "auto"},
        {CDPositioningType.Angle, "angle"},
        {CDPositioningType.Offset, "offset"},
        {CDPositioningType.Absolute, "absolute"}
      };

  static final Object[][] CDXMLSideType =
      new Object[][] {
        {CDSideType.Undefined, "undefined"},
        {CDSideType.Top, "top"},
        {CDSideType.Left, "left"},
        {CDSideType.Bottom, "bottom"},
        {CDSideType.Right, "right"}
      };

  static final Object[][] CDXMLSequenceType =
      new Object[][] {
        {CDSequenceType.Unknown, "Unknown"},
        {CDSequenceType.Peptide1, "Peptide1"},
        {CDSequenceType.Peptide3, "Peptide3"},
        {CDSequenceType.DNA, "DNA"},
        {CDSequenceType.RNA, "RNA"}
      };

  static final Object[][] CDXMLCharSet =
      new Object[][] {
        {CDCharSet.Unknown, "Unknown"},
        {CDCharSet.EBCDICOEM, "EBCDICOEM"},
        {CDCharSet.MSDOSUS, "MSDOSUS"},
        {CDCharSet.EBCDIC500V1, "EBCDIC500V1"},
        {CDCharSet.ArabicASMO708, "ASMO-708"},
        {CDCharSet.ArabicASMO449P, "ArabicASMO449P"},
        {CDCharSet.ArabicTransparent, "ArabicTransparent"},
        {CDCharSet.ArabicTransparentASMO, "DOS-720"},
        {CDCharSet.Greek437G, "Greek437G"},
        {CDCharSet.BalticOEM, "cp775"},
        {CDCharSet.MSDOSLatin1, "windows-850"},
        {CDCharSet.MSDOSLatin2, "ibm852"},
        {CDCharSet.IBMCyrillic, "cp855"},
        {CDCharSet.IBMTurkish, "cp857"},
        {CDCharSet.MSDOSPortuguese, "cp860"},
        {CDCharSet.MSDOSIcelandic, "cp861"},
        {CDCharSet.HebrewOEM, "DOS-862"},
        {CDCharSet.MSDOSCanadianFrench, "cp863"},
        {CDCharSet.ArabicOEM, "cp864"},
        {CDCharSet.MSDOSNordic, "cp865"},
        {CDCharSet.MSDOSRussian, "cp866"},
        {CDCharSet.IBMModernGreek, "cp869"},
        {CDCharSet.Thai, "windows-874"},
        {CDCharSet.EBCDIC, "EBCDIC"},
        {CDCharSet.Japanese, "shift_jis"},
        {CDCharSet.ChineseSimplified, "gb2312"},
        {CDCharSet.Korean, "ks_c_5601-1987"},
        {CDCharSet.ChineseTraditional, "big5"},
        {CDCharSet.UnicodeISO10646, "iso-10646"},
        {CDCharSet.Win31EasternEuropean, "windows-1250"},
        {CDCharSet.Win31Cyrillic, "windows-1251"},
        {CDCharSet.Win31Latin1, "iso-8859-1"},
        {CDCharSet.Win31Greek, "iso-8859-7"},
        {CDCharSet.Win31Turkish, "iso-8859-9"},
        {CDCharSet.Hebrew, "windows-1255"},
        {CDCharSet.Arabic, "windows-1256"},
        {CDCharSet.Baltic, "windows-1257"},
        {CDCharSet.Vietnamese, "windows-1258"},
        {CDCharSet.KoreanJohab, "windows-1361"},
        {CDCharSet.MacRoman, "x-mac-roman"},
        {CDCharSet.MacJapanese, "x-mac-japanese"},
        {CDCharSet.MacTradChinese, "x-mac-tradchinese"},
        {CDCharSet.MacKorean, "x-mac-korean"},
        {CDCharSet.MacArabic, "x-mac-arabic"},
        {CDCharSet.MacHebrew, "x-mac-hebrew"},
        {CDCharSet.MacGreek, "x-mac-greek"},
        {CDCharSet.MacCyrillic, "x-mac-cyrillic"},
        {CDCharSet.MacReserved, "x-mac-reserved"},
        {CDCharSet.MacDevanagari, "x-mac-devanagari"},
        {CDCharSet.MacGurmukhi, "x-mac-gurmukhi"},
        {CDCharSet.MacGujarati, "x-mac-gujarati"},
        {CDCharSet.MacOriya, "x-mac-oriya"},
        {CDCharSet.MacBengali, "x-mac-nengali"},
        {CDCharSet.MacTamil, "x-mac-tamil"},
        {CDCharSet.MacTelugu, "x-mac-telugu"},
        {CDCharSet.MacKannada, "x-mac-kannada"},
        {CDCharSet.MacMalayalam, "x-mac-Malayalam"},
        {CDCharSet.MacSinhalese, "x-mac-sinhalese"},
        {CDCharSet.MacBurmese, "x-mac-burmese"},
        {CDCharSet.MacKhmer, "x-mac-khmer"},
        {CDCharSet.MacThai, "x-mac-thai"},
        {CDCharSet.MacLao, "x-mac-lao"},
        {CDCharSet.MacGeorgian, "x-mac-georgian"},
        {CDCharSet.MacArmenian, "x-mac-armenian"},
        {CDCharSet.MacSimpChinese, "x-mac-simpChinese"},
        {CDCharSet.MacTibetan, "x-mac-tibetan"},
        {CDCharSet.MacMongolian, "x-mac-mongolian"},
        {CDCharSet.MacEthiopic, "x-mac-ethiopic"},
        {CDCharSet.MacCentralEuroRoman, "x-mac-ce"},
        {CDCharSet.MacVietnamese, "x-mac-vietnamese"},
        {CDCharSet.MacExtArabic, "x-mac-extArabic"},
        {CDCharSet.MacUninterpreted, "x-mac-uninterpreted"},
        {CDCharSet.MacIcelandic, "x-mac-icelandic"},
        {CDCharSet.MacTurkish, "x-mac-turkish"}
      };

  static final Object[][] CDXMLArrowheadType =
      new Object[][] {
        {CDArrowHeadType.Solid, "Solid"},
        {CDArrowHeadType.Hollow, "Hollow"},
        {CDArrowHeadType.Angle, "Angle"}
      };

  static final Object[][] CDXMLArrowhead =
      new Object[][] {
        {CDArrowHeadPositionType.Unspecified, "Unspecified"},
        {CDArrowHeadPositionType.None, "None"},
        {CDArrowHeadPositionType.Full, "Full"},
        {CDArrowHeadPositionType.HalfLeft, "HalfLeft"},
        {CDArrowHeadPositionType.HalfRight, "HalfRight"}
      };

  static final Object[][] CDXMLFillType =
      new Object[][] {
        {CDFillType.Unspecified, "Unspecified"},
        {CDFillType.None, "None"},
        {CDFillType.Solid, "Solid"},
        {CDFillType.Shaded, "Shaded"},
        {CDFillType.Faded, "Faded"}
      };

  static final Object[][] CDXMLNoGoType =
      new Object[][] {
        {CDNoGoType.Unspecified, "Unspecified"},
        {CDNoGoType.None, "None"},
        {CDNoGoType.Cross, "Cross"},
        {CDNoGoType.Hash, "Hash"}
      };
}
