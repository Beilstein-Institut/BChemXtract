# HOWTO

This document contains some code snippets of how to use the libary in your own code.

## Extracting substances

```
FileInputStream in = new FileInputStream(filename);

// parse CDX into in-memory model
CDDocument document = CDXReader.readDocument(in);

// extract substances from CDX document
BCXSubstanceInfo info = new BCXSubstanceInfo();
List<BCXSubstance> bcxSubstances = ChemInfConverterUtils.getUniqueArticleSubstancesOfCdDocument(document, info, false);

// use CDK to generate a structure depiction, save as PNG output
SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
for (BCXSubstance bcxSubstance : bcxSubstances) {
  String outputfile = bcxSubstance.getInchiKey() + ".png";
  FileOutputStream fos = new FileOutputStream(outputfile);
  IAtomContainer container = sp.parseSmiles(bcxSubstance.getSmiles());
  DepictionGenerator dg = new DepictionGenerator().withAtomColors().withFillToFit().withBackgroundColor(Color.WHITE);
  Depiction d = dg.depict(container);
  d.writeTo(Depiction.PNG_FMT, fos);
  fos.flush();
  fos.close();
}
```

## Extracting substances with original coordinates
In addition to the example above, the original coordinates from the drawing are retained in the depiction instead
of the structure being auto-layouted.

```
FileInputStream in = new FileInputStream(filename);

// parse CDX into in-memory model
CDDocument document = CDXReader.readDocument(in);

// extract substances from CDX document
BCXSubstanceInfo info = new BCXSubstanceInfo();
List<BCXSubstance> bcxSubstances = ChemInfConverterUtils.getUniqueArticleSubstancesOfCdDocument(document, info, false);

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
```

## Extracting substances with abbreviations
In addition to the example above, common abbreviations are collapsed if they were found during extraction 

```
FileInputStream in = new FileInputStream(filename);

// parse CDX into in-memory model
CDDocument document = CDXReader.readDocument(in);

// extract substances from CDX document
BCXSubstanceInfo info = new BCXSubstanceInfo();
List<BCXSubstance> bcxSubstances = ChemInfConverterUtils.getUniqueArticleSubstancesOfCdDocument(document, info, false);

// use CDK to generate a structure depiction, save as PNG output
SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
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
}
```
