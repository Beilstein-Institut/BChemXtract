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

import java.awt.geom.Line2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentHandler.class);

  /**
   * Upper bound on the number of position-variation isomers enumerated for a single fragment. The
   * candidate choices of all variable attachment nodes in a fragment multiply, so a drawing with a
   * handful of nodes can span billions of combinations - a generic depiction rather than an
   * enumerable set of substances. Such a fragment is skipped instead of exhausting the heap.
   */
  private static final long MAX_VARIANTS = 1000L;

  /**
   * How far a position-variation bond may stay clear of the bond it attaches to, as a fraction of
   * that bond's length. ChemDraw's crossing-bond list is a drawing relationship and also names
   * bonds the crossing bond never reaches, whose endpoints must not become attachment candidates.
   * Across the reference corpus every real attachment closes to within 0.4 bond lengths of its
   * crossed bond while unrelated pairs stay beyond 0.8.
   */
  private static final double MAX_ATTACHMENT_GAP = 0.5;

  /**
   * How many of a fragment's bonds may carry a crossing reference before it is read as a second
   * drawing laid over the first rather than a position-variation substituent. A substituent is
   * attached by the one bond it is drawn across, or by the handful that mark several junctions; two
   * drawings sharing a page area cross each other along their whole length. Across the reference
   * corpus every merged substituent carries at most three such bonds (145 of 160 carry exactly
   * one), while the overlapping drawings of {@code vol20Test/m28096433-4.cdx} carry 34 and 49 - an
   * empty valley between.
   */
  private static final int MAX_SUBSTITUENT_CROSSING_BONDS = 3;

  /**
   * Largest ring a position-variation stub may be read as pointing into. Rings this size and below
   * are the ones drawn as a closed figure a bond can come to rest inside; beyond it a "ring" is a
   * macrocycle whose interior is page background the author draws through, not a position set.
   */
  private static final int MAX_RING_SIZE = 8;

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
   * <p>A crossing reference only describes an attachment when the two bonds actually meet on the
   * page (see {@link #reaches(CDBond, CDBond)}); bonds merely named by each other, as overlapping
   * drawings are, contribute no candidates. A fragment that crosses its surroundings along its
   * whole length is a second drawing rather than a substituent and is left where it was drawn (see
   * {@link #MAX_SUBSTITUENT_CROSSING_BONDS}). A substituent moves to one scaffold and moves once,
   * however many of its bonds cross it, so that several crossings mark several junctions rather
   * than duplicating the substituent. For the same reason the candidates of a junction are limited
   * to the scaffold the substituent moves into: a crossed bond in a third fragment names atoms that
   * no enumerated variant contains.
   *
   * <p>A junction whose stub comes to rest inside a ring is widened from the atoms its crossings
   * name to every free position on that ring, which is what the drawing means (see {@link
   * #widenToRing(CDFragment, CDBond, List)}).
   *
   * @param fragments the fragments collected from a page; mutated in place
   * @return the fragments to extract, with substituent fragments folded into their scaffolds
   */
  public static List<CDFragment> normalizeVariableAttachmentBonds(List<CDFragment> fragments) {
    List<CDFragment> merged = new ArrayList<>();
    // Counted before the first merge: a scaffold inherits the crossing bonds of everything folded
    // into it, so counting as we go would judge a fragment by its neighbours' drawing rather than
    // its own, and the outcome would depend on the order the fragments happen to arrive in.
    Map<CDFragment, Long> crossingBonds = new IdentityHashMap<>();
    for (CDFragment fragment : fragments) {
      crossingBonds.put(fragment, crossingBondCount(fragment));
    }
    for (CDFragment sub : fragments) {
      // A fragment that crosses its surroundings along its whole length is a second drawing
      // sharing the page area, not a substituent attaching to one: reading it as position
      // variation would make a junction of each of its free ends.
      if (crossingBonds.get(sub) > MAX_SUBSTITUENT_CROSSING_BONDS) {
        LOGGER.info(
            "Not a position-variation substituent: {} of the fragment's {} bonds cross another"
                + " fragment; leaving the overlapping drawings unmerged.",
            crossingBonds.get(sub),
            sub.getBonds().size());
        continue;
      }
      // The substituent's atoms and bonds move to its scaffold once, however many of its bonds
      // carry a crossing reference; a second copy would duplicate the whole substituent.
      CDFragment target = null;
      for (CDBond bond : new ArrayList<>(sub.getBonds())) {
        Set<CDBond> crossed = bond.getCrossingBonds();
        if (crossed == null || crossed.isEmpty()) {
          continue;
        }
        List<CDAtom> candidates = new ArrayList<>();
        for (CDBond c : crossed) {
          if (!reaches(bond, c)) {
            continue;
          }
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
        // A scaffold that is itself a substituent has already moved elsewhere, and merging into it
        // would discard these atoms with it; that chained drawing keeps its bond unresolved.
        if (scaffold == null || scaffold == sub || merged.contains(scaffold)) {
          continue;
        }
        if (target != null && target != scaffold) {
          continue;
        }
        // One stub may cross bonds in several fragments, but the substituent moves into one of
        // them. Candidates left behind in another fragment are absent from every enumerated
        // variant, so bonding to them would leave the variant's bond dangling.
        List<CDAtom> scoped = atomsIn(scaffold, candidates);
        if (scoped.isEmpty()) {
          continue;
        }
        scoped = widenToRing(scaffold, bond, scoped);
        attach.setNodeType(CDNodeType.VariableAttachment);
        attach.setAttachedAtoms(scoped);
        if (target == null) {
          target = scaffold;
          scaffold.addAllAtoms(sub.getAtoms());
          sub.getBonds().forEach(scaffold::addBond);
          merged.add(sub);
        }
      }
    }
    if (merged.isEmpty()) {
      return fragments;
    }
    List<CDFragment> result = new ArrayList<>(fragments);
    result.removeAll(merged);
    return result;
  }

  /**
   * Widens a junction's candidates from the endpoints of the bonds it crosses to every free carbon
   * of the ring it points into.
   *
   * <p>A crossing names only the two atoms of the one bond the stub happens to cut, and a straight
   * bond crosses a convex ring exactly twice, so the crossing encoding can never name more than
   * three atoms of a six-ring however the author draws it. The drawing says more than that: a
   * substituent bond that runs in from outside and comes to rest <em>inside</em> the ring is the
   * standard way to say "any free position on this ring" (ACD's own Markush tutorial draws exactly
   * this, a hydroxyl stopping inside the benzo ring of an indene, and reads it as all four free
   * carbons).
   *
   * <p>Two conditions keep this narrow. One end of the stub must carry a label and the other must
   * not: that is what makes it a substituent with an identity to place, and it leaves the
   * unlabelled annotation marks that cross a ring - cut marks, brackets, leader lines - naming only
   * what they touch. And the unlabelled end must lie inside the ring's own polygon: a bond drawn
   * straight through a ring and out the other side is passing over it, not aimed at it.
   *
   * <p>The widened set is a union, never a replacement. A crossing may name a ring atom that
   * already carries a substituent, and that atom is still what the author drew a line across.
   *
   * @param scaffold the fragment holding the candidates
   * @param bond the position-variation bond crossing the scaffold
   * @param candidates the candidates named by the crossings, scoped to the scaffold
   * @return the candidates, widened to the ring when the drawing asks for it
   */
  private static List<CDAtom> widenToRing(
      CDFragment scaffold, CDBond bond, List<CDAtom> candidates) {
    CDAtom inner = unlabelledEnd(bond);
    if (inner == null || inner.getPosition2D() == null) {
      return candidates;
    }
    List<CDAtom> ring = ringEnclosing(scaffold, candidates, inner.getPosition2D());
    if (ring == null) {
      return candidates;
    }
    List<CDAtom> widened = new ArrayList<>(candidates);
    for (CDAtom atom : ring) {
      if (isSubstitutableCarbon(scaffold, atom)) {
        addDistinct(widened, atom);
      }
    }
    return widened;
  }

  /**
   * Returns the end of the bond that carries no label, when exactly one of the two does, and {@code
   * null} otherwise. Two labelled ends are a bond between two named groups rather than a stub, and
   * two plain ends carry no substituent to place.
   */
  private static CDAtom unlabelledEnd(CDBond bond) {
    boolean beginLabelled = !label(bond.getBegin()).isEmpty();
    boolean endLabelled = !label(bond.getEnd()).isEmpty();
    if (beginLabelled == endLabelled) {
      return null;
    }
    return beginLabelled ? bond.getEnd() : bond.getBegin();
  }

  /** The atom's text label, or the empty string when it carries none. */
  private static String label(CDAtom atom) {
    if (atom == null || atom.getText() == null || atom.getText().getText() == null) {
      return "";
    }
    String text = atom.getText().getText().getText();
    return text == null ? "" : text.trim();
  }

  /**
   * Whether the ring atom can take another substituent: an unlabelled carbon still holding an
   * implicit hydrogen. Counting bonds is what tells them apart before conversion - a ring atom with
   * a third bond is either fused or already substituted, and either way the author did not leave a
   * position there.
   */
  private static boolean isSubstitutableCarbon(CDFragment fragment, CDAtom atom) {
    return atom.getElementNumber() == 6
        && label(atom).isEmpty()
        && incidentBonds(fragment, atom).size() <= 2;
  }

  /**
   * Returns the smallest ring through any bond between two candidates that encloses the given
   * point, or {@code null} when no such ring exists. Bonds are walked in the fragment's own order
   * so that a junction whose candidates span more than one ring resolves the same way every run.
   */
  private static List<CDAtom> ringEnclosing(
      CDFragment fragment, List<CDAtom> candidates, CDPoint2D point) {
    for (CDBond bond : fragment.getBonds()) {
      if (!containsIdentical(candidates, bond.getBegin())
          || !containsIdentical(candidates, bond.getEnd())) {
        continue;
      }
      List<CDAtom> ring = smallestRingThrough(fragment, bond);
      if (ring != null
          && ring.stream().allMatch(atom -> atom.getPosition2D() != null)
          && encloses(ring, point)) {
        return ring;
      }
    }
    return null;
  }

  /**
   * Returns the atoms of the smallest ring containing the given bond, or {@code null} when the bond
   * is acyclic or its ring is larger than {@link #MAX_RING_SIZE}. A breadth-first walk from one
   * endpoint back to the other, with the bond itself removed.
   */
  private static List<CDAtom> smallestRingThrough(CDFragment fragment, CDBond bond) {
    CDAtom from = bond.getBegin();
    CDAtom to = bond.getEnd();
    if (from == null || to == null) {
      return null;
    }
    Map<CDAtom, List<CDAtom>> neighbours = new IdentityHashMap<>();
    for (CDBond other : fragment.getBonds()) {
      if (other.getBegin() == null || other.getEnd() == null) {
        continue;
      }
      neighbours.computeIfAbsent(other.getBegin(), key -> new ArrayList<>()).add(other.getEnd());
      neighbours.computeIfAbsent(other.getEnd(), key -> new ArrayList<>()).add(other.getBegin());
    }
    Deque<List<CDAtom>> paths = new ArrayDeque<>();
    paths.add(new ArrayList<>(List.of(from)));
    List<CDAtom> shortest = null;
    while (!paths.isEmpty()) {
      List<CDAtom> path = paths.poll();
      if (shortest != null && path.size() >= shortest.size()) {
        continue;
      }
      CDAtom last = path.get(path.size() - 1);
      for (CDAtom next : neighbours.getOrDefault(last, List.of())) {
        if (last == from && next == to && path.size() == 1) {
          // The bond under test itself; a ring has to come back the long way round.
          continue;
        }
        if (next == to && path.size() >= 2) {
          if (shortest == null || path.size() + 1 < shortest.size()) {
            shortest = new ArrayList<>(path);
            shortest.add(next);
          }
          continue;
        }
        if (containsIdentical(path, next) || path.size() >= MAX_RING_SIZE - 1) {
          continue;
        }
        List<CDAtom> extended = new ArrayList<>(path);
        extended.add(next);
        paths.add(extended);
      }
    }
    return shortest;
  }

  /**
   * Whether the point lies inside the polygon the ring atoms draw. The atoms come off the ring walk
   * in connection order, which is already the polygon's outline, but they are re-sorted by angle
   * about the centroid so that the test does not depend on the walk's direction or start.
   */
  private static boolean encloses(List<CDAtom> ring, CDPoint2D point) {
    CDPoint2D centre = centroid(ring);
    if (centre == null) {
      return false;
    }
    List<CDPoint2D> outline =
        ring.stream()
            .map(CDAtom::getPosition2D)
            .sorted(
                Comparator.comparingDouble(
                    p -> Math.atan2(p.getY() - centre.getY(), p.getX() - centre.getX())))
            .toList();
    boolean inside = false;
    for (int i = 0, j = outline.size() - 1; i < outline.size(); j = i++) {
      CDPoint2D a = outline.get(i);
      CDPoint2D b = outline.get(j);
      if ((a.getY() > point.getY()) != (b.getY() > point.getY())
          && point.getX()
              < (b.getX() - a.getX()) * (point.getY() - a.getY()) / (b.getY() - a.getY())
                  + a.getX()) {
        inside = !inside;
      }
    }
    return inside;
  }

  /** Whether the list holds the given atom by object identity. */
  private static boolean containsIdentical(List<CDAtom> atoms, CDAtom atom) {
    return atom != null && atoms.stream().anyMatch(a -> a == atom);
  }

  /**
   * Whether the given bond reaches the bond it is said to cross: the two segments meet, or their
   * gap stays within {@link #MAX_ATTACHMENT_GAP} of the crossed bond's length. A pair without
   * coordinates is accepted, there being nothing to judge it by.
   *
   * @param bond the bond carrying the crossing reference
   * @param crossed the bond it names
   * @return {@code true} if the two are close enough to describe an attachment
   */
  private static boolean reaches(CDBond bond, CDBond crossed) {
    CDPoint2D b1 = position(bond.getBegin());
    CDPoint2D b2 = position(bond.getEnd());
    CDPoint2D c1 = position(crossed.getBegin());
    CDPoint2D c2 = position(crossed.getEnd());
    if (b1 == null || b2 == null || c1 == null || c2 == null) {
      return true;
    }
    Line2D bondLine = new Line2D.Float(b1.getX(), b1.getY(), b2.getX(), b2.getY());
    Line2D crossedLine = new Line2D.Float(c1.getX(), c1.getY(), c2.getX(), c2.getY());
    if (bondLine.intersectsLine(crossedLine)) {
      return true;
    }
    double gap =
        Math.min(
            Math.min(
                crossedLine.ptSegDist(b1.getX(), b1.getY()),
                crossedLine.ptSegDist(b2.getX(), b2.getY())),
            Math.min(
                bondLine.ptSegDist(c1.getX(), c1.getY()),
                bondLine.ptSegDist(c2.getX(), c2.getY())));
    return gap <= MAX_ATTACHMENT_GAP * Math.hypot(c2.getX() - c1.getX(), c2.getY() - c1.getY());
  }

  private static CDPoint2D position(CDAtom atom) {
    return atom == null ? null : atom.getPosition2D();
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
    return Definitions.RGROUP_LABEL_PATTERN.matcher(label(atom)).find();
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

  /** Returns those of the given atoms that the fragment contains (by identity). */
  private static List<CDAtom> atomsIn(CDFragment fragment, List<CDAtom> atoms) {
    return atoms.stream()
        .filter(atom -> fragment.getAtoms().stream().anyMatch(a -> a == atom))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** Returns how many of the fragment's bonds carry at least one crossing reference. */
  private static long crossingBondCount(CDFragment fragment) {
    return fragment.getBonds().stream()
        .filter(bond -> bond.getCrossingBonds() != null && !bond.getCrossingBonds().isEmpty())
        .count();
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
   *     attachment node is present, and an empty list when the combinations exceed {@link
   *     #MAX_VARIANTS}
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

    long combinations = 1L;
    for (VariablePoint point : points) {
      combinations *= point.candidates().size();
      if (combinations > MAX_VARIANTS) {
        LOGGER.warn(
            "Skipping fragment: {} variable attachment nodes enumerate more than {} isomers.",
            points.size(),
            MAX_VARIANTS);
        return List.of();
      }
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
