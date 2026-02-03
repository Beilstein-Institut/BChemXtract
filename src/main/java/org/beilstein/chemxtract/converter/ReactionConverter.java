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

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import javax.vecmath.Point2d;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.CDArrowHeadPositionType;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint3D;
import org.beilstein.chemxtract.lookups.ReactionAgents;
import org.beilstein.chemxtract.lookups.UnwantedWords;
import org.beilstein.chemxtract.model.BCXReaction;
import org.beilstein.chemxtract.model.BCXReactionComponent;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.utils.Definitions;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Converts ChemDraw {@link CDReactionStep} objects into CDK {@link IReaction} instances.
 *
 * <p>The {@code ReactionConverter} acts as a bridge between ChemDraw's reaction model and the CDK
 * representation. It transforms reactants, products, and agents into {@link IAtomContainer}
 * structures, determines reaction directionality, and collects unrecognized textual agents for
 * reporting.
 *
 * <h2>Usage</h2>
 *
 * The converter requires a mapping of ChemDraw fragments to CDK atom containers (typically produced
 * by {@link org.beilstein.chemxtract.xtractor.ReactionXtractor}). It uses this mapping to
 * reconstruct the full {@link IReaction} object.
 *
 * <pre>{@code
 * ReactionConverter converter = new ReactionConverter(fragmentMap, SilentChemObjectBuilder.getInstance());
 * IReaction reaction = converter.convert(cdReactionStep);
 * }</pre>
 */
public class ReactionConverter {

  private final IChemObjectBuilder builder;
  private final SmilesParser smilesParser;
  private static final Log logger = LogFactory.getLog(ReactionConverter.class);
  private final Map<CDFragment, BCXSubstance> fragmentsAtomContainerMap;
  private final Set<String> unknowns;
  private boolean sanitize = false;

  /**
   * Constructs a {@code ReactionConverter} with the specified fragment–substance mapping and CDK
   * object builder.
   *
   * @param fragmentsAtomContainerMap a mapping between {@link CDFragment} instances and their
   *     corresponding {@link BCXSubstance} representations
   * @param builder the {@link IChemObjectBuilder} used to create CDK chemical objects
   * @param sanitize if {@code true}, enables sanitization of the reaction during the conversion
   *     process: only structures in line with the reaction arrow are considered; if {@code false},
   *     reactions are processed as-is
   */
  public ReactionConverter(
      Map<CDFragment, BCXSubstance> fragmentsAtomContainerMap,
      IChemObjectBuilder builder,
      boolean sanitize) {
    this.fragmentsAtomContainerMap = fragmentsAtomContainerMap;
    this.builder = builder;
    this.sanitize = sanitize;
    this.smilesParser = new SmilesParser(this.builder);
    this.unknowns = new HashSet<>();
  }

  /**
   * Converts a {@link CDReactionStep} from ChemDraw into a CDK {@link IReaction} object.
   *
   * @param reactionStep the ChemDraw {@link CDReactionStep} to convert
   * @return a new {@link IReaction} as an Optional object
   */
  public Optional<IReaction> convert(CDReactionStep reactionStep) {
    IReaction cdkReaction = new Reaction();
    try {
      for (Object reactant : reactionStep.getReactants()) {
        if (this.sanitize && !isInLineWithArrow(reactant, reactionStep.getArrows())) {
          continue;
        }
        processReactionComponents(reactant, cdkReaction::addReactant);
      }
      for (Object product : reactionStep.getProducts()) {
        if (this.sanitize && !isInLineWithArrow(product, reactionStep.getArrows())) {
          continue;
        }
        processReactionComponents(product, cdkReaction::addProduct);
      }
      for (Object agent : getAgents(reactionStep)) {
        processReactionComponents(agent, cdkReaction::addAgent);
      }
      if (cdkReaction.getReactants().isEmpty() || cdkReaction.getProducts().isEmpty()) {
        logger.error("Reaction has zero reactants or zero products.");
        return Optional.empty();
      }
    } catch (IOException | CDKException e) {
      logger.error("Conversion of a reaction component has failed.", e);
      return Optional.empty();
    }
    cdkReaction.setDirection(
        getReactionDirection(reactionStep, cdkReaction.getReactants(), cdkReaction.getProducts()));
    return Optional.of(cdkReaction);
  }

  /**
   * Processes a list of reaction components and adds them to both the internal CDK {@link
   * IReaction} and the BOA {@link BCXReaction} representation.
   *
   * <p>This method supports {@link CDFragment}, {@link CDGroup}, and {@link String} components. If
   * a {@code String} component matches a known abbreviation, it will be parsed using a SMILES
   * parser and converted into a {@link BCXReactionComponent}. If not recognized, the component will
   * be logged and added to the {@code unkowns} list.
   *
   * @param component component (reactants, products, or agents)
   * @param addCdkComponent a consumer that adds an {@link IAtomContainer} to the internal CDK
   *     reaction
   * @throws CDKException if an error occurs while parsing SMILES or generating InChI identifiers
   */
  private void processReactionComponents(Object component, Consumer<IAtomContainer> addCdkComponent)
      throws CDKException, IOException {
    if (component instanceof CDFragment fragment
        && fragmentsAtomContainerMap.containsKey(fragment)) {
      addCdkComponent.accept(fragmentsAtomContainerMap.get(fragment).getAtomContainer());
    } else if (component instanceof CDGroup group) {
      // At the moment do not handle grouped reaction fragments
      IAtomContainer groupContainer = builder.newAtomContainer();
      if (CDDocumentUtils.getFragmentsOfGroup(group).size() == 1) {
        CDFragment fragment = CDDocumentUtils.getFragmentsOfGroup(group).get(0);
        if (fragmentsAtomContainerMap.containsKey(fragment)) {
          groupContainer.add(fragmentsAtomContainerMap.get(fragment).getAtomContainer());
        } else {
          return;
        }
      }
      addCdkComponent.accept(groupContainer);
    } else if (component instanceof String text) {
      String key = text.toLowerCase();
      if (ReactionAgents.contains(key)) {
        IAtomContainer ac = smilesParser.parseSmiles(ReactionAgents.get(key));
        addCdkComponent.accept(ac);
      } else {
        logger.warn("Agent may not be converted (correctly): " + text);
        unknowns.add(text);
      }
    }
  }

  /**
   * Determines the direction of a chemical reaction based on the reaction step and graphical arrow
   * representation.
   *
   * @param reactionStep the {@code CDReactionStep} containing the reaction details
   * @param reactants the set of reactant molecules
   * @param products the set of product molecules
   * @return the determined {@code IReaction.Direction} for the reaction
   * @throws NullPointerException if {@code reactionStep} is null
   */
  private IReaction.Direction getReactionDirection(
      CDReactionStep reactionStep, IAtomContainerSet reactants, IAtomContainerSet products) {
    Objects.requireNonNull(reactionStep, "CDReactionStep must not be null.");

    List<Object> arrows = reactionStep.getArrows();

    if (arrows.get(0) instanceof CDGraphic graphic
        && graphic.getSupersededBy() instanceof CDArrow arrow) {
      if (arrow.getArrowHeadPositionStart() == CDArrowHeadPositionType.HalfLeft
          && arrow.getArrowHeadPositionTail() == CDArrowHeadPositionType.HalfLeft)
        return IReaction.Direction.BIDIRECTIONAL;
      if (arrow.getArrowHeadPositionStart() == CDArrowHeadPositionType.HalfRight
          && arrow.getArrowHeadPositionTail() == CDArrowHeadPositionType.HalfRight)
        return IReaction.Direction.BIDIRECTIONAL;

      CDPoint3D head = arrow.getHead3D();

      double distToReactants = this.getDistanceToCompounds2D(reactants, head);
      double distToProducts = this.getDistanceToCompounds2D(products, head);

      if (distToReactants < distToProducts) return IReaction.Direction.BACKWARD;
    }
    return IReaction.Direction.FORWARD;
  }

  /**
   * Calculates the squared distance from a given 3D point (projected onto 2D) to the closest atom
   * in a set of compounds.
   *
   * <p>The Z-coordinate of the point is ignored, and the Y-coordinate is inverted to align with the
   * 2D coordinate system used in the atom containers.
   *
   * @param compounds the set of atom containers (compounds) to check for proximity
   * @param head the 3D point (typically representing a head or reference point) to measure from
   * @return the squared distance to the nearest atom in the set of compounds
   */
  private double getDistanceToCompounds2D(IAtomContainerSet compounds, CDPoint3D head) {
    Point2d headPoint = new Point2d(head.getX(), -head.getY());

    double distClosestCompound = Double.MAX_VALUE;

    for (IAtomContainer ac : compounds) {
      for (IAtom atom : ac.atoms()) {
        double atomDistSquared;
        if (atom.getPoint2d() != null)
          atomDistSquared = this.getPoint2DDistanceSquared(headPoint, atom.getPoint2d());
        else
          atomDistSquared =
              this.getPoint2DDistanceSquared(
                  headPoint, new Point2d(atom.getPoint3d().x, atom.getPoint3d().y));
        distClosestCompound = Math.min(distClosestCompound, atomDistSquared);
      }
    }
    return distClosestCompound;
  }

  /**
   * Calculates the squared Euclidean distance between two 2D points.
   *
   * <p>This method avoids the square root calculation, making it more efficient when comparing
   * distances rather than needing the exact distance.
   *
   * @param point1 the first 2D point
   * @param point2 the second 2D point
   * @return the squared distance between {@code point1} and {@code point2}
   */
  private double getPoint2DDistanceSquared(Point2d point1, Point2d point2) {
    return (point2.x - point1.x) * (point2.x - point1.x)
        + (point2.y - point1.y) * (point2.y - point1.y);
  }

  /**
   * Extracts and returns the set of agent compounds (substances above or below the reaction arrow)
   * from a given reaction step.
   *
   * <p>This method processes various object types that may represent agents, such as {@code
   * CDFragment}, {@code CDGroup}, {@code CDText}, and {@code String}. In the case of simple
   * text-based agents, they are treated as textual labels.
   *
   * @param reactionStep the reaction step from which to extract agents
   * @return an {@code IAtomContainerSet} representing the agents
   * @throws CDKException if an unknown object type is encountered during processing
   */
  private List<Object> getAgents(CDReactionStep reactionStep) throws CDKException {
    List<Object> objects = new ArrayList<>();
    objects.addAll(reactionStep.getObjectsAboveArrow());
    objects.addAll(reactionStep.getObjectsBelowArrow());

    List<Object> agents = new ArrayList<>();

    for (Object object : objects) {
      if (object instanceof CDFragment fragment) {
        if (fragment.getAtoms().size() == 1
            && fragment.getAtoms().get(0).getNodeType() == CDNodeType.Unspecified
            && fragment.getAtoms().get(0).getText() != null) {
          agents.add(fragment.getAtoms().get(0).getText().getText().getText());
        } else agents.add(fragment);
      } else if (object instanceof String agentName) {
        agents.add(agentName);
      } else if (object instanceof CDText cdText) {
        String text = cdText.getText().getText();
        List<String> agentStrings = this.filterAndSplitAgentsString(text);
        agents.addAll(agentStrings);
      } else if (object instanceof CDGroup group) {
        agents.add(group);
      } else {
        throw new CDKException("Unknown agent object type!");
      }
    }
    return agents;
  }

  /**
   * Splits and filters agent strings into meaningful components.
   *
   * <p>Removes numeric values and common unwanted words using {@link
   * Definitions#AGENTS_SPLIT_REGEX} and {@link UnwantedWords}.
   *
   * @param agentsString the raw agent label text
   * @return a filtered list of agent strings
   */
  private List<String> filterAndSplitAgentsString(String agentsString) {
    if (agentsString.isEmpty()) {
      return new ArrayList<>();
    }
    String[] possibleAgents = agentsString.split(Definitions.AGENTS_SPLIT_REGEX);
    return Arrays.stream(possibleAgents)
        .filter(a -> a.length() > 1)
        .filter(a -> !a.matches("-?\\d+(\\.\\d+)?"))
        .filter(
            a -> {
              try {
                return !UnwantedWords.contains(a);
              } catch (IOException e) {
                logger.error(e.getMessage());
                return false;
              }
            })
        .toList();
  }

  /**
   * Checks if a component lies on the infinite line defined by the bounding box diagonal of the
   * first arrow in the list.
   *
   * @param component The object to check for intersection (must be an instance of {@code
   *     CDFragment}).
   * @param arrows A list of objects, where the first element is treated as the reference arrow
   */
  private boolean isInLineWithArrow(Object component, List<Object> arrows) {

    if (!arrows.isEmpty()
        && (arrows.get(0) instanceof CDGraphic arrow)
        && (component instanceof CDFragment fragment)) {
      return intersectsRectangle(
          arrow.getBounds().getMinX(), arrow.getBounds().getMinY(),
          arrow.getBounds().getMaxX(), arrow.getBounds().getMaxY(),
          fragment.getBounds().getMinX(), fragment.getBounds().getMinY(),
          fragment.getBounds().getMaxX(), fragment.getBounds().getMaxY());
    }
    return false;
  }

  /**
   * Determines whether a line defined by two points intersects with an axis-aligned rectangle.
   *
   * <p>This method calculates the general line equation (ax + by + c = 0) passing through the
   * points {@code (gx1, gy1)} and {@code (gx2, gy2)}. It then evaluates this equation at the four
   * corners of the specified rectangle.
   *
   * <p>If the evaluated values of the corners include both non-positive and non-negative results,
   * the rectangle lies on the line or crosses it (vertices are on opposite sides of the line). This
   * method is based on the separation theorem.
   *
   * <p><strong>Note:</strong> This checks against an <em>infinite line</em>, not a line segment.
   * Even if the segment defined by the {@code g} points is far away from the rectangle, this method
   * will return {@code true} if the line extending through those points passes through the
   * rectangle.
   *
   * @param gx1 The X coordinate of the first point defining the line.
   * @param gy1 The Y coordinate of the first point defining the line.
   * @param gx2 The X coordinate of the second point defining the line.
   * @param gy2 The Y coordinate of the second point defining the line.
   * @param rx1 The X coordinate of the first corner of the rectangle.
   * @param ry1 The Y coordinate of the first corner of the rectangle.
   * @param rx2 The X coordinate of the diagonally opposite corner of the rectangle.
   * @param ry2 The Y coordinate of the diagonally opposite corner of the rectangle.
   * @return {@code true} if the infinite line passes through or touches the rectangle; {@code
   *     false} if the rectangle lies entirely on one side of the line.
   */
  private boolean intersectsRectangle(
      double gx1,
      double gy1,
      double gx2,
      double gy2,
      double rx1,
      double ry1,
      double rx2,
      double ry2) {

    // Line in normal form ax + by + c = 0
    double a = gy2 - gy1;
    double b = gx1 - gx2;
    double c = gx2 * gy1 - gx1 * gy2;

    // Rectangle formed by diagonal points
    double xmin = Math.min(rx1, rx2);
    double xmax = Math.max(rx1, rx2);
    double ymin = Math.min(ry1, ry2);
    double ymax = Math.max(ry1, ry2);

    // Vertices
    double f1 = a * xmin + b * ymin + c;
    double f2 = a * xmin + b * ymax + c;
    double f3 = a * xmax + b * ymin + c;
    double f4 = a * xmax + b * ymax + c;

    double min = Math.min(Math.min(f1, f2), Math.min(f3, f4));
    double max = Math.max(Math.max(f1, f2), Math.max(f3, f4));

    return min <= 0.0 && max >= 0.0;
  }

  /**
   * Returns the set of unknown agent strings that could not be resolved or converted during
   * compound processing.
   *
   * <p>These typically represent agent labels that were not found in the abbreviation map and could
   * not be parsed as valid chemical structures.
   *
   * @return a set of unresolved or unrecognized agent strings
   */
  public Set<String> getUnknowns() {
    return unknowns;
  }
}
