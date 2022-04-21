import processing.core.PShape;
import processing.core.PVector;

/**
 * N.B. Implementing subclasses must have a method of token generation.
 * Recommended to be static, hence not being an abstract to inherit.
 */
public abstract class AbstractDrawable {

    public static final int VARIANT_TOKENS = 15;

    protected final DontDrown sketch;
    protected final LevelState state;
    protected final int redrawOffset;

    public PVector oldPos; // movement in the last frame
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

    public void render() {
        oldPos = pos.copy();

        if (onScreen()) {
            if (token == null
                    || (sketch.frameCount + redrawOffset) % state.framesPerResketch == 0
            /* || Math.abs(state.stress - state.oldStress) > 5 */) {
                tokenIndex = (tokenIndex + 1) % VARIANT_TOKENS;
                token = tokens[state.stress][tokenIndex];
            }

            sketch.shape(token, pos.x, pos.y);
        }
    }
}
