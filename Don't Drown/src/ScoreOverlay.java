import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class ScoreOverlay {

    private class StressBar extends AbstractDrawable {

        private static final float STRESS_BAR_WIDTH_DIV = 2f;
        private static final float STRESS_BAR_HEIGHT_DIV = 20f;

        private final float stressBarWidth;
        private final float stressBarHeight;
        private final int stressBarOutline;

        protected StressBar(DontDrown sketch, LevelState state) {
            super(sketch, state);
            this.stressBarWidth = sketch.width / STRESS_BAR_WIDTH_DIV;
            this.stressBarHeight = sketch.height / STRESS_BAR_HEIGHT_DIV;
            this.pos = new PVector(sketch.width / 2f - stressBarWidth / 2, stressBarHeight);
            this.stressBarOutline = (int) (stressBarHeight / 10);
        }

        @Override
        protected void generateToken() {
            token = new PShape(PConstants.GROUP);
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            /* outer box */
            sketch.colorMode(PConstants.RGB, 255, 255, 255);
            token.addChild(sketch.handDraw(PConstants.RECT, 0xFF000000, 0xFFFFFFFF, pos.x,
                    pos.y, stressBarWidth,
                    stressBarHeight));

            /* bar fill */
            if (state.stress != 0) {
                sketch.colorMode(PConstants.HSB, 360, 1, 1);
                float[] colour = sketch.levelState.stressHSBColour;
                int fillColour = sketch.color(colour[0], colour[1], colour[2]);
                token.addChild(
                        sketch.handDraw(PConstants.RECT, fillColour, fillColour, pos.x + stressBarOutline,
                                pos.y + stressBarOutline,
                                (stressBarWidth - 2 * stressBarOutline)
                                        * ((float) sketch.levelState.stress / LevelState.ABS_MAX_STRESS),
                                stressBarHeight - 2 * stressBarOutline));
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

    private static final float SCORE_TEXT_DIV = 20f;

    private final StressBar stressBar;
    private final DontDrown sketch;
    private final float textSize;

    public ScoreOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.height / SCORE_TEXT_DIV;
        stressBar = new StressBar(sketch, sketch.levelState);
    }

    public void render() {
        // stress bar
        stressBar.render();

        // token count
        sketch.colorMode(PConstants.RGB, 255, 255, 255);
        sketch.fill(0xFF000000);
        sketch.textAlign(PConstants.RIGHT, PConstants.TOP);
        sketch.textSize(textSize);
        StringBuilder content = new StringBuilder();
        sketch.text(content.toString(), textSize, textSize);
    }

}
