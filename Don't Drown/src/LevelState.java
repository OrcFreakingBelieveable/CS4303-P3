public class LevelState {

    private final DontDrown sketch;

    public static final int ABS_MAX_STRESS = 100;

    public enum Debuff {
        STRESS_MOTIVATED,
        TUNNEL_VISION,
        PANIC_PRONE,
        LACK_CONTRAST,
        NONE;
    }

    public int tokensCollected = 0; 
    public int stress = 0;
    public int maxStress = 100;
    public int minStress = 0;
    public int stressEffectThreshold = 20;
    public float saturation = 1f; // 0 <= s <= 1
    public Debuff debuff = Debuff.NONE;
    public boolean debugging = false;

    private float pcThrustDenominator = (float) ABS_MAX_STRESS - stressEffectThreshold;
    private float pcFrictionDenominator = pcThrustDenominator * 2f;
    private float pcHueMultiplier = (PlayerCharacter.PC_MAX_HUE - PlayerCharacter.PC_MIN_HUE)
            / (ABS_MAX_STRESS - stressEffectThreshold);
    private float pcSatMultiplier = (PlayerCharacter.PC_MAX_SAT - PlayerCharacter.PC_MIN_SAT)
            / (ABS_MAX_STRESS - stressEffectThreshold);
    private float pcLightMultiplier = (PlayerCharacter.PC_MAX_LIGHT - PlayerCharacter.PC_MIN_LIGHT)
            / (ABS_MAX_STRESS - stressEffectThreshold);

    public LevelState(DontDrown sketch) {
        this.sketch = sketch;
    }

    public float pcThrust() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            return sketch.pc.horizontalThrustDef * (1 + (stress - stressEffectThreshold) / pcThrustDenominator);
        } else {
            return sketch.pc.horizontalThrustDef;
        }
    }

    public float pcFriction() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            return sketch.pc.horizontalFrictionDef * (1 - (stress - stressEffectThreshold) / pcFrictionDenominator);
        } else {
            return sketch.pc.horizontalFrictionDef;
        }
    }

    public float[] pcHSBColour() {
        if (debuff.equals(Debuff.STRESS_MOTIVATED) || stress >= stressEffectThreshold) {
            float[] hsb = new float[3];
            hsb[0] = PlayerCharacter.PC_MIN_HUE + (stress - stressEffectThreshold) * pcHueMultiplier;
            hsb[1] = PlayerCharacter.PC_MIN_SAT + (stress - stressEffectThreshold) * pcSatMultiplier;
            hsb[2] = PlayerCharacter.PC_MIN_LIGHT + (stress - stressEffectThreshold) * pcLightMultiplier;
            return hsb;
        } else {
            return new float[] { PlayerCharacter.PC_MIN_HUE, PlayerCharacter.PC_MIN_SAT, PlayerCharacter.PC_MIN_LIGHT };
        }

    }
}
