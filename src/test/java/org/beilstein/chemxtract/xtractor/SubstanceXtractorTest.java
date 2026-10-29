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

import java.util.ArrayList;
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
import org.beilstein.chemxtract.utils.Definitions;
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

  private static CDFragment carbonChain(int length) {
    List<CDAtom> atoms = new ArrayList<>();
    List<CDBond> bonds = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      atoms.add(atomAt(i * 10f, (i % 2) * 5f));
      if (i > 0) {
        bonds.add(bond(atoms.get(i - 1), atoms.get(i)));
      }
    }
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(atoms);
    fragment.setBonds(bonds);
    return fragment;
  }

  /**
   * InChI is only generated up to {@link Definitions#MAX_ATOM_COUNT} atoms. A larger structure
   * would have no InChI, and every extracted substance must carry one, so it is skipped.
   */
  @Test
  public void structureAboveAtomLimitIsSkipped() throws Exception {
    List<BCXSubstance> atLimit =
        new SubstanceXtractor()
            .xtractSubstances(carbonChain(Definitions.MAX_ATOM_COUNT), new CDPage(), null);
    assertThat(atLimit).as("a structure at the limit is extracted").hasSize(1);
    assertThat(atLimit.getFirst().getInchi()).as("it carries an InChI").isNotBlank();

    List<BCXSubstance> aboveLimit =
        new SubstanceXtractor()
            .xtractSubstances(carbonChain(Definitions.MAX_ATOM_COUNT + 1), new CDPage(), null);
    assertThat(aboveLimit).as("a structure above the limit has no InChI").isEmpty();
  }

  /**
   * A position-variation scaffold whose R-group expansion yields nothing keeps its unresolved
   * R-group, so it has no InChI and must not be emitted. Every extracted substance carries a valid
   * InChI.
   */
  @Test
  public void unresolvedPositionVariationScaffoldIsSkipped() throws Exception {
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

    assertThat(substances).as("unresolved scaffolds have no InChI").isEmpty();
  }
}
