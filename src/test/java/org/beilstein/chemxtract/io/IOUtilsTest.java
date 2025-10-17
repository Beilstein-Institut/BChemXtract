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
