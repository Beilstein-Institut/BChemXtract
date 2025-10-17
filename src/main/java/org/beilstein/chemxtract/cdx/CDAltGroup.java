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
import java.util.List;

/**
 * A container that groups fragments that represent alternative substituents,
 * also known as R-Group.
 */
public class CDAltGroup extends CDObject {

  /** The captions collection of this alternative group. */
  private List<CDText> captions = new ArrayList<>();
  /** The groups collection of this alternative group. */
  private List<CDGroup> groups = new ArrayList<>();
  /**
   * The bounds of the rectangle drawn around the contents of this alternative
   * group.
   */
  private CDRectangle groupFrame;
  /**
   * The bounds of the rectangle drawn around the title of this alternative
   * group.
   */
  private CDRectangle textFrame;

  private List<CDFragment> fragments = new ArrayList<>();
  private int valence;

  public List<CDGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<CDGroup> groups) {
    this.groups = groups;
  }

  public List<CDFragment> getFragments() {
    return fragments;
  }

  public void setFragments(List<CDFragment> fragments) {
    this.fragments = fragments;
  }

  public List<CDText> getCaptions() {
    return captions;
  }

  public void setCaptions(List<CDText> captions) {
    this.captions = captions;
  }

  public CDRectangle getTextFrame() {
    return textFrame;
  }

  public void setTextFrame(CDRectangle textFrame) {
    this.textFrame = textFrame;
  }

  public CDRectangle getGroupFrame() {
    return groupFrame;
  }

  public void setGroupFrame(CDRectangle groupFrame) {
    this.groupFrame = groupFrame;
  }

  public int getValence() {
    return valence;
  }

  public void setValence(int valence) {
    this.valence = valence;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitNamedAlternativeGroup(this);
    for (CDGroup group : groups) {
      group.accept(visitor);
    }
    for (CDFragment fragment : fragments) {
      fragment.accept(visitor);
    }
    for (CDText text : captions) {
      text.accept(visitor);
    }
    super.accept(visitor);
  }

}
