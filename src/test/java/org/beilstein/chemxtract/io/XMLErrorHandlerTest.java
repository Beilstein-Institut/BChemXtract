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
    SAXParseException exception = new SAXParseException("test message", "test public id", "test system id", 10, 20, testException);
    try {
      handler.error(exception);
      fail();
    }
    catch(SAXException handlerException) {
      assertThat(handlerException.getMessage()).isEqualTo("test message (Line 10, Column 20, System Id test system id, Public Id test public id)");
    }
  }

  @Test
  public void testFatalError() {
    XMLErrorHandler handler = new XMLErrorHandler();
    RuntimeException testException = new RuntimeException("test exception");
    SAXParseException exception = new SAXParseException("test message", "test public id", "test system id", 10, 20, testException);
    try {
      handler.fatalError(exception);
      fail();
    }
    catch(SAXException handlerException) {
      assertThat(handlerException.getMessage()).isEqualTo("test message (Line 10, Column 20, System Id test system id, Public Id test public id)");
    }
  }
}
