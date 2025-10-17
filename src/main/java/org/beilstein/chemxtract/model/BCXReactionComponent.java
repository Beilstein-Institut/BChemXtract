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
package org.beilstein.chemxtract.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A component that is part of a reaction.
 * In contrast to the Substance entity, this component does not exist standalone as-is,
 * but within the scope of a reaction.
 */
public class BCXReactionComponent implements Serializable {

  @Serial
  private static final long serialVersionUID = -901699159889250570L;

  /**
   * Inchi and Inchi Key
   */
  private String inchi;

  /**
   * Business key
   */
  private String inchiKey;

  /**
   * The coordinates in the CDX file
   */
  private float cdxTop;
  private float cdxLeft;
  private float cdxBottom;
  private float cdxRight;

  public BCXReactionComponent() {
    super();
  }

  public BCXReactionComponent(String inchiKey) {
    super();
    this.inchiKey = inchiKey;
  }

  public BCXReactionComponent(float cdxTop, float cdxLeft, float cdxBottom, float cdxRight) {
    super();
    this.cdxTop = cdxTop;
    this.cdxLeft = cdxLeft;
    this.cdxBottom = cdxBottom;
    this.cdxRight = cdxRight;
  }

  public String getInchi() {
    return inchi;
  }

  public void setInchi(String inchi) {
    this.inchi = inchi;
  }

  public String getInchiKey() {
    return inchiKey;
  }

  public void setInchiKey(String inchiKey) {
    this.inchiKey = inchiKey;
  }

  public float getCdxTop() {
    return cdxTop;
  }

  public void setCdxTop(float cdxTop) {
    this.cdxTop = cdxTop;
  }

  public float getCdxLeft() {
    return cdxLeft;
  }

  public void setCdxLeft(float cdxLeft) {
    this.cdxLeft = cdxLeft;
  }

  public float getCdxBottom() {
    return cdxBottom;
  }

  public void setCdxBottom(float cdxBottom) {
    this.cdxBottom = cdxBottom;
  }

  public float getCdxRight() {
    return cdxRight;
  }

  public void setCdxRight(float cdxRight) {
    this.cdxRight = cdxRight;
  }

  @Override
  public int hashCode() {
    return Objects.hash(inchiKey);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BCXReactionComponent other = (BCXReactionComponent) obj;
    return Objects.equals(inchiKey, other.inchiKey);
  }

  @Override
  public String toString() {
    return "ReactionComponent [inchiKey=" + inchiKey + ", cdxTop=" + cdxTop + ", cdxLeft=" + cdxLeft + ", cdxBottom=" + cdxBottom +
            ", cdxRight=" + cdxRight + "]";
  }

}
