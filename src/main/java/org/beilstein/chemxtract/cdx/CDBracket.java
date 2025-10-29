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
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerFlipType;
import org.beilstein.chemxtract.cdx.datatypes.CDPolymerRepeatPattern;

/**
 * A bracket is a group of objects that may be repeated several times, like in polymers. External
 * connections to this group are contained as bracket attachments.
 */
public class CDBracket extends CDObject {
  /** The chemical usage of the bracket. */
  private CDBracketUsage bracketUsage;

  /** The component order for brackets. */
  private int componentOrder;

  /** The polymer flip type of the bracket. */
  private CDPolymerFlipType polymerFlipType;

  /** the polymer repeat pattern of the bracket. */
  private CDPolymerRepeatPattern polymerRepeatPattern;

  /** The repeat count for brackets. */
  private double repeatCount;

  /** The structural repeating unit label for brackets. */
  private String sruLabel;

  private List<CDBracket> brackets = new ArrayList<>();
  private List<CDBracketAttachment> bracketAttachments = new ArrayList<>();
  private List<Object> bracketedObjects = new ArrayList<>();

  public List<CDBracket> getBrackets() {
    return brackets;
  }

  public void setBrackets(List<CDBracket> brackets) {
    this.brackets = brackets;
  }

  public List<CDBracketAttachment> getBracketAttachments() {
    return bracketAttachments;
  }

  public void setBracketAttachments(List<CDBracketAttachment> bracketAttachments) {
    this.bracketAttachments = bracketAttachments;
  }

  public CDBracketUsage getBracketUsage() {
    return bracketUsage;
  }

  public void setBracketUsage(CDBracketUsage bracketUsage) {
    this.bracketUsage = bracketUsage;
  }

  public CDPolymerRepeatPattern getPolymerRepeatPattern() {
    return polymerRepeatPattern;
  }

  public void setPolymerRepeatPattern(CDPolymerRepeatPattern polymerRepeatPattern) {
    this.polymerRepeatPattern = polymerRepeatPattern;
  }

  public CDPolymerFlipType getPolymerFlipType() {
    return polymerFlipType;
  }

  public void setPolymerFlipType(CDPolymerFlipType polymerFlipType) {
    this.polymerFlipType = polymerFlipType;
  }

  public List<Object> getBracketedObjects() {
    return bracketedObjects;
  }

  public void setBracketedObjects(List<Object> bracketedObjects) {
    this.bracketedObjects = bracketedObjects;
  }

  public double getRepeatCount() {
    return repeatCount;
  }

  public void setRepeatCount(double repeatCount) {
    this.repeatCount = repeatCount;
  }

  public int getComponentOrder() {
    return componentOrder;
  }

  public void setComponentOrder(int componentOrder) {
    this.componentOrder = componentOrder;
  }

  public String getSRULabel() {
    return sruLabel;
  }

  public void setSRULabel(String label) {
    sruLabel = label;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitBracketedGroup(this);
    for (CDBracket bracket : brackets) {
      bracket.accept(visitor);
    }
    super.accept(visitor);
  }
}
