import processing.core.PShape;
import processing.core.PVector;

/**
 * N.B. Implementing subclasses must have a method of token generation.
 * Recommended to be static, hence not being an abstract to inherit.
 */
public abstract class AbstractDrawable {

    public static final int VARIANT_TOKENS = 15;
    public static final int FRAMES_PER_STRESS_BAR_RESKETCH = 2;

    protected final DontDrown sketch;
    protected final LevelState state;
    protected final int redrawOffset;

    public PVector pos; // position

    private final PShape[][] tokens;
    private PShape token;
    private int tokenIndex = 0;

    protected abstract boolean onScreen();

    protected AbstractDrawable(DontDrown sketch, PShape[][] tokens) {
        this.sketch = sketch;
        state = sketch.levelState;
        redrawOffset = (int) Math.floor(sketch.random(LevelState.FRAMES_PER_RESKETCH_MIN));
        this.tokens = tokens;
    }

    protected void renderAD() {
        if (onScreen()) {
            if (token == null
                    || (sketch.frameCount + redrawOffset) % state.framesPerResketch == 0) {
                tokenIndex = (tokenIndex + 1) % VARIANT_TOKENS;
                token = tokens[(int) state.stress][tokenIndex];
            }

            sketch.shape(token, pos.x, pos.y);
        }
    }

    protected void renderADStress() {
        if (onScreen()) {
            if (token == null
                    || (sketch.frameCount + redrawOffset) % FRAMES_PER_STRESS_BAR_RESKETCH == 0) {
                tokenIndex = (tokenIndex + 1) % VARIANT_TOKENS;
                int stressIndex = (int) Math.max(0, (state.stress * ScoreOverlay.StressBar.STRESS_BAR_RESOLUTION));
                token = tokens[stressIndex][tokenIndex];
            }

            sketch.shape(token, pos.x, pos.y);
        }
    }

    public abstract void render();
}
