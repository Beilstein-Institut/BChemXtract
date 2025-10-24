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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class IOUtilsTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testWriteText() throws IOException {
    String fixture = "Hello world!";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.writeText(baos, fixture);
    assertThat(baos.toString()).isEqualTo(fixture);
  }
  
  @Test
  public void testReadBytes() throws IOException {
    String fixture = "Hello world!";
    ByteArrayInputStream bais = new ByteArrayInputStream(fixture.getBytes());
    byte[] out = IOUtils.readBytes(bais);
    assertThat(new String(out)).isEqualTo(fixture);
  }

  @Test
  public void testReadBytesSize() throws IOException {
    String fixture = "Hello world!";
    ByteArrayInputStream bais = new ByteArrayInputStream(fixture.getBytes());
    byte[] out = IOUtils.readBytes(bais, 5);
    assertThat(new String(out)).isEqualTo(fixture);
  }
  
  @Test
  public void testStartsWithBytes() {
    byte[] fixture = "Hello world!".getBytes();
    byte[] right = "Hello".getBytes();
    byte[] wrong = "Hola".getBytes();
    assertThat(IOUtils.startsWithBytes(fixture, right)).isTrue();
    assertThat(IOUtils.startsWithBytes(fixture, wrong)).isFalse();
  }
  
  @Test
  public void testCompressRoundtrip() throws Exception {
    byte[] fixture = "Hello world!".getBytes();
    byte[] result = IOUtils.compress(fixture);
    byte[] roundtrip = IOUtils.uncompress(result);
    assertThat(fixture).containsExactly(roundtrip);
  }
}
