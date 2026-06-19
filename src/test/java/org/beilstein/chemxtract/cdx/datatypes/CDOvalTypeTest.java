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

public class CDOvalTypeTest {

  static Stream<Arguments> flags() {
    return Stream.of(
        Arguments.of(
            "circle",
            (Consumer<CDOvalType>) o -> o.setCircle(true),
            (Predicate<CDOvalType>) CDOvalType::isCircle),
        Arguments.of(
            "shaded",
            (Consumer<CDOvalType>) o -> o.setShaded(true),
            (Predicate<CDOvalType>) CDOvalType::isShaded),
        Arguments.of(
            "filled",
            (Consumer<CDOvalType>) o -> o.setFilled(true),
            (Predicate<CDOvalType>) CDOvalType::isFilled),
        Arguments.of(
            "dashed",
            (Consumer<CDOvalType>) o -> o.setDashed(true),
            (Predicate<CDOvalType>) CDOvalType::isDashed),
        Arguments.of(
            "bold",
            (Consumer<CDOvalType>) o -> o.setBold(true),
            (Predicate<CDOvalType>) CDOvalType::isBold),
        Arguments.of(
            "shadowed",
            (Consumer<CDOvalType>) o -> o.setShadowed(true),
            (Predicate<CDOvalType>) CDOvalType::isShadowed));
  }

  @Test
  public void newInstanceIsPlainWithAllFlagsFalse() {
    CDOvalType oval = new CDOvalType();
    assertThat(oval.isPlain()).isTrue();
    assertThat(oval.isCircle()).isFalse();
    assertThat(oval.isShaded()).isFalse();
    assertThat(oval.isFilled()).isFalse();
    assertThat(oval.isDashed()).isFalse();
    assertThat(oval.isBold()).isFalse();
    assertThat(oval.isShadowed()).isFalse();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("flags")
  public void settingAnyFlagBreaksPlain(
      String name, Consumer<CDOvalType> setter, Predicate<CDOvalType> getter) {
    CDOvalType oval = new CDOvalType();
    setter.accept(oval);
    assertThat(getter.test(oval)).as("getter for %s", name).isTrue();
    assertThat(oval.isPlain()).as("isPlain after setting %s", name).isFalse();
  }

  @Test
  public void setPlainTrueResetsEveryFlag() {
    CDOvalType oval = new CDOvalType();
    oval.setCircle(true);
    oval.setShaded(true);
    oval.setFilled(true);
    oval.setDashed(true);
    oval.setBold(true);
    oval.setShadowed(true);

    oval.setPlain(true);

    assertThat(oval.isPlain()).isTrue();
    assertThat(oval.isCircle()).isFalse();
    assertThat(oval.isShaded()).isFalse();
    assertThat(oval.isFilled()).isFalse();
    assertThat(oval.isDashed()).isFalse();
    assertThat(oval.isBold()).isFalse();
    assertThat(oval.isShadowed()).isFalse();
  }

  @Test
  public void setPlainFalseIsNoOp() {
    CDOvalType oval = new CDOvalType();
    oval.setCircle(true);
    oval.setBold(true);

    oval.setPlain(false);

    assertThat(oval.isCircle()).isTrue();
    assertThat(oval.isBold()).isTrue();
  }

  @Test
  public void equalsIsReflexiveAndRejectsNullAndDifferentClass() {
    CDOvalType oval = new CDOvalType();
    assertThat(oval).isEqualTo(oval);
    assertThat(oval).isNotEqualTo(null);
    assertThat(oval).isNotEqualTo("not an oval");
  }

  @Test
  public void twoDefaultInstancesAreEqualAndShareHashCode() {
    CDOvalType a = new CDOvalType();
    CDOvalType b = new CDOvalType();
    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }

  @ParameterizedTest(name = "differs on {0}")
  @MethodSource("flags")
  public void equalsAndHashCodeAreSensitiveToEachFlag(
      String name, Consumer<CDOvalType> setter) {
    CDOvalType a = new CDOvalType();
    CDOvalType b = new CDOvalType();
    setter.accept(b);
    assertThat(a).as("equals differs by %s", name).isNotEqualTo(b);
    assertThat(a.hashCode()).as("hashCode differs by %s", name).isNotEqualTo(b.hashCode());
  }

  @Test
  public void toStringContainsEveryLabeledFlag() {
    CDOvalType oval = new CDOvalType();
    oval.setCircle(true);
    oval.setBold(true);

    String s = oval.toString();

    assertThat(s)
        .startsWith("OvalType[")
        .endsWith("]")
        .contains(
            "bold=true",
            "circle=true",
            "dashed=false",
            "filled=false",
            "shaped=false",
            "shadowed=false");
  }
}
