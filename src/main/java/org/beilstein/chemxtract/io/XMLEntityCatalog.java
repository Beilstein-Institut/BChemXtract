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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class represents a catalog for XML entities, which is used for a XML parser to map System
 * and Public IDs to URLs.
 */
public class XMLEntityCatalog implements EntityResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLEntityCatalog.class);

  private final Map<String, String> publicIdURLs = new HashMap<>();
  private final Map<String, String> systemIdURLs = new HashMap<>();

  /**
   * Add an entry for a Public ID.
   *
   * @param publicId Public ID
   * @param url URL
   */
  public void addPublicId(String publicId, String url) {
    publicIdURLs.put(publicId, url);
  }

  /**
   * Add an entry for a System ID
   *
   * @param systemId System ID
   * @param url URL
   */
  public void addSystemId(String systemId, String url) {
    systemIdURLs.put(systemId, url);
  }

  /* (non-Javadoc)
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {
    LOGGER.debug("Resolving input source for public id:{} and system id:{}", publicId, systemId);

    // resolve public id or system id to URL
    String url = publicIdURLs.get(publicId);
    if (url == null) {
      url = systemIdURLs.get(systemId);
    }
    if (url == null && systemId != null && systemId.length() > 0) {
      for (Entry<String, String> entry : systemIdURLs.entrySet()) {
        if (entry.getKey().startsWith("*") && systemId.endsWith(entry.getKey().substring(1))) {
          url = entry.getValue();
          break;
        }
      }
    }

    LOGGER.debug("Resolve to url {}", url);

    // resolve URL to input stream
    if (url != null) {
      InputStream in = XMLEntityCatalog.class.getClassLoader().getResourceAsStream(url);
      if (in == null) {
        LOGGER.debug("No resource found for url {}", url);
        File file = new File(url);
        if (!file.exists()) {
          LOGGER.error("Could not load file {}", url);
          return null;
        }
        in = new FileInputStream(file);
        systemId = file.toURI().toString();
      } else {
        systemId = XMLEntityCatalog.class.getClassLoader().getResource(url).toString();
      }

      // Hand ownership of the stream to InputSource; close it ourselves if construction fails.
      try {
        InputSource inputSource = new InputSource(in);
        inputSource.setPublicId(publicId);
        inputSource.setSystemId(systemId);
        return inputSource;
      } catch (RuntimeException e) {
        try {
          in.close();
        } catch (IOException ignored) {
          // best-effort close on the error path
        }
        throw e;
      }
    }
    LOGGER.warn("No entry found for public id:{} and system id:{}", publicId, systemId);
    return null;
  }
}
