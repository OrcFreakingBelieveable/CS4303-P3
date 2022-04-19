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

    public int tokensAvailable = 0; 
    public int tokensCollected = 0;
    public int oldStress = 0;
    public int stress = 0;
    public int maxStress = 100;
    public int minStress = 0;
    public int stressEffectThreshold = 20;
    public Debuff debuff = Debuff.NONE;

    public float pcThrust;
    public float pcFriction;
    public float pcMinSpeed;
    public float[] stressHSBColour;
    public int framesPerResketch;

    private float pcThrustMultiplier;
    private float pcFrictionMultiplier;

    private final float stressRange = (ABS_MAX_STRESS - stressEffectThreshold);

    private final float stressHueMultiplier = (PlayerCharacter.PC_MAX_HUE - PlayerCharacter.PC_MIN_HUE)
            / stressRange;
    private final float stressSatMultiplier = (PlayerCharacter.PC_MAX_SAT - PlayerCharacter.PC_MIN_SAT)
            / stressRange;
    private final float stressLightMultiplier = (PlayerCharacter.PC_MAX_LIGHT - PlayerCharacter.PC_MIN_LIGHT)
            / stressRange;

    private float strokeVariabilityMultiplier = (Sketcher.RSV_MAX - Sketcher.RSV_MIN) / stressRange;
    private float strokeShakinessMultiplier = (Sketcher.RSS_MAX - Sketcher.RSS_MIN) / stressRange;
    private float framesPerResketchMultiplier = FRAMES_PER_RESKETCH_RANGE / stressRange;

    public LevelState(DontDrown sketch) {
        this.sketch = sketch;
    }

    public void reset(Level level) {
        tokensAvailable = level.tokens.size(); 
        tokensCollected = 0;
        oldStress = 0;
        stress = 0;
        maxStress = 100;
        minStress = 0;
        stressEffectThreshold = 20;
        debuff = Debuff.NONE;
        update(); 
    }

    public void pcCalcs() {
        this.pcThrustMultiplier = (sketch.pc.maxHorizontalThrust - sketch.pc.minHorizontalThrust) / stressRange;
        this.pcFrictionMultiplier = (sketch.pc.maxHorizontalFriction - sketch.pc.minHorizontalFriction) / stressRange;
    }

    private void pcThrust() {
        int stressRating = stress - stressEffectThreshold;
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcThrust = sketch.pc.minHorizontalThrust + stressRating * pcThrustMultiplier;
        } else {
            pcThrust = sketch.pc.minHorizontalThrust;
        }
    }

    private void pcFriction() {
        int stressRating = stress - stressEffectThreshold;
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcFriction = sketch.pc.maxHorizontalFriction - stressRating * pcFrictionMultiplier;

        } else {
            pcFriction = sketch.pc.maxHorizontalFriction;
        }
    }

    private void pcMinSpeed() {
        pcMinSpeed = pcFriction * PlayerCharacter.I_MASS * sketch.pc.incr;
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
            sketch.roughStrokeShakiness = (int) (Sketcher.RSS_MIN + stressRating * strokeShakinessMultiplier);

        } else {
            framesPerResketch = FRAMES_PER_RESKETCH_MAX;
            sketch.roughStrokeVariabilityRate = Sketcher.RSV_MIN;
            sketch.roughStrokeShakiness = Sketcher.RSS_MIN;
        }
    }

    public void collectToken(Token token) {
        sketch.level.tokens.remove(token);
        tokensCollected++;
    }

    public void update() {
        if (stress > maxStress) {
            stress = maxStress;
        } else if (stress < minStress) {
            stress = minStress;
        }
        pcThrust();
        pcFriction();
        pcMinSpeed();
        stressHSBColour();
        sketchiness();
        oldStress = stress;
    }
}
