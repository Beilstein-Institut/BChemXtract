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

import java.util.BitSet;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.ringsearch.RingSearch;

/**
 * Class for detecting sugar-like rings (furanose/pyranose motifs) in chemical structures.
 *
 * <p>Heuristic check for small heteroaliphatic rings (5–6 atoms) with exactly one ring oxygen, all
 * single bonds, and multiple oxygen substituents (–OH or –OR groups). This approximates common
 * monosaccharide ring structures such as furanoses and pyranoses.
 *
 * <h2>Example usage:</h2>
 *
 * <pre>{@code
 * IAtomContainer mol = ...; // load or build a molecule
 * if (SugarRings.containsSugarRings(mol)) {
 *     System.out.println("Molecule contains a sugar ring!");
 * }
 * }</pre>
 */
// TODO needs to check for chair config by turns, just ‘sugar’ is not enough.
public final class SugarRings {

  /** Private constructor to hide the implicit public one */
  private SugarRings() {}

  /**
   * Determines whether a molecule contains at least one sugar-like ring.
   *
   * @param mol the {@link IAtomContainer} to analyze
   * @return {@code true} if the molecule contains at least one sugar-like ring, {@code false}
   *     otherwise
   */
  public static boolean containsSugarRings(IAtomContainer mol) {
    GraphUtil.EdgeToBondMap bondMap = GraphUtil.EdgeToBondMap.withSpaceFor(mol);
    int[][] graph = GraphUtil.toAdjList(mol, bondMap);
    RingSearch ringSearch = new RingSearch(mol, graph);
    for (int[] isolated : ringSearch.isolated()) {
      if (SugarRings.isSugarRing(mol, isolated)) return true;
    }
    return false;
  }

  /**
   * Checks whether a given ring in a molecule has sugar-like features.
   *
   * <p>A ring is considered sugar-like if it meets the following criteria:
   *
   * <ul>
   *   <li>Ring size is 5 or 6 atoms
   *   <li>Exactly one oxygen atom is part of the ring
   *   <li>All ring bonds are single and non-aromatic
   *   <li>All other ring atoms are carbon
   *   <li>The carbons have multiple substituent oxygens (single-bonded)
   *   <li>Threshold: at least 3 substituent oxygens for 6-membered rings (pyranoses), at least 2
   *       for 5-membered rings (furanoses)
   * </ul>
   *
   * @param mol the {@link IAtomContainer} containing the ring
   * @param cycle array of atom indices representing the ring
   * @return {@code true} if the ring matches the sugar heuristic, {@code false} otherwise
   */
  public static boolean isSugarRing(IAtomContainer mol, int[] cycle) {
    int n = cycle.length;
    // Heuristic: 5–6 ring atoms
    if (n < 5 || n > 6) return false;

    // fast membership test, BitSet for fast lookup later on
    BitSet inRing = new BitSet(mol.getAtomCount());
    for (int idx : cycle) inRing.set(idx);

    // count ring oxygen atoms and check ring bond saturation
    int ringOxy = 0;
    for (int i = 0; i < n; i++) {
      IAtom atom = mol.getAtom(cycle[i]);
      if ("O".equals(atom.getSymbol())) ringOxy++;
      // check the bond to the next ring atom
      int j = cycle[(i + 1) % n];
      IBond bond = mol.getBond(atom, mol.getAtom(j));
      // sugar rings are saturated
      if (bond == null || bond.getOrder() != IBond.Order.SINGLE || bond.isAromatic()) return false;
    }
    // sugars have exactly one ring oxygen
    if (ringOxy != 1) return false;

    // count exocyclic oxygens on ring carbons (not counting the ring oxygen)
    int substiuentOxy = 0;
    for (int idx : cycle) {
      IAtom atom = mol.getAtom(idx);
      // skip the ring oxygen
      if ("O".equals(atom.getSymbol())) continue;
      // classic aldo-/keto-hexose/pentose: ring C only
      if (!"C".equals(atom.getSymbol())) return false;
      for (IAtom neighbor : mol.getConnectedAtomsList(atom)) {
        int neighborIdx = mol.indexOf(neighbor);
        if (!inRing.get(neighborIdx) && "O".equals(neighbor.getSymbol())) {
          IBond bond = mol.getBond(atom, neighbor);
          if (bond.getOrder() == IBond.Order.SINGLE && !bond.isAromatic()) substiuentOxy++;
        }
      }
    }

    // typical thresholds: ≥3 for pyranoses (6-membered), ≥2 for furanoses (5-membered)
    if (n == 6) return substiuentOxy >= 3;
    else return substiuentOxy >= 2;
  }
}
