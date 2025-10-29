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
import java.util.ArrayList;
import java.util.List;

/** A reaction with RInchi that could be extracted from a CDX file. */
public class BCXReaction implements Serializable {

  @Serial private static final long serialVersionUID = -291128317668966826L;

  /** RInchi and RInchi Key(s) */
  private String rinchi;

  /** Business key */
  private String rinchiKey;

  private String shortRinchiKey;
  private String longRinchiKey;
  private String webRinchiKey;

  /** Canonical reaction smiles */
  private String reactionSmiles;

  /** Aux info collected during RInchi generation */
  private String auxInfo;

  /** The components, i.e. substances that are part of the reaction */
  private List<BCXReactionComponent> products = new ArrayList<BCXReactionComponent>();

  private List<BCXReactionComponent> reactants = new ArrayList<BCXReactionComponent>();
  private List<BCXReactionComponent> agents = new ArrayList<BCXReactionComponent>();

  public BCXReaction() {
    super();
  }

  public BCXReaction(String rinchiKey) {
    super();
    this.rinchiKey = rinchiKey;
  }

  public String getRinchi() {
    return rinchi;
  }

  public void setRinchi(String rinchi) {
    this.rinchi = rinchi;
  }

  public String getRinchiKey() {
    return rinchiKey;
  }

  public void setRinchiKey(String rinchiKey) {
    this.rinchiKey = rinchiKey;
  }

  public String getShortRinchiKey() {
    return shortRinchiKey;
  }

  public void setShortRinchiKey(String shortRinchiKey) {
    this.shortRinchiKey = shortRinchiKey;
  }

  public String getLongRinchiKey() {
    return longRinchiKey;
  }

  public void setLongRinchiKey(String longRinchiKey) {
    this.longRinchiKey = longRinchiKey;
  }

  public String getWebRinchiKey() {
    return webRinchiKey;
  }

  public void setWebRinchiKey(String webRinchiKey) {
    this.webRinchiKey = webRinchiKey;
  }

  public String getReactionSmiles() {
    return reactionSmiles;
  }

  public void setReactionSmiles(String reactionSmiles) {
    this.reactionSmiles = reactionSmiles;
  }

  public String getAuxInfo() {
    return auxInfo;
  }

  public void setAuxInfo(String auxInfo) {
    this.auxInfo = auxInfo;
  }

  public List<BCXReactionComponent> getAgents() {
    return agents;
  }

  public void addAgent(BCXReactionComponent agent) {
    agents.add(agent);
  }

  public List<BCXReactionComponent> getReactants() {
    return reactants;
  }

  public void addReactant(BCXReactionComponent reactant) {
    reactants.add(reactant);
  }

  public List<BCXReactionComponent> getProducts() {
    return products;
  }

  public void addProduct(BCXReactionComponent product) {
    products.add(product);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((webRinchiKey == null) ? 0 : webRinchiKey.hashCode());
    return result;
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
    BCXReaction other = (BCXReaction) obj;
    if (webRinchiKey == null) {
      if (other.webRinchiKey != null) {
        return false;
      }
    } else if (!webRinchiKey.equals(other.webRinchiKey)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BCXReaction [rinchi="
        + rinchi
        + ", rinchiKey="
        + rinchiKey
        + ", shortRinchiKey="
        + shortRinchiKey
        + ", longRinchiKey="
        + longRinchiKey
        + ", webRinchiKey="
        + webRinchiKey
        + ", reactionSmiles="
        + reactionSmiles
        + "]";
  }
}
