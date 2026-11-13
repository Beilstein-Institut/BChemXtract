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
package org.beilstein.chemxtract.integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/** Regression tests for R-group definitions the text parser currently mishandles. */
public class MarkushBugTest {

  private List<BCXSubstance> xtract(String fileName) throws IOException {
    return xtract(fileName, false);
  }

  private List<BCXSubstance> xtract(String fileName, boolean unrestricted) throws IOException {
    InputStream in = MarkushBugTest.class.getResourceAsStream("/cheminf/bugs/markush/" + fileName);
    assertNotNull(in, "fixture missing on classpath: " + fileName);

    CDDocument document =
        fileName.endsWith(".cdxml") ? CDXMLReader.readDocument(in) : CDXReader.readDocument(in);
    assertNotNull(document);

    BCXSubstanceInfo info = new BCXSubstanceInfo();
    SubstanceXtractor xtractor =
        new SubstanceXtractor(SilentChemObjectBuilder.getInstance())
            .setUnrestrictedMarkush(unrestricted);
    return xtractor.xtractUnique(document, info, true);
  }

  /**
   * R-groups defined structurally via a ChemDraw {@code NamedAlternativeGroup} (not text) must be
   * resolved: the benzene scaffold with alternatives {methyl, chloro} enumerates to toluene and
   * chlorobenzene, with and without the expansion rules.
   */
  @Test
  public void testNamedAlternativeGroupResolved() throws IOException {
    for (boolean unrestricted : new boolean[] {false, true}) {
      Set<String> formulas =
          xtract("altgroup_R_methyl_chloro.cdxml", unrestricted).stream()
              .map(BCXSubstance::getMolecularFormula)
              .collect(Collectors.toSet());
      assertEquals(
          Set.of("C7H8", "C6H5Cl"),
          formulas,
          "expected toluene and chlorobenzene, unrestricted=" + unrestricted);
    }
  }

  /**
   * Substituents written as {@code <position>-<group>} must be grafted onto the ring atom that the
   * position index names, counted from the ring's attachment (ipso) atom — not onto wherever the
   * R-group happens to be drawn. The fixture draws a position-variation R across the C3–C4 bond of
   * a benzamide's phenyl ring and defines {@code R = H, 2-Cl, 3-Cl, 4-Br}, so the ortho value has
   * to reach an atom the drawn attachment never covers.
   */
  @Test
  public void testPositionalArylSubstituentsResolved() throws IOException {
    List<BCXSubstance> substances = xtract("positional_aryl_R_benzamide.cdxml");

    Set<String> inchiKeys =
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());
    assertEquals(
        Set.of(
            "KXDAEFPNCMNJSK-UHFFFAOYSA-N", // benzamide (R = H)
            "RBGDLYUEXLWQBZ-UHFFFAOYSA-N", // 2-chlorobenzamide
            "MJTGQALMWUUPQM-UHFFFAOYSA-N", // 3-chlorobenzamide
            "ZRWNRAJCPNLYAK-UHFFFAOYSA-N"), // 4-bromobenzamide
        inchiKeys,
        "each n-Group value must land on ring position n");
  }

  /**
   * The ortho/meta/para prefixes are the same statement as 2/3/4: the same fixture with {@code R =
   * H, o-Cl, m-Cl, p-Br} must enumerate to the same four benzamides as its numeric twin.
   */
  @Test
  public void testOrthoMetaParaSubstituentsResolved() throws IOException {
    List<BCXSubstance> substances = xtract("positional_aryl_R_benzamide_omp.cdxml");

    Set<String> inchiKeys =
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());
    assertEquals(
        Set.of(
            "KXDAEFPNCMNJSK-UHFFFAOYSA-N", // benzamide (R = H)
            "RBGDLYUEXLWQBZ-UHFFFAOYSA-N", // 2-chlorobenzamide (o-Cl)
            "MJTGQALMWUUPQM-UHFFFAOYSA-N", // 3-chlorobenzamide (m-Cl)
            "ZRWNRAJCPNLYAK-UHFFFAOYSA-N"), // 4-bromobenzamide (p-Br)
        inchiKeys,
        "o-/m-/p- must name ring positions 2/3/4");
  }

  /**
   * Scheme 77 of m50167957-14 lists its R as 4-BrBn, n-C8H17 and n-C11H25. The first two resolve as
   * 4-bromobenzyl and n-octyl; n-C11H25 is no alkyl formula (undecyl is C11H23) and stays
   * unresolved. The scaffold must not take the values of the other legends on the page (4-CN,
   * 2-OCH3, 3-Cl, ...), which used to put a nitrile or a chlorine on the triazole N.
   */
  @Test
  public void testUnresolvedLegendDoesNotBorrowOtherSchemesValues() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-14.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "JFRLSXWVNRAAJJ-MDZDMXLPSA-N", // 77a, R = 4-bromobenzyl
                "IMYUECJCIFBZLN-OUKQBFOZSA-N")), // 77b, R = n-octyl
        "77a and 77b must be extracted: " + inchiKeys);
    for (String borrowed :
        List.of(
            "AMDYKOHIXBLWCN-AATRIKPKSA-N", // N-CN, from 74's 4-CN
            "IPJHTVAMXZXVLD-AATRIKPKSA-N", // N-H, from 76's H
            "HLQKYOYKEZIKTF-VOTSOKGWSA-N", // C-OCH3, from 76's 2-OCH3
            "PDHDCFQEZDSGNN-AATRIKPKSA-N", // C-OH, from 76's 2-OH
            "OOCBJXBLLKHESZ-AATRIKPKSA-N", // N-Cl, from 79's 3-Cl
            "OYBYVSIIVJJFIL-AATRIKPKSA-N")) { // N-CF3, from 79's 3-CF3
      assertTrue(
          !inchiKeys.contains(borrowed), "scheme 77 took another legend's value: " + borrowed);
    }
  }

  /**
   * In m50167957-13, schemes 69 and 70 share one legend, a-q, drawn as two columns (a-i | j-q)
   * whose gutter is a little wider than the plain column merge allows; their continuous lettering
   * makes them one legend, so both scaffolds take all of it. Scheme 72 gives its R as drawings
   * beside "a: R =", "b: R =", "c: R ="; it must not take the a-q values instead.
   */
  @Test
  public void testLetteredColumnsMergeAndDrawnLegendDoesNotBorrow() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-13.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "BJPDPRDMWFFZPV-UHFFFAOYSA-N", // 69j, R = 3-Cl, from the second column
                "RPLXHRKISWDTOM-UHFFFAOYSA-N")), // 70a, R = 4-CH3, from the first column
        "69 and 70 must each take both legend columns: " + inchiKeys);
    for (String borrowed :
        List.of(
            "JKHJZPOBYPJMDS-UHFFFAOYSA-N", // N-Cl
            "VTAMPPWPAOZORH-UHFFFAOYSA-N", // N-OCH3
            "JEAZYBAXYCQDSR-UHFFFAOYSA-N", // C-CH3
            "MBRIKFDTHBFKMZ-UHFFFAOYSA-N", // C-F
            "OCIXKTFFUHFVNY-UHFFFAOYSA-N")) { // N-CH3
      assertFalse(
          inchiKeys.contains(borrowed), "scheme 72 took the a-q legend's value: " + borrowed);
    }
  }

  /**
   * Scheme 72 of m50167957-13 gives R as drawings: "a: R =", "b: R =" and "c: R =", each followed
   * by a group whose free bond ends at a wavy line. The groups are R's values (4-sulfamoylphenyl,
   * 1-naphthyl, 4-nitrophenyl), not substances; their end atoms are the attachment, not methyls,
   * and the wavy lines are not ethane. The scaffold's ring-fusion bond is drawn single in the
   * source, which the keys keep.
   */
  @Test
  public void testDrawnLegendValuesGraftAndAreNotSubstances() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-13.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "MNACKBPSVHGIFK-UHFFFAOYSA-N", // 72a, R = 4-sulfamoylphenyl
                "NHUTVENUAYNHAF-UHFFFAOYSA-N", // 72b, R = 1-naphthyl
                "OYGKJLXVXXRMML-UHFFFAOYSA-N")), // 72c, R = 4-nitrophenyl
        "72a-c must be extracted: " + inchiKeys);
    for (String drawing :
        List.of(
            "LMYRWZFENFIFIT-UHFFFAOYSA-N", // p-toluenesulfonamide, the 72a drawing
            "QPUYECUOLPXSFR-UHFFFAOYSA-N", // 1-methylnaphthalene, the 72b drawing
            "ZPTVNYMJQHSSEA-UHFFFAOYSA-N", // 4-nitrotoluene, the 72c drawing
            "OTMSDBZUPAUEDD-UHFFFAOYSA-N")) { // ethane, a wavy line
      assertFalse(inchiKeys.contains(drawing), "a drawn R value was extracted: " + drawing);
    }
  }

  /**
   * Entries p and q of the a-q legend in m50167957-13: "p: R = 2-CH3, 5-NO2" names one compound
   * with both groups on the ring, and "q: R = 3-CH2CH3" is a written-out ethyl. Both apply to
   * schemes 69 and 70.
   */
  @Test
  public void testOneCompoundsRingSubstituentsAndWrittenOutChain() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-13.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "GYXRWPZWRLTDGJ-UHFFFAOYSA-N", // 69p, 2-methyl-5-nitrophenyl
                "WLXSFHKTZSAWTO-UHFFFAOYSA-N", // 69q, 3-ethylphenyl
                "WIZFPTRAYBRGSJ-UHFFFAOYSA-N", // 70p, 2-methyl-5-nitrophenyl
                "LOWFXIMCKAUZEY-UHFFFAOYSA-N")), // 70q, 3-ethylphenyl
        "69p/q and 70p/q must be extracted: " + inchiKeys);
  }

  /**
   * m50167957-9 names its compounds by number. The 36a-c and 37a-c legends sit side by side and
   * stay two legends, and "36c: Ar = 4-OH, 3-MeOC6H3" is one aryl group. The caption "40a–m: R1 =
   * H, 41a–e: R1 = OMe" pairs each series with the legend numbered 40 or 41, though the R legend of
   * 38 sits nearer the scaffold. 35a-r (R given as names), 37c, 40a and 41a (lookup gaps) are not
   * covered here.
   */
  @Test
  public void testCompoundNumbersTieLegendsToTheirScheme() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-9.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "LGPZCDCSEHZTPT-RGVLZGJSSA-N", // 36a, 4-hydroxyphenyl
                "WBVPXBMFSONTKP-CIAFOILYSA-N", // 36b, 4-methoxyphenyl
                "LGUZZZBOBXRRBP-SRZZPIQSSA-N", // 36c, 4-hydroxy-3-methoxyphenyl
                "SJEKVJZPAXMSSB-UHFFFAOYSA-N", // 37a, 4-hydroxyphenyl
                "FJSMUQHUSCMUGE-UHFFFAOYSA-N", // 37b, 4-chlorophenyl
                "ROBZSMHGNGUOCR-UHFFFAOYSA-N", // 38a
                "PNTBIXNRJZQREZ-UHFFFAOYSA-N", // 38b
                "QPNYFQNYVKEIQJ-UHFFFAOYSA-N", // 39a
                "VVNREYAIYSKNFT-UHFFFAOYSA-N")), // 39b
        "36a-c, 37a-b and 38/39 must be extracted: " + inchiKeys);
    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "OGWPQDPLDCFVOG-UHFFFAOYSA-N", // 40b, 2-ClC6H4
                "QSCAEXWQYBMEIR-UHFFFAOYSA-N", // 40c, 3-ClC6H4
                "XKDDXZYQYUGJBG-UHFFFAOYSA-N", // 40d, 4-ClC6H4
                "VRGZHJNCDMTPIJ-UHFFFAOYSA-N", // 40e, 4-FC6H4
                "COJRETFVDXXNSG-UHFFFAOYSA-N", // 40f, 3-BrC6H4
                "UNIZKAODHJACIS-UHFFFAOYSA-N", // 40g, 4-BrC6H4
                "VAQQCLRPWYNQAM-UHFFFAOYSA-N", // 40h, 2-MeC6H4
                "NLVQLSQYIHBQIZ-UHFFFAOYSA-N", // 40i, 3-MeC6H4
                "BYYGUCGDZIGFGT-UHFFFAOYSA-N", // 40j, 4-MeC6H4
                "JXFGEKNTHRSPKZ-UHFFFAOYSA-N", // 40k, 2,4-Me2C6H3
                "SQXKZMHYSQUNQM-UHFFFAOYSA-N", // 40l, 2,6-Me2C6H3
                "NNLOQZPRIKSEQD-UHFFFAOYSA-N", // 40m, 4-OMeC6H4
                "NNMYJYLPDAIOPI-UHFFFAOYSA-N", // 41b, 2-ClC6H4
                "RODYGUNLZSJSLK-UHFFFAOYSA-N", // 41c, 4-ClC6H4
                "GNCMWYFTVOLURY-UHFFFAOYSA-N", // 41d, 4-FC6H4
                "NSANRXXFOLBNPO-UHFFFAOYSA-N")), // 41e, 4-MeC6H4
        "40b-m with R1 = H and 41b-e with R1 = OMe must be extracted: " + inchiKeys);
  }

  /**
   * 35a-r of m50167957-9 give R as names: methyl, 4-chlorobenzyl, 3,4-dichlorobenzyl. Read from the
   * name table, all 18 rows of the positional table are extracted.
   */
  @Test
  public void testNamedSubstituentsResolve() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-9.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "QMOVTOKHCWUOGR-UHFFFAOYSA-N", // 35a, methyl
                "ODWFNZSLMMIJNP-UHFFFAOYSA-N", // 35b, 4-chlorobenzyl
                "ITCNXOKSNSYPOP-UHFFFAOYSA-N", // 35c, 4-bromobenzyl
                "IRAHZKYNKNCHSE-UHFFFAOYSA-N", // 35d, 3-bromobenzyl
                "YUSCBGIKGXUJNK-UHFFFAOYSA-N", // 35e, 2-bromobenzyl
                "QXHUFXLFUOVCFO-UHFFFAOYSA-N", // 35f, 3,4-dichlorobenzyl
                "CNSWOQSQACRCFZ-UHFFFAOYSA-N", // 35g
                "NMXHQKXSEATSIB-UHFFFAOYSA-N", // 35h
                "RBPMLTLQRDMHLF-UHFFFAOYSA-N", // 35i
                "UPROICUELGFOSZ-UHFFFAOYSA-N", // 35j
                "FYNVCALUIYIVMH-UHFFFAOYSA-N", // 35k
                "QAZHVDXUTCDBFT-UHFFFAOYSA-N", // 35l
                "NYZNWUOYBKBSLQ-UHFFFAOYSA-N", // 35m
                "FVBCUANCDBIGQI-UHFFFAOYSA-N", // 35n
                "AXWYTBUXWCKXNB-UHFFFAOYSA-N", // 35o
                "LLEADQLKRZTUNK-UHFFFAOYSA-N", // 35p
                "JJSKJZCMXJKMGC-UHFFFAOYSA-N", // 35q
                "OUWXKJYJBUNLLY-UHFFFAOYSA-N")), // 35r
        "35a-r must be extracted: " + inchiKeys);
  }

  /**
   * m50167957-10: the 43 scaffold is captioned "43" and takes the legend numbered 43, not the 42
   * legend that touches it too; series 49 and 50 ("49a–n: R1 = H / 50a–n: R1 = F") share the
   * lettered R2 legend, which names no compounds.
   */
  @Test
  public void testCaptionsAndSharedSeriesLegend() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-10.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "MRWCGIKQTYUHIZ-UHFFFAOYSA-N", // 43a, R = H
                "PAYKMJZOLNJHTI-UHFFFAOYSA-N", // 43b, R = 4-bromo
                "VCTGPSWNOZSEFL-UHFFFAOYSA-N", // 43f, R = 2-methoxy
                "DPSGCNWEQKOKPX-ZHZULCJRSA-N", // 49b, R1 = H, R2 = 2-CH3-C6H4
                "BJSDFJONOHASER-NVMNQCDNSA-N")), // 50n, R1 = F, R2 = 3-CF3, 4-Cl-C6H3
        "43a-l and 49/50 must be extracted: " + inchiKeys);
  }

  /**
   * m50167957-10 writes multipliers in front of the group (47c 3,5-diMe, 42c 2,4-diBr-6-NO2C6H2)
   * and names R1 of 45a-d "allyl".
   */
  @Test
  public void testLeadingMultipliersAndAllyl() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-10.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "YUMIFMLHNFWFRO-UHFFFAOYSA-N", // 42c, R = 2,4-diBr-6-NO2C6H2
                "DMXKOIYBKXVHMX-LICLKQGHSA-N", // 45b, R = 4-methoxy, R1 = allyl
                "DLPBVSQJBJTJPC-UHFFFAOYSA-N", // 47c, R = 3,5-diMe
                "JZOKHJZSIWEXFO-UHFFFAOYSA-N")), // 47f, R = 3,4,5-triOMe
        "42c, 45a-d and 47c/f/i must be extracted: " + inchiKeys);
  }

  /**
   * 64a/b of m50167957-12 give R1 as drawings ("64a: R1 =", "64b: R1 ="), each grouped with its
   * wavy line into one fragment and attached through an attachment point node rather than a carbon.
   * The groups are R1's values, Boc-azetidin-3-yl and Boc-piperidin-4-yl, not substances.
   */
  @Test
  public void testDrawnValueGroupedWithItsWavyLine() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-12.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "JCNNZMFADDRDOO-UHFFFAOYSA-N", // 64a, R1 = 1-Boc-azetidin-3-yl
                "CBRAVKAOQFEQMN-UHFFFAOYSA-N")), // 64b, R1 = 1-Boc-piperidin-4-yl
        "64a-b must be extracted: " + inchiKeys);
    for (String drawing :
        List.of(
            "BXRALUJTLUIVSG-UHFFFAOYSA-N", // the 64a drawing read as 3-ethyl-1-Boc-azetidine
            "DZEKXNZRIOWTHG-UHFFFAOYSA-N")) { // the 64b drawing read as 4-ethyl-1-Boc-piperidine
      assertFalse(inchiKeys.contains(drawing), "a drawn R value was extracted: " + drawing);
    }
  }

  /**
   * Scheme 80 of m50167957-15 draws its Ar values in a row below "Ar =", each cut off at a wavy
   * line, where no legend line takes them. They are substituents all the same, and were extracted
   * with their end atom and wavy line read as an ethyl group.
   */
  @Test
  public void testGroupsAtAWavyLineAreNotSubstances() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-15.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    for (String drawing :
        List.of(
            "BLDNWXVISIXWKZ-UHFFFAOYSA-N", // 4-ethylfluorobenzene, the 80b drawing
            "HLPIHRDZBHXTFJ-UHFFFAOYSA-N", // 2-ethylfuran, the 80c drawing
            "KLQNCSLBKKYPET-UHFFFAOYSA-N")) { // 4-ethyl-N,N-dimethylaniline, the 80a drawing
      assertFalse(inchiKeys.contains(drawing), "a drawn group was extracted: " + drawing);
    }
  }

  /**
   * Scheme 80 of m50167957-15 is a table of drawings: "Ar =" followed by six drawn groups, with the
   * letter lines "R = Cl a b c d e f" and "R = NO2 g h i j k" under them. Each letter takes the
   * drawing above it and its line's R; f has no NO2 partner.
   */
  @Test
  public void testDrawnTableTakesEachLetterFromItsColumn() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-15.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "OMVKAIFNRGTEKS-AWNIVKPZSA-N", // 80a, Ar = 4-(dimethylamino)phenyl, R = Cl
                "ISIGLMAPJCTJMQ-AWNIVKPZSA-N", // 80g, Ar = 4-(dimethylamino)phenyl, R = NO2
                "RPESPARQKUWFLG-CMDGGOBGSA-N", // 80c, Ar = 2-furyl, R = Cl
                "JHXPBBHAAXELGW-YCRREMRBSA-N", // 80k, Ar = 3-hydroxy-4-methoxyphenyl, R = NO2
                "NVBQPCDWKVJNQZ-ONNFQVAWSA-N")), // 80f, Ar = 4-hydroxy-3-methoxyphenyl, R = Cl
        "80a-k must be extracted: " + inchiKeys);
    assertFalse(
        inchiKeys.contains("RPLCVSQNTHFTFI-ONNFQVAWSA-N"), // f's drawing with R = NO2
        "the NO2 line has no letter under the sixth drawing");
  }

  /**
   * The linker of scheme 26 in m50167957-7 is a bracketed CH2 with repeat count n, which the legend
   * gives with R ("26g: n = 2, R = H"). Each n is built with the CH2 repeated that often, taking R
   * from the entries with that n.
   */
  @Test
  public void testRepeatCountFromTheLegend() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-7.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "FWCALDQWDFRQOR-UHFFFAOYSA-N", // 26b, n = 1, R = F
                "RXGJODRZHCDDPK-UHFFFAOYSA-N", // 26g, n = 2, R = H
                "FVPARRRAMFSXTE-UHFFFAOYSA-N", // 26k, n = 3, R = H
                "BHAZHQYFDXRQEX-UHFFFAOYSA-N")), // 26m, n = 3, R = Cl
        "26a-m must be extracted: " + inchiKeys);
  }

  /**
   * Schemes 32-34 of m50167957-8 give only n ("32a: n = 6", "32b: n = 4"); the scaffold has no R
   * and is built once per n instead of with the one CH2 drawn.
   */
  @Test
  public void testRepeatCountWithoutRGroups() throws IOException {
    List<BCXSubstance> substances = xtract("m50167957-8.cdx");
    Set<String> inchiKeys =
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());
    Set<String> markushKeys =
        substances.stream()
            .filter(BCXSubstance::isMarkush)
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());
    assertTrue(
        markushKeys.containsAll(
            Set.of("XWEFUSDSVPPVHJ-UHFFFAOYSA-N", "KPZRTSZGLDBHRP-UHFFFAOYSA-N")),
        "32a-b are built from the legend's repeat counts and must count as Markush: "
            + markushKeys);

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "XWEFUSDSVPPVHJ-UHFFFAOYSA-N", // 32a, n = 6
                "KPZRTSZGLDBHRP-UHFFFAOYSA-N")), // 32b, n = 4
        "32a-b must be extracted: " + inchiKeys);
    assertTrue(
        inchiKeys.contains("WVYWMCQVKOVNDH-UHFFFAOYSA-N"), // 30f, R = 2-NO2, 4-Cl
        "30f is one compound with both groups: " + inchiKeys);
    for (String split :
        List.of(
            "BRZGLCWDCAMAOC-UHFFFAOYSA-N", // 2-NO2 alone
            "NBLMEZPMDULSAW-UHFFFAOYSA-N")) { // 4-Cl alone
      assertFalse(inchiKeys.contains(split), "30f was split into two compounds: " + split);
    }
    assertFalse(
        inchiKeys.contains("RDJQXOXMVXYRMJ-UHFFFAOYSA-N"), "32 as drawn, with n = 1, is no entry");
  }

  /**
   * Schemes 20 and 21 of m50167957-7 letter their chalcone rings A and B in the drawing, with the
   * triazole and an OMe drawn across them as position variation, and give per compound where each
   * goes ("A ring B ring", "20a: 3-triazolyl 3,4-diOMe"). Each line is built once, instead of one
   * OMe at every position combined with every triazole position.
   */
  @Test
  public void testRingTablePlacesEachRingsGroup() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-7.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "WUHVEXZNAMRPKL-IZZDOVSWSA-N", // 20a, A: 3-triazolyl, B: 3,4-diOMe
                "TXCKVPNEJNLISB-NTEUORMPSA-N", // 20c, A: 4-triazolyl, B: 2,3-diOMe
                "GASHLVRXZIPQLU-ACCUITESSA-N", // 21c, A: 3-triazolyl, B: 2,3,4-triOMe
                "XJAAYLPYUDNMMY-GXDHUFHOSA-N")), // 21d, A: 4-triazolyl, B: 2,3,4-triOMe
        "20a-c and 21a-d must be extracted: " + inchiKeys);
    assertFalse(
        inchiKeys.contains("AAWUKLHFPLSCEP-NTEUORMPSA-N"), // one 2-OMe, 4-triazolyl
        "the position variation is not enumerated beside the table");
  }

  /**
   * The mechanism of m50848776-i4 numbers its intermediates III, IV, V and VI with the structure
   * tool: III also lies on the page as three bonded iodine atoms, IV and VI as nodes expanding to
   * iodine and vanadium, and V as a vanadium atom. None of them is a compound.
   */
  @Test
  public void testRomanNumeralsAreNoCompounds() throws IOException {
    Set<String> smiles =
        xtract("m50848776-i4.cdx").stream()
            .map(BCXSubstance::getSmiles)
            .collect(Collectors.toSet());

    for (String numeral : List.of("III", "I[V]", "[V]")) {
      assertFalse(smiles.contains(numeral), "a Roman numeral was extracted: " + numeral);
    }
    assertEquals(30, smiles.size(), "27 scope entries, 11, 11' and the HFIP drawing: " + smiles);
  }

  /**
   * Reagent 26 of m50848776-i9 is a benzene ring drawn with aromatic (order 1.5) bonds, which
   * ChemDraw stores with no hydrogens on the ring carbons. Its SMILES was a hexaradical; the ring
   * now gets its double bonds.
   */
  @Test
  public void testAromaticBondsGetDoubleBonds() throws IOException {
    Set<String> smiles =
        xtract("m50848776-i9.cdx").stream()
            .map(BCXSubstance::getSmiles)
            .collect(Collectors.toSet());

    assertTrue(smiles.contains("C1=CC(=C(C=C1)N)N"), "o-phenylenediamine: " + smiles);
    assertFalse(
        smiles.stream().anyMatch(smi -> smi.contains("[CH]")),
        "a radical was extracted: " + smiles);
  }

  /**
   * m50848776-i11 sets three schemes in a row, ortho-, para- and meta-R, each with its legend under
   * it. Side by side, the legends were merged as columns of one, and each scaffold took every
   * value. Each now keeps its own.
   */
  @Test
  public void testLegendsUnderTheirOwnScaffoldsStaySeparate() throws IOException {
    Set<String> inchiKeys =
        xtract("m50848776-i11.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "VCYNMKHKCLKODE-UHFFFAOYSA-N", // ortho, R = F
                "JUKGOVSNHDRQSS-UHFFFAOYSA-N")), // meta, R = F
        "own values must be extracted: " + inchiKeys);
    for (String borrowed :
        List.of(
            "GPWYRDPTGJYJNL-UHFFFAOYSA-N", // ortho with the para legend's Cl
            "WIQLLVBXSMNLAI-UHFFFAOYSA-N", // meta with the para legend's OMe
            "MIQATMAUOFDNCO-UHFFFAOYSA-N")) { // para with the ortho legend's F
      assertFalse(inchiKeys.contains(borrowed), "a neighbour's value was taken: " + borrowed);
    }
  }

  /**
   * "R = Ts" in m50848776-i14 means tosyl. Ts is also the symbol of tennessine, and a value the
   * abbreviation table does not hold was put on the scaffold as that element.
   */
  @Test
  public void testTosylValueIsNotTennessine() throws IOException {
    List<BCXSubstance> substances = xtract("m50848776-i14.cdx");

    for (BCXSubstance substance : substances) {
      assertFalse(
          substance.getSmiles().contains("[Ts]"), "tennessine atom: " + substance.getSmiles());
    }
  }

  /**
   * m50848776-i14 marks the ring bond each reaction forms with a red bold bond, against black
   * bonds. A bold bond is otherwise read as a wedge, which made the CH(Ar) centre of most products
   * one enantiomer, depending on the direction the bond was drawn in. A bold bond coloured apart
   * from its neighbours is a highlight and carries no stereo.
   */
  @Test
  public void testHighlightedBoldBondIsNotAWedge() throws IOException {
    List<BCXSubstance> substances = xtract("m50848776-i14.cdx");

    Set<String> inchiKeys =
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());
    assertTrue(
        inchiKeys.contains("YJMQFVNLHSNHTG-UHFFFAOYSA-N"),
        "4-F, N-Ts product must have no defined centre: " + inchiKeys);
    for (BCXSubstance substance : substances) {
      assertFalse(substance.getSmiles().contains("@"), "defined centre: " + substance.getSmiles());
    }
  }

  /**
   * m50966958-i14 writes the aryl of three scaffolds into the label, SAr and OAr. ChemDraw cannot
   * interpret those labels and stores S (or O) and Ar unbonded, so Ar never reached the scaffold
   * and none of the scaffolds' legend values was grafted. The skeletons below are computed from
   * hand-written SMILES; the E/Z layer is left out.
   */
  @Test
  public void testArylWrittenIntoTheLabelIsSubstituted() throws IOException {
    Set<String> skeletons =
        xtract("m50966958-i14.cdx").stream()
            .map(substance -> substance.getInchiKey().substring(0, 14))
            .collect(Collectors.toSet());

    assertTrue(
        skeletons.containsAll(
            Set.of(
                "WWZSINBEUHVOOJ", // N-Ts scaffold, SAr, Ar = Ph
                "ITBGDISPURENFG", // N-Ts scaffold, SAr, Ar = 4-MeOC6H4
                "GQNLLCGRGJUEHU", // OAr, Ar = 4-MeC6H4
                "LLWRTOYZVGRIEF", // OAr, Ar = 4-BrC6H4
                "GURSDJOEKSYBON", // benzoxazolyl scaffold, SAr, Ar = Ph
                "JXVLWTFMOQJDNA")), // benzoxazolyl scaffold, SAr, Ar = 4-BrC6H4
        "aryl values must be grafted: " + skeletons);
  }

  /**
   * m50966958-i16 splits one series over two scaffolds: 49a-f under Ph-NH-C6H4-R and 49g-m under
   * R-C6H4-NH-tolyl. The lettering runs on, but each line names its compound (49a, 49g), so each
   * letter is one compound and the columns are two legends, not one set in columns.
   */
  @Test
  public void testNumberedRunOnLegendsStayWithTheirScaffolds() throws IOException {
    List<BCXSubstance> substances = xtract("m50966958-i16.cdx");
    Set<String> inchiKeys =
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "CASDLXCHUTYPAO-UHFFFAOYSA-N", // 49e, Ph-NH-C6H4-2-Cl
                "CUOBXBGOURGGON-UHFFFAOYSA-N")), // 49g, 2-Me-C6H4-NH-tolyl
        "own values must be extracted: " + inchiKeys);
    for (String borrowed :
        List.of(
            "JTMODJXOTWYBOZ-UHFFFAOYSA-N", // Ph-NH-C6H4-2-Me, 49g on the 49a-f scaffold
            "YOBSWLIPRRHPJF-UHFFFAOYSA-N")) { // 2-Cl-C6H4-NH-tolyl, 49e on the 49g-m scaffold
      assertFalse(inchiKeys.contains(borrowed), "the other column's value was taken: " + borrowed);
    }
    assertEquals(16, substances.size(), "6 + 7 legend products, 2 drawn amines, triflate");
  }

  /**
   * Legend values that resolve only through BChemLookup's abbreviations.smi, one per fixture. Each
   * one left its structure with an unresolved R-group, so the structure was skipped. Skeletons
   * (InChIKey first block) keep coordinate-dependent stereo out of the assertion.
   */
  @Test
  public void testLegendValuesFromTheLookupTableResolve() throws IOException {
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("m50167957-8.cdx", "MBVRTBYGZBSHMP"); // CH2OAc
    expected.put("m50167957-9.cdx", "RBUAIXDSNOWRNA"); // 4-H2NSO2C6H4
    expected.put("m50167957-10.cdx", "KSKVFJZUPIPVLJ"); // COC6H5
    expected.put("m50167957-12.cdx", "GSJMMRBZOXIRSE"); // thiophen-2-yl
    expected.put("m50167957-14.cdx", "ZMEOULWJVHLOSW"); // 4-(diethylamino)phenyl
    expected.put("m50848776-i10.cdx", "OMSNJSAFDZXWFM"); // R1 = OCF3
    expected.put("m50848776-i14.cdx", "GPRNRFHXOGQXSD"); // R = Ts on the carbazole N
    expected.put("m50848776-i18.cdx", "BEQXOEJOKGKNNW"); // 1-naph
    expected.put("m50966958-i14.cdx", "KCUGABUAJCLBTM"); // 3-NH2C6H4

    for (Map.Entry<String, String> entry : expected.entrySet()) {
      Set<String> skeletons =
          xtract(entry.getKey()).stream()
              .map(substance -> substance.getInchiKey().substring(0, 14))
              .collect(Collectors.toSet());
      assertTrue(
          skeletons.contains(entry.getValue()),
          entry.getKey() + " must yield " + entry.getValue() + ": " + skeletons);
    }
  }

  /**
   * m19546377-i17 lists one compound per line, each with its own number ("60 : R = CH3", "61 : R =
   * H", ...), under a scheme whose two scaffolds carry no number. Lines numbered with different
   * compounds are one legend here, not the legends of different schemes, and both scaffolds take
   * all its values.
   */
  @Test
  public void testLegendOfSingleNumberedCompoundsStaysOneLegend() throws IOException {
    assertEquals(
        Set.of(
            "QUHOIGLGSORZDD-UHFFFAOYSA-N", // 60, R = CH3, starting material
            "DVAOKAXLOOTUEB-UHFFFAOYSA-N", // 61, R = H
            "WUODYPVVGQSEFW-UHFFFAOYSA-N", // 62, R = i-Pr
            "XCRKAXWZVHTFNF-UHFFFAOYSA-N", // 63, R = Ph
            "VMOJVZHPPAADFO-UHFFFAOYSA-N", // 64, R = p-CH3OC6H4
            "BICHKGYNVODKKT-UHFFFAOYSA-N", // 65, R = p-CNC6H4
            "WYPHMBCYXSXAQY-UHFFFAOYSA-N", // 67, R = p-CF3C6H4
            "GNOBSDSRABCCJJ-UHFFFAOYSA-N", // 60, R = CH3, product
            "MHPLKEREIIYDRC-UHFFFAOYSA-N", // 61, R = H
            "UUAOOWVRBQEFDF-UHFFFAOYSA-N", // 62, R = i-Pr
            "BJLBKTKCAVWISZ-UHFFFAOYSA-N", // 63, R = Ph
            "CGSKCMAFFTZTFE-UHFFFAOYSA-N", // 64, R = p-CH3OC6H4
            "TZLPCFCHSCFMPF-UHFFFAOYSA-N", // 65, R = p-CNC6H4
            "UOEIZWUERCGHMI-UHFFFAOYSA-N"), // 67, R = p-CF3C6H4
        markushKeys("m19546377-i17.cdx"),
        "every line of the legend must reach both scaffolds, and nothing else");
  }

  /**
   * The published version of m19546377-i17 captions starting material 35 and product 36 and letters
   * the legend 36a-h. The legend names product 36 only, but starting material 35 is the same
   * reaction's and takes its values too.
   */
  @Test
  public void testStartingMaterialSharesItsProductsNumberedLegend() throws IOException {
    Set<String> keys = markushKeys("m19546377-i17-captioned.cdx");

    assertTrue(
        keys.containsAll(
            Set.of(
                "QUHOIGLGSORZDD-UHFFFAOYSA-N", // 35a, R = CH3
                "XCRKAXWZVHTFNF-UHFFFAOYSA-N", // 35d, R = Ph
                "GNOBSDSRABCCJJ-UHFFFAOYSA-N", // 36a, R = CH3
                "BJLBKTKCAVWISZ-UHFFFAOYSA-N")), // 36d, R = Ph
        "starting material 35 and product 36 take the legend: " + keys);
  }

  /**
   * m30810289-i26 sets the legend of product 73 in four columns in a row under products 72 and 73.
   * A mechanism drawing with the same R lies 1 pt nearer below one column than product 73 above it,
   * which split the legend so that its last column (R = CF3, CN) reached no scaffold. A legend
   * belongs to the structure above it. The mechanism drawings below the legend use R for the whole
   * N-aryl group and take none of its values.
   */
  @Test
  public void testLegendColumnBelongsToTheScaffoldAboveIt() throws IOException {
    assertEquals(
        Set.of(
            "ONQBUHWENXKHHP-UHFFFAOYSA-N", // 72, R = H
            "NCGNJJRGLWENIQ-UHFFFAOYSA-N", // 72, R = Br
            "OTWGSFCKTHTQIP-UHFFFAOYSA-N", // 72, R = SF5
            "AUINWHFNCUUISO-UHFFFAOYSA-N", // 72, R = NO2
            "GNVXOIRECQQWBJ-UHFFFAOYSA-N", // 72, R = OMe
            "XDGKEQYJMZFKAZ-UHFFFAOYSA-N", // 72, R = Me
            "ZLHKWIRYBGFXCN-UHFFFAOYSA-N", // 72, R = CF3
            "ULJBGEGYQGIJMJ-UHFFFAOYSA-N", // 72, R = CN
            "GIYSQRQZNXRMEJ-UHFFFAOYSA-N", // 73, R = H
            "JVOXSSDBKAAMDI-UHFFFAOYSA-N", // 73, R = Br
            "JIXDOEYXALVJJB-UHFFFAOYSA-N", // 73, R = SF5
            "CUINWIDXCKNPQP-UHFFFAOYSA-N", // 73, R = NO2
            "UNFQNAZJMCNXNI-UHFFFAOYSA-N", // 73, R = OMe
            "ZJLUZNPKQDILOV-UHFFFAOYSA-N", // 73, R = Me
            "QRNCQWPDWGHURQ-UHFFFAOYSA-N", // 73, R = CF3
            "XJAINSGUJFLKAL-UHFFFAOYSA-N"), // 73, R = CN
        markushKeys("m30810289-i26.cdx"),
        "the legend must reach products 72 and 73 and no mechanism drawing");
  }

  /**
   * m27252839-i71 numbers three catalyst lines 1 to 3 ("1. [(IPr)CuCl] R = 2,6-iPr2C6H3"). They are
   * one legend for the NHC drawn beside them. Product 187 carries its own caption and an R of its
   * own, and takes none of the catalysts' values.
   */
  @Test
  public void testCaptionedProductDoesNotTakeAnotherCompoundsLegend() throws IOException {
    assertEquals(
        Set.of(
            "NJLYZISHBSABMZ-UHFFFAOYSA-N", // 1, IPr
            "RSTLYZXJMXWXEI-UHFFFAOYSA-N", // 2, IMes
            "HHSNIRLXHJNXAK-UHFFFAOYSA-N"), // 3, IiPr
        markushKeys("m27252839-i71.cdx"),
        "only the NHC takes the catalyst legend");
  }

  /**
   * m35370432-10 heads a table column with a lone X, and a legend gives "81: X = O". The heading is
   * no structure and must not come out as water.
   */
  @Test
  public void testLoneLabelIsNotExpanded() throws IOException {
    Set<String> inchiKeys =
        xtract("m35370432-10.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertFalse(inchiKeys.contains("XLYOFNOQVPJJNP-UHFFFAOYSA-N"), "water from the X heading");
  }

  /** The InChIKeys of the structures a file's legends build. */
  private Set<String> markushKeys(String fileName) throws IOException {
    return xtract(fileName).stream()
        .filter(BCXSubstance::isMarkush)
        .map(BCXSubstance::getInchiKey)
        .collect(Collectors.toSet());
  }

  /**
   * 63i and 63j of m50167957-12 write two ring substituents in one value joined by a hyphen, "R =
   * 2-NO2-5-F", where other legends use a comma ("2-NO2, 4-Cl"). Both groups go on the ring at
   * their own positions. The keys are computed from hand-written SMILES.
   */
  @Test
  public void testTwoSubstituentsJoinedByAHyphen() throws IOException {
    Set<String> inchiKeys =
        xtract("m50167957-12.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "MBROATSVDAZAKS-UHFFFAOYSA-N", // 63i, 2-NO2-5-F
                "PSGUJSJDWDBPPG-UHFFFAOYSA-N")), // 63j, 2-NO2-4-F
        "63i and 63j must be extracted: " + inchiKeys);
  }

  /**
   * m26579870-i14 puts its R values on a pyridine drawn with the H that reacts at C2. A pyridine is
   * numbered from its nitrogen, not from that drawn H: R = 3-F is 3-fluoropyridine, not
   * 2-fluoropyridine. The keys are computed from hand-written SMILES.
   */
  @Test
  public void testPyridinePositionsCountFromTheNitrogen() throws IOException {
    Set<String> inchiKeys =
        xtract("m26579870-i14.cdx").stream()
            .map(BCXSubstance::getInchiKey)
            .collect(Collectors.toSet());

    assertTrue(
        inchiKeys.containsAll(
            Set.of(
                "CELKOWQJPVJKIL-UHFFFAOYSA-N", // R = 3-F, 3-fluoropyridine
                "HWWYDZCSSYKIAD-UHFFFAOYSA-N", // R = 3,5-diMe, 3,5-lutidine
                "WUPLOZFIOAEYMG-UHFFFAOYSA-N")), // 69d, R = 3-NO2-6-OMe
        "pyridine positions must count from N: " + inchiKeys);
    for (String wrong :
        List.of(
            "MTAODLNXWYIKSO-UHFFFAOYSA-N", // 2-fluoropyridine
            "JYYNAJVZFGKDEQ-UHFFFAOYSA-N", // 2,4-lutidine
            "MWQMHHLIYVQKNM-UHFFFAOYSA-N")) { // 5-methoxy-2-nitropyridine
      assertFalse(inchiKeys.contains(wrong), "counted from the drawn H: " + wrong);
    }
  }

  /**
   * A residue grafted at a stereocentre takes the drawn residue's place in the centre's
   * configuration. m30177584-2 draws one catalyst twice, as its (E) and (Z) amide rotamers, with R
   * wedged at the pyrrolidine C2 in opposite senses and the naphthyl wedged against it; the legend
   * gives R = H and R = Me. Both drawings describe the same centre, R for R = Me. The stereo
   * element used to keep the removed residue (read as an implicit hydrogen), so the SMILES gave S,
   * the R = H centre was lost, and the InChI followed wherever the layout put the new methyl.
   */
  @Test
  public void testGraftAtStereocentreKeepsConfiguration() throws IOException, CDKException {
    List<BCXSubstance> substances = xtract("m30177584-2.cdx");

    assertEquals(
        Set.of(
            "HHHJIMMFAOJRPW-ANHUGMMASA-N", // R = H
            "CQYCKTNKJYWUGM-ZLHGTEGGSA-N"), // R = Me, C2 is R
        substances.stream().map(BCXSubstance::getInchiKey).collect(Collectors.toSet()),
        "both rotamer drawings give the same two products");
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    for (BCXSubstance substance : substances) {
      String fromSmiles =
          InChIGeneratorFactory.getInstance()
              .getInChIGenerator(smilesParser.parseSmiles(substance.getSmiles()))
              .getInchi();
      assertEquals(substance.getInchi(), fromSmiles, "SMILES and InChI disagree");
    }
  }
}
