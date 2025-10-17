package org.beilstein.chemxtract.samples;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXReaction;
import org.beilstein.chemxtract.xtractor.ReactionXtractor;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * A simple showcase for reaction extraction. Takes a CDX file as input. If none given, loads
 * the default test fixture from resources. Reads the CDX and extracts reactions. Converts
 * each found reaciton to PNG and saves the resulting file to the working directory. The calculated 
 * RInChI Web key is the filename of the result files.
 */
public class BCXTractReactions {

  public static void main(String[] args) throws Exception {
    BCXTractReactions bcx = new BCXTractReactions();
    bcx.extract(args.length > 0 ? args[0] : null);
  }

  public void extract(String filename) throws Exception {
    // load input CDX file - either from command-line args or resources
    InputStream in = null;
    if (filename == null) {
      in = getClass().getResourceAsStream("/org/beilstein/chemxtract/tools/test_fixture.cdx");
    } else {
      in = new FileInputStream(filename);
    }

    // parse CDX into in-memory model
    CDDocument document = CDXReader.readDocument(in);

    // extract reactions from CDX document
    ReactionXtractor xtractor = new ReactionXtractor();
    List<BCXReaction> bcxReactions = xtractor.xtract(document);

    // use CDK to generate a reaction depiction, save as PNG output
    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
    for (BCXReaction bcxReaction : bcxReactions) {
      String outputfile = bcxReaction.getWebRinchiKey() + ".png";
      FileOutputStream fos = new FileOutputStream(outputfile);
      IReaction reaction = sp.parseReactionSmiles(bcxReaction.getReactionSmiles());
      DepictionGenerator dg = new DepictionGenerator().withAtomColors().withFillToFit().withBackgroundColor(Color.WHITE);
      Depiction d = dg.depict(reaction);
      d.writeTo(Depiction.PNG_FMT, fos);
      fos.flush();
      fos.close();
    }
  }

}
