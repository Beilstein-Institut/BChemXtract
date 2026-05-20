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
package org.beilstein.chemxtract.cdx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Represents a lane of spots arranged vertically on a TLC plate. */
public class CDTLCLane {

  /** The spots contained within this TLC lane. */
  private List<CDTLCSpot> spots = new ArrayList<>();

  private List<CDObjectTag> objectTags = new ArrayList<>();
  private boolean visible = true;

  public List<CDObjectTag> getObjectTags() {
    return Collections.unmodifiableList(objectTags);
  }

  public void setObjectTags(List<CDObjectTag> objectTags) {
    this.objectTags = objectTags == null ? new ArrayList<>() : new ArrayList<>(objectTags);
  }

  public void addObjectTag(CDObjectTag objectTag) {
    this.objectTags.add(objectTag);
  }

  public List<CDTLCSpot> getSpots() {
    return Collections.unmodifiableList(spots);
  }

  public void setSpots(List<CDTLCSpot> spots) {
    this.spots = spots == null ? new ArrayList<>() : new ArrayList<>(spots);
  }

  public void addSpot(CDTLCSpot spot) {
    this.spots.add(spot);
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
