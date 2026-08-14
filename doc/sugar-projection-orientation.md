# Sugar chair projections and drawing orientation

Discussion notes for the reported concern that a sugar drawn in a chair
conformation "will not work correctly anymore" if it is not drawn
horizontally but shifted by some degrees.

Evidence below comes from `SugarProjectionRotationTest`
(`src/test/java/org/beilstein/chemxtract/cheminf/`), which rotates the
fixture `src/test/resources/integrationTests/sugars/bond_up_SRSSR.cdx`
(β-D-glucopyranose chair) and re-runs the full extraction pipeline.

## Summary of measured behaviour

Fixture rotated in 5° steps through the full circle:

| Rotation | Result |
|---|---|
| \|θ\| < 90° | `WQZGKKKJIJFFOK-DVKNGEFBSA-N` — correct, unchanged |
| \|θ\| = 90° | `WQZGKKKJIJFFOK-UHFFFAOYSA-N` — stereo layer dropped |
| \|θ\| > 90° | `WQZGKKKJIJFFOK-MDMQIMBFSA-N` — the enantiomer |

Chair *detection* succeeded at every angle tested, including 90° and 180°.

## 1. The reported concern does not reproduce

- For every tilt with |θ| < 90° the extracted InChIKey is unchanged. A few
  degrees of tilt — the case that was reported — is handled correctly.
- Chair recognition is rotation-invariant by construction.
  `SugarProjectionDetector` classifies a ring from its winding pattern (the
  sign of a determinant at each vertex). Rotation preserves determinant
  signs, so the classification cannot depend on orientation.
- The 5° `CARDINALITY_THRESHOLD` in `SugarProjectionDetector` is not on the
  chair path at all. It only gates *Haworth* alignment
  (`checkHaworthAlignment`), which chair detection skips. The mechanism the
  report suspected is not in play.
- CDK handles tilt explicitly rather than by accident:
  `CyclicCarbohydrateRecognition.horizontalOffset()` computes the chair's
  axis angle and de-rotates every substituent vector by it before
  classifying up/down. Tilt support is a designed feature.

## 2. The 180° result is the drawing convention, not a defect

- The fixture's chair ring is **exactly centrosymmetric** about its own
  centroid — verified numerically from the extracted coordinates: atom 0 ↔ 3,
  1 ↔ 4, 2 ↔ 5 map onto each other to within float noise. A 180° rotation
  maps the ring outline onto itself, so nothing in the ring geometry can
  distinguish the two orientations.
- Chair reading rule: at a peak vertex the axial bond points up, at a valley
  vertex it points down. Rotate the drawing 180° and every atom swaps
  peak ↔ valley while its substituent swaps up ↔ down. Read by the standard
  convention, every atom's implied z flips. Flipping all z is a reflection,
  therefore the enantiomer.
- So `R180(D-glucose chair)` *is* the conventional drawing of L-glucose.
  `…-MDMQIMBFSA-N` is the correct reading of that picture, not a corruption
  of it.
- Precedent: Fischer projections have the same property — a Fischer rotated
  90° denotes a different compound. Chair and Haworth projections are
  orientation-conventional drawing systems. Orientation is data, not
  decoration.

## 3. Forcing rotation-invariance would be strictly worse

- A rotation-invariant algorithm must, by definition, return the same answer
  for a drawing `D` and for `R180(D)`. The two differ only by a rotation.
- Achieving that means canonicalising the orientation from some
  rotation-covariant feature of the drawing. The ring has none — it is
  centrosymmetric.
- Every candidate tiebreak is arbitrary. The apex chosen by
  `chairCenterOffset()` depends on where `GraphUtil.cycle()` starts walking
  the ring; a centroid-based vector depends on the substituent pattern.
  Either would place "up" correctly for roughly half of all inputs.
- Net effect: we would trade a defensible reading of an unusual drawing for
  silent inversion of **normally drawn** structures — a far larger blast
  radius on the common path.
- Rotation-invariance and correctness on canonically drawn chairs are
  mutually exclusive here. That is a property of the notation, not of this
  implementation.

## 4. The ±90° stereo dropout is correct behaviour

- With a near-vertical axis CDK bails out
  (`|angle| − π/2 ≤ 0.05`, roughly ±2.9°) and emits no stereo elements.
  Measured: `…-UHFFFAOYSA-N` at exactly ±90°.
- That is a refusal to guess inside a genuinely ambiguous band, which is the
  right default. Missing stereo is visible and recoverable; wrong stereo is
  silent.

## 5. Where the report has a point

- The failure mode is silent. Nothing in the output signals that a drawing
  was in an orientation where the convention may not match intent.
- `StereoHandler.extractSugarStereoElements()` calls `removeBondDisplay()`,
  which resets **every** bond to `IBond.Display.Solid` before perception. If
  a chemist drew explicit wedge/hash bonds on the chair — unambiguous,
  orientation-independent 3D information — it is discarded in favour of the
  ambiguous outline convention. This is a real gap and arguably the thing
  worth fixing.
- None of this is documented for callers.

## 6. Possible follow-ups

- **Documentation** — record the orientation convention in
  [LIMITATIONS.md](../LIMITATIONS.md) and in the `SugarProjectionDetector`
  javadoc.
- **Diagnostics** — log at WARN when a chair's axis angle falls outside a
  comfortable band, so the case is at least visible in the extraction log.
- **Real improvement** — honour drawn wedge/hash bonds on chairs instead of
  erasing them, giving chemists an escape hatch that does not depend on page
  orientation.
- **Regression cover** — already in place: `SugarProjectionRotationTest`
  pins tilt-invariance up to ±85° and pins the ≥90° reading, so any future
  change to either trips a test.

## Open caveat

The peak/valley derivation in section 2 is reasoned from the ring geometry
plus CDK's implementation. CDK's own unit tests for
`CyclicCarbohydrateRecognition` were not available locally to confirm the
behaviour is asserted deliberately, and the paper CDK cites for the method
(`batchelor13`) has not been consulted. If the discussion turns on that
point, those are the two sources to pull.
