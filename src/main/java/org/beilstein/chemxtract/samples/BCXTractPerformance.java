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
package org.beilstein.chemxtract.samples;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;

/**
 * Performance overview sample. Recursively walks a directory, extracts substances from every {@code
 * .cdx} and {@code .cdxml} file and reports per-file timing plus an aggregate summary (throughput,
 * success/empty/error counts, slowest files, total substances). Generates no image output.
 *
 * <p>Usage: {@code java -cp <fat-jar> org.beilstein.chemxtract.samples.BCXTractPerformance [dir]}
 * where {@code dir} defaults to {@code src/test/resources}.
 */
public class BCXTractPerformance {

  /** Outcome of extracting a single CDX file. */
  private static final class Result {
    final Path file;
    final long millis;
    final int substances;
    final int fragments;
    final int realInchis;
    final int wildcards;
    final String error;

    Result(
        Path file,
        long millis,
        int substances,
        int fragments,
        int realInchis,
        int wildcards,
        String error) {
      this.file = file;
      this.millis = millis;
      this.substances = substances;
      this.fragments = fragments;
      this.realInchis = realInchis;
      this.wildcards = wildcards;
      this.error = error;
    }

    boolean failed() {
      return error != null;
    }
  }

  public static void main(String[] args) throws Exception {
    Path root = Paths.get(args.length > 0 ? args[0] : "src/test/resources");
    // Single-file mode: extract one file and print a machine-parseable RESULT
    // line (prefixed so it survives interleaved logging). Lets callers wrap
    // each file in an external timeout to survive pathological inputs.
    if (Files.isRegularFile(root)) {
      Path parent = root.getParent() != null ? root.getParent() : Paths.get(".");
      Result r = new BCXTractPerformance().extractOne(parent, root);
      String status = r.failed() ? "error" : r.substances == 0 ? "empty" : "ok";
      System.out.printf(
          "RESULT\t%d\t%d\t%d\t%d\t%s\t%s%n",
          r.millis,
          r.substances,
          r.realInchis,
          r.wildcards,
          status,
          r.error == null ? "" : r.error);
      return;
    }
    if (!Files.isDirectory(root)) {
      System.err.println("Not a file or directory: " + root.toAbsolutePath());
      System.exit(1);
    }
    new BCXTractPerformance().run(root);
  }

  public void run(Path root) throws Exception {
    List<Path> cdxFiles = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(root)) {
      walk.filter(Files::isRegularFile)
          .filter(BCXTractPerformance::isChemDrawFile)
          .sorted()
          .forEach(cdxFiles::add);
    }

    System.out.printf(
        "Scanning %d .cdx/.cdxml files under %s%n%n", cdxFiles.size(), root.toAbsolutePath());

    List<Result> results = new ArrayList<>(cdxFiles.size());
    for (Path file : cdxFiles) {
      results.add(extractOne(root, file));
    }

    printReport(results);
  }

  /**
   * True for the two ChemDraw formats this sample reads: binary {@code .cdx} and XML {@code
   * .cdxml}.
   */
  private static boolean isChemDrawFile(Path file) {
    String name = fileName(file);
    return name.endsWith(".cdx") || name.endsWith(".cdxml");
  }

  private static boolean isCdxml(Path file) {
    return fileName(file).endsWith(".cdxml");
  }

  /** Lower-cased file name, or {@code ""} for a root path, which has none. */
  private static String fileName(Path file) {
    Path name = file.getFileName();
    return name == null ? "" : name.toString().toLowerCase(Locale.ROOT);
  }

  private Result extractOne(Path root, Path file) {
    long start = System.nanoTime();
    try (InputStream in = Files.newInputStream(file)) {
      CDDocument document =
          isCdxml(file) ? CDXMLReader.readDocument(in) : CDXReader.readDocument(in);
      BCXSubstanceInfo info = new BCXSubstanceInfo();
      SubstanceXtractor xtractor = new SubstanceXtractor();
      List<BCXSubstance> substances = xtractor.xtractUnique(document, info, false);
      long millis = (System.nanoTime() - start) / 1_000_000L;
      int realInchis = 0;
      int wildcards = 0;
      for (BCXSubstance s : substances) {
        if (s.getInchi() != null && !s.getInchi().isEmpty()) {
          realInchis++;
        }
        String smiles = s.getSmiles();
        if (smiles != null && smiles.indexOf('*') >= 0) {
          wildcards++;
        }
      }
      return new Result(
          root.relativize(file),
          millis,
          substances.size(),
          info.getNoFragments(),
          realInchis,
          wildcards,
          null);
    } catch (Exception | Error e) {
      long millis = (System.nanoTime() - start) / 1_000_000L;
      String msg =
          e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
      return new Result(root.relativize(file), millis, 0, 0, 0, 0, msg);
    }
  }

  private void printReport(List<Result> results) {
    int total = results.size();
    long errors = results.stream().filter(Result::failed).count();
    long empty = results.stream().filter(r -> !r.failed() && r.substances == 0).count();
    long ok = total - errors - empty;

    long totalMillis = results.stream().mapToLong(r -> r.millis).sum();
    long totalSubstances =
        results.stream().filter(r -> !r.failed()).mapToInt(r -> r.substances).sum();
    long totalFragments =
        results.stream().filter(r -> !r.failed()).mapToInt(r -> r.fragments).sum();
    long totalRealInchis =
        results.stream().filter(r -> !r.failed()).mapToInt(r -> r.realInchis).sum();
    long totalWildcards =
        results.stream().filter(r -> !r.failed()).mapToInt(r -> r.wildcards).sum();
    double avgMillis = total == 0 ? 0 : (double) totalMillis / total;

    System.out.println("==================== EXTRACTION PERFORMANCE ====================");
    System.out.printf("Files processed       : %d%n", total);
    System.out.printf("  with substances     : %d%n", ok);
    System.out.printf("  empty (0 found)     : %d%n", empty);
    System.out.printf("  errors              : %d%n", errors);
    System.out.printf("Total substances      : %d%n", totalSubstances);
    System.out.printf("  with real InChI     : %d%n", totalRealInchis);
    System.out.printf("  SMILES fallback     : %d%n", totalSubstances - totalRealInchis);
    System.out.printf("  unexpanded alias (*): %d%n", totalWildcards);
    System.out.printf("  fully expanded      : %d%n", totalSubstances - totalWildcards);
    System.out.printf("Total fragments       : %d%n", totalFragments);
    System.out.printf(
        "Total time            : %d ms (%.2f s)%n", totalMillis, totalMillis / 1000.0);
    System.out.printf("Average per file      : %.1f ms%n", avgMillis);
    if (totalMillis > 0) {
      System.out.printf("Throughput            : %.1f files/s%n", total * 1000.0 / totalMillis);
    }
    System.out.println();

    System.out.println("---- 10 slowest files ----");
    results.stream()
        .sorted(Comparator.comparingLong((Result r) -> r.millis).reversed())
        .limit(10)
        .forEach(
            r -> System.out.printf("  %6d ms  subs=%-3d  %s%n", r.millis, r.substances, r.file));
    System.out.println();

    if (errors > 0) {
      System.out.println("---- errors ----");
      results.stream()
          .filter(Result::failed)
          .forEach(r -> System.out.printf("  %s%n      -> %s%n", r.file, r.error));
      System.out.println();
    }

    if (empty > 0) {
      System.out.println("---- files with no substances extracted ----");
      results.stream()
          .filter(r -> !r.failed() && r.substances == 0)
          .forEach(r -> System.out.printf("  %s%n", r.file));
      System.out.println();
    }

    System.out.println("================================================================");
  }
}
