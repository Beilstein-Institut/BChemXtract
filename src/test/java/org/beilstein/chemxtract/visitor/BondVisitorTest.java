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
package org.beilstein.chemxtract.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

// rationale: @BeforeEach setUp() stubs an 8-property shared fixture (bond + fragment +
// 2 atoms, each with NodeType / Fragments / getBegin / getEnd / getBonds / getAtoms),
// but the 7 @Test methods exercise different code paths through BondVisitor — each
// test reads a different subset of those stubs and at least one test (noBondsTest)
// uses a real CDFragment that bypasses the entire fixture. Per-stub lenient() across
// all 8 fixture stubs would be uniform clutter; class-level LENIENT is D-09 tier 3
// (3+ stubs needing leniency) and preserves the shared-fixture readability.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BondVisitorTest {

  private CDAtom atom1;
  private CDAtom atom2;
  private CDBond bond;
  private CDFragment fragment;

  @BeforeEach
  public void setUp() {
    atom1 = mock(CDAtom.class);
    atom2 = mock(CDAtom.class);
    bond = mock(CDBond.class);
    fragment = mock(CDFragment.class);

    when(bond.getBegin()).thenReturn(atom1);
    when(bond.getEnd()).thenReturn(atom2);
    when(fragment.getBonds()).thenReturn(Collections.singletonList(bond));
    when(fragment.getAtoms()).thenReturn(Arrays.asList(atom1, atom2));

    // default: both atoms are plain elements without fragments
    when(atom1.getNodeType()).thenReturn(CDNodeType.Element);
    when(atom2.getNodeType()).thenReturn(CDNodeType.Element);
    when(atom1.getFragments()).thenReturn(Collections.emptyList());
    when(atom2.getFragments()).thenReturn(Collections.emptyList());
  }

  @Test
  public void simpleBondAddedTest() {
    doAnswer(
            invocation -> {
              ((BondVisitor) invocation.getArguments()[0]).visitBond(bond);
              return null;
            })
        .when(fragment)
        .accept(any());

    BondVisitor visitor = new BondVisitor(fragment);
    assertEquals(1, visitor.getBonds().size());
    assertSame(bond, visitor.getBonds().get(0));
  }

  @Test
  public void nestedFragmentBeginSideTest() {
    // external connection point inside nested fragment
    CDAtom external = mock(CDAtom.class);
    CDAtom connected = mock(CDAtom.class);
    CDBond exBond = mock(CDBond.class);
    CDFragment nested = mock(CDFragment.class);

    when(external.getNodeType()).thenReturn(CDNodeType.ExternalConnectionPoint);
    when(exBond.getBegin()).thenReturn(external);
    when(exBond.getEnd()).thenReturn(connected);
    when(nested.getAtoms()).thenReturn(Collections.singletonList(external));
    when(nested.getBonds()).thenReturn(Collections.singletonList(exBond));

    when(atom1.getFragments()).thenReturn(Collections.singletonList(nested));

    doAnswer(
            invocation -> {
              ((BondVisitor) invocation.getArguments()[0]).visitBond(bond);
              return null;
            })
        .when(fragment)
        .accept(any());

    BondVisitor visitor = new BondVisitor(fragment);
    assertEquals(1, visitor.getBonds().size());
    verify(bond).setBegin(connected);
  }

  @Test
  public void nestedFragmentEndSideTest() {
    CDAtom external = mock(CDAtom.class);
    CDAtom connected = mock(CDAtom.class);
    CDBond exBond = mock(CDBond.class);
    CDFragment nested = mock(CDFragment.class);

    when(external.getNodeType()).thenReturn(CDNodeType.ExternalConnectionPoint);
    when(exBond.getBegin()).thenReturn(external);
    when(exBond.getEnd()).thenReturn(connected);
    when(nested.getAtoms()).thenReturn(Collections.singletonList(external));
    when(nested.getBonds()).thenReturn(Collections.singletonList(exBond));

    when(atom2.getFragments()).thenReturn(Collections.singletonList(nested));

    doAnswer(
            invocation -> {
              ((BondVisitor) invocation.getArguments()[0]).visitBond(bond);
              return null;
            })
        .when(fragment)
        .accept(any());

    BondVisitor visitor = new BondVisitor(fragment);
    assertEquals(1, visitor.getBonds().size());
    verify(bond).setEnd(connected);
  }

  @Test
  public void missingExternalConnectionPointThrowsTest() {
    CDFragment nested = mock(CDFragment.class);
    when(nested.getAtoms()).thenReturn(Collections.emptyList());
    when(atom1.getFragments()).thenReturn(Collections.singletonList(nested));

    doAnswer(
            invocation -> {
              ((BondVisitor) invocation.getArguments()[0]).visitBond(bond);
              return null;
            })
        .when(fragment)
        .accept(any());

    assertThrows(IllegalArgumentException.class, () -> new BondVisitor(fragment));
  }

  @Test
  public void noBondForExternalThrowsTest() {
    CDAtom external = mock(CDAtom.class);
    CDFragment nested = mock(CDFragment.class);

    when(external.getNodeType()).thenReturn(CDNodeType.ExternalConnectionPoint);
    when(nested.getAtoms()).thenReturn(Collections.singletonList(external));
    when(nested.getBonds()).thenReturn(Collections.emptyList());

    when(atom1.getFragments()).thenReturn(Collections.singletonList(nested));

    doAnswer(
            invocation -> {
              ((BondVisitor) invocation.getArguments()[0]).visitBond(bond);
              return null;
            })
        .when(fragment)
        .accept(any());

    assertThrows(IllegalArgumentException.class, () -> new BondVisitor(fragment));
  }

  @Test
  public void noBondsTest() {
    CDFragment realFragment = new CDFragment();
    // one atom is an element, the other is an abbreviation (not an element)
    when(atom1.getNodeType()).thenReturn(CDNodeType.Element);

    BondVisitor visitor = new BondVisitor(realFragment);
    assertTrue(visitor.getBonds().isEmpty()); // bond should not be collected
  }

  @Test
  public void visitBondIsAbbreviationAtBondTest() {
    CDAtom a1 = mock(CDAtom.class);
    CDAtom a2 = mock(CDAtom.class);
    when(a1.getNodeType()).thenReturn(CDNodeType.Unspecified);
    when(a1.getChemicalWarning()).thenReturn("ChemDraw can't interpret this label.");
    when(a2.getNodeType()).thenReturn(CDNodeType.Element);
    when(a1.getFragments()).thenReturn(Collections.emptyList());
    when(a2.getFragments()).thenReturn(Collections.emptyList());

    when(bond.getBegin()).thenReturn(a1);
    when(bond.getEnd()).thenReturn(a2);

    BondVisitor visitor = new BondVisitor(fragment);
    visitor.visitBond(bond);

    assertTrue(visitor.getBonds().contains(bond));
  }
}
