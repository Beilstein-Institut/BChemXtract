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
package org.beilstein.chemxtract.cdx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.beilstein.chemxtract.cdx.datatypes.*;

/**
 * A bond defines a connection between atoms and corresponds to a chemical bond. Each bond has a
 * begin and end atom.
 */
public class CDBond extends CDObject {
  /** The atom at the first end of the bond. */
  private CDAtom begin;

  /** The atom at the second end of the bond. */
  private CDAtom end;

  /**
   * The offset within the label of the atom on the first end of the bond to which this bond is
   * attached (zero-based).
   */
  private int beginAttach = -1;

  /**
   * The offset within the label of the atom on the second end of the bond to which this bond is
   * attached (zero-based).
   */
  private int endAttach = -1;

  /** The primary display type of the bond. */
  private CDBondDisplay bondDisplay = CDBondDisplay.Solid;

  /** The secondary display type of the bond. */
  private CDBondDisplay bondDisplay2 = CDBondDisplay.Solid;

  /** The positioning type of the bond. */
  private CDBondDoublePosition bondDoublePosition = CDBondDoublePosition.AutoLeft;

  /** The bond order of the bond. */
  private CDBondOrder bondOrder = CDBondOrder.Single;

  /** The bonds that cross the bond. */
  private Set<CDBond> crossingBonds;

  /** The reaction participation of the bond. */
  private CDBondReactionParticipation reactionParticipation =
      CDBondReactionParticipation.Unspecified;

  /** The absolute stereochemistry of the bond. */
  private CDBondCIPType stereochemistry = CDBondCIPType.Undetermined;

  /** The topology of the bond. */
  private CDBondTopology topology = CDBondTopology.Unspecified;

  /** Ordered list of attached bond IDs; plays a role in retaining stereochemistry. */
  private List<CDBond> bondCircularOrdering;

  public CDBondOrder getBondOrder() {
    return bondOrder;
  }

  public void setBondOrder(CDBondOrder bondOrder) {
    this.bondOrder = bondOrder;
  }

  public CDBondDisplay getBondDisplay() {
    return bondDisplay;
  }

  public void setBondDisplay(CDBondDisplay bondDisplay) {
    this.bondDisplay = bondDisplay;
  }

  public CDBondDisplay getBondDisplay2() {
    return bondDisplay2;
  }

  public void setBondDisplay2(CDBondDisplay display2) {
    bondDisplay2 = display2;
  }

  public CDBondDoublePosition getBondDoublePosition() {
    return bondDoublePosition;
  }

  public void setBondDoublePosition(CDBondDoublePosition bondDoublePosition) {
    this.bondDoublePosition = bondDoublePosition;
  }

  public CDAtom getBegin() {
    return begin;
  }

  public void setBegin(CDAtom begin) {
    this.begin = begin;
  }

  public CDAtom getEnd() {
    return end;
  }

  public void setEnd(CDAtom end) {
    this.end = end;
  }

  public CDBondTopology getTopology() {
    return topology;
  }

  public void setTopology(CDBondTopology topology) {
    this.topology = topology;
  }

  public CDBondReactionParticipation getReactionParticipation() {
    return reactionParticipation;
  }

  public void setReactionParticipation(CDBondReactionParticipation reactionParticipation) {
    this.reactionParticipation = reactionParticipation;
  }

  public int getBeginAttach() {
    return beginAttach;
  }

  public void setBeginAttach(int beginAttach) {
    this.beginAttach = beginAttach;
  }

  public int getEndAttach() {
    return endAttach;
  }

  public void setEndAttach(int endAttach) {
    this.endAttach = endAttach;
  }

  public CDBondCIPType getStereochemistry() {
    return stereochemistry;
  }

  public void setStereochemistry(CDBondCIPType stereochemistry) {
    this.stereochemistry = stereochemistry;
  }

  public List<CDBond> getBondCircularOrdering() {
    return bondCircularOrdering;
  }

  public void setBondCircularOrdering(List<CDBond> bondCircularOrdering) {
    this.bondCircularOrdering = bondCircularOrdering;
  }

  public Set<CDBond> getCrossingBonds() {
    return crossingBonds;
  }

  public void setCrossingBonds(Set<CDBond> crossingBonds) {
    this.crossingBonds = crossingBonds;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitBond(this);
    super.accept(visitor);
  }

  public CDBond() {
    // empty constructor
  }

  public CDBond(CDBond other) {
    if (other == null) {
      throw new IllegalArgumentException("The provided CDBond object cannot be null.");
    }

    this.begin = other.begin; // Note: Reference copied; consider deep copy if necessary
    this.end = other.end; // Note: Reference copied; consider deep copy if necessary
    this.beginAttach = other.beginAttach;
    this.endAttach = other.endAttach;
    this.bondDisplay = other.bondDisplay;
    this.bondDisplay2 = other.bondDisplay2;
    this.bondDoublePosition = other.bondDoublePosition;
    this.bondOrder = other.bondOrder;
    this.crossingBonds = other.crossingBonds != null ? new HashSet<>(other.crossingBonds) : null;
    this.reactionParticipation = other.reactionParticipation;
    this.stereochemistry = other.stereochemistry;
    this.topology = other.topology;
    this.bondCircularOrdering =
        other.bondCircularOrdering != null ? new ArrayList<>(other.bondCircularOrdering) : null;
  }
}
