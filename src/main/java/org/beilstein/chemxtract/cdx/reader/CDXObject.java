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
package org.beilstein.chemxtract.cdx.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store intermediate values during the reading of CDX files. This class is
 * only used by the {@link CDXReader}.
 */
public class CDXObject {
  /** Tag of the object. */
  private int tag;
  /** Unique id of the object. */
  private int id;
  /** Position of the object in the stream. */
  private int position;
  /** List of child objects. */
  private List<CDXObject> objects = new ArrayList<>();
  /** List of properties for this object. */
  private List<CDXProperty> properties = new ArrayList<>();
  /** User-defined object, used to store the mapping to real objects. */
  private Object instance;

  public Object getInstance() {
    return instance;
  }

  public void setInstance(Object instance) {
    this.instance = instance;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getTag() {
    return tag;
  }

  public void setTag(int tag) {
    this.tag = tag;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<CDXObject> getObjects() {
    return objects;
  }

  public void setObjects(List<CDXObject> objects) {
    this.objects = objects;
  }

  public List<CDXProperty> getProperties() {
    return properties;
  }

  public void setProperties(List<CDXProperty> properties) {
    this.properties = properties;
  }

}
