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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.beilstein.chemxtract.cdx.CDDocument;
import org.beilstein.chemxtract.cdx.reader.CDXMLReader;
import org.beilstein.chemxtract.cdx.reader.CDXReader;
import org.beilstein.chemxtract.model.BCXSubstance;
import org.beilstein.chemxtract.model.BCXSubstanceInfo;
import org.beilstein.chemxtract.xtractor.SubstanceXtractor;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

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
}
