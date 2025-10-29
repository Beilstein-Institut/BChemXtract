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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLErrorHandlerTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testError() {
    XMLErrorHandler handler = new XMLErrorHandler();
    RuntimeException testException = new RuntimeException("test exception");
    SAXParseException exception =
        new SAXParseException(
            "test message", "test public id", "test system id", 10, 20, testException);
    try {
      handler.error(exception);
      fail();
    } catch (SAXException handlerException) {
      assertThat(handlerException.getMessage())
          .isEqualTo(
              "test message (Line 10, Column 20, System Id test system id, Public Id test public id)");
    }
  }

  @Test
  public void testFatalError() {
    XMLErrorHandler handler = new XMLErrorHandler();
    RuntimeException testException = new RuntimeException("test exception");
    SAXParseException exception =
        new SAXParseException(
            "test message", "test public id", "test system id", 10, 20, testException);
    try {
      handler.fatalError(exception);
      fail();
    } catch (SAXException handlerException) {
      assertThat(handlerException.getMessage())
          .isEqualTo(
              "test message (Line 10, Column 20, System Id test system id, Public Id test public id)");
    }
  }
}
