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

/** The position of the second line of a double bond. */
public enum CDBondDoublePosition {
  /** Double bond is centered, but was positioned automatically by the program */
  AutoCenter,
  /**
   * Double bond is on the right (viewing from the "begin" atom to the "end" atom), but was
   * positioned automatically by the program
   */
  AutoRight,
  /**
   * Double bond is on the left (viewing from the "begin" atom to the "end" atom), but was
   * positioned automatically by the program
   */
  AutoLeft,
  /** Double bond is centered, and was positioned manually by the user */
  UserCenter,
  /**
   * Double bond is on the right (viewing from the "begin" atom to the "end" atom), and was
   * positioned manually by the user
   */
  UserRight,
  /**
   * Double bond is on the left (viewing from the "begin" atom to the "end" atom), and was
   * positioned manually by the user
   */
  UserLeft;
}
