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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This object represents a single step in a reaction and encompasses the participating reactants,
 * products or agents above or below the arrow.
 */
public class CDReactionStep extends CDObject {
  /** The objects that act as reactants relative to this step. */
  private List<Object> reactants = new ArrayList<>();

  /** The objects that act as products relative to this step. */
  private List<Object> products = new ArrayList<>();

  /** The plus symbols in this step. */
  private List<Object> plusses = new ArrayList<>();

  /** The arrow that denotes this step. */
  private List<Object> arrows = new ArrayList<>();

  /** The objects that are positioned above the arrow in this step. */
  private List<Object> objectsAboveArrow = new ArrayList<>();

  /** The objects that are positioned below the arrow in this step. */
  private List<Object> objectsBelowArrow = new ArrayList<>();

  private Map<CDAtom, CDAtom> atomMap = new HashMap<>();
  private Map<CDAtom, CDAtom> atomMapManual = new HashMap<>();
  private Map<CDAtom, CDAtom> atomMapAuto = new HashMap<>();

  public Map<CDAtom, CDAtom> getAtomMap() {
    return atomMap;
  }

  public void setAtomMap(Map<CDAtom, CDAtom> atomMap) {
    this.atomMap = atomMap;
  }

  public List<Object> getReactants() {
    return Collections.unmodifiableList(reactants);
  }

  public void setReactants(List<Object> reactants) {
    this.reactants = reactants == null ? new ArrayList<>() : new ArrayList<>(reactants);
  }

  public void addReactant(Object reactant) {
    this.reactants.add(reactant);
  }

  public List<Object> getProducts() {
    return Collections.unmodifiableList(products);
  }

  public void setProducts(List<Object> products) {
    this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
  }

  public void addProduct(Object product) {
    this.products.add(product);
  }

  public List<Object> getPlusses() {
    return Collections.unmodifiableList(plusses);
  }

  public void setPlusses(List<Object> plusses) {
    this.plusses = plusses == null ? new ArrayList<>() : new ArrayList<>(plusses);
  }

  public void addPlus(Object plus) {
    this.plusses.add(plus);
  }

  public List<Object> getArrows() {
    return Collections.unmodifiableList(arrows);
  }

  public void setArrows(List<Object> arrows) {
    this.arrows = arrows == null ? new ArrayList<>() : new ArrayList<>(arrows);
  }

  public void addArrow(Object arrow) {
    this.arrows.add(arrow);
  }

  public List<Object> getObjectsAboveArrow() {
    return Collections.unmodifiableList(objectsAboveArrow);
  }

  public void setObjectsAboveArrow(List<Object> objectsAboveArrow) {
    this.objectsAboveArrow =
        objectsAboveArrow == null ? new ArrayList<>() : new ArrayList<>(objectsAboveArrow);
  }

  public void addObjectAboveArrow(Object objectAboveArrow) {
    this.objectsAboveArrow.add(objectAboveArrow);
  }

  public List<Object> getObjectsBelowArrow() {
    return Collections.unmodifiableList(objectsBelowArrow);
  }

  public void setObjectsBelowArrow(List<Object> objectsBelowArrow) {
    this.objectsBelowArrow =
        objectsBelowArrow == null ? new ArrayList<>() : new ArrayList<>(objectsBelowArrow);
  }

  public void addObjectBelowArrow(Object objectBelowArrow) {
    this.objectsBelowArrow.add(objectBelowArrow);
  }

  public Map<CDAtom, CDAtom> getAtomMapManual() {
    return atomMapManual;
  }

  public void setAtomMapManual(Map<CDAtom, CDAtom> atomMapManual) {
    this.atomMapManual = atomMapManual;
  }

  public Map<CDAtom, CDAtom> getAtomMapAuto() {
    return atomMapAuto;
  }

  public void setAtomMapAuto(Map<CDAtom, CDAtom> atomMapAuto) {
    this.atomMapAuto = atomMapAuto;
  }

  @Override
  public void accept(CDVisitor visitor) {
    visitor.visitReactionStep(this);
    super.accept(visitor);
  }
}
