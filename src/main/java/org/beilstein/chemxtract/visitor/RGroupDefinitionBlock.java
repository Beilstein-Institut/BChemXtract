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
package org.beilstein.chemxtract.visitor;

import java.util.List;
import java.util.Map;
import org.beilstein.chemxtract.cdx.CDRectangle;

/**
 * A single block of R-group definitions extracted from one text node, together with the position of
 * that text on the page. The position lets callers associate a block with the scaffold it belongs
 * to when several scaffolds on the same page reuse the same R-group labels.
 *
 * @param bounds the bounding box of the source text ({@code null} if the text had no position)
 * @param definitions independent per-label substituent lists (each label varies on its own)
 * @param correlatedGroups positional tables whose labels vary together as fixed row-tuples
 */
public record RGroupDefinitionBlock(
    CDRectangle bounds,
    Map<String, List<String>> definitions,
    List<CorrelatedGroup> correlatedGroups) {}
