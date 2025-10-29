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
package org.beilstein.chemxtract.lookups;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

public class SMILESLookupReader {

  private static final Log logger = LogFactory.getLog(SMILESLookupReader.class);
  private final SmilesParser smilesParser;

  public SMILESLookupReader(IChemObjectBuilder builder) {
    this.smilesParser = new SmilesParser(builder);
  }

  /**
   * Imports the abbreviations and their corresponding SMILES from the file specified by the given
   * path
   *
   * @param path String path to the abbreviation file
   * @throws IOException if file could not be imported
   */
  public Map<String, String> loadSmilesLookup(String path, int initialSize) throws IOException {
    try (InputStream in = this.getClass().getResourceAsStream(path)) {
      if (in != null) return this.loadAbbreviations(in, initialSize);
      File file = new File(path);
      if (file.exists() && file.canRead())
        return this.loadAbbreviations(new FileInputStream(file), initialSize);
    }
    return Collections.emptyMap();
  }

  /**
   * Reads the given InputStream and adds the abbreviations and corresponding SMILES to a map. If a
   * SMILES could not be parsed it will be skipped and ignored.
   *
   * @param inputStream InputStream
   * @throws IOException if a line could not be read
   */
  private Map<String, String> loadAbbreviations(InputStream inputStream, int initialSize)
      throws IOException {
    Map<String, String> abbreviations = new HashMap<>(initialSize);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        try {
          this.addAbbreviationToMap(line, abbreviations);
        } catch (CDKException e) {
          logger.warn("Invalid SMILES", e);
        }
      }
    }
    return abbreviations;
  }

  /**
   * Splits the given line and add the abbreviation as a key and the corresponding SMILES as value
   * to a map. If a SMILES could not be parsed it will be skipped and ignored.
   *
   * @param line to be parsed
   * @throws CDKException if SMILES could not be parsed
   */
  private void addAbbreviationToMap(String line, Map<String, String> map) throws CDKException {
    String[] items = this.getLineItems(line);
    String abbr = items[0];
    IAtomContainer smilesStructure = this.smilesParser.parseSmiles(items[1]);
    String smiles = SmilesGenerator.unique().create(smilesStructure);
    map.putIfAbsent(abbr.toLowerCase(), smiles);
  }

  /**
   * Splits string at space or tab and returns the items as String array
   *
   * @param line String to split
   * @return String array with items
   */
  private String[] getLineItems(String line) {
    String[] items = new String[2];
    int lastIndex = 0;
    if (line.contains(" ")) {
      lastIndex = line.lastIndexOf(" ");
    }
    if (line.contains("\t")) {
      lastIndex = Math.max(line.lastIndexOf("\t"), lastIndex);
    }
    items[0] = line.substring(0, lastIndex).trim();
    items[1] = line.substring(lastIndex + 1).trim();
    return items;
  }
}
