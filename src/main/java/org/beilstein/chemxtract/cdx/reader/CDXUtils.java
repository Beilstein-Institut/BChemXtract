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

import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Any;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Deficient;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Enriched;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Natural;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Nonnatural;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAbundance_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowNoGo_Cross;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowNoGo_Hash;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowNoGo_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowNoGo_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_Equilibrium;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_FullHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_HalfHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_Hollow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_NoHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_Resonance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowType_RetroSynthetic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowheadType_Angle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowheadType_Hollow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowheadType_Solid;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowhead_Full;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowhead_HalfLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowhead_HalfRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowhead_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXArrowhead_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_10Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_1Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_5Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_6Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_7Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_8Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_9Ligand;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_Bent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_Linear;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_Octahedral;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_SquarePlanar;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_SquarePyramidal;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_Tetrahedral;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_TrigonalBipyramidal;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_TrigonalPlanar;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_TrigonalPyramidal;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXAtomGeometry_Unknown;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Dash;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_DashDot;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Dot;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Hash;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_HollowWedgeBegin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_HollowWedgeEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Solid;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_Wavy;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WavyWedgeBegin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WavyWedgeEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WedgeBegin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WedgeEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WedgedHashBegin;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDisplay_WedgedHashEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_AutoCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_AutoLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_AutoRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_UserCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_UserLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondDoublePosition_UserRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Any;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Dative;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Double;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_DoubleOrAromatic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_FiveHalf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_FourHalf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Half;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Hydrogen;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Ionic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_OneHalf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Quadruple;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Quintuple;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Sextuple;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Single;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_SingleOrAromatic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_SingleOrDouble;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_ThreeCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_ThreeHalf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_Triple;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondOrder_TwoHalf;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_ChangeType;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_MakeAndChange;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_MakeOrBreak;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_NoChange;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_NotReactionCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_ReactionCenter;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_Unmapped;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondReactionParticipation_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondTopology_Chain;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondTopology_Ring;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondTopology_RingOrChain;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBondTopology_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_Curly;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_CurlyPair;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_Round;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_RoundPair;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_Square;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketType_SquarePair;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Anypolymer;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Component;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Copolymer;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_CopolymerAlternating;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_CopolymerBlock;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_CopolymerRandom;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Crosslink;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Generic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Graft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Mer;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_MixtureOrdered;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_MixtureUnordered;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Modification;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Monomer;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_MultipleGroup;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_SRU;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Unused1;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXBracketUsage_Unused2;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_R;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_S;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_Undetermined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_r;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPAtom_s;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPBond_E;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPBond_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPBond_Undetermined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCIPBond_Z;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXConstraintType_Angle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXConstraintType_Distance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXConstraintType_ExclusionSphere;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXConstraintType_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_ArrowAtEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_ArrowAtStart;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Closed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Dashed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Doubled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Filled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_HalfArrowAtEnd;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_HalfArrowAtStart;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXCurveType_Shaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXDrawingSpace_Pages;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXDrawingSpace_Poster;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_Diamond;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_PolymerBead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_Residue;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_Star;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXExternalConnection_Wavy;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFillType_Faded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFillType_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFillType_Shaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFillType_Solid;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFillType_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Formula;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Italic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Outline;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Shadow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Subscript;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Superscript;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXFontFace_Underline;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_CentroidFromPoints;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_LineFromPoints;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_NormalFromPointPlane;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_PlaneFromPointLine;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_PlaneFromPoints;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_PointFromPointNormalDistance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_PointFromPointPointDistance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_PointFromPointPointPercentage;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGeometricFeature_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Arc;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Bracket;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Line;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Orbital;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Oval;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Rectangle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Symbol;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXGraphicType_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Above;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Auto;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Below;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_BestInitial;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Center;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Left;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLabelDisplay_Right;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLineType_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLineType_Dashed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXLineType_Wavy;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_AnonymousAlternativeGroup;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_Element;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_ElementList;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_ElementListNickname;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_ExternalConnectionPoint;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_Formula;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_Fragment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_GenericNickname;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_LinkNode;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_MultiAttachment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_NamedAlternativeGroup;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_Nickname;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXNodeType_VariableAttachment;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObjectTagType_Double;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObjectTagType_Long;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObjectTagType_String;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXObjectTagType_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dxy;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dxyFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dz2Minus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dz2MinusFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dz2Plus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_dz2PlusFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_hybridMinus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_hybridMinusFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_hybridPlus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_hybridPlusFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_lobe;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_lobeFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_lobeShaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_oval;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_ovalFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_ovalShaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_p;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_pFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_pShaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_s;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_sFilled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOrbitalType_sShaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Circle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Dashed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Filled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Shaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXOvalType_Shadowed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_Center;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_FlushLeft;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_FlushRight;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_IDTerm;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_MulticolumnNonTL4;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_MulticolumnTL4;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_Reaction1;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_Reaction2;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_TL4;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPageDefinition_UserDefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerFlipType_Flip;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerFlipType_NoFlip;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerFlipType_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerRepeatPattern_EitherUnknown;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerRepeatPattern_HeadToHead;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPolymerRepeatPattern_HeadToTail;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPositioningType_Absolute;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPositioningType_Angle;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPositioningType_Auto;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXPositioningType_Offset;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXProp_EndObject;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRadical_Doublet;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRadical_None;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRadical_Singlet;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRadical_Triplet;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXReactionStereo_Inversion;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXReactionStereo_Retention;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXReactionStereo_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_Bold;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_Dashed;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_Filled;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_RoundEdge;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_Shaded;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRectangleType_Shadow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_AsDrawn;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_Fusion;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_NoRingBonds;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_SimpleRing;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_SpiroOrHigher;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXRingBondCount_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSideType_Bottom;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSideType_Left;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSideType_Right;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSideType_Top;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSideType_Undefined;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Atomic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Chromatogram;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Fluorescence;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Infrared;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_MassSpectrum;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_NMR;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Raman;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_UVVis;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_Unknown;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumClass_XRayDiffraction;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_Hertz;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_MassUnits;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_Microns;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_Other;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_PartsPerMillion;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_Unknown;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumXType_Wavenumbers;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_Absorbance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_ArbitraryUnits;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_Other;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_PercentTransmittance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_Transmittance;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSpectrumYType_Unknown;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Absolute;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_CircleMinus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_CirclePlus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Dagger;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_DoubleDagger;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Electron;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_LonePair;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Minus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Plus;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Racemic;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_RadicalAnion;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_RadicalCation;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXSymbolType_Relative;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTag_Object;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Above;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Auto;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Below;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_BestInitial;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Center;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Full;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Left;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTextJustification_Right;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTranslation_Any;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTranslation_Broad;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTranslation_Equal;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXTranslation_Narrow;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXUnsaturation_MustBeAbsent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXUnsaturation_MustBePresent;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.CDXUnsaturation_Unspecified;
import static org.beilstein.chemxtract.cdx.reader.CDXConstants.getCdxSignature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAltGroup;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDGroup;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDSettings;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadPositionType;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadType;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDAtomGeometry;
import org.beilstein.chemxtract.cdx.datatypes.CDBondCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDoublePosition;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDBondReactionParticipation;
import org.beilstein.chemxtract.cdx.datatypes.CDBondTopology;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketType;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;
import org.beilstein.chemxtract.cdx.datatypes.CDConstraintType;
import org.beilstein.chemxtract.cdx.datatypes.CDDrawingSpaceType;
import org.beilstein.chemxtract.cdx.datatypes.CDExternalConnectionType;
import org.beilstein.chemxtract.cdx.datatypes.CDFillType;
import org.beilstein.chemxtract.cdx.datatypes.CDFontFace;
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
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerFlipType;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerRepeatPattern;
import org.beilstein.chemxtract.cdx.datatypes.CDPositioningType;
import org.beilstein.chemxtract.cdx.datatypes.CDRadical;
import org.beilstein.chemxtract.cdx.datatypes.CDReactionStereo;
import org.beilstein.chemxtract.cdx.datatypes.CDRectangleType;
import org.beilstein.chemxtract.cdx.datatypes.CDRingBondCount;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a set of static helper methods to do binary conversions and convert CDX
 * constant values to Java enums.
 */
public class CDXUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CDXUtils.class);

  public static boolean isCDX(byte[] bytes) {
    return IOUtils.startsWithBytes(bytes, getCdxSignature());
  }

  /**
   * The main entry for reading a binary CDX document.
   *
   * @param bytes the raw CDX document bytes
   * @param position single-element cursor holding the current read offset into {@code bytes}
   * @return the root {@link CDXObject} parsed from the document
   * @throws IOException If header is not recognized.
   */
  public static CDXObject readCDXDocument(byte[] bytes, int[] position) throws IOException {
    // read header string
    for (byte element : getCdxSignature()) {
      if (bytes[position[0]++] != element) {
        throw new IOException("Header not recognized");
      }
    }

    // read reserved bytes for backward compatibility
    position[0] += 4;

    // read reserved bytes
    position[0] += 10;
    // position += 16;

    int tag = readUInt16(bytes, position[0]);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read root tag={} position={}",
          Integer.toHexString(tag),
          Integer.toHexString(position[0]));
    }
    position[0] += 2;

    return readCDXObject(tag, bytes, position);
  }

  private static CDXObject readCDXObject(int rootTag, byte[] bytes, int[] position)
      throws IOException {
    // read object id
    int id = readInt32(bytes, position[0]);
    position[0] += 4;

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read object with tag 0x{} and  id {}(0x{}) at {}(0x{})",
          Integer.toHexString(rootTag),
          id,
          Integer.toHexString(id),
          position[0] - 6,
          Integer.toHexString(position[0] - 6));
    }

    CDXObject object = new CDXObject();
    object.setTag(rootTag);
    object.setId(id);
    object.setPosition(position[0] - 6);

    // read content
    while (position[0] < bytes.length) {
      int tag = readUInt16(bytes, position[0]);
      position[0] += 2;
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "read tag=0x{} at {}(0x{})",
            Integer.toHexString(tag),
            position[0],
            Integer.toHexString(position[0]));
      }
      if (tag == CDXProp_EndObject) {
        break;
      } else if (tag >= CDXTag_Object) {
        CDXObject object2 = readCDXObject(tag, bytes, position);
        object2.setTag(tag);
        object.addObject(object2);
      } else {
        CDXProperty property = readCDXProperty(tag, bytes, position);
        object.addProperty(property);
      }
    }
    return object;
  }

  private static CDXProperty readCDXProperty(int tag, byte[] bytes, int[] position)
      throws IOException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "read property with tag 0x{} at {}(0x{})",
          Integer.toHexString(tag),
          position[0] - 2,
          Integer.toHexString(position[0] - 2));
    }

    CDXProperty property = new CDXProperty();
    property.setTag(tag);
    property.setPosition(position[0] - 2);

    // read property length
    int length = readUInt16(bytes, position[0]);
    position[0] += 2;
    if (length == 0xFFFF) {
      length = readInt32(bytes, position[0]);
      position[0] += 4;
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("property length={}", length);
    }
    property.setLength(length);

    if (position[0] + length >= bytes.length) {
      throw new IOException(
          "Property size doesn't fit into the remaining data at " + getPositionAsString(property));
    }

    // read data
    byte[] data = new byte[length];
    System.arraycopy(bytes, position[0], data, 0, length);
    position[0] += length;
    property.setData(data);

    return property;
  }

  public static int readUInt8(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff;
  }

  public static int readInt8(byte[] bytes, int offset) {
    return bytes[offset + 0];
  }

  public static short readInt16(byte[] bytes, int offset) {
    short a = (short) (bytes[offset + 0] & (short) 0xff);
    short b = (short) (bytes[offset + 1] << 8);

    return (short) (a | b);
  }

  public static int readUInt16(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff | (bytes[offset + 1] & 0xff) << 8;
  }

  public static int readInt32(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0xff
        | (bytes[offset + 1] & 0xff) << 8
        | (bytes[offset + 2] & 0xff) << 16
        | (bytes[offset + 3] & 0xff) << 24;
  }

  public static long readUInt32(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0x00000000000000ffL
        | (bytes[offset + 1] & 0x00000000000000ffL) << 8
        | (bytes[offset + 2] & 0x00000000000000ffL) << 16
        | (bytes[offset + 3] & 0x00000000000000ffL) << 24;
  }

  public static long readInt64(byte[] bytes, int offset) {
    return bytes[offset + 0] & 0x00000000000000ffL
        | (bytes[offset + 1] & 0x00000000000000ffL) << 8
        | (bytes[offset + 2] & 0x00000000000000ffL) << 16
        | (bytes[offset + 3] & 0x00000000000000ffL) << 24
        | (bytes[offset + 4] & 0x00000000000000ffL) << 32
        | (bytes[offset + 5] & 0x00000000000000ffL) << 40
        | (bytes[offset + 6] & 0x00000000000000ffL) << 48
        | (bytes[offset + 7] & 0x00000000000000ffL) << 56;
  }

  public static double readFloat64(byte[] bytes, int offset) {
    return Double.longBitsToDouble(readInt64(bytes, offset));
  }

  public static float readFixedPoint(byte[] bytes, int offset) {
    return readInt32(bytes, offset) / 65536f;
  }

  public static CDFontFace convertIntToFontFace(int type) {
    CDFontFace fontType = new CDFontFace();
    if ((type & CDXFontFace_Bold) != 0) {
      fontType.setBold(true);
    }
    if ((type & CDXFontFace_Italic) != 0) {
      fontType.setItalic(true);
    }
    if ((type & CDXFontFace_Underline) != 0) {
      fontType.setUnderline(true);
    }
    if ((type & CDXFontFace_Outline) != 0) {
      fontType.setOutline(true);
    }
    if ((type & CDXFontFace_Shadow) != 0) {
      fontType.setShadow(true);
    }

    // special handling for formula
    if ((type & CDXFontFace_Formula) == CDXFontFace_Formula) {
      fontType.setFormula(true);
    } else if ((type & CDXFontFace_Subscript) != 0) {
      fontType.setSubscript(true);
    } else if ((type & CDXFontFace_Superscript) != 0) {
      fontType.setSuperscript(true);
    }
    return fontType;
  }

  public static int convertFontType(CDFontFace fontType) {
    int value = 0;
    if (fontType.isBold()) {
      value |= CDXFontFace_Bold;
    }
    if (fontType.isItalic()) {
      value |= CDXFontFace_Italic;
    }
    if (fontType.isUnderline()) {
      value |= CDXFontFace_Underline;
    }
    if (fontType.isOutline()) {
      value |= CDXFontFace_Outline;
    }
    if (fontType.isShadow()) {
      value |= CDXFontFace_Shadow;
    }

    // special handling for formula
    if (fontType.isFormula()) {
      value |= CDXFontFace_Formula;
    } else if (fontType.isSubscript()) {
      value |= CDXFontFace_Subscript;
    } else if (fontType.isSuperscript()) {
      value |= CDXFontFace_Superscript;
    }
    return value;
  }

  public static CDSplineType convertIntToSplineType(int value) {
    CDSplineType curveType = new CDSplineType();
    if ((value & CDXCurveType_Closed) != 0) {
      curveType.setClosed(true);
    }
    if ((value & CDXCurveType_Dashed) != 0) {
      curveType.setDashed(true);
    }
    if ((value & CDXCurveType_Bold) != 0) {
      curveType.setBold(true);
    }
    if ((value & CDXCurveType_ArrowAtEnd) != 0) {
      curveType.setArrowAtEnd(true);
    }
    if ((value & CDXCurveType_ArrowAtStart) != 0) {
      curveType.setArrowAtStart(true);
    }
    if ((value & CDXCurveType_HalfArrowAtEnd) != 0) {
      curveType.setHalfArrowAtEnd(true);
    }
    if ((value & CDXCurveType_HalfArrowAtStart) != 0) {
      curveType.setHalfArrowAtStart(true);
    }
    if ((value & CDXCurveType_Filled) != 0) {
      curveType.setFilled(true);
    }
    if ((value & CDXCurveType_Shaded) != 0) {
      curveType.setShaded(true);
    }
    if ((value & CDXCurveType_Doubled) != 0) {
      curveType.setDoubled(true);
    }
    return curveType;
  }

  public static int convertCurveTypeToInt(CDSplineType curveType) {
    int value = 0;
    if (curveType.isClosed()) {
      value |= CDXCurveType_Closed;
    }
    if (curveType.isDashed()) {
      value |= CDXCurveType_Dashed;
    }
    if (curveType.isBold()) {
      value |= CDXCurveType_Bold;
    }
    if (curveType.isArrowAtEnd()) {
      value |= CDXCurveType_ArrowAtEnd;
    }
    if (curveType.isArrowAtStart()) {
      value |= CDXCurveType_ArrowAtStart;
    }
    if (curveType.isHalfArrowAtEnd()) {
      value |= CDXCurveType_HalfArrowAtEnd;
    }
    if (curveType.isHalfArrowAtStart()) {
      value |= CDXCurveType_HalfArrowAtStart;
    }
    if (curveType.isFilled()) {
      value |= CDXCurveType_Filled;
    }
    if (curveType.isShaded()) {
      value |= CDXCurveType_Shaded;
    }
    if (curveType.isDoubled()) {
      value |= CDXCurveType_Doubled;
    }
    return value;
  }

  public static CDBondCIPType readBondCIPTypeProperty(CDXProperty property) throws IOException {
    int type = property.getDataAsUInt8();
    switch (type) {
      case CDXCIPBond_Undetermined:
        return CDBondCIPType.Undetermined;
      case CDXCIPBond_None:
        return CDBondCIPType.None;
      case CDXCIPBond_E:
        return CDBondCIPType.E;
      case CDXCIPBond_Z:
        return CDBondCIPType.Z;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond CIP type 0x"
            + Integer.toHexString(type)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondCIPType.Undetermined;
  }

  public static CDJustification readTextJustificationProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsInt8();
    switch (value) {
      case CDXTextJustification_Right:
        return CDJustification.Right;
      case CDXTextJustification_Left:
        return CDJustification.Left;
      case CDXTextJustification_Center:
        return CDJustification.Center;
      case CDXTextJustification_Full:
        return CDJustification.Full;
      case CDXTextJustification_Above:
        return CDJustification.Above;
      case CDXTextJustification_Below:
        return CDJustification.Below;
      case CDXTextJustification_Auto:
        return CDJustification.Auto;
      case CDXTextJustification_BestInitial:
        return CDJustification.BestInitial;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Text justification 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDJustification.Auto;
  }

  public static CDPageDefinition readPageDefinitionProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXPageDefinition_Undefined:
        return CDPageDefinition.Undefined;
      case CDXPageDefinition_Center:
        return CDPageDefinition.Center;
      case CDXPageDefinition_TL4:
        return CDPageDefinition.TL4;
      case CDXPageDefinition_IDTerm:
        return CDPageDefinition.IDTerm;
      case CDXPageDefinition_FlushLeft:
        return CDPageDefinition.FlushLeft;
      case CDXPageDefinition_FlushRight:
        return CDPageDefinition.FlushRight;
      case CDXPageDefinition_Reaction1:
        return CDPageDefinition.Reaction1;
      case CDXPageDefinition_Reaction2:
        return CDPageDefinition.Reaction2;
      case CDXPageDefinition_MulticolumnTL4:
        return CDPageDefinition.MulticolumnTL4;
      case CDXPageDefinition_MulticolumnNonTL4:
        return CDPageDefinition.MulticolumnNonTL4;
      case CDXPageDefinition_UserDefined:
        return CDPageDefinition.UserDefined;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Page definition 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPageDefinition.Undefined;
  }

  public static CDDrawingSpaceType readDrawingSpaceTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXDrawingSpace_Pages:
        return CDDrawingSpaceType.Pages;
      case CDXDrawingSpace_Poster:
        return CDDrawingSpaceType.Poster;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Drawing space type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDDrawingSpaceType.Pages;
  }

  public static CDUnsaturation readUnsaturationProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXUnsaturation_Unspecified:
        return CDUnsaturation.Unspecified;
      case CDXUnsaturation_MustBeAbsent:
        return CDUnsaturation.MustBeAbsent;
      case CDXUnsaturation_MustBePresent:
        return CDUnsaturation.MustBePresent;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Unsaturation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDUnsaturation.Unspecified;
  }

  public static CDExternalConnectionType readExternalConnectionTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXExternalConnection_Unspecified:
        return CDExternalConnectionType.Unspecified;
      case CDXExternalConnection_Diamond:
        return CDExternalConnectionType.Diamond;
      case CDXExternalConnection_Star:
        return CDExternalConnectionType.Star;
      case CDXExternalConnection_PolymerBead:
        return CDExternalConnectionType.PolymerBead;
      case CDXExternalConnection_Wavy:
        return CDExternalConnectionType.Wavy;
      case CDXExternalConnection_Residue:
        return CDExternalConnectionType.Residue;
      default:
        break;
    }
    handleUnrecognizedValue(
        "External connection type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDExternalConnectionType.Unspecified;
  }

  public static CDIsotopicAbundance readAbundanceProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXAbundance_Unspecified:
        return CDIsotopicAbundance.Unspecified;
      case CDXAbundance_Any:
        return CDIsotopicAbundance.Any;
      case CDXAbundance_Natural:
        return CDIsotopicAbundance.Natural;
      case CDXAbundance_Enriched:
        return CDIsotopicAbundance.Enriched;
      case CDXAbundance_Deficient:
        return CDIsotopicAbundance.Deficient;
      case CDXAbundance_Nonnatural:
        return CDIsotopicAbundance.Nonnatural;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Abundance 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDIsotopicAbundance.Unspecified;
  }

  public static CDTranslation readTranslationProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXTranslation_Equal:
        return CDTranslation.Equal;
      case CDXTranslation_Broad:
        return CDTranslation.Broad;
      case CDXTranslation_Narrow:
        return CDTranslation.Narrow;
      case CDXTranslation_Any:
        return CDTranslation.Any;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Translation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDAtomCIPType readAtomCIPTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXCIPAtom_Undetermined:
        return CDAtomCIPType.Undetermined;
      case CDXCIPAtom_None:
        return CDAtomCIPType.None;
      case CDXCIPAtom_R:
        return CDAtomCIPType.R;
      case CDXCIPAtom_S:
        return CDAtomCIPType.S;
      case CDXCIPAtom_r:
        return CDAtomCIPType.PseudoR;
      case CDXCIPAtom_s:
        return CDAtomCIPType.PseudoS;
      case CDXCIPAtom_Unspecified:
        return CDAtomCIPType.Unspecified;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Atom CIP type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDAtomCIPType.Unspecified;
  }

  public static CDAtomGeometry readAtomGeometryProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXAtomGeometry_Unknown:
        return CDAtomGeometry.Unknown;
      case CDXAtomGeometry_1Ligand:
        return CDAtomGeometry.OneLigand;
      case CDXAtomGeometry_Linear:
        return CDAtomGeometry.Linear;
      case CDXAtomGeometry_Bent:
        return CDAtomGeometry.Bent;
      case CDXAtomGeometry_TrigonalPlanar:
        return CDAtomGeometry.TrigonalPlanar;
      case CDXAtomGeometry_TrigonalPyramidal:
        return CDAtomGeometry.TrigonalPyramidal;
      case CDXAtomGeometry_SquarePlanar:
        return CDAtomGeometry.SquarePlanar;
      case CDXAtomGeometry_Tetrahedral:
        return CDAtomGeometry.Tetrahedral;
      case CDXAtomGeometry_TrigonalBipyramidal:
        return CDAtomGeometry.TrigonalBipyramidal;
      case CDXAtomGeometry_SquarePyramidal:
        return CDAtomGeometry.SquarePyramidal;
      case CDXAtomGeometry_5Ligand:
        return CDAtomGeometry.FiveLigand;
      case CDXAtomGeometry_Octahedral:
        return CDAtomGeometry.Octahedral;
      case CDXAtomGeometry_6Ligand:
        return CDAtomGeometry.SixLigand;
      case CDXAtomGeometry_7Ligand:
        return CDAtomGeometry.SevenLigand;
      case CDXAtomGeometry_8Ligand:
        return CDAtomGeometry.EightLigand;
      case CDXAtomGeometry_9Ligand:
        return CDAtomGeometry.NineLigand;
      case CDXAtomGeometry_10Ligand:
        return CDAtomGeometry.TenLigand;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Atom geometry 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDAtomGeometry.Unknown;
  }

  public static CDReactionStereo readReactionStereoProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXReactionStereo_Unspecified:
        return CDReactionStereo.Unspecified;
      case CDXReactionStereo_Inversion:
        return CDReactionStereo.Inversion;
      case CDXReactionStereo_Retention:
        return CDReactionStereo.Retention;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Reaction stereo 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDReactionStereo.Unspecified;
  }

  public static CDRadical readRadicalProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXRadical_None:
        return CDRadical.None;
      case CDXRadical_Singlet:
        return CDRadical.Singlet;
      case CDXRadical_Doublet:
        return CDRadical.Doublet;
      case CDXRadical_Triplet:
        return CDRadical.Triplet;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Radical 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDRadical.None;
  }

  public static int[] readElementListProperty(CDXProperty property) throws IOException {
    return property.getDataAsInt16Array();
  }

  public static CDLabelDisplay readLabelDisplayProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXLabelDisplay_Auto:
        return CDLabelDisplay.Auto;
      case CDXLabelDisplay_Left:
        return CDLabelDisplay.Left;
      case CDXLabelDisplay_Center:
        return CDLabelDisplay.Center;
      case CDXLabelDisplay_Right:
        return CDLabelDisplay.Right;
      case CDXLabelDisplay_Above:
        return CDLabelDisplay.Above;
      case CDXLabelDisplay_Below:
        return CDLabelDisplay.Below;
      case CDXLabelDisplay_BestInitial:
        return CDLabelDisplay.BestInitial;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Radical 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDLabelDisplay.Auto;
  }

  public static CDNodeType readNodeTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXNodeType_Unspecified:
        return CDNodeType.Unspecified;
      case CDXNodeType_Element:
        return CDNodeType.Element;
      case CDXNodeType_ElementList:
        return CDNodeType.ElementList;
      case CDXNodeType_ElementListNickname:
        return CDNodeType.ElementListNickname;
      case CDXNodeType_Nickname:
        return CDNodeType.Nickname;
      case CDXNodeType_Fragment:
        return CDNodeType.Fragment;
      case CDXNodeType_Formula:
        return CDNodeType.Formula;
      case CDXNodeType_GenericNickname:
        return CDNodeType.GenericNickname;
      case CDXNodeType_AnonymousAlternativeGroup:
        return CDNodeType.AnonymousAlternativeGroup;
      case CDXNodeType_NamedAlternativeGroup:
        return CDNodeType.NamedAlternativeGroup;
      case CDXNodeType_MultiAttachment:
        return CDNodeType.MultiAttachment;
      case CDXNodeType_VariableAttachment:
        return CDNodeType.VariableAttachment;
      case CDXNodeType_ExternalConnectionPoint:
        return CDNodeType.ExternalConnectionPoint;
      case CDXNodeType_LinkNode:
        return CDNodeType.LinkNode;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Node type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDNodeType.Unspecified;
  }

  public static CDBondReactionParticipation readBondReactionParticipationProperty(
      CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXBondReactionParticipation_Unspecified:
        return CDBondReactionParticipation.Unspecified;
      case CDXBondReactionParticipation_ReactionCenter:
        return CDBondReactionParticipation.ReactionCenter;
      case CDXBondReactionParticipation_MakeOrBreak:
        return CDBondReactionParticipation.MakeOrBreak;
      case CDXBondReactionParticipation_ChangeType:
        return CDBondReactionParticipation.ChangeType;
      case CDXBondReactionParticipation_MakeAndChange:
        return CDBondReactionParticipation.MakeAndChange;
      case CDXBondReactionParticipation_NotReactionCenter:
        return CDBondReactionParticipation.NotReactionCenter;
      case CDXBondReactionParticipation_NoChange:
        return CDBondReactionParticipation.NoChange;
      case CDXBondReactionParticipation_Unmapped:
        return CDBondReactionParticipation.Unmapped;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond reaction participation 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondReactionParticipation.Unspecified;
  }

  public static CDBondTopology readBondTopologyProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXBondTopology_Unspecified:
        return CDBondTopology.Unspecified;
      case CDXBondTopology_Ring:
        return CDBondTopology.Ring;
      case CDXBondTopology_Chain:
        return CDBondTopology.Chain;
      case CDXBondTopology_RingOrChain:
        return CDBondTopology.RingOrChain;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond topology 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondTopology.Unspecified;
  }

  public static CDBondDoublePosition readBondDoublePositionProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXBondDoublePosition_AutoCenter:
        return CDBondDoublePosition.AutoCenter;
      case CDXBondDoublePosition_AutoRight:
        return CDBondDoublePosition.AutoRight;
      case CDXBondDoublePosition_AutoLeft:
        return CDBondDoublePosition.AutoLeft;
      case CDXBondDoublePosition_UserCenter:
        return CDBondDoublePosition.UserCenter;
      case CDXBondDoublePosition_UserRight:
        return CDBondDoublePosition.UserRight;
      case CDXBondDoublePosition_UserLeft:
        return CDBondDoublePosition.UserLeft;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond double position 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBondDisplay readBondDisplayProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXBondDisplay_Solid:
        return CDBondDisplay.Solid;
      case CDXBondDisplay_Dash:
        return CDBondDisplay.Dash;
      case CDXBondDisplay_Hash:
        return CDBondDisplay.Hash;
      case CDXBondDisplay_WedgedHashBegin:
        return CDBondDisplay.WedgedHashBegin;
      case CDXBondDisplay_WedgedHashEnd:
        return CDBondDisplay.WedgedHashEnd;
      case CDXBondDisplay_Bold:
        return CDBondDisplay.Bold;
      case CDXBondDisplay_WedgeBegin:
        return CDBondDisplay.WedgeBegin;
      case CDXBondDisplay_WedgeEnd:
        return CDBondDisplay.WedgeEnd;
      case CDXBondDisplay_Wavy:
        return CDBondDisplay.Wavy;
      case CDXBondDisplay_HollowWedgeBegin:
        return CDBondDisplay.HollowWedgeBegin;
      case CDXBondDisplay_HollowWedgeEnd:
        return CDBondDisplay.HollowWedgeEnd;
      case CDXBondDisplay_WavyWedgeBegin:
        return CDBondDisplay.WavyWedgeBegin;
      case CDXBondDisplay_WavyWedgeEnd:
        return CDBondDisplay.WavyWedgeEnd;
      case CDXBondDisplay_Dot:
        return CDBondDisplay.Dot;
      case CDXBondDisplay_DashDot:
        return CDBondDisplay.DashDot;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bond display 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondDisplay.Solid;
  }

  public static CDBondOrder readBondOrdersProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXBondOrder_Single:
        return CDBondOrder.Single;
      case CDXBondOrder_Double:
        return CDBondOrder.Double;
      case CDXBondOrder_Triple:
        return CDBondOrder.Triple;
      case CDXBondOrder_Quadruple:
        return CDBondOrder.Quadruple;
      case CDXBondOrder_Quintuple:
        return CDBondOrder.Quintuple;
      case CDXBondOrder_Sextuple:
        return CDBondOrder.Sextuple;
      case CDXBondOrder_Half:
        return CDBondOrder.Half;
      case CDXBondOrder_OneHalf:
        return CDBondOrder.OneHalf;
      case CDXBondOrder_TwoHalf:
        return CDBondOrder.TwoHalf;
      case CDXBondOrder_ThreeHalf:
        return CDBondOrder.ThreeHalf;
      case CDXBondOrder_FourHalf:
        return CDBondOrder.FourHalf;
      case CDXBondOrder_FiveHalf:
        return CDBondOrder.FiveHalf;
      case CDXBondOrder_Dative:
        return CDBondOrder.Dative;
      case CDXBondOrder_Ionic:
        return CDBondOrder.Ionic;
      case CDXBondOrder_Hydrogen:
        return CDBondOrder.Hydrogen;
      case CDXBondOrder_ThreeCenter:
        return CDBondOrder.ThreeCenter;
      case CDXBondOrder_SingleOrDouble:
        return CDBondOrder.SingleOrDouble;
      case CDXBondOrder_SingleOrAromatic:
        return CDBondOrder.SingleOrAromatic;
      case CDXBondOrder_DoubleOrAromatic:
        return CDBondOrder.DoubleOrAromatic;
      case CDXBondOrder_Any:
        return CDBondOrder.Any;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bonder order 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBondOrder.Single;
  }

  public static CDGraphicType readGraphicTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXGraphicType_Undefined:
        return CDGraphicType.Undefined;
      case CDXGraphicType_Line:
        return CDGraphicType.Line;
      case CDXGraphicType_Arc:
        return CDGraphicType.Arc;
      case CDXGraphicType_Rectangle:
        return CDGraphicType.Rectangle;
      case CDXGraphicType_Oval:
        return CDGraphicType.Oval;
      case CDXGraphicType_Orbital:
        return CDGraphicType.Orbital;
      case CDXGraphicType_Bracket:
        return CDGraphicType.Bracket;
      case CDXGraphicType_Symbol:
        return CDGraphicType.Symbol;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Graphic type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDGraphicType.Undefined;
  }

  public static CDLineType readLineTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    // Bug: Combinations of line types are not supported by CDXML
    CDLineType lineType = new CDLineType();
    if ((value & CDXLineType_Dashed) == CDXLineType_Dashed) {
      lineType.setDashed(true);
    }
    if ((value & CDXLineType_Bold) == CDXLineType_Bold) {
      lineType.setBold(true);
    }
    if ((value & CDXLineType_Wavy) == CDXLineType_Wavy) {
      lineType.setWavy(true);
    }
    return lineType;
  }

  public static CDArrowType readArrowTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXArrowType_NoHead:
        return CDArrowType.NoHead;
      case CDXArrowType_HalfHead:
        return CDArrowType.HalfHead;
      case CDXArrowType_FullHead:
        return CDArrowType.FullHead;
      case CDXArrowType_Resonance:
        return CDArrowType.Resonance;
      case CDXArrowType_Equilibrium:
        return CDArrowType.Equilibrium;
      case CDXArrowType_Hollow:
        return CDArrowType.Hollow;
      case CDXArrowType_RetroSynthetic:
        return CDArrowType.RetroSynthetic;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowType.NoHead;
  }

  public static CDRectangleType readRectangleTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    CDRectangleType rectangleType = new CDRectangleType();
    if ((value & CDXRectangleType_RoundEdge) != 0) {
      rectangleType.setRoundEdge(true);
    }
    if ((value & CDXRectangleType_Shadow) != 0) {
      rectangleType.setShadow(true);
    }
    if ((value & CDXRectangleType_Shaded) != 0) {
      rectangleType.setShaded(true);
    }
    if ((value & CDXRectangleType_Filled) != 0) {
      rectangleType.setFilled(true);
    }
    if ((value & CDXRectangleType_Dashed) != 0) {
      rectangleType.setDashed(true);
    }
    if ((value & CDXRectangleType_Bold) != 0) {
      rectangleType.setBold(true);
    }
    return rectangleType;
  }

  public static CDOvalType readOvalTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    CDOvalType ovalType = new CDOvalType();
    if ((value & CDXOvalType_Circle) != 0) {
      ovalType.setCircle(true);
    }
    if ((value & CDXOvalType_Shaded) != 0) {
      ovalType.setShaded(true);
    }
    if ((value & CDXOvalType_Filled) != 0) {
      ovalType.setFilled(true);
    }
    if ((value & CDXOvalType_Dashed) != 0) {
      ovalType.setDashed(true);
    }
    if ((value & CDXOvalType_Bold) != 0) {
      ovalType.setBold(true);
    }
    if ((value & CDXOvalType_Shadowed) != 0) {
      ovalType.setShadowed(true);
    }
    return ovalType;
  }

  public static CDSplineType readCurveTypeProperty(CDXProperty property) throws IOException {
    return convertIntToSplineType(property.getDataAsInt16());
  }

  public static CDFillType readFillTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXFillType_Unspecified:
        return CDFillType.Unspecified;
      case CDXFillType_None:
        return CDFillType.None;
      case CDXFillType_Solid:
        return CDFillType.Solid;
      case CDXFillType_Shaded:
        return CDFillType.Shaded;
      case CDXFillType_Faded:
        return CDFillType.Faded;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Fill type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDFillType.Unspecified;
  }

  public static CDOrbitalType readOrbitalTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXOrbitalType_s:
        return CDOrbitalType.s;
      case CDXOrbitalType_oval:
        return CDOrbitalType.oval;
      case CDXOrbitalType_lobe:
        return CDOrbitalType.lobe;
      case CDXOrbitalType_p:
        return CDOrbitalType.p;
      case CDXOrbitalType_hybridPlus:
        return CDOrbitalType.hybridPlus;
      case CDXOrbitalType_hybridMinus:
        return CDOrbitalType.hybridMinus;
      case CDXOrbitalType_dz2Plus:
        return CDOrbitalType.dz2Plus;
      case CDXOrbitalType_dz2Minus:
        return CDOrbitalType.dz2Minus;
      case CDXOrbitalType_dxy:
        return CDOrbitalType.dxy;
      case CDXOrbitalType_sShaded:
        return CDOrbitalType.sShaded;
      case CDXOrbitalType_ovalShaded:
        return CDOrbitalType.ovalShaded;
      case CDXOrbitalType_lobeShaded:
        return CDOrbitalType.lobeShaded;
      case CDXOrbitalType_pShaded:
        return CDOrbitalType.pShaded;
      case CDXOrbitalType_sFilled:
        return CDOrbitalType.sFilled;
      case CDXOrbitalType_ovalFilled:
        return CDOrbitalType.ovalFilled;
      case CDXOrbitalType_lobeFilled:
        return CDOrbitalType.lobeFilled;
      case CDXOrbitalType_pFilled:
        return CDOrbitalType.pFilled;
      case CDXOrbitalType_hybridPlusFilled:
        return CDOrbitalType.hybridPlusFilled;
      case CDXOrbitalType_hybridMinusFilled:
        return CDOrbitalType.hybridMinusFilled;
      case CDXOrbitalType_dz2PlusFilled:
        return CDOrbitalType.dz2PlusFilled;
      case CDXOrbitalType_dz2MinusFilled:
        return CDOrbitalType.dz2MinusFilled;
      case CDXOrbitalType_dxyFilled:
        return CDOrbitalType.dxyFilled;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Orbital type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBracketType readBracketTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXBracketType_RoundPair:
        return CDBracketType.RoundPair;
      case CDXBracketType_SquarePair:
        return CDBracketType.SquarePair;
      case CDXBracketType_CurlyPair:
        return CDBracketType.CurlyPair;
      case CDXBracketType_Square:
        return CDBracketType.Square;
      case CDXBracketType_Curly:
        return CDBracketType.Curly;
      case CDXBracketType_Round:
        return CDBracketType.Round;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bracket type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDSymbolType readSymbolTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXSymbolType_LonePair:
        return CDSymbolType.LonePair;
      case CDXSymbolType_Electron:
        return CDSymbolType.Electron;
      case CDXSymbolType_RadicalCation:
        return CDSymbolType.RadicalCation;
      case CDXSymbolType_RadicalAnion:
        return CDSymbolType.RadicalAnion;
      case CDXSymbolType_CirclePlus:
        return CDSymbolType.CirclePlus;
      case CDXSymbolType_CircleMinus:
        return CDSymbolType.CircleMinus;
      case CDXSymbolType_Dagger:
        return CDSymbolType.Dagger;
      case CDXSymbolType_DoubleDagger:
        return CDSymbolType.DoubleDagger;
      case CDXSymbolType_Plus:
        return CDSymbolType.Plus;
      case CDXSymbolType_Minus:
        return CDSymbolType.Minus;
      case CDXSymbolType_Racemic:
        return CDSymbolType.Racemic;
      case CDXSymbolType_Absolute:
        return CDSymbolType.Absolute;
      case CDXSymbolType_Relative:
        return CDSymbolType.Relative;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Symbol type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDBracketUsage readBracketUsageProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXBracketUsage_Unspecified:
        return CDBracketUsage.Unspecified;
      case CDXBracketUsage_Anypolymer:
        return CDBracketUsage.Anypolymer;
      case CDXBracketUsage_Component:
        return CDBracketUsage.Component;
      case CDXBracketUsage_Copolymer:
        return CDBracketUsage.Copolymer;
      case CDXBracketUsage_CopolymerAlternating:
        return CDBracketUsage.CopolymerAlternating;
      case CDXBracketUsage_CopolymerBlock:
        return CDBracketUsage.CopolymerBlock;
      case CDXBracketUsage_CopolymerRandom:
        return CDBracketUsage.CopolymerRandom;
      case CDXBracketUsage_Crosslink:
        return CDBracketUsage.Crosslink;
      case CDXBracketUsage_Generic:
        return CDBracketUsage.Generic;
      case CDXBracketUsage_Graft:
        return CDBracketUsage.Graft;
      case CDXBracketUsage_Mer:
        return CDBracketUsage.Mer;
      case CDXBracketUsage_MixtureOrdered:
        return CDBracketUsage.MixtureOrdered;
      case CDXBracketUsage_MixtureUnordered:
        return CDBracketUsage.MixtureUnordered;
      case CDXBracketUsage_Modification:
        return CDBracketUsage.Modification;
      case CDXBracketUsage_Monomer:
        return CDBracketUsage.Monomer;
      case CDXBracketUsage_MultipleGroup:
        return CDBracketUsage.MultipleGroup;
      case CDXBracketUsage_SRU:
        return CDBracketUsage.SRU;
      case CDXBracketUsage_Unused1:
        return CDBracketUsage.Unused1;
      case CDXBracketUsage_Unused2:
        return CDBracketUsage.Unused2;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Bracket usage 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDBracketUsage.Unspecified;
  }

  public static CDPolymerRepeatPattern readPolymerRepeatPatternProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXPolymerRepeatPattern_HeadToTail:
        return CDPolymerRepeatPattern.HeadToTail;
      case CDXPolymerRepeatPattern_HeadToHead:
        return CDPolymerRepeatPattern.HeadToHead;
      case CDXPolymerRepeatPattern_EitherUnknown:
        return CDPolymerRepeatPattern.EitherUnknown;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Polymer repeat pattern 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return null;
  }

  public static CDPolymerFlipType readPolymerFlipTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt();
    switch (value) {
      case CDXPolymerFlipType_Unspecified:
        return CDPolymerFlipType.Unspecified;
      case CDXPolymerFlipType_NoFlip:
        return CDPolymerFlipType.NoFlip;
      case CDXPolymerFlipType_Flip:
        return CDPolymerFlipType.Flip;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Polymer flip type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPolymerFlipType.Unspecified;
  }

  public static CDGeometryType readGeometricFeatureProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXGeometricFeature_Undefined:
        return CDGeometryType.Undefined;
      case CDXGeometricFeature_PointFromPointPointDistance:
        return CDGeometryType.PointFromPointPointDistance;
      case CDXGeometricFeature_PointFromPointPointPercentage:
        return CDGeometryType.PointFromPointPointPercentage;
      case CDXGeometricFeature_PointFromPointNormalDistance:
        return CDGeometryType.PointFromPointNormalDistance;
      case CDXGeometricFeature_LineFromPoints:
        return CDGeometryType.LineFromPoints;
      case CDXGeometricFeature_PlaneFromPoints:
        return CDGeometryType.PlaneFromPoints;
      case CDXGeometricFeature_PlaneFromPointLine:
        return CDGeometryType.PlaneFromPointLine;
      case CDXGeometricFeature_CentroidFromPoints:
        return CDGeometryType.CentroidFromPoints;
      case CDXGeometricFeature_NormalFromPointPlane:
        return CDGeometryType.NormalFromPointPlane;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Geometric feature 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDGeometryType.Undefined;
  }

  public static CDConstraintType readConstraintTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXConstraintType_Undefined:
        return CDConstraintType.Undefined;
      case CDXConstraintType_Distance:
        return CDConstraintType.Distance;
      case CDXConstraintType_Angle:
        return CDConstraintType.Angle;
      case CDXConstraintType_ExclusionSphere:
        return CDConstraintType.ExclusionSphere;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Constraint type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDConstraintType.Undefined;
  }

  public static CDSideType readSideTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXSideType_Undefined:
        return CDSideType.Undefined;
      case CDXSideType_Top:
        return CDSideType.Top;
      case CDXSideType_Left:
        return CDSideType.Left;
      case CDXSideType_Bottom:
        return CDSideType.Bottom;
      case CDXSideType_Right:
        return CDSideType.Right;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Side type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSideType.Undefined;
  }

  public static CDObjectTagType readObjectTagTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXObjectTagType_Undefined:
        return CDObjectTagType.Undefined;
      case CDXObjectTagType_Double:
        return CDObjectTagType.Double;
      case CDXObjectTagType_Long:
        return CDObjectTagType.Long;
      case CDXObjectTagType_String:
        return CDObjectTagType.String;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Object tag type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDObjectTagType.Undefined;
  }

  public static CDPositioningType readPositioningTypeProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXPositioningType_Auto:
        return CDPositioningType.Auto;
      case CDXPositioningType_Angle:
        return CDPositioningType.Angle;
      case CDXPositioningType_Offset:
        return CDPositioningType.Offset;
      case CDXPositioningType_Absolute:
        return CDPositioningType.Absolute;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Positioning type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDPositioningType.Auto;
  }

  public static CDSpectrumClass readSpectrumClassProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXSpectrumClass_Unknown:
        return CDSpectrumClass.Unknown;
      case CDXSpectrumClass_Chromatogram:
        return CDSpectrumClass.Chromatogram;
      case CDXSpectrumClass_Infrared:
        return CDSpectrumClass.Infrared;
      case CDXSpectrumClass_UVVis:
        return CDSpectrumClass.UVVis;
      case CDXSpectrumClass_XRayDiffraction:
        return CDSpectrumClass.XRayDiffraction;
      case CDXSpectrumClass_MassSpectrum:
        return CDSpectrumClass.MassSpectrum;
      case CDXSpectrumClass_NMR:
        return CDSpectrumClass.NMR;
      case CDXSpectrumClass_Raman:
        return CDSpectrumClass.Raman;
      case CDXSpectrumClass_Fluorescence:
        return CDSpectrumClass.Fluorescence;
      case CDXSpectrumClass_Atomic:
        return CDSpectrumClass.Atomic;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum class 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumClass.Unknown;
  }

  public static CDSpectrumXType readSpectrumXTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXSpectrumXType_Unknown:
        return CDSpectrumXType.Unknown;
      case CDXSpectrumXType_Wavenumbers:
        return CDSpectrumXType.Wavenumbers;
      case CDXSpectrumXType_Microns:
        return CDSpectrumXType.Microns;
      case CDXSpectrumXType_Hertz:
        return CDSpectrumXType.Hertz;
      case CDXSpectrumXType_MassUnits:
        return CDSpectrumXType.MassUnits;
      case CDXSpectrumXType_PartsPerMillion:
        return CDSpectrumXType.PartsPerMillion;
      case CDXSpectrumXType_Other:
        return CDSpectrumXType.Other;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum x type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumXType.Unknown;
  }

  public static CDSpectrumYType readSpectrumYTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt16();
    switch (value) {
      case CDXSpectrumYType_Unknown:
        return CDSpectrumYType.Unknown;
      case CDXSpectrumYType_Absorbance:
        return CDSpectrumYType.Absorbance;
      case CDXSpectrumYType_Transmittance:
        return CDSpectrumYType.Transmittance;
      case CDXSpectrumYType_PercentTransmittance:
        return CDSpectrumYType.PercentTransmittance;
      case CDXSpectrumYType_Other:
        return CDSpectrumYType.Other;
      case CDXSpectrumYType_ArbitraryUnits:
        return CDSpectrumYType.ArbitraryUnits;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Spectrum y type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDSpectrumYType.Unknown;
  }

  public static CDRingBondCount readRingBondCountProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsInt8();
    switch (value) {
      case CDXRingBondCount_Unspecified:
        return CDRingBondCount.Unspecified;
      case CDXRingBondCount_NoRingBonds:
        return CDRingBondCount.Unspecified;
      case CDXRingBondCount_AsDrawn:
        return CDRingBondCount.Unspecified;
      case CDXRingBondCount_SimpleRing:
        return CDRingBondCount.Unspecified;
      case CDXRingBondCount_Fusion:
        return CDRingBondCount.Unspecified;
      case CDXRingBondCount_SpiroOrHigher:
        return CDRingBondCount.Unspecified;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Ring bond count 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDRingBondCount.Unspecified;
  }

  public static float readLineHeight(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    if (value == 0) {
      return CDSettings.LineHeight_Variable;
    }
    if (value == 1) {
      return CDSettings.LineHeight_Automatic;
    }
    return value / 20f;
  }

  public static CDArrowHeadType readArrowheadTypeProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXArrowheadType_Solid:
        return CDArrowHeadType.Solid;
      case CDXArrowheadType_Hollow:
        return CDArrowHeadType.Hollow;
      case CDXArrowheadType_Angle:
        return CDArrowHeadType.Angle;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow head type 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowHeadType.Solid;
  }

  public static CDArrowHeadPositionType readArrowheadProperty(CDXProperty property)
      throws IOException {
    int value = property.getDataAsUInt16();
    switch (value) {
      case CDXArrowhead_Unspecified:
        return CDArrowHeadPositionType.Unspecified;
      case CDXArrowhead_None:
        return CDArrowHeadPositionType.None;
      case CDXArrowhead_Full:
        return CDArrowHeadPositionType.Full;
      case CDXArrowhead_HalfLeft:
        return CDArrowHeadPositionType.HalfLeft;
      case CDXArrowhead_HalfRight:
        return CDArrowHeadPositionType.HalfRight;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow head 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDArrowHeadPositionType.Unspecified;
  }

  public static CDNoGoType readNoGoProperty(CDXProperty property) throws IOException {
    int value = property.getDataAsUInt8();
    switch (value) {
      case CDXArrowNoGo_Unspecified:
        return CDNoGoType.Unspecified;
      case CDXArrowNoGo_None:
        return CDNoGoType.None;
      case CDXArrowNoGo_Cross:
        return CDNoGoType.Cross;
      case CDXArrowNoGo_Hash:
        return CDNoGoType.Hash;
      default:
        break;
    }
    handleUnrecognizedValue(
        "Arrow no go 0x"
            + Integer.toHexString(value)
            + " not recognized at "
            + getPositionAsString(property));
    return CDNoGoType.Unspecified;
  }

  public static String getPositionAsString(CDXObject object) {
    return object.getPosition() + "(0x" + Integer.toHexString(object.getPosition()) + ")";
  }

  public static String getPositionAsString(CDXProperty property) {
    return property.getPosition() + "(0x" + Integer.toHexString(property.getPosition()) + ")";
  }

  public static String dumpProperty(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for (byte b : data) {
      String hex = Integer.toHexString(b & 0xff);
      if (hex.length() == 1) {
        sb.append("0");
      }
      sb.append(hex);
      sb.append(" ");
    }
    return sb.toString();
  }

  private static void handleUnrecognizedValue(String message) throws IOException {
    if (CDXReader.RIGID) {
      throw new IOException(message);
    }
    LOGGER.warn(message);
  }

  public static boolean containsLineWrapBug(CDDocument doc) {
    // search for bug
    if (doc.getPages() == null || doc.getPages().isEmpty()) {
      return false;
    }
    boolean foundBug = false;
    for (CDPage page : doc.getPages()) {
      foundBug |= containsLineWrapBug(page);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDPage page) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(page.getTexts());
    for (CDGroup g : page.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDAltGroup ag : page.getNamedAlternativeGroups()) {
      foundBug |= containsLineWrapBug(ag);
    }
    for (CDFragment f : page.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDGroup group) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(group.getCaptions());
    for (CDGroup g : group.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDAltGroup ag : group.getNamedAlternativeGroups()) {
      foundBug |= containsLineWrapBug(ag);
    }
    for (CDFragment f : group.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDAltGroup altgroup) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(altgroup.getCaptions());
    for (CDGroup g : altgroup.getGroups()) {
      foundBug |= containsLineWrapBug(g);
    }
    for (CDFragment f : altgroup.getFragments()) {
      foundBug |= containsLineWrapBug(f);
    }
    return foundBug;
  }

  static boolean containsLineWrapBug(CDFragment fragment) {
    boolean foundBug = false;
    foundBug |= containsLineWrapBug(fragment.getTexts());
    return foundBug;
  }

  static boolean containsLineWrapBug(List<CDText> texts) {
    for (CDText text : texts) {
      List<Integer> definedLineStarts = text.getLineStarts();
      CDStyledString css = text.getText();
      if (css == null) {
        continue;
      }
      String t = css.getText();
      if (t == null) {
        continue;
      }

      if (containsLineWrapBugCharacter(t)) {
        List<Integer> calculatedLineStarts = getCalculatedLineStarts(t, (char) 13);
        for (int i : calculatedLineStarts) {
          if (!definedLineStarts.contains(i)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static void fixLineWrapBug(CDDocument doc) {
    // search for bug
    if (doc.getPages() == null || doc.getPages().isEmpty()) {
      return;
    }
    for (CDPage page : doc.getPages()) {
      fixLineWrapBug(page);
    }
  }

  static void fixLineWrapBug(CDPage page) {
    fixLineWrapBug(page.getTexts());
    for (CDGroup group : page.getGroups()) {
      fixLineWrapBug(group);
    }
    for (CDAltGroup altGroup : page.getNamedAlternativeGroups()) {
      fixLineWrapBug(altGroup);
    }
    for (CDFragment fragment : page.getFragments()) {
      fixLineWrapBug(fragment);
    }
  }

  static void fixLineWrapBug(CDGroup group) {
    fixLineWrapBug(group.getCaptions());
    for (CDGroup g : group.getGroups()) {
      fixLineWrapBug(g);
    }
    for (CDAltGroup ag : group.getNamedAlternativeGroups()) {
      fixLineWrapBug(ag);
    }
    for (CDFragment f : group.getFragments()) {
      fixLineWrapBug(f);
    }
  }

  static void fixLineWrapBug(CDAltGroup altgroup) {
    fixLineWrapBug(altgroup.getCaptions());
    for (CDGroup g : altgroup.getGroups()) {
      fixLineWrapBug(g);
    }
    for (CDFragment f : altgroup.getFragments()) {
      fixLineWrapBug(f);
    }
  }

  static void fixLineWrapBug(CDFragment fragment) {
    fixLineWrapBug(fragment.getTexts());
  }

  static void fixLineWrapBug(List<CDText> texts) {
    for (CDText text : texts) {
      List<Integer> definedLineStarts = text.getLineStarts();
      CDStyledString css = text.getText();
      if (css == null) {
        continue;
      }
      String t = css.getText();
      if (t == null) {
        continue;
      }

      //      for (int i=0; i < t.length(); i++) {
      //        System.out.println(i + ": " + t.charAt(i) + " (" + (int)t.charAt(i) + "/" +
      // Integer.toHexString((int)t.charAt(i)) + ")");
      //      }

      if (containsLineWrapBugCharacter(t)) {
        t += (char) 13;
        List<Integer> calculatedLineStarts = getCalculatedLineStarts(t, (char) 13);
        boolean erroneous = false;
        for (int i : calculatedLineStarts) {
          if (!definedLineStarts.contains(i)) {
            erroneous = true;
          }
        }
        if (erroneous) {
          text.setLineStarts(calculatedLineStarts);
        }
      }
    }
  }

  public static List<Integer> getCalculatedLineStarts(String s, char c) {
    List<Integer> result = new ArrayList<Integer>();
    while (s.lastIndexOf(c) > -1) {
      int i = s.lastIndexOf(c);
      s = s.substring(0, i);
      result.add(0, i + 1);
    }
    return result;
  }

  public static boolean containsLineWrapBugCharacter(String t) {
    return t.contains("\u00B0")
        || // degree sign
        t.contains("\u00B7")
        || // middle dot
        t.contains("\u00D7")
        || // multiplication sign
        t.contains("\u00AE")
        || // rightwards arrow (actually registered)
        t.contains("\u00B3")
        || // less-than or equal to (actually superscript three)
        t.contains("\u00A3")
        || // greater-than or equal to (actually pound sign)
        t.contains("\u00A9")
        || // copyright
        t.contains("\u00D4")
        || // trademark (actually latin capital letter O with circumflex
        t.contains("\u00B9")
        || // not equals (actually superscript one)
        t.contains("\u00BB")
        || // almost equal to (actually right-pointing double angle quotation mark)
        t.contains("\u00C5"); // latin capital letter A with ring above
  }
}
