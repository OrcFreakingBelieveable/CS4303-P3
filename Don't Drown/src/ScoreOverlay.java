import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class ScoreOverlay {

    public static class StressBar extends AbstractDrawable {

        public static final int STRESS_BAR_RESOLUTION = 10;
        private static final float STRESS_BAR_WIDTH_DIV = 2f;
        private static final float STRESS_BAR_HEIGHT_DIV = 20f; // as a ratio of width

        private static PShape[][] staticTokens = null;

        private static float width;
        private static float height;

        protected StressBar(DontDrown sketch) {
            super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
            pos = new PVector(0, 0);
        }

        protected static PShape[][] generateTokens(DontDrown sketch) {
            staticTokens = new PShape[(LevelState.ABS_MAX_STRESS + 1) * STRESS_BAR_RESOLUTION][VARIANT_TOKENS];
            width = sketch.width / STRESS_BAR_WIDTH_DIV;
            height = width / STRESS_BAR_HEIGHT_DIV;
            int outlineWeight = (int) (height / 10);
            PVector pos = new PVector(sketch.width / 2f - width / 2, height);

            for (int i = 0; i <= LevelState.ABS_MAX_STRESS * STRESS_BAR_RESOLUTION; i++) {
                sketch.levelState.stress = i / (float) STRESS_BAR_RESOLUTION;
                sketch.levelState.sketchiness();
                sketch.levelState.recalcStressHSBColour();

                for (int j = 0; j < VARIANT_TOKENS; j++) {
                    PShape token = new PShape(PConstants.GROUP);
                    sketch.roughStrokeWeight = outlineWeight;

                    /* outer box */
                    sketch.colorModeRGB();
                    token.addChild(sketch.handDraw(PConstants.RECT, 0xFF000000, 0xFFFFFFFF,
                            pos.x, pos.y, width, height));

                    /* bar fill */
                    if (sketch.levelState.stress > 0) {
                        sketch.colorModeHSB();
                        float[] colour = sketch.levelState.stressHSBColour;
                        int fillColour = sketch.color(colour[0], colour[1], colour[2]);
                        token.addChild(
                                sketch.handDraw(PConstants.RECT, fillColour, fillColour, pos.x + outlineWeight,
                                        pos.y + outlineWeight,
                                        (width - 2 * outlineWeight)
                                                * (sketch.levelState.stress / LevelState.ABS_MAX_STRESS),
                                        height - 2 * outlineWeight));
                    }

                    staticTokens[i][j] = token;
                }
            }

            return staticTokens;
        }

        protected boolean onScreen() {
            return true;
        }

        public void render() {
            renderADStress();
        }
    }

    private static class BigToken extends AbstractDrawable {

        private static PShape[][] staticTokens = null;

        protected BigToken(DontDrown sketch) {
            super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
            pos = new PVector(sketch.width - StressBar.width / 3, 1.5f * StressBar.height);
        }

        protected static PShape[][] generateTokens(DontDrown sketch) {
            staticTokens = new PShape[LevelState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];
            sketch.colorModeRGB();
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            for (int i = 0; i <= LevelState.ABS_MAX_STRESS; i++) {
                sketch.levelState.stress = i;
                sketch.levelState.sketchiness();

                for (int j = 0; j < VARIANT_TOKENS; j++) {
                    staticTokens[i][j] = sketch.handDraw(PConstants.QUAD, Token.T_STROKE_COLOUR, Token.T_FILL_COLOUR,
                            0, -StressBar.height,
                            StressBar.height, 0,
                            0, StressBar.height,
                            -StressBar.height, 0);
                }
            }

            return staticTokens;
        }

        protected boolean onScreen() {
            return true;
        }

        public void render() {
            renderAD();
        }
    }

    private static final float SCORE_TEXT_DIV = 65f;

    public final float endOfPadding;

    private final StressBar stressBar;
    private final BigToken bigToken;
    private final DontDrown sketch;
    private final float textSize;

    public ScoreOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.width / SCORE_TEXT_DIV;
        stressBar = new StressBar(sketch);
        bigToken = new BigToken(sketch);
        this.endOfPadding = StressBar.height * 3;
    }

    public void render() {
        // stress bar
        stressBar.render();

        // token count
        sketch.colorModeRGB();
        bigToken.render();
        sketch.fill(0xFF000000);
        sketch.textAlign(PConstants.CENTER, PConstants.CENTER);
        sketch.textSize(textSize);
        StringBuilder content = new StringBuilder();
        content.append(sketch.levelState.tokensCollected);
        content.append("/");
        content.append(sketch.levelState.tokensAvailable);
        sketch.text(content.toString(), bigToken.pos.x, bigToken.pos.y - textSize / 6);
    }

}
