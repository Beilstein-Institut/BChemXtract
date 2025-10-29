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
 * Specifies that a bond is affected by a reaction. The value stored in this property corresponds to
 * the Reacting Center Status property.
 */
public enum CDBondReactionParticipation {
  /** Bond involvement in reacting center is not specified */
  Unspecified,
  /** Bond is part of reacting center but not made/broken nor order changed */
  ReactionCenter,
  /** Bond is made or broken in reaction */
  MakeOrBreak,
  /** Bond's order changes in reaction */
  ChangeType,
  /** Bond is made or broken, or its order changes in the reaction */
  MakeAndChange,
  /** Bond is not part of reacting center */
  NotReactionCenter,
  /** Bond does not change in course of reaction, but it is part of the reacting center */
  NoChange,
  /**
   * The structure was partially mapped, but the reaction involvement of this bond was not
   * determined
   */
  Unmapped;
}
