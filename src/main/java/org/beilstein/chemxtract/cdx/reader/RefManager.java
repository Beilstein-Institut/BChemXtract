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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Object reference manger, used by {@link CDXReader} and {@link CDXMLReader}.
 */
public class RefManager {
  private static final Log logger = LogFactory.getLog(RefManager.class);

  private Map<Integer,Object> references = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T getObjectRef(int id, Class<T> clazz, boolean rigid) throws IOException {
    if (id == 0) {
      return null;
    }
    Object object = references.get(id);
    if (object == null) {
      String message = "Object for id " + Integer.toHexString(id) + " and class " + clazz.getName() + " not found";

      if (rigid) {
        throw new IOException(message);
      }
      logger.warn(message);
      return null;
    }
    while (object instanceof ReferenceContainer) {
      ReferenceContainer container = (ReferenceContainer) object;
      if (clazz.isAssignableFrom(container.reference.getClass())) {
        object = container.reference;
        break;
      }
      object = container.next;
    }
    if (!clazz.isAssignableFrom(object.getClass())) {
      String message = "Object with id 0x" + Integer.toHexString(id) + " is not instance of " + clazz.getName() + " instead instance of " +
              object.getClass().getName();

      if (rigid) {
        throw new IOException(message);
      }
      logger.warn(message);
      return null;
    }
    return (T) object;
  }

  public void putObjectRef(int id, Object reference) {
    if (id > 0 && reference != null) {
      if (references.get(id) != null) {
        if (!(references.get(id) instanceof ReferenceContainer)) {
          references.put(id, new ReferenceContainer(references.get(id), null));
        }
        references.put(id, new ReferenceContainer(reference, (ReferenceContainer) references.get(id)));
      } else {
        references.put(id, reference);
      }
    }
  }

  private static class ReferenceContainer {
    public final Object reference;
    public final ReferenceContainer next;

    public ReferenceContainer(Object reference, ReferenceContainer next) {
      this.reference = reference;
      this.next = next;
    }
  }

}
