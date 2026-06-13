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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Default XML error handler used for parsing XML documents. */
public class XMLErrorHandler implements ErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLErrorHandler.class);

  @Override
  public void error(SAXParseException exception) throws SAXException {
    throw new XMLErrorHandlerException(getMessage(exception), exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    throw new XMLErrorHandlerException(getMessage(exception), exception);
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    LOGGER.warn(getMessage(exception), exception);
  }

  private String getMessage(SAXParseException exception) {
    StringBuilder message = new StringBuilder();
    if (exception.getLineNumber() >= 0) {
      message.append("Line ");
      message.append(exception.getLineNumber());
    }
    if (exception.getColumnNumber() >= 0) {
      if (message.length() > 0) {
        message.append(", ");
      }
      message.append("Column ");
      message.append(exception.getColumnNumber());
    }
    if (exception.getSystemId() != null) {
      if (message.length() > 0) {
        message.append(", ");
      }
      message.append("System Id ");
      message.append(exception.getSystemId());
    }
    if (exception.getPublicId() != null) {
      if (message.length() > 0) {
        message.append(", ");
      }
      message.append("Public Id ");
      message.append(exception.getPublicId());
    }
    if (message.length() > 0) {
      message.insert(0, " (");
      message.append(")");
    }
    message.insert(0, exception.getMessage());
    return message.toString();
  }

  /** Own exception for exceptions, which can occur during the parsing process of XML documents. */
  public static class XMLErrorHandlerException extends SAXException {
    private static final long serialVersionUID = -8987602073675231565L;

    /**
     * Create new instance with the given message and original exception.
     *
     * @param message Message
     * @param exception Original exception
     */
    public XMLErrorHandlerException(String message, SAXParseException exception) {
      super(message, exception);
    }

    @Override
    public String toString() {
      return getClass() + ": " + getMessage();
    }
  }
}
