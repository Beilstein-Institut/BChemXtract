package org.beilstein.chemxtract.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class ResidueTest {

  @Test
  public void testResidues() throws IOException {
    String fileName = "complex_rgroups.cdx";
    InputStream in = this.getClass().getResourceAsStream("/integrationTests/" + fileName);
    assertNotNull(in);

    CDDocument document = CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor(SilentChemObjectBuilder.getInstance());
    List<BCXSubstance> substances = xtractor.xtractUnique(document, info, true);
    for (BCXSubstance substance : substances) {
      System.out.println("extracted: " + substance.getMolecularFormula());
    }

  }

}
