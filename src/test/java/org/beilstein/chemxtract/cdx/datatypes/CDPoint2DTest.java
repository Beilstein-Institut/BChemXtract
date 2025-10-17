package org.beilstein.chemxtract.cdx.datatypes;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CDPoint2DTest {
  @Test
  public void testEqualsTolerance() {
    CDPoint2D point = new CDPoint2D(20.33f, 107.47f);
    CDPoint2D other = new CDPoint2D(108.29f, 203.44f);
    assertThat(point.equalsTolerance(other)).isFalse();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(108.29f, 107.47f);
    assertThat(point.equalsTolerance(other)).isFalse();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.33f, 12.47f);
    assertThat(point.equalsTolerance(other)).isFalse();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.33f, 107.47f);
    assertThat(point.equalsTolerance(other)).isTrue();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.33f, 107.471f);
    assertThat(point.equalsTolerance(other)).isTrue();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.33f, 107.479f);
    assertThat(point.equalsTolerance(other)).isTrue();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.33f, 107.48f);
    assertThat(point.equalsTolerance(other)).isFalse();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.329f, 107.47f);
    assertThat(point.equalsTolerance(other)).isTrue();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.321f, 107.47f);
    assertThat(point.equalsTolerance(other)).isTrue();

    point = new CDPoint2D(20.33f, 107.47f);
    other = new CDPoint2D(20.32f, 107.47f);
    assertThat(point.equalsTolerance(other)).isFalse();
  }

}