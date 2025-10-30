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
package org.beilstein.chemxtract.cheminf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point2d;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.ringsearch.RingSearch;

/**
 * Detector for identifying sugar rings in Chair or Haworth projections.
 *
 * <p>This class determines if a given molecular structure contains cyclic carbohydrate structures
 * drawn in standard Chair or Haworth projections. This class is based on {@link
 * org.openscience.cdk.stereo.CyclicCarbohydrateRecognition}.
 */
public class SugarProjectionDetector {

  /**
   * The threshold at which to snap bonds to the cardinal direction. The threshold allows bonds
   * slightly off absolute directions to be interpreted. The tested vector is of unit length and so
   * the threshold is simply the angle (in radians).
   */
  public static final double CARDINALITY_THRESHOLD = Math.toRadians(5);

  private final IAtomContainer container;
  private final int[][] graph;

  /**
   * Create a detector for the given molecular structure.
   *
   * @param container input structure
   */
  public SugarProjectionDetector(IAtomContainer container) {
    this.container = container;
    this.graph = GraphUtil.toAdjList(container);
  }

  /**
   * Check if the structure contains any sugar rings in Chair or Haworth projections.
   *
   * @return true if at least one sugar ring in Chair or Haworth projection is found
   */
  public boolean containsSugarProjections() {
    return containsHaworthProjections() || containsChairProjections();
  }

  /**
   * Check if the structure contains any sugar rings in Haworth projections.
   *
   * @return true if at least one sugar ring in Haworth projection is found
   */
  public boolean containsHaworthProjections() {
    return !findHaworthProjections().isEmpty();
  }

  /**
   * Check if the structure contains any sugar rings in Chair projections.
   *
   * @return true if at least one sugar ring in Chair projection is found
   */
  public boolean containsChairProjections() {
    return !findChairProjections().isEmpty();
  }

  /**
   * Find all sugar rings in Haworth projections.
   *
   * @return set of atom indices representing Haworth projection rings
   */
  public Set<int[]> findHaworthProjections() {
    return findProjections(Projection.Haworth);
  }

  /**
   * Find all sugar rings in Chair projections.
   *
   * @return set of atom indices representing Chair projection rings
   */
  public Set<int[]> findChairProjections() {
    return findProjections(Projection.Chair);
  }

  /**
   * Find all sugar rings matching the specified projection type.
   *
   * @param projectionType the type of projection to search for
   * @return set of atom indices representing matching projection rings
   */
  private Set<int[]> findProjections(Projection projectionType) {
    Set<int[]> projections = new HashSet<>();

    RingSearch ringSearch = new RingSearch(container, graph);
    for (int[] isolated : ringSearch.isolated()) {
      // Sugar rings are typically 5-7 atoms (furanose/pyranose)
      if (isolated.length < 5 || isolated.length > 7) continue;

      int[] cycle = Arrays.copyOf(GraphUtil.cycle(graph, isolated), isolated.length);
      Point2d[] points = coordinatesOfCycle(cycle, container);

      // Check if the ring is aligned correctly for Haworth
      if (projectionType == Projection.Haworth && !checkHaworthAlignment(points)) continue;

      Turn[] turns = turns(points);
      if (turns == null) continue;

      WoundProjection projection = WoundProjection.ofTurns(turns);
      if (projection.projection == projectionType) {
        projections.add(cycle);
      }
    }

    return projections;
  }

  /**
   * Determine the turns in the polygon formed of the provided coordinates.
   *
   * @param points polygon points
   * @return array of turns (left, right) or null if a parallel line was found Copy of {@link
   *     org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.turns}
   */
  private static Turn[] turns(Point2d[] points) {
    final Turn[] turns = new Turn[points.length];

    for (int i = 1; i <= points.length; i++) {
      Point2d prevXy = points[i - 1];
      Point2d currXy = points[i % points.length];
      Point2d nextXy = points[(i + 1) % points.length];

      int parity =
          (int) Math.signum(det(prevXy.x, prevXy.y, currXy.x, currXy.y, nextXy.x, nextXy.y));
      if (parity == 0) return null;
      turns[i % points.length] = parity < 0 ? Turn.Right : Turn.Left;
    }

    return turns;
  }

  /**
   * Ensures at least one cyclic bond is horizontal for Haworth alignment.
   *
   * @param points the points of atoms in the ring
   * @return whether the Haworth alignment is correct Copy of {@link
   *     org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.checkHaworthAlignment}
   */
  private boolean checkHaworthAlignment(Point2d[] points) {
    for (int i = 0; i < points.length; i++) {
      Point2d curr = points[i];
      Point2d next = points[(i + 1) % points.length];

      double deltaY = curr.y - next.y;

      if (Math.abs(deltaY) < CARDINALITY_THRESHOLD) return true;
    }

    return false;
  }

  /**
   * Obtain the coordinates of atoms in a cycle.
   *
   * @param cycle vertices that form a cycle
   * @param container structure representation
   * @return coordinates of the cycle Copy of {@link
   *     org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.coordinatesOfCycle}
   */
  private static Point2d[] coordinatesOfCycle(int[] cycle, IAtomContainer container) {
    Point2d[] points = new Point2d[cycle.length];
    for (int i = 0; i < cycle.length; i++) {
      points[i] = container.getAtom(cycle[i]).getPoint2d();
    }
    return points;
  }

  /**
   * 3x3 determinant helper for a constant third column Copy of {@link
   * org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.det}
   */
  private static double det(double xa, double ya, double xb, double yb, double xc, double yc) {
    return (xa - xc) * (yb - yc) - (ya - yc) * (xb - xc);
  }

  /**
   * Turns, recorded when walking around the cycle. Copy of {@link
   * org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.Turn}
   */
  private enum Turn {
    Left,
    Right
  }

  /** Projection types supported for sugar rings. */
  private enum Projection {
    Haworth,
    Chair
  }

  /**
   * Pairing of Projection type. Determined from an array of turns. Copy of {@link
   * org.openscience.cdk.stereo.CyclicCarbohydrateRecognition.WoundProjection}
   */
  private enum WoundProjection {
    HaworthClockwise(Projection.Haworth),
    HaworthAnticlockwise(Projection.Haworth),
    ChairClockwise(Projection.Chair),
    ChairAnticlockwise(Projection.Chair),
    Other(null);

    private final Projection projection;
    private static final Map<Key, WoundProjection> map = new HashMap<>();

    static {
      // Haworth |V| = 5
      map.put(new Key(Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left), HaworthAnticlockwise);
      map.put(
          new Key(Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right), HaworthClockwise);

      // Haworth |V| = 6
      map.put(
          new Key(Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left),
          HaworthAnticlockwise);
      map.put(
          new Key(Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right),
          HaworthClockwise);

      // Haworth |V| = 7
      map.put(
          new Key(Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left, Turn.Left),
          HaworthAnticlockwise);
      map.put(
          new Key(
              Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right, Turn.Right),
          HaworthClockwise);

      // Chair
      map.put(
          new Key(Turn.Left, Turn.Right, Turn.Right, Turn.Left, Turn.Right, Turn.Right),
          ChairClockwise);
      map.put(
          new Key(Turn.Right, Turn.Left, Turn.Right, Turn.Right, Turn.Left, Turn.Right),
          ChairClockwise);
      map.put(
          new Key(Turn.Right, Turn.Right, Turn.Left, Turn.Right, Turn.Right, Turn.Left),
          ChairClockwise);
      map.put(
          new Key(Turn.Right, Turn.Left, Turn.Left, Turn.Right, Turn.Left, Turn.Left),
          ChairAnticlockwise);
      map.put(
          new Key(Turn.Left, Turn.Right, Turn.Left, Turn.Left, Turn.Right, Turn.Left),
          ChairAnticlockwise);
      map.put(
          new Key(Turn.Left, Turn.Left, Turn.Right, Turn.Left, Turn.Left, Turn.Right),
          ChairAnticlockwise);
    }

    WoundProjection(Projection projection) {
      this.projection = projection;
    }

    static WoundProjection ofTurns(Turn[] turns) {
      if (turns == null) return Other;
      WoundProjection type = map.get(new Key(turns));
      return type != null ? type : Other;
    }

    private static final class Key {
      private final Turn[] turns;

      private Key(Turn... turns) {
        this.turns = turns;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        return Arrays.equals(turns, key.turns);
      }

      @Override
      public int hashCode() {
        return turns != null ? Arrays.hashCode(turns) : 0;
      }
    }
  }
}
