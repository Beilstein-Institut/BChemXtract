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

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class holds various helper methods for the IO package.
 */
public class IOUtils {
  private static final Log logger = LogFactory.getLog(IOUtils.class);

  private static final int BUFFER_SIZE = 4096;

  /** Standard Character Encoding. */
  public static final String ENCODING = "UTF-8";

  /**
   * Write text into an {@link OutputStream} with the standard character encoding. Closing and
   * flushing the stream is not necessary.
   *
   * @param out OutputStream
   * @param text Text
   * @throws IOException Occurs if the method cannot write the content into the OutputStream
   */
  public static void writeText(OutputStream out, CharSequence text) throws IOException {
    Writer writer = null;
    try {
      writer = new OutputStreamWriter(out, ENCODING);
      writer.write(text.toString());
      writer.flush();
    } finally {
      close(writer);
    }
  }

  /**
   * Read a byte array from an {@link InputStream}. Closing the stream is not necessary.
   * 
   * @param in InputStream
   * @return Byte array
   * @throws IOException Occurs if the method cannot read the content from InputStream
   */
  public static byte[] readBytes(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(in, out);
    return out.toByteArray();
  }

  /**
   * Read a byte array from an {@link InputStream}. Closing the stream is not necessary.
   * 
   * @param in InputStream
   * @param length Expected length of the byte array
   * @return Byte array
   * @throws IOException Occurs if the method cannot read the content from InputStream
   */
  public static byte[] readBytes(InputStream in, int length) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(length);
    copy(in, out);
    return out.toByteArray();
  }

  /**
   * Copy the content from an {@link InputStream} to an {@link OutputStream}. Closing and flushing
   * the streams is not necessary.
   *
   * @param in InputStream
   * @param out OutputStream
   * @throws IOException Occurs if the method cannot read or write the content from the streams
   */
  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[BUFFER_SIZE];
    try {
      int length;
      while ((length = in.read(buffer)) >= 0) {
        out.write(buffer, 0, length);
      }
      out.flush();
    } finally {
      close(in);
      close(out);
    }
  }

  /**
   * Close an {@link Closeable} object.
   *
   * @param closable {@link Closeable} instance
   */
  public static void close(Closeable closable) {
    if (closable != null) {
      try {
        closable.close();
      } catch (IOException e) {
        logger.debug("Unable to close stream", e);
      }
    }
  }

  /**
   * Test if a byte array starts with the given pattern.
   * 
   * @param bytes Byte array
   * @param pattern Pattern
   * @return True, if the byte array starts with the pattern
   */
  public static boolean startsWithBytes(byte[] bytes, byte[] pattern) {
    if (pattern.length > bytes.length) {
      return false;
    }
    for (int i = 0; i < pattern.length; i++) {
      if (bytes[i] != pattern[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compress a byte array with ZLIB compression.
   * 
   * @param data Uncompressed byte array
   * @return Compressed byte array
   */
  public static byte[] compress(byte[] data) {
    Deflater compresser = new Deflater();
    compresser.setInput(data);
    compresser.finish();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = compresser.deflate(buffer)) > 0) {
      baos.write(buffer, 0, length);
    }
    return baos.toByteArray();
  }

  /**
   * Uncompress a byte array with the ZLIB compression.
   * 
   * @param data Compressed byte array
   * @return Uncompressed byte array
   * @throws DataFormatException Occurs if the compressed byte array is corrupted
   */
  public static byte[] uncompress(byte[] data) throws DataFormatException {
    Inflater decompresser = new Inflater();
    decompresser.setInput(data, 0, data.length);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = decompresser.inflate(buffer)) > 0) {
      baos.write(buffer, 0, length);
    }
    decompresser.end();
    return baos.toByteArray();
  }

}
