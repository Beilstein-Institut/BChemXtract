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
package org.beilstein.chemxtract.cdx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CDFragment}. */
public class CDFragmentTest {

  private static CDAtom labelled(String label) {
    CDStyledString styled = new CDStyledString();
    styled.addChunk(new CDStyledString.CDXChunk(null, 10f, null, null, label));
    CDText text = new CDText();
    text.setText(styled);
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.GenericNickname);
    atom.setText(text);
    return atom;
  }

  /** Primed labels are distinct, repeats count once, and plain atoms and abbreviations add none. */
  @Test
  public void rGroupLabelsAreTheDistinctLabelsInDrawingOrder() {
    CDAtom carbon = new CDAtom();
    carbon.setNodeType(CDNodeType.Element);
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(
        List.of(
            labelled("R''"),
            carbon,
            labelled("R'"),
            labelled("OMe"),
            labelled("R'"),
            labelled("R'''")));

    assertThat(fragment.getRGroupLabels()).containsExactly("R''", "R'", "R'''");
  }
}
