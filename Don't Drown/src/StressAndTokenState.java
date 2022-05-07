public class StressAndTokenState {

    private final DontDrown sketch;

    public static final int ABS_MAX_STRESS = 100;
    public static final int FRAMES_PER_RESKETCH_MAX = 40;
    public static final int FRAMES_PER_RESKETCH_MIN = 10;
    public static final int FRAMES_PER_RESKETCH_RANGE = FRAMES_PER_RESKETCH_MAX - FRAMES_PER_RESKETCH_MIN;
    public static final float STRESS_INCR_RATE = 0.75f;
    public static final float STRESS_DECR_RATE = 0.75f;
    public static final float STRESS_INCR_RANGE_DIV = 2.5f;
    public static final float STRESS_DECR_RANGE_DIV = 2.5f;


    // level and stress values 
    public int tokensAvailable = 0;
    public int tokensCollected = 0;
    public float oldStress = 0;
    public float stress = 0f;
    public int maxStress = 100;
    public int minStress = 0;
    public int stressEffectThreshold = 20;
    public float stressIncrRange;
    public float stressDecrRange;
    public Debuff debuff = Debuff.NONE;

    // pc values 
    public float pcThrust;
    public float pcFriction;
    public float pcMinSpeed;
    public float[] stressHSBColour;
    public int framesPerResketch;

    // calculation values 
    private float pcThrustMultiplier;
    private float pcFrictionMultiplier;

    // debuffs 

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

    public StressAndTokenState(DontDrown sketch) {
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
        stressIncrRange = sketch.height / STRESS_INCR_RANGE_DIV;
        stressDecrRange = sketch.height / STRESS_DECR_RANGE_DIV;
        level.reset();
        update();
    }

    /**
     * Calculates the force multipliers for the PC.
     */
    public void pcCalcs() {
        this.pcThrustMultiplier = (sketch.pc.maxHorizontalThrust - sketch.pc.minHorizontalThrust) / stressRange;
        this.pcFrictionMultiplier = (sketch.pc.maxHorizontalFriction - sketch.pc.minHorizontalFriction) / stressRange;
    }

    private void pcThrust() {
        float stressRating = stress - stressEffectThreshold;
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcThrust = sketch.pc.minHorizontalThrust + stressRating * pcThrustMultiplier;
        } else {
            pcThrust = sketch.pc.minHorizontalThrust;
        }
    }

    private void pcFriction() {
        float stressRating = stress - stressEffectThreshold;
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            pcFriction = sketch.pc.maxHorizontalFriction - stressRating * pcFrictionMultiplier;

        } else {
            pcFriction = sketch.pc.maxHorizontalFriction;
        }
    }

    /** The speed at which the PC comes to rest. */
    private void pcMinSpeed() {
        pcMinSpeed = pcFriction * PlayerCharacter.I_MASS * sketch.pc.incr;
    }

    public void recalcStressHSBColour() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            float[] hsb = new float[3];
            float stressRating = stress - stressEffectThreshold;
            hsb[0] = PlayerCharacter.PC_MIN_HUE + stressRating * stressHueMultiplier;
            hsb[1] = PlayerCharacter.PC_MIN_SAT + stressRating * stressSatMultiplier;
            hsb[2] = PlayerCharacter.PC_MIN_LIGHT + stressRating * stressLightMultiplier;
            stressHSBColour = hsb;
        } else {
            stressHSBColour = new float[] { PlayerCharacter.PC_MIN_HUE, PlayerCharacter.PC_MIN_SAT,
                    PlayerCharacter.PC_MIN_LIGHT };
        }

    }

    public void sketchiness() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            float stressRating = stress - stressEffectThreshold;
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
        token.collected = true;
        tokensCollected++;
    }

    public void updateStress() {
        float waveDistance = Math.abs(sketch.risingWave.pos.y - sketch.pc.pos.y);
        if (waveDistance > stressIncrRange) {
            stress -= STRESS_DECR_RATE * (waveDistance - stressIncrRange) / stressDecrRange;
        } else {
            stress += STRESS_DECR_RATE * ((stressIncrRange - waveDistance) / stressIncrRange);
        }

        if (stress > maxStress) {
            stress = maxStress;
        } else if (stress < minStress) {
            stress = minStress;
        }
    }

    public void update() {
        updateStress();
        pcThrust();
        pcFriction();
        pcMinSpeed();
        recalcStressHSBColour();
        sketchiness();
        sketch.risingWave.pos.sub(0, sketch.level.waveRiseRate);
        oldStress = stress;
    }
}
