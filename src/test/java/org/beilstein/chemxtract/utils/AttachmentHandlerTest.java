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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.beilstein.chemxtract.cdx.CDAtom;
import org.beilstein.chemxtract.cdx.CDBond;
import org.beilstein.chemxtract.cdx.CDFragment;
import org.beilstein.chemxtract.cdx.CDText;
import org.beilstein.chemxtract.cdx.datatypes.CDNodeType;
import org.beilstein.chemxtract.cdx.datatypes.CDPoint2D;
import org.beilstein.chemxtract.cdx.datatypes.CDStyledString;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AttachmentHandler} covering the multi-center and variable node logic. */
public class AttachmentHandlerTest {

  private static CDAtom element() {
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.Element);
    return atom;
  }

  private static CDAtom element(float x, float y) {
    CDAtom atom = element();
    atom.setPosition2D(new CDPoint2D(x, y));
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
  public void expandVariableAttachmentsSkipsFragmentsBeyondTheCombinationLimit() {
    // 12 variable nodes with three candidates each multiply out to 531441 isomers - far past what
    // a drawing can mean, and enough to exhaust the heap when enumerated.
    CDFragment fragment = new CDFragment();
    List<CDAtom> atoms = new ArrayList<>();
    List<CDBond> bonds = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      CDAtom substituent = element();
      CDAtom c1 = element();
      CDAtom c2 = element();
      CDAtom c3 = element();
      CDAtom node = new CDAtom();
      node.setNodeType(CDNodeType.VariableAttachment);
      node.setAttachedAtoms(List.of(c1, c2, c3));
      atoms.addAll(List.of(substituent, c1, c2, c3, node));
      bonds.add(bond(node, substituent));
    }
    fragment.setAtoms(atoms);
    fragment.setBonds(bonds);

    assertThat(AttachmentHandler.expandVariableAttachments(fragment)).isEmpty();
  }

  @Test
  public void normalizeConvertsCrossingBondIntoVariableAttachmentAndMergesFragments() {
    // Scaffold: a two-atom "ring" edge c1-c2, each further bonded so both have degree >= 2.
    CDAtom c1 = element(0f, 0f);
    CDAtom c2 = element(0f, 10f);
    CDAtom x = element(-10f, 0f);
    CDAtom y = element(-10f, 10f);
    CDBond crossed = bond(c1, c2);
    CDFragment scaffold = new CDFragment();
    scaffold.setAtoms(List.of(c1, c2, x, y));
    scaffold.setBonds(new ArrayList<>(List.of(crossed, bond(c1, x), bond(c2, y))));

    // Substituent fragment: attachment point sits near the crossed-bond midpoint (0,5); the
    // substituent is drawn farther away. The position-variation bond crosses the scaffold edge.
    CDAtom attach = element(-3f, 5f);
    CDAtom substituent = element(5f, 5f);
    CDBond varBond = bond(attach, substituent);
    varBond.setCrossingBonds(new java.util.HashSet<>(List.of(crossed)));
    crossed.setCrossingBonds(new java.util.HashSet<>(List.of(varBond)));
    CDFragment sub = new CDFragment();
    sub.setAtoms(List.of(attach, substituent));
    sub.setBonds(new ArrayList<>(List.of(varBond)));

    List<CDFragment> result =
        AttachmentHandler.normalizeVariableAttachmentBonds(new ArrayList<>(List.of(scaffold, sub)));

    // The substituent fragment is folded into the scaffold and dropped from the list.
    assertThat(result).containsExactly(scaffold);
    assertThat(scaffold.getAtoms()).contains(attach, substituent);
    assertThat(attach.getNodeType()).isEqualTo(CDNodeType.VariableAttachment);
    assertThat(attach.getAttachedAtoms()).containsExactlyInAnyOrder(c1, c2);

    // The normalized scaffold now enumerates through the existing variable-attachment machinery.
    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(scaffold);
    assertThat(variants).hasSize(2);
    for (CDFragment variant : variants) {
      assertThat(variant.getAtoms()).doesNotContain(attach);
      assertThat(other(substituentBond(variant, substituent), substituent)).isIn(c1, c2);
    }
  }

  @Test
  public void normalizeIgnoresCrossedBondsTheStubDoesNotReach() {
    // Scaffold edge c1-c2, both endpoints further bonded so neither is a free end.
    CDAtom c1 = element(0f, 0f);
    CDAtom c2 = element(0f, 10f);
    CDAtom x = element(-10f, 0f);
    CDAtom y = element(-10f, 10f);
    CDBond crossed = bond(c1, c2);
    CDFragment scaffold = new CDFragment();
    scaffold.setAtoms(List.of(c1, c2, x, y));
    scaffold.setBonds(new ArrayList<>(List.of(crossed, bond(c1, x), bond(c2, y))));

    // A bond that names the scaffold edge as crossed but is drawn two bond lengths clear of it -
    // ChemDraw records such pairs, and their endpoints are no attachment candidates.
    CDAtom near = element(20f, 5f);
    CDAtom far = element(30f, 5f);
    CDBond stub = bond(near, far);
    stub.setCrossingBonds(new HashSet<>(List.of(crossed)));
    crossed.setCrossingBonds(new HashSet<>(List.of(stub)));
    CDFragment sub = new CDFragment();
    sub.setAtoms(List.of(near, far));
    sub.setBonds(new ArrayList<>(List.of(stub)));

    List<CDFragment> result =
        AttachmentHandler.normalizeVariableAttachmentBonds(new ArrayList<>(List.of(scaffold, sub)));

    assertThat(result).containsExactly(scaffold, sub);
    assertThat(near.getNodeType()).isEqualTo(CDNodeType.Element);
    assertThat(near.getAttachedAtoms()).isNull();
    assertThat(scaffold.getAtoms()).doesNotContain(near, far);
  }

  @Test
  public void normalizeMovesASubstituentWithSeveralCrossingBondsOnlyOnce() {
    // Scaffold: a four-ring whose left (c1-c2) and right (c3-c4) edges are both crossed.
    CDAtom c1 = element(0f, 0f);
    CDAtom c2 = element(0f, 10f);
    CDAtom c3 = element(20f, 0f);
    CDAtom c4 = element(20f, 10f);
    CDBond left = bond(c1, c2);
    CDBond right = bond(c3, c4);
    CDFragment scaffold = new CDFragment();
    scaffold.setAtoms(List.of(c1, c2, c3, c4));
    scaffold.setBonds(new ArrayList<>(List.of(left, right, bond(c2, c4), bond(c1, c3))));

    // One substituent chain, drawn across both edges: each of its two bonds crosses one edge.
    CDAtom endLeft = element(-2f, 5f);
    CDAtom middle = element(10f, 5f);
    CDAtom endRight = element(22f, 5f);
    CDBond toLeft = bond(endLeft, middle);
    CDBond toRight = bond(middle, endRight);
    toLeft.setCrossingBonds(new HashSet<>(List.of(left)));
    toRight.setCrossingBonds(new HashSet<>(List.of(right)));
    left.setCrossingBonds(new HashSet<>(List.of(toLeft)));
    right.setCrossingBonds(new HashSet<>(List.of(toRight)));
    CDFragment sub = new CDFragment();
    sub.setAtoms(List.of(endLeft, middle, endRight));
    sub.setBonds(new ArrayList<>(List.of(toLeft, toRight)));

    List<CDFragment> result =
        AttachmentHandler.normalizeVariableAttachmentBonds(new ArrayList<>(List.of(scaffold, sub)));

    // Both free ends become junctions, but the substituent's atoms and bonds move across once.
    assertThat(result).containsExactly(scaffold);
    assertThat(scaffold.getAtoms()).containsExactly(c1, c2, c3, c4, endLeft, middle, endRight);
    assertThat(scaffold.getBonds()).hasSize(6);
    assertThat(endLeft.getNodeType()).isEqualTo(CDNodeType.VariableAttachment);
    assertThat(endLeft.getAttachedAtoms()).containsExactlyInAnyOrder(c1, c2);
    assertThat(endRight.getNodeType()).isEqualTo(CDNodeType.VariableAttachment);
    assertThat(endRight.getAttachedAtoms()).containsExactlyInAnyOrder(c3, c4);

    // Two junctions with two candidates each -> the four position isomers, no duplicated atoms.
    assertThat(AttachmentHandler.expandVariableAttachments(scaffold)).hasSize(4);
  }

  @Test
  public void normalizeLeavesFragmentsWithoutCrossingBondsUntouched() {
    CDAtom a = element(0f, 0f);
    CDAtom b = element(0f, 10f);
    CDFragment fragment = new CDFragment();
    fragment.setAtoms(List.of(a, b));
    fragment.setBonds(new ArrayList<>(List.of(bond(a, b))));

    List<CDFragment> fragments = new ArrayList<>(List.of(fragment));
    List<CDFragment> result = AttachmentHandler.normalizeVariableAttachmentBonds(fragments);

    assertThat(result).containsExactly(fragment);
    assertThat(a.getNodeType()).isEqualTo(CDNodeType.Element);
  }

  /**
   * The same crossing-bond stub drawn the other way round: the residue label, not a plain atom, is
   * the endpoint sitting on the crossed bond. The junction that expansion deletes must then be the
   * plain end, so the R node survives and can still be substituted from the legend.
   */
  @Test
  public void normalizeKeepsResidueLabelWhenItSitsOnTheCrossedBond() {
    CDAtom c1 = element(0f, 0f);
    CDAtom c2 = element(0f, 10f);
    CDAtom x = element(-10f, 0f);
    CDAtom y = element(-10f, 10f);
    CDBond crossed = bond(c1, c2);
    CDFragment scaffold = new CDFragment();
    scaffold.setAtoms(List.of(c1, c2, x, y));
    scaffold.setBonds(new ArrayList<>(List.of(crossed, bond(c1, x), bond(c2, y))));

    CDAtom residue = residue(-3f, 5f);
    CDAtom plain = element(5f, 5f);
    CDBond varBond = bond(residue, plain);
    varBond.setCrossingBonds(new java.util.HashSet<>(List.of(crossed)));
    crossed.setCrossingBonds(new java.util.HashSet<>(List.of(varBond)));
    CDFragment sub = new CDFragment();
    sub.setAtoms(List.of(residue, plain));
    sub.setBonds(new ArrayList<>(List.of(varBond)));

    AttachmentHandler.normalizeVariableAttachmentBonds(new ArrayList<>(List.of(scaffold, sub)));

    assertThat(plain.getNodeType()).isEqualTo(CDNodeType.VariableAttachment);
    assertThat(residue.getNodeType()).isEqualTo(CDNodeType.GenericNickname);
    assertThat(plain.getAttachedAtoms()).containsExactlyInAnyOrder(c1, c2);

    List<CDFragment> variants = AttachmentHandler.expandVariableAttachments(scaffold);
    assertThat(variants).hasSize(2);
    for (CDFragment variant : variants) {
      assertThat(variant.getAtoms()).contains(residue).doesNotContain(plain);
      assertThat(other(substituentBond(variant, residue), residue)).isIn(c1, c2);
    }
  }

  private static CDAtom residue(float x, float y) {
    CDStyledString styled = new CDStyledString();
    styled.addChunk(new CDStyledString.CDXChunk(null, 10f, null, null, "R"));
    CDText text = new CDText();
    text.setText(styled);
    CDAtom atom = new CDAtom();
    atom.setNodeType(CDNodeType.GenericNickname);
    atom.setPosition2D(new CDPoint2D(x, y));
    atom.setText(text);
    return atom;
  }

  private static CDBond substituentBond(CDFragment fragment, CDAtom substituent) {
    return fragment.getBonds().stream()
        .filter(b -> substituent.equals(b.getBegin()) || substituent.equals(b.getEnd()))
        .findFirst()
        .orElseThrow();
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
