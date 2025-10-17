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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Singleton utility class that provides access to a predefined set of
 * unwanted or excluded words used for text and label filtering.
 * <p>
 *
 * <h2>Purpose</h2>
 * This class is used to filter out common or 'non-chemical' words
 * (e.g., "step", "reaction", "mixture", etc.) when parsing text near reaction
 * arrows or in fragment annotations.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * if (!UnwantedWords.contains("solution")) {
 *     // treat as potential chemical label
 * }
 */
public class UnwantedWords {

  private static UnwantedWords instance;
  private final Set<String> words;

  /**
   * Private constructor that initializes the unwanted word set.
   *
   * @throws IOException if the resource file cannot be found or read
   */
  private UnwantedWords() throws IOException {
    words = loadUnwantedWords();
  }

  /**
   * Returns the singleton instance of {@link UnwantedWords},
   * creating it on first access if necessary.
   *
   * @return the singleton instance
   * @throws IOException if the unwanted words file cannot be loaded
   */
  private static UnwantedWords getInstance() throws IOException {
    if (instance == null)
      instance = new UnwantedWords();
    return instance;
  }

  /**
   * Loads the unwanted word list from the {@code unwantedWords.txt} resource file.
   * <p>
   * Each non-blank line in the file is added to a set, which is then made
   * unmodifiable for safe concurrent access.
   * </p>
   *
   * @return an unmodifiable set of unwanted words
   * @throws IOException if the resource cannot be found or read
   */
  private static Set<String> loadUnwantedWords() throws IOException {
    Set<String> words = new HashSet<>(500);
    InputStream in = UnwantedWords.class.getResourceAsStream("unwantedWords.txt");
    if (in == null) {
      throw new IOException("unwantedWords.txt not found on classpath");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (!line.isBlank()) {
        words.add(line);
      }
    }
    return Collections.unmodifiableSet(words);
  }

  /**
   * Returns the full set of unwanted words.
   *
   * @return an unmodifiable {@link Set} of unwanted words
   * @throws IOException if the word list cannot be loaded
   */
  public static Set<String> getUnwantedWords() throws IOException {
    return UnwantedWords.getInstance().words;
  }

  /**
   * Checks whether the given word is part of the unwanted word list.
   *
   * @param word the word to check
   * @return {@code true} if the word is in the unwanted word list, {@code false} otherwise
   * @throws IOException if the word list cannot be initialized or loaded
   */
  public static boolean contains(String word) throws IOException {
    return UnwantedWords.getInstance().words.contains(word);
  }
}
