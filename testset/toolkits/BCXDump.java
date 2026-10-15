/*
 * Dump every substance BChemXtract extracts from a ChemDraw corpus, one per line, in the
 * layout pair_structures.py reads (file, smiles, inchikey).
 *
 * Usage (against a jar-with-dependencies):
 *   javac -d /tmp/bcxdump -cp JAR testset/toolkits/BCXDump.java
 *   java -cp /tmp/bcxdump:JAR BCXDump CORPUS_DIR OUT.tsv [resolveRGroups]
 */
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;

public class BCXDump {
  public static void main(String[] args) throws Exception {
    Path root = Path.of(args[0]);
    boolean resolveRGroups = args.length > 2 && Boolean.parseBoolean(args[2]);
    List<Path> files;
    try (Stream<Path> s = Files.walk(root)) {
      files = s.filter(p -> p.toString().endsWith(".cdx") || p.toString().endsWith(".cdxml")).sorted().toList();
    }
    try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(args[1])))) {
      out.println("file\tsmiles\tinchikey");
      for (Path f : files) {
        try (InputStream in = Files.newInputStream(f)) {
          CDDocument doc =
              f.toString().endsWith(".cdxml") ? CDXMLReader.readDocument(in) : CDXReader.readDocument(in);
          for (BCXSubstance x : new SubstanceXtractor().xtractUnique(doc, new BCXSubstanceInfo(), resolveRGroups)) {
            String key = x.getInchiKey() == null ? "" : x.getInchiKey();
            out.println(root.relativize(f) + "\t" + x.getSmiles().split(" ")[0] + "\t" + key);
          }
        } catch (Exception e) {
          // unreadable file: no structures, as in the performance benchmark
        }
      }
    }
  }
}
