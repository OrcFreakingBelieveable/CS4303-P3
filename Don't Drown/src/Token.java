import processing.core.PConstants;
import processing.core.PShape;

/**
 * The collectable token objects. Not to be confused with the sprites/shapes of
 * drawable objects.
 */
public class Token extends AbstractCollectable {

    public static final int T_STROKE_COLOUR = 0xDDB09500;
    public static final int T_FILL_COLOUR = 0xAAE3C800;

    private static final float T_HEIGHT_DIV = 40f;
    private static final int T_BOUNCE_FRAMES = 30;

    private static PShape[][] staticTokens = null;
    private static float bounceIncr;
    public static float height;
    public static float width;

    private boolean movingDown = false;

    public Token(DontDrown sketch, float x, float y) {
        super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens), x, y);
        pos.y -= bounceIncr * (sketch.frameCount % T_BOUNCE_FRAMES);
    }

    public void reset() {
        resetAC();
        pos.y -= bounceIncr * (sketch.frameCount % T_BOUNCE_FRAMES);
    }

    /* Bobs up and down */
    public void integrate() {
        if (collected) {
            return;
        }
        if (sketch.frameCount % T_BOUNCE_FRAMES == 0) {
            movingDown = !movingDown;
        }

        if (movingDown) {
            pos.y += bounceIncr;
        } else {
            pos.y -= bounceIncr;
        }
    }

    protected static PShape[][] generateTokens(DontDrown sketch) {
        staticTokens = new PShape[StressAndTokenState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];

        height = sketch.width / T_HEIGHT_DIV;
        width = height + sketch.random(-sketch.RSW_DEF, sketch.RSW_DEF);
        float bounceHeight = height / 4;
        bounceIncr = (bounceHeight / T_BOUNCE_FRAMES);

        sketch.colorModeRGB();
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        for (int i = 0; i <= StressAndTokenState.ABS_MAX_STRESS; i++) {
            sketch.levelState.stress = i;
            sketch.levelState.sketchiness();

            for (int j = 0; j < VARIANT_TOKENS; j++) {
                staticTokens[i][j] = sketch.handDraw(PConstants.QUAD, T_STROKE_COLOUR, T_FILL_COLOUR,
                        0, -height / 2,
                        width / 2, 0,
                        0, height / 2,
                        -width / 2, 0);
            }
        }

        return staticTokens;
    }

    protected boolean onScreen() {
        return pos.y - height <= sketch.height && pos.y + height >= height;
    }

}
