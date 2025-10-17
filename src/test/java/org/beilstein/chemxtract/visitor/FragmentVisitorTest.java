package org.beilstein.chemxtract.visitor;

import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDPage;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    doAnswer(invocation -> {
      FragmentVisitor visitor = (FragmentVisitor) invocation.getArguments()[0];
      visitor.visitFragment(fragmentWithNoExternal);
      visitor.visitFragment(fragmentWithExternal);
      return null;
    }).when(pageMock).accept(any(FragmentVisitor.class));
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