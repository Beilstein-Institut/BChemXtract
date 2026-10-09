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
package org.beilstein.chemxtract.integrationTests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXReactionInfo;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.ReactionXtractor;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Extraction must leave the parsed document as it found it, so that a second extraction over the
 * same {@link CDDocument} — with either xtractor — returns what a fresh parse would.
 */
public class RepeatedExtractionTest {

  private static CDDocument read(String resource) throws IOException {
    InputStream in = RepeatedExtractionTest.class.getResourceAsStream(resource);
    assertThat(in).as(resource).isNotNull();
    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).as(resource).isNotNull();
    return document;
  }

  private static List<String> substanceKeys(CDDocument document, boolean resolveRGroups) {
    return new SubstanceXtractor()
        .xtractUnique(document, new BCXSubstanceInfo(), resolveRGroups).stream()
            .map(s -> s.getInchiKey() + " " + s.getSmiles())
            .sorted()
            .toList();
  }

  private static List<String> reactionKeys(CDDocument document) {
    return new ReactionXtractor()
        .xtract(document, new BCXReactionInfo()).stream()
            .map(r -> r.getRinchiKey() + " " + r.getReactionSmiles())
            .sorted()
            .toList();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/integrationTests/sgroups/multipleGroups.cdx",
        "/integrationTests/sgroups/nonane_MUL2.cdx",
        "/cheminf/bugs/m2490959-i1.cdx"
      })
  public void secondSubstanceExtractionMatchesFirst(String resource) throws Exception {
    CDDocument document = read(resource);

    List<String> first = substanceKeys(document, true);
    List<String> second = substanceKeys(document, true);

    assertThat(first).as("first pass").isNotEmpty();
    assertThat(second).as("second pass over the same document").isEqualTo(first);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/integrationTests/reaction.cdx", "/cheminf/bugs/m2490959-i1.cdx"})
  public void reactionsAfterSubstancesMatchAFreshParse(String resource) throws Exception {
    List<String> fresh = reactionKeys(read(resource));

    CDDocument document = read(resource);
    substanceKeys(document, false);

    assertThat(reactionKeys(document)).as("reactions after substances").isEqualTo(fresh);
  }
}
