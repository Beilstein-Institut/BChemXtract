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

import java.util.regex.Pattern;

public class Definitions {

  private Definitions() {
    //hide implicit public constructor
  }

  public static final String RGROUP_LABEL_STRING = "^(?:R|X|Y|Ar|E|L)\\d*\\b";
  public static final Pattern RGROUP_LABEL_PATTERN = Pattern.compile(RGROUP_LABEL_STRING);
  public static final String RGROUP_STRING = "\\b(R|X|Y|Ar|E|L)\\b\\s*=\\s*(.+)";
  public static final Pattern RGROUP_PATTERN =Pattern.compile(RGROUP_STRING);
  public static final String ABBREVIATION_PATH = "/org/beilstein/chemxtract/lookups/abbreviations.smi";
  public static final int ABBREVIATION_SIZE = 400;
  public static final String AGENT_ABBREVIATION_PATH = "/org/beilstein/chemxtract/lookups/agents_abbreviations.smi";
  public static final int AGENTS_SIZE = 400;
  // Regular expression for splitting:
  // \s includes all whitespace (spaces, tabs, line feeds, etc.)
  // \n ensures we explicitly catch line feeds (optional since \s includes it)
  // \[\](){}:;, matches individual bracket types, colon, comma, semicolon
  public static final String AGENTS_SPLIT_REGEX = "[\\s\\n\\[\\](){}:;,%/]+";
}
