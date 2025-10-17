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
package org.beilstein.chemxtract.xtractor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.converter.ReactionConverter;
import org.beilstein.chemxtract.model.BCXReaction;
import org.beilstein.chemxtract.model.BCXReactionComponent;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.utils.ChemicalUtils;
import org.beilstein.chemxtract.visitor.FragmentVisitor;
import org.beilstein.chemxtract.visitor.ReactionStepVisitor;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IReaction;

import java.io.IOException;
import java.util.*;

/**
 * Class for extracting chemical reactions from a {@link CDDocument}.
 * <p>
 * This class traverses ChemDraw pages, fragments, and reaction steps to construct
 * {@link BCXReaction} objects, which contain reactants, products, agents, and
 * associated identifiers such as InChI and InChIKey.
 */
public class ReactionXtractor {

  private final IChemObjectBuilder builder;
  private final Log logger = LogFactory.getLog(ReactionXtractor.class);
  private Set<String> unknowns;

  /**
   * Constructs a {@code ReactionXtractor} with a custom {@link IChemObjectBuilder}.
   *
   * @param builder the CDK chem object builder used for creating CDK objects
   */
  public ReactionXtractor(IChemObjectBuilder builder) {
    this.builder = builder;
  }

  /**
   * Constructs a {@code ReactionXtractor} using the default CDK object builder.
   */
  public ReactionXtractor() {
    this(DefaultChemObjectBuilder.getInstance());
  }

  /**
   * Extracts all reactions from the given ChemDraw document.
   * <p>
   * For each page, fragments are visited and converted into {@link BCXReactionComponent}s.
   * Reaction steps are then converted to {@link BCXReaction} objects with reactants,
   * products, and agents linked to their corresponding components.
   * </p>
   *
   * @param document the ChemDraw {@link CDDocument} to extract reactions from
   * @return a list of extracted {@link BCXReaction} objects
   */
  public List<BCXReaction> xtract(CDDocument document) {
    List<BCXReaction> reactions = new ArrayList<>();
    SubstanceXtractor substanceXtractor = new SubstanceXtractor(this.builder);
    this.unknowns = new HashSet<>();

    for (CDPage page : document.getPages()) {
      FragmentVisitor fragmentVisitor = new FragmentVisitor(page);
      List<CDFragment> fragments = fragmentVisitor.getFragments();
      Map<CDFragment, BCXSubstance> fragmentSubstanceMap = new HashMap<>();
      Map<IAtomContainer, BCXReactionComponent> atomContainerReactionComponentMap = new HashMap<>();
      for (CDFragment fragment : fragments) {
        try {
          Optional<BCXSubstance> substance = Optional.ofNullable(substanceXtractor.xtractSubstance(fragment, page));
          if (substance.isEmpty()) {
            continue;
          }
          fragmentSubstanceMap.putIfAbsent(fragment, substance.get());
          BCXReactionComponent component = convertToReactionComponent(fragment, substance.get().getAtomContainer());
          atomContainerReactionComponentMap.putIfAbsent(substance.get().getAtomContainer(), component);
        } catch (CDKException | IOException e) {
          logger.error(e.getMessage());
        }
      }
      ReactionStepVisitor rsVisitor = new ReactionStepVisitor(page);
      List<CDReactionStep> steps = rsVisitor.getReactionSteps();
      for (CDReactionStep step : steps) {
        try {
          ReactionConverter reactionConverter = new ReactionConverter(fragmentSubstanceMap, builder);
          IReaction cdkReaction = reactionConverter.convert(step);
          BCXReaction reaction = createBcxReaction(cdkReaction, atomContainerReactionComponentMap);
          reactions.add(reaction);
          unknowns.addAll(reactionConverter.getUnknowns());
        } catch (CDKException e) {
          logger.error(e.getMessage());
        }

      }
    }
    return reactions;
  }

  /**
   * Converts a CDK {@link IReaction} and its mapping of {@link IAtomContainer} to
   * {@link BCXReactionComponent} into a {@link BCXReaction} with proper identifiers and components.
   *
   * @param cdkReaction the CDK {@link IReaction} to convert
   * @param atomContainerBCXReactionComponentMap a mapping of atom containers to reaction components
   * @return a {@link BCXReaction} containing all reactants, products, and agents
   */
  private BCXReaction createBcxReaction(IReaction cdkReaction, Map<IAtomContainer, BCXReactionComponent> atomContainerBCXReactionComponentMap) {
    BCXReaction reaction = new BCXReaction();
    reaction.setReactionSmiles(ChemicalUtils.createAbsoluteReactionSmiles(cdkReaction));
    reaction.setRinchi(ChemicalUtils.getRInChI(cdkReaction));
    reaction.setLongRinchiKey(ChemicalUtils.getLongRInChIKey(cdkReaction));
    reaction.setShortRinchiKey(ChemicalUtils.getShortRInChIKey(cdkReaction));
    reaction.setWebRinchiKey(ChemicalUtils.getWebRInChIKey(cdkReaction));
    reaction.setAuxInfo(ChemicalUtils.getRAuxInfo(cdkReaction));
    for (IAtomContainer reactant : cdkReaction.getReactants()) {
      reaction.addReactant(atomContainerBCXReactionComponentMap.get(reactant));
    }
    for(IAtomContainer product : cdkReaction.getProducts()) {
      reaction.addProduct(atomContainerBCXReactionComponentMap.get(product));
    }
    for(IAtomContainer agent : cdkReaction.getAgents()) {
      reaction.addAgent(atomContainerBCXReactionComponentMap.get(agent));
    }
    return reaction;
  }

  /**
   * Converts a {@link CDFragment} and its associated {@link IAtomContainer} into a {@link BCXReactionComponent}.
   * <p>
   * This method attempts to generate InChI and InChIKey identifiers from the given atom container,
   * and extracts the fragment's spatial bounds to populate the {@code ReactionComponent}.
   * </p>
   *
   * @param fragment the {@code CDFragment} representing the source structure
   * @param atomContainer the corresponding CDK {@code IAtomContainer} for structure data
   * @return a {@link BCXReactionComponent} containing InChI information and layout bounds
   * @throws CDKException if InChI generation fails critically (non-recoverable)
   */
  private BCXReactionComponent convertToReactionComponent(CDFragment fragment, IAtomContainer atomContainer) throws CDKException {
    BCXReactionComponent component = new BCXReactionComponent();
    try {
      InChIGenerator gen = ChemicalUtils.getInChI(atomContainer);
      component.setInchi(gen.getInchi());
      component.setInchiKey(gen.getInchiKey());
    } catch (CDKException e) {
      logger.error("Failed to generate InChI for reaction component: " + e.getMessage(), e);
    }
    Optional<CDRectangle> boundsOptional = Optional.ofNullable(fragment.getBounds());
    // add bounds
    if (boundsOptional.isPresent()) {
      CDRectangle bounds = boundsOptional.get();
      component.setCdxTop(bounds.getTop());
      component.setCdxLeft(bounds.getLeft());
      component.setCdxRight(bounds.getRight());
      component.setCdxBottom(bounds.getBottom());
    }
    return component;
  }

  /**
   * Returns the set of unknown or unrecognized structures encountered during extraction.
   *
   * @return a set of unknown structure identifiers as strings
   */
  public Set<String> getUnknowns() {
    return unknowns;
  }
}
