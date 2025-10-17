package org.beilstein.chemxtract.integrationTests;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SgroupTest {

  @Test
  public void multipleGroupsTest() throws Exception {
    String fileName = "multipleGroups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());

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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
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
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());
    Assert.assertEquals(substances.get(0).getInchi(), substances.get(1).getInchi());
    Assert.assertEquals(substances.get(0).getInchiKey(), substances.get(1).getInchiKey());
  }

  @Test
  public void multiple_groups_Test() throws IOException {
    String fileName = "multiple_groups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/sgroups/" + fileName);
    Assert.assertNotNull(in);
    CDDocument document = CDXReader.readDocument(in);
    Assert.assertNotNull(document);
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtract(document, new BCXSubstanceInfo());

    Set<String> expectedInChIKeys = new HashSet<>();
    expectedInChIKeys.add("IGBSZPRCTMBPTC-AAVRWANBSA-N");
    expectedInChIKeys.add("IGBSZPRCTMBPTC-SYQHCUMBSA-N");
    expectedInChIKeys.add("RBRLRKACDFDWRL-GMTAPVOTSA-N");
    expectedInChIKeys.add("XRPPEHQKPPMLJX-GZBOUJLJSA-N");
    expectedInChIKeys.add("YHWJXMYSKZVKKZ-GMTAPVOTSA-N");
    expectedInChIKeys.add("TYHGKLBJBHACOI-UHFFFAOYSA-N");
    expectedInChIKeys.add("FQKSDILMSCMUET-OWCLPIDISA-N");
    expectedInChIKeys.add("XPAHJIJZHYZDSW-FXBNSDBLSA-N");
    expectedInChIKeys.add("NOUIRZGGWSLLAJ-MFKMUULPSA-N");
    expectedInChIKeys.add("ZZMCMYBKCMGRDK-XWSJACJDSA-N");

    Set<String> inchiKeys = new HashSet<>();
    substances.forEach(s -> {
      inchiKeys.add(s.getInchiKey());
    });

    Assert.assertEquals(expectedInChIKeys.size(), inchiKeys.size());
    inchiKeys.forEach(s -> {
      Assert.assertTrue(expectedInChIKeys.contains(s));
    });

  }
}
