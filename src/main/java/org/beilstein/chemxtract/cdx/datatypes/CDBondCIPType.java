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
 * This enum represents a bond's absolute stereochemistry according to the CIP system, which is
 * defined in the following references:
 *
 * <ul>
 *   <li>R.S. Cahn, C.K. Ingold, and V. Prelog, Specification of Molecular Chirality, Angew. Chem.,
 *       Int. Ed. Engl. 1966, 5, 385-414 (errata: 1966, 5, 511); Angew. Chem. 1966, 78, 413-447.
 *   <li>V. Prelog and G. Helmchen, Basic principals of the CIP-System and Proposals for a Revision,
 *       Angew Chem. 1982, 94, 614-631; Angew. Chem., Int. Ed. Engl. 1982, 21, 567-583.
 *   <li>P. Mata and A.M. Lobo, The CIP Sequence Rules: Analysis and Proposal for a Revision,
 *       Tetrahedron: Asymmetry.1993, 4, 657-668.
 * </ul>
 */
public enum CDBondCIPType {
  /** Undetermined. */
  Undetermined,
  /** Determined to be symmetric. */
  None,
  /** Asymmetric: (E). */
  E,
  /** Asymmetric: (Z). */
  Z;
}
