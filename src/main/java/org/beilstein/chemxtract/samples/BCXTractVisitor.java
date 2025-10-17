package org.beilstein.chemxtract.samples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDVisitor;
import org.beilstein.chemxtract.cdx.reader.CDXReader;

/**
 * A simple showcase for a visitor. Prints out statistics about atoms.
 */
public class BCXTractVisitor extends CDVisitor {

  private int visitedAtoms;
  
  public static void main(String[] args) throws Exception {
    BCXTractVisitor bcx = new BCXTractVisitor();
    bcx.work(args.length > 0 ? args[0] : null);
  }

  public void work(String filename) throws Exception {
    // load input CDX file - either from command-line args or resources
    InputStream in = null;
    if (filename == null) {
      in = getClass().getResourceAsStream("/org/beilstein/chemxtract/tools/test_fixture.cdx");
    } else {
      in = new FileInputStream(filename);
    }

    // parse CDX into in-memory model
    CDDocument document = CDXReader.readDocument(in);

    // get first page
    List<CDPage> pages = document.getPages();

    // send this visitor to first page object
    if (pages != null && pages.size() > 0) {
      CDPage page = pages.get(0);
      page.accept(this);
      System.out.println("\nVisited " + visitedAtoms + " atoms in total.");
    }
    else {
      System.out.println("Nothing to do.");
    }
  }

  @Override
  public void visitAtom(CDAtom atom) {
    System.out.println("\nAtom\n=======================");
    System.out.println(" Element#: " + atom.getElementNumber());
    System.out.println(" Charge  : " + atom.getCharge());
    System.out.println(" Text    : " + (atom.getText() == null ? "" : atom.getText().getText().getText()));
    visitedAtoms++;
  }
  
}
