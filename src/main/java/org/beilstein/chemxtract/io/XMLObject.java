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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import static org.beilstein.chemxtract.io.XMLUtils.getInvalidAttributeMessage;
import static org.beilstein.chemxtract.io.XMLUtils.getInvalidObjectMessage;

/**
 * Object for a lightweight XML DOM implementation. This implementation doesn't support XML
 * namespaces yet.
 */
public class XMLObject {
  private String name;
  private Map<String,String> attributes = new LinkedHashMap<>();
  private List<XMLObject> objects = new ArrayList<>();
  private List<String> texts = new ArrayList<>();
  private Object instance;
  private int lineNumber = 0;
  private int columnNumber = 0;

  private static final String NS = "";
  private static final String CDATA = "CDATA";

  /**
   * Returns the line number of the XML element.
   * 
   * @return Line number
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Sets the line number of the XML element. This method is invoked by the XML parser.
   * 
   * @param lineNumber
   */
  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  /**
   * Returns the column number of the XML element.
   * 
   * @return Column number
   */
  public int getColumnNumber() {
    return columnNumber;
  }

  /**
   * Sets the column number of the XML element. This method is invoked by the XML parser.
   * 
   * @param columnNumber Column number
   */
  public void setColumnNumber(int columnNumber) {
    this.columnNumber = columnNumber;
  }

  /**
   * Returns the number of XMLObjects in the tree this object is root of
   * @param recursive If true, count recursively, otherwise just count the direct children
   * @return The number of objects in the tree 
   */
  public int getNumberOfObjects(boolean recursive) {
    if (!recursive) {
      return getObjects().size();
    } else {
      int count = 0;
      for (XMLObject child : getObjects()) {
        count += child.getNumberOfObjects(true);
      }
      return getObjects().size() + count;
    }
  }

  /**
   * Returns the tree depth.
   * @return
   */
  public int getDepth() {
    if (getObjects().isEmpty()) {
      return 1;
    } else {
      List<Integer> subDepths = new ArrayList<>();
      for (XMLObject child : getObjects()) {
        subDepths.add(child.getDepth());
      }
      return Collections.max(subDepths) + 1;
    }
  }

  /**
   * Return the text elements of the XML element. Normally you have only one text element, but for
   * Mix-Content elements you have n+1 text elements where n is the number of child elements.
   * 
   * @return Text elements
   */
  public List<String> getTexts() {
    return texts;
  }

  /**
   * Sets the texts elements. This method is invoked by the XML parser.
   * 
   * @param texts Text elements.
   */
  public void setTexts(List<String> texts) {
    this.texts = texts;
  }

  /**
   * Returns the name of the XML element.
   * 
   * @return Element name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the XML element. This method is invoked by the XML parser.
   * 
   * @param name Element name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the element attributes as map, where the key is the attribute name and the value the
   * attribute value.
   * 
   * @return Element attributes
   */
  public Map<String,String> getAttributes() {
    return attributes;
  }

  /**
   * Sets the attributes of the element. This method is invoked by the XML parser.
   * 
   * @param attributes Element attributes
   */
  public void setAttributes(Map<String,String> attributes) {
    this.attributes = attributes;
  }

  /**
   * Returns the list of child XML elements.
   * 
   * @return Child elements
   */
  public List<XMLObject> getObjects() {
    return objects;
  }

  /**
   * Sets the list of child elements. This method is invoked by the XML parser.
   * 
   * @param objects Child elements
   */
  public void setObjects(List<XMLObject> objects) {
    this.objects = objects;
  }

  /**
   * Returns an user-defined object, which can be attached during a XML-Java mapping process.
   * 
   * @return User-defined object
   */
  public Object getInstance() {
    return instance;
  }

  /**
   * Sets an user-defined object, which can be attached during a XML-Java mapping process.
   * 
   * @param instance User-defined object
   */
  public void setInstance(Object instance) {
    this.instance = instance;
  }

  // ============== HELPER METHODS ===============

  /**
   * Add or replace an attribute of this XML object
   * 
   * @param name Name of the attribute
   * @param value Value of the attribute
   */
  public void setAttribute(String name, String value) {
    attributes.put(name, value);
  }

  /**
   * Remove an attribute of this XML object.
   * 
   * @param name Name of the attribute
   */
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  /**
   * Add the XML object as new child element.
   * 
   * @param object The new child element
   */
  public void addObject(XMLObject object) {
    objects.add(object);
  }

  /**
   * Add a new child element with the given name.
   * 
   * @param name Name of the child element
   * @return The new XML object
   */
  public XMLObject addObject(String name) {
    XMLObject object = new XMLObject();
    object.setName(name);
    objects.add(object);
    return object;
  }

  /**
   * Returns true, if this element has a child element with the given name.
   * 
   * @param name Name of child element
   * @return True, if this element has a child element with the given name
   */
  public boolean hasObjects(String name) {
    for (XMLObject object : getObjects()) {
      if (object.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a list of child elements with the given name.
   * 
   * @param name Name of the child elements
   * @return List of child elements
   */
  public List<XMLObject> getObjects(String name) {
    List<XMLObject> list = new ArrayList<>(getObjects().size());
    for (XMLObject object : getObjects()) {
      if (object.getName().equals(name)) {
        list.add(object);
      }
    }
    return list;
  }

  /**
   * Returns a list of all child elements independent of position the with the given name.
   * 
   * @param name Name of the child elements
   * @return List of all child elements
   */
  public List<XMLObject> getAllObjects(String name) {
    LinkedList<XMLObject> stack = new LinkedList<>();
    stack.add(this);

    List<XMLObject> list = new ArrayList<>();

    // order of visit is IMPORTANT
    while (!stack.isEmpty()) {
      XMLObject object = stack.remove(0);
      stack.addAll(0, object.getObjects());

      if (object != this && object.getName().equals(name)) {
        list.add(object);
      }
    }

    return list;
  }

  /**
   * Returns the first child element with the given name
   * 
   * @param name Name of the child element
   * @return First child element
   */
  public XMLObject getFirstObject(String name) {
    for (XMLObject object : getObjects()) {
      if (object.getName().equals(name)) {
        return object;
      }
    }
    return null;
  }

  /**
   * Returns the text elements as text string.
   * 
   * @return Text elements as text string
   */
  public String getTextsAsString() {
    StringBuilder sb = new StringBuilder();
    for (String text : getTexts()) {
      if (text != null) {
        sb.append(text);
      }
    }
    return sb.toString();
  }

  /**
   * Returns the text elements as integer.
   * 
   * @return Text elements as integer
   * @throws IOException Occurs if the texts elements cannot be parse to an integer
   */
  public int getTextsAsInt() throws IOException {
    try {
      return Integer.parseInt(getTextsAsString());
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidObjectMessage(this), e);
    }
  }

  /**
   * Returns true if the element has an attribute with the given name.
   * 
   * @param name Name of the attribute
   * @return True if the element has an attribute with the given name
   */
  public boolean hasAttribute(String name) {
    return attributes.containsKey(name);
  }

  /**
   * Returns the attribute value for the given attribute name.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute, otherwise null if the attribute doesn't exists
   */
  public String getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * Returns the attribute value for the given attribute name.
   * 
   * @param name Name of the attribute
   * @param defaultValue Value, which should be returned if the attribute doesn't exists
   * @return Value of the attribute
   */
  public String getAttribute(String name, String defaultValue) {
    if (hasAttribute(name)) {
      return attributes.get(name);
    }
    return defaultValue;
  }

  /**
   * Returns the attribute value for the given name as boolean. "yes"/"no" and "true"/"false" are
   * valid values.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute as boolean
   * @throws IOException
   */
  public boolean getAttributeAsBoolean(String name) throws IOException {
    if (!getAttribute(name).equalsIgnoreCase("yes") && !getAttribute(name).equalsIgnoreCase("true") &&
            !getAttribute(name).equalsIgnoreCase("no") && !getAttribute(name).equalsIgnoreCase("false")) {
      throw new IOException(getInvalidAttributeMessage(this, name));
    }
    return getAttribute(name).equalsIgnoreCase("yes") || getAttribute(name).equalsIgnoreCase("true");
  }

  /**
   * Returns the attribute value for the given name as boolean.
   * 
   * @param name Name of the attribute
   * @param defaultValue Value, which should be returned if the attribute doesn't exists
   * @return Value of the attribute as boolean
   * @throws IOException
   */
  public boolean getAttributeAsBoolean(String name, boolean defaultValue) throws IOException {
    if (hasAttribute(name)) {
      return getAttributeAsBoolean(name);
    }
    return defaultValue;
  }

  /**
   * Returns the attribute value for the given name as integer.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute as integer
   * @throws IOException
   */
  public int getAttributeAsInt(String name) throws IOException {
    try {
      return Integer.parseInt(getAttribute(name));
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidAttributeMessage(this, name), e);
    }
  }

  /**
   * Returns the attribute value for the given name as integer.
   * 
   * @param name Name of the attribute
   * @param defaultValue Value, which should be returned if the attribute doesn't exists
   * @return Value of the attribute as integer
   * @throws IOException
   */
  public int getAttributeAsInt(String name, int defaultValue) throws IOException {
    if (hasAttribute(name)) {
      return getAttributeAsInt(name);
    }
    return defaultValue;
  }

  /**
   * Returns the attribute value for the given name as long.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute as long
   * @throws IOException
   */
  public long getAttributeAsLong(String name) throws IOException {
    try {
      return Long.parseLong(getAttribute(name));
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidAttributeMessage(this, name), e);
    }
  }

  /**
   * Returns the attribute value for the given name as float.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute as float
   * @throws IOException
   */
  public float getAttributeAsFloat(String name) throws IOException {
    try {
      return Float.parseFloat(getAttribute(name));
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidAttributeMessage(this, name), e);
    }
  }

  /**
   * Returns the attribute value for the given name as double.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute as double
   * @throws IOException
   */
  public double getAttributeAsDouble(String name) throws IOException {
    try {
      return Double.parseDouble(getAttribute(name));
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidAttributeMessage(this, name), e);
    }
  }

  /**
   * Returns the attribute value as list of text strings. The text strings are separated by one or
   * more whitespace characters.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute
   */
  public List<String> getAttributeAsStringList(String name) {
    return Arrays.asList(getAttribute(name).split("\\s+"));
  }

  /**
   * Returns the attribute value as list of text strings. The text strings are separated by 
   * the given separator char
   * 
   * @param name Name of the attribute
   * @param separatorChar Name of the separator char
   * @return Value of the attribute
   */
  public List<String> getAttributeAsStringList(String name, String separatorChar) {
    String[] tmp = getAttribute(name).split(separatorChar);
    List<String> result = new ArrayList<>();
    if (tmp != null) {
      for (String s : tmp) {
        result.add(s.trim());
      }
    }
    return result;
  }

  /**
   * Returns the attribute value as list of integers. The integers are separated by one or more
   * whitespace characters.
   * 
   * @param name Name of the attribute
   * @return Value of the attribute
   * @throws IOException
   */
  public int[] getAttributeAsIntArray(String name) throws IOException {
    List<String> stringList = getAttributeAsStringList(name);
    int[] intArray = new int[stringList.size()];
    try {
      for (int i = 0; i < intArray.length; i++) {
        intArray[i] = Integer.parseInt(stringList.get(i));
      }
    } catch (NumberFormatException e) {
      throw new IOException(getInvalidAttributeMessage(this, name), e);
    }
    return intArray;
  }

  @SuppressWarnings("unchecked")
  private <T> T getReference(String id, Class<T> clazz, Map<String,Object> references) throws IOException {
    if (!references.containsKey(id)) {
      throw new IOException("Object not found for id \"" + id + "\" at " + getLocation());
    }
    Object object = references.get(id);
    if (object == null) {
      return null;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      throw new IOException("Object with id \"" + id + "\" is not instance of " + clazz.getName() + " instead instance of " +
              object.getClass().getName() + " at " + getLocation());
    }
    return (T) object;
  }

  /**
   * Returns the object, which referenced by the attribute's value.
   * 
   * @param <T> Type of the reference
   * @param name Name of the attribute
   * @param clazz Class of the type
   * @param references Map, where the objects are associated by the reference IDs
   * @return Object, which is referenced by the attribute's value
   * @throws IOException Occurs if no object is mapped to the given attribute value or if the
   *           object has a different type
   */
  public <T> T getAttributeAsReference(String name, Class<T> clazz, Map<String,Object> references) throws IOException {
    return getReference(getAttribute(name), clazz, references);
  }

  /**
   * Returns the list of objects referenced by the attribute's values. The values are separated by
   * one or more whitespaces.
   * 
   * @param <T> Type of the reference
   * @param name Name of the attribute
   * @param clazz Class of the type
   * @param references Map, where the objects are associated by the reference IDs
   * @return List of object, which are referenced by the attribute's values
   * @throws IOException Occurs if no objects is mapped to the given attribute values or if the
   *           objects have a different type
   */
  public <T> List<T> getAttributeAsReferenceList(String name, Class<T> clazz, Map<String,Object> references) throws IOException {
    List<String> intList = getAttributeAsStringList(name);
    List<T> list = new ArrayList<>(intList.size());
    for (String id : intList) {
      list.add(getReference(id, clazz, references));
    }
    return list;
  }

  /**
   * Returns the line and column number as text.
   * 
   * @return Location as text
   */
  public String getLocation() {
    return "Line " + getLineNumber() + ",Column " + getColumnNumber();
  }

  /**
   * Write this XML element and all his child elements to a XML document.
   * 
   * @param out {@link OutputStream}, which retrieves the generated output
   * @param namespace Default XML namespace
   * @throws SAXException Occurs if the writer couldn't write the output to the
   *           {@link ContentHandler}
   * @throws IOException Occurs if the writer couldn't write the output into the
   *           {@link OutputStream}
   * @throws TransformerConfigurationException Occurs if the writer couldn't configure the XML
   *           serializer
   */
  public void write(OutputStream out, String namespace) throws SAXException, IOException, TransformerConfigurationException {
    SAXTransformerFactory serializerfactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    serializerfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

    Properties format = new Properties();
    format.put(OutputKeys.ENCODING, "ASCII");
    format.put(OutputKeys.INDENT, "yes");
    format.put(OutputKeys.METHOD, "xml");

    TransformerHandler handler = serializerfactory.newTransformerHandler();
    handler.getTransformer().setOutputProperties(format);

    CharArrayWriter writer = new CharArrayWriter();
    handler.setResult(new StreamResult(writer));

    handler.startDocument();
    handler.startPrefixMapping("", namespace);

    this.write(handler);

    handler.endPrefixMapping("");
    handler.endDocument();

    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
    outputStreamWriter.write(new String(writer.toCharArray()));
    outputStreamWriter.flush();
    outputStreamWriter.close();
  }

  private void write(ContentHandler handler) throws SAXException {
    AttributesImpl attributes = new AttributesImpl();
    for (String name : getAttributes().keySet()) {
      attributes.addAttribute("", name, name, CDATA, getAttribute(name));
    }

    handler.startElement(NS, getName(), getName(), attributes);
    int index = 0;
    String text = texts.get(index);
    if (text != null && text.length() > 0) {
      handler.characters(text.toCharArray(), 0, text.length());
    }
    for (XMLObject object : getObjects()) {
      object.write(handler);
      text = texts.get(index++);
      if (text != null && text.length() > 0) {
        handler.characters(text.toCharArray(), 0, text.length());
      }
    }
    handler.endElement(NS, getName(), getName());
  }

}