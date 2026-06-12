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

import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Arrow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Bond;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Border;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_BracketAttachment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_BracketedGroup;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_ChemicalProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_ColoredMolecularArea;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Constraint;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_CrossReference;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_CrossingBond;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Curve;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Document;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_EmbeddedObject;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Fragment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Geometry;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Graphic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Group;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_NamedAlternativeGroup;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Node;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_ObjectTag;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Page;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_ReactionScheme;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_ReactionStep;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Sequence;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Spectrum;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Splitter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_TLCLane;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_TLCPlate;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_TLCSpot;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Table;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_TemplateGrid;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObj_Text;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_2DExtent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_2DPosition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_3DCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_3DHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_3DPosition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_3DTail;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arc_AngularSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_Dipole;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_EquilibriumRatio;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_HeadSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_NoGo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_ShaftSpacing;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Arrow_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_AbnormalValence;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_AltGroupID;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_AtomNumber;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_BondOrdering;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_CIPStereochemistry;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Charge;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ElementList;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ExternalConnectionType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Formula;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_GenericList;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_GenericNickname;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Geometry;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_HDash;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_HDot;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Isotope;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_IsotopicAbundance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_LinkCountHigh;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_LinkCountLow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_NumHydrogens;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Radical;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictFreeSites;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictImplicitHydrogens;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictRingBondCount;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictRxnChange;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictRxnStereo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictSubstituentsExactly;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictSubstituentsUpTo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_RestrictUnsaturatedBonds;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ShowAtomNumber;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ShowEnhancedStereo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ShowQuery;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_ShowStereo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Atom_Translation;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BMP;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BackgroundColor;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BasisObjects;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BoldWidth;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BondLength;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BondSpacing;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BondSpacingAbs;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_Begin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_BeginAttach;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_BondOrdering;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_CIPStereochemistry;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_CrossingBonds;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_Display;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_Display2;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_DoublePosition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_End;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_EndAttach;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_Order;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_RestrictRxnParticipation;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_RestrictTopology;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_ShowQuery;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_ShowRxn;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bond_ShowStereo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BottomLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BottomRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BoundingBox;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BoundsInParent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_BondID;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_ComponentOrder;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_GraphicID;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_InnerAtomID;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_LipSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_RepeatCount;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_SRULabel;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Bracket_Usage;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_BracketedObjects;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionJustification;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionLineHeight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionStyle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionStyleColor;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionStyleFace;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionStyleFont;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CaptionStyleSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CartridgeData;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ChainAngle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ChemicalPropertyDisplayID;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ChemicalPropertyIsActive;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ChemicalPropertyType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ChemicalWarning;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ColorTable;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Comment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CompressedEnhancedMetafile;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CompressedOLEObject;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CompressedWindowsMetafile;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ConstraintMax;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ConstraintMin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ConstraintType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CornerRadius;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CreationDate;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CreationProgram;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CreationUserName;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CrossReference_Container;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CrossReference_Document;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CrossReference_Identifier;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_CrossReference_Sequence;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_ArrowheadCenterSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_ArrowheadHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_ArrowheadTail;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_ArrowheadType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_ArrowheadWidth;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_Closed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_FillType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_Points;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_Points3D;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Curve_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_DihedralIsChiral;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_DrawingSpaceType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_EnhancedMetafile;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FadePercent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FixInplaceExtent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FixInplaceGap;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FontTable;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Footer;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FooterPosition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ForegroundColor;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_FractionalWidths;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Frag_ConnectionOrder;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_GIF;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_GeometricFeature;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Graphic_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Group_Integral;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_HashSpacing;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Header;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_HeaderPosition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Height;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_HeightPages;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_HideImplicitHydrogens;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_HighlightColor;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_IgnoreUnconnectedAtoms;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_IgnoreWarnings;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_InterpretChemically;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_JPEG;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Justification;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelAlignment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelJustification;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelLineHeight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelStyle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelStyleColor;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelStyleFace;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelStyleFont;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LabelStyleSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LineHeight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LineStarts;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_LineWidth;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Line_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_MacPICT;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_MacPrintInfo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Magnification;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_MajorAxisEnd3D;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_MarginWidth;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_MinorAxisEnd3D;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ModificationDate;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ModificationProgram;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ModificationUserName;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Mole_Absolute;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Mole_Formula;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Mole_Racemic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Mole_Relative;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Mole_Weight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Name;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_NamedAlternativeGroup_GroupFrame;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_NamedAlternativeGroup_TextFrame;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_NamedAlternativeGroup_Valence;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Node_Attachments;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Node_Element;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Node_LabelDisplay;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Node_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_OLEObject;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ObjectTag_Persistent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ObjectTag_Tracking;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ObjectTag_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ObjectTag_Value;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Orbital_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Oval_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PNG;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PageDefinition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PageOverlap;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Picture_Edition;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Picture_EditionAlias;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PointIsDirected;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Polymer_FlipType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Polymer_RepeatPattern;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Positioning;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PositioningAngle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PositioningOffset;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PrintMargins;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_PrintTrimMarks;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Arrows;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Atom_Map;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Atom_Map_Auto;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Atom_Map_Manual;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_ObjectsAboveArrow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_ObjectsBelowArrow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Plusses;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Products;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ReactionStep_Reactants;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Rectangle_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_RelationValue;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_RepresentsProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_RotationAngle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Sequence_Identifier;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ShadowSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ShowNonTerminalCarbonLabels;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ShowTerminalCarbonLabels;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Side;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_Class;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_DataPoint;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_XAxisLabel;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_XLow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_XSpacing;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_XType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_YAxisLabel;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_YLow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_YScale;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Spectrum_YType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_SplitterPositions;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_SupersededBy;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Symbol_Type;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TIFF;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_OriginFraction;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_Rf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_ShowBorders;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_ShowOrigin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_ShowRf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_ShowSolventFront;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_SolventFrontFraction;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TLC_Tail;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Template_NumColumns;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Template_NumRows;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Template_PaneHeight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Text;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TopLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_TopRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_UncompressedEnhancedMetafileSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_UncompressedOLEObjectSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_UncompressedWindowsMetafileSize;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Visible;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Width;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_WidthPages;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_WinPrintInfo;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Window_IsZoomed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Window_Position;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_Window_Size;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_WindowsMetafile;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_WordWrapWidth;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_ZOrder;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.getPositionAsString;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readAbundanceProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readArrowTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readArrowheadProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readArrowheadTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readAtomCIPTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readAtomGeometryProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondCIPTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondDisplayProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondDoublePositionProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondOrdersProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondReactionParticipationProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBondTopologyProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBracketTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readBracketUsageProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readCDXDocument;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readConstraintTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readCurveTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readDrawingSpaceTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readExternalConnectionTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readFillTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readGeometricFeatureProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readGraphicTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readLabelDisplayProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readLineHeight;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readLineTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readNoGoProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readNodeTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readObjectTagTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readOrbitalTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readOvalTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readPageDefinitionProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readPolymerFlipTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readPolymerRepeatPatternProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readPositioningTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readRadicalProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readReactionStereoProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readRectangleTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readRingBondCountProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readSideTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readSpectrumClassProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readSpectrumXTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readSpectrumYTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readSymbolTypeProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readTextJustificationProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readTranslationProperty;
import static org.beilstein.chemxtract.cdx.reader.CDXUtils.readUnsaturationProperty;

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
    CDXObject object = readCDXDocument(bytes, new int[] {0});

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
        case CDXObj_Page:
          document.addPage(createPageObject(object));
          break;
        case CDXObj_TemplateGrid:
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
        case CDXProp_ColorTable:
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
        case CDXProp_FontTable:
          fonts = property.getDataAsFontTable();
          break;
        default:
          break;
      }
    }

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXProp_CreationUserName:
          document.setCreationUserName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_CreationDate:
          document.setCreationDate(property.getDataAsDate());
          break;
        case CDXProp_CreationProgram:
          document.setCreationProgram(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_ModificationUserName:
          document.setModificationUserName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_ModificationDate:
          document.setModificationDate(property.getDataAsDate());
          break;
        case CDXProp_ModificationProgram:
          document.setModificationProgram(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Name:
          document.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Comment:
          document.setComment(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_FontTable:
          // ignore here
          break;
        case CDXProp_BoundingBox:
          document.setBoundingBox(property.getDataAsRectangle());
          break;
        case CDXProp_ColorTable:
          // ignore here
          break;
        case CDXProp_Atom_ShowQuery:
          document.getSettings().setShowAtomQuery(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_ShowStereo:
          document.getSettings().setShowAtomStereo(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_ShowAtomNumber:
          document.getSettings().setShowAtomNumber(property.getDataAsBoolean());
          break;
        case CDXProp_Bond_ShowQuery:
          document.getSettings().setShowBondQuery(property.getDataAsBoolean());
          break;
        case CDXProp_Bond_ShowStereo:
          document.getSettings().setShowBondStereo(property.getDataAsBoolean());
          break;
        case CDXProp_Bond_ShowRxn:
          document.getSettings().setShowBondReaction(property.getDataAsBoolean());
          break;
        case CDXProp_LabelLineHeight:
          document.getSettings().setLabelLineHeight(readLineHeight(property));
          break;
        case CDXProp_CaptionLineHeight:
          document.getSettings().setCaptionLineHeight(readLineHeight(property));
          break;
        case CDXProp_InterpretChemically:
          document.getSettings().setInterpretChemically(property.getDataAsBoolean());
          break;
        case CDXProp_MacPrintInfo:
          document.setMacPrintInfo(property.getData());
          break;
        case CDXProp_WinPrintInfo:
          document.setWinPrintInfo(property.getData());
          break;
        case CDXProp_PrintMargins:
          document.setPrintMargins(property.getDataAsRectangle());
          break;
        case CDXProp_ChainAngle:
          document.getSettings().setChainAngle(property.getDataAsCoordinate());
          break;
        case CDXProp_BondSpacing:
          document.getSettings().setBondSpacing(property.getDataAsInt16() / 10f);
          break;
        case CDXProp_BondSpacingAbs:
          document.getSettings().setBondSpacingAbs(Math.max(property.getDataAsCoordinate(), 0f));
          break;
        case CDXProp_BondLength:
          document.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXProp_BoldWidth:
          document.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          document.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_MarginWidth:
          document.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          document.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          document.getSettings().setLabelFont(fontStyle.getFont());
          document.getSettings().setLabelSize(fontStyle.getSize());
          document.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionStyle:
          fontStyle = property.getDataAsFontStyle(fonts, colors);
          document.getSettings().setCaptionFont(fontStyle.getFont());
          document.getSettings().setCaptionSize(fontStyle.getSize());
          document.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionJustification:
          document.getSettings().setCaptionJustification(readTextJustificationProperty(property));
          break;
        case CDXProp_FractionalWidths:
          document.setFractionalWidths(property.getDataAsBoolean());
          break;
        case CDXProp_Magnification:
          document.setMagnification(property.getDataAsInt16() / 10f);
          break;
        case CDXProp_LabelStyleFont:
          document.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_CaptionStyleFont:
          document.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          document.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_CaptionStyleSize:
          document.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          document.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_CaptionStyleFace:
          document.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXProp_LabelStyleColor:
          document.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_CaptionStyleColor:
          document.getSettings().setCaptionColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_LabelJustification:
          document.getSettings().setLabelJustification(readTextJustificationProperty(property));
          break;
        case CDXProp_FixInplaceExtent:
          document.setFixInPlaceExtent(property.getDataAsPoint2D());
          break;
        case CDXProp_FixInplaceGap:
          document.setFixInPlaceGap(property.getDataAsPoint2D());
          break;
        case CDXProp_CartridgeData:
          document.setCartridgeData(property.getData());
          break;
        case CDXProp_Window_IsZoomed:
          document.setWindowIsZoomed(property.getDataAsBoolean());
          break;
        case CDXProp_Window_Position:
          document.setWindowPosition(property.getDataAsPoint2D());
          break;
        case CDXProp_Window_Size:
          document.setWindowSize(property.getDataAsPoint2D());
          break;
        case CDXProp_ShowTerminalCarbonLabels:
          document.getSettings().setShowTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXProp_ShowNonTerminalCarbonLabels:
          document.getSettings().setShowNonTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXProp_HideImplicitHydrogens:
          document.getSettings().setHideImplicitHydrogens(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_ShowEnhancedStereo:
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
        case CDXObj_Group:
          page.addGroup(createGroupObject(object));
          break;
        case CDXObj_Fragment:
          page.addFragment(createFragmentObject(object));
          break;
        case CDXObj_Text:
          page.addText(createTextObject(object));
          break;
        case CDXObj_Graphic:
          page.addGraphic(createGraphicObject(object));
          break;
        case CDXObj_BracketedGroup:
          page.addBracketedGroup(createBracketedGroupObject(object));
          break;
        case CDXObj_Curve:
          page.addCurve(createSplineObject(object));
          break;
        case CDXObj_EmbeddedObject:
          page.addEmbeddedObject(createEmbeddedObjectObject(object));
          break;
        case CDXObj_Table:
          page.addTable(createTableObject(object));
          break;
        case CDXObj_NamedAlternativeGroup:
          page.addNamedAlternativeGroup(createNamedAlternativeGroupObject(object));
          break;
        case CDXObj_ReactionScheme:
          page.addReactionScheme(createReactionSchemeObject(object));
          break;
        case CDXObj_ReactionStep:
          page.addReactionStep(createReactionStepObject(object));
          break;
        case CDXObj_Spectrum:
          page.addSpectrum(createSpectrumObject(object));
          break;
        case CDXObj_Sequence:
          page.addSequence(createSequenceObject(object));
          break;
        case CDXObj_CrossReference:
          page.addCrossReference(createCrossReferenceObject(object));
          break;
        case CDXObj_Border:
          page.addBorder(createBorderObject(object));
          break;
        case CDXObj_Geometry:
          page.addGeometry(createGeometryObject(object));
          break;
        case CDXObj_Constraint:
          page.addConstraint(createConstraintObject(object));
          break;
        case CDXObj_TLCPlate:
          page.addTLCPlate(createTLCPlateObject(object));
          break;
        case CDXObj_Splitter:
          page.addSplitter(createSplitterObject(object));
          break;
        case CDXObj_ChemicalProperty:
          page.addChemicalProperty(createChemicalPropertyObject(object));
          break;

        case CDXObj_Arrow:
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
        case CDXProp_BoundingBox:
          page.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_BackgroundColor:
          page.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_WidthPages:
          page.setWidthPages(property.getDataAsInt16());
          break;
        case CDXProp_HeightPages:
          page.setHeightPages(property.getDataAsInt16());
          break;
        case CDXProp_DrawingSpaceType:
          page.setDrawingSpaceType(readDrawingSpaceTypeProperty(property));
          break;
        case CDXProp_Width:
          page.setWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_Height:
          page.setHeight(property.getDataAsCoordinate());
          break;
        case CDXProp_PageOverlap:
          page.setPageOverlap(property.getDataAsCoordinate());
          break;
        case CDXProp_Header:
          page.setHeader(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_HeaderPosition:
          page.setHeaderPosition(property.getDataAsCoordinate());
          break;
        case CDXProp_Footer:
          page.setFooter(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_FooterPosition:
          page.setFooterPosition(property.getDataAsCoordinate());
          break;
        case CDXProp_PrintTrimMarks:
          page.setPrintTrimMarks(property.getDataAsBoolean());
          break;
        case CDXProp_SplitterPositions:
          break;
        case CDXProp_PageDefinition:
          page.setPageDefinition(readPageDefinitionProperty(property));
          break;
        case CDXProp_BoundsInParent:
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
        case CDXObj_Node:
          fragment.addAtom(createNodeObject(object));
          break;
        case CDXObj_Bond:
          fragment.addBond(createBondObject(object));
          break;
        case CDXObj_Graphic:
          fragment.addGraphic(createGraphicObject(object));
          break;
        case CDXObj_Curve:
          fragment.addCurve(createSplineObject(object));
          break;
        case CDXObj_ObjectTag:
          fragment.addObjectTag(createObjectTagObject(object));
          break;

        case CDXObj_Text:
          fragment.addText(createTextObject(object));
          break;
        case CDXObj_Arrow:
          fragment.addArrow(createArrowObject(object));
          break;
        case CDXObj_ColoredMolecularArea:
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
        case CDXProp_BoundingBox:
          fragment.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_Mole_Racemic:
          fragment.setRacemic(property.getDataAsBoolean());
          break;
        case CDXProp_Mole_Absolute:
          fragment.setAbsolute(property.getDataAsBoolean());
          break;
        case CDXProp_Mole_Relative:
          fragment.setRelative(property.getDataAsBoolean());
          break;
        case CDXProp_Mole_Formula:
          fragment.setFormula(property.getData());
          break;
        case CDXProp_Mole_Weight:
          fragment.setWeight(property.getDataAsFloat64());
          break;
        case CDXProp_Frag_ConnectionOrder:
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
        case CDXObj_Fragment:
          node.addFragment(createFragmentObject(object));
          break;
        case CDXObj_Text:
          if (node.getText() != null) {
            throw new IOException("Unexpected object");
          }
          node.setText(createTextObject(object));
          break;
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          node.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          node.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          node.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          node.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_2DPosition:
          node.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXProp_3DPosition:
          node.setPosition3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_ForegroundColor:
          node.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          node.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_HighlightColor:
          node.getSettings().setHighlightColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_Node_Type:
          node.setNodeType(readNodeTypeProperty(property));
          break;
        case CDXProp_Node_LabelDisplay:
          node.setLabelDisplay(readLabelDisplayProperty(property));
          break;
        case CDXProp_Node_Element:
          node.setElementNumber(property.getDataAsInt16());
          break;
        case CDXProp_Atom_ElementList:
          node.setElementList(property.getDataAsElementList());
          break;
        case CDXProp_Atom_Formula:
          node.setFormula(property.getData());
          break;
        case CDXProp_Atom_Isotope:
          node.setIsotope(property.getDataAsInt16());
          break;
        case CDXProp_Atom_Charge:
          node.setCharge(property.getDataAsInt());
          break;
        case CDXProp_Atom_Radical:
          node.setRadical(readRadicalProperty(property));
          break;
        case CDXProp_Atom_RestrictFreeSites:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.FreeSites);
          break;
        case CDXProp_Atom_RestrictImplicitHydrogens:
          node.setImplicitHydrogensAllowed(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_RestrictRingBondCount:
          node.setRingBondCount(readRingBondCountProperty(property));
          break;
        case CDXProp_Atom_RestrictUnsaturatedBonds:
          node.setUnsaturatedBonds(readUnsaturationProperty(property));
          break;
        case CDXProp_Atom_RestrictRxnChange:
          node.setRestrictReactionChange(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_RestrictRxnStereo:
          node.setReactionStereo(readReactionStereoProperty(property));
          break;
        case CDXProp_Atom_AbnormalValence:
          node.setAbnormalValenceAllowed(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_NumHydrogens:
          node.setNumImplicitHydrogens(property.getDataAsInt16());
          break;
        case CDXProp_Atom_HDot:
          node.setHDot(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_HDash:
          node.setHDash(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_Geometry:
          node.setAtomGeometry(readAtomGeometryProperty(property));
          break;
        case CDXProp_Atom_BondOrdering:
          node.setBondOrdering(property.getDataAsObjectRefArray(CDBond.class, refManager));
          break;
        case CDXProp_Node_Attachments:
          node.setAttachedAtoms(
              property.getDataAsObjectRefArrayWithCounts(CDAtom.class, refManager));
          break;
        case CDXProp_Atom_GenericNickname:
          node.setLabelText(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Atom_AltGroupID:
          node.setAltGroup(property.getDataAsObjectRef(CDAltGroup.class, refManager));
          break;
        case CDXProp_Atom_RestrictSubstituentsUpTo:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.SubstituentsUpTo);
          break;
        case CDXProp_Atom_RestrictSubstituentsExactly:
          node.setSubstituentCount(property.getDataAsUInt8());
          node.setSubstituentType(CDAtomSubstituentType.SubstituentsExactly);
          break;
        case CDXProp_Atom_CIPStereochemistry:
          node.setStereochemistry(readAtomCIPTypeProperty(property));
          break;
        case CDXProp_Atom_Translation:
          node.setTranslation(readTranslationProperty(property));
          break;
        case CDXProp_Atom_AtomNumber:
          node.setAtomNumber(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Atom_ShowQuery:
          node.getSettings().setShowAtomQuery(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_ShowStereo:
          node.getSettings().setShowAtomStereo(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_ShowAtomNumber:
          node.getSettings().setShowAtomNumber(property.getDataAsBoolean());
          break;
        case CDXProp_Atom_LinkCountLow:
          node.setLinkCountLow(property.getDataAsInt16());
          break;
        case CDXProp_Atom_LinkCountHigh:
          node.setLinkCountHigh(property.getDataAsInt16());
          break;
        case CDXProp_Atom_IsotopicAbundance:
          node.setIsotopicAbundance(readAbundanceProperty(property));
          break;
        case CDXProp_Atom_ExternalConnectionType:
          node.setAttachmentPointType(readExternalConnectionTypeProperty(property));
          break;
        case CDXProp_Atom_GenericList:
          node.setGenericList(property.getDataAsGenericList(fonts, colors));
          break;
        case CDXProp_LineWidth:
          node.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          node.getSettings().setLabelFont(fontStyle.getFont());
          node.getSettings().setLabelSize(fontStyle.getSize());
          node.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXProp_LabelStyleFont:
          node.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          node.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          node.getSettings().setLabelFace(property.getDataAsFontFace());
          break;

        case CDXProp_MarginWidth:
          node.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;

        case CDXProp_ShowTerminalCarbonLabels:
          node.getSettings().setShowTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXProp_ShowNonTerminalCarbonLabels:
          node.getSettings().setShowNonTerminalCarbonLabels(property.getDataAsBoolean());
          break;
        case CDXProp_HideImplicitHydrogens:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          bond.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          bond.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          bond.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          bond.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_ForegroundColor:
          bond.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          bond.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_HighlightColor:
          bond.getSettings().setHighlightColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_Bond_Order:
          bond.setBondOrder(readBondOrdersProperty(property));
          break;
        case CDXProp_Bond_Display:
          bond.setBondDisplay(readBondDisplayProperty(property));
          break;
        case CDXProp_Bond_Display2:
          bond.setBondDisplay2(readBondDisplayProperty(property));
          break;
        case CDXProp_Bond_DoublePosition:
          bond.setBondDoublePosition(readBondDoublePositionProperty(property));
          break;
        case CDXProp_Bond_Begin:
          bond.setBegin(property.getDataAsObjectRef(CDAtom.class, refManager));
          break;
        case CDXProp_Bond_End:
          bond.setEnd(property.getDataAsObjectRef(CDAtom.class, refManager));
          break;
        case CDXProp_Bond_RestrictTopology:
          bond.setTopology(readBondTopologyProperty(property));
          break;
        case CDXProp_Bond_RestrictRxnParticipation:
          bond.setReactionParticipation(readBondReactionParticipationProperty(property));
          break;
        case CDXProp_Bond_BeginAttach:
          bond.setBeginAttach(property.getDataAsUInt8());
          break;
        case CDXProp_Bond_EndAttach:
          bond.setEndAttach(property.getDataAsUInt8());
          break;
        case CDXProp_Bond_CIPStereochemistry:
          bond.setStereochemistry(readBondCIPTypeProperty(property));
          break;
        case CDXProp_Bond_BondOrdering:
          bond.setBondCircularOrdering(property.getDataAsObjectRefArray(CDBond.class, refManager));
          break;
        case CDXProp_Bond_ShowQuery:
          bond.getSettings().setShowBondQuery(property.getDataAsBoolean());
          break;
        case CDXProp_Bond_ShowStereo:
          bond.getSettings().setShowBondStereo(property.getDataAsBoolean());
          break;
        case CDXProp_Bond_CrossingBonds:
          bond.setCrossingBonds(
              new HashSet<CDBond>(property.getDataAsObjectRefArray(CDBond.class, refManager)));
          break;
        case CDXProp_Bond_ShowRxn:
          bond.getSettings().setShowBondReaction(property.getDataAsBoolean());
          break;
        case CDXProp_BondSpacing:
          bond.getSettings().setBondSpacing(property.getDataAsInt16() / 10f);
          break;
        case CDXProp_BondLength:
          bond.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXProp_BoldWidth:
          bond.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          bond.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_MarginWidth:
          bond.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          bond.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          bond.getSettings().setLabelFont(fontStyle.getFont());
          bond.getSettings().setLabelSize(fontStyle.getSize());
          bond.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXProp_LabelStyleFont:
          bond.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          bond.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          bond.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_BondSpacingAbs:
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
        case CDXProp_2DExtent:
          templateGrid.setExtent(property.getDataAsPoint2D());
          break;
        case CDXProp_Template_PaneHeight:
          templateGrid.setPaneHeight(property.getDataAsCoordinate());
          break;
        case CDXProp_Template_NumRows:
          templateGrid.setNumRows(property.getDataAsInt16());
          break;
        case CDXProp_Template_NumColumns:
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
        case CDXObj_Group:
          group.addGroup(createGroupObject(object));
          break;
        case CDXObj_Fragment:
          group.addFragment(createFragmentObject(object));
          break;
        case CDXObj_Text:
          group.addCaption(createTextObject(object));
          break;
        case CDXObj_Graphic:
          group.addGraphic(createGraphicObject(object));
          break;
        case CDXObj_Curve:
          group.addCurve(createSplineObject(object));
          break;
        case CDXObj_NamedAlternativeGroup:
          group.addNamedAlternativeGroup(createNamedAlternativeGroupObject(object));
          break;
        case CDXObj_ReactionStep:
          group.addReactionStep(createReactionStepObject(object));
          break;
        case CDXObj_Spectrum:
          group.addSpectrum(createSpectrumObject(object));
          break;
        case CDXObj_EmbeddedObject:
          group.addEmbeddedObject(createEmbeddedObjectObject(object));
          break;
        case CDXObj_ObjectTag:
          group.addObjectTag(createObjectTagObject(object));
          break;

        case CDXObj_Arrow:
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
        case CDXProp_BoundingBox:
          group.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_Group_Integral:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          text.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          text.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          text.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          text.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_2DPosition:
          text.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXProp_BoundingBox:
          text.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_RotationAngle:
          text.setAngle(property.getDataAsCoordinate());
          break;
        case CDXProp_Text:
          text.setText(property.getDataAsStyledString(fonts, colors));
          break;
        case CDXProp_Justification:
          text.setJustification(readTextJustificationProperty(property));
          text.getSettings().setLabelJustification(readTextJustificationProperty(property));
          break;
        case CDXProp_LineHeight:
          text.setLineHeight(readLineHeight(property));
          break;
        case CDXProp_WordWrapWidth:
          text.setWrapWidth(property.getDataAsInt16());
          break;
        case CDXProp_LineStarts:
          text.setLineStarts(property.getDataAsInt16ListWithCounts());
          break;
        case CDXProp_LabelAlignment:
          text.setLabelAlignment(readLabelDisplayProperty(property));
          break;
        case CDXProp_LabelLineHeight:
          text.getSettings().setLabelLineHeight(readLineHeight(property));
          break;
        case CDXProp_CaptionLineHeight:
          text.getSettings().setCaptionLineHeight(readLineHeight(property));
          break;
        case CDXProp_InterpretChemically:
          text.getSettings().setInterpretChemically(property.getDataAsBoolean());
          break;
        case CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          text.getSettings().setLabelFont(fontStyle.getFont());
          text.getSettings().setLabelSize(fontStyle.getSize());
          text.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionStyle:
          fontStyle = property.getDataAsFontStyle(fonts, colors);
          text.getSettings().setCaptionFont(fontStyle.getFont());
          text.getSettings().setCaptionSize(fontStyle.getSize());
          text.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionJustification:
          text.getSettings().setCaptionJustification(readTextJustificationProperty(property));
          break;
        case CDXProp_LabelStyleFont:
          text.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_CaptionStyleFont:
          text.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          text.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_CaptionStyleSize:
          text.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          text.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_CaptionStyleFace:
          text.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXProp_LabelStyleColor:
          text.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_CaptionStyleColor:
          text.getSettings().setCaptionColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_LabelJustification:
          text.setJustification(readTextJustificationProperty(property));
          text.getSettings().setLabelJustification(readTextJustificationProperty(property));
          break;
        case CDXProp_ForegroundColor:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_RepresentsProperty:
          graphic.setRepresents(property.getDataAsRepresentsProperties(refManager));
          break;
        case CDXProp_ZOrder:
          graphic.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          graphic.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          graphic.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          graphic.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_SupersededBy:
          graphic.setSupersededBy(property.getDataAsObjectRef(CDObject.class, refManager));
          break;
        case CDXProp_BoundingBox:
          graphic.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_3DHead:
          graphic.setHead3D(property.getDataAsPoint3D(false));
          break;
        case CDXProp_3DTail:
          graphic.setTail3D(property.getDataAsPoint3D(false));
          break;
        case CDXProp_ForegroundColor:
          graphic.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          graphic.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BoldWidth:
          graphic.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          graphic.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          graphic.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_CaptionStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          graphic.getSettings().setCaptionFont(fontStyle.getFont());
          graphic.getSettings().setCaptionSize(fontStyle.getSize());
          graphic.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionStyleFont:
          graphic.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_CaptionStyleSize:
          graphic.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXProp_CaptionStyleFace:
          graphic.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXProp_Graphic_Type:
          graphic.setGraphicType(readGraphicTypeProperty(property));
          break;
        case CDXProp_Line_Type:
          graphic.setLineType(readLineTypeProperty(property));
          break;
        case CDXProp_Arrow_Type:
          graphic.setArrowType(readArrowTypeProperty(property));
          break;
        case CDXProp_Rectangle_Type:
          graphic.setRectangleType(readRectangleTypeProperty(property));
          break;
        case CDXProp_Oval_Type:
          graphic.setOvalType(readOvalTypeProperty(property));
          break;
        case CDXProp_Orbital_Type:
          graphic.setOrbitalType(readOrbitalTypeProperty(property));
          break;
        case CDXProp_Bracket_Type:
          graphic.setBracketType(readBracketTypeProperty(property));
          break;
        case CDXProp_Symbol_Type:
          graphic.setSymbolType(readSymbolTypeProperty(property));
          break;
        case CDXProp_Arrow_HeadSize:
          graphic.setArrowHeadSize(property.getDataAsInt16() / 100f);
          break;
        case CDXProp_Arc_AngularSize:
          graphic.setArcAngularSize(property.getDataAsInt16() / 10f);
          break;
        case CDXProp_Bracket_LipSize:
          graphic.setBracketLipSize(property.getDataAsInt16());
          break;
        case CDXProp_Bracket_Usage:
          graphic.setBracketUsage(readBracketUsageProperty(property));
          break;
        case CDXProp_Polymer_RepeatPattern:
          graphic.setPolymerRepeatPattern(readPolymerRepeatPatternProperty(property));
          break;
        case CDXProp_Polymer_FlipType:
          graphic.setPolymerFlipType(readPolymerFlipTypeProperty(property));
          break;

        case CDXProp_Curve_FillType:
          graphic.setFillType(readFillTypeProperty(property));
          break;
        case CDXProp_ShadowSize:
          graphic.setShadowSize(property.getDataAsUInt16());
          break;
        case CDXProp_CornerRadius:
          graphic.setCornerRadius(property.getDataAsUInt16());
          break;
        case CDXProp_3DCenter:
          graphic.setCenter3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_MajorAxisEnd3D:
          graphic.setMajorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_MinorAxisEnd3D:
          graphic.setMinorAxisEnd3D(property.getDataAsPoint3D(true));
          break;

        case CDXProp_FadePercent:
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
        case CDXProp_BackgroundColor:
          area.setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BasisObjects:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          arrow.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          arrow.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          arrow.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          arrow.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          arrow.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_3DHead:
          arrow.setHead3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_3DTail:
          arrow.setTail3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_ForegroundColor:
          arrow.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          arrow.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BoldWidth:
          arrow.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          arrow.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          arrow.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_CaptionStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          arrow.getSettings().setCaptionFont(fontStyle.getFont());
          arrow.getSettings().setCaptionSize(fontStyle.getSize());
          arrow.getSettings().setCaptionFace(fontStyle.getFontType());
          break;
        case CDXProp_CaptionStyleFont:
          arrow.getSettings().setCaptionFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_CaptionStyleSize:
          arrow.getSettings().setCaptionSize(property.getDataAsInt16());
          break;
        case CDXProp_CaptionStyleFace:
          arrow.getSettings().setCaptionFace(property.getDataAsFontFace());
          break;
        case CDXProp_Line_Type:
          arrow.setLineType(readLineTypeProperty(property));
          break;
        case CDXProp_Arrow_HeadSize:
          arrow.setHeadSize(property.getDataAsInt16() / 100f);
          break;
        case CDXProp_Arc_AngularSize:
          arrow.setAngularSize(property.getDataAsInt16() / 10f);
          break;

        case CDXProp_Curve_ArrowheadType:
          arrow.setArrowHeadType(readArrowheadTypeProperty(property));
          break;
        case CDXProp_Curve_ArrowheadHead:
          arrow.setArrowHeadPositionStart(readArrowheadProperty(property));
          break;
        case CDXProp_Curve_ArrowheadTail:
          arrow.setArrowHeadPositionTail(readArrowheadProperty(property));
          break;
        case CDXProp_Curve_ArrowheadCenterSize:
          arrow.setHeadCenterSize(property.getDataAsUInt16() / 100f);
          break;
        case CDXProp_Curve_ArrowheadWidth:
          arrow.setHeadWidth(property.getDataAsUInt16() / 100f);
          break;
        case CDXProp_3DCenter:
          arrow.setCenter3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_MajorAxisEnd3D:
          arrow.setMajorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_MinorAxisEnd3D:
          arrow.setMinorAxisEnd3D(property.getDataAsPoint3D(true));
          break;
        case CDXProp_Arrow_NoGo:
          arrow.setNoGoType(readNoGoProperty(property));
          break;
        case CDXProp_Arrow_ShaftSpacing:
          arrow.setShaftSpacing(property.getDataAsUInt16() / 100f);
          break;
        case CDXProp_Curve_FillType:
          arrow.setFillType(readFillTypeProperty(property));
          break;
        case CDXProp_Arrow_Dipole:
          arrow.setDipole(property.getDataAsBoolean());
          break;
        case CDXProp_Arrow_EquilibriumRatio:
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
        case CDXObj_BracketedGroup:
          bracketedGroup.addBracket(createBracketedGroupObject(object));
          break;
        case CDXObj_BracketAttachment:
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
        case CDXProp_Bracket_Usage:
          bracketedGroup.setBracketUsage(readBracketUsageProperty(property));
          break;
        case CDXProp_Polymer_RepeatPattern:
          bracketedGroup.setPolymerRepeatPattern(readPolymerRepeatPatternProperty(property));
          break;
        case CDXProp_Polymer_FlipType:
          bracketedGroup.setPolymerFlipType(readPolymerFlipTypeProperty(property));
          break;
        case CDXProp_BracketedObjects:
          bracketedGroup.setBracketedObjects(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_Bracket_RepeatCount:
          bracketedGroup.setRepeatCount(property.getDataAsFloat64());
          break;
        case CDXProp_Bracket_ComponentOrder:
          bracketedGroup.setComponentOrder(property.getDataAsInt16());
          break;
        case CDXProp_Bracket_SRULabel:
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
        case CDXObj_CrossingBond:
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
        case CDXProp_Bracket_GraphicID:
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
        case CDXProp_Bracket_BondID:
          crossingBond.setBond(property.getDataAsObjectRef(CDBond.class, refManager));
          break;
        case CDXProp_Bracket_InnerAtomID:
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
        case CDXProp_2DPosition:
          splitter.setPosition2D(property.getDataAsPoint2D());
          break;
        case CDXProp_PageDefinition:
          splitter.setPageDefinition(readPageDefinitionProperty(property));
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
        case CDXObj_ObjectTag:
          plate.addObjectTag(createObjectTagObject(object));
          break;
        case CDXObj_TLCLane:
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
        case CDXProp_ZOrder:
          plate.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_Visible:
          plate.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          plate.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_TopLeft:
          plate.setTopLeft(property.getDataAsPoint2D());
          break;
        case CDXProp_TopRight:
          plate.setTopRight(property.getDataAsPoint2D());
          break;
        case CDXProp_BottomRight:
          plate.setBottomRight(property.getDataAsPoint2D());
          break;
        case CDXProp_BottomLeft:
          plate.setBottomLeft(property.getDataAsPoint2D());
          break;
        case CDXProp_ForegroundColor:
          plate.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          plate.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BoldWidth:
          plate.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          plate.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_MarginWidth:
          plate.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyleFont:
          plate.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          plate.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          plate.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_TLC_OriginFraction:
          plate.setOriginFraction(property.getDataAsFloat64());
          break;
        case CDXProp_TLC_SolventFrontFraction:
          plate.setSolventFrontFraction(property.getDataAsFloat64());
          break;
        case CDXProp_TLC_ShowOrigin:
          plate.setShowOrigin(property.getDataAsBoolean());
          break;
        case CDXProp_TLC_ShowSolventFront:
          plate.setShowSolventFront(property.getDataAsBoolean());
          break;
        case CDXProp_TLC_ShowBorders:
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
        case CDXObj_ObjectTag:
          lane.addObjectTag(createObjectTagObject(object));
          break;
        case CDXObj_TLCSpot:
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
        case CDXProp_Visible:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_Visible:
          spot.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_Width:
          spot.setWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_Height:
          spot.setHeight(property.getDataAsCoordinate());
          break;
        case CDXProp_Curve_Type:
          spot.setCurveType(readCurveTypeProperty(property));
          break;
        case CDXProp_TLC_Rf:
          spot.setRf(property.getDataAsFloat64());
          break;
        case CDXProp_TLC_Tail:
          spot.setTail(property.getDataAsCoordinate());
          break;
        case CDXProp_TLC_ShowRf:
          spot.setShowRf(property.getDataAsBoolean());
          break;
        case CDXProp_ForegroundColor:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_Name:
          constraint.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_ForegroundColor:
          constraint.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BondLength:
          constraint.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          constraint.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          constraint.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyleFont:
          constraint.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          constraint.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          constraint.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_LabelStyleColor:
          constraint.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BasisObjects:
          constraint.setBasisObjects(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ConstraintType:
          constraint.setConstraintType(readConstraintTypeProperty(property));
          break;
        case CDXProp_ConstraintMin:
          constraint.setMinRange(property.getDataAsFloat64());
          break;
        case CDXProp_ConstraintMax:
          constraint.setMaxRange(property.getDataAsFloat64());
          break;
        case CDXProp_IgnoreUnconnectedAtoms:
          constraint.setIgnoreUnconnectedAtoms(property.getDataAsBoolean());
          break;
        case CDXProp_DihedralIsChiral:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_Name:
          geometry.setName(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_ForegroundColor:
          geometry.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BondLength:
          geometry.getSettings().setBondLength(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          geometry.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyleFont:
          geometry.getSettings().setLabelFont(property.getDataAsFontRef(fonts)); // deprecated
          break;
        case CDXProp_LabelStyleSize:
          geometry.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          geometry.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_LabelStyleColor:
          geometry.getSettings().setLabelColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_GeometricFeature:
          geometry.setGeometricType(readGeometricFeatureProperty(property));
          break;
        case CDXProp_RelationValue:
          geometry.setRelationValue(property.getDataAsFloat64());
          break;
        case CDXProp_BasisObjects:
          geometry.setBasisObjects(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_PointIsDirected:
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
        case CDXProp_ForegroundColor:
          border.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_LineWidth:
          border.setWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_Side:
          border.setSide(readSideTypeProperty(property));
          break;
        case CDXProp_Line_Type:
          border.setLineType(readLineTypeProperty(property));
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
        case CDXProp_CrossReference_Container:
          crossReference.setContainer(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_CrossReference_Document:
          crossReference.setDocument(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_CrossReference_Identifier:
          crossReference.setIdentifier(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_CrossReference_Sequence:
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
        case CDXProp_Sequence_Identifier:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          spectrum.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          spectrum.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          spectrum.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          spectrum.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          spectrum.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_ForegroundColor:
          spectrum.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          spectrum.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BoldWidth:
          spectrum.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          spectrum.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyle:
          CDXFontStyle fontStyle = property.getDataAsFontStyle(fonts, colors);
          spectrum.getSettings().setLabelFont(fontStyle.getFont());
          spectrum.getSettings().setLabelSize(fontStyle.getSize());
          spectrum.getSettings().setLabelFace(fontStyle.getFontType());
          break;
        case CDXProp_LabelStyleFont:
          spectrum.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          spectrum.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
          spectrum.getSettings().setLabelFace(property.getDataAsFontFace());
          break;
        case CDXProp_Spectrum_XSpacing:
          spectrum.setXSpacing(property.getDataAsFloat64());
          break;
        case CDXProp_Spectrum_XLow:
          spectrum.setXLow(property.getDataAsFloat64());
          break;
        case CDXProp_Spectrum_XType:
          spectrum.setXType(readSpectrumXTypeProperty(property));
          break;
        case CDXProp_Spectrum_YType:
          spectrum.setYType(readSpectrumYTypeProperty(property));
          break;
        case CDXProp_Spectrum_XAxisLabel:
          spectrum.setXAxisLabel(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Spectrum_YAxisLabel:
          spectrum.setYAxisLabel(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Spectrum_DataPoint:
          spectrum.setDataPoint(property.getDataAsFloat64Array());
          break;
        case CDXProp_Spectrum_Class:
          spectrum.setSpectrumClass(readSpectrumClassProperty(property));
          break;
        case CDXProp_Spectrum_YLow:
          spectrum.setYLow(property.getDataAsFloat64());
          break;
        case CDXProp_Spectrum_YScale:
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
        case CDXProp_ReactionStep_Atom_Map:
          reactionStep.setAtomMap(
              property.getDataObjectRefMap(CDAtom.class, CDAtom.class, refManager));
          break;
        case CDXProp_ReactionStep_Reactants:
          reactionStep.setReactants(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_Products:
          reactionStep.setProducts(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_Plusses:
          reactionStep.setPlusses(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_Arrows:
          reactionStep.setArrows(property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_ObjectsAboveArrow:
          reactionStep.setObjectsAboveArrow(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_ObjectsBelowArrow:
          reactionStep.setObjectsBelowArrow(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ReactionStep_Atom_Map_Manual:
          reactionStep.setAtomMapManual(
              property.getDataObjectRefMap(CDAtom.class, CDAtom.class, refManager));
          break;
        case CDXProp_ReactionStep_Atom_Map_Auto:
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
        case CDXObj_ReactionStep:
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
        case CDXObj_Group:
          namedAlternativeGroup.addGroup(createGroupObject(object));
          break;
        case CDXObj_Fragment:
          namedAlternativeGroup.addFragment(createFragmentObject(object));
          break;
        case CDXObj_Text:
          namedAlternativeGroup.addCaption(createTextObject(object));
          break;
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          namedAlternativeGroup.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          namedAlternativeGroup.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          namedAlternativeGroup.setChemicalWarning(property.getDataAsString());
          break;
        case CDXProp_Visible:
          namedAlternativeGroup.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          namedAlternativeGroup.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_ForegroundColor:
          namedAlternativeGroup.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          namedAlternativeGroup
              .getSettings()
              .setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_NamedAlternativeGroup_TextFrame:
          namedAlternativeGroup.setTextFrame(property.getDataAsRectangle());
          break;
        case CDXProp_NamedAlternativeGroup_GroupFrame:
          namedAlternativeGroup.setGroupFrame(property.getDataAsRectangle());
          break;
        case CDXProp_NamedAlternativeGroup_Valence:
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
        case CDXObj_Page:
          table.addPage(createPageObject(object));
          break;
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          table.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_Visible:
          table.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          table.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_ForegroundColor:
          table.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          table.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BoldWidth:
          table.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LineWidth:
          table.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_MarginWidth:
          table.getSettings().setMarginWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_LabelStyleFont:
          table.getSettings().setLabelFont(property.getDataAsFontRef(fonts));
          break;
        case CDXProp_LabelStyleSize:
          table.getSettings().setLabelSize(property.getDataAsInt16());
          break;
        case CDXProp_LabelStyleFace:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          picture.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_BoundingBox:
          picture.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_RotationAngle:
          picture.setRotationAngle(property.getDataAsCoordinate());
          break;
        case CDXProp_ForegroundColor:
          picture.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          picture.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_Picture_Edition:
          picture.setPictureEdition(property.getData());
          break;
        case CDXProp_Picture_EditionAlias:
          picture.setPictureEditionAlias(property.getData());
          break;
        case CDXProp_MacPICT:
          picture.setMacPICT(property.getData());
          break;
        case CDXProp_WindowsMetafile:
          picture.setWindowsMetafile(property.getData());
          break;
        case CDXProp_OLEObject:
          picture.setOleObject(property.getData());
          break;
        case CDXProp_EnhancedMetafile:
          picture.setEnhancedMetafile(property.getData());
          break;

        case CDXProp_CompressedWindowsMetafile:
          compressedWindowsMetafile = property.getData();
          break;
        case CDXProp_CompressedOLEObject:
          compressedOLEObject = property.getData();
          break;
        case CDXProp_CompressedEnhancedMetafile:
          compressedEnhancedMetafile = property.getData();
          break;

        case CDXProp_UncompressedWindowsMetafileSize:
          uncompressedWindowsMetafileSize = property.getDataAsInt();
          break;
        case CDXProp_UncompressedOLEObjectSize:
          uncompressedOLEObjectSize = property.getDataAsInt();
          break;
        case CDXProp_UncompressedEnhancedMetafileSize:
          uncompressedEnhancedMetafileSize = property.getDataAsInt();
          break;

        case CDXProp_GIF:
          picture.setGif(property.getData());
          break;
        case CDXProp_TIFF:
          picture.setTiff(property.getData());
          break;
        case CDXProp_PNG:
          picture.setPng(property.getData());
          break;
        case CDXProp_JPEG:
          picture.setJpeg(property.getData());
          break;
        case CDXProp_BMP:
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
        case CDXObj_ObjectTag:
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
        case CDXProp_ZOrder:
          spline.setZOrder(property.getDataAsInt16());
          break;
        case CDXProp_IgnoreWarnings:
          spline.setIgnoreWarnings(property.getDataAsBoolean());
          break;
        case CDXProp_ChemicalWarning:
          spline.setChemicalWarning(property.getDataAsUnstyledString(fonts, colors));
          break;
        case CDXProp_Visible:
          spline.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_BoundingBox:
          spline.setBounds(property.getDataAsRectangle());
          break;
        case CDXProp_ForegroundColor:
          spline.setColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_BackgroundColor:
          spline.getSettings().setBackgroundColor(property.getDataAsColorRef(colors));
          break;
        case CDXProp_Curve_Type:
          CDSplineType curveType = readCurveTypeProperty(property);
          spline.setFillType(curveType.getFillType());
          spline.setLineType(curveType.getLineType());
          spline.setClosed(curveType.isClosed());
          break;
        case CDXProp_Curve_Points:
          spline.setPoints2D(property.getDataAsPoint2DArray());
          break;
        case CDXProp_Curve_Points3D:
          spline.setPoints3D(property.getDataAsPoint3DArray());
          break;

        case CDXProp_LineWidth:
          spline.getSettings().setLineWidth(property.getDataAsCoordinate());
          break;
        case CDXProp_HashSpacing:
          spline.getSettings().setHashSpacing(property.getDataAsCoordinate());
          break;
        case CDXProp_BoldWidth:
          spline.getSettings().setBoldWidth(property.getDataAsCoordinate());
          break;

        case CDXProp_Curve_ArrowheadType:
          spline.setArrowHeadType(readArrowheadTypeProperty(property));
          break;
        case CDXProp_Curve_ArrowheadHead:
          spline.setArrowHeadPositionAtStart(readArrowheadProperty(property));
          break;
        case CDXProp_Curve_ArrowheadTail:
          spline.setArrowHeadPositionAtEnd(readArrowheadProperty(property));
          break;
        case CDXProp_Curve_Closed:
          spline.setClosed(property.getDataAsBoolean());
          break;

        case CDXProp_Line_Type:
          spline.setLineType(readLineTypeProperty(property));
          break;
        case CDXProp_Curve_FillType:
          spline.setFillType(readFillTypeProperty(property));
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
        case CDXObj_Text:
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
        case CDXProp_ObjectTag_Type:
          objectTag.setObjectTagType(readObjectTagTypeProperty(property));
          break;
        default:
          break;
      }
    }

    for (CDXProperty property : root.getProperties()) {
      handleProperty(property);
      switch (property.getTag()) {
        case CDXProp_Visible:
          objectTag.setVisible(property.getDataAsBoolean());
          break;
        case CDXProp_Name:
          objectTag.setName(property.getDataAsString());
          break;
        case CDXProp_ObjectTag_Type:
          objectTag.setObjectTagType(readObjectTagTypeProperty(property));
          break;
        case CDXProp_ObjectTag_Tracking:
          objectTag.setTracking(property.getDataAsBoolean());
          break;
        case CDXProp_ObjectTag_Persistent:
          objectTag.setPersistent(property.getDataAsBoolean());
          break;
        case CDXProp_ObjectTag_Value:
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
        case CDXProp_Positioning:
          objectTag.setPositioningType(readPositioningTypeProperty(property));
          break;
        case CDXProp_PositioningAngle:
          objectTag.setPositioningAngle(property.getDataAsCoordinate());
          break;
        case CDXProp_PositioningOffset:
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
        case CDXProp_BasisObjects:
          chemicalProperty.setBasisObjects(
              property.getDataAsObjectRefArray(Object.class, refManager));
          break;
        case CDXProp_ChemicalPropertyType:
          chemicalProperty.setType(property.getDataAsInt32());
          break;
        case CDXProp_ChemicalPropertyDisplayID:
          chemicalProperty.setDisplay(property.getDataAsObjectRef(Object.class, refManager));
          break;
        case CDXProp_ChemicalPropertyIsActive:
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
            getPositionAsString(object));
        continue;
      }
      switch (object.getTag()) {
        case CDXObj_Document:
          populateDocumentObject(object);
          break;
        case CDXObj_Page:
          populatePageObject(object);
          break;
        case CDXObj_Group:
          populateGroupObject(object);
          break;
        case CDXObj_Fragment:
          populateFragmentObject(object);
          break;
        case CDXObj_Node:
          populateNodeObject(object);
          break;
        case CDXObj_Bond:
          populateBondObject(object);
          break;
        case CDXObj_Text:
          populateTextObject(object);
          break;
        case CDXObj_Graphic:
          populateGraphicObject(object);
          break;
        case CDXObj_Curve:
          populateSplineObject(object);
          break;
        case CDXObj_EmbeddedObject:
          populateEmbeddedObjectObject(object);
          break;
        case CDXObj_NamedAlternativeGroup:
          populateNamedAlternativeGroupObject(object);
          break;
        case CDXObj_TemplateGrid:
          populateTemplateGridObject(object);
          break;
        case CDXObj_ReactionScheme:
          populateReactionSchemeObject(object);
          break;
        case CDXObj_ReactionStep:
          populateReactionStepObject(object);
          break;
        case CDXObj_Spectrum:
          populateSpectrumObject(object);
          break;
        case CDXObj_ObjectTag:
          populateObjectTagObject(object);
          break;
        case CDXObj_Sequence:
          populateSequenceObject(object);
          break;
        case CDXObj_CrossReference:
          populateCrossReferenceObject(object);
          break;
        case CDXObj_Splitter:
          populateSplitterObject(object);
          break;
        case CDXObj_Table:
          populateTableObject(object);
          break;
        case CDXObj_BracketedGroup:
          populateBracketedGroupObject(object);
          break;
        case CDXObj_BracketAttachment:
          populateBracketAttachmentObject(object);
          break;
        case CDXObj_CrossingBond:
          populateCrossingBondObject(object);
          break;
        case CDXObj_Border:
          populateBorderObject(object);
          break;
        case CDXObj_Geometry:
          populateGeometryObject(object);
          break;
        case CDXObj_Constraint:
          populateConstraintObject(object);
          break;
        case CDXObj_TLCPlate:
          populateTLCPlateObject(object);
          break;
        case CDXObj_TLCLane:
          populateTLCLaneObject(object);
          break;
        case CDXObj_TLCSpot:
          populateTLCSpotObject(object);
          break;
        case CDXObj_ChemicalProperty:
          populateChemicalPropertyObject(object);
          break;

        case CDXObj_Arrow:
          populateArrowObject(object);
          break;

        case CDXObj_ColoredMolecularArea:
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
          getPositionAsString(object));
    }
  }

  private void handlePopulation(String name, CDXObject object) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "populate {} object with id {}(0x{}) at {}",
          name,
          object.getId(),
          Integer.toHexString(object.getId()),
          getPositionAsString(object));
    }
  }

  private void handleProperty(CDXProperty property) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "handle tag {} (0x{}) at {} with length {}(0x{})",
          property.getTag(),
          Integer.toHexString(property.getTag()),
          getPositionAsString(property),
          property.getLength(),
          Integer.toHexString(property.getLength()));
    }
  }

  private void handleMissingTag(CDXObject object) throws IOException {
    String message =
        "Object with tag 0x"
            + Integer.toHexString(object.getTag())
            + " not recognized at "
            + getPositionAsString(object);
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
            + getPositionAsString(property);
    if (RIGID) {
      throw new IOException(message);
    }
    LOGGER.warn(message);
  }
}
