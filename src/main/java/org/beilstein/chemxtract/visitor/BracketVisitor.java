package org.beilstein.chemxtract.visitor;

import org.beilstein.chemxtract.cdx.*;
import org.beilstein.chemxtract.cdx.datatypes.CDBracketUsage;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor class for traversing a ChemDraw page and collecting multiple-atom brackets.
 */
public class BracketVisitor extends CDVisitor {

  private final List<CDBracket> multipleGroups;

  /**
   * Constructs a {@code BracketVisitor} and immediately traverses the given page
   * to collect all relevant multiple-group brackets.
   *
   * @param page the {@link CDPage} to traverse
   */
  public BracketVisitor(CDPage page) {
    multipleGroups = new ArrayList<>();
    page.accept(this);
  }

  /**
   * Visits a {@link CDBracket} node during traversal.
   * <p>
   * If the bracket represents a multiple group (usage is {@link CDBracketUsage#MultipleGroup})
   * and the bracketed objects contain atoms ({@link CDAtom}), it is added to the internal
   * list of multiple groups.
   * </p>
   *
   * @param bracket the {@link CDBracket} node being visited
   */
  @Override
  public void visitBracketedGroup(CDBracket bracket) {
    if (CDBracketUsage.MultipleGroup.equals(bracket.getBracketUsage()) &&
            bracket.getBracketedObjects() != null &&
            !bracket.getBracketedObjects().isEmpty() &&
            bracket.getBracketedObjects().get(0) instanceof CDAtom
    ) {
      multipleGroups.add(bracket);
    }
  }

  /**
   * Returns the list of multiple-group brackets collected during traversal.
   *
   * @return list of {@link CDBracket} objects representing multiple-atom groups
   */
  public List<CDBracket> getMultipleGroups() {
    return multipleGroups;
  }
}
