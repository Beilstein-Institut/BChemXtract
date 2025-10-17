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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * Default content handler to generate an instance of {@link XMLObject}.
 */
public class XMLReaderHandler implements ContentHandler {
  private static final Log logger = LogFactory.getLog(XMLReaderHandler.class);

  private Stack<XMLObject> stack = new Stack<>();
  private XMLObject root;
  private StringBuilder sb;
  private Locator locator;

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  @Override
  public void startDocument() throws SAXException {
    setRoot(null);
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
  public void endDocument() throws SAXException {
    // do nothing here
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    if (logger.isDebugEnabled()) {
      logger.debug("Start element " + localName +
              (locator == null ? "" : " at line " + locator.getLineNumber() + " and column " + locator.getColumnNumber()));
    }
    if (!stack.isEmpty()) {
      XMLObject object = stack.peek();
      object.getTexts().add(sb.length() > 0 ? sb.toString() : null);
    }

    XMLObject object = new XMLObject();
    object.setName(localName);
    if (locator != null) {
      object.setLineNumber(locator.getLineNumber());
      object.setColumnNumber(locator.getColumnNumber());
    }
    for (int i = 0; i < atts.getLength(); i++) {
      // remove empty attributes
      if (atts.getValue(i) != null && atts.getValue(i).length() > 0) {
        object.getAttributes().put(atts.getLocalName(i), atts.getValue(i));
      }
    }
    if (!stack.isEmpty()) {
      stack.peek().getObjects().add(object);
    }
    stack.push(object);

    sb = new StringBuilder();
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void endElement(String uri, String localName, String name) throws SAXException {
    if (logger.isDebugEnabled()) {
      logger.debug("End element " + localName +
              (locator == null ? "" : " at line " + locator.getLineNumber() + " and column " + locator.getColumnNumber()));
    }

    XMLObject object = stack.pop();
    if (stack.isEmpty()) {
      setRoot(object);
    }

    object.getTexts().add(sb.length() > 0 ? sb.toString() : null);
    sb = new StringBuilder();
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (logger.isDebugEnabled()) {
      logger.debug(
              "Characters" + (locator == null ? "" : " at line " + locator.getLineNumber() + " and column " + locator.getColumnNumber()));
    }

    sb.append(new String(ch, start, length));
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    // ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
   */
  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    // ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  @Override
  public void skippedEntity(String name) throws SAXException {
    logger.warn("Skipped entity found:" + name);
    // ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // ignore
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    // ignore
  }

  /**
   * Return the root XML element of the parsed document.
   * 
   * @return Root element
   */
  public XMLObject getRoot() {
    return root;
  }

  private void setRoot(XMLObject root) {
    this.root = root;
  }

}