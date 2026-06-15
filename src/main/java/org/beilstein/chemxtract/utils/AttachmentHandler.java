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
package org.beilstein.chemxtract.utils;

import java.util.ArrayList;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;

/**
 * Utility class for resolving multi-center and variable attachment nodes within a {@link
 * CDFragment} before conversion to a CDK structure.
 *
 * <p>ChemDraw encodes two kinds of "star" attachment nodes, both carrying a list of attached atoms
 * (see {@link CDAtom#getAttachedAtoms()}):
 *
 * <ul>
 *   <li>{@link CDNodeType#MultiAttachment} &mdash; a multicenter (haptic) attachment where one
 *       central atom bonds to a whole set of atoms simultaneously, as in metallocene-style
 *       &eta;-coordination (e.g. ferrocene, Zeise's salt). These are resolved in place by
 *       materialising a discrete bond from the central atom to every attached atom and discarding
 *       the synthetic attachment node, yielding a single connected structure whose standard InChI
 *       relies on metal disconnection.
 *   <li>{@link CDNodeType#VariableAttachment} &mdash; position variation, where a substituent
 *       attaches to one of several candidate atoms. These are expanded into one fragment per
 *       candidate atom.
 * </ul>
 */
public final class AttachmentHandler {

  private AttachmentHandler() {
    // private constructor to hide implicit public one
  }

  /**
   * Indicates whether the fragment contains at least one {@link CDNodeType#VariableAttachment}
   * node.
   *
   * @param fragment the fragment to inspect
   * @return {@code true} if a variable (position-variation) attachment node is present
   */
  public static boolean hasVariableAttachment(CDFragment fragment) {
    return fragment.getAtoms().stream()
        .anyMatch(atom -> CDNodeType.VariableAttachment.equals(atom.getNodeType()));
  }

  /**
   * Resolves every {@link CDNodeType#MultiAttachment} node in the fragment in place.
   *
   * <p>For each multicenter node, the bond connecting it to its central atom is replaced by a set
   * of single bonds from that central atom to each atom in the node's attachment list, and the
   * synthetic node is removed.
   *
   * @param fragment the fragment to modify
   */
  public static void resolveMultiAttachments(CDFragment fragment) {
    List<CDAtom> multiNodes =
        fragment.getAtoms().stream()
            .filter(atom -> CDNodeType.MultiAttachment.equals(atom.getNodeType()))
            .toList();
    if (multiNodes.isEmpty()) {
      return;
    }

    List<CDBond> bondsToAdd = new ArrayList<>();
    List<CDBond> bondsToRemove = new ArrayList<>();

    for (CDAtom node : multiNodes) {
      List<CDAtom> attached = node.getAttachedAtoms();
      for (CDBond incident : incidentBonds(fragment, node)) {
        // The synthetic node and its bonds always go; coordination bonds are added only when the
        // node actually enumerates attached atoms.
        bondsToRemove.add(incident);
        if (attached == null || attached.isEmpty()) {
          continue;
        }
        CDAtom central = incident.getBegin().equals(node) ? incident.getEnd() : incident.getBegin();
        for (CDAtom ligand : attached) {
          CDBond bond = new CDBond();
          bond.setBegin(central);
          bond.setEnd(ligand);
          bond.setBondOrder(CDBondOrder.Single);
          bond.setCoordination(true);
          bondsToAdd.add(bond);
        }
      }
    }

    bondsToRemove.forEach(fragment::removeBond);
    bondsToAdd.forEach(fragment::addBond);
    multiNodes.forEach(fragment::removeAtom);
  }

  /**
   * Expands every {@link CDNodeType#VariableAttachment} node into one fragment per candidate atom.
   *
   * <p>A variable attachment node connects a single substituent to one of several candidate atoms
   * (its attachment list). For each combination of candidate choices across all variable nodes a
   * new fragment is produced, in which the substituent bonds directly to the chosen candidate atom
   * and the synthetic node is removed. The Cartesian product is taken when several variable nodes
   * are present.
   *
   * <p>The returned fragments share atom instances with the input fragment but carry independent
   * bond instances, so that conversion of one variant cannot disturb another.
   *
   * @param fragment the fragment to expand
   * @return the list of expanded fragments; the singleton list {@code [fragment]} when no variable
   *     attachment node is present
   */
  public static List<CDFragment> expandVariableAttachments(CDFragment fragment) {
    List<CDAtom> variableNodes =
        fragment.getAtoms().stream()
            .filter(atom -> CDNodeType.VariableAttachment.equals(atom.getNodeType()))
            .toList();
    if (variableNodes.isEmpty()) {
      return List.of(fragment);
    }

    // Collect the substituent attachment for each variable node; skip nodes that carry no usable
    // substituent bond or no candidate atoms.
    List<VariablePoint> points = new ArrayList<>();
    for (CDAtom node : variableNodes) {
      List<CDAtom> candidates = node.getAttachedAtoms();
      List<CDBond> incident = incidentBonds(fragment, node);
      if (candidates == null || candidates.isEmpty() || incident.isEmpty()) {
        continue;
      }
      CDBond substituentBond = incident.get(0);
      CDAtom substituent =
          substituentBond.getBegin().equals(node)
              ? substituentBond.getEnd()
              : substituentBond.getBegin();
      points.add(new VariablePoint(substituent, candidates, substituentBond.getBondOrder()));
    }

    if (points.isEmpty()) {
      return List.of(fragment);
    }

    // Atoms and bonds that are common to every variant: everything except the variable nodes and
    // the bonds incident to them.
    List<CDAtom> baseAtoms =
        fragment.getAtoms().stream().filter(atom -> !variableNodes.contains(atom)).toList();
    List<CDBond> baseBonds =
        fragment.getBonds().stream()
            .filter(
                bond ->
                    !variableNodes.contains(bond.getBegin())
                        && !variableNodes.contains(bond.getEnd()))
            .toList();

    List<List<CDAtom>> selections = new ArrayList<>();
    cartesianProduct(points, 0, new ArrayList<>(), selections);

    List<CDFragment> variants = new ArrayList<>(selections.size());
    for (List<CDAtom> selection : selections) {
      variants.add(buildVariant(fragment, baseAtoms, baseBonds, points, selection));
    }
    return variants;
  }

  /**
   * Builds a single variant fragment in which each variable point's substituent is bonded to the
   * selected candidate atom.
   */
  private static CDFragment buildVariant(
      CDFragment source,
      List<CDAtom> baseAtoms,
      List<CDBond> baseBonds,
      List<VariablePoint> points,
      List<CDAtom> selection) {
    CDFragment variant = new CDFragment();
    variant.setAtoms(baseAtoms);
    variant.setAbsolute(source.isAbsolute());
    variant.setRelative(source.isRelative());
    variant.setRacemic(source.isRacemic());
    variant.setBounds(source.getBounds());

    List<CDBond> bonds = new ArrayList<>(baseBonds.size() + points.size());
    for (CDBond bond : baseBonds) {
      bonds.add(new CDBond(bond));
    }
    for (int i = 0; i < points.size(); i++) {
      VariablePoint point = points.get(i);
      CDBond bond = new CDBond();
      bond.setBegin(point.substituent());
      bond.setEnd(selection.get(i));
      bond.setBondOrder(point.order());
      bonds.add(bond);
    }
    variant.setBonds(bonds);
    return variant;
  }

  /**
   * Recursively enumerates the Cartesian product of candidate choices across all variable points.
   */
  private static void cartesianProduct(
      List<VariablePoint> points, int index, List<CDAtom> current, List<List<CDAtom>> result) {
    if (index == points.size()) {
      result.add(new ArrayList<>(current));
      return;
    }
    for (CDAtom candidate : points.get(index).candidates()) {
      current.add(candidate);
      cartesianProduct(points, index + 1, current, result);
      current.remove(current.size() - 1);
    }
  }

  /** Returns all bonds in the fragment incident to the given atom (by object identity). */
  private static List<CDBond> incidentBonds(CDFragment fragment, CDAtom atom) {
    return fragment.getBonds().stream()
        .filter(bond -> atom.equals(bond.getBegin()) || atom.equals(bond.getEnd()))
        .toList();
  }

  /**
   * Describes a single variable attachment point: the substituent atom, the candidate atoms it may
   * connect to, and the order of the substituent bond.
   */
  private record VariablePoint(CDAtom substituent, List<CDAtom> candidates, CDBondOrder order) {}
}
