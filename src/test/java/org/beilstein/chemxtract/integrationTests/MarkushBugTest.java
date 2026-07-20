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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.openscience.cdk.silent.SilentChemObjectBuilder;

/** Regression tests for R-group definitions the text parser currently mishandles. */
public class MarkushBugTest {

  private List<BCXSubstance> xtract(String fileName) throws IOException {
    InputStream in = MarkushBugTest.class.getResourceAsStream("/cheminf/bugs/markush/" + fileName);
    assertNotNull(in, "fixture missing on classpath: " + fileName);

    CDDocument document =
        fileName.endsWith(".cdxml") ? CDXMLReader.readDocument(in) : CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    return xtractor.xtractUnique(document, info, true);
  }

  /**
   * "R1 = H; R2 = F" is a single text node holding two semicolon-separated definitions. Both R1 and
   * R2 must be resolved, yielding exactly one enumerated structure that contains fluorine and no
   * unresolved residue.
   */
  @Test
  public void testTwoResiduesSemicolonSeparated() throws IOException {
    List<BCXSubstance> substances = xtract("Test_2_Reste.cdx");

    assertEquals(1, substances.size(), "expected one enumerated structure for R1=H; R2=F");

    String formula = substances.get(0).getMolecularFormula();
    assertNotNull(formula);
    assertTrue(formula.contains("F"), "R2 must be resolved to fluorine, got: " + formula);
    assertFalse(formula.contains("R"), "no unresolved residue expected, got: " + formula);
  }

  /**
   * R-groups defined structurally via a ChemDraw {@code NamedAlternativeGroup} (not text) must be
   * resolved: the benzene scaffold with alternatives {methyl, chloro} enumerates to toluene and
   * chlorobenzene.
   */
  @Test
  public void testNamedAlternativeGroupResolved() throws IOException {
    List<BCXSubstance> substances = xtract("altgroup_R_methyl_chloro.cdxml");

    Set<String> formulas =
        substances.stream().map(BCXSubstance::getMolecularFormula).collect(Collectors.toSet());
    assertEquals(Set.of("C7H8", "C6H5Cl"), formulas, "expected toluene and chlorobenzene");
  }
}
