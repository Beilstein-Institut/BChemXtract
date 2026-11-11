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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point2d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.converter.FragmentConverter;
import org.beilstein.chemxtract.visitor.TextVisitor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the compounds of a {@link TextVisitor.RingTable}: a scaffold whose rings are lettered in
 * the drawing ({@code A}, {@code B}) and each carry one position-variation group, and a legend that
 * gives per compound where each ring's group sits ({@code 3-triazolyl}) or, for a one-atom group,
 * where its copies sit ({@code 3,4-diOMe}). Positions count from the ring atom the scaffold joins,
 * position 1. A table that does not fit the drawing in every respect builds nothing.
 */
final class RingLetterTable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RingLetterTable.class);

  /**
   * A value of a ring column: positions, an optional multiplier, the group's name. A multiplier
   * stands before a capital or a bracket ({@code diOMe}), so the {@code tri} of {@code triazolyl}
   * is part of the name.
   */
  private static final Pattern VALUE =
      Pattern.compile("(\\d(?:,\\d)*)-(?:(di|tri)(?=[A-Z(]))?(.+)");

  /**
   * A ring letter drawn inside a ring.
   *
   * @param letter the letter
   * @param at where it is drawn
   */
  record Letter(String letter, CDPoint2D at) {}

  /**
   * A lettered ring of the scaffold and its position-variation group.
   *
   * @param node the variable attachment node
   * @param group the group's atom bonded to the node
   * @param positions the ring atoms by position, index 0 being position 1
   */
  private record LetteredRing(CDAtom node, CDAtom group, List<CDAtom> positions) {}

  private RingLetterTable() {}

  /**
   * The ring letters drawn on a page: fragments of one atom labelled with one capital letter.
   *
   * @param fragments the page's fragments
   * @return the letters
   */
  static List<Letter> letters(List<CDFragment> fragments) {
    List<Letter> letters = new ArrayList<>();
    for (CDFragment fragment : fragments) {
      if (fragment.getAtoms().size() == 1) {
        CDAtom atom = fragment.getAtoms().get(0);
        String label = label(atom);
        if (label.matches("[A-Z]") && atom.getPosition2D() != null) {
          letters.add(new Letter(label, atom.getPosition2D()));
        }
      }
    }
    return letters;
  }

  /**
   * Builds one structure per compound line of the table.
   *
   * @param fragment the scaffold, its position-variation groups folded in
   * @param table the ring table
   * @param letters the ring letters on the page
   * @param converter converter for the built fragments
   * @return the structures in line order, or {@code null} if the table does not fit the scaffold
   */
  static List<IAtomContainer> build(
      CDFragment fragment,
      TextVisitor.RingTable table,
      List<Letter> letters,
      FragmentConverter converter) {
    List<CDAtom> nodes =
        fragment.getAtoms().stream()
            .filter(atom -> CDNodeType.VariableAttachment.equals(atom.getNodeType()))
            .toList();
    Map<String, LetteredRing> rings = new HashMap<>();
    for (String letter : table.rings()) {
      LetteredRing ring = letteredRing(fragment, nodes, letter, letters);
      if (ring == null) {
        return null;
      }
      rings.put(letter, ring);
    }
    if (rings.size() != nodes.size()) {
      return null; // a variable group the table does not place
    }
    for (int i = 0; i < table.rings().size(); i++) {
      if (!namesOneGroup(table.rows(), i, label(rings.get(table.rings().get(i)).group()))) {
        LOGGER.debug("Ring table column {} names more than the drawn group.", i);
        return null;
      }
    }
    List<IAtomContainer> structures = new ArrayList<>();
    for (TextVisitor.RingRow row : table.rows()) {
      Map<CDAtom, CDAtom> targets = new IdentityHashMap<>();
      Map<LetteredRing, List<CDAtom>> copies = new IdentityHashMap<>();
      for (int i = 0; i < table.rings().size(); i++) {
        LetteredRing ring = rings.get(table.rings().get(i));
        List<CDAtom> at = placement(ring, row.values().get(i));
        if (at == null) {
          LOGGER.debug("Ring table value {} does not fit ring {}.", row.values().get(i), ring);
          return null;
        }
        targets.put(ring.node(), at.get(0));
        copies.put(ring, at.subList(1, at.size()));
      }
      try {
        IAtomContainer structure = converter.convert(variant(fragment, nodes, targets));
        for (Map.Entry<LetteredRing, List<CDAtom>> copy : copies.entrySet()) {
          for (CDAtom extra : copy.getValue()) {
            if (!copyGroup(
                structure, copy.getKey().positions(), targets.get(copy.getKey().node()), extra)) {
              return null;
            }
          }
        }
        structures.add(structure);
      } catch (CDKException | RuntimeException e) {
        LOGGER.debug("Ring table entry {} does not convert.", row.entry(), e);
        return null;
      }
    }
    return structures;
  }

  /**
   * Whether a column's values all name the one group drawn on its ring: each names the drawn
   * group's label ({@code OMe} of {@code 3,4-diOMe}), or every value names the same group ({@code
   * triazolyl} of {@code 3-triazolyl} and {@code 4-triazolyl}). A column that names two different
   * groups ({@code 3-triazolyl}, {@code 3-imidazolyl}) says which group sits on the ring, which the
   * drawing does not show.
   *
   * @param rows the table's compound lines
   * @param column the column's index
   * @param drawnLabel the label of the drawn group's atom bonded to the ring, empty if unlabelled
   * @return whether the values name one group
   */
  static boolean namesOneGroup(List<TextVisitor.RingRow> rows, int column, String drawnLabel) {
    Set<String> names = new HashSet<>();
    for (TextVisitor.RingRow row : rows) {
      Matcher matcher = VALUE.matcher(row.values().get(column).strip());
      if (!matcher.matches()) {
        return false;
      }
      String name = matcher.group(3).strip().toLowerCase(Locale.ROOT);
      if (!name.equals(drawnLabel.toLowerCase(Locale.ROOT))) {
        names.add(name);
      }
    }
    return names.size() <= 1;
  }

  /** The ring the letter is drawn in, if one of the variable groups attaches to it. */
  private static LetteredRing letteredRing(
      CDFragment fragment, List<CDAtom> nodes, String letter, List<Letter> letters) {
    LetteredRing found = null;
    for (Letter drawn : letters) {
      if (!drawn.letter().equals(letter)) {
        continue;
      }
      for (CDAtom node : nodes) {
        List<CDAtom> positions = ringPositions(fragment, node);
        if (positions != null && inside(drawn.at(), positions)) {
          if (found != null) {
            return null;
          }
          List<CDBond> incident = incident(fragment, node);
          if (incident.size() != 1) {
            return null;
          }
          CDBond bond = incident.get(0);
          found =
              new LetteredRing(
                  node, bond.getBegin() == node ? bond.getEnd() : bond.getBegin(), positions);
        }
      }
    }
    return found;
  }

  /**
   * The six ring atoms a node's candidates belong to, from the one atom of the ring that is no
   * candidate (where the scaffold joins it) round the ring.
   */
  private static List<CDAtom> ringPositions(CDFragment fragment, CDAtom node) {
    List<CDAtom> candidates = node.getAttachedAtoms();
    if (candidates == null || candidates.size() != 5) {
      return null;
    }
    Set<CDAtom> ring = Collections.newSetFromMap(new IdentityHashMap<>());
    ring.addAll(candidates);
    CDAtom joint = null;
    for (CDAtom atom : fragment.getAtoms()) {
      if (!ring.contains(atom) && atom != node && neighbours(fragment, atom, ring).size() == 2) {
        if (joint != null) {
          return null;
        }
        joint = atom;
      }
    }
    if (joint == null) {
      return null;
    }
    ring.add(joint);
    List<CDAtom> positions = new ArrayList<>(List.of(joint));
    CDAtom previous = null;
    CDAtom current = joint;
    while (positions.size() < 6) {
      CDAtom next = null;
      for (CDAtom neighbour : neighbours(fragment, current, ring)) {
        if (neighbour != previous && !positions.contains(neighbour)) {
          next = neighbour;
          break;
        }
      }
      if (next == null) {
        return null;
      }
      positions.add(next);
      previous = current;
      current = next;
    }
    return neighbours(fragment, current, ring).contains(joint) ? positions : null;
  }

  /** Whether a point lies within the ring, nearer its centre than its atoms are. */
  private static boolean inside(CDPoint2D point, List<CDAtom> ring) {
    double x = 0;
    double y = 0;
    for (CDAtom atom : ring) {
      x += atom.getPosition2D().getX();
      y += atom.getPosition2D().getY();
    }
    x /= ring.size();
    y /= ring.size();
    double radius = 0;
    for (CDAtom atom : ring) {
      radius =
          Math.max(
              radius, Math.hypot(atom.getPosition2D().getX() - x, atom.getPosition2D().getY() - y));
    }
    return Math.hypot(point.getX() - x, point.getY() - y) < 0.7 * radius;
  }

  /**
   * The ring atoms a value puts the ring's group on, first the one the drawn group moves to, then
   * those that take a copy of it.
   *
   * @return the atoms, or {@code null} if the value does not fit: a position outside 2 to 6, a
   *     multiplier that does not match the positions, or copies of a group that is not one labelled
   *     atom named as the value names it
   */
  private static List<CDAtom> placement(LetteredRing ring, String value) {
    Matcher matcher = VALUE.matcher(value.strip());
    if (!matcher.matches()) {
      return null;
    }
    String[] locants = matcher.group(1).split(",");
    int multiplier = matcher.group(2) == null ? 1 : matcher.group(2).equals("di") ? 2 : 3;
    if (locants.length != multiplier) {
      return null;
    }
    if (multiplier > 1
        && !label(ring.group())
            .toLowerCase(Locale.ROOT)
            .equals(matcher.group(3).strip().toLowerCase(Locale.ROOT))) {
      return null;
    }
    List<CDAtom> atoms = new ArrayList<>();
    for (String locant : locants) {
      int position = Character.digit(locant.charAt(0), 10);
      if (position < 2 || position > 6 || atoms.contains(ring.positions().get(position - 1))) {
        return null;
      }
      atoms.add(ring.positions().get(position - 1));
    }
    return atoms;
  }

  /** The fragment with each variable group bonded to its target, the nodes left out. */
  private static CDFragment variant(
      CDFragment fragment, List<CDAtom> nodes, Map<CDAtom, CDAtom> targets) {
    CDFragment variant = new CDFragment();
    variant.setAtoms(fragment.getAtoms().stream().filter(atom -> !nodes.contains(atom)).toList());
    variant.setAbsolute(fragment.isAbsolute());
    variant.setRelative(fragment.isRelative());
    variant.setRacemic(fragment.isRacemic());
    variant.setBounds(fragment.getBounds());
    List<CDBond> bonds = new ArrayList<>();
    for (CDBond bond : fragment.getBonds()) {
      if (nodes.contains(bond.getBegin()) || nodes.contains(bond.getEnd())) {
        CDAtom node = nodes.contains(bond.getBegin()) ? bond.getBegin() : bond.getEnd();
        CDBond moved = new CDBond(bond);
        moved.setCrossingBonds(null);
        if (moved.getBegin() == node) {
          moved.setBegin(targets.get(node));
        } else {
          moved.setEnd(targets.get(node));
        }
        bonds.add(moved);
      } else {
        bonds.add(new CDBond(bond));
      }
    }
    variant.setBonds(bonds);
    return variant;
  }

  /**
   * Copies the group on one ring atom to another ring atom of the converted structure: every atom
   * reached from the first ring atom's substituent without passing back through the ring.
   */
  private static boolean copyGroup(
      IAtomContainer structure, List<CDAtom> ring, CDAtom from, CDAtom to) {
    IAtom source = atomAt(structure, from);
    IAtom target = atomAt(structure, to);
    List<IAtom> ringAtoms = new ArrayList<>();
    for (CDAtom atom : ring) {
      ringAtoms.add(atomAt(structure, atom));
    }
    if (source == null || target == null || ringAtoms.contains(null)) {
      return false;
    }
    IAtom root = null;
    for (IAtom neighbour : structure.getConnectedAtomsList(source)) {
      if (!ringAtoms.contains(neighbour)) {
        if (root != null) {
          return false;
        }
        root = neighbour;
      }
    }
    Integer hydrogens = target.getImplicitHydrogenCount();
    if (root == null || hydrogens == null || hydrogens < 1) {
      return false;
    }
    List<IAtom> group = new ArrayList<>();
    Deque<IAtom> queue = new ArrayDeque<>(List.of(root));
    while (!queue.isEmpty()) {
      IAtom atom = queue.pop();
      if (ringAtoms.contains(atom) || group.contains(atom)) {
        continue;
      }
      group.add(atom);
      queue.addAll(structure.getConnectedAtomsList(atom));
    }
    double dx = target.getPoint2d().x - source.getPoint2d().x;
    double dy = target.getPoint2d().y - source.getPoint2d().y;
    Map<IAtom, IAtom> copies = new IdentityHashMap<>();
    List<IBond> groupBonds = new ArrayList<>();
    for (IBond bond : structure.bonds()) {
      if (group.contains(bond.getBegin()) && group.contains(bond.getEnd())) {
        groupBonds.add(bond);
      }
    }
    try {
      for (IAtom atom : group) {
        IAtom copy = atom.clone();
        if (atom.getPoint2d() != null) {
          copy.setPoint2d(new Point2d(atom.getPoint2d().x + dx, atom.getPoint2d().y + dy));
        }
        structure.addAtom(copy);
        copies.put(atom, structure.getAtom(structure.getAtomCount() - 1));
      }
      for (IBond bond : groupBonds) {
        structure.addBond(
            structure.indexOf(copies.get(bond.getBegin())),
            structure.indexOf(copies.get(bond.getEnd())),
            bond.getOrder());
      }
    } catch (CloneNotSupportedException e) {
      return false;
    }
    structure.addBond(
        structure.indexOf(target), structure.indexOf(copies.get(root)), IBond.Order.SINGLE);
    target.setImplicitHydrogenCount(hydrogens - 1);
    return true;
  }

  private static IAtom atomAt(IAtomContainer structure, CDAtom drawn) {
    CDPoint2D at = drawn.getPosition2D();
    for (IAtom atom : structure.atoms()) {
      Point2d point = atom.getPoint2d();
      if (point != null
          && Math.abs(point.x - at.getX()) < 1e-3
          && Math.abs(point.y + at.getY()) < 1e-3) {
        return atom;
      }
    }
    return null;
  }

  private static List<CDAtom> neighbours(CDFragment fragment, CDAtom atom, Set<CDAtom> within) {
    List<CDAtom> neighbours = new ArrayList<>();
    for (CDBond bond : fragment.getBonds()) {
      CDAtom other =
          bond.getBegin() == atom ? bond.getEnd() : bond.getEnd() == atom ? bond.getBegin() : null;
      if (other != null && within.contains(other) && !neighbours.contains(other)) {
        neighbours.add(other);
      }
    }
    return neighbours;
  }

  private static List<CDBond> incident(CDFragment fragment, CDAtom atom) {
    return fragment.getBonds().stream()
        .filter(bond -> bond.getBegin() == atom || bond.getEnd() == atom)
        .toList();
  }

  private static String label(CDAtom atom) {
    if (atom == null || atom.getText() == null || atom.getText().getText() == null) {
      return "";
    }
    return atom.getText().getText().getText().strip();
  }
}
