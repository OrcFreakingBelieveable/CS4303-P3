public class LevelState {

    private final DontDrown sketch;

    public static final int ABS_MAX_STRESS = 100;

    private static final int FRAMES_PER_RESKETCH_MAX = 40;
    private static final int FRAMES_PER_RESKETCH_MIN = 10;
    private static final int FRAMES_PER_RESKETCH_RANGE = FRAMES_PER_RESKETCH_MAX - FRAMES_PER_RESKETCH_MIN;

    public enum Debuff {
        STRESS_MOTIVATED,
        TUNNEL_VISION,
        PANIC_PRONE,
        LACK_CONTRAST,
        NONE;
    }

    public int tokensCollected = 0;
    public int oldStress = 0;
    public int stress = 0;
    public int maxStress = 100;
    public int minStress = 0;
    public int stressEffectThreshold = 20;
    public Debuff debuff = Debuff.NONE;
    public boolean debugging = true;

    public float pcThrust;
    public float pcFriction;
    public float[] stressHSBColour;
    public int framesPerResketch;

    private float pcThrustDenominator = (float) ABS_MAX_STRESS - stressEffectThreshold;
    private float pcFrictionDenominator = pcThrustDenominator * 2f;

    private float stressRange = (ABS_MAX_STRESS - stressEffectThreshold);

    private float stressHueMultiplier = (PlayerCharacter.PC_MAX_HUE - PlayerCharacter.PC_MIN_HUE)
            / stressRange;
    private float stressSatMultiplier = (PlayerCharacter.PC_MAX_SAT - PlayerCharacter.PC_MIN_SAT)
            / stressRange;
    private float stressLightMultiplier = (PlayerCharacter.PC_MAX_LIGHT - PlayerCharacter.PC_MIN_LIGHT)
            / stressRange;

    private float strokeVariabilityMultiplier = (Sketcher.RSV_MAX - Sketcher.RSV_MIN) / stressRange;
    private float strokeShakinessMultiplier = (Sketcher.RSS_MAX - Sketcher.RSS_MIN) / stressRange;
    private float framesPerResketchMultiplier = FRAMES_PER_RESKETCH_RANGE / stressRange;

    public LevelState(DontDrown sketch) {
        this.sketch = sketch;
    }

    private void pcThrust() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcThrust = sketch.pc.horizontalThrustDef * (1 + (stress - stressEffectThreshold) / pcThrustDenominator);
        } else {
            pcThrust = sketch.pc.horizontalThrustDef;
        }
    }

    private void pcFriction() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcFriction = sketch.pc.horizontalFrictionDef
                    * (1 - (stress - stressEffectThreshold) / pcFrictionDenominator);
        } else {
            pcFriction = sketch.pc.horizontalFrictionDef;
        }
    }

    private void stressHSBColour() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            float[] hsb = new float[3];
            int stressRating = stress - stressEffectThreshold;
            hsb[0] = PlayerCharacter.PC_MIN_HUE + stressRating * stressHueMultiplier;
            hsb[1] = PlayerCharacter.PC_MIN_SAT + stressRating * stressSatMultiplier;
            hsb[2] = PlayerCharacter.PC_MIN_LIGHT + stressRating * stressLightMultiplier;
            stressHSBColour = hsb;
        } else {
            stressHSBColour = new float[] { PlayerCharacter.PC_MIN_HUE, PlayerCharacter.PC_MIN_SAT,
                    PlayerCharacter.PC_MIN_LIGHT };
        }

    }

    private void sketchiness() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            int stressRating = stress - stressEffectThreshold;
            framesPerResketch = (int) (FRAMES_PER_RESKETCH_MAX - stressRating * framesPerResketchMultiplier);
            sketch.roughStrokeVariabilityRate = Sketcher.RSV_MIN + stressRating * strokeVariabilityMultiplier;
            sketch.roughStrokeShakiness = Sketcher.RSS_MIN + stressRating * strokeShakinessMultiplier;

        } else {
            framesPerResketch = FRAMES_PER_RESKETCH_MAX;
            sketch.roughStrokeVariabilityRate = Sketcher.RSV_MIN;
            sketch.roughStrokeShakiness = Sketcher.RSS_MIN;
        }
    }

    public void update() {
        if (stress > maxStress) {
            stress = maxStress;
        } else if (stress < minStress) {
            stress = minStress;
        }
        pcThrust();
        pcFriction();
        stressHSBColour();
        sketchiness();
        oldStress = stress;
    }
}
