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

/** Constants defined by the CDX file format Specification. */
public class CDXConstants {
  public static final int CDXUndefinedId = -1;

  private static final byte[] CDX_Signature = new byte[] {'V', 'j', 'C', 'D', '0', '1', '0', '0'};

  public static final int CDX_HeaderLength = 28;

  private static final byte[] CHEMDRAW_INTERCHANGE_FORMAT = "CDIF".getBytes();

  public static final int CDXTag_Object = 0x8000;

  public static final int CDXTag_UserDefined = 0x4000;

  // enum CDXDatumID

  // General properties.

  /** Marks end of object. */
  public static final int CDXProp_EndObject = 0x0000;

  /** The name of the creator (program user's name) of the document. (CDXString) */
  public static final int CDXProp_CreationUserName = 0x0001;

  /** The time of object creation. (CDXDate) */
  public static final int CDXProp_CreationDate = 0x0002;

  /**
   * The name of the program, including version and platform, that created the associated CDX
   * object. ChemDraw 4.0 uses ChemDraw 4.0 as the value of CreationProgram. (CDXString)
   */
  public static final int CDXProp_CreationProgram = 0x0003;

  /** The name of the last modifier (program user's name) of the document. (CDXString) */
  public static final int CDXProp_ModificationUserName = 0x0004;

  /** Time of the last modification. (CDXDate) */
  public static final int CDXProp_ModificationDate = 0x0005;

  /**
   * The name of the program, including version and platform, of the last program to perform a
   * modification. ChemDraw 4.0 uses ChemDraw 4.0 as the value of CreationProgram. (CDXString)
   */
  public static final int CDXProp_ModificationProgram = 0x0006;

  /** Table of contents. (obsolete) */
  public static final int CDXProp_Unused1 = 0x0007;

  /** Name of an object. (CDXString) */
  public static final int CDXProp_Name = 0x0008;

  /** An arbitrary string intended to be meaningful to a user. (CDXString) */
  public static final int CDXProp_Comment = 0x0009;

  /** Back-to-front ordering index in 2D drawing. (INT16) */
  public static final int CDXProp_ZOrder = 0x000A;

  /** A registry or catalog number of a molecule object. (CDXString) */
  public static final int CDXProp_RegistryNumber = 0x000B;

  /**
   * A string that specifies the authority which issued a registry or catalog number. Some examples
   * of registry authorities are CAS, Beilstein, Aldrich, and Merck. (CDXString)
   */
  public static final int CDXProp_RegistryAuthority = 0x000C;

  /**
   * Indicates that this object (the reference object) is an alias to an object elsewhere in the
   * document (the target object). The attributes and contained objects should be taken from the
   * target object. (obsolete)
   */
  public static final int CDXProp_Unused2 = 0x000D;

  /**
   * Indicates that this object represents some property in some other object.
   * (CDXRepresentsProperty)
   */
  public static final int CDXProp_RepresentsProperty = 0x000E;

  /**
   * Signifies whether chemical warnings should be suppressed on this object. (CDXBooleanImplied)
   */
  public static final int CDXProp_IgnoreWarnings = 0x000F;

  /** A warning concerning possible chemical problems with this object. (CDXString) */
  public static final int CDXProp_ChemicalWarning = 0x0010;

  /** The object is visible if non-zero. (CDXBoolean) */
  public static final int CDXProp_Visible = 0x0011;

  /** The ID of the object that should be read instead of this one. */
  public static final int CDXProp_SupersededBy = 0x0013;

  // Fonts.

  /** A list of fonts used in the document. (CDXFontTable) */
  public static final int CDXProp_FontTable = 0x0100;

  // Coordinates.

  /**
   * The 2D location (in the order of vertical and horizontal locations) of an object. (CDXPoint2D)
   */
  public static final int CDXProp_2DPosition = 0x0200;

  /**
   * The 3D location (in the order of X-, Y-, and Z-locations in right-handed coordinate system) of
   * an object in CDX coordinate units. The precise meaning of this attribute varies depending on
   * the type of object. (CDXPoint3D)
   */
  public static final int CDXProp_3DPosition = 0x0201;

  /**
   * The width and height of an object in CDX coordinate units. The precise meaning of this
   * attribute varies depending on the type of object. (CDXPoint2D)
   */
  public static final int CDXProp_2DExtent = 0x0202;

  /**
   * The width, height, and depth of an object in CDX coordinate units (right-handed coordinate
   * system). The precise meaning of this attribute varies depending on the type of object.
   * (CDXPoint3D)
   */
  public static final int CDXProp_3DExtent = 0x0203;

  /**
   * The smallest rectangle that encloses the graphical representation of the object. (CDXRectangle)
   */
  public static final int CDXProp_BoundingBox = 0x0204;

  /** The angular orientation of an object in degrees * 65536. (INT32) */
  public static final int CDXProp_RotationAngle = 0x0205;

  /**
   * The bounds of this object in the coordinate system of its parent (used for pages within
   * tables). (CDXRectangle)
   */
  public static final int CDXProp_BoundsInParent = 0x0206;

  /**
   * The 3D location (in the order of X-, Y- and Z-locations in right-handed coordinate system) of
   * the head of an object in CDX coordinate units. The precise meaning of this attribute varies
   * depending on the type of object. (CDXPoint3D)
   */
  public static final int CDXProp_3DHead = 0x0207;

  /**
   * The 3D location (in the order of X-, Y- and Z-locations in right-handed coordinate system) of
   * the tail of an object in CDX coordinate units. The precise meaning of this attribute varies
   * depending on the type of object. (CDXPoint3D)
   */
  public static final int CDXProp_3DTail = 0x0208;

  /**
   * The location of the top-left corner of a quadrilateral object, possibly in a rotated or skewed
   * frame. (CDXPoint2D)
   */
  public static final int CDXProp_TopLeft = 0x0209;

  /**
   * The location of the top-right corner of a quadrilateral object, possibly in a rotated or skewed
   * frame. (CDXPoint2D)
   */
  public static final int CDXProp_TopRight = 0x020A;

  /**
   * The location of the bottom-right corner of a quadrilateral object, possibly in a rotated or
   * skewed frame. (CDXPoint2D)
   */
  public static final int CDXProp_BottomRight = 0x020B;

  /**
   * The location of the bottom-left corner of a quadrilateral object, possibly in a rotated or
   * skewed frame. (CDXPoint2D)
   */
  public static final int CDXProp_BottomLeft = 0x020C;

  /**
   * The 3D location (in the order of X-, Y- and Z-locations in right-handed coordinate system) of
   * the center of an object in CDX coordinate units. The precise meaning of this attribute varies
   * depending on the type of object. (CDXPoint3D)
   */
  public static final int CDXProp_3DCenter = 0x020D;

  /** The 3D location of the end of the major axis of an object in CDX coordinate units. */
  public static final int CDXProp_MajorAxisEnd3D = 0x20E;

  /** The 3D location of the end of the minor axis of an object in CDX coordinate units. */
  public static final int CDXProp_MinorAxisEnd3D = 0x20F;

  // Colors.

  /** The color palette used throughout the document. (CDXColorTable) */
  public static final int CDXProp_ColorTable = 0x0300;

  /**
   * The foreground color of an object represented as the two-based index into the object's color
   * table. (UINT16)
   */
  public static final int CDXProp_ForegroundColor = 0x0301;

  /**
   * The background color of an object represented as the two-based index into the object's color
   * table. (INT16)
   */
  public static final int CDXProp_BackgroundColor = 0x0302;

  public static final int CDXProp_FadePercent = 0x0303;

  /**
   * The highlight color of an object represented as the two-based index into the object's color
   * table. (INT16)
   */
  public static final int CDXProp_HighlightColor = 0x0308;

  // Atom properties.

  /** The type of a node object. (INT16) */
  public static final int CDXProp_Node_Type = 0x0400;

  /** The characteristics of node label display. (INT8) */
  public static final int CDXProp_Node_LabelDisplay = 0x0401;

  /** The atomic number of the atom representing this node. (INT16) */
  public static final int CDXProp_Node_Element = 0x0402;

  /** A list of atomic numbers. (CDXElementList) */
  public static final int CDXProp_Atom_ElementList = 0x0403;

  /**
   * The composition of a node representing a fragment whose composition is known, but whose
   * connectivity is not. For example, C<sub>4</sub>H<sub>9</sub> represents a mixture of the 4
   * butyl isomers. (CDXFormula)
   */
  public static final int CDXProp_Atom_Formula = 0x0404;

  /** The absolute isotopic mass of an atom (2 for deuterium, 14 for carbon-14). (INT16) */
  public static final int CDXProp_Atom_Isotope = 0x0420;

  /** The atomic charge of an atom. (INT8) */
  public static final int CDXProp_Atom_Charge = 0x0421;

  /** The atomic radical attribute of an atom. (UINT8) */
  public static final int CDXProp_Atom_Radical = 0x0422;

  /**
   * Indicates that up to the specified number of additional substituents are permitted on this
   * atom. (UINT8)
   */
  public static final int CDXProp_Atom_RestrictFreeSites = 0x0423;

  /** Signifies that implicit hydrogens are not allowed on this atom. (CDXBooleanImplied) */
  public static final int CDXProp_Atom_RestrictImplicitHydrogens = 0x0424;

  /** The number of ring bonds attached to an atom. (INT8) */
  public static final int CDXProp_Atom_RestrictRingBondCount = 0x0425;

  /** Indicates whether unsaturation should be present or absent. (INT8) */
  public static final int CDXProp_Atom_RestrictUnsaturatedBonds = 0x0426;

  /**
   * If present, signifies that the reaction change of an atom must be as specified.
   * (CDXBooleanImplied)
   */
  public static final int CDXProp_Atom_RestrictRxnChange = 0x0427;

  /** The change of stereochemistry of an atom during a reaction. (INT8) */
  public static final int CDXProp_Atom_RestrictRxnStereo = 0x0428;

  /** Signifies that an abnormal valence for an atom is permitted. (CDXBooleanImplied) */
  public static final int CDXProp_Atom_AbnormalValence = 0x0429;

  public static final int CDXProp_Unused3 = 0x042A;

  /**
   * The number of (explicit) hydrogens in a labeled atom consisting of one heavy atom and
   * (optionally) the symbol H (e.g., CH<sub>3</sub>). (UINT16)
   */
  public static final int CDXProp_Atom_NumHydrogens = 0x042B;

  public static final int CDXProp_Unused4 = 0x042C;

  public static final int CDXProp_Unused5 = 0x042D;

  /**
   * Signifies the presence of an implicit hydrogen with stereochemistry specified equivalent to an
   * explicit H atom with a wedged bond. (CDXBooleanImplied)
   */
  public static final int CDXProp_Atom_HDot = 0x042E;

  /**
   * Signifies the presence of an implicit hydrogen with stereochemistry specified equivalent to an
   * explicit H atom with a hashed bond. (CDXBooleanImplied)
   */
  public static final int CDXProp_Atom_HDash = 0x042F;

  /** The geometry of the bonds about this atom. (INT8) */
  public static final int CDXProp_Atom_Geometry = 0x0430;

  /**
   * An ordering of the bonds to this node, used for stereocenters, fragments, and named alternative
   * groups with more than one attachment. (CDXObjectIDArray)
   */
  public static final int CDXProp_Atom_BondOrdering = 0x0431;

  /**
   * For multicenter attachment nodes or variable attachment nodes, a list of IDs of the nodes which
   * are multiply or variably attached to this node. (CDXObjectIDArrayWithCounts)
   */
  public static final int CDXProp_Node_Attachments = 0x0432;

  /** The name of the generic nickname. (CDXString) */
  public static final int CDXProp_Atom_GenericNickname = 0x0433;

  /** The ID of the alternative group object that describes this node. (CDXObjectID) */
  public static final int CDXProp_Atom_AltGroupID = 0x0434;

  /** Indicates that substitution is restricted to no more than the specified value. (UINT8) */
  public static final int CDXProp_Atom_RestrictSubstituentsUpTo = 0x0435;

  /** Indicates that exactly the specified number of substituents must be present. (UINT8) */
  public static final int CDXProp_Atom_RestrictSubstituentsExactly = 0x0436;

  /** The node's absolute stereochemistry according to the Cahn-Ingold-Prelog system. (INT8) */
  public static final int CDXProp_Atom_CIPStereochemistry = 0x0437;

  /**
   * Provides for restrictions on whether a given node may match other more- or less-general nodes.
   * (INT8)
   */
  public static final int CDXProp_Atom_Translation = 0x0438;

  /** Atom number, as text. (CDXString) */
  public static final int CDXProp_Atom_AtomNumber = 0x0439;

  /** Show the query indicator if non-zero. (CDXBoolean) */
  public static final int CDXProp_Atom_ShowQuery = 0x043A;

  /** Show the stereochemistry indicator if non-zero. (CDXBoolean) */
  public static final int CDXProp_Atom_ShowStereo = 0x043B;

  /** Show the atom number if non-zero. (CDXBoolean) */
  public static final int CDXProp_Atom_ShowAtomNumber = 0x043C;

  /** Low end of repeat count for link nodes. (INT16) */
  public static final int CDXProp_Atom_LinkCountLow = 0x043D;

  /** High end of repeat count for link nodes. (INT16) */
  public static final int CDXProp_Atom_LinkCountHigh = 0x043E;

  /** Isotopic abundance of this atom's isotope. (INT8) */
  public static final int CDXProp_Atom_IsotopicAbundance = 0x043F;

  /** Type of external connection, for atoms of type CDXNodeType_ExternalConnectionPoint. (INT8) */
  public static final int CDXProp_Atom_ExternalConnectionType = 0x0440;

  /** A list of generic nicknames. */
  public static final int CDXProp_Atom_GenericList = 0x0441;

  /**
   * Signifies whether terminal carbons (carbons with zero or one bond) should display a text label
   * with the element symbol and appropriate hydrogens.
   */
  public static final int CDXProp_ShowTerminalCarbonLabels = 0x0442;

  /**
   * Signifies whether non-terminal carbons (carbons with more than one bond) should display a text
   * label with the element symbol and appropriate hydrogens.
   */
  public static final int CDXProp_ShowNonTerminalCarbonLabels = 0x0443;

  /**
   * Signifies whether implicit hydrogens should be displayed on otherwise-atomic atom labels (NH2
   * versus N).
   */
  public static final int CDXProp_HideImplicitHydrogens = 0x0444;

  /** Show the enhanced stereochemistry indicator if non-zero. */
  public static final int CDXProp_Atom_ShowEnhancedStereo = 0x0445;

  /** The type of enhanced stereochemistry present on this atom. This is an enumerated property. */
  public static final int CDXProp_Atom_EnhancedStereoType = 0x0446;

  /** The group number associated with Or and And enhanced stereochemistry types. */
  public static final int CDXProp_Atom_EnhancedStereoGroupNum = 0x0447;

  // Molecule properties.

  /** Indicates that the molecule is a racemic mixture. (CDXBoolean) */
  public static final int CDXProp_Mole_Racemic = 0x0500;

  /** Indicates that the molecule has known absolute configuration. (CDXBoolean) */
  public static final int CDXProp_Mole_Absolute = 0x0501;

  /**
   * Indicates that the molecule has known relative stereochemistry, but unknown absolute
   * configuration. (CDXBoolean)
   */
  public static final int CDXProp_Mole_Relative = 0x0502;

  /** The molecular formula representation of a molecule object. (CDXFormula) */
  public static final int CDXProp_Mole_Formula = 0x0503;

  /** The average molecular weight of a molecule object. (FLOAT64) */
  public static final int CDXProp_Mole_Weight = 0x0504;

  /** An ordered list of attachment points within a fragment. (CDXObjectIDArray) */
  public static final int CDXProp_Frag_ConnectionOrder = 0x0505;

  // Bond properties.

  /** The order of a bond object. (INT16) */
  public static final int CDXProp_Bond_Order = 0x0600;

  /** The display type of a bond object. (INT16) */
  public static final int CDXProp_Bond_Display = 0x0601;

  /** The display type for the second line of a double bond. (INT16) */
  public static final int CDXProp_Bond_Display2 = 0x0602;

  /** The position of the second line of a double bond. (INT16) */
  public static final int CDXProp_Bond_DoublePosition = 0x0603;

  /** The ID of the CDX node object at the first end of a bond. (CDXObjectID) */
  public static final int CDXProp_Bond_Begin = 0x0604;

  /** The ID of the CDX node object at the second end of a bond. (CDXObjectID) */
  public static final int CDXProp_Bond_End = 0x0605;

  /** Indicates the desired topology of a bond in a query. (INT8) */
  public static final int CDXProp_Bond_RestrictTopology = 0x0606;

  /** Specifies that a bond is affected by a reaction. (INT8) */
  public static final int CDXProp_Bond_RestrictRxnParticipation = 0x0607;

  /** Indicates where within the Bond_Begin node a bond is attached. (UINT8) */
  public static final int CDXProp_Bond_BeginAttach = 0x0608;

  /** Indicates where within the Bond_End node a bond is attached. (UINT8) */
  public static final int CDXProp_Bond_EndAttach = 0x0609;

  /** The bond's absolute stereochemistry according to the Cahn-Ingold-Prelog system. (INT8) */
  public static final int CDXProp_Bond_CIPStereochemistry = 0x060A;

  /** Ordered list of attached bond IDs. (CDXObjectIDArray) */
  public static final int CDXProp_Bond_BondOrdering = 0x060B;

  /** Show the query indicator if non-zero. (CDXBoolean) */
  public static final int CDXProp_Bond_ShowQuery = 0x060C;

  /** Show the stereochemistry indicator if non-zero. (CDXBoolean) */
  public static final int CDXProp_Bond_ShowStereo = 0x060D;

  /**
   * Unordered list of IDs of bonds that cross this one (either above or below). (CDXObjectIDArray)
   */
  public static final int CDXProp_Bond_CrossingBonds = 0x060E;

  /** Show the reaction-change indicator if non-zero. (CDXBoolean) */
  public static final int CDXProp_Bond_ShowRxn = 0x060F;

  // Text properties.

  /** The text of a text object. (CDXString) */
  public static final int CDXProp_Text = 0x0700;

  /** The horizontal justification of a text object. (INT8) */
  public static final int CDXProp_Justification = 0x0701;

  /** The line height of a text object. (UINT16) */
  public static final int CDXProp_LineHeight = 0x0702;

  /** The word-wrap width of a text object. (INT16) */
  public static final int CDXProp_WordWrapWidth = 0x0703;

  /**
   * The number of lines of a text object followed by that many values indicating the zero-based
   * text position of each line start. (INT16ListWithCounts)
   */
  public static final int CDXProp_LineStarts = 0x0704;

  /** The alignment of the text with respect to the node position. (INT8) */
  public static final int CDXProp_LabelAlignment = 0x0705;

  /** Text line height for atom labels (INT16) */
  public static final int CDXProp_LabelLineHeight = 0x0706;

  /** Text line height for non-atomlabel text objects (INT16) */
  public static final int CDXProp_CaptionLineHeight = 0x0707;

  /**
   * Signifies whether to the text label should be interpreted chemically (if possible).
   * (CDXBooleanImplied)
   */
  public static final int CDXProp_InterpretChemically = 0x0708;

  // Document properties.

  /**
   * The 120 byte Macintosh TPrint data associated with the CDX document object. Refer to Macintosh
   * Toolbox manual for detailed description. (Unformatted)
   */
  public static final int CDXProp_MacPrintInfo = 0x0800;

  /** The Windows DEVMODE structure associated with the CDX document object. (Unformatted) */
  public static final int CDXProp_WinPrintInfo = 0x0801;

  /** The outer margins of the Document. (CDXRectangle) */
  public static final int CDXProp_PrintMargins = 0x0802;

  /** The default chain angle setting in degrees * 65536. (INT32) */
  public static final int CDXProp_ChainAngle = 0x0803;

  /** The spacing between segments of a multiple bond, measured relative to bond length. (INT16) */
  public static final int CDXProp_BondSpacing = 0x0804;

  /** The default bond length. (CDXCoordinate) */
  public static final int CDXProp_BondLength = 0x0805;

  /** The default bold bond width. (CDXCoordinate) */
  public static final int CDXProp_BoldWidth = 0x0806;

  /** The default line width. (CDXCoordinate) */
  public static final int CDXProp_LineWidth = 0x0807;

  /** The default amount of space surrounding atom labels. (CDXCoordinate) */
  public static final int CDXProp_MarginWidth = 0x0808;

  /** The default spacing between hashed lines used in wedged hashed bonds. (CDXCoordinate) */
  public static final int CDXProp_HashSpacing = 0x0809;

  /** The default style for atom labels. (CDXFontStyle) */
  public static final int CDXProp_LabelStyle = 0x080A;

  /** The default style for non-atomlabel text objects. (CDXFontStyle) */
  public static final int CDXProp_CaptionStyle = 0x080B;

  /** The horizontal justification of a caption (non-atomlabel text object) (INT8) */
  public static final int CDXProp_CaptionJustification = 0x080C;

  /**
   * Signifies whether to use fractional width information when drawing text. (CDXBooleanImplied)
   */
  public static final int CDXProp_FractionalWidths = 0x080D;

  /** The view magnification factor (INT16) */
  public static final int CDXProp_Magnification = 0x080E;

  /** The width of the document in pages. (INT16) */
  public static final int CDXProp_WidthPages = 0x080F;

  /** The height of the document in pages. (INT16) */
  public static final int CDXProp_HeightPages = 0x0810;

  /** The type of drawing space used for this document. (INT8) */
  public static final int CDXProp_DrawingSpaceType = 0x0811;

  /**
   * The width of an object in CDX coordinate units, possibly in a rotated or skewed frame.
   * (CDXCoordinate)
   */
  public static final int CDXProp_Width = 0x0812;

  /**
   * The height of an object in CDX coordinate units, possibly in a rotated or skewed frame.
   * (CDXCoordinate)
   */
  public static final int CDXProp_Height = 0x0813;

  /** The amount of overlap of pages when a poster is tiled. (CDXCoordinate) */
  public static final int CDXProp_PageOverlap = 0x0814;

  /** The text of the header. (CDXString) */
  public static final int CDXProp_Header = 0x0815;

  /** The vertical offset of the header baseline from the top of the page. (CDXCoordinate) */
  public static final int CDXProp_HeaderPosition = 0x0816;

  /** The text of the footer. (CDXString) */
  public static final int CDXProp_Footer = 0x0817;

  /** The vertical offset of the footer baseline from the bottom of the page. (CDXCoordinate) */
  public static final int CDXProp_FooterPosition = 0x0818;

  /** If present, trim marks are to printed in the margins. (CDXBooleanImplied) */
  public static final int CDXProp_PrintTrimMarks = 0x0819;

  /** The default font family for atom labels. (INT16) */
  public static final int CDXProp_LabelStyleFont = 0x081A;

  /** The default font style for captions (non-atom-label text objects). (INT16) */
  public static final int CDXProp_CaptionStyleFont = 0x081B;

  /** The default font size for atom labels. (INT16) */
  public static final int CDXProp_LabelStyleSize = 0x081C;

  /** The default font size for captions (non-atom-label text objects). (INT16) */
  public static final int CDXProp_CaptionStyleSize = 0x081D;

  /** The default font style for atom labels. (INT16) */
  public static final int CDXProp_LabelStyleFace = 0x081E;

  /** The default font face for captions (non-atom-label text objects). (INT16) */
  public static final int CDXProp_CaptionStyleFace = 0x081F;

  /** The default color for atom labels (INT16) */
  public static final int CDXProp_LabelStyleColor = 0x0820;

  /** The default color for captions (non-atom-label text objects). (INT16) */
  public static final int CDXProp_CaptionStyleColor = 0x0821;

  /** The absolute distance between segments of a multiple bond. (CDXCoordinate) */
  public static final int CDXProp_BondSpacingAbs = 0x0822;

  /** The default justification for atom labels. (INT8) */
  public static final int CDXProp_LabelJustification = 0x0823;

  /** Defines a size for OLE In-Place editing. (CDXPoint2D) */
  public static final int CDXProp_FixInplaceExtent = 0x0824;

  /** A specific side of an object (rectangle). (INT16) */
  public static final int CDXProp_Side = 0x0825;

  /** Defines a padding for OLE In-Place editing. (CDXPoint2D) */
  public static final int CDXProp_FixInplaceGap = 0x0826;

  /** Transient data used by the CambridgeSoft Oracle Cartridge. */
  public static final int CDXProp_CartridgeData = 0x0827;

  // Window properties.

  /** Signifies whether the main viewing window is zoomed (maximized). (CDXBooleanImplied) */
  public static final int CDXProp_Window_IsZoomed = 0x0900;

  /** The top-left position of the main viewing window. (CDXPoint2D) */
  public static final int CDXProp_Window_Position = 0x0901;

  /** Height and width of the document window. (CDXPoint2D) */
  public static final int CDXProp_Window_Size = 0x0902;

  // Graphic object properties.

  /** The type of graphical object. (INT16) */
  public static final int CDXProp_Graphic_Type = 0x0A00;

  /** The type of a line object. (INT16) */
  public static final int CDXProp_Line_Type = 0x0A01;

  /** The type of arrow object, which represents line, arrow, arc, rectangle, or orbital. (INT16) */
  public static final int CDXProp_Arrow_Type = 0x0A02;

  /** The type of a rectangle object. (INT16) */
  public static final int CDXProp_Rectangle_Type = 0x0A03;

  /** The type of an arrow object that represents a circle or ellipse. (INT16) */
  public static final int CDXProp_Oval_Type = 0x0A04;

  /** The type of orbital object. (INT16) */
  public static final int CDXProp_Orbital_Type = 0x0A05;

  /** The type of symbol object. (INT16) */
  public static final int CDXProp_Bracket_Type = 0x0A06;

  /** The type of symbol object. (INT16) */
  public static final int CDXProp_Symbol_Type = 0x0A07;

  /** The type of curve object. (INT16) */
  public static final int CDXProp_Curve_Type = 0x0A08;

  /** The size of the arrow's head. (INT16) */
  public static final int CDXProp_Arrow_HeadSize = 0x0A20;

  /** The size of an arc (in degrees * 10, so 90 degrees = 900). (INT16) */
  public static final int CDXProp_Arc_AngularSize = 0x0A21;

  /** The size of a bracket. (INT16) */
  public static final int CDXProp_Bracket_LipSize = 0x0A22;

  /** The B&eacute;zier curve's control point locations. (CDXCurvePoints) */
  public static final int CDXProp_Curve_Points = 0x0A23;

  /** The syntactical chemical meaning of the bracket (SRU, mer, mon, xlink, etc). (INT8) */
  public static final int CDXProp_Bracket_Usage = 0x0A24;

  /** The head-to-tail connectivity of objects contained within the bracket. (INT8) */
  public static final int CDXProp_Polymer_RepeatPattern = 0x0A25;

  /** The flip state of objects contained within the bracket. (INT8) */
  public static final int CDXProp_Polymer_FlipType = 0x0A26;

  /** The set of objects contained in a BracketedGroup. (CDXObjectIDArray) */
  public static final int CDXProp_BracketedObjects = 0x0A27;

  /** The number of times a multiple-group BracketedGroup is repeated. (INT16) */
  public static final int CDXProp_Bracket_RepeatCount = 0x0A28;

  /** The component order associated with a BracketedGroup. (INT16) */
  public static final int CDXProp_Bracket_ComponentOrder = 0x0A29;

  /** The label associated with a BracketedGroup that represents an SRU. (CDXString) */
  public static final int CDXProp_Bracket_SRULabel = 0x0A2A;

  /**
   * The ID of a graphical object (bracket, brace, or parenthesis) associated with a Bracket
   * Attachment. (CDXObjectID)
   */
  public static final int CDXProp_Bracket_GraphicID = 0x0A2B;

  /** The ID of a bond that crosses a Bracket Attachment. (CDXObjectID) */
  public static final int CDXProp_Bracket_BondID = 0x0A2C;

  /**
   * The ID of the node located within the Bracketed Group and attached to a bond that crosses a
   * Bracket Attachment. (CDXObjectID)
   */
  public static final int CDXProp_Bracket_InnerAtomID = 0x0A2D;

  /** The B&eacute;zier curve's control point locations. (CDXCurvePoints3D) */
  public static final int CDXProp_Curve_Points3D = 0x0A2E;

  /** Type of curve's arrow head */
  public static final int CDXProp_Curve_ArrowheadType = 0x0A2F;

  /** Center size of arrow head. */
  public static final int CDXProp_Curve_ArrowheadCenterSize = 0x0A30;

  /** Center size of arrow head. */
  public static final int CDXProp_Curve_ArrowheadWidth = 0x0A31;

  /** The size of the object's shadow. */
  public static final int CDXProp_ShadowSize = 0x0A32;

  /**
   * The width of the space between a multiple-component arrow shaft, as in an equilibrium arrow.
   */
  public static final int CDXProp_Arrow_ShaftSpacing = 0x0A33;

  /**
   * The ratio of the length of the left component of an equilibrium arrow (viewed from the end to
   * the start) to the right component.
   */
  public static final int CDXProp_Arrow_EquilibriumRatio = 0x0A34;

  /** Arrow head of curve's head */
  public static final int CDXProp_Curve_ArrowheadHead = 0xA35;

  /** Arrow head of curve's tail */
  public static final int CDXProp_Curve_ArrowheadTail = 0xA36;

  /** The type of the fill, for objects that can be filled. This is an enumerated property. */
  public static final int CDXProp_Curve_FillType = 0xA37;

  /** The width of the space between a a Doubled curve. */
  public static final int CDXProp_Curve_Spacing = 0x0A38;

  /** Type of curve's arrow head */
  public static final int CDXProp_Curve_Closed = 0x0A39;

  /** Signifies whether the arrow is a dipole arrow. */
  public static final int CDXProp_Arrow_Dipole = 0x0A3A;

  /**
   * Signifies whether arrow is a no-go arrow, and the type of no-go (crossed-through or hashed-out)
   * if so. This is an enumerated property.
   */
  public static final int CDXProp_Arrow_NoGo = 0x0A3B;

  /** The radius of the rounded corner of a rounded rectangle. */
  public static final int CDXProp_CornerRadius = 0x0A3C;

  /** The type of frame on an object. This is an enumerated property. */
  public static final int CDXProp_Frame_Type = 0x0A3D;

  // Embedded pictures.

  /**
   * The section information (SectionHandle) of the Macintosh Publish and Subscribe edition embedded
   * in the CDX picture object. (Unformatted)
   */
  public static final int CDXProp_Picture_Edition = 0x0A60;

  /**
   * The alias information of the Macintosh Publish and Subscribe edition embedded in the CDX
   * picture object. (Unformatted)
   */
  public static final int CDXProp_Picture_EditionAlias = 0x0A61;

  /** A Macintosh PICT data object. (Unformatted) */
  public static final int CDXProp_MacPICT = 0x0A62;

  /** A Microsoft Windows Metafile object. (Unformatted) */
  public static final int CDXProp_WindowsMetafile = 0x0A63;

  /** An OLE object. (Unformatted) */
  public static final int CDXProp_OLEObject = 0x0A64;

  /** A Microsoft Windows Enhanced Metafile object. (Unformatted) */
  public static final int CDXProp_EnhancedMetafile = 0x0A65;

  /** A compressed Microsoft Windows Metafile object. */
  public static final int CDXProp_CompressedWindowsMetafile = 0x0A67;

  /** An compressed OLE object. */
  public static final int CDXProp_CompressedOLEObject = 0x0A68;

  /** A compressed Microsoft Windows Enhanced Metafile object. */
  public static final int CDXProp_CompressedEnhancedMetafile = 0x0A69;

  /** Size of the uncompressed Microsoft Windows Metafile object. */
  public static final int CDXProp_UncompressedWindowsMetafileSize = 0x0A6B;

  /** Size of the uncompressed OLE object. */
  public static final int CDXProp_UncompressedOLEObjectSize = 0x0A6C;

  /** Size of the uncompressed Microsoft Windows Enhanced Metafile object. */
  public static final int CDXProp_UncompressedEnhancedMetafileSize = 0x0A6D;

  /** A binary GIF data object. */
  public static final int CDXProp_GIF = 0x0A6E;

  /** A binary TIFF data object. */
  public static final int CDXProp_TIFF = 0x0A6F;

  /** A binary PNG data object. */
  public static final int CDXProp_PNG = 0x0A70;

  /** A binary JPEG data object. */
  public static final int CDXProp_JPEG = 0x0A71;

  /** A binary BMP data object. */
  public static final int CDXProp_BMP = 0x0A72;

  // Spectrum properties

  /**
   * The spacing in logical units (ppm, Hz, wavenumbers) between points along the X-axis of an
   * evenly-spaced grid. (FLOAT64)
   */
  public static final int CDXProp_Spectrum_XSpacing = 0x0A80;

  /** The first data point for the X-axis of an evenly-spaced grid. (FLOAT64) */
  public static final int CDXProp_Spectrum_XLow = 0x0A81;

  /** The type of units the X-axis represents. (INT16) */
  public static final int CDXProp_Spectrum_XType = 0x0A82;

  /** The type of units the Y-axis represents. (INT16) */
  public static final int CDXProp_Spectrum_YType = 0x0A83;

  /** A label for the X-axis. (CDXString) */
  public static final int CDXProp_Spectrum_XAxisLabel = 0x0A84;

  /** A label for the Y-axis. (CDXString) */
  public static final int CDXProp_Spectrum_YAxisLabel = 0x0A85;

  /**
   * The Y-axis values for the spectrum. It is an array of double values corresponding to X-axis
   * values. (FLOAT64)
   */
  public static final int CDXProp_Spectrum_DataPoint = 0x0A86;

  /** The type of spectrum represented. (INT16) */
  public static final int CDXProp_Spectrum_Class = 0x0A87;

  /** Y value to be used to offset data when storing XML. (FLOAT64) */
  public static final int CDXProp_Spectrum_YLow = 0x0A88;

  /** Y scaling used to scale data when storing XML. (FLOAT64) */
  public static final int CDXProp_Spectrum_YScale = 0x0A89;

  // TLC properties

  /**
   * The distance of the origin line from the bottom of a TLC Plate, as a fraction of the total
   * height of the plate. (FLOAT64)
   */
  public static final int CDXProp_TLC_OriginFraction = 0x0AA0;

  /**
   * The distance of the solvent front from the top of a TLC Plate, as a fraction of the total
   * height of the plate. (FLOAT64)
   */
  public static final int CDXProp_TLC_SolventFrontFraction = 0x0AA1;

  /** Show the origin line near the base of the TLC Plate if non-zero. (CDXBoolean) */
  public static final int CDXProp_TLC_ShowOrigin = 0x0AA2;

  /** Show the solvent front line near the top of the TLC Plate if non-zero. (CDXBoolean) */
  public static final int CDXProp_TLC_ShowSolventFront = 0x0AA3;

  /** Show borders around the edges of the TLC Plate if non-zero. (CDXBoolean) */
  public static final int CDXProp_TLC_ShowBorders = 0x0AA4;

  /** Show tickmarks up the side of the TLC Plate if non-zero. (CDXBoolean) */
  public static final int CDXProp_TLC_ShowSideTicks = 0x0AA5;

  /** The Retention Factor of an individual spot. (FLOAT64) */
  public static final int CDXProp_TLC_Rf = 0x0AB0;

  /** The length of the "tail" of an individual spot. (CDXCoordinate) */
  public static final int CDXProp_TLC_Tail = 0x0AB1;

  /** Show the spot's Retention Fraction (Rf) value if non-zero. (CDXBoolean) */
  public static final int CDXProp_TLC_ShowRf = 0x0AB2;

  // Alternate Group properties

  /**
   * The bounding box of upper portion of the Named Alternative Group, containing the name of the
   * group. (CDXRectangle)
   */
  public static final int CDXProp_NamedAlternativeGroup_TextFrame = 0x0B00;

  /**
   * The bounding box of the lower portion of the Named Alternative Group, containing the definition
   * of the group. (CDXRectangle)
   */
  public static final int CDXProp_NamedAlternativeGroup_GroupFrame = 0x0B01;

  /** The number of attachment points in each alternative in a named alternative group. (INT16) */
  public static final int CDXProp_NamedAlternativeGroup_Valence = 0x0B02;

  // Geometry and Constraint properties

  /** The type of the geometrical feature (point, line, plane, etc.). (INT8) */
  public static final int CDXProp_GeometricFeature = 0x0B80;

  /**
   * The numeric relationship (if any) among the basis objects used to define this object. (INT8)
   */
  public static final int CDXProp_RelationValue = 0x0B81;

  /** An ordered list of objects used to define this object. (CDXObjectIDArray) */
  public static final int CDXProp_BasisObjects = 0x0B82;

  /** The constraint type (distance or angle). (INT8) */
  public static final int CDXProp_ConstraintType = 0x0B83;

  /** The minimum value of the constraint (FLOAT64) */
  public static final int CDXProp_ConstraintMin = 0x0B84;

  /** The maximum value of the constraint (FLOAT64) */
  public static final int CDXProp_ConstraintMax = 0x0B85;

  /**
   * Signifies whether unconnected atoms should be ignored within the exclusion sphere.
   * (CDXBooleanImplied)
   */
  public static final int CDXProp_IgnoreUnconnectedAtoms = 0x0B86;

  /** Signifies whether a dihedral is signed or unsigned. (CDXBooleanImplied) */
  public static final int CDXProp_DihedralIsChiral = 0x0B87;

  /**
   * For a point based on a normal, signifies whether it is in a specific direction relative to the
   * reference point. (CDXBooleanImplied)
   */
  public static final int CDXProp_PointIsDirected = 0x0B88;

  // Chemical properties

  /** The type of property (name, formula, molecular weight, etc.). */
  public static final int CDXProp_ChemicalPropertyType = 0x0BB0;

  /** The ID of a graphical object used to display the property value. */
  public static final int CDXProp_ChemicalPropertyDisplayID = 0x0BB1;

  /** Whether the property should be recalculated in response to changes in the basis objects. */
  public static final int CDXProp_ChemicalPropertyIsActive = 0x0BB2;

  // Reaction properties

  /**
   * Represents pairs of mapped atom IDs; each pair is a reactant atom mapped to to a product atom.
   * (CDXObjectIDArray)
   */
  public static final int CDXProp_ReactionStep_Atom_Map = 0x0C00;

  /** An order list of reactants present in the Reaction Step. (CDXObjectIDArray) */
  public static final int CDXProp_ReactionStep_Reactants = 0x0C01;

  /** An order list of products present in the Reaction Step. (CDXObjectIDArray) */
  public static final int CDXProp_ReactionStep_Products = 0x0C02;

  /**
   * An ordered list of pluses used to separate components of the Reaction Step. (CDXObjectIDArray)
   */
  public static final int CDXProp_ReactionStep_Plusses = 0x0C03;

  /**
   * An ordered list of arrows used to separate components of the Reaction Step. (CDXObjectIDArray)
   */
  public static final int CDXProp_ReactionStep_Arrows = 0x0C04;

  /** An order list of objects above the arrow in the Reaction Step. (CDXObjectIDArray) */
  public static final int CDXProp_ReactionStep_ObjectsAboveArrow = 0x0C05;

  /** An order list of objects below the arrow in the Reaction Step. (CDXObjectIDArray) */
  public static final int CDXProp_ReactionStep_ObjectsBelowArrow = 0x0C06;

  /**
   * Represents pairs of mapped atom IDs; each pair is a reactant atom mapped to to a product atom.
   * (CDXObjectIDArray)
   */
  public static final int CDXProp_ReactionStep_Atom_Map_Manual = 0x0C07;

  /**
   * Represents pairs of mapped atom IDs; each pair is a reactant atom mapped to to a product atom.
   * (CDXObjectIDArray)
   */
  public static final int CDXProp_ReactionStep_Atom_Map_Auto = 0x0C08;

  // Object tag properties

  /** The tag's data type. (INT16) */
  public static final int CDXProp_ObjectTag_Type = 0x0D00;

  /** obsolete (obsolete) */
  public static final int CDXProp_Unused6 = 0x0D01;

  /** obsolete (obsolete) */
  public static final int CDXProp_Unused7 = 0x0D02;

  /** The tag will participate in tracking if non-zero. (CDXBoolean) */
  public static final int CDXProp_ObjectTag_Tracking = 0x0D03;

  /** The tag will be resaved to a CDX file if non-zero. (CDXBoolean) */
  public static final int CDXProp_ObjectTag_Persistent = 0x0D04;

  /**
   * The value is a INT32, FLOAT64 or unformatted string depending on the value of ObjectTag_Type.
   * (varies)
   */
  public static final int CDXProp_ObjectTag_Value = 0x0D05;

  /** How the indicator should be positioned with respect to its containing object. (INT8) */
  public static final int CDXProp_Positioning = 0x0D06;

  /** Angular positioning, in radians * 65536. (INT32) */
  public static final int CDXProp_PositioningAngle = 0x0D07;

  /** Offset positioning. (CDXPoint2D) */
  public static final int CDXProp_PositioningOffset = 0x0D08;

  // Sequence properties

  /** A unique (but otherwise random) identifier for a given Sequence object. (CDXString) */
  public static final int CDXProp_Sequence_Identifier = 0x0E00;

  // Cross reference properties

  /**
   * An external object containing (as an embedded object) the document containing the Sequence
   * object being referenced. (CDXString)
   */
  public static final int CDXProp_CrossReference_Container = 0x0F00;

  /** An external document containing the Sequence object being referenced. (CDXString) */
  public static final int CDXProp_CrossReference_Document = 0x0F01;

  /** A unique (but otherwise random) identifier for a given Cross-Reference object. (CDXString) */
  public static final int CDXProp_CrossReference_Identifier = 0x0F02;

  /**
   * A value matching the SequenceIdentifier of the Sequence object to be referenced. (CDXString)
   */
  public static final int CDXProp_CrossReference_Sequence = 0x0F03;

  // Miscellaneous properties.

  /** The height of the viewing window of a template grid. (CDXCoordinate) */
  public static final int CDXProp_Template_PaneHeight = 0x1000;

  /** The number of rows of the CDX TemplateGrid object. (INT16) */
  public static final int CDXProp_Template_NumRows = 0x1001;

  /** The number of columns of the CDX TemplateGrid object. (INT16) */
  public static final int CDXProp_Template_NumColumns = 0x1002;

  /** The group is considered to be integral (non-subdivisible) if non-zero. (CDXBoolean) */
  public static final int CDXProp_Group_Integral = 0x1100;

  /**
   * An array of vertical positions that subdivide a page into regions. This property was defined
   * for future compatibility and was not read or written by any public release of ChemDraw. It is
   * obsolete starting with ChemDraw 7.0, and the more-flexible Splitter object should be used
   * instead. (CDXObjectIDArray)
   */
  public static final int CDXProp_SplitterPositions = 0x1FF0;

  /** An array of vertical positions that subdivide a page into regions. (CDXObjectIDArray) */
  public static final int CDXProp_PageDefinition = 0x1FF1;

  // User defined properties
  // First 1024 tags are reserved for temporary tags used only during the
  // runtime.

  public static final int CDXUser_TemporaryBegin = CDXTag_UserDefined;

  public static final int CDXUser_TemporaryEnd = CDXTag_UserDefined + 0x0400;

  // Objects.
  public static final int CDXObj_Document = CDXTag_Object; // 0x8000

  public static final int CDXObj_Page = 0x8001;

  public static final int CDXObj_Group = 0x8002;

  public static final int CDXObj_Fragment = 0x8003;

  public static final int CDXObj_Node = 0x8004;

  public static final int CDXObj_Bond = 0x8005;

  public static final int CDXObj_Text = 0x8006;

  public static final int CDXObj_Graphic = 0x8007;

  public static final int CDXObj_Curve = 0x8008;

  public static final int CDXObj_EmbeddedObject = 0x8009;

  public static final int CDXObj_NamedAlternativeGroup = 0x800a;

  public static final int CDXObj_TemplateGrid = 0x800b;

  public static final int CDXObj_RegistryNumber = 0x800c;

  public static final int CDXObj_ReactionScheme = 0x800d;

  public static final int CDXObj_ReactionStep = 0x800e;

  public static final int CDXObj_ObjectDefinition = 0x800f;

  public static final int CDXObj_Spectrum = 0x8010;

  public static final int CDXObj_ObjectTag = 0x8011;

  public static final int CDXObj_OleClientItem = 0x8012; // obsolete

  public static final int CDXObj_Sequence = 0x8013;

  public static final int CDXObj_CrossReference = 0x8014;

  public static final int CDXObj_Splitter = 0x8015;

  public static final int CDXObj_Table = 0x8016;

  public static final int CDXObj_BracketedGroup = 0x8017;

  public static final int CDXObj_BracketAttachment = 0x8018;

  public static final int CDXObj_CrossingBond = 0x8019;

  public static final int CDXObj_Border = /*0x8020*/ 0x801a;

  public static final int CDXObj_Geometry = /*0x8021*/ 0x801b;

  public static final int CDXObj_Constraint = /*0x8022*/ 0x801c;

  public static final int CDXObj_TLCPlate = /*0x8023*/ 0x801d;

  public static final int CDXObj_TLCLane = /*0x8024*/ 0x801e;

  public static final int CDXObj_TLCSpot = /*0x8025*/ 0x801f;

  public static final int CDXObj_ChemicalProperty = /*0x8026*/ 0x8020;

  public static final int CDXObj_Arrow = 0x8021;

  // Add new objects here
  public static final int CDXObj_UnknownObject = 0x8FFF;

  public static final int CDXObj_ColoredMolecularArea = 0x8032;

  // enum CDXNodeType

  /** A node of unspecified type. */
  public static final int CDXNodeType_Unspecified = 0;

  public static final int CDXNodeType_Element = 1;

  public static final int CDXNodeType_ElementList = 2;

  public static final int CDXNodeType_ElementListNickname = 3;

  public static final int CDXNodeType_Nickname = 4;

  public static final int CDXNodeType_Fragment = 5;

  public static final int CDXNodeType_Formula = 6;

  public static final int CDXNodeType_GenericNickname = 7;

  public static final int CDXNodeType_AnonymousAlternativeGroup = 8;

  public static final int CDXNodeType_NamedAlternativeGroup = 9;

  public static final int CDXNodeType_MultiAttachment = 10;

  public static final int CDXNodeType_VariableAttachment = 11;

  public static final int CDXNodeType_ExternalConnectionPoint = 12;

  public static final int CDXNodeType_LinkNode = 13;

  // enum CDXLabelDisplay

  /** Label is aligned automatically */
  public static final int CDXLabelDisplay_Auto = 0;

  /** Label is left-aligned */
  public static final int CDXLabelDisplay_Left = 1;

  /** Label is centered */
  public static final int CDXLabelDisplay_Center = 2;

  /** Label is right-aligned */
  public static final int CDXLabelDisplay_Right = 3;

  /** Label is stacked above */
  public static final int CDXLabelDisplay_Above = 4;

  /** Label is stacked below */
  public static final int CDXLabelDisplay_Below = 5;

  /** Best initial alignment */
  public static final int CDXLabelDisplay_BestInitial = 6;

  // enum CDXRadical // Same as MDL codes

  /** Not a radical */
  public static final int CDXRadical_None = 0;

  /** diradical singlet (two dots) */
  public static final int CDXRadical_Singlet = 1;

  /** monoradical (one dot) */
  public static final int CDXRadical_Doublet = 2;

  /** diradical triplet (two dots) */
  public static final int CDXRadical_Triplet = 3;

  // enum CDXIsotope
  public static final int CDXIsotope_Natural = 0;

  // enum CDXRingBondCount
  public static final int CDXRingBondCount_Unspecified = -1;

  public static final int CDXRingBondCount_NoRingBonds = 0;

  public static final int CDXRingBondCount_AsDrawn = 1;

  public static final int CDXRingBondCount_SimpleRing = 2;

  public static final int CDXRingBondCount_Fusion = 3;

  public static final int CDXRingBondCount_SpiroOrHigher = 4;

  // enum CDXUnsaturation
  public static final int CDXUnsaturation_Unspecified = 0;

  public static final int CDXUnsaturation_MustBeAbsent = 1;

  public static final int CDXUnsaturation_MustBePresent = 2;

  // enum CDXReactionStereo
  public static final int CDXReactionStereo_Unspecified = 0;

  public static final int CDXReactionStereo_Inversion = 1;

  public static final int CDXReactionStereo_Retention = 2;

  // enum CDXTranslation
  public static final int CDXTranslation_Equal = 0;

  public static final int CDXTranslation_Broad = 1;

  public static final int CDXTranslation_Narrow = 2;

  public static final int CDXTranslation_Any = 3;

  // enum CDXAbundance
  public static final int CDXAbundance_Unspecified = 0;

  public static final int CDXAbundance_Any = 1;

  public static final int CDXAbundance_Natural = 2;

  public static final int CDXAbundance_Enriched = 3;

  public static final int CDXAbundance_Deficient = 4;

  public static final int CDXAbundance_Nonnatural = 5;

  // enum CDXExternalConnectionType
  public static final int CDXExternalConnection_Unspecified = 0;

  public static final int CDXExternalConnection_Diamond = 1;

  public static final int CDXExternalConnection_Star = 2;

  public static final int CDXExternalConnection_PolymerBead = 3;

  public static final int CDXExternalConnection_Wavy = 4;

  public static final int CDXExternalConnection_Residue = 5;

  // enum CDXAtomGeometry
  public static final int CDXAtomGeometry_Unknown = 0;

  public static final int CDXAtomGeometry_1Ligand = 1;

  public static final int CDXAtomGeometry_Linear = 2;

  public static final int CDXAtomGeometry_Bent = 3;

  public static final int CDXAtomGeometry_TrigonalPlanar = 4;

  public static final int CDXAtomGeometry_TrigonalPyramidal = 5;

  public static final int CDXAtomGeometry_SquarePlanar = 6;

  public static final int CDXAtomGeometry_Tetrahedral = 7;

  public static final int CDXAtomGeometry_TrigonalBipyramidal = 8;

  public static final int CDXAtomGeometry_SquarePyramidal = 9;

  public static final int CDXAtomGeometry_5Ligand = 10;

  public static final int CDXAtomGeometry_Octahedral = 11;

  public static final int CDXAtomGeometry_6Ligand = 12;

  public static final int CDXAtomGeometry_7Ligand = 13;

  public static final int CDXAtomGeometry_8Ligand = 14;

  public static final int CDXAtomGeometry_9Ligand = 15;

  public static final int CDXAtomGeometry_10Ligand = 16;

  // enum CDXBondOrder

  /** Single bond */
  public static final int CDXBondOrder_Single = 0x0001;

  /** Double bond */
  public static final int CDXBondOrder_Double = 0x0002;

  /** Triple bond */
  public static final int CDXBondOrder_Triple = 0x0004;

  /** Quadruple bond (used for some inorganic complexes) */
  public static final int CDXBondOrder_Quadruple = 0x0008;

  /** Quintuple bond (used for some inorganic complexes) */
  public static final int CDXBondOrder_Quintuple = 0x0010;

  /** Hextuple bond (used for some inorganic complexes) */
  public static final int CDXBondOrder_Sextuple = 0x0020;

  /** Bond of order one-half */
  public static final int CDXBondOrder_Half = 0x0040;

  /** Bond of order one and one-half (an aromatic bond) */
  public static final int CDXBondOrder_OneHalf = 0x0080;

  /** Bond of order two and one-half (in benzyne, for example) */
  public static final int CDXBondOrder_TwoHalf = 0x0100;

  /** Bond of order three and one-half (used for some inorganic complexes) */
  public static final int CDXBondOrder_ThreeHalf = 0x0200;

  /** Bond of order four and one-half (used for some inorganic complexes) */
  public static final int CDXBondOrder_FourHalf = 0x0400;

  /** Bond of order five and one-half (used for some inorganic complexes) */
  public static final int CDXBondOrder_FiveHalf = 0x0800;

  /** Dative bond, from the "begin" atom to the "end" atom */
  public static final int CDXBondOrder_Dative = 0x1000;

  /** Ionic bond */
  public static final int CDXBondOrder_Ionic = 0x2000;

  /** Hydrogen bond */
  public static final int CDXBondOrder_Hydrogen = 0x4000;

  /** Three-center-bond (in boranes, for example) */
  public static final int CDXBondOrder_ThreeCenter = 0x8000;

  /** Single or double bond for substructure queries */
  public static final int CDXBondOrder_SingleOrDouble = CDXBondOrder_Single | CDXBondOrder_Double;

  /** Single or aromatic bond for substructure queries */
  public static final int CDXBondOrder_SingleOrAromatic =
      CDXBondOrder_Single | CDXBondOrder_OneHalf;

  /** Double or aromatic bond for substructure queries */
  public static final int CDXBondOrder_DoubleOrAromatic =
      CDXBondOrder_Double | CDXBondOrder_OneHalf;

  /** Any bond for substructure queries */
  public static final int CDXBondOrder_Any = -1;

  // Permit combination of CDXBondOrder values

  // enum CDXBondDisplay
  /** Solid bond */
  public static final int CDXBondDisplay_Solid = 0;

  /** Dashed bond */
  public static final int CDXBondDisplay_Dash = 1;

  /** Hashed bond */
  public static final int CDXBondDisplay_Hash = 2;

  /** Wedged hashed bond with the narrow end on the "begin" atom */
  public static final int CDXBondDisplay_WedgedHashBegin = 3;

  /** Wedged hashed bond with the narrow end on the "end" atom */
  public static final int CDXBondDisplay_WedgedHashEnd = 4;

  /** Bold bond */
  public static final int CDXBondDisplay_Bold = 5;

  /** Wedged solid bond with the narrow end on the "begin" atom */
  public static final int CDXBondDisplay_WedgeBegin = 6;

  /** Wedged solid bond with the narrow end on the "end" atom */
  public static final int CDXBondDisplay_WedgeEnd = 7;

  /** Wavy bond */
  public static final int CDXBondDisplay_Wavy = 8;

  /** Wedged hollow bond with the narrow end on the "begin" atom */
  public static final int CDXBondDisplay_HollowWedgeBegin = 9;

  /** Wedged hollow bond with the narrow end on the "end" atom */
  public static final int CDXBondDisplay_HollowWedgeEnd = 10;

  /** Wedged wavy bond with the narrow end on the "begin" atom */
  public static final int CDXBondDisplay_WavyWedgeBegin = 11;

  /** Wedged wavy bond with the narrow end on the "end" atom */
  public static final int CDXBondDisplay_WavyWedgeEnd = 12;

  /** Dotted bond */
  public static final int CDXBondDisplay_Dot = 13;

  /** Dashed-and-dotted bond */
  public static final int CDXBondDisplay_DashDot = 14;

  // enum CDXBondDoublePosition

  /** Double bond is centered, but was positioned automatically by the program */
  public static final int CDXBondDoublePosition_AutoCenter = 0x0000;

  /**
   * Double bond is on the right (viewing from the "begin" atom to the "end" atom), but was
   * positioned automatically by the program
   */
  public static final int CDXBondDoublePosition_AutoRight = 0x0001;

  /**
   * Double bond is on the left (viewing from the "begin" atom to the "end" atom), but was
   * positioned automatically by the program
   */
  public static final int CDXBondDoublePosition_AutoLeft = 0x0002;

  /** Double bond is centered, and was positioned manually by the user */
  public static final int CDXBondDoublePosition_UserCenter = 0x0100;

  /**
   * Double bond is on the right (viewing from the "begin" atom to the "end" atom), and was
   * positioned manually by the user
   */
  public static final int CDXBondDoublePosition_UserRight = 0x0101;

  /**
   * Double bond is on the left (viewing from the "begin" atom to the "end" atom), and was
   * positioned manually by the user
   */
  public static final int CDXBondDoublePosition_UserLeft = 0x0102;

  // enum CDXBondTopology

  /** Ring/chain status of the bond is unspecified. */
  public static final int CDXBondTopology_Unspecified = 0;

  /** Bond must be in a ring. */
  public static final int CDXBondTopology_Ring = 1;

  /** Bond must not be in a ring. */
  public static final int CDXBondTopology_Chain = 2;

  /** Bond may be in either a ring or a chain. */
  public static final int CDXBondTopology_RingOrChain = 3;

  // enum CDXBondReactionParticipation

  /** Bond involvement in reacting center is not specified */
  public static final int CDXBondReactionParticipation_Unspecified = 0;

  /** Bond is part of reacting center but not made/broken nor order changed */
  public static final int CDXBondReactionParticipation_ReactionCenter = 1;

  /** Bond is made or broken in reaction */
  public static final int CDXBondReactionParticipation_MakeOrBreak = 2;

  /** Bond's order changes in reaction */
  public static final int CDXBondReactionParticipation_ChangeType = 3;

  /** Bond is made or broken, or its order changes in the reaction */
  public static final int CDXBondReactionParticipation_MakeAndChange = 4;

  /** Bond is not part of reacting center */
  public static final int CDXBondReactionParticipation_NotReactionCenter = 5;

  /** Bond does not change in course of reaction, but it is part of the reacting center */
  public static final int CDXBondReactionParticipation_NoChange = 6;

  /**
   * The structure was partially mapped, but the reaction involvement of this bond was not
   * determined
   */
  public static final int CDXBondReactionParticipation_Unmapped = 7;

  // enum CDXTextJustification
  public static final int CDXTextJustification_Right = -1;

  public static final int CDXTextJustification_Left = 0;

  public static final int CDXTextJustification_Center = 1;

  public static final int CDXTextJustification_Full = 2;

  public static final int CDXTextJustification_Above = 3;

  public static final int CDXTextJustification_Below = 4;

  public static final int CDXTextJustification_Auto = 5;

  public static final int CDXTextJustification_BestInitial = 6;

  public static final String CDXTagType_Unknown = "unknown";

  public static final String CDXTagType_Query = "query";

  public static final String CDXTagType_Rxn = "reaction";

  public static final String CDXTagType_Stereo = "org/openscience/cdk/stereo";

  public static final String CDXTagType_Number = "number";

  public static final String CDXTagType_Heading = "heading";

  public static final String CDXTagType_IDTerm = "idterm";

  public static final String CDXTagType_BracketUsage = "bracketusage";

  public static final String CDXTagType_PolymerRepeat = "polymerrepeat";

  public static final String CDXTagType_PolymerFlip = "polymerflip";

  public static final String CDXTagType_Deviation = "deviation";

  public static final String CDXTagType_Distance = "distance";

  public static final String CDXTagType_Angle = "angle";

  public static final String CDXTagType_Rf = "rf";

  // enum CDXPositioningType
  public static final int CDXPositioningType_Auto = 0;

  public static final int CDXPositioningType_Angle = 1;

  public static final int CDXPositioningType_Offset = 2;

  public static final int CDXPositioningType_Absolute = 3;

  // enum CDXPageDefinition
  public static final int CDXPageDefinition_Undefined = 0;

  public static final int CDXPageDefinition_Center = 1;

  public static final int CDXPageDefinition_TL4 = 2;

  public static final int CDXPageDefinition_IDTerm = 3;

  public static final int CDXPageDefinition_FlushLeft = 4;

  public static final int CDXPageDefinition_FlushRight = 5;

  public static final int CDXPageDefinition_Reaction1 = 6;

  public static final int CDXPageDefinition_Reaction2 = 7;

  public static final int CDXPageDefinition_MulticolumnTL4 = 8;

  public static final int CDXPageDefinition_MulticolumnNonTL4 = 9;

  public static final int CDXPageDefinition_UserDefined = 10;

  public static final int CDXLineHeight_Variable = 0;

  public static final int CDXLineHeight_Automatic = 1;

  // enum CDXGraphicType
  /** Undefined */
  public static final int CDXGraphicType_Undefined = 0;

  /** Line */
  public static final int CDXGraphicType_Line = 1;

  /** Arc */
  public static final int CDXGraphicType_Arc = 2;

  /** Rectangle */
  public static final int CDXGraphicType_Rectangle = 3;

  /** Oval */
  public static final int CDXGraphicType_Oval = 4;

  /** Orbital */
  public static final int CDXGraphicType_Orbital = 5;

  /** Bracket */
  public static final int CDXGraphicType_Bracket = 6;

  /** Symbol */
  public static final int CDXGraphicType_Symbol = 7;

  // enum CDXBracketType

  /** Round-pair */
  public static final int CDXBracketType_RoundPair = 0;

  /** Square-pair */
  public static final int CDXBracketType_SquarePair = 1;

  /** Curly pair */
  public static final int CDXBracketType_CurlyPair = 2;

  /** Square */
  public static final int CDXBracketType_Square = 3;

  /** Curly */
  public static final int CDXBracketType_Curly = 4;

  /** Round */
  public static final int CDXBracketType_Round = 5;

  // enum CDXRectangleType

  /** Plain rectangle. */
  public static final int CDXRectangleType_Plain = 0x0000;

  /** Round-edge rectangle. */
  public static final int CDXRectangleType_RoundEdge = 0x0001;

  /** Shadow rectangle. */
  public static final int CDXRectangleType_Shadow = 0x0002;

  /** Shaded rectangle. */
  public static final int CDXRectangleType_Shaded = 0x0004;

  /** Filled rectangle. */
  public static final int CDXRectangleType_Filled = 0x0008;

  /** Dashed rectangle. */
  public static final int CDXRectangleType_Dashed = 0x0010;

  /** Bold rectangle. */
  public static final int CDXRectangleType_Bold = 0x0020;

  // enum CDXOvalType

  /** Circle */
  public static final int CDXOvalType_Circle = 0x0001;

  /** Shaded */
  public static final int CDXOvalType_Shaded = 0x0002;

  /** Filled */
  public static final int CDXOvalType_Filled = 0x0004;

  /** Dashed */
  public static final int CDXOvalType_Dashed = 0x0008;

  /** Bold */
  public static final int CDXOvalType_Bold = 0x0010;

  /** Shadowed */
  public static final int CDXOvalType_Shadowed = 0x0020;

  // enum CDXSymbolType

  /** Lone-pair */
  public static final int CDXSymbolType_LonePair = 0;

  /** Elecron */
  public static final int CDXSymbolType_Electron = 1;

  /** Radical cation */
  public static final int CDXSymbolType_RadicalCation = 2;

  /** Radical anion */
  public static final int CDXSymbolType_RadicalAnion = 3;

  /** Circle plus */
  public static final int CDXSymbolType_CirclePlus = 4;

  /** Circle minus */
  public static final int CDXSymbolType_CircleMinus = 5;

  /** Dagger */
  public static final int CDXSymbolType_Dagger = 6;

  /** Double dagger */
  public static final int CDXSymbolType_DoubleDagger = 7;

  /** Plus */
  public static final int CDXSymbolType_Plus = 8;

  /** Minus */
  public static final int CDXSymbolType_Minus = 9;

  /** Racemic */
  public static final int CDXSymbolType_Racemic = 10;

  /** Absolute */
  public static final int CDXSymbolType_Absolute = 11;

  /** Relative */
  public static final int CDXSymbolType_Relative = 12;

  // enum CDXLineType

  /** Solid line */
  public static final int CDXLineType_Solid = 0x0000;

  /** Dashed line */
  public static final int CDXLineType_Dashed = 0x0001;

  /** Bold line */
  public static final int CDXLineType_Bold = 0x0002;

  /** Wavy line */
  public static final int CDXLineType_Wavy = 0x0004;

  // enum CDXArrowType

  /** No head. */
  public static final int CDXArrowType_NoHead = 0;

  /** Half head. */
  public static final int CDXArrowType_HalfHead = 1;

  /** Full head. */
  public static final int CDXArrowType_FullHead = 2;

  /** Resonance. */
  public static final int CDXArrowType_Resonance = 4;

  /** Equilibrium. */
  public static final int CDXArrowType_Equilibrium = 8;

  /** Hollow. */
  public static final int CDXArrowType_Hollow = 16;

  /** Retro-Synthetic. */
  public static final int CDXArrowType_RetroSynthetic = 32;

  // enum CDXOrbitalType

  /** s orbital */
  public static final int CDXOrbitalType_s = 0x0000;

  /** Oval-shaped sigma or pi orbital */
  public static final int CDXOrbitalType_oval = 0x0001;

  /** One lobe of a p orbital */
  public static final int CDXOrbitalType_lobe = 0x0002;

  /** Complete p orbital */
  public static final int CDXOrbitalType_p = 0x0003;

  /** hydrid orbital */
  public static final int CDXOrbitalType_hybridPlus = 0x0004;

  /** hydrid orbital (opposite shading) */
  public static final int CDXOrbitalType_hybridMinus = 0x0005;

  /** d<sub>z<sup>2</sup></sub> orbital */
  public static final int CDXOrbitalType_dz2Plus = 0x0006;

  /** d<sub>z<sup>2</sup></sub> orbital (opposite shading) */
  public static final int CDXOrbitalType_dz2Minus = 0x0007;

  /** d<sub>xy</sub> orbital */
  public static final int CDXOrbitalType_dxy = 0x0008;

  /** shaded s orbital */
  public static final int CDXOrbitalType_sShaded = 0x0100;

  /** shaded Oval-shaped sigma or pi orbital */
  public static final int CDXOrbitalType_ovalShaded = 0x0101;

  /** shaded single lobe of a p orbital */
  public static final int CDXOrbitalType_lobeShaded = 0x0102;

  /** shaded Complete p orbital */
  public static final int CDXOrbitalType_pShaded = 0x0103;

  /** filled s orbital */
  public static final int CDXOrbitalType_sFilled = 0x0200;

  /** filled Oval-shaped sigma or pi orbital */
  public static final int CDXOrbitalType_ovalFilled = 0x0201;

  /** filled single lobe of a p orbital */
  public static final int CDXOrbitalType_lobeFilled = 0x0202;

  /** filled Complete p orbital */
  public static final int CDXOrbitalType_pFilled = 0x0203;

  /** filled hydrid orbital */
  public static final int CDXOrbitalType_hybridPlusFilled = 0x0204;

  /** filled hydrid orbital (opposite shading) */
  public static final int CDXOrbitalType_hybridMinusFilled = 0x0205;

  /** filled d<sub>z<sup>2</sup></sub> orbital */
  public static final int CDXOrbitalType_dz2PlusFilled = 0x0206;

  /** filled d<sub>z<sup>2</sup></sub> orbital (opposite shading) */
  public static final int CDXOrbitalType_dz2MinusFilled = 0x0207;

  /** filled d<sub>xy</sub> orbital */
  public static final int CDXOrbitalType_dxyFilled = 0x0208;

  // enum CDXCurveType

  /** Curve type : Plain */
  public static final int CDXCurveType_Plain = 0x0000;

  /** Curve type : Closed */
  public static final int CDXCurveType_Closed = 0x0001;

  /** Curve type : Dashed */
  public static final int CDXCurveType_Dashed = 0x0002;

  /** Curve type : Bold */
  public static final int CDXCurveType_Bold = 0x0004;

  /** Curve type : Arrow at end */
  public static final int CDXCurveType_ArrowAtEnd = 0x0008;

  /** Curve type : Arrow at start */
  public static final int CDXCurveType_ArrowAtStart = 0x0010;

  /** Curve type : Half-arrow at end */
  public static final int CDXCurveType_HalfArrowAtEnd = 0x0020;

  /** Curve type : Half-arrow at start */
  public static final int CDXCurveType_HalfArrowAtStart = 0x0040;

  /** Curve type : Filled */
  public static final int CDXCurveType_Filled = 0x0080;

  /** Curve type : Shaded */
  public static final int CDXCurveType_Shaded = 0x0100;

  /** Curve type : Doubled */
  public static final int CDXCurveType_Doubled = 0x0200;

  // enum CDXFillType

  /** Unspecified fill type. */
  public static final int CDXFillType_Unspecified = 0x0000;

  /** No fill type. */
  public static final int CDXFillType_None = 0x0001;

  /** Solid fill type. */
  public static final int CDXFillType_Solid = 0x0002;

  /** Shaded fill type. */
  public static final int CDXFillType_Shaded = 0x0003;

  /** Faded fill type. */
  public static final int CDXFillType_Faded = 0x0004;

  // enum CDXBracketUsage

  /** Unspecified bracket usage. */
  public static final int CDXBracketUsage_Unspecified = 0;

  /** Brackets enclose any polymer. */
  public static final int CDXBracketUsage_Anypolymer = 18;

  /** Brackets enclose an individual component of an ordered or unordered mixture. */
  public static final int CDXBracketUsage_Component = 13;

  /** Brackets enclose one of several repeating units that co-polymerize. */
  public static final int CDXBracketUsage_Copolymer = 6;

  /**
   * Brackets enclose one of several repeating units that co-polymerize in an alternating fashion.
   */
  public static final int CDXBracketUsage_CopolymerAlternating = 7;

  /** Brackets enclose one of several repeating units that co-polymerize in a block fashion. */
  public static final int CDXBracketUsage_CopolymerBlock = 9;

  /** Brackets enclose one of several repeating units that co-polymerize in a random fashion. */
  public static final int CDXBracketUsage_CopolymerRandom = 8;

  /** Brackets enclose a cross-linking repeating unit in a source-based representation. */
  public static final int CDXBracketUsage_Crosslink = 10;

  /** Brackets enclose a generic polymer. */
  public static final int CDXBracketUsage_Generic = 17;

  /** Brackets enclose a graft repeating unit in a source-based representation. */
  public static final int CDXBracketUsage_Graft = 11;

  /** Brackets enclose a source-based monomeric unit that is known not to self-polymerize. */
  public static final int CDXBracketUsage_Mer = 5;

  /**
   * Brackets enclose a collection of substances that comprise an ordered mixture (also called a
   * formulation).
   */
  public static final int CDXBracketUsage_MixtureOrdered = 15;

  /** Brackets enclose a collection of substances that comprise an unordered mixture. */
  public static final int CDXBracketUsage_MixtureUnordered = 14;

  /** Brackets enclose a modified repeating unit in a source-based representation. */
  public static final int CDXBracketUsage_Modification = 12;

  /** Brackets enclose a source-based monomeric unit. */
  public static final int CDXBracketUsage_Monomer = 4;

  /** Brackets enclose a structure or fragment that is repeated some number of times. */
  public static final int CDXBracketUsage_MultipleGroup = 16;

  /** Brackets enclose a Structural Repeating Unit in a structure-based representation. */
  public static final int CDXBracketUsage_SRU = 3;

  /** (unused) */
  public static final int CDXBracketUsage_Unused1 = 1;

  /** (unused) */
  public static final int CDXBracketUsage_Unused2 = 2;

  // enum CDXPolymerRepeatPattern

  /** One end of the repeating unit is connected to the other end of the adjacent repeating unit. */
  public static final int CDXPolymerRepeatPattern_HeadToTail = 0;

  /** One end of the repeating unit is connected to the same end of the adjacent repeating unit. */
  public static final int CDXPolymerRepeatPattern_HeadToHead = 1;

  /** A mixture of the above, or an unknown repeat pattern. */
  public static final int CDXPolymerRepeatPattern_EitherUnknown = 2;

  // enum CDXPolymerFlipType

  /** Unspecified flip type. */
  public static final int CDXPolymerFlipType_Unspecified = 0;

  /** The orientation of the repeating unit does not change between adjacent units. */
  public static final int CDXPolymerFlipType_NoFlip = 1;

  /** The orientation of the repeating unit does change between adjacent units. */
  public static final int CDXPolymerFlipType_Flip = 2;

  // enum CDXSpectrumYType

  /** The axis type is unknown. Not recommended. */
  public static final int CDXSpectrumYType_Unknown = 0;

  /**
   * Axis is in absorbance units, and consequently has a baseline of 0.0 with peaks pointing up.
   * Only for IR spectra.
   */
  public static final int CDXSpectrumYType_Absorbance = 1;

  /**
   * Axis is in transmittance units. The baseline is at 1.0 and peaks points down to a value of 0.0
   * being no transmission. Only for IR spectra.
   */
  public static final int CDXSpectrumYType_Transmittance = 2;

  /**
   * Axis is in transmittance units*100. The baseline is at 100% and peaks points down to a value of
   * 0.0 being no transmission. Only for IR spectra.
   */
  public static final int CDXSpectrumYType_PercentTransmittance = 3;

  /** Axis is some other type. */
  public static final int CDXSpectrumYType_Other = 4;

  /**
   * Axis is unscaled -- essentially, the absolute values have no meaning and only relative values
   * matter.
   */
  public static final int CDXSpectrumYType_ArbitraryUnits = 5;

  // enum CDXSpectrumXType

  /** The axis type is unknown. Not recommended */
  public static final int CDXSpectrumXType_Unknown = 0;

  /** Axis is in wavenumbers. Only for IR spectra */
  public static final int CDXSpectrumXType_Wavenumbers = 1;

  /** Axis is in microns. Only for IR spectra */
  public static final int CDXSpectrumXType_Microns = 2;

  /** Axis is in Hertz */
  public static final int CDXSpectrumXType_Hertz = 3;

  /** Axis is in m/e. Only for MS spectra */
  public static final int CDXSpectrumXType_MassUnits = 4;

  /** Axis is in parts per million. Only for NMR spectra */
  public static final int CDXSpectrumXType_PartsPerMillion = 5;

  /** Axis is some other type */
  public static final int CDXSpectrumXType_Other = 6;

  // enum CDXSpectrumClass

  /** Unknown spectral type. Not recommended */
  public static final int CDXSpectrumClass_Unknown = 0;

  /** GC (not supported in ChemDraw) */
  public static final int CDXSpectrumClass_Chromatogram = 1;

  /** Infrared */
  public static final int CDXSpectrumClass_Infrared = 2;

  /** UVVis (not supported in ChemDraw) */
  public static final int CDXSpectrumClass_UVVis = 3;

  /** X-Ray Diffraction (not supported in ChemDraw) */
  public static final int CDXSpectrumClass_XRayDiffraction = 4;

  /** Mass Spectrum */
  public static final int CDXSpectrumClass_MassSpectrum = 5;

  /** NMR */
  public static final int CDXSpectrumClass_NMR = 6;

  /** Raman */
  public static final int CDXSpectrumClass_Raman = 7;

  /** Fluorescence */
  public static final int CDXSpectrumClass_Fluorescence = 8;

  /** Atomic Absorption (not supported in ChemDraw) */
  public static final int CDXSpectrumClass_Atomic = 9;

  // enum CDXDrawingSpaceType
  public static final int CDXDrawingSpace_Pages = 0;

  public static final int CDXDrawingSpace_Poster = 1;

  // enum CDXAtomCIPType
  public static final int CDXCIPAtom_Undetermined = 0;

  public static final int CDXCIPAtom_None = 1;

  public static final int CDXCIPAtom_R = 2;

  public static final int CDXCIPAtom_S = 3;

  public static final int CDXCIPAtom_r = 4;

  public static final int CDXCIPAtom_s = 5;

  /** No hash/wedge, but if there were one, it would have stereochemistry. */
  public static final int CDXCIPAtom_Unspecified = 6;

  // enum CDXBondCIPType
  public static final int CDXCIPBond_Undetermined = 0;

  public static final int CDXCIPBond_None = 1;

  public static final int CDXCIPBond_E = 2;

  public static final int CDXCIPBond_Z = 3;

  // enum CDXObjectTagType
  public static final int CDXObjectTagType_Undefined = 0;

  public static final int CDXObjectTagType_Double = 1;

  public static final int CDXObjectTagType_Long = 2;

  public static final int CDXObjectTagType_String = 3;

  // enum CDXSideType
  public static final int CDXSideType_Undefined = 0;

  public static final int CDXSideType_Top = 1;

  public static final int CDXSideType_Left = 2;

  public static final int CDXSideType_Bottom = 3;

  public static final int CDXSideType_Right = 4;

  // enum CDXGeometricFeature
  public static final int CDXGeometricFeature_Undefined = 0;

  public static final int CDXGeometricFeature_PointFromPointPointDistance = 1;

  public static final int CDXGeometricFeature_PointFromPointPointPercentage = 2;

  public static final int CDXGeometricFeature_PointFromPointNormalDistance = 3;

  public static final int CDXGeometricFeature_LineFromPoints = 4;

  public static final int CDXGeometricFeature_PlaneFromPoints = 5;

  public static final int CDXGeometricFeature_PlaneFromPointLine = 6;

  public static final int CDXGeometricFeature_CentroidFromPoints = 7;

  public static final int CDXGeometricFeature_NormalFromPointPlane = 8;

  // enum CDXConstraintType
  public static final int CDXConstraintType_Undefined = 0;

  public static final int CDXConstraintType_Distance = 1;

  public static final int CDXConstraintType_Angle = 2;

  public static final int CDXConstraintType_ExclusionSphere = 3;

  // enum CDXCharSet

  /** Unknown */
  public static final int CDXCharSetUnknown = 0;

  /** EBCDICOEM */
  public static final int CDXCharSetEBCDICOEM = 37;

  /** MSDOSUS */
  public static final int CDXCharSetMSDOSUS = 437;

  /** EBCDIC500V1 */
  public static final int CDXCharSetEBCDIC500V1 = 500;

  /** ASMO-708 */
  public static final int CDXCharSetArabicASMO708 = 708;

  /** ArabicASMO449P */
  public static final int CDXCharSetArabicASMO449P = 709;

  /** ArabicTransparent */
  public static final int CDXCharSetArabicTransparent = 710;

  /** DOS-720 */
  public static final int CDXCharSetArabicTransparentASMO = 720;

  /** Greek437G */
  public static final int CDXCharSetGreek437G = 737;

  /** cp775 */
  public static final int CDXCharSetBalticOEM = 775;

  /** windows-850 */
  public static final int CDXCharSetMSDOSLatin1 = 850;

  /** ibm852 */
  public static final int CDXCharSetMSDOSLatin2 = 852;

  /** cp855 */
  public static final int CDXCharSetIBMCyrillic = 855;

  /** cp857 */
  public static final int CDXCharSetIBMTurkish = 857;

  /** cp860 */
  public static final int CDXCharSetMSDOSPortuguese = 860;

  /** cp861 */
  public static final int CDXCharSetMSDOSIcelandic = 861;

  /** DOS-862 */
  public static final int CDXCharSetHebrewOEM = 862;

  /** cp863 */
  public static final int CDXCharSetMSDOSCanadianFrench = 863;

  /** cp864 */
  public static final int CDXCharSetArabicOEM = 864;

  /** cp865 */
  public static final int CDXCharSetMSDOSNordic = 865;

  /** cp866 */
  public static final int CDXCharSetMSDOSRussian = 866;

  /** cp869 */
  public static final int CDXCharSetIBMModernGreek = 869;

  /** windows-874 */
  public static final int CDXCharSetThai = 874;

  /** EBCDIC */
  public static final int CDXCharSetEBCDIC = 875;

  /** shift_jis */
  public static final int CDXCharSetJapanese = 932;

  /** PRC, Singapore, gb2312 */
  public static final int CDXCharSetChineseSimplified = 936;

  /** ks_c_5601-1987 */
  public static final int CDXCharSetKorean = 949;

  /** Taiwan, Hong Kong, big5 */
  public static final int CDXCharSetChineseTraditional = 950;

  /** iso-10646 */
  public static final int CDXCharSetUnicodeISO10646 = 1200;

  /** windows-1250 */
  public static final int CDXCharSetWin31EasternEuropean = 1250;

  /** windows-1251 */
  public static final int CDXCharSetWin31Cyrillic = 1251;

  /** iso-8859-1 */
  public static final int CDXCharSetWin31Latin1 = 1252;

  /** iso-8859-7 */
  public static final int CDXCharSetWin31Greek = 1253;

  /** iso-8859-9 */
  public static final int CDXCharSetWin31Turkish = 1254;

  /** windows-1255 */
  public static final int CDXCharSetHebrew = 1255;

  /** windows-1256 */
  public static final int CDXCharSetArabic = 1256;

  /** windows-1257 */
  public static final int CDXCharSetBaltic = 1257;

  /** windows-1258 */
  public static final int CDXCharSetVietnamese = 1258;

  /** windows-1361 */
  public static final int CDXCharSetKoreanJohab = 1361;

  /** x-mac-roman */
  public static final int CDXCharSetMacRoman = 10000;

  /** x-mac-japanese */
  public static final int CDXCharSetMacJapanese = 10001;

  /** x-mac-tradchinese */
  public static final int CDXCharSetMacTradChinese = 10002;

  /** x-mac-korean */
  public static final int CDXCharSetMacKorean = 10003;

  /** x-mac-arabic */
  public static final int CDXCharSetMacArabic = 10004;

  /** x-mac-hebrew */
  public static final int CDXCharSetMacHebrew = 10005;

  /** x-mac-greek */
  public static final int CDXCharSetMacGreek = 10006;

  /** x-mac-cyrillic */
  public static final int CDXCharSetMacCyrillic = 10007;

  /** x-mac-reserved */
  public static final int CDXCharSetMacReserved = 10008;

  /** x-mac-devanagari */
  public static final int CDXCharSetMacDevanagari = 10009;

  /** x-mac-gurmukhi */
  public static final int CDXCharSetMacGurmukhi = 10010;

  /** x-mac-gujarati */
  public static final int CDXCharSetMacGujarati = 10011;

  /** x-mac-oriya */
  public static final int CDXCharSetMacOriya = 10012;

  /** x-mac-nengali */
  public static final int CDXCharSetMacBengali = 10013;

  /** x-mac-tamil */
  public static final int CDXCharSetMacTamil = 10014;

  /** x-mac-telugu */
  public static final int CDXCharSetMacTelugu = 10015;

  /** x-mac-kannada */
  public static final int CDXCharSetMacKannada = 10016;

  /** x-mac-Malayalam */
  public static final int CDXCharSetMacMalayalam = 10017;

  /** x-mac-sinhalese */
  public static final int CDXCharSetMacSinhalese = 10018;

  /** x-mac-burmese */
  public static final int CDXCharSetMacBurmese = 10019;

  /** x-mac-khmer */
  public static final int CDXCharSetMacKhmer = 10020;

  /** x-mac-thai */
  public static final int CDXCharSetMacThai = 10021;

  /** x-mac-lao */
  public static final int CDXCharSetMacLao = 10022;

  /** x-mac-georgian */
  public static final int CDXCharSetMacGeorgian = 10023;

  /** x-mac-armenian */
  public static final int CDXCharSetMacArmenian = 10024;

  /** x-mac-simpChinese */
  public static final int CDXCharSetMacSimpChinese = 10025;

  /** x-mac-tibetan */
  public static final int CDXCharSetMacTibetan = 10026;

  /** x-mac-mongolian */
  public static final int CDXCharSetMacMongolian = 10027;

  /** x-mac-ethiopic */
  public static final int CDXCharSetMacEthiopic = 10028;

  /** x-mac-ce */
  public static final int CDXCharSetMacCentralEuroRoman = 10029;

  /** x-mac-vietnamese */
  public static final int CDXCharSetMacVietnamese = 10030;

  /** x-mac-extArabic */
  public static final int CDXCharSetMacExtArabic = 10031;

  /** x-mac-uninterpreted */
  public static final int CDXCharSetMacUninterpreted = 10032;

  /** x-mac-icelandic */
  public static final int CDXCharSetMacIcelandic = 10079;

  /** x-mac-turkish */
  public static final int CDXCharSetMacTurkish = 10081;

  // font types

  public static final int CDXFontFace_Plain = 0x0000;
  public static final int CDXFontFace_Bold = 0x0001;
  public static final int CDXFontFace_Italic = 0x0002;
  public static final int CDXFontFace_Underline = 0x0004;
  public static final int CDXFontFace_Outline = 0x0008;
  public static final int CDXFontFace_Shadow = 0x0010;
  public static final int CDXFontFace_Subscript = 0x0020;
  public static final int CDXFontFace_Superscript = 0x0040;
  public static final int CDXFontFace_Formula = 0x0060;

  // arrow head types

  // TODO: check if this are the correct values
  /** Solid */
  public static final int CDXArrowheadType_Solid = 1;

  /** Hollow */
  public static final int CDXArrowheadType_Hollow = 2;

  /** Angle */
  public static final int CDXArrowheadType_Angle = 3;

  // arrow head

  /** Unspecified arrow head */
  public static final int CDXArrowhead_Unspecified = 0;

  /** No arrow head */
  public static final int CDXArrowhead_None = 1;

  /** Full arrow head */
  public static final int CDXArrowhead_Full = 2;

  /** Left arrow head */
  public static final int CDXArrowhead_HalfLeft = 3;

  /** Right arrow head */
  public static final int CDXArrowhead_HalfRight = 4;

  // No go type

  public static final int CDXArrowNoGo_Unspecified = 0;

  public static final int CDXArrowNoGo_None = 1;

  public static final int CDXArrowNoGo_Cross = 2;

  public static final int CDXArrowNoGo_Hash = 3;

  public static byte[] getCdxSignature() {
    return CDX_Signature;
  }

  public static byte[] getChemdrawInterchangeFormat() {
    return CHEMDRAW_INTERCHANGE_FORMAT;
  }
}
