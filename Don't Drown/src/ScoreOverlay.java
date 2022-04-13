import processing.core.PConstants;
import processing.core.PVector;

public class ScoreOverlay {

    private static final float SCORE_TEXT_DIV = 20f;
    private static final float STRESS_BAR_WIDTH_DIV = 2f;
    private static final float STRESS_BAR_HEIGHT_DIV = 20f;

    private final DontDrown sketch;
    private final float textSize;
    private final float stressBarWidth;
    private final float stressBarHeight;
    private final PVector stressBarOrigin;
    private final int stressBarOutline;

    public ScoreOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.height / SCORE_TEXT_DIV;
        this.stressBarWidth = sketch.width / STRESS_BAR_WIDTH_DIV;
        this.stressBarHeight = sketch.height / STRESS_BAR_HEIGHT_DIV;
        this.stressBarOrigin = new PVector(sketch.width / 2f - stressBarWidth / 2, stressBarHeight);
        this.stressBarOutline = (int) (stressBarHeight / 10);
    }

    public void render() {
        // stress bar
        sketch.colorMode(PConstants.RGB, 255, 255, 255);
        sketch.fill(0xFF000000);
        sketch.rect(stressBarOrigin.x, stressBarOrigin.y, stressBarWidth, stressBarHeight);
        sketch.fill(0xFFFFFFFF);
        sketch.rect(stressBarOrigin.x + stressBarOutline, stressBarOrigin.y + stressBarOutline,
                stressBarWidth - 2 * stressBarOutline, stressBarHeight - 2 * stressBarOutline);
        sketch.colorMode(PConstants.HSB, 360, 1, 1);
        float[] colour = sketch.levelState.stressHSBColour;
        sketch.fill(colour[0], colour[1], colour[2]);
        sketch.rect(stressBarOrigin.x + stressBarOutline,
                stressBarOrigin.y + stressBarOutline,
                (stressBarWidth - 2 * stressBarOutline)
                        * ((float) sketch.levelState.stress / LevelState.ABS_MAX_STRESS),
                stressBarHeight - 2 * stressBarOutline);
        sketch.colorMode(PConstants.RGB, 255, 255, 255);

        // token count
        sketch.fill(0xFF000000);
        sketch.textAlign(PConstants.RIGHT, PConstants.TOP);
        sketch.textSize(textSize);
        StringBuilder content = new StringBuilder();
        sketch.text(content.toString(), textSize, textSize);
    }

}
