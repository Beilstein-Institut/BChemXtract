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

import java.io.IOException;
import java.util.Map;
import org.beilstein.chemxtract.utils.Definitions;
import org.openscience.cdk.DefaultChemObjectBuilder;

/**
 * Singleton utility class providing access to a lookup table of common chemical abbreviations and
 * their corresponding SMILES representations.
 *
 * <p>This class centralizes abbreviation handling for standard molecular fragments (e.g., "Ph",
 * "Bn", "tBu") by reading them from a predefined SMILES abbreviation file. The lookup enables
 * uniform and reproducible conversion of shorthand notations to chemical structures across the
 * application.
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * if (SmilesAbbreviations.contains("Ph")) {
 *     String smiles = SmilesAbbreviations.get("Ph");  // returns "c1ccccc1"
 *     IAtomContainer phenyl = smilesParser.parseSmiles(smiles);
 * }
 * }</pre>
 */
public class SmilesAbbreviations {

  private static SmilesAbbreviations instance;
  private final Map<String, String> smilesLookup;

  /**
   * Private constructor that initializes the SMILES abbreviation lookup table.
   *
   * <p>The lookup is loaded from a resource file defined in {@link Definitions#ABBREVIATION_PATH}
   * and limited to a maximum size given by {@link Definitions#ABBREVIATION_SIZE}.
   *
   * @throws IOException if the abbreviation file cannot be read or parsed
   */
  private SmilesAbbreviations() throws IOException {
    SMILESLookupReader reader = new SMILESLookupReader(DefaultChemObjectBuilder.getInstance());
    this.smilesLookup =
        reader.loadSmilesLookup(Definitions.ABBREVIATION_PATH, Definitions.ABBREVIATION_SIZE);
  }

  /**
   * Returns the singleton instance of this class, creating it on first access.
   *
   * @return the singleton instance of {@code SmilesAbbreviations}
   * @throws IOException if the abbreviation file cannot be loaded
   */
  private static SmilesAbbreviations getInstance() throws IOException {
    if (instance == null) instance = new SmilesAbbreviations();
    return instance;
  }

  /**
   * Retrieves the SMILES string corresponding to the given chemical abbreviation.
   *
   * @param key the chemical abbreviation (e.g., "Ph", "Me", "tBu")
   * @return the corresponding SMILES string, or {@code null} if not found
   * @throws IOException if the lookup table cannot be initialized
   */
  public static String get(String key) throws IOException {
    return SmilesAbbreviations.getInstance().smilesLookup.get(key.toLowerCase());
  }

  /**
   * Checks whether the given abbreviation exists in the SMILES lookup table.
   *
   * @param key the abbreviation to check (e.g., "Et", "Ph")
   * @return {@code true} if the abbreviation exists, {@code false} otherwise
   * @throws IOException if the lookup table cannot be initialized
   */
  public static boolean contains(String key) throws IOException {
    return SmilesAbbreviations.getInstance().smilesLookup.containsKey(key.toLowerCase());
  }

  /**
   * Return complete Abbreviation SMILES lookup map
   *
   * @return the lookup table
   * @throws IOException if the lookup table cannot be initialized
   */
  public static Map<String, String> getSmilesLookup() throws IOException {
    return SmilesAbbreviations.getInstance().smilesLookup;
  }
}
