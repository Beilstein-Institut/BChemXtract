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
 * The order of a bond object. Dative and Ionic bonds are set with the method
 * {@link org.beilstein.boa.io.cdx.CDBond#setBegin(CDAtom)} as positive end. 
 * Hydrogen bonds are set with the method
 * {@link org.beilstein.boa.io.cdx.CDBond#setBegin(CDAtom)} as the hydrogen. 
 * Three center bonds are set with two Bond objects, each with 
 * {@link org.beilstein.boa.io.cdx.CDBond#getBegin()} as the center atom. 
 */
public enum CDBondOrder{
  /** Single bond */
  Single,
  /** Double bond */
  Double,
  /** Triple bond */
  Triple,
  /** Quadruple bond (used for some inorganic complexes) */
  Quadruple,
  /** Quintuple bond (used for some inorganic complexes) */
  Quintuple,
  /** Hextuple bond (used for some inorganic complexes) */
  Sextuple,
  /** Bond of order one-half */
  Half,
  /** Bond of order one and one-half (an aromatic bond) */
  OneHalf,
  /** Bond of order two and one-half (in benzyne, for example) */
  TwoHalf,
  /** Bond of order three and one-half (used for some inorganic complexes) */
  ThreeHalf,
  /** Bond of order four and one-half (used for some inorganic complexes) */
  FourHalf,
  /** Bond of order five and one-half (used for some inorganic complexes) */
  FiveHalf,
  /** Dative bond, from the "begin" atom to the "end" atom */
  Dative,
  /** Ionic bond */
  Ionic,
  /** Hydrogen bond */
  Hydrogen,
  /** Three-center-bond (in boranes, for example) */
  ThreeCenter,
  /** Single or double bond for substructure queries */
  SingleOrDouble,
  /** Single or aromatic bond for substructure queries */
  SingleOrAromatic,
  /** Double or armoatic bond for substructure queries */
  DoubleOrAromatic,
  /** Any bond for substructure queries */
  Any;
}
