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
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class IntegrationTest {
  @Test
  public void E_1_Bromo_1_2_dichloroetheneTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/(E)-1-Bromo-1,2-dichloroethene.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs = { "InChI=1S/C2HBrCl2/c3-2(5)1-4/h1H/b2-1-" };
    String[] expectedInChIKeys = { "RXIALFZUTHFURS-UPHRSURJSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertFalse(subs.isEmpty());
    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void Z_1_Bromo_1_2_dichloroetheneTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/(Z)-1-Bromo-1,2-dichloroethene.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs = { "InChI=1S/C2HBrCl2/c3-2(5)1-4/h1H/b2-1+" };
    String[] expectedInChIKeys = { "RXIALFZUTHFURS-OWOJBTEDSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertFalse(subs.isEmpty());
    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void E_Z_either_butene_IUPACTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/E_Z_either_butene.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs = { "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3" };
    String[] expectedInChIKeys = { "IAQRGUVFOMOMEM-UHFFFAOYSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertFalse(subs.isEmpty());
    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void S_laballenic_acidTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/(S)-laballenic acid.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs =
            { "InChI=1S/C18H32O2/c1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18(19)20/h12,14H,2-11,15-17H2,1H3,(H,19,20)/t13-/m1/s1" };
    String[] expectedInChIKeys = { "YXJXBVWHSBEPDQ-CYBMUJFWSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());

    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void R_laballenic_acidTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/(R)-laballenic acid.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs =
            { "InChI=1S/C18H32O2/c1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18(19)20/h12,14H,2-11,15-17H2,1H3,(H,19,20)/t13-/m0/s1" };
    String[] expectedInChIKeys = { "YXJXBVWHSBEPDQ-ZDUSSCGKSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertFalse(subs.isEmpty());
    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void testStructureWithAmbiguousStereo() throws IOException {
    String fileName = "ambiguousStereo.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());
    String expectedSmiles = "CC1=CC[C@H]2[C@@](C)(CC[C@@]3([H])C(C)(C)CCC[C@@]32C)[C@]1([H])CC(=O)O";
    Assert.assertEquals(expectedSmiles, subs.get(0).getSmiles());
  }

  @Test
  public void getInChITest_1() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/stereo_bug.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String expectedInChI = "InChI=1S/C13H15NO4/c1-9(15)13(10(2)16)12(8-14(17)18)11-6-4-3-5-7-11/h3-7,12-13H,8H2,1-2H3/t12-/m1/s1";
    String expectedInChIKey = "OPKHMCBLQAHSEN-GFCCVEGCSA-N";
    String expectedSmiles = "CC(=O)C(C(=O)C)[C@H](C[N+](=O)[O-])C1=CC=CC=C1";

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());
    Assert.assertEquals(expectedInChI, subs.get(0).getInchi());
    Assert.assertEquals(expectedInChIKey, subs.get(0).getInchiKey());
    Assert.assertEquals(expectedSmiles, subs.get(0).getSmiles());
  }

  @Test
  public void dLacticAcidStereoTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/D-lactic-acid.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);


    String[] expectedInChIs = { "InChI=1S/C3H6O3/c1-2(4)3(5)6/h2,4H,1H3,(H,5,6)/t2-/m1/s1" };
    String[] expectedInChIKeys = { "JVTAAEKCZFNVCJ-UWTATZPHSA-N" };
    String[] expectedSmiles = { "C[C@]([H])(C(=O)O)O" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());

    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
      Assert.assertEquals(expectedSmiles[i], subs.get(i).getSmiles());
    }
  }

  @Test
  public void lLacticAcidStereoTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/L-lactic-acid.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs = { "InChI=1S/C3H6O3/c1-2(4)3(5)6/h2,4H,1H3,(H,5,6)/t2-/m0/s1" };
    String[] expectedInChIKeys = { "JVTAAEKCZFNVCJ-REOHCLBHSA-N" };
    String[] expectedSmiles = { "C[C@@]([H])(C(=O)O)O" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());

    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
      Assert.assertEquals(expectedSmiles[i], subs.get(i).getSmiles());
    }
  }

  @Test
  public void isotopesTest() throws IOException {
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/Isotopes-H-D-T-C-O.cdx");
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    String[] expectedInChIs = { "InChI=1S/C3H8O/c1-2-3-4/h4H,2-3H2,1H3/i1D2,2T2,3+2,4+2" };
    String[] expectedInChIKeys = { "BDERNNFJNOPAEC-FHXJQOPXSA-N" };

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> subs = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertFalse(subs.isEmpty());

    for (int i = 0; i < subs.size(); i++) {
      Assert.assertEquals(expectedInChIs[i], subs.get(i).getInchi());
      Assert.assertEquals(expectedInChIKeys[i], subs.get(i).getInchiKey());
    }
  }

  @Test
  public void sugarStereoTest() throws IOException {
    String fileName = "wavy_sugars.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sugars/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertEquals("JYVGPYJRTSEMTB-APCUNRMYSA-N", substances.get(0).getInchiKey());
    Assert.assertEquals("USIHOLUAXUVUHQ-MPUZOFBHSA-N", substances.get(2).getInchiKey());
    Assert.assertEquals("OQRGNZAPLDELGL-JGEXHRIQSA-N", substances.get(3).getInchiKey());
    Assert.assertEquals("QLYBVIQMCILRJU-SGZWNVLDSA-N", substances.get(4).getInchiKey());
    Assert.assertEquals("LMICALCPRSCSMO-WAPOLKSXSA-N", substances.get(6).getInchiKey());
  }

  @Test
  public void sugar_bond_up_SRSSR_Test() throws IOException {
    String fileName = "bond_up_SRSSR.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sugars/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals("WQZGKKKJIJFFOK-DVKNGEFBSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void sugarr_bond_down_SRRSR_Test() throws IOException {
    String fileName = "bond_down_SRRSR.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sugars/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals("WQZGKKKJIJFFOK-UKFBFLRUSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void sugarr_bond_halfdown_SRRS_Test() throws IOException {
    String fileName = "bond_halfdown_SRRS.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sugars/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals("WQZGKKKJIJFFOK-UKFBFLRUSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void radicalTest() throws Exception {
    String fileName = "radical.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Assert.assertEquals("InChI=1S/C14H13F3NO/c15-14(16,17)9-4-3-7-13(19)18-10-8-11-5-1-2-6-12(11)18/h1-2,4-6,8,10H,3,7,9H2",
            substances.get(0).getInchi());
    Assert.assertEquals("ZVLDKIFKRXLFQV-UHFFFAOYSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void multiple_groups_Test() throws IOException {
    String fileName = "multiple_groups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);

    Stack<String> expectedInChIKeys = new Stack<>();
    expectedInChIKeys.add("ZZMCMYBKCMGRDK-XWSJACJDSA-N");
    expectedInChIKeys.add("NOUIRZGGWSLLAJ-MFKMUULPSA-N");
    expectedInChIKeys.add("XPAHJIJZHYZDSW-FXBNSDBLSA-N");
    expectedInChIKeys.add("FQKSDILMSCMUET-OWCLPIDISA-N");
    expectedInChIKeys.add("TYHGKLBJBHACOI-UHFFFAOYSA-N");
    expectedInChIKeys.add("YHWJXMYSKZVKKZ-GMTAPVOTSA-N");
    expectedInChIKeys.add("XRPPEHQKPPMLJX-GZBOUJLJSA-N");
    expectedInChIKeys.add("RBRLRKACDFDWRL-GMTAPVOTSA-N");
    expectedInChIKeys.add("IGBSZPRCTMBPTC-SYQHCUMBSA-N");
    expectedInChIKeys.add("IGBSZPRCTMBPTC-AAVRWANBSA-N");

    substances.forEach(s -> {
      Assert.assertEquals(expectedInChIKeys.pop(), s.getInchiKey());
    });
  }

  @Test
  public void multipleGroupsTest() throws Exception {
    String fileName = "multipleGroups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals("InChI=1S/C8H18/c1-3-5-7-8-6-4-2/h3-8H2,1-2H3", substances.get(0).getInchi());
    Assert.assertEquals("TVMXDCGIABBOFY-UHFFFAOYSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void multipleGroupNonaneMUL2Test() throws Exception {
    String fileName = "nonane_MUL2.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void multipleGroupDimethylOctanceMUL2Test() throws Exception {
    String fileName = "dimethyloctane_MUL2.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void multipleGroupDimethylStyreneMUL5Test() throws Exception {
    String fileName = "Sgroups_MultipleGroup 01.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals("InChI=1S/5C8H8/c5*1-2-8-6-4-3-5-7-8/h5*2-7H,1H2", substances.get(0).getInchi());
    Assert.assertEquals("IQNATEYHNUCOMB-UHFFFAOYSA-N", substances.get(0).getInchiKey());
  }

  @Test
  public void multipleGroupTetramethylnonaneMUL2Test() throws Exception {
    String fileName = "tetramethylnonane_MUL2.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void multipleGroupDecamethylpentadecane_MUL5Test() throws Exception {
    String fileName = "decamethylpentadecane_MUL5.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void multipleGroupTrimethylnonane_MUL3Test() throws Exception {
    String fileName = "trimethylnonane_MUL3.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), false);
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void rgroups_OnlyR_Test() throws IOException {
    String fileName = "simple_rgroups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);

    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo(), true);

    Set<String> expectedSmiles = new HashSet<>();
    expectedSmiles.add("C1=C(C(=CC(=C1Br)Cl)Br)Cl");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C3=C(C(=C4C=C([H])C([H])=CC4=C3C)C)C2=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C5=C1C6=C(C)C7=C(C=C([H])C([H])=C7)C(=C65)C)(C(C)C)C(C)C");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C3=C(C(=C4C=C(C(=CC4=C3C)F)F)C)C2=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C5=C1C6=C(C)C7=C(C=C(C(=C7)F)F)C(=C65)C)(C(C)C)C(C)C");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C3=C(C(=C4C=C5C(=CC4=C3C)OCO5)C)C2=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C6=C1C7=C(C)C8=C(C=C9C(=C8)OCO9)C(=C76)C)(C(C)C)C(C)C");
    expectedSmiles.add("CC12C3=C(C=C([H])C([H])=C3)C(C)(C4C5=C(C6=C(C7C6C8(C)C9=C(C=C([H])C([H])=C9)C7(C)O8)C(=C5C41)Cl)Cl)O2");
    expectedSmiles.add("CC12C3=C(C=C(C(=C3)F)F)C(C)(C4C5=C(C6=C(C7C6C8(C)C9=C(C=C(C(=C9)F)F)C7(C)O8)C(=C5C41)Cl)Cl)O2");
    expectedSmiles.add("CC12C3=C(C=C4C(=C3)OCO4)C(C)(C5C6=C(C7=C(C8C7C9(C)C%10=C(C=C%11C(=C%10)OCO%11)C8(C)O9)C(=C6C51)Cl)Cl)O2");
    expectedSmiles.add("CC12C=CC(C)(C3=C1C=C([H])C([H])=C3)O2");
    expectedSmiles.add("CC12C=CC(C)(C3=C1C=C(C(=C3)F)F)O2");
    expectedSmiles.add("CC12C=CC(C)(C3=C1C=C4C(=C3)OCO4)O2");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C(=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C3=C1C4C3C5(C)C6=C(C=C([H])C([H])=C6)C4(C)O5)C7C2C8(C)C9=C(C=C([H])C([H])=C9)C7(C)O8)(C(C)C)C(C)C");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C(=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C3=C1C4C3C5(C)C6=C(C=C(C(=C6)F)F)C4(C)O5)C7C2C8(C)C9=C(C=C(C(=C9)F)F)C7(C)O8)(C(C)C)C(C)C");
    expectedSmiles.add("CC(C)[Si](C#CC1=C2C(=C(C#C[Si](C(C)C)(C(C)C)C(C)C)C3=C1C4C3C5(C)C6=C(C=C7C(=C6)OCO7)C4(C)O5)C8C2C9(C)C%10=C(C=C%11C(=C%10)OCO%11)C8(C)O9)(C(C)C)C(C)C");
    expectedSmiles.add("C#C[Si](C(C)C)(C(C)C)C(C)C");

    Set<String> smiles = new HashSet<>();
    substances.forEach(s -> {
      smiles.add(s.getSmiles());
    });

    Assert.assertEquals(expectedSmiles.size(), smiles.size());
    smiles.forEach(s -> {
      Assert.assertTrue(expectedSmiles.contains(s));
    });
  }
}
