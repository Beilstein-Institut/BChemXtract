package org.beilstein.chemxtract.converter;

import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.datatypes.CDBondCIPType;
import org.beilstein.chemxtract.cdx.datatypes.CDBondDisplay;
import org.beilstein.chemxtract.cdx.datatypes.CDBondOrder;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.isomorphism.matchers.Expr;
import org.openscience.cdk.isomorphism.matchers.QueryBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BondConverterTest {

  private BondConverter converter;
  private IChemObjectBuilder builder;
  private Map<CDAtom,IAtom> atomMap;
  private CDAtom cdAtom1;
  private CDAtom cdAtom2;
  private IAtom atom1;
  private IAtom atom2;

  @Before
  public void setUp() throws Exception {
    atomMap = new HashMap<>();
    builder = SilentChemObjectBuilder.getInstance();
    converter = new BondConverter(builder, atomMap);

    cdAtom1 = mock(CDAtom.class);
    cdAtom2 = mock(CDAtom.class);
    atom1 = new Atom("C");
    atom2 = new Atom("O");
    atomMap = new HashMap<>();
    atomMap.put(cdAtom1, atom1);
    atomMap.put(cdAtom2, atom2);
    converter = new BondConverter(SilentChemObjectBuilder.getInstance(), atomMap);
  }

  private CDBond mockBond(CDBondOrder order, CDBondDisplay display, CDBondCIPType stereo) {
    CDBond bond = mock(CDBond.class);
    when(bond.getBegin()).thenReturn(cdAtom1);
    when(bond.getEnd()).thenReturn(cdAtom2);
    when(bond.getBondOrder()).thenReturn(order);
    when(bond.getBondDisplay()).thenReturn(display);
    when(bond.getStereochemistry()).thenReturn(stereo);
    return bond;
  }

  @Test
  public void singleSolidBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.Solid, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Solid, result.getDisplay());
  }

  @Test
  public void singleDashBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.Dash, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Dash, result.getDisplay());
  }

  @Test
  public void singleHashBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.Hash, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Hash, result.getDisplay());
  }

  @Test
  public void singleWedgedHashBeginBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.WedgedHashBegin, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.WedgedHashBegin, result.getDisplay());
  }

  @Test
  public void singleWedgedHashEndBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.WedgedHashEnd, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.WedgedHashEnd, result.getDisplay());
  }

  @Test
  public void singleBoldBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.Bold, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Bold, result.getDisplay());
  }

  @Test
  public void singleWedgeBeginBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.WedgeBegin, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.WedgeBegin, result.getDisplay());
  }

  @Test
  public void singleWedgeEndBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.WedgeEnd, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.WedgeEnd, result.getDisplay());
  }

  @Test
  public void singleWavyBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, CDBondDisplay.Wavy, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Wavy, result.getDisplay());
  }

  @Test
  public void singleNullDisplayBondTest() throws CDKException {
    CDBond cdBond = mockBond(CDBondOrder.Single, null, null);
    IBond result = converter.convert(cdBond);
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertEquals(IBond.Display.Solid, result.getDisplay());
  }

  @Test
  public void doubleBondUndeterminedTest() throws CDKException {
    CDBond bond = mockBond(CDBondOrder.Double, null, CDBondCIPType.Undetermined);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.DOUBLE, result.getOrder());
    assertEquals(IBond.Stereo.E_OR_Z, result.getStereo());
  }

  @Test
  public void doubleBondETest() throws CDKException {
    CDBond bond = mockBond(CDBondOrder.Double, null, CDBondCIPType.E);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.DOUBLE, result.getOrder());
    assertEquals(IBond.Stereo.E, result.getStereo());
  }

  @Test
  public void doubleBondZTest() throws CDKException {
    CDBond bond = mockBond(CDBondOrder.Double, null, CDBondCIPType.Z);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.DOUBLE, result.getOrder());
    assertEquals(IBond.Stereo.Z, result.getStereo());
  }

  @Test
  public void doubleBondNoneStereoTest() throws CDKException {
    CDBond bond = mockBond(CDBondOrder.Double, null, CDBondCIPType.None);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.DOUBLE, result.getOrder());
    assertEquals(IBond.Stereo.NONE, result.getStereo());
  }

  @Test
  public void doubleBondNullStereoTest() throws CDKException {
    CDBond bond = mockBond(CDBondOrder.Double, null, null);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.DOUBLE, result.getOrder());
    assertEquals(IBond.Stereo.NONE, result.getStereo());
  }

  @Test
  public void testTripleBond() throws Exception {
    CDBond bond = mockBond(CDBondOrder.Triple, null, null);
    IBond result = converter.convert(bond);
    assertEquals(IBond.Order.TRIPLE, result.getOrder());
  }

  @Test
  public void testOneHalfBond() throws Exception {
    CDBond bond = mockBond(CDBondOrder.OneHalf, null, null);
    IBond result = converter.convert(bond);
    assertTrue(result.isAromatic());
    assertEquals(IBond.Order.SINGLE, result.getOrder());
    assertTrue(result.getBegin().isAromatic());
    assertTrue(result.getEnd().isAromatic());
  }

  @Test
  public void testQueryBonds() throws Exception {
    CDBond bond1 = mockBond(CDBondOrder.SingleOrDouble, null, null);
    assertTrue(converter.convert(bond1) instanceof org.openscience.cdk.isomorphism.matchers.QueryBond);

    CDBond bond2 = mockBond(CDBondOrder.SingleOrAromatic, null, null);
    assertTrue(converter.convert(bond2) instanceof org.openscience.cdk.isomorphism.matchers.QueryBond);

    CDBond bond3 = mockBond(CDBondOrder.DoubleOrAromatic, null, null);
    assertTrue(converter.convert(bond3) instanceof org.openscience.cdk.isomorphism.matchers.QueryBond);

    CDBond bond4 = mockBond(CDBondOrder.Any, null, null);
    assertTrue(converter.convert(bond4) instanceof org.openscience.cdk.isomorphism.matchers.QueryBond);
  }

  @Test
  public void testUnsupportedBondOrderThrows() {
    CDBond bond = mockBond(null, null, null);
    assertThrows(NullPointerException.class, () -> converter.convert(bond));
  }
}