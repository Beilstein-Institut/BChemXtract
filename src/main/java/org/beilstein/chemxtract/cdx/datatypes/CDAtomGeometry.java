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
 * This enum describes an atom's bond geometry.
 * <p>
 * The number of bonds and implicit and explicit hydrogens must be the number
 * of attachments given in the table for the specified geometry.
 */
public enum CDAtomGeometry{
  /** Unknown */
  Unknown,
  /** 1 ligand */
  OneLigand,
  /** 2 ligands: linear */
  Linear,
  /** 2 ligands: bent */
  Bent,
  /** 3 ligands: trigonal planar */
  TrigonalPlanar,
  /** 3 ligands: trigonal pyramidal */
  TrigonalPyramidal,
  /** 4 ligands: square planar */
  SquarePlanar,
  /** 4 ligands: tetrahedral */
  Tetrahedral,
  /** 5 ligands: trigonal bipyramidal */
  TrigonalBipyramidal,
  /** 5 ligands: square pyramidal */
  SquarePyramidal,
  /** 5 ligands: unspecified */
  FiveLigand,
  /** 6 ligands: octahedral */
  Octahedral,
  /** 6 ligands: unspecified */
  SixLigand,
  /** 7 ligands: unspecified */
  SevenLigand,
  /** 8 ligands: unspecified */
  EightLigand,
  /** 9 ligands: unspecified */
  NineLigand,
  /** 10 ligands: unspecified */
  TenLigand;
}
