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

import static org.assertj.core.api.Assertions.assertThat;

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
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXReaction;
import org.beilstein.chemxtract.xtractor.ReactionXtractor;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class ReactionIntegrationTest {

  @Test
  public void rinchi_direction_backwardsTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/backwards.cdx", "/integrationTests/reactions/backwards.txt");
  }

  @Test
  public void rinchi_direction_forwardTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/forward.cdx", "/integrationTests/reactions/forward.txt");
  }

  @Test
  public void rinchi_direction_reversibleTest() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/reversible.cdx", "/integrationTests/reactions/reversible.txt");
  }

  @Test
  public void rinchi_chemotion_25530Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/chemotion_25530.cdx",
        "/integrationTests/reactions/chemotion_25530_adapted.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_26493Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/chemotion_CRR-26493.cdx",
        "/integrationTests/reactions/chemotion_CRR-26493.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_8205Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/chemotion_CRR_8205.cdx",
        "/integrationTests/reactions/chemotion_CRR_8205.txt");
  }

  @Test
  public void rinchi_chemotion_CRR_43Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/chemotion_CRR-43.cdx",
        "/integrationTests/reactions/chemotion_CRR-43.txt");
  }

  @Test
  public void simple_reaction_Test() throws IOException, URISyntaxException {
    cdxReactionRinchiFullInformationForFirstReactionTest(
        "/integrationTests/reactions/simple_reaction.cdx",
        "/integrationTests/reactions/simple_reaction.txt");
  }

  @Test
  public void two_step_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/two_step_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis =
        Arrays.asList(
            "RInChI=1.00.1S/C15H8F4O/c16-14-8-11-7-10(3-6-13(11)20-14)9-1-4-12(5-2-9)15(17,18)19/h1-8H<>C8H4BrFO/c9-6-1-2-7-5(3-6)4-8(10)11-7/h1-4H<>3K.H3O4P/c;;;1-5(2,3)4/h;;;(H3,1,2,3,4)/q3*+1;/p-3!C4H8O2/c1-2-6-4-3-5-1/h1-4H2!C7H6BF3O2/c9-7(10,11)5-1-3-6(4-2-5)8(12)13/h1-4,12-13H!H2O/h1H2/d-",
            "RInChI=1.00.1S/C15H8F4O/c16-14-8-11-7-10(3-6-13(11)20-14)9-1-4-12(5-2-9)15(17,18)19/h1-8H<>C21H13F3O/c22-21(23,24)18-9-6-14(7-10-18)16-8-11-19-17(12-16)13-20(25-19)15-4-2-1-3-5-15/h1-13H<>C18H33P/c1-4-10-16(11-5-1)19(17-12-6-2-7-13-17)18-14-8-3-9-15-18/h16-18H,1-15H2!C6H7BO2/c8-7(9)6-4-2-1-3-5-6/h1-5,8-9H!CH2O3.2K/c2-1(3)4;;/h(H2,2,3,4);;/q;2*+1/p-2/d+");
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getRinchi());
    }
  }

  @Test
  public void multi_step_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/multi_step_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis =
        Arrays.asList(
            "C[C@@H]1CO1>CCOCC.C=CC[Mg]Br>C=CC[C@H](C)CO.C=CCC[C@@H](C)O",
            "C=CC[C@H](C)CO.C=CCC[C@@H](C)O>CN(C)C1=CC=NC=C1.CCN(CC)CC.C(Cl)Cl.C(Cl)Cl>C=CCC[C@@H](C)OS(=O)(=O)C1=CC=C(C)C=C1",
            "C=CCC[C@@H](C)OS(=O)(=O)C1=CC=C(C)C=C1>CN(C)C=O.[I-].[Na+].[Na+].[H-]>C=CCC[C@H](C)C(C(=O)OCC)C(=O)OCC",
            "C=CCC[C@H](C)C(C(=O)OCC)C(=O)OCC>CO.[Na]O.O>C=CCC[C@H](C)C(C(=O)O)C(=O)O",
            "C=CCC[C@H](C)C(C(=O)O)C(=O)O>CS(=O)C.O>C=CCC[C@H](C)CC(=O)O",
            "C=CCC[C@H](C)CC(=O)O>>C=CCC[C@H](C)CCO",
            "C=CCC[C@H](C)CCO>>C=CCC[C@H](C)CCOS(=O)(=O)C1=CC=C(C)C=C1");
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getReactionSmiles());
    }
  }

  @Test
  public void backwards_no_products_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/backwards_no_products.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void backwards_no_reactants_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/backwards_no_reactants.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void forward_no_products_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/forward_no_products.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void forward_no_reactants_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/forward_no_reactants.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void reversible_no_products_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/reversible_no_products.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void reversible_no_reactants_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/reversible_no_reactants.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void multi_direction_reaction_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/multi_direction_reaction.cdx";
    List<BCXReaction> reactions = convertReactions(cdxFile);
    List<String> rinchis =
        Arrays.asList(
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>CC(C)(C)O.[Na]O>CC(C)(/C=C/C1=CC=CC=C1)C(=O)N",
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>CCOC(=O)C.CO.[Pd].[H][H]>CC(C)(CCC1=CC=CC=C1)C#N",
            "CC(C)(/C=C\\C1=CC=CC=C1)C#N>C1CCOC1.[AlH4-].[Li+]>CC(C)(/C=C/C1=CC=CC=C1)CN",
            "C=CC(C)(C#N)C1=C(C=CC=C1)Br>CC(C)(C)O.[Na]O>C=CC1(C)C2=C(C=CC=C2)NC1=O");
    for (int i = 0; i < reactions.size(); i++) {
      Assert.assertEquals(rinchis.get(i), reactions.get(i).getReactionSmiles());
    }
  }

  @Test
  public void horizontal_reactant_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/horizontal_reactant_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void horizontal_product_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/horizontal_product_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void horizontal_both_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/horizontal_both_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void vertical_reactant_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/vertical_reactant_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void vertical_product_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/vertical_product_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void vertical_both_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/vertical_both_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void diagonal_reactant_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/diagonal_reactant_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void diagonal_product_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/diagonal_product_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  @Test
  public void diagonal_both_off_Test() throws IOException {
    String cdxFile = "/integrationTests/reactions/diagonal_both_off.cdx";
    List<BCXReaction> reactions = convertReactionsWithSanitizeOption(cdxFile, true);
    assert reactions != null;
    Assert.assertTrue(reactions.isEmpty());
  }

  void cdxReactionRinchiFullInformationForFirstReactionTest(
      final String cdxFile, final String rinchiFile) throws IOException, URISyntaxException {
    final BCXReaction reaction = convertFirstReactionFromCdxFile(cdxFile);
    final Map<String, String> rinchiInfo = readRinchiFullInformationFromResourceFile(rinchiFile);

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
    return convertReactionsWithSanitizeOption(fileName, false);
  }

  private List<BCXReaction> convertReactionsWithSanitizeOption(String fileName, boolean sanitize)
      throws IOException {
    ReactionXtractor xtractor =
        new ReactionXtractor(SilentChemObjectBuilder.getInstance(), sanitize);
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

  private Map<String, String> readRinchiFullInformationFromResourceFile(final String filename)
      throws IOException, URISyntaxException {
    final String[] rinchiPrefixes =
        new String[] {"RInChI", "RAuxInfo", "Long-RInChIKey", "Short-RInChIKey", "Web-RInChIKey"};
    final Map<String, String> rinchiFullInformation = new HashMap<>();

    final URL resource = this.getClass().getResource(filename);
    assertThat(resource)
        .describedAs(String.format("File %s not found in classpath!", filename))
        .isNotNull();
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
