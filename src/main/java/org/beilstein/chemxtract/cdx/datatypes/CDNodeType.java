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
package org.beilstein.chemxtract.cdx.datatypes;

/**
 * The type of a node object.
 */
public enum CDNodeType{
  /**
   * A node of unspecified type. 
   */
  Unspecified,
  /**
   * A node consisting either of one chemical element or of one heavy element and attached
   * hydrogens. 
   */
  Element,
  /**
   * An element list node (e.g. [O,S]) representing a node with alternative elements. 
   */
  ElementList,
  /**
   * A special type of element list node representing a group of elements of a common attribute as a
   * nickname instead of representing each element in a list. 
   */
  ElementListNickname,
  /**
   * A molecular fragment represented by a single symbol, such as Ph. For example, Ph commonly
   * represents a monosubstituted phenyl ring, C6H5. 
   */
  Nickname,
  /**
   * An interpretable label, such as CH(CH<sub>2</sub>OH)<sub>2</sub>, which may include elements,
   * nicknames, or named alternative groups. 
   */
  Fragment,
  /**
   * A labeled node such as C<sub>5</sub>H<sub>10O</sub>, representing any or all of the possible
   * isomers.
   */
  Formula,
  /**
   * A large or infinite set of alternative fragments, which may be defined by example. 
   */
  GenericNickname,
  /**
   * A set of alternative fragments defined by enumeration, such as CH<sub>3</sub>,
   * CH<sub>2</sub>OH, Ph. 
   */
  AnonymousAlternativeGroup,
  /**
   * A set of fragments grouped together and given a name. 
   */
  NamedAlternativeGroup,
  /**
   * The endpoint of a bonding to a set of atoms, such as in p-allyl and p-aryl bonding. 
   */
  MultiAttachment,
  /**
   * A node representing alternative positional isomers. 
   */
  VariableAttachment,
  /**
   * An external connection point node is used in defining fragments for fragment nicknames and
   * named alternative groups. 
   */
  ExternalConnectionPoint,
  /**
   * A node containing a single element or generic nickname, repeated some number of times in a
   * chain, as in [CH2]1-5 (which indicates an alkyl chain of at most 5 carbons). 
   */
  LinkNode;
}
