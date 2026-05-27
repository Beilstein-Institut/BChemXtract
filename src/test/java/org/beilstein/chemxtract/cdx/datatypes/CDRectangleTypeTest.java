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
package org.beilstein.chemxtract.cdx.datatypes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CDRectangleTypeTest {

  static Stream<Arguments> flags() {
    return Stream.of(
        Arguments.of(
            "roundEdge",
            (Consumer<CDRectangleType>) r -> r.setRoundEdge(true),
            (Predicate<CDRectangleType>) CDRectangleType::isRoundEdge),
        Arguments.of(
            "shadow",
            (Consumer<CDRectangleType>) r -> r.setShadow(true),
            (Predicate<CDRectangleType>) CDRectangleType::isShadow),
        Arguments.of(
            "shaded",
            (Consumer<CDRectangleType>) r -> r.setShaded(true),
            (Predicate<CDRectangleType>) CDRectangleType::isShaded),
        Arguments.of(
            "filled",
            (Consumer<CDRectangleType>) r -> r.setFilled(true),
            (Predicate<CDRectangleType>) CDRectangleType::isFilled),
        Arguments.of(
            "dashed",
            (Consumer<CDRectangleType>) r -> r.setDashed(true),
            (Predicate<CDRectangleType>) CDRectangleType::isDashed),
        Arguments.of(
            "bold",
            (Consumer<CDRectangleType>) r -> r.setBold(true),
            (Predicate<CDRectangleType>) CDRectangleType::isBold));
  }

  @Test
  public void newInstanceIsPlainWithAllFlagsFalse() {
    CDRectangleType rect = new CDRectangleType();
    assertThat(rect.isPlain()).isTrue();
    assertThat(rect.isRoundEdge()).isFalse();
    assertThat(rect.isShadow()).isFalse();
    assertThat(rect.isShaded()).isFalse();
    assertThat(rect.isFilled()).isFalse();
    assertThat(rect.isDashed()).isFalse();
    assertThat(rect.isBold()).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("flags")
  public void settingAnyFlagBreaksPlain(
      String name, Consumer<CDRectangleType> setter, Predicate<CDRectangleType> getter) {
    CDRectangleType rect = new CDRectangleType();
    setter.accept(rect);
    assertThat(getter.test(rect)).as("getter for %s", name).isTrue();
    assertThat(rect.isPlain()).as("isPlain after setting %s", name).isFalse();
  }

  @Test
  public void setPlainTrueResetsEveryFlag() {
    CDRectangleType rect = new CDRectangleType();
    rect.setRoundEdge(true);
    rect.setShadow(true);
    rect.setShaded(true);
    rect.setFilled(true);
    rect.setDashed(true);
    rect.setBold(true);

    rect.setPlain(true);

    assertThat(rect.isPlain()).isTrue();
    assertThat(rect.isRoundEdge()).isFalse();
    assertThat(rect.isShadow()).isFalse();
    assertThat(rect.isShaded()).isFalse();
    assertThat(rect.isFilled()).isFalse();
    assertThat(rect.isDashed()).isFalse();
    assertThat(rect.isBold()).isFalse();
  }

  @Test
  public void setPlainFalseIsNoOp() {
    CDRectangleType rect = new CDRectangleType();
    rect.setRoundEdge(true);
    rect.setBold(true);

    rect.setPlain(false);

    assertThat(rect.isRoundEdge()).isTrue();
    assertThat(rect.isBold()).isTrue();
  }

  @Test
  public void equalsIsReflexiveAndRejectsNullAndDifferentClass() {
    CDRectangleType rect = new CDRectangleType();
    assertThat(rect).isEqualTo(rect);
    assertThat(rect).isNotEqualTo(null);
    assertThat(rect).isNotEqualTo("not a rectangle");
  }

  @Test
  public void twoDefaultInstancesAreEqualAndShareHashCode() {
    CDRectangleType a = new CDRectangleType();
    CDRectangleType b = new CDRectangleType();
    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }

  @ParameterizedTest(name = "differs on {0}")
  @MethodSource("flags")
  public void equalsAndHashCodeAreSensitiveToEachFlag(
      String name, Consumer<CDRectangleType> setter, Predicate<CDRectangleType> getter) {
    CDRectangleType a = new CDRectangleType();
    CDRectangleType b = new CDRectangleType();
    setter.accept(b);
    assertThat(a).as("equals differs by %s", name).isNotEqualTo(b);
    assertThat(a.hashCode()).as("hashCode differs by %s", name).isNotEqualTo(b.hashCode());
  }

  @Test
  public void toStringContainsEveryLabeledFlag() {
    CDRectangleType rect = new CDRectangleType();
    rect.setRoundEdge(true);
    rect.setBold(true);

    String s = rect.toString();

    assertThat(s)
        .startsWith("RectangleType[")
        .endsWith("]")
        .contains(
            "bold=true",
            "dashed=false",
            "filled=false",
            "round-edge=true",
            "shaped=false",
            "shadow=false");
  }
}
