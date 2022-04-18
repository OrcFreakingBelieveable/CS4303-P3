import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class ScoreOverlay {

    private class StressBar extends AbstractDrawable {

        private static final float STRESS_BAR_WIDTH_DIV = 2f;
        private static final float STRESS_BAR_HEIGHT_DIV = 20f;

        private final float width;
        private final float height;
        private final int outlineWeight;

        protected StressBar(DontDrown sketch) {
            super(sketch);
            this.width = sketch.width / STRESS_BAR_WIDTH_DIV;
            this.height = sketch.height / STRESS_BAR_HEIGHT_DIV;
            this.pos = new PVector(sketch.width / 2f - width / 2, height);
            this.outlineWeight = (int) (height / 10);
        }

        @Override
        protected void generateToken() {
            token = new PShape(PConstants.GROUP);
            sketch.roughStrokeWeight = sketch.RSW_DEF;

            /* outer box */
            sketch.colorMode(PConstants.RGB, 255, 255, 255);
            token.addChild(sketch.handDraw(PConstants.RECT, 0xFF000000, 0xFFFFFFFF, pos.x,
                    pos.y, width,
                    height));

            /* bar fill */
            if (state.stress != 0) {
                sketch.colorMode(PConstants.HSB, 360, 1, 1);
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

    private static final float SCORE_TEXT_DIV = 20f;

    public final float height; 

    private final StressBar stressBar;
    private final DontDrown sketch;
    private final float textSize;

    public ScoreOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.height / SCORE_TEXT_DIV;
        stressBar = new StressBar(sketch);
        this.height = stressBar.height * 3; 
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
