# BChemXtract

## A pure-Java extractor of ChemDraw structures and reactions
Welcome to **BChemXtract** – an open-source Java parser for ChemDraw files and structures and reactions extraction software, originally developed for the Beilstein
Diamond Open Access Journals.

This open-science repository aims to provide a pure-Java implementation for reading ChemDraw files (binary and XML) as well as extracting and validating 
meaningful chemical structures and reactions from the parsed file. Structures and reactions are enriched with calculated chemical descriptors such as InChI and SMILES
(or RInChI and reaction smiles respectively).

**Please note**, that while the structure extraction part is relatively mature, the reaction extraction is still in an experimental stage.

## ChemDraw file specification
The implementation of the parser was originally developed against the ChemDraw file specification that was published online for years at www.cambridgesoft.com.
The website is offline nowadays, however the specification can still be browsed here:
- [St. Olaf College](https://chemapps.stolaf.edu/iupac/cdx/sdk/index.htm)
- [Wayback Machine](https://web.archive.org/web/20160225200221/http://www.cambridgesoft.com/services/documentation/sdk/chemdraw/cdx/)

## Getting started
Some concepts of the parser and extraction software are given in the [documentation](doc/CONCEPTS.md). Simple code snippets are given on the [HOWTO page](doc/HOWTO.md).

### Building using Maven
Clone this repository, change to the project directory and type:

```
mvn clean package
```

This will compile the sources and create two artifacts below your target directory:
* bchemxtract-<x.y>.jar: A jar file containing just the compiled classes from this project. This can be used to be packaged with your software.
* bchemxtract-<x.y>-jar-with-dependencies.jar: A jar file containing all required libraries. This can be used to be run standalone. 

### Installing using Maven
To use this library in your own project, add this dependency to your project's POM file:

```
<dependency>
  <groupId>org.beilstein</groupId>
  <artifactId>bchemxtract</artifactId>
  <version>1.0</version>
</dependency>
```

## Usage
To extract all structures from a CDX file and save the structures as PNG to the current working directory, type:

```
java -jar target/bchemxtract-<x.y>-jar-with-dependencies.jar <CDX file>
```

## Contributing
We believe the best resource is a community-built one, so you are welcome to contribute.
To contribute:
1. [Fork](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/fork-a-repo) this repository 
2. Add or update files
3. Submit a [pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request) 

## License
All data in this repository is released under the **[MIT License](https://en.wikipedia.org/wiki/MIT_License)**. Use it freely in research, software, education, or commercial applications.

## Acknowledgments
This project is inspired by the needs of the cheminformatics community, and we thank the many open-source projects that aim to make chemical knowledge more [FAIR (Findable, Accessible, Interoperable, Reusable)](https://www.go-fair.org/fair-principles/). 

The code makes extensive use of the [Chemistry Development Toolkit](https://cdk.github.io/) which is licensed under the [Gnu Lesser Public License](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html). Please check the following publications for background information:

``` 
Willighagen et al. The Chemistry Development Kit (CDK) v2.0: atom typing, depiction, molecular formulas, 
and substructure searching. J. Cheminform. 2017; 9(3), doi:10.1186/s13321-017-0220-4

May and Steinbeck. Efficient ring perception for the Chemistry Development Kit. J. Cheminform. 2014, 
doi:10.1186/1758-2946-6-3

Steinbeck et al. Recent Developments of the Chemistry Development Kit (CDK) - An Open-Source Java Library 
for Chemo- and Bioinformatics. Curr. Pharm. Des. 2006; 12(17):2111-2120, doi:10.2174/138161206777585274 
(free green Open Acccess version)

Steinbeck et al. The Chemistry Development Kit (CDK): An Open-Source Java Library for Chemo- and Bioinformatics. 
J. Chem. Inf. Comput. Sci. 2003 Mar-Apr; 43(2):493-500, doi:10.1021/ci025584y
```

## Feedback? 
Open an issue or contact the maintainers: **open-source@beilstein-institut.de**.
