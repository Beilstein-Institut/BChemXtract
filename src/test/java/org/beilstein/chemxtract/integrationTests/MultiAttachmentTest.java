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
package org.beilstein.chemxtract.integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/**
 * Golden integration tests for multi-center ({@code MultiAttachment}, haptic) and variable ({@code
 * VariableAttachment}, position-variation) attachment points.
 *
 * <p>{@code MultiAttachment} nodes (e.g. ferrocene, Zeise's salt) must materialise discrete bonds
 * from the central atom to every atom in the attachment list and yield a single connected
 * substance. {@code VariableAttachment} nodes must enumerate one substance per candidate atom.
 */
public class MultiAttachmentTest {

  private static List<BCXSubstance> xtractCdx(String fileName) throws IOException {
    InputStream in = MultiAttachmentTest.class.getResourceAsStream("/cdx/multipoint/" + fileName);
    assertNotNull(in, "Missing fixture: " + fileName);
    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    return xtractor.xtract(document, new BCXSubstanceInfo(), false);
  }

  private static List<BCXSubstance> xtractCdxml(String fileName) throws IOException {
    InputStream in = MultiAttachmentTest.class.getResourceAsStream("/cdx/multipoint/" + fileName);
    assertNotNull(in, "Missing fixture: " + fileName);
    CDDocument document = CDXMLReader.readDocument(in);
    assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    return xtractor.xtract(document, new BCXSubstanceInfo(), false);
  }

  /** Returns the number of bonds incident to the first atom with the given atomic number, or -1. */
  private static int degreeOfElement(IAtomContainer container, int atomicNumber) {
    for (IAtom atom : container.atoms()) {
      if (atom.getAtomicNumber() != null && atom.getAtomicNumber() == atomicNumber) {
        return container.getConnectedBondsCount(atom);
      }
    }
    return -1;
  }

  // Two eta-5 cyclopentadienyl rings coordinate iron; ligand hydrogens are restored to give
  // C10H10Fe (the standard InChI disconnects the neutral metal).
  private static final String FERROCENE_INCHI = "InChI=1S/2C5H5.Fe/c2*1-2-4-5-3-1;/h2*1-5H;";
  private static final String FERROCENE_KEY = "DFRHTHSZMBROSH-UHFFFAOYSA-N";

  // Three chlorides plus an eta-2 ethylene coordinate platinum.
  private static final String ZEISE_INCHI =
      "InChI=1S/C2H4.3ClH.Pt/c1-2;;;;/h1-2H2;3*1H;/q;;;;+3/p-3";
  private static final String ZEISE_KEY = "VYZZXYBUKKHQII-UHFFFAOYSA-K";

  private static void assertFerrocene(BCXSubstance substance) {
    // Iron is bonded to all 10 ring carbons, and each cyclopentadienyl ring keeps its hydrogens.
    assertEquals(10, degreeOfElement(substance.getAtomContainer(), 26));
    assertEquals("C10H10Fe", substance.getMolecularFormula());
    assertEquals(FERROCENE_INCHI, substance.getInchi());
    assertEquals(FERROCENE_KEY, substance.getInchiKey());
  }

  private static void assertZeiseSalt(BCXSubstance substance) {
    // Platinum is bonded to three chlorides and both ethylene carbons; ethylene stays C2H4.
    assertEquals(5, degreeOfElement(substance.getAtomContainer(), 78));
    assertEquals("C2H4Cl3Pt", substance.getMolecularFormula());
    assertEquals(ZEISE_INCHI, substance.getInchi());
    assertEquals(ZEISE_KEY, substance.getInchiKey());
  }

  @Test
  public void ferroceneCdxBondsIronToBothRings() throws IOException {
    List<BCXSubstance> subs = xtractCdx("ferrocene.cdx");
    assertEquals(1, subs.size());
    assertFerrocene(subs.get(0));
  }

  @Test
  public void ferroceneCdxmlBondsIronToBothRings() throws IOException {
    List<BCXSubstance> subs = xtractCdxml("ferrocene.cdxml");
    assertEquals(1, subs.size());
    assertFerrocene(subs.get(0));
  }

  @Test
  public void zeiseSaltCdxBondsPlatinumToChloridesAndEthylene() throws IOException {
    List<BCXSubstance> subs = xtractCdx("zeise_salt.cdx");
    assertEquals(1, subs.size());
    assertZeiseSalt(subs.get(0));
  }

  @Test
  public void zeiseSaltCdxmlBondsPlatinumToChloridesAndEthylene() throws IOException {
    List<BCXSubstance> subs = xtractCdxml("zeise_salt.cdxml");
    assertEquals(1, subs.size());
    assertZeiseSalt(subs.get(0));
  }

  // The two regioisomers produced when the substituent attaches to either candidate ring atom.
  private static final Set<String> VARIABLE_ATTACHMENT_KEYS =
      Set.of("PWZMDUBWBDWOMC-UHFFFAOYSA-N", "ZGNNZVBGHLEQSO-UHFFFAOYSA-N");

  // R1/R2 stay as pseudo-atoms (no R-group definitions present), so only SMILES is asserted.
  private static final Set<String> MARKUSH_SMILES =
      Set.of("C1=CC(=C(C=C1)*)C2CC(=O)C3C(N3)(O2)*", "C1=CC(=CC(=C1)C2CC(=O)C3C(N3)(O2)*)*");

  private static Set<String> inchiKeys(List<BCXSubstance> substances) {
    return substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());
  }

  private static Set<String> smiles(List<BCXSubstance> substances) {
    return substances.stream().map(BCXSubstance::getSmiles).collect(Collectors.toSet());
  }

  @Test
  public void variableAttachmentCdxEnumeratesCandidateAtoms() throws IOException {
    // The substituent attaches to one of two candidate ring atoms -> two regioisomers.
    List<BCXSubstance> subs = xtractCdx("variableAttachment.cdx");
    assertEquals(2, subs.size());
    assertEquals(VARIABLE_ATTACHMENT_KEYS, inchiKeys(subs));
  }

  @Test
  public void variableAttachmentCdxmlEnumeratesCandidateAtoms() throws IOException {
    List<BCXSubstance> subs = xtractCdxml("variableAttachment.cdxml");
    assertEquals(2, subs.size());
    assertEquals(VARIABLE_ATTACHMENT_KEYS, inchiKeys(subs));
  }

  @Test
  public void markushCdxEnumeratesVariableRgroupPosition() throws IOException {
    // The R-group's attachment position varies over two candidate ring atoms -> two substances.
    List<BCXSubstance> subs = xtractCdx("markush.cdx");
    assertEquals(2, subs.size());
    assertEquals(MARKUSH_SMILES, smiles(subs));
  }

  @Test
  public void markushCdxmlEnumeratesVariableRgroupPosition() throws IOException {
    List<BCXSubstance> subs = xtractCdxml("markush.cdxml");
    assertEquals(2, subs.size());
    assertEquals(MARKUSH_SMILES, smiles(subs));
  }
}
