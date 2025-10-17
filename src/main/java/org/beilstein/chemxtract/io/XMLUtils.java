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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class holds various helper methods for XML related actions.
 */
public class XMLUtils {
  private static final Log logger = LogFactory.getLog(XMLUtils.class);

  /**
   * Parse a XML document and return the root XML element.
   * 
   * @param in {@link InputStream} from which the input are read
   * @param entityResolver Entity resolver, normally an instance of {@link XMLEntityCatalog}
   * @return Root XML element
   * @throws IOException Occurs if the reader couldn't read the input from the {@link InputStream}
   */
  public static XMLObject parse(InputStream in, EntityResolver entityResolver) throws IOException {
    return parse(in, entityResolver, true);
  }

  /**
   * Parse a XML document and return the root XML element.
   * 
   * @param in {@link InputStream} from which the input are read
   * @param entityResolver Entity resolver, normally an instance of {@link XMLEntityCatalog}
   * @param validate Flag, if the XML document, should be validated by the DTD
   * @return Root XML element
   * @throws IOException Occurs if the reader couldn't read the input from the {@link InputStream}
   */
  public static XMLObject parse(InputStream in, EntityResolver entityResolver, boolean validate) throws IOException {
    if (in == null) {
      throw new NullPointerException("Input stream is null");
    }
    XMLReader parser = null;
    try {
      parser = XMLReaderFactory.createXMLReader();
      if (validate) {
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      }
    } catch (SAXException e) {
      throw new IOException("Could not create XML parser", e);
    }

    parser.setEntityResolver(entityResolver);
    parser.setErrorHandler(new XMLErrorHandler());

    XMLReaderHandler handler = new XMLReaderHandler();
    parser.setContentHandler(handler);

    InputSource source = new InputSource(in);
    try {
      parser.parse(source);
    } catch (SAXParseException e) {
      // Create message from the location information
      StringBuilder message = new StringBuilder();
      if (e.getPublicId() != null) {
        message.append(" (");
        message.append("PublicId=").append(e.getPublicId());
      }
      if (e.getSystemId() != null) {
        if (message.length() > 0) {
          message.append(",");
        } else {
          message.append(" ");
        }
        message.append("SystemId=").append(e.getSystemId());
      }
      if (e.getLineNumber() >= 0) {
        if (message.length() > 0) {
          message.append(",");
        } else {
          message.append(" (");
        }
        message.append("Line=").append(e.getLineNumber());
      }
      if (e.getColumnNumber() >= 0) {
        if (message.length() > 0) {
          message.append(",");
        } else {
          message.append(" (");
        }
        message.append("Column=").append(e.getColumnNumber());
      }
      if (message.length() > 0) {
        message.append(")");
      }
      if (e.getMessage() != null) {
        message.insert(0, e.getMessage());
        message.insert(0, " : ");
      }
      throw new IOException("Could not parse XML file" + message.toString(), e);
    } catch (SAXException e) {
      throw new IOException("Could not parse XML file", e);
    }
    in.close();

    return handler.getRoot();
  }

  /**
   * Returns the attribute of an element node
   * 
   * @param node Element node
   * @param attributeName Name of attribute
   * @return Attribute node
   */
  public static Node getAttributeByName(Node node, String attributeName) {
    if (node == null) {
      return null;
    }
    NamedNodeMap nnm = node.getAttributes();
    for (int i = 0; i < nnm.getLength(); i++) {
      Node n = nnm.item(i);
      if (n.getNodeName().equals(attributeName)) {
        return n;
      }
    }
    // no such attribute was found
    return null;
  }

  /**
   * Standard message for unexpected elements in a XML format.
   * 
   * @param object XML object
   * @return Error message
   */
  public static String getUnexpectedObjectMessage(XMLObject object) {
    return "Encountered unexpected element \'" + object.getName() + "\' at " + object.getLocation();
  }

  /**
   * Standard message for unexpected attributes in a XML format
   * 
   * @param object XML Object
   * @param attribute Attribute name
   * @return Error message
   */
  public static String getUnexpectedAttributeMessage(XMLObject object, String attribute) {
    return "Encountered unexpected attribute \'" + attribute + "\' of element \'" + object.getName() + "\'" + " (value=" +
            object.getAttributes().get(attribute) + ") at " + object.getLocation();
  }

  /**
   * Standard message for invalid values of elements in a XML format.
   * 
   * @param object XML object
   * @return Error message
   */
  public static String getInvalidObjectMessage(XMLObject object) {
    return "Encountered invalid value \'" + object.getTextsAsString() + "\' for element \'" + object.getName() + "\' at " +
            object.getLocation();
  }

  /**
   * Standard message for invalid values of attributes in a XML format.
   * 
   * @param object XML object
   * @param attribute Attribute name
   * @return Error message
   */
  public static String getInvalidAttributeMessage(XMLObject object, String attribute) {
    return "Encountered invalid attribute value \'" + object.getAttributes().get(attribute) + "\' for attribute \'" + attribute +
            "\' of element \'" + object.getName() + "\' at " + object.getLocation();
  }

}
