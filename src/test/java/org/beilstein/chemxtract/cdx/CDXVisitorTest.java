package org.beilstein.chemxtract.cdx;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implements an object "visiting" all CD objects within the tree. Each visited object accepts the visitor
 * and invokes its corresponding method. Depending on what the visitor's subclass (this) overrides,
 * the visited object may change state or add itself, therefore presenting an alternative to recursively
 * and actively traversing the tree.
 */
public class CDXVisitorTest extends CDVisitor {

  List<CDAtom> atomsWithWarnings = new ArrayList<CDAtom>();
  List<CDBond> bondsWithWarnings = new ArrayList<CDBond>();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testVisitor() throws IOException {

    String fileName = "test_fixture.cdxml";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertThat(in).isNotNull();

    CDDocument document = CDXMLReader.readDocument(in);
    assertThat(document).isNotNull();

    List<CDPage> pages = document.getPages();
    assertThat(pages.size()).isEqualTo(1);

    CDPage page = pages.get(0);
    page.accept(this);

    assertThat(atomsWithWarnings.size()).isEqualTo(8);
    assertThat(bondsWithWarnings.size()).isEqualTo(17);

    System.out.println("\n\nAtom Warnings:");
    for (CDAtom a : atomsWithWarnings) {
      System.out.println(a.getChemicalWarning());
    }

    System.out.println("\n\nBond Warnings:");
    for (CDBond b : bondsWithWarnings) {
      System.out.println(b.getChemicalWarning());
    }
  }

  @Override
  public void visitAtom(CDAtom atom) {
    if (atom.getChemicalWarning() != null) {
      atomsWithWarnings.add(atom);
    }
  }

  @Override
  public void visitBond(CDBond bond) {
    if (bond.getChemicalWarning() != null) {
      bondsWithWarnings.add(bond);
    }
  }
}
