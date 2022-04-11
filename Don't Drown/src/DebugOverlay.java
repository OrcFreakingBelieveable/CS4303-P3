import processing.core.PConstants;

public class DebugOverlay {

    private final DDSketch sketch;
    private final float textSize;

    private static final float DEBUG_TEXT_DIV = 50f;

    public DebugOverlay(DDSketch sketch) {
        this.sketch = sketch;
        this.textSize = sketch.height / DEBUG_TEXT_DIV;
    }

    public void render() {
        sketch.textAlign(PConstants.LEFT, PConstants.TOP);
        sketch.textSize(textSize);
        StringBuilder content = new StringBuilder();
        content.append("fps: " + Math.round(sketch.frameRate) + "\n");
        content.append("horizontal velocity: " + sketch.pc.vel.x + "\n");
        content.append("vertical velocity: " + sketch.pc.vel.y + "\n");
        content.append("fallState: " + sketch.pc.fallState + "\n");
        content.append("steerState: " + sketch.pc.getSteerState() + "\n");
        content.append("movingHorizontally: " + sketch.pc.movingHorizontally + "\n");

        sketch.text(content.toString(), textSize, textSize);
    }

}
