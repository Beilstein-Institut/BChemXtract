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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint3D;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.openscience.cdk.Isotope;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.tools.periodictable.PeriodicTable;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Converts {@link CDAtom} instances (e.g. from ChemDraw/CDX or CDXML) into CDK {@link IAtom} objects.
 * <p>
 * The {@code AtomConverter} handles various ChemDraw node types such as elements,
 * pseudo atoms, external connection points, and chemical abbreviations.
 * It also takes care of isotope configuration (including deuterium and tritium),
 * coordinate conversion between ChemDraw and CDK coordinate systems.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
 * AtomConverter converter = new AtomConverter(builder);
 * IAtom cdkAtom = converter.convert(cdAtom);
 * }</pre>
 */
public class AtomConverter {

  private final IChemObjectBuilder builder;
  private final IChemObjectReader.Mode mode;
  private static final Log logger = LogFactory.getLog(AtomConverter.class);
  private final Map<CDAtom, IAtom> atomMap;

  /**
   * Constructs an {@code AtomConverter} with the given {@link IChemObjectBuilder}
   * and mode.
   *
   * @param builder the CDK {@link IChemObjectBuilder} used to instantiate atoms
   * @param mode    the parsing mode determining how strictly to interpret atom symbols;
   *                see {@link IChemObjectReader.Mode}
   */
  public AtomConverter(IChemObjectBuilder builder, IChemObjectReader.Mode mode) {
    this.builder = builder;
    this.mode = mode;
    this.atomMap = new HashMap<>();
  }

  /**
   * Constructs an {@code AtomConverter} using the given {@link IChemObjectBuilder}
   * and defaulting to {@link IChemObjectReader.Mode#RELAXED}.
   *
   * @param builder the CDK {@link IChemObjectBuilder} used to instantiate atoms
   */
  public AtomConverter(IChemObjectBuilder builder) {
    this(builder, IChemObjectReader.Mode.RELAXED);
  }

  /**
   * Converts a {@link CDAtom} to a CDK {@link IAtom}.
   * <p>
   * This method determines the correct atom type ({@link IAtom} or {@link IPseudoAtom})
   * based on the ChemDraw node type and label, sets its coordinates, charge,
   * and isotope information. Deuterium (D) and tritium (T) are automatically
   * converted to hydrogen atoms with the appropriate mass number.
   * </p>
   *
   * @param cdAtom the source ChemDraw atom to convert
   * @return the created {@link IAtom} instance
   * @throws CDKException if atom creation fails or invalid symbols are encountered
   *                      in strict mode
   */
  public IAtom convert(CDAtom cdAtom) throws CDKException {
    String atomSymbol = PeriodicTable.getSymbol(cdAtom.getElementNumber());
    String label = resolveTextLabel(cdAtom);
    IAtom atom;
    if (CDNodeType.ExternalConnectionPoint.equals(cdAtom.getNodeType())) {
      atom = this.builder.newInstance(IPseudoAtom.class, "*");
    } else if (isAbbreviation(cdAtom) || !CDNodeType.Element.equals(cdAtom.getNodeType())) {
      if (isHydrogenIsotope(label)) {
        atom = createAtom(atomSymbol, label);
      } else {
        atom = this.builder.newInstance(IPseudoAtom.class, label);
      }
    } else {
      atom = createAtom(atomSymbol, label);
    }
    // set coordinates
    setCoordinates(atom, cdAtom);
    // set formal charge
    setCharge(atom, cdAtom.getCharge());
    // check for isotopes
    if (!isHydrogenIsotope(label)) {
      try {
        configureIsotope(atom, cdAtom.getIsotope());
      } catch (IOException e) {
        logger.error("Unable to configure isotope for : " + atomSymbol);
        logger.error(e.getMessage());
      }
    }
    atomMap.putIfAbsent(cdAtom, atom);
    return atom;
  }

  /**
   * Creates a new {@link IAtom} based on the provided atomic symbol and label.
   * <p>
   * Handles isotopic hydrogen (D and T), unknown symbols, and pseudo-atoms.
   * In {@link IChemObjectReader.Mode#STRICT} mode, encountering an invalid symbol
   * results in a {@link CDKException}.
   * </p>
   *
   * @param symbol the chemical symbol
   * @param label  the textual label possibly representing isotopes or abbreviations
   * @return the created {@link IAtom} or {@link IPseudoAtom} if the symbol is invalid
   * @throws CDKException if strict mode is enabled and an invalid symbol is encountered
   */
  public IAtom createAtom(String symbol, String label) throws CDKException {
    Elements element = Elements.ofString(symbol);
    IAtom atom;
    try {
      if ("D".equals(label)) { // convert deuterium into hydrogen with mass 2
        atom = this.builder.newInstance(IAtom.class, Elements.Hydrogen.symbol());
        atom.setAtomicNumber(1);
        this.configureIsotope(atom, 2);
        return atom;
      } else if ("T".equals(label)) { // convert tritium into hydrogen with mass 3
        atom = this.builder.newInstance(IAtom.class, Elements.Hydrogen.symbol());
        atom.setAtomicNumber(1);
        this.configureIsotope(atom, 3);
        return atom;
      } else if (!element.equals(Elements.Unknown)) {
        atom = this.builder.newAtom();
        atom.setSymbol(element.symbol());
        atom.setAtomicNumber(element.number());
        return atom;
      }
    } catch (IllegalArgumentException | IOException e) {
      logger.error("Unexpected behaviour in creating an atom with symbol: " + symbol +
              ". PseudoAtom will be created.");
      logger.error(e.getMessage());
    }
    logger.info("Unknown symbol, PseudoAtom created for " + symbol + ".");
    if (IChemObjectReader.Mode.STRICT.equals(mode)) {
      throw new CDKException("Invalid atom symbol not allowed in strict mode: " + symbol);
    }
    atom = this.builder.newInstance(IPseudoAtom.class, label);
    atom.setSymbol(symbol);
    return atom;
  }


  /**
   * Sets 2D or 3D coordinates of the CDK atom based on the source {@link CDAtom}.
   * <p>
   * Note: ChemDraw uses an inverted Y-axis compared to CDK,
   * so the Y coordinate is negated during conversion.
   * </p>
   *
   * @param atom   the target {@link IAtom}
   * @param source the source {@link CDAtom} containing position data
   */
  private void setCoordinates(IAtom atom, CDAtom source) {
    double x;
    double y;
    double z;
    // ChemDraw and CDK use different coordination system, therefore the '-' in the y coordinate
    if (source.getPosition3D() != null) {
      CDPoint3D point3D = source.getPosition3D();
      x = point3D.getX();
      y = -point3D.getY();
      z = point3D.getZ();
      atom.setPoint3d(new Point3d(x, y, z));
      atom.setPoint2d(new Point2d(x, y));
    } else if (source.getPosition2D() != null) {
      CDPoint2D point2D = source.getPosition2D();
      x = point2D.getX();
      y = -point2D.getY();
      atom.setPoint2d(new Point2d(x, y));
    }
  }

  /**
   * Checks if the given symbol represents a hydrogen isotope
   * (deuterium {@code "D"} or tritium {@code "T"}).
   *
   * @param symbol the atom label or symbol
   * @return {@code true} if the symbol is {@code "D"} or {@code "T"}; otherwise {@code false}
   */
  private boolean isHydrogenIsotope(String symbol) {
    return "D".equals(symbol) || "T".equals(symbol);
  }


  /**
  * Configures an {@link IAtom} with the given isotope information using
  * {@link Isotopes} from the CDK database.
  *
  * @param atom        the atom to configure
  * @param massNumber  the isotope mass number
  * @throws IOException if the isotope configuration cannot be performed
  */
  void configureIsotope(IAtom atom, int massNumber) throws IOException {
    IIsotope iso = new Isotope(atom.getSymbol(), massNumber);
    Isotopes isofac = Isotopes.getInstance();
    isofac.configure(atom, iso);
  }

  /**
   * Sets the formal charge of the given atom, clamping the value to the
   * range of a signed byte ({@link Byte#MIN_VALUE} … {@link Byte#MAX_VALUE}).
   *
   * @param atom   the atom whose formal charge is to be set
   * @param charge the desired formal-charge value; will be clamped if it
   *               lies outside the byte range
   */
  private void setCharge(IAtom atom, int charge) {
    if(charge < Byte.MIN_VALUE){
      atom.setFormalCharge((int) Byte.MIN_VALUE);
    } else if (charge > Byte.MAX_VALUE) {
      atom.setFormalCharge((int) Byte.MAX_VALUE);
    } else {
      atom.setFormalCharge(charge);
    }
  }

  /**
   * Resolves the textual label of the given {@link CDAtom}.
   *
   * @param cdAtom the source atom
   * @return the resolved text label
   */
  private String resolveTextLabel(CDAtom cdAtom) {
    return Optional.ofNullable(cdAtom.getText())
            .map(CDText::getText)
            .map(CDStyledString::getText)
            .orElseGet(() ->
                    Optional.ofNullable(cdAtom.getLabelText())
                            .orElse(PeriodicTable.getSymbol(cdAtom.getElementNumber()))
            );
  }

  /**
   * Determines whether the given {@link CDAtom} represents a ChemDraw abbreviation
   * or unrecognized label, based on its {@code chemicalWarning} property.
   *
   * @param cdAtom the source atom
   * @return {@code true} if ChemDraw indicated the label cannot be interpreted;
   *         otherwise {@code false}
   */
  private boolean isAbbreviation(CDAtom cdAtom) {
    return Optional.ofNullable(cdAtom.getChemicalWarning())
            .map("ChemDraw can't interpret this label."::equals)
            .orElseGet(() -> false);
  }

  /**
   * Returns the internal mapping between source {@link CDAtom} and created
   * {@link IAtom}.
   *
   * @return an unmodifiable view of the atom map
   */
  public Map<CDAtom, IAtom> getAtomMap() { return atomMap; }
}
