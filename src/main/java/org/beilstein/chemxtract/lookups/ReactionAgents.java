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
 * Singleton utility class that provides a lookup table for chemical reaction agents, mapping common
 * textual abbreviations (e.g., "THF", "Acetic Acid") to their corresponding SMILES representations.
 *
 * <p>This class loads a predefined SMILES abbreviation list from a file at initialization, allowing
 * consistent access to standardized agent definitions throughout the application.
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * if (ReactionAgents.contains("EtOH")) {
 *     String smiles = ReactionAgents.get("EtOH");  // returns "CCO"
 *     IAtomContainer ethanol = smilesParser.parseSmiles(smiles);
 * }
 * }</pre>
 */
public class ReactionAgents {

  private static ReactionAgents instance;
  private final Map<String, String> lookup;

  /**
   * Private constructor that loads the SMILES lookup table from the defined resource file. The
   * lookup is read using a {@link SMILESLookupReader} and stored as a lowercase key-to-SMILES
   * mapping.
   *
   * @throws IOException if the SMILES abbreviation file cannot be read or parsed
   */
  private ReactionAgents() throws IOException {
    SMILESLookupReader reader = new SMILESLookupReader(DefaultChemObjectBuilder.getInstance());
    this.lookup =
        reader.loadSmilesLookup(Definitions.AGENT_ABBREVIATION_PATH, Definitions.AGENTS_SIZE);
  }

  /**
   * Returns the singleton instance of the {@code ReactionAgents} class.
   *
   * <p>
   *
   * @return the singleton instance
   * @throws IOException if the SMILES abbreviation file cannot be loaded
   */
  private static ReactionAgents getInstance() throws IOException {
    if (instance == null) instance = new ReactionAgents();
    return instance;
  }

  /**
   * Retrieves the SMILES representation associated with the given agent abbreviation.
   *
   * <p>If the abbreviation is not found, {@code null} is returned.
   *
   * @param key the agent abbreviation (e.g., "EtOH", "H2O2")
   * @return the corresponding SMILES string, or {@code null} if not found
   * @throws IOException if the lookup table could not be loaded
   */
  public static String get(String key) throws IOException {
    return ReactionAgents.getInstance().lookup.get(key.toLowerCase());
  }

  /**
   * Checks whether the given abbreviation is defined in the agent lookup table.
   *
   * <p>
   *
   * @param key the agent abbreviation to test
   * @return {@code true} if the abbreviation is known, {@code false} otherwise
   * @throws IOException if the lookup table could not be loaded
   */
  public static boolean contains(String key) throws IOException {
    return ReactionAgents.getInstance().lookup.containsKey(key.toLowerCase());
  }
}
