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

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.openscience.cdk.depict.Abbreviations;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * A simple showcase for substance extraction. Takes a CDX file as input. Reads the CDX and extracts substances. 
 * Converts each found substance to PNG and saves the resulting file to the working directory. The calculated 
 * InChI key is the filename of the result files.
 */
public class BCXTractSubstancesWithAbbreviations {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Input CDX file must be given as argument.");
      System.exit(1);
    }
    BCXTractSubstancesWithAbbreviations bcx = new BCXTractSubstancesWithAbbreviations();
    bcx.extract(args[0]);
  }

  public void extract(String filename) throws Exception {
    // load input CDX file
    InputStream in = new FileInputStream(filename);

    // parse CDX into in-memory model
    CDDocument document = CDXReader.readDocument(in);

    // extract substances from CDX document
    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor();
    List<BCXSubstance> bcxSubstances = xtractor.xtractUnique(document, info, false);

    // use CDK to generate a structure depiction with collapsed abbreviations, save as PNG output
    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
    int i=0;
    for (BCXSubstance bcxSubstance : bcxSubstances) {
      String outputfile = bcxSubstance.getInchiKey() + "-abbreviations.png";
      FileOutputStream fos = new FileOutputStream(outputfile);

      // use smiles without coordinates, auto layout
      IAtomContainer container = sp.parseSmiles(bcxSubstance.getSmiles());

      // if abbreviations where used in the structure, apply them to the atom container
      Map<String,String> abbreviations = bcxSubstance.getAbbreviations();
      Abbreviations abb = new Abbreviations();
      if (abbreviations != null && !abbreviations.isEmpty()) {
        for (String abbSmiles : abbreviations.keySet()) {
          String abbLabel = abbreviations.get(abbSmiles);
          abb.add(abbSmiles + " " + abbLabel);
        }
        abb.apply(container);
      }

      // render depiction
      String title = bcxSubstance.getMolecularFormula() + " (InChI Key: " + bcxSubstance.getInchiKey() + ")";
      container.setTitle(title);
      DepictionGenerator dg = new DepictionGenerator().withAtomColors().withFillToFit().withBackgroundColor(Color.WHITE).withMolTitle();
      Depiction d = dg.depict(container);
      d.writeTo(Depiction.PNG_FMT, fos);
      fos.flush();
      fos.close();
      i++;
    }

    System.out.println("\n\n" + i + " substances extracted to current working directory.");
  }

}
