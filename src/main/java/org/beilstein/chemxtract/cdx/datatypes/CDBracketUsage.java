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
 * The chemical meaning of the bracket.
 */
public enum CDBracketUsage{
  /** Unspecified bracket usage. */
  Unspecified,
  /** Brackets enclose any polymer. */
  Anypolymer,
  /**
   * Brackets enclose an individual component of an ordered or unordered
   * mixture.
   */
  Component,
  /** Brackets enclose one of several repeating units that co-polymerize. */
  Copolymer,
  /**
   * Brackets enclose one of several repeating units that co-polymerize in an
   * alternating fashion.
   */
  CopolymerAlternating,
  /**
   * Brackets enclose one of several repeating units that co-polymerize in a
   * block fashion.
   */
  CopolymerBlock,
  /**
   * Brackets enclose one of several repeating units that co-polymerize in a
   * random fashion.
   */
  CopolymerRandom,
  /**
   * Brackets enclose a cross-linking repeating unit in a source-based
   * representation.
   */
  Crosslink,
  /** Brackets enclose a generic polymer. */
  Generic,
  /** Brackets enclose a graft repeating unit in a source-based representation. */
  Graft,
  /**
   * Brackets enclose a source-based monomeric unit that is known not to
   * self-polymerize.
   */
  Mer,
  /**
   * Brackets enclose a collection of substances that comprise an ordered
   * mixture (also called a formulation).
   */
  MixtureOrdered,
  /**
   * Brackets enclose a collection of substances that comprise an unordered
   * mixture.
   */
  MixtureUnordered,
  /**
   * Brackets enclose a modified repeating unit in a source-based
   * representation.
   */
  Modification,
  /** Brackets enclose a source-based monomeric unit. */
  Monomer,
  /**
   * Brackets enclose a structure or fragment that is repeated some number of
   * times.
   */
  MultipleGroup,
  /**
   * Brackets enclose a Structural Repeating Unit in a structure-based
   * representation.
   */
  SRU,
  /** (unused) */
  Unused1,
  /** (unused) */
  Unused2;
}
