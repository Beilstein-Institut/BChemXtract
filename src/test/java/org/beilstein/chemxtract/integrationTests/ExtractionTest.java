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

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.*;
import org.beilstein.chemxtract.xtractor.ReactionXtractor;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class ExtractionTest {

  private static final String OUTPUT_DIR =
      System.getProperty("user.dir") + "/target/test-output/bchemxtract";
  private File outputDir;

  @Before
  public void setUp() throws Exception {
    outputDir = new File(OUTPUT_DIR);
    outputDir.mkdirs();
  }

  @Test
  public void testExtractSubstances() throws IOException {
    String fileName = "test_fixture.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info);
    assertThat(info.getNoFragments()).isEqualTo(14);
    assertThat(info.getNoInchis()).isEqualTo(12);
    assertThat(info.getNoSubstances()).isEqualTo(12);

    assertThat(substances.size()).isEqualTo(12);
    BCXSubstance s0 = substances.get(0);
    BCXSubstance s1 = substances.get(1);
    assertThat(s0).isNotEqualTo(s1);
    assertThat(s0.toString()).isNotEqualTo(s1.toString());
    assertThat(s0.getOccurrences().size()).isEqualTo(1);
    assertThat(s1.getOccurrences().size()).isEqualTo(1);
    BCXSubstanceOccurrence so0 = s0.getOccurrences().iterator().next();
    BCXSubstanceOccurrence so1 = s1.getOccurrences().iterator().next();
    assertThat(so0).isNotEqualTo(so1);
    assertThat(so0.toString()).isNotEqualTo(so1.toString());

    for (BCXSubstance substance : substances) {
      try {
        depictSubstance(substance);
      } catch (IOException | CDKException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void testExtractReactions() throws IOException {
    String fileName = "test_fixture.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    ReactionXtractor xtractor = new ReactionXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXReaction> reactions = xtractor.xtract(document);

    assertThat(reactions.size()).isEqualTo(4);

    assertThat(reactions.size()).isEqualTo(4);
    BCXReaction r0 = reactions.get(0);
    BCXReaction r1 = reactions.get(1);
    assertThat(r0).isNotEqualTo(r1);
    assertThat(r0.toString()).isNotEqualTo(r1.toString());
    assertThat(r0.getProducts().size()).isEqualTo(1);
    assertThat(r1.getProducts().size()).isEqualTo(1);
    BCXReactionComponent rc0 = r0.getProducts().get(0);
    BCXReactionComponent rc1 = r1.getProducts().get(0);
    assertThat(rc0).isNotEqualTo(rc1);
    assertThat(rc0.toString()).isNotEqualTo(rc1.toString());

    for (BCXReaction reaction : reactions) {
      try {
        depictReaction(reaction);
      } catch (IOException | CDKException e) {
        e.printStackTrace();
      }
    }
  }

  private void depictSubstance(BCXSubstance substance) throws IOException, CDKException {
    File output = new File(outputDir, substance.getInchiKey() + ".png");
    FileOutputStream fos = new FileOutputStream(output);

    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer m = sp.parseSmiles(substance.getSmiles());
    DepictionGenerator dg =
        new DepictionGenerator()
            .withSize(1024, 1024)
            .withAtomColors()
            .withFillToFit()
            .withBackgroundColor(Color.WHITE);
    Depiction d = dg.depict(m);
    d.writeTo(Depiction.PNG_FMT, fos);
    fos.flush();
    fos.close();
  }

  private void depictReaction(BCXReaction reaction) throws IOException, CDKException {
    File output = new File(outputDir, reaction.getShortRinchiKey() + ".png");
    FileOutputStream fos = new FileOutputStream(output);

    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IReaction r = sp.parseReactionSmiles(reaction.getReactionSmiles());
    DepictionGenerator dg =
        new DepictionGenerator().withAtomColors().withFillToFit().withBackgroundColor(Color.WHITE);
    Depiction d = dg.depict(r);
    d.writeTo(Depiction.PNG_FMT, fos);
    fos.flush();
    fos.close();
  }
}
