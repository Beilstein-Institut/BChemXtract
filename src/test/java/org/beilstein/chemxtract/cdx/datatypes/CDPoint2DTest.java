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