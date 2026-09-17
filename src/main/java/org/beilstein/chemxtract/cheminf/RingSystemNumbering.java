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
package org.beilstein.chemxtract.cheminf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;

/**
 * Numbers the atoms of a fused ring system the way its name does, so that a substituent given by
 * ring position ({@code 5-OMe} on an indole) can be placed on the atom that position names.
 *
 * <p>For an ortho-fused bicyclic the integer locants run round the periphery and skip the two
 * fusion atoms, which carry letters instead — indole is {@code N1, C2, C3, C3a, C4, C5, C6, C7,
 * C7a}, so its integer positions are simply the peripheral non-fusion atoms in order. Walking the
 * periphery from position 1 and numbering everything that is not a fusion atom therefore reproduces
 * the conventional numbering of indole, oxindole, benzofuran, benzothiophene, quinoline and
 * indazole alike, without a table of ring systems.
 *
 * <p>What the walk needs is where to start and which way to go, and that is what limits it. Only a
 * heteroatom can anchor position 1, and only when exactly one of its two peripheral neighbours is a
 * fusion atom — then the walk must head the other way, and the numbering is forced. Isoquinoline
 * (whose nitrogen sits between two non-fusion atoms), benzimidazole (whose two nitrogens both
 * qualify, giving mirror-image numberings) and carbocycles such as naphthalene (no heteroatom at
 * all) leave the start or the direction open. Those are not numbered at all rather than guessed at:
 * a wrong position grafts a substituent onto the wrong atom and yields a plausible but incorrect
 * structure, which is worse than declining to place it.
 */
public final class RingSystemNumbering {

  private RingSystemNumbering() {}

  /**
   * Numbers the ring system the given atom belongs to.
   *
   * <p>Atoms are identified by their index in the container throughout. The ring containers CDK
   * builds from a cycle basis wrap the container's atoms rather than holding them directly, so the
   * same atom is a different object in each ring it belongs to and comparing by identity would see
   * a fused bicyclic as two disjoint rings.
   *
   * @param container the structure the ring system belongs to
   * @param rings the container's ring set
   * @param ringAtom an atom of the ring system to number
   * @return locant to atom, or an empty map if the system is not one this can number
   */
  public static Map<Integer, IAtom> locants(
      IAtomContainer container, IRingSet rings, IAtom ringAtom) {
    List<Set<Integer>> system = ringSystemContaining(container, rings, ringAtom);
    if (system.size() < 2) {
      // A single ring is numbered from its attachment atom, not from a heteroatom; that is the
      // caller's ortho/meta/para model and is left to it.
      return Map.of();
    }

    Set<Integer> systemAtoms = new LinkedHashSet<>();
    system.forEach(systemAtoms::addAll);

    Set<Integer> fusionAtoms = new LinkedHashSet<>();
    for (int atom : systemAtoms) {
      int member = 0;
      for (Set<Integer> ring : system) {
        if (ring.contains(atom)) {
          member++;
        }
      }
      if (member > 1) {
        fusionAtoms.add(atom);
      }
    }

    Map<Integer, List<Integer>> periphery = periphery(container, system, systemAtoms);
    if (periphery.isEmpty()) {
      return Map.of();
    }

    Integer start = soleStartAtom(container, systemAtoms, fusionAtoms, periphery);
    if (start == null) {
      return Map.of();
    }

    Map<Integer, Integer> byLocant = walk(start, fusionAtoms, periphery, systemAtoms.size());
    Map<Integer, IAtom> atoms = new LinkedHashMap<>();
    byLocant.forEach((locant, index) -> atoms.put(locant, container.getAtom(index)));
    return atoms;
  }

  /**
   * The rings of the system the atom belongs to, as sets of container atom indices: every ring
   * reachable from its own by sharing atoms.
   *
   * @param container the structure the ring system belongs to
   * @param rings the container's ring set
   * @param atom the atom whose ring system is wanted
   * @return the rings of that system, empty if the atom lies in none
   */
  private static List<Set<Integer>> ringSystemContaining(
      IAtomContainer container, IRingSet rings, IAtom atom) {
    int seed = container.indexOf(atom);
    if (seed < 0) {
      return List.of();
    }
    List<Set<Integer>> all = new ArrayList<>();
    for (IAtomContainer ring : rings.atomContainers()) {
      Set<Integer> indices = new LinkedHashSet<>();
      for (IAtom ringAtom : ring.atoms()) {
        int index = container.indexOf(ringAtom);
        if (index >= 0) {
          indices.add(index);
        }
      }
      all.add(indices);
    }

    List<Set<Integer>> system = new ArrayList<>();
    for (Set<Integer> ring : all) {
      if (ring.contains(seed)) {
        system.add(ring);
      }
    }
    boolean grew = !system.isEmpty();
    while (grew) {
      grew = false;
      for (Set<Integer> candidate : all) {
        if (system.contains(candidate)) {
          continue;
        }
        for (Set<Integer> member : system) {
          if (!Collections.disjoint(candidate, member)) {
            system.add(candidate);
            grew = true;
            break;
          }
        }
      }
    }
    return system;
  }

  /**
   * The peripheral neighbours of every system atom: those joined by a bond that belongs to exactly
   * one ring of the system, i.e. every bond but the fusion bonds.
   *
   * @param container the structure the ring system belongs to
   * @param system the rings of the system
   * @param systemAtoms the atoms of the system
   * @return atom to its peripheral neighbours, or empty if the periphery is not a single cycle
   *     through every atom of the system
   */
  private static Map<Integer, List<Integer>> periphery(
      IAtomContainer container, List<Set<Integer>> system, Set<Integer> systemAtoms) {
    Map<Integer, List<Integer>> periphery = new LinkedHashMap<>();
    for (int atom : systemAtoms) {
      periphery.put(atom, new ArrayList<>());
    }
    for (IBond bond : container.bonds()) {
      int begin = container.indexOf(bond.getBegin());
      int end = container.indexOf(bond.getEnd());
      if (!systemAtoms.contains(begin) || !systemAtoms.contains(end)) {
        continue;
      }
      int member = 0;
      for (Set<Integer> ring : system) {
        if (ring.contains(begin) && ring.contains(end)) {
          member++;
        }
      }
      if (member != 1) {
        continue;
      }
      periphery.get(begin).add(end);
      periphery.get(end).add(begin);
    }
    // A bridged or spiro system, or one with an interior atom, does not give every atom exactly two
    // peripheral neighbours; those are outside what this numbering models.
    for (List<Integer> neighbours : periphery.values()) {
      if (neighbours.size() != 2) {
        return Map.of();
      }
    }
    return periphery;
  }

  /**
   * The system's only possible position 1: a ring heteroatom with exactly one fusion atom among its
   * two peripheral neighbours, which fixes both where to start and which way to walk.
   *
   * @param container the structure the ring system belongs to
   * @param systemAtoms the atoms of the system
   * @param fusionAtoms the system's fusion atoms
   * @param periphery the peripheral adjacency
   * @return that atom, or {@code null} if the system has no such atom or more than one
   */
  private static Integer soleStartAtom(
      IAtomContainer container,
      Set<Integer> systemAtoms,
      Set<Integer> fusionAtoms,
      Map<Integer, List<Integer>> periphery) {
    Integer start = null;
    for (int atom : systemAtoms) {
      if (fusionAtoms.contains(atom) || isCarbon(container.getAtom(atom))) {
        continue;
      }
      int fusionNeighbours = 0;
      for (int neighbour : periphery.get(atom)) {
        if (fusionAtoms.contains(neighbour)) {
          fusionNeighbours++;
        }
      }
      if (fusionNeighbours != 1) {
        continue;
      }
      if (start != null) {
        // Two candidates number the system two different ways; neither is safe to assume.
        return null;
      }
      start = atom;
    }
    return start;
  }

  /**
   * Walks the periphery from position 1, numbering every atom that is not a fusion atom.
   *
   * @param start the atom at position 1
   * @param fusionAtoms the system's fusion atoms, which take letters rather than numbers
   * @param periphery the peripheral adjacency
   * @param systemSize the number of atoms in the system
   * @return locant to atom index, or an empty map if the periphery does not close on the start
   */
  private static Map<Integer, Integer> walk(
      int start, Set<Integer> fusionAtoms, Map<Integer, List<Integer>> periphery, int systemSize) {
    int forward = -1;
    for (int neighbour : periphery.get(start)) {
      if (!fusionAtoms.contains(neighbour)) {
        forward = neighbour;
      }
    }
    if (forward < 0) {
      return Map.of();
    }

    Map<Integer, Integer> byLocant = new LinkedHashMap<>();
    int locant = 1;
    byLocant.put(locant, start);

    int previous = start;
    int current = forward;
    for (int step = 1; step < systemSize; step++) {
      if (!fusionAtoms.contains(current)) {
        byLocant.put(++locant, current);
      }
      int next = -1;
      for (int neighbour : periphery.get(current)) {
        if (neighbour != previous) {
          next = neighbour;
        }
      }
      if (next < 0) {
        return Map.of();
      }
      previous = current;
      current = next;
    }
    // The walk must arrive back where it started, having seen every atom exactly once.
    return current == start ? byLocant : Map.of();
  }

  /** Whether the atom is a carbon, i.e. cannot anchor position 1. */
  private static boolean isCarbon(IAtom atom) {
    return "C".equals(atom.getSymbol());
  }
}
