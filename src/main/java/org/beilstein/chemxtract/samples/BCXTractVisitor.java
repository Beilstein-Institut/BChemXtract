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
    if (args.length != 1) {
      System.err.println("Input CDX file must be given as argument.");
      System.exit(1);
    }
    BCXTractVisitor bcx = new BCXTractVisitor();
    bcx.work(args[0]);
  }

  public void work(String filename) throws Exception {
    // load input CDX file
    InputStream in = new FileInputStream(filename);

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
