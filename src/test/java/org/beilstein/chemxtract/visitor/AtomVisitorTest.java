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

import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AtomVisitorTest {

  private void mockDeepText(CDAtom atom, String value) {
    var t1 = mock(CDText.class);
    var t2 = mock(CDStyledString.class);
    when(atom.getText()).thenReturn(t1);
    when(t1.getText()).thenReturn(t2);
    when(t2.getText()).thenReturn(value);
  }

  @Test
  public void constructorTriggersVisitTest() {
    CDFragment fragment = mock(CDFragment.class);
    // AtomVisitor calls fragment.accept(this)
    new AtomVisitor(fragment);
    verify(fragment).accept(any(AtomVisitor.class));
  }

  @Test
  public void visitAtomAddsNicknameTest() {
    CDAtom atom = mock(CDAtom.class);
    CDFragment frag = mock(CDFragment.class);
    when(atom.getFragments()).thenReturn(List.of(frag));
    mockDeepText(atom, "Nick");

    AtomVisitor visitor = new AtomVisitor(mock(CDFragment.class));
    visitor.visitAtom(atom);

    assertEquals(frag, visitor.getNicknames().get("Nick"));
    assertTrue("nickname atoms should not be added to atoms list", visitor.getAtoms().isEmpty());
  }

  @Test
  public void visitAtomNicknamePutIfAbsentTest() {
    CDFragment frag1 = mock(CDFragment.class);
    CDFragment frag2 = mock(CDFragment.class);

    CDAtom atom1 = mock(CDAtom.class);
    when(atom1.getFragments()).thenReturn(List.of(frag1));
    mockDeepText(atom1, "Dup");

    CDAtom atom2 = mock(CDAtom.class);
    when(atom2.getFragments()).thenReturn(List.of(frag2));
    mockDeepText(atom2, "Dup");

    AtomVisitor visitor = new AtomVisitor(mock(CDFragment.class));
    visitor.visitAtom(atom1);
    visitor.visitAtom(atom2);

    // Should keep frag1, not overwrite with frag2
    assertEquals(frag1, visitor.getNicknames().get("Dup"));
  }

  @Test
  public void visitAtomAddsAbbreviationTest() {
    CDAtom atom = mock(CDAtom.class);
    when(atom.getFragments()).thenReturn(List.of()); // empty
    when(atom.getNodeType()).thenReturn(CDNodeType.Unspecified); // not Element
    mockDeepText(atom, "Abbr");

    AtomVisitor visitor = new AtomVisitor(mock(CDFragment.class));
    visitor.visitAtom(atom);

    assertTrue(visitor.getAbbreviations().contains("Abbr"));
    assertTrue(visitor.getAtoms().contains(atom));
  }

  @Test
  public void visitAtomAddsElementTest() {
    CDAtom atom = mock(CDAtom.class);
    when(atom.getFragments()).thenReturn(List.of());
    when(atom.getNodeType()).thenReturn(CDNodeType.Element);

    AtomVisitor visitor = new AtomVisitor(mock(CDFragment.class));
    visitor.visitAtom(atom);

    assertTrue(visitor.getAtoms().contains(atom));
    assertTrue(visitor.getAbbreviations().isEmpty());
    assertTrue(visitor.getNicknames().isEmpty());
  }

  @Test
  public void visitAtomOtherTypeWithNullTextTest() {
    CDAtom atom = mock(CDAtom.class);
    when(atom.getFragments()).thenReturn(List.of());
    when(atom.getNodeType()).thenReturn(CDNodeType.Unspecified);
    when(atom.getText()).thenReturn(null); // avoid abbreviation branch

    AtomVisitor visitor = new AtomVisitor(mock(CDFragment.class));
    visitor.visitAtom(atom);

    assertTrue(visitor.getAtoms().isEmpty());
    assertTrue(visitor.getAbbreviations().isEmpty());
    assertTrue(visitor.getNicknames().isEmpty());
  }
}
