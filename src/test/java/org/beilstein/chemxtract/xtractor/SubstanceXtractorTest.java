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
package org.beilstein.chemxtract.xtractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.utils.MarkushHandler;
import org.junit.jupiter.api.Test;

/** Unit tests for the R-group expansion control flow in {@link SubstanceXtractor}. */
public class SubstanceXtractorTest {

  private static CDAtom atomAt(float x, float y) {
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.Element);
    atom.setPosition2D(new CDPoint2D(x, y));
    return atom;
  }

  private static CDAtom rGroupAt(float x, float y, String label) {
    CDStyledString styled = new CDStyledString();
    styled.addChunk(new CDStyledString.CDXChunk(null, 10f, null, null, label));
    CDText text = new CDText();
    text.setText(styled);

    CDAtom atom = atomAt(x, y);
    atom.setNodeType(CDNodeType.GenericNickname);
    atom.setText(text);
    return atom;
  }

  private static CDBond bond(CDAtom begin, CDAtom end) {
    CDBond bond = new CDBond();
    bond.setBegin(begin);
    bond.setEnd(end);
    return bond;
  }

  /**
   * A position-variation scaffold whose R-group expansion yields nothing must still emit its
   * enumerated scaffolds. Reporting the expansion as done regardless of its result suppressed the
   * unexpanded fallback and dropped the structures entirely.
   */
  @Test
  public void emptyRGroupExpansionStillEmitsScaffold() throws Exception {
    CDAtom c1 = atomAt(0f, 0f);
    CDAtom c2 = atomAt(10f, 0f);
    CDAtom residue = rGroupAt(5f, 20f, "R");
    CDAtom variableNode = new CDAtom();
    variableNode.setNodeType(CDNodeType.VariableAttachment);
    variableNode.setAttachedAtoms(List.of(c1, c2));
    variableNode.setPosition2D(new CDPoint2D(5f, 10f));

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(c1, c2, residue, variableNode));
    fragment.setBonds(List.of(bond(c1, c2), bond(variableNode, residue)));
    assertThat(fragment.hasRGroup()).isTrue();

    // The handler knows an R definition but resolves the scaffold to no structure at all.
    MarkushHandler handler = mock(MarkushHandler.class);
    when(handler.getResidueLabels()).thenReturn(Map.of("R", List.of("Me")));
    when(handler.replaceRGroups(any(), any())).thenReturn(List.of());

    List<BCXSubstance> substances =
        new SubstanceXtractor().xtractSubstances(fragment, new CDPage(), handler);

    // Two candidate attachment atoms -> two enumerated scaffolds, both kept despite the R-group
    // staying unresolved (position-variation scaffolds tolerate a missing InChI).
    assertThat(substances).hasSize(2);
    assertThat(substances).allSatisfy(s -> assertThat(s.getSmiles()).isNotEmpty());
  }
}
