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
import java.util.Set;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;

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
 *
 * <p>ChemDraw also encodes position variation without a dedicated node: the substituent is drawn in
 * its own fragment and its bond simply <em>crosses</em> the scaffold bond it may attach to (see
 * {@link CDBond#getCrossingBonds()}). {@link #normalizeVariableAttachmentBonds(List)} rewrites that
 * bond-level encoding into the {@link CDNodeType#VariableAttachment} node encoding above, so a
 * single downstream expansion path handles both.
 */
public final class AttachmentHandler {

  private AttachmentHandler() {
    // private constructor to hide implicit public one
  }

  /**
   * Rewrites bond-encoded position-variation attachments across the given fragments into the {@link
   * CDNodeType#VariableAttachment} node encoding.
   *
   * <p>ChemDraw may draw a position-variation substituent in its own fragment, connecting it with a
   * bond that <em>crosses</em> the scaffold bond it attaches to rather than terminating on an atom
   * (see {@link CDBond#getCrossingBonds()}). Such a substituent is otherwise a disconnected
   * fragment and its attachment is lost. For each crossing bond whose free end (the endpoint
   * nearest, and only bonded within, the crossed bond) floats onto a scaffold bond in another
   * fragment, this method: marks that free end as a {@link CDNodeType#VariableAttachment} node
   * whose candidate atoms are the endpoints of the crossed bond(s), merges the substituent fragment
   * into the scaffold fragment, and drops the now-empty substituent fragment from the returned
   * list.
   *
   * <p>The reciprocal crossing reference carried by the scaffold bond, and re-encountering the same
   * relationship after a merge, are both ignored (the scaffold endpoint is not a degree-one free
   * end, and the candidates then live in the fragment being inspected).
   *
   * @param fragments the fragments collected from a page; mutated in place
   * @return the fragments to extract, with substituent fragments folded into their scaffolds
   */
  public static List<CDFragment> normalizeVariableAttachmentBonds(List<CDFragment> fragments) {
    List<CDFragment> merged = new ArrayList<>();
    for (CDFragment sub : fragments) {
      for (CDBond bond : new ArrayList<>(sub.getBonds())) {
        Set<CDBond> crossed = bond.getCrossingBonds();
        if (crossed == null || crossed.isEmpty()) {
          continue;
        }
        List<CDAtom> candidates = new ArrayList<>();
        for (CDBond c : crossed) {
          addDistinct(candidates, c.getBegin());
          addDistinct(candidates, c.getEnd());
        }
        if (candidates.isEmpty()) {
          continue;
        }
        CDAtom attach = junctionEnd(bond, candidates);
        // Only the substituent side has a free (degree-one) end floating onto the crossed bond; the
        // reciprocal reference on the scaffold bond lands on a ring/chain atom and is skipped.
        if (incidentBonds(sub, attach).size() != 1) {
          continue;
        }
        CDFragment scaffold = fragmentContaining(fragments, candidates);
        if (scaffold == null || scaffold == sub) {
          continue;
        }
        attach.setNodeType(CDNodeType.VariableAttachment);
        attach.setAttachedAtoms(candidates);
        scaffold.addAllAtoms(sub.getAtoms());
        sub.getBonds().forEach(scaffold::addBond);
        merged.add(sub);
      }
    }
    if (merged.isEmpty()) {
      return fragments;
    }
    List<CDFragment> result = new ArrayList<>(fragments);
    result.removeAll(merged);
    return result;
  }

  private static void addDistinct(List<CDAtom> atoms, CDAtom atom) {
    if (atom != null && atoms.stream().noneMatch(a -> a == atom)) {
      atoms.add(atom);
    }
  }

  /**
   * Picks the stub endpoint to turn into the synthetic variable-attachment junction: the one
   * nearest the crossed bond, except when that endpoint is a residue (R-group) node and the other
   * is not. The junction is deleted during expansion and its neighbour is bonded to the chosen
   * candidate atom, so making the residue the junction would discard the very label the legend
   * defines — authors draw the stub from either end, and both spellings have to survive.
   *
   * @param bond the position-variation bond crossing the scaffold
   * @param candidates the endpoints of the crossed bond(s)
   * @return the endpoint to mark as the variable attachment
   */
  private static CDAtom junctionEnd(CDBond bond, List<CDAtom> candidates) {
    CDAtom nearest = nearestEndpoint(bond, candidates);
    CDAtom other = nearest == bond.getBegin() ? bond.getEnd() : bond.getBegin();
    if (other != null && isResidue(nearest) && !isResidue(other)) {
      return other;
    }
    return nearest;
  }

  /** Whether the atom's label is an R-group label ({@code R}, {@code R1}, {@code Ar}, …). */
  private static boolean isResidue(CDAtom atom) {
    if (atom.getText() == null || atom.getText().getText() == null) {
      return false;
    }
    String label = atom.getText().getText().getText();
    return label != null && Definitions.RGROUP_LABEL_PATTERN.matcher(label).find();
  }

  /** Returns the endpoint of {@code bond} closest to the centroid of the candidate atoms. */
  private static CDAtom nearestEndpoint(CDBond bond, List<CDAtom> candidates) {
    CDPoint2D centroid = centroid(candidates);
    if (centroid == null) {
      return bond.getBegin();
    }
    return distance(bond.getBegin(), centroid) <= distance(bond.getEnd(), centroid)
        ? bond.getBegin()
        : bond.getEnd();
  }

  private static CDPoint2D centroid(List<CDAtom> atoms) {
    float sumX = 0f;
    float sumY = 0f;
    int count = 0;
    for (CDAtom atom : atoms) {
      CDPoint2D p = atom.getPosition2D();
      if (p != null) {
        sumX += p.getX();
        sumY += p.getY();
        count++;
      }
    }
    return count == 0 ? null : new CDPoint2D(sumX / count, sumY / count);
  }

  private static double distance(CDAtom atom, CDPoint2D point) {
    CDPoint2D p = atom.getPosition2D();
    if (p == null) {
      return Double.MAX_VALUE;
    }
    double dx = p.getX() - point.getX();
    double dy = p.getY() - point.getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  /** Returns the fragment that contains any of the given atoms (by identity), or {@code null}. */
  private static CDFragment fragmentContaining(List<CDFragment> fragments, List<CDAtom> atoms) {
    for (CDFragment fragment : fragments) {
      for (CDAtom atom : atoms) {
        if (fragment.getAtoms().stream().anyMatch(a -> a == atom)) {
          return fragment;
        }
      }
    }
    return null;
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
      CDBond substituentBond = incident.getFirst();
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
      current.removeLast();
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
