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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.junit.Before;
import org.junit.Test;

public class FragmentVisitorTest {

  @Test
  public void test() throws IOException {
    String fileName = "nicknames.cdx";
    //    String fileName = "abbreviation.cdx";
    InputStream in = this.getClass().getResourceAsStream("/cdx/reader/" + fileName);
    assertThat(in).isNotNull();

    CDDocument document = CDXReader.readDocument(in);
    assertThat(document).isNotNull();

    List<CDPage> pages = document.getPages();

    FragmentVisitor fragmentVisitor = new FragmentVisitor(pages.get(0));

    System.out.println(fragmentVisitor.getFragments().size());
    System.out.println(fragmentVisitor.getAllFragments().size());
  }

  private CDPage pageMock;
  private CDFragment fragmentWithNoExternal;
  private CDFragment fragmentWithExternal;
  private CDAtom externalAtom;
  private CDAtom normalAtom;

  @Before
  public void setUp() {
    pageMock = mock(CDPage.class);

    fragmentWithNoExternal = mock(CDFragment.class);
    fragmentWithExternal = mock(CDFragment.class);

    externalAtom = mock(CDAtom.class);
    when(externalAtom.getNodeType()).thenReturn(CDNodeType.ExternalConnectionPoint);

    normalAtom = mock(CDAtom.class);
    when(normalAtom.getNodeType()).thenReturn(CDNodeType.Element);

    // fragments atoms setup
    when(fragmentWithNoExternal.getAtoms()).thenReturn(Collections.singletonList(normalAtom));
    when(fragmentWithExternal.getAtoms()).thenReturn(Arrays.asList(normalAtom, externalAtom));

    // page.accept(visitor) should call back into visitor.visitFragment(...)
    doAnswer(
            invocation -> {
              FragmentVisitor visitor = (FragmentVisitor) invocation.getArguments()[0];
              visitor.visitFragment(fragmentWithNoExternal);
              visitor.visitFragment(fragmentWithExternal);
              return null;
            })
        .when(pageMock)
        .accept(any(FragmentVisitor.class));
  }

  @Test
  public void fragmentsClassificationTest() {
    FragmentVisitor visitor = new FragmentVisitor(pageMock);

    // fragmentWithNoExternal should be in both lists
    assertTrue(visitor.getAllFragments().contains(fragmentWithNoExternal));
    assertTrue(visitor.getFragments().contains(fragmentWithNoExternal));

    // fragmentWithExternal should only be in allFragments
    assertTrue(visitor.getAllFragments().contains(fragmentWithExternal));
    assertFalse(visitor.getFragments().contains(fragmentWithExternal));

    // sizes
    assertEquals(2, visitor.getAllFragments().size());
    assertEquals(1, visitor.getFragments().size());
  }

  @Test
  public void emptyPageTest() {
    CDPage emptyPage = mock(CDPage.class);
    // page.accept(visitor) does nothing
    doAnswer(invocation -> null).when(emptyPage).accept(any(FragmentVisitor.class));

    FragmentVisitor visitor = new FragmentVisitor(emptyPage);

    assertTrue(visitor.getAllFragments().isEmpty());
    assertTrue(visitor.getFragments().isEmpty());
  }
}
