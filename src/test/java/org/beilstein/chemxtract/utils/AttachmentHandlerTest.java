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
package org.beilstein.chemxtract.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AttachmentHandler} covering the multi-center and variable node logic. */
public class AttachmentHandlerTest {

  private static CDAtom element() {
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.Element);
    return atom;
  }

  private static CDBond bond(CDAtom begin, CDAtom end) {
    CDBond bond = new CDBond();
    bond.setBegin(begin);
    bond.setEnd(end);
    return bond;
  }

  private static CDAtom other(CDBond bond, CDAtom atom) {
    return atom.equals(bond.getBegin()) ? bond.getEnd() : bond.getBegin();
  }

  private static long coordinationBondsTo(CDFragment fragment, CDAtom central) {
    return fragment.getBonds().stream()
        .filter(CDBond::isCoordination)
        .filter(b -> central.equals(b.getBegin()) || central.equals(b.getEnd()))
        .count();
  }

  @Test
  public void resolveMultiAttachmentsBondsCentralToEveryAttachedAtom() {
    CDAtom central = element();
    CDAtom l1 = element();
    CDAtom l2 = element();
    CDAtom l3 = element();
    CDAtom multiNode = new CDAtom();
    multiNode.setNodeType(CDNodeType.MultiAttachment);
    multiNode.setAttachedAtoms(List.of(l1, l2, l3));

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(central, l1, l2, l3, multiNode));
    fragment.setBonds(List.of(bond(central, multiNode)));

    AttachmentHandler.resolveMultiAttachments(fragment);

    // The synthetic node and its bond are gone, replaced by a coordination bond per attached atom.
    assertThat(fragment.getAtoms()).doesNotContain(multiNode);
    assertThat(fragment.getBonds()).hasSize(3);
    assertThat(fragment.getBonds()).allMatch(CDBond::isCoordination);
    assertThat(coordinationBondsTo(fragment, central)).isEqualTo(3);
  }

  @Test
  public void resolveMultiAttachmentsWithoutAttachedAtomsDropsNodeAndBond() {
    CDAtom central = element();
    CDAtom multiNode = new CDAtom();
    multiNode.setNodeType(CDNodeType.MultiAttachment);
    // malformed node: no attachment list

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(central, multiNode));
    fragment.setBonds(List.of(bond(central, multiNode)));

    AttachmentHandler.resolveMultiAttachments(fragment);

    // The synthetic node and its dangling bond are removed; no coordination bond is created.
    assertThat(fragment.getAtoms()).containsExactly(central);
    assertThat(fragment.getBonds()).isEmpty();
  }

  @Test
  public void resolveMultiAttachmentsWithoutNodeLeavesFragmentUntouched() {
    CDAtom a = element();
    CDAtom b = element();
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(a, b));
    fragment.setBonds(List.of(bond(a, b)));

    AttachmentHandler.resolveMultiAttachments(fragment);

    assertThat(fragment.getAtoms()).containsExactly(a, b);
    assertThat(fragment.getBonds()).hasSize(1);
    assertThat(fragment.getBonds().get(0).isCoordination()).isFalse();
  }

  @Test
  public void expandVariableAttachmentsHandlesReversedSubstituentBond() {
    CDAtom substituent = element();
    CDAtom c1 = element();
    CDAtom c2 = element();
    CDAtom variableNode = new CDAtom();
    variableNode.setNodeType(CDNodeType.VariableAttachment);
    variableNode.setAttachedAtoms(List.of(c1, c2));

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(substituent, c1, c2, variableNode));
    // substituent bond stored with the variable node as the END atom (reversed orientation)
    fragment.setBonds(List.of(bond(substituent, variableNode)));

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(fragment);

    assertThat(variants).hasSize(2);
    for (CDFragment variant : variants) {
      assertThat(variant.getBonds()).hasSize(1);
      assertThat(other(variant.getBonds().get(0), substituent)).isIn(c1, c2);
    }
  }

  @Test
  public void expandVariableAttachmentsWithoutCandidatesReturnsOriginalFragment() {
    CDAtom substituent = element();
    CDAtom variableNode = new CDAtom();
    variableNode.setNodeType(CDNodeType.VariableAttachment);
    // node has no candidate atoms to enumerate

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(substituent, variableNode));
    fragment.setBonds(List.of(bond(variableNode, substituent)));

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(fragment);

    assertThat(variants).containsExactly(fragment);
  }

  @Test
  public void expandVariableAttachmentsWithoutNodeReturnsOriginalFragment() {
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(element(), element()));

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(fragment);

    assertThat(variants).containsExactly(fragment);
  }

  @Test
  public void expandVariableAttachmentsEnumeratesOnePerCandidate() {
    CDAtom substituent = element();
    CDAtom c1 = element();
    CDAtom c2 = element();
    CDAtom variableNode = new CDAtom();
    variableNode.setNodeType(CDNodeType.VariableAttachment);
    variableNode.setAttachedAtoms(List.of(c1, c2));

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(substituent, c1, c2, variableNode));
    fragment.setBonds(List.of(bond(variableNode, substituent)));

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(fragment);

    assertThat(variants).hasSize(2);
    // Each variant drops the synthetic node and bonds the substituent to exactly one candidate.
    for (CDFragment variant : variants) {
      assertThat(variant.getAtoms()).doesNotContain(variableNode);
      assertThat(variant.getBonds()).hasSize(1);
      CDBond newBond = variant.getBonds().get(0);
      assertThat(other(newBond, substituent)).isIn(c1, c2);
    }
    assertThat(other(variants.get(0).getBonds().get(0), substituent))
        .isNotEqualTo(other(variants.get(1).getBonds().get(0), substituent));
  }

  @Test
  public void expandVariableAttachmentsTakesCartesianProductAcrossNodes() {
    CDAtom s1 = element();
    CDAtom s2 = element();
    CDAtom c1 = element();
    CDAtom c2 = element();
    CDAtom c3 = element();
    CDAtom c4 = element();
    CDAtom node1 = new CDAtom();
    node1.setNodeType(CDNodeType.VariableAttachment);
    node1.setAttachedAtoms(List.of(c1, c2));
    CDAtom node2 = new CDAtom();
    node2.setNodeType(CDNodeType.VariableAttachment);
    node2.setAttachedAtoms(List.of(c3, c4));

    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(s1, s2, c1, c2, c3, c4, node1, node2));
    fragment.setBonds(List.of(bond(node1, s1), bond(node2, s2)));

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(fragment);

    // Two nodes with two candidates each -> 2 x 2 = 4 enumerated fragments, each with both
    // substituents reconnected.
    assertThat(variants).hasSize(4);
    assertThat(variants).allSatisfy(variant -> assertThat(variant.getBonds()).hasSize(2));
  }

  @Test
  public void hasVariableAttachmentReflectsPresenceOfVariableNode() {
    CDFragment plain = new CDFragment();
    plain.setAtoms(List.of(element(), element()));
    assertThat(AttachmentHandler.hasVariableAttachment(plain)).isFalse();

    CDAtom variableNode = new CDAtom();
    variableNode.setNodeType(CDNodeType.VariableAttachment);
    CDFragment withVariable = new CDFragment();
    withVariable.setAtoms(List.of(element(), variableNode));
    assertThat(AttachmentHandler.hasVariableAttachment(withVariable)).isTrue();
  }
}
