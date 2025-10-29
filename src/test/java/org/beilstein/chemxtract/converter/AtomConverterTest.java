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
package org.beilstein.chemxtract.converter;

import static org.junit.Assert.*;

import java.io.IOException;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.datatypes.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.silent.PseudoAtom;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

public class AtomConverterTest {

  private IChemObjectBuilder builder;

  @Before
  public void setUp() {
    builder = SilentChemObjectBuilder.getInstance();
  }

  private CDAtom baseAtom(String symbol, int atomicNumber) {
    CDAtom atom = new CDAtom();
    atom.setElementNumber(atomicNumber);
    atom.setNodeType(CDNodeType.Element);
    atom.setLabelText(symbol);
    return atom;
  }

  @Test
  public void convertCarbonElementTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("C", 6);
    IAtom atom = converter.convert(cdAtom);

    assertEquals("C", atom.getSymbol());
    assertEquals(6, atom.getAtomicNumber().intValue());
    assertTrue(converter.getAtomMap().containsKey(cdAtom));
  }

  @Test
  public void convertDeuteriumTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("D", 1);
    cdAtom.setNodeType(CDNodeType.GenericNickname); // triggers pseudo but "D" is special
    IAtom atom = converter.convert(cdAtom);

    assertEquals("H", atom.getSymbol());
    assertEquals(1, atom.getAtomicNumber().intValue());
    assertEquals(2, atom.getMassNumber().intValue());
  }

  @Test
  public void convertTritiumTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("T", 1);
    cdAtom.setNodeType(CDNodeType.GenericNickname);
    IAtom atom = converter.convert(cdAtom);

    assertEquals("H", atom.getSymbol());
    assertEquals(1, atom.getAtomicNumber().intValue());
    assertEquals(3, atom.getMassNumber().intValue());
  }

  @Test
  public void externalConnectionPointTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("*", 0);
    cdAtom.setNodeType(CDNodeType.ExternalConnectionPoint);
    IAtom atom = converter.convert(cdAtom);

    assertTrue(atom instanceof PseudoAtom);
    assertEquals("*", ((PseudoAtom) atom).getLabel());
  }

  @Test
  public void nonElementWithWarningBecomesPseudoAtomTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("Xx", 0);
    cdAtom.setNodeType(CDNodeType.GenericNickname);
    cdAtom.setChemicalWarning("invalid");
    IAtom atom = converter.convert(cdAtom);

    assertTrue(atom instanceof PseudoAtom);
    assertEquals("Xx", ((PseudoAtom) atom).getLabel());
  }

  @Test
  public void coordinates3DTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("C", 6);
    cdAtom.setPosition3D(new CDPoint3D(1.0f, 2.0f, 3.0f));

    IAtom atom = converter.convert(cdAtom);
    Point3d p = atom.getPoint3d();

    assertNotNull(p);
    assertEquals(1.0, p.x, 0.0001);
    assertEquals(-2.0, p.y, 0.0001); // inverted
    assertEquals(3.0, p.z, 0.0001);
  }

  @Test
  public void coordinates2DTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("C", 6);
    cdAtom.setPosition2D(new CDPoint2D(4.0f, 5.0f));

    IAtom atom = converter.convert(cdAtom);
    Point2d p = atom.getPoint2d();

    assertNotNull(p);
    assertEquals(4.0, p.x, 0.0001);
    assertEquals(-5.0, p.y, 0.0001); // inverted
  }

  @Test
  public void formalChargeIsSetTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder);
    CDAtom cdAtom = baseAtom("C", 6);
    cdAtom.setCharge(-1);

    IAtom atom = converter.convert(cdAtom);

    assertEquals(-1, atom.getFormalCharge().intValue());
  }

  @Test
  public void configureIsotopeExplicitlyTest() throws CDKException, IOException {
    AtomConverter converter = new AtomConverter(builder);
    IAtom atom = converter.createAtom("C", "C");
    converter.configureIsotope(atom, 13);

    assertEquals(13, atom.getMassNumber().intValue());
  }

  @Test
  public void unknownSymbolRelaxedModeTest() throws Exception {
    AtomConverter converter = new AtomConverter(builder, IChemObjectReader.Mode.RELAXED);
    CDAtom cdAtom = baseAtom("Xyz", 0);

    IAtom atom = converter.convert(cdAtom);

    assertTrue(atom instanceof PseudoAtom);
    assertEquals("Xyz", ((PseudoAtom) atom).getLabel());
  }

  @Test
  public void unknownSymbolStrictModeThrowsTest() {
    AtomConverter converter = new AtomConverter(builder, IChemObjectReader.Mode.STRICT);
    CDAtom cdAtom = baseAtom("Xyz", 0);

    assertThrows(CDKException.class, () -> converter.convert(cdAtom));
  }
}
