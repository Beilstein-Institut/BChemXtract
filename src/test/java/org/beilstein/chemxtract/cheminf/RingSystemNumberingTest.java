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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

/** Numbering of fused ring systems, as {@code 5-OMe} on an indole needs it. */
class RingSystemNumberingTest {

  private final SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
  private final SmilesGenerator generator = new SmilesGenerator(SmiFlavor.Canonical);

  private Map<Integer, IAtom> locantsOf(String smiles) throws Exception {
    IAtomContainer molecule = parser.parseSmiles(smiles);
    IRingSet rings = Cycles.mcb(molecule).toRingSet();
    return RingSystemNumbering.locants(molecule, rings, ringAtom(molecule, rings));
  }

  private static IAtom ringAtom(IAtomContainer molecule, IRingSet rings) {
    for (IAtomContainer ring : rings.atomContainers()) {
      return molecule.getAtom(molecule.indexOf(ring.getAtom(0)));
    }
    throw new IllegalArgumentException("no ring in " + molecule);
  }

  /** Methylates the named position and returns the canonical SMILES, as a check on the atom. */
  private String methylateAt(String smiles, int locant) throws Exception {
    IAtomContainer molecule = parser.parseSmiles(smiles);
    IRingSet rings = Cycles.mcb(molecule).toRingSet();
    IAtom target =
        RingSystemNumbering.locants(molecule, rings, ringAtom(molecule, rings)).get(locant);
    assertThat(target).as("position %d must exist", locant).isNotNull();
    IAtom methyl = new Atom("C");
    methyl.setImplicitHydrogenCount(3);
    molecule.addAtom(methyl);
    molecule.addBond(
        new Bond(target, molecule.getAtom(molecule.getAtomCount() - 1), IBond.Order.SINGLE));
    Integer hydrogens = target.getImplicitHydrogenCount();
    if (hydrogens != null && hydrogens > 0) {
      target.setImplicitHydrogenCount(hydrogens - 1);
    }
    return generator.create(molecule);
  }

  @Test
  void numbersIndoleFromItsNitrogen() throws Exception {
    Map<Integer, IAtom> locants = locantsOf("c1ccc2[nH]ccc2c1");

    assertThat(locants.keySet())
        .as("indole has positions 1 to 7; 3a and 7a are fusion atoms and take letters")
        .containsExactly(1, 2, 3, 4, 5, 6, 7);
    assertThat(locants.get(1).getSymbol()).as("position 1 is the nitrogen").isEqualTo("N");
  }

  @Test
  void numbersQuinolineThroughItsSecondRing() throws Exception {
    Map<Integer, IAtom> locants = locantsOf("c1ccc2ncccc2c1");

    assertThat(locants.keySet()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    assertThat(locants.get(1).getSymbol()).isEqualTo("N");
  }

  @Test
  void numbersBenzofuranFromItsOxygen() throws Exception {
    Map<Integer, IAtom> locants = locantsOf("c1ccc2occc2c1");

    assertThat(locants.keySet()).containsExactly(1, 2, 3, 4, 5, 6, 7);
    assertThat(locants.get(1).getSymbol()).isEqualTo("O");
  }

  @Test
  void placesIndoleSubstituentsOnTheAtomTheirPositionNames() throws Exception {
    String indole = "c1ccc2[nH]ccc2c1";
    List<String> methylated = new ArrayList<>();
    for (int locant = 1; locant <= 7; locant++) {
      methylated.add(methylateAt(indole, locant));
    }

    assertThat(methylated)
        .as("indole's seven positions are seven different methylindoles")
        .doesNotHaveDuplicates();
  }

  @Test
  void putsIndolePositionsFourToSevenOnTheBenzoRing() throws Exception {
    IAtomContainer indole = parser.parseSmiles("c1ccc2[nH]ccc2c1");
    IRingSet rings = Cycles.mcb(indole).toRingSet();
    Map<Integer, IAtom> locants =
        RingSystemNumbering.locants(indole, rings, ringAtom(indole, rings));

    // This is what issue #164 was about: 5, 6 and 7 name atoms of the benzo ring, which counting
    // round the attachment ring alone could never reach.
    for (int locant : new int[] {4, 5, 6, 7}) {
      assertThat(ringSizesOf(indole, rings, locants.get(locant)))
          .as("position %d belongs to the six-membered ring only", locant)
          .containsExactly(6);
    }
    for (int locant : new int[] {1, 2, 3}) {
      assertThat(ringSizesOf(indole, rings, locants.get(locant)))
          .as("position %d belongs to the five-membered ring only", locant)
          .containsExactly(5);
    }
  }

  /** The sizes of the rings the atom belongs to. */
  private static List<Integer> ringSizesOf(IAtomContainer molecule, IRingSet rings, IAtom atom) {
    List<Integer> sizes = new ArrayList<>();
    for (IAtomContainer ring : rings.atomContainers()) {
      for (IAtom ringAtom : ring.atoms()) {
        if (molecule.indexOf(ringAtom) == molecule.indexOf(atom)) {
          sizes.add(ring.getAtomCount());
          break;
        }
      }
    }
    return sizes;
  }

  @Test
  void declinesRingSystemsWhosePositionOneIsNotForced() throws Exception {
    assertThat(locantsOf("c1ccc2cnccc2c1"))
        .as("isoquinoline's nitrogen sits between two non-fusion atoms, so 1 could go either way")
        .isEmpty();
    assertThat(locantsOf("c1ccc2[nH]cnc2c1"))
        .as("benzimidazole's two nitrogens both qualify, giving mirror-image numberings")
        .isEmpty();
    assertThat(locantsOf("c1ccc2ccccc2c1"))
        .as("naphthalene has no heteroatom to anchor position 1")
        .isEmpty();
  }

  @Test
  void leavesSingleRingsToTheOrthoMetaParaModel() throws Exception {
    assertThat(locantsOf("c1ccccc1"))
        .as("a lone ring is numbered from its attachment atom, which is not this class's business")
        .isEmpty();
  }
}
