package org.beilstein.chemxtract.cdx;

import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Utils class for handling CD documents and their properties when converting CD fragments in CDK AtomContainers.
 *
 * @author Felix Bänsch
 */
public class CDDocumentUtils {

  /**
   * Constant representing the maximum acceptable distance between a fragment and a structure label.
   */
  private static final double LABEL_MAX_DISTANCE_CUT_OFF = 5.0;

  /**
   * Retrieves a list of all fragments present in the given CDDocument.
   *
   * @param document CDDocument containing the fragments to retrieve
   * @return list of CDFragment objects representing all fragments found in the document
   */
  public static List<CDFragment> getListOfFragments(CDDocument document) {
    // generate a list of all fragments
    Stack<CDObject> objects = new Stack<>();
    List<CDFragment> fragments = new ArrayList<>();
    objects.addAll(document.getPages());
    // breadth-first search
    while (!objects.isEmpty()) {
      CDObject object = objects.pop();
      if (object instanceof CDPage page) {
        objects.addAll(page.getBracketedGroups());
        objects.addAll(page.getFragments());
        objects.addAll(page.getGroups());
        objects.addAll(page.getNamedAlternativeGroups());
      } else if (object instanceof CDGroup group) {
        objects.addAll(group.getFragments());
        objects.addAll(group.getGroups());
        objects.addAll(group.getNamedAlternativeGroups());
      } else if (object instanceof CDAltGroup altgroup) {
        objects.addAll(altgroup.getFragments());
        objects.addAll(altgroup.getGroups());
      } else if (object instanceof CDFragment fragment) {
        fragments.add(fragment);
      }
    }
    return fragments;
  }

  /**
   * Recursively collects all {@link CDFragment} instances contained within a {@link CDGroup},
   * including those nested within subgroups.
   *
   * @param group the top-level {@link CDGroup} from which to collect fragments
   * @return a list of all {@link CDFragment} objects found within the group and its subgroups
   */
  public static List<CDFragment> getFragmentsOfGroup(CDGroup group) {
    // generate a list of all fragments
    Stack<CDObject> objects = new Stack<>();
    List<CDFragment> fragments = new ArrayList<>();
    objects.addAll(group.getGroups());
    objects.addAll(group.getFragments());
    while (!objects.isEmpty()) {
      CDObject object = objects.pop();
      if (object instanceof CDGroup internalGroup) {
        objects.addAll(internalGroup.getGroups());
        objects.addAll(internalGroup.getFragments());
      } else if (object instanceof CDFragment fragment) {
        fragments.add(fragment);
      }
    }
    return fragments;
  }

  /**
   * Extracts and returns a list of all {@code CDReactionStep} instances from a given {@code CDDocument}.
   * This method performs a breadth-first search (BFS) to traverse through the document structure,
   * including pages, reaction schemes, and reaction steps.
   *
   * @param document the {@code CDDocument} containing reaction data
   * @return a {@code List} of {@code CDReactionStep} objects found in the document
   */
  public static List<CDReactionStep> getListOfReactionSteps(CDDocument document) {
    // generate a list of all reaction steps
    Stack<Object> objects = new Stack<>();
    List<CDReactionStep> reactionSteps = new ArrayList<>();
    objects.addAll(document.getPages());
    // breadth-first search
    while (!objects.isEmpty()) {
      Object object = objects.pop();
      if (object instanceof CDPage page) {
        objects.addAll(page.getReactionSteps());
        objects.addAll(page.getReactionSchemes());
      } else if (object instanceof CDReactionScheme scheme) {
        objects.addAll(scheme.getSteps());
      } else if (object instanceof CDReactionStep step) {
        reactionSteps.add(step);
      }
    }
    return reactionSteps;
  }

  /**
   * Experimental code
   * Retrieves a map of residues from the given CDDocument
   *
   * @param document CDDocument containing the residues to retrieve
   * @return map where keys represent residue names and values represent their corresponding SMILES
   */
  public static Map<String,String> getResidues(CDDocument document) {
    // generate a list of all fragments
    Stack<CDObject> objects = new Stack<>();
    List<CDText> cDTexts = new ArrayList<>();

    objects.addAll(document.getPages());
    // breadth-first search
    while (!objects.isEmpty()) {
      CDObject object = objects.pop();
      if (object instanceof CDPage page) {
        objects.addAll(page.getBracketedGroups());
        objects.addAll(page.getFragments());
        objects.addAll(page.getGroups());
        objects.addAll(page.getNamedAlternativeGroups());
        objects.addAll(page.getTexts());
      } else if (object instanceof CDText text && text.getText().getText().startsWith("R")) {
        cDTexts.add(text);
      }
    }
    Map<String,String> residues = new HashMap<>((int) (cDTexts.size() / 0.75f + 2));
    for (CDText cdText : cDTexts) {
      String text = cdText.getText().getText().trim();
      int idx = text.indexOf('R');
      if (idx > -1)
        text = text.substring(idx);
      String[] items = text.split("=");
      if (items.length < 2)
        continue;
      String[] textResidues = items[1].split(", ");
      if (textResidues.length == 1) {
        residues.putIfAbsent(items[0], textResidues[0].trim());
      } else {
        for (String textResidue : textResidues) {
          int index1 = textResidue.indexOf('(');
          int index2 = textResidue.indexOf(')');
          String key = textResidue.substring(index1 + 1, index2).trim();
          String value;
          if (index2 == textResidue.length() - 1) {
            value = textResidue.substring(0, index1).trim();
          } else {
            value = textResidue.substring(index2 + 1).trim();
          }
          residues.putIfAbsent(key, value);
        }
      }
    }
    return residues;
  }

  /**
   * Experimental code
   * Returns a list of texts from the given CDDocument
   *
   * @param document CDDocument containing the residues to retrieve
   * @return list of CDText objects found in the document
   */
  public static List<CDText> getTexts(CDDocument document) {
    // generate a list of all fragments
    Stack<CDObject> objects = new Stack<>();
    List<CDText> texts = new ArrayList<>();
    objects.addAll(document.getPages());
    // breadth-first search
    while (!objects.isEmpty()) {
      CDObject object = objects.pop();
      if (object instanceof CDPage page) {
        objects.addAll(page.getTexts());
        objects.addAll(page.getGroups());
      }
      if (object instanceof CDGroup group) {
        objects.addAll(group.getCaptions());
        objects.addAll(group.getGroups());
      } else if (object instanceof CDText text) {
        texts.add(text);
      }
    }
    texts.addAll(filterFragmentsForText(document));
    return texts;
  }

  /**
   * Returns a list of all brackets of all pages of the document
   * @param document CDDocument containing the residues to retrieve
   * @return list of CDBracket objects found in the document
   */
  public static List<CDBracket> getBrackets(CDDocument document) {
    // generate a list of all fragments
    List<CDBracket> brackets = new ArrayList<>();
    Stack<CDObject> objects = new Stack<>();
    objects.addAll(document.getPages());
    while (!objects.isEmpty()) {
      CDObject object = objects.pop();
      if (object instanceof CDPage page) {
        objects.addAll(page.getBracketedGroups());
      } else if (object instanceof CDBracket bracket) {
        objects.addAll(bracket.getBrackets());
        brackets.add(bracket);
      }
    }
    return brackets;
  }

  /**
   * Experimental code
   * Checks if the given fragment contains an R-group.
   *
   * @param fragment The fragment to check
   * @return {@code true} if the fragment contains an R-group, otherwise {@code false}
   * @Depreacted use CDFragment.hasRGroup instead
   */
  @Deprecated
  public static boolean containsRGroup(CDFragment fragment) {
    for (CDAtom atom : fragment.getAtoms()) {
      if (!atom.getFragments().isEmpty() && atom.getNodeType() != CDNodeType.Unspecified) {
        for (CDFragment tmpFragment : atom.getFragments()) {
          if (!tmpFragment.getAtoms().isEmpty()) {
            return containsRGroup(tmpFragment);
          }
        }
      } else {
        if (atom.getLabelText() != null && atom.getLabelText().contains("R"))
          return true;
      }
    }
    return false;
  }

  /**
   * Checks if a fragment is valid.
   *
   * @param fragment CDFragment to check
   * @return {@code true} if CDFragment is valid (i.e., contains at least two atoms), otherwise {@code false}.
   * * @Depreacted use CDFragment.hasRGroup instead
   */

  public static boolean isFragmentValid(CDFragment fragment) {
    return fragment.getAtoms() != null && fragment.getAtoms().size() >= 2;
  }

  /**
   * Experimental code
   * Filters a CDDocument object and returns a new list containing only the text fragments.
   * This method delegates the task of filtering fragments to the filterFragmentsForText method,
   * assuming getListOfFragments provides a list of all fragments within the document.
   *
   * @param document The CDDocument object to be filtered.
   * @return A new list containing only the CDText objects extracted from the text fragments within the document.
   */
  public static List<CDText> filterFragmentsForText(CDDocument document) {
    return filterFragmentsForText(getListOfFragments(document));
  }

  /**
   * Experimental code
   * Filters a list of CDFragments and returns a new list containing only the text fragments.
   * This method iterates through the fragments and applies the following criteria to identify text fragments:
   *     The fragment is not valid (assumed to be determined by the isFragmentValid method).
   *     The fragment contains only one atom.
   *     The single atom's node type is CDNodeType.Unspecified (assumed to represent text data).
   * If a fragment meets these criteria, the text content is extracted from the single atom using the getText method
   * and added to the resulting list.
   *
   * @param fragments The list of CDFragment objects to be filtered.
   * @return A new list containing only the CDText objects extracted from the text fragments.
   */
  public static List<CDText> filterFragmentsForText(List<CDFragment> fragments) {
    List<CDText> textFragments = new ArrayList<>();
    for (CDFragment fragment : fragments) {
      if (!isFragmentValid(fragment) && fragment.getAtoms().size() == 1) {
        CDAtom atom = fragment.getAtoms().get(0);
        if (atom.getNodeType() != CDNodeType.Unspecified) {
          continue;
        }
        textFragments.add(atom.getText());
      }
    }
    return textFragments;
  }

  /**
   * Experimental code
   * Checks if a CDText object contains a structure label.
   * This method calls the getStructureLabelString method to extract the potential label string
   * from the text object. If the extracted string is not empty, it is considered a structure label
   * and this method returns true. Otherwise, it returns false.
   *
   * @param text The CDText object to be checked.
   * @return True if the text object contains a structure label, false otherwise.
   */
  public static boolean containsStructureLabel(CDText text) {
    return !getStructureLabelString(text).isEmpty();
  }

  /**
   * Experimental code
   * Filters a list of CDText objects and returns a new list containing only the structure labels.
   *
   * @param texts The list of CDText objects to be filtered.
   * @return A new list containing only the CDText objects identified as structure labels.
   */
  public static List<CDText> getStructureLabels(List<CDText> texts) {
    List<CDText> labels = new ArrayList<>();
    for (CDText text : texts) {
      if (containsStructureLabel(text))
        labels.add(text);
    }
    return labels;
  }

  /**
   * Experimental code
   * Extracts and returns the structure label string from a CDText object.
   * The method iterates through the text chunks and searches for a chunk that meets these criteria:
   *     The chunk's font is bold.
   *     The chunk's text contains at least one digit character.
   * If such a chunk is found, its text content is considered the structure label and is returned.
   * Otherwise, an empty string is returned.
   *
   * @param text The CDText object from which to extract the label string.
   * @return The structure label string extracted from the text chunk, or an empty string if no suitable chunk is found.
   */
  public static String getStructureLabelString(CDText text) {
    if (text == null || text.getText() == null)
      return "";
    String label = "";
    List<CDStyledString.CDXChunk> chunks = text.getText().getChunks();
    for (var chunk : chunks) {
      String textValue = chunk.getText();
      boolean isBold = chunk.getFontType().isBold();
      if (isBold && textValue.chars().anyMatch(Character::isDigit)) {
        label = textValue;
      }
    }
    return label;
  }

  /**
   * Experimental code
   * Calculates and returns the center point of a CDText object.
   *
   * @param text The CDText object.
   * @return A Point2D object representing the center point of the text.
   */
  public static Point2D getCenterOfCDText(CDText text) {
    return getCenterOfCDRectangle(text.getBounds());
  }

  /**
   * Experimental code
   * Calculates and returns the center point of a CDFragment object.
   *
   * @param fragment The CDFragment object.
   * @return A Point2D object representing the center point of the fragment.
   */
  public static Point2D getCenterOfCDFragment(CDFragment fragment) {
    return getCenterOfCDRectangle(fragment.getBounds());
  }

  /**
   * Experimental code
   * Calculates and returns the center point of a CDRectangle object.
   *
   * @param rectangle The CDRectangle object.
   * @return A Point2D object representing the center point of the rectangle.
   */
  public static Point2D getCenterOfCDRectangle(CDRectangle rectangle) {
    double centerX = rectangle.getCenterX();
    double centerY = rectangle.getCenterY();
    return new Point2D.Double(centerX, centerY);
  }

  /**
   * Experimental code
   * Calculates the distance between the center of theCDFragment and
   * the center of the CDText object.
   *
   * @param fragment The CDFragment object.
   * @param text The CDText object.
   * @return The distance between the center of the fragment and the center of the text as a double.
   */
  public static double getDistanceOfFragmentToText(CDFragment fragment, CDText text) {
    Point2D textCenter = getCenterOfCDText(text);
    Point2D fragmentCenter = getCenterOfCDFragment(fragment);
    return fragmentCenter.distance(textCenter);
  }

  /**
   * Experimental code
   * Assigns a label to a given fragment based on proximity and distance cutoff.
   * This method iterates through the list of text elements and searches for a structure label
   * (determined by the containsStructureLabel method) that is closest to the fragment.
   *
   * @param fragment The CDFragment object to which a label will be assigned.
   * @param texts The list of CDText objects to be considered as potential labels.
   */
  public static void assignLabelToFragment(CDFragment fragment, List<CDText> texts) {
    double shortestDist = Double.MAX_VALUE;
    double cutOff = LABEL_MAX_DISTANCE_CUT_OFF + getLongestSideOfCDRectangle(fragment.getBounds());
    CDText label = null;
    for (CDText text : texts) {
      if (!containsStructureLabel(text))
        continue;
      double dist = getDistanceOfFragmentToText(fragment, text);
      if (dist < shortestDist && dist < cutOff) {
        shortestDist = dist;
        label = text;
      }
    }
    if (label != null)
      fragment.getTexts().add(label);
  }

  /**
   * Returns the longest side of a CDRectangle object.
   * This method compares the width and height of the rectangle and returns the larger value.
   *
   * @param rectangle The CDRectangle object for which the longest side is to be calculated.
   * @return The length of the longest side of the rectangle as a double.
   **/
  public static double getLongestSideOfCDRectangle(CDRectangle rectangle) {
    return Math.max(rectangle.getHeight(), rectangle.getWidth());
  }

  /**
   * Extracts a list of CDBracket objects from a provided list that are associated with a given CDFragment.
   *
   * @param fragment The CDFragment object for which to find associated brackets.
   * @param brackets The list of CDBracket objects to be searched.
   * @return A new list containing only the CDBracket objects that are associated with the fragment (i.e. contain one or more of its atoms).
   */
  public static List<CDBracket> getFragmentBrackets(CDFragment fragment, List<CDBracket> brackets) {
    List<CDBracket> fragmentBrackets = new ArrayList<>();
    outer:
    for (CDBracket b : brackets) {
      for (Object o : b.getBracketedObjects()) {
        if (o instanceof CDAtom) {
          if (fragment.getAtoms().contains(o)) {
            fragmentBrackets.add(b);
            continue outer;
          }
        }
      }
    }
    return fragmentBrackets;
  }

  /**
   * Checks whether the given node type contains chemical knowledge in the form of a fragment or similar, and returns true if so.
   * CDNodeType that are defined to contain chemical knowledge are Fragment, Nickname and GenericNickname
   *
   * @param type CDNodeType
   * @return true whether the given node type contains chemical knowledge in the form of a fragment or similar.
   */
  public static boolean hasNodeTypeChemicalKnowledge(CDNodeType type) {
    switch (type) {
      case Fragment, Nickname, GenericNickname -> {
        return true;
      }
      default -> {
        return false;
      }
    }
  }
}
