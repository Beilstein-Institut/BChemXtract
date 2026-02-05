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
package org.beilstein.chemxtract.io;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XMLObjectTest {

  XMLObject obj = new XMLObject();
  Object instance = new Object();
  XMLObject child = new XMLObject();
  XMLObject grandchild = new XMLObject();

  String xml;

  @Before
  public void setUp() throws Exception {

    StringBuilder tmp = new StringBuilder();
    tmp.append("<?xml version=\"1.0\" encoding=\"ASCII\"?><root attname1=\"attval1\" attname2=\"1\" attname3=\"false\" attname31=\"yes\" attname4=\"3.14\" attname5=\"1 2 3\" attname6=\"a b c\" attname61=\"a,b,c\" attname7=\"dummy\">").append(System.lineSeparator());
    tmp.append("    text1").append(System.lineSeparator());
    tmp.append("    <child>").append(System.lineSeparator());
    tmp.append("        child text").append(System.lineSeparator());
    tmp.append("        <grandchild>grandchild text</grandchild>").append(System.lineSeparator());
    tmp.append("        child text").append(System.lineSeparator());
    tmp.append("    </child>").append(System.lineSeparator());
    tmp.append("    text1").append(System.lineSeparator());
    tmp.append("</root>").append(System.lineSeparator());
    xml = tmp.toString();
    
    obj = new XMLObject();
    obj.setName("root");
    obj.setInstance(instance);
    obj.setLineNumber(2);
    obj.setColumnNumber(1);
    obj.setTexts(Arrays.asList("text1", "text2", "text3"));
    obj.setAttribute("attname1", "attval1");
    obj.setAttribute("attname2", "1");
    obj.setAttribute("attname3", "false");
    obj.setAttribute("attname31", "yes");
    obj.setAttribute("attname4", "3.14");
    obj.setAttribute("attname5", "1 2 3");
    obj.setAttribute("attname6", "a b c");
    obj.setAttribute("attname61", "a,b,c");
    obj.setAttribute("attname7", "dummy");
    obj.setObjects(Arrays.asList(child));
    child.setName("child");
    child.setTexts(Arrays.asList("child text"));
    grandchild.setName("grandchild");
    grandchild.setTexts(Arrays.asList("grandchild text"));
    obj.setObjects(Arrays.asList(child));
    child.setObjects(Arrays.asList(grandchild));
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testBasic() {
    assertThat(obj.getName()).isEqualTo("root");
    assertThat(obj.getLineNumber()).isEqualTo(2);
    assertThat(obj.getColumnNumber()).isEqualTo(1);
    assertThat(obj.getInstance()).isEqualTo(instance);
  }

  @Test
  public void testTexts() throws IOException {
    assertThat(obj.getTexts()).isEqualTo(Arrays.asList("text1", "text2", "text3"));
    assertThat(obj.getTextsAsString()).isEqualTo("text1text2text3");
    try {
      obj.getTextsAsInt();
      fail("No exception");
    } catch (Exception e) {
    }
    obj.setTexts(Arrays.asList("1", "2", "3"));
    assertThat(obj.getTextsAsInt()).isEqualTo(123);
  }

  @Test
  public void testObjects() {
    assertThat(obj.getObjects("grandchild")).isEmpty();
    assertThat(obj.getObjects("child")).isNotEmpty();
    assertThat(obj.getAllObjects("grandchild")).isEqualTo(Arrays.asList(grandchild));
    assertThat(obj.getObjects()).isEqualTo(Arrays.asList(child));
    assertThat(obj.getNumberOfObjects(false)).isEqualTo(1);
    assertThat(obj.getNumberOfObjects(true)).isEqualTo(2);
    assertThat(obj.getDepth()).isEqualTo(3);
    assertThat(obj.hasObjects("child")).isTrue();
    assertThat(obj.hasObjects("grandchild")).isFalse();
    assertThat(obj.getFirstObject("child")).isEqualTo(child);
    assertThat(obj.getFirstObject("grandchild")).isNull();
  }

  @Test
  public void testAttributes() throws IOException {
    assertThat(obj.getAttributes().size()).isEqualTo(9);
    obj.removeAttribute("attname7");
    assertThat(obj.getAttributes().size()).isEqualTo(8);
    assertThat(obj.getAttribute("attname1")).isEqualTo("attval1");
    assertThat(obj.getAttribute("attname1", "default")).isEqualTo("attval1");
    assertThat(obj.getAttribute("attname10", "default")).isEqualTo("default");
    assertThat(obj.getAttributeAsBoolean("attname3")).isFalse();
    assertThat(obj.getAttributeAsBoolean("attname31")).isTrue();
    assertThat(obj.getAttributeAsBoolean("attname3", true)).isFalse();
    assertThat(obj.getAttributeAsBoolean("attname10", true)).isTrue();
    try {
      obj.getAttributeAsBoolean("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsInt("attname2")).isEqualTo(1);
    assertThat(obj.getAttributeAsInt("attname2", 12)).isEqualTo(1);
    assertThat(obj.getAttributeAsInt("attname10", 12)).isEqualTo(12);
    try {
      obj.getAttributeAsInt("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsLong("attname2")).isEqualTo(1L);
    try {
      obj.getAttributeAsLong("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsFloat("attname4")).isEqualTo(3.14F);
    try {
      obj.getAttributeAsFloat("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsDouble("attname4")).isEqualTo(3.14D);
    try {
      obj.getAttributeAsDouble("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsStringList("attname6")).isEqualTo(Arrays.asList("a", "b", "c"));
    assertThat(obj.getAttributeAsStringList("attname61", ","))
        .isEqualTo(Arrays.asList("a", "b", "c"));
    assertThat(obj.getAttributeAsIntArray("attname5")).isEqualTo(new int[] {1, 2, 3});
    try {
      obj.getAttributeAsIntArray("attname1");
      fail("No exception");
    } catch (Exception e) {
    }
    Map<String, Object> refs = new HashMap<String, Object>();
    refs.put("a", "Letter A");
    refs.put("b", "Letter B");
    refs.put("c", "Letter C");
    refs.put("attval1", "Some random text");
    refs.put("3.14", 3.14D);
    assertThat(obj.getAttributeAsReference("attname1", String.class, refs))
        .isEqualTo("Some random text");
    try {
      obj.getAttributeAsReference("attname4", String.class, refs);
      fail("No exception");
    } catch (Exception e) {
    }
    assertThat(obj.getAttributeAsReferenceList("attname6", String.class, refs))
        .isEqualTo(Arrays.asList("Letter A", "Letter B", "Letter C"));
  }

  @Test
  public void testWrite() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    obj.write(baos, "");
    assertThat(baos.toString()).isEqualTo(xml);
  }
}
