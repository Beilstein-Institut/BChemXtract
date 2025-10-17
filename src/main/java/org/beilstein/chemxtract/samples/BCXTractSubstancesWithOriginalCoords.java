package org.beilstein.chemxtract.samples;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * A simple showcase for substance extraction. Takes a CDX file as input. If none given, loads
 * the default test fixture from resources. Reads the CDX and extracts substances. Converts
 * each found substance to PNG and saves the resulting file to the working directory. The calculated 
 * InChI key is the filename of the result files.
 */
public class BCXTractSubstancesWithOriginalCoords {

  public static void main(String[] args) throws Exception {
    BCXTractSubstancesWithOriginalCoords bcx = new BCXTractSubstancesWithOriginalCoords();
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

    // extract substances from CDX document
    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor = new SubstanceXtractor();
    List<BCXSubstance> bcxSubstances = xtractor.xtractUnique(document, info, false);

    // use CDK to generate a structure depiction with the original coordinates, save as PNG output
    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
    for (BCXSubstance bcxSubstance : bcxSubstances) {
      String outputfile = bcxSubstance.getInchiKey() + "-original-coords.png";
      FileOutputStream fos = new FileOutputStream(outputfile);

      // use extended smiles that carries coordinates
      IAtomContainer container = sp.parseSmiles(bcxSubstance.getExtendedSmiles());

      // set stereo information not contained in extended smiles
      StructureDiagramGenerator sdg = new StructureDiagramGenerator();
      sdg.setMolecule(container);
      sdg.generateCoordinates();
      IAtomContainer structureDiagram = sdg.getMolecule();
      Iterator<IBond> sdgBonds = structureDiagram.bonds().iterator();
      Iterator<IBond> acBonds = container.bonds().iterator();
      while (sdgBonds.hasNext()) {
        IBond sdgBond = sdgBonds.next();
        IBond acBond = acBonds.next();
        acBond.setDisplay(sdgBond.getDisplay());
      }

      // render depiction
      String title = bcxSubstance.getMolecularFormula() + " (InChI Key: " + bcxSubstance.getInchiKey() + ")";
      container.setTitle(title);
      DepictionGenerator dg = new DepictionGenerator().withAtomColors().withFillToFit().withBackgroundColor(Color.WHITE).withMolTitle();
      Depiction d = dg.depict(container);
      d.writeTo(Depiction.PNG_FMT, fos);
      fos.flush();
      fos.close();
    }
  }

}
