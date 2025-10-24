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

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXReaction;
import org.beilstein.chemxtract.xtractor.ReactionXtractor;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactionIntegrationTest {


  @Test
  public void rinchi_direction_backwardsTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/backwards.cdx",
            "/integrationTests/reactions/backwards.txt");
  }

  @Test
  public void rinchi_direction_forwardTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/forward.cdx",
            "/integrationTests/reactions/forward.txt");
  }

  @Test
  public void rinchi_direction_reversibleTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/reversible.cdx",
            "/integrationTests/reactions/reversible.txt");
  }

  @Test
  public void rinchi_chemotion_25530Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/chemotion_25530.cdx",
            "/integrationTests/reactions/chemotion_25530_adapted.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_26493Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/chemotion_CRR-26493.cdx",
            "/integrationTests/reactions/chemotion_CRR-26493.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_8205Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/chemotion_CRR_8205.cdx",
            "/integrationTests/reactions/chemotion_CRR_8205.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_43Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/chemotion_CRR-43.cdx",
            "/integrationTests/reactions/chemotion_CRR-43.txt");
  }

  @Test
  public void simple_reaction_Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest("/integrationTests/reactions/simple_reaction.cdx",
            "/integrationTests/reactions/simple_reaction.txt");
  }

  @Test
  public void two_step_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/two_step_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis = Arrays.asList(
            "C1=CC2=C(C=C1Br)C=C(F)O2>C1=C(C=CC(=C1)B(O)O)C(F)(F)F.O>C1=CC(=CC=C1C2=CC=C3C(=C2)C=C(F)O3)C(F)(F)F",
            "C1=CC(=CC=C1C2=CC=C3C(=C2)C=C(F)O3)C(F)(F)F>C1=CC=C(C=C1)B(O)O>C1=CC=C(C=C1)C2=CC3=CC(=CC=C3O2)C4=CC=C(C=C4)C(F)(F)F"
            );
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getReactionSmiles());
    }
  }

  @Test
  public void multi_step_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/multi_step_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis = Arrays.asList(
            "C[C@@H]1CO1>C=CC[Mg]Br>C=CC[C@H](C)CO.C=CCC[C@@H](C)O",
            "C=CC[C@H](C)CO.C=CCC[C@@H](C)O>>C=CCC[C@@H](C)OS(=O)(=O)C1=CC=C(C)C=C1",
            "C=CCC[C@@H](C)OS(=O)(=O)C1=CC=C(C)C=C1>>C=CCC[C@H](C)C(C(=O)OCC)C(=O)OCC",
            "C=CCC[C@H](C)C(C(=O)OCC)C(=O)OCC>CO.[Na]O.O>C=CCC[C@H](C)C(C(=O)O)C(=O)O",
            "C=CCC[C@H](C)C(C(=O)O)C(=O)O>O>C=CCC[C@H](C)CC(=O)O",
            "C=CCC[C@H](C)CC(=O)O>>C=CCC[C@H](C)CCO",
            "C=CCC[C@H](C)CCO>>C=CCC[C@H](C)CCOS(=O)(=O)C1=CC=C(C)C=C1"
    );
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getReactionSmiles());
    }
  }

  @Test
  public void multi_direction_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/multi_direction_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis = Arrays.asList(
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>[Na]O>CC(C)(/C=C/C1=CC=CC=C1)C(=O)N",
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>>CC(C)(CCC1=CC=CC=C1)C#N",
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>C1CCOC1>CC(C)(/C=C/C1=CC=CC=C1)CN",

            "C=CC(C)(C#N)C1=C(C=CC=C1)Br>[Na]O>C=CC1(C)C2=C(C=CC=C2)NC1=O"
            );
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getReactionSmiles());
    }
  }

  void cdxReactionRinchiFullInformationForFirstReactionTest(final String cdxFile, final String rinchiFile)
          throws IOException, URISyntaxException {
    final BCXReaction reaction = convertFirstReactionFromCdxFile(cdxFile);
    final Map<String,String> rinchiInfo = readRinchiFullInformationFromResourceFile(rinchiFile);

    Assert.assertEquals(rinchiInfo.get("RInChI"), reaction.getRinchi());
    Assert.assertEquals(rinchiInfo.get("Long-RInChIKey"), reaction.getLongRinchiKey());
    Assert.assertEquals(rinchiInfo.get("Short-RInChIKey"), reaction.getShortRinchiKey());
    Assert.assertEquals(rinchiInfo.get("Web-RInChIKey"), reaction.getWebRinchiKey());
  }

  private BCXReaction convertFirstReactionFromCdxFile(String fileName) throws IOException {
    ReactionXtractor xtractor = new ReactionXtractor(SilentChemObjectBuilder.getInstance());
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      Assert.assertNotNull(in);
      CDDocument document;
      if (fileName.endsWith(".cdx")) {
        document = CDXReader.readDocument(in);
      } else if (fileName.endsWith(".cdxml")) {
        document = CDXMLReader.readDocument(in);
      } else {
        return null;
      }
      Assert.assertNotNull(document);
      return xtractor.xtract(document).get(0);
      }
  }

  private List<BCXReaction> convertReactions(String fileName) throws IOException {
    ReactionXtractor xtractor = new ReactionXtractor(SilentChemObjectBuilder.getInstance());
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      Assert.assertNotNull(in);
      CDDocument document;
      if (fileName.endsWith(".cdx")) {
        document = CDXReader.readDocument(in);
      } else if (fileName.endsWith(".cdxml")) {
        document = CDXMLReader.readDocument(in);
      } else {
        return null;
      }
      Assert.assertNotNull(document);
      return xtractor.xtract(document);
    }
  }

  private Map<String,String> readRinchiFullInformationFromResourceFile(final String filename) throws IOException, URISyntaxException {
    final String[] rinchiPrefixes = new String[] { "RInChI", "RAuxInfo", "Long-RInChIKey", "Short-RInChIKey", "Web-RInChIKey" };
    final Map<String,String> rinchiFullInformation = new HashMap<>();

    final URL resource = this.getClass().getResource(filename);
    assertThat(resource).describedAs(String.format("File %s not found in classpath!", filename)).isNotNull();
    final Path path = Paths.get(resource.toURI());
    final List<String> lines = Files.readAllLines(path);

    for (final String line : lines) {
      for (final String rinchiPrefix : rinchiPrefixes) {
        if (line.startsWith(rinchiPrefix + "=")) {
          rinchiFullInformation.put(rinchiPrefix, line);
          break;
        }
      }
    }

    return rinchiFullInformation;
  }
}
