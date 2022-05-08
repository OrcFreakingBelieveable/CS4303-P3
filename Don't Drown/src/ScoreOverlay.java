import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PShape;
import processing.core.PVector;

public class ScoreOverlay {

    public static class StressBar {

        public static final int STRESS_BAR_RESOLUTION = 10;
        private static final float STRESS_BAR_WIDTH_DIV = 2f;
        private static final float STRESS_BAR_HEIGHT_DIV = 20f; // as a ratio of width

        private static float width;
        private static float height;

        public final StressBarOuter outer;
        public final StressBarFill fill;

        public StressBar(DontDrown sketch) {
            outer = new StressBarOuter(sketch);
            fill = new StressBarFill(sketch);
        }

        public static class StressBarOuter extends AbstractDrawable {
            private static PShape[][] staticTokens = null;

            protected StressBarOuter(DontDrown sketch) {
                super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
                pos = new PVector(0, 0);
            }

            protected static PShape[][] generateTokens(DontDrown sketch) {
                staticTokens = new PShape[(StressAndTokenState.ABS_MAX_STRESS + 1)][VARIANT_TOKENS];

                width = sketch.width / STRESS_BAR_WIDTH_DIV;
                height = width / STRESS_BAR_HEIGHT_DIV;
                PVector pos = new PVector(sketch.width / 2f - width / 2, height);

                for (int i = 0; i <= StressAndTokenState.ABS_MAX_STRESS; i++) {
                    sketch.levelState.stress = i / (float) STRESS_BAR_RESOLUTION;
                    sketch.levelState.sketchiness();
                    sketch.levelState.recalcStressHSBColour();

                    for (int j = 0; j < VARIANT_TOKENS; j++) {
                        PShape token = new PShape(PConstants.GROUP);

                        /* outer box */
                        sketch.colorModeRGB();
                        token.addChild(sketch.handDraw(PConstants.RECT, 0xFF000000, 0xFFFFFFFF,
                                pos.x, pos.y, width, height));

                        staticTokens[i][j] = token;
                    }
                }

                return staticTokens;
            }

            @Override
            protected boolean onScreen() {
                return true;
            }

            @Override
            public void render() {
                renderAD();
            }

        }

        public static class StressBarFill extends AbstractDrawable {
            private static PShape[][] staticTokens = null;

            protected StressBarFill(DontDrown sketch) {
                super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
                pos = new PVector(0, 0);
            }

            protected static PShape[][] generateTokens(DontDrown sketch) {
                final int maxStressIndex = (StressAndTokenState.ABS_MAX_STRESS + 1) * STRESS_BAR_RESOLUTION;
                staticTokens = new PShape[maxStressIndex][VARIANT_TOKENS];

                width = sketch.width / STRESS_BAR_WIDTH_DIV;
                height = width / STRESS_BAR_HEIGHT_DIV;
                int outlineWeight = (int) (height / 10);
                PVector pos = new PVector(sketch.width / 2f - width / 2, height);

                for (int i = 0; i < maxStressIndex; i++) {
                    sketch.levelState.stress = i / (float) STRESS_BAR_RESOLUTION;
                    sketch.levelState.sketchiness();
                    sketch.levelState.recalcStressHSBColour();

                    for (int j = 0; j < VARIANT_TOKENS; j++) {
                        PShape token = new PShape(PConstants.GROUP);
                        sketch.roughStrokeWeight = outlineWeight;

                        /* bar fill */
                        if (sketch.levelState.stress > 0) {
                            sketch.colorModeHSB();
                            float[] colour = sketch.levelState.stressHSBColour;
                            int fillColour = sketch.color(colour[0], colour[1], colour[2]);
                            token.addChild(
                                    sketch.handDraw(PConstants.RECT, fillColour, fillColour, pos.x + outlineWeight,
                                            pos.y + outlineWeight,
                                            (width - 2 * outlineWeight)
                                                    * (sketch.levelState.stress / StressAndTokenState.ABS_MAX_STRESS),
                                            height - 2 * outlineWeight));
                        }

                        staticTokens[i][j] = token;
                    }
                }

                return staticTokens;
            }

            @Override
            protected boolean onScreen() {
                return true;
            }

            @Override
            public void render() {
                renderADStress();
            }
        }

        public void render() {
            outer.render();
            fill.render();
        }
    }

    private static class BigToken extends AbstractDrawable {

        private static PShape[][] staticTokens = null;

        protected BigToken(DontDrown sketch) {
            super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
            pos = new PVector(sketch.width - StressBar.width / 3, 1.5f * StressBar.height);
        }

        protected static PShape[][] generateTokens(DontDrown sketch) {
            staticTokens = new PShape[StressAndTokenState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];
            sketch.colorModeRGB();
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            for (int i = 0; i <= StressAndTokenState.ABS_MAX_STRESS; i++) {
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
    private final PFont scoreFont;

    public ScoreOverlay(DontDrown sketch) {
        this.sketch = sketch;
        scoreFont = sketch.createFont(DontDrown.FONT_PATH, sketch.width / SCORE_TEXT_DIV);
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
        sketch.textFont(scoreFont);
        StringBuilder content = new StringBuilder();
        content.append(sketch.levelState.tokensCollected);
        content.append("/");
        content.append(sketch.levelState.tokensAvailable);
        sketch.text(content.toString(), bigToken.pos.x, bigToken.pos.y);
    }

}
