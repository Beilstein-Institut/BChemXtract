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
 * Type of external connection, for atoms of type
 * CDXNodeType.CDXExternalConnectionType.
 */
public enum CDExternalConnectionType{

  /** Nothing is specified about the node's isotopic abundance */
  Unspecified,
  /** The attachment point is indicated by a black diamond at the end of a bond */
  Diamond,
  /** The attachment point is indicated by an asterisk at the end of a bond */
  Star,
  /**
   * The attachment point is indicated by large shaded sphere at the end of a
   * bond
   */
  PolymerBead,
  /**
   * The attachment point is indicated by a wavy line perpendicular to the end
   * of a bond
   */
  Wavy,
  // TODO: javadoc
  Residue;
}
