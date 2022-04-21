import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class ScoreOverlay {

    private class StressBar extends AbstractDrawable {

        private static final float STRESS_BAR_WIDTH_DIV = 2f;
        private static final float STRESS_BAR_HEIGHT_DIV = 20f; // as a ratio of width

        private final float width;
        private final float height;
        private final int outlineWeight;

        protected StressBar(DontDrown sketch) {
            super(sketch);
            this.width = sketch.width / STRESS_BAR_WIDTH_DIV;
            this.height = width / STRESS_BAR_HEIGHT_DIV;
            this.pos = new PVector(sketch.width / 2f - width / 2, height);
            this.outlineWeight = (int) (height / 10);
        }

        @Override
        protected void generateToken() {
            token = new PShape(PConstants.GROUP);
            sketch.roughStrokeWeight = outlineWeight;

            /* outer box */
            sketch.colorModeRGB();
            token.addChild(sketch.handDraw(PConstants.RECT, 0xFF000000, 0xFFFFFFFF, pos.x,
                    pos.y, width,
                    height));

            /* bar fill */
            if (state.stress != 0) {
                sketch.colorModeHSB();
                float[] colour = sketch.levelState.stressHSBColour;
                int fillColour = sketch.color(colour[0], colour[1], colour[2]);
                token.addChild(
                        sketch.handDraw(PConstants.RECT, fillColour, fillColour, pos.x + outlineWeight,
                                pos.y + outlineWeight,
                                (width - 2 * outlineWeight)
                                        * ((float) sketch.levelState.stress / LevelState.ABS_MAX_STRESS),
                                height - 2 * outlineWeight));
            }
        }

        @Override
        public void render() {
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            if (token == null || frameCounter++ % state.framesPerResketch == 0
                    || Math.abs(state.stress - state.oldStress) > 5) {
                generateToken();
            } else if (oldPos.x != pos.x || oldPos.y != pos.y) {
                PVector movement = pos.copy().sub(oldPos);
                token.translate(movement.x, movement.y);
            }

            oldPos = pos.copy();
            sketch.shape(token);
        }

    }

    private class BigToken extends AbstractDrawable {

        protected BigToken(DontDrown sketch) {
            super(sketch);
            pos = new PVector(sketch.width - stressBar.width / 3, 1.5f * stressBar.height);
        }

        @Override
        protected void generateToken() {
            sketch.colorModeRGB();
            token = sketch.handDraw(PConstants.QUAD, Token.T_STROKE_COLOUR, Token.T_FILL_COLOUR,
                    0, -stressBar.height,
                    stressBar.height, 0,
                    0, stressBar.height,
                    -stressBar.height, 0);
            token.translate(pos.x, pos.y);

        }

        @Override
        public void render() {
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            if (token == null || frameCounter++ % state.framesPerResketch == 0
                    || Math.abs(state.stress - state.oldStress) > 5) {
                generateToken();
            } else if (oldPos.x != pos.x || oldPos.y != pos.y) {
                PVector movement = pos.copy().sub(oldPos);
                token.translate(movement.x, movement.y);
            }

            oldPos = pos.copy();
            sketch.shape(token);
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
        this.endOfPadding = stressBar.height * 3;
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
        sketch.text(content.toString(), bigToken.pos.x, bigToken.pos.y - textSize/6);
    }

}
