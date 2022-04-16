import processing.core.PConstants;

public class DebugOverlay {

    private final DontDrown sketch;
    private final float textSize;

    private static final float DEBUG_TEXT_DIV = 50f;

    public DebugOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.height / DEBUG_TEXT_DIV;
    }

    public void render() {
        sketch.fill(0xFF000000);
        sketch.textAlign(PConstants.LEFT, PConstants.TOP);
        sketch.textSize(textSize);
        StringBuilder content = new StringBuilder();
        content.append("fps: " + Math.round(sketch.frameRate) + "\n");
        content.append("pos: " + sketch.pc.pos + "\n");
        content.append("incr: " + sketch.pc.incr + "\n");
        content.append("maxSpeed: " + sketch.pc.maxSpeed + "\n");
        content.append("horizontal velocity: " + sketch.pc.vel.x + "\n");
        content.append("vertical velocity: " + sketch.pc.vel.y + "\n");
        content.append("pcThrust: " + sketch.levelState.pcThrust + "\n");
        content.append("pcFriction: " + sketch.levelState.pcFriction + "\n");
        content.append("fallState: " + sketch.pc.fallState + "\n");
        content.append("steerState: " + sketch.pc.getSteerState() + "\n");
        content.append("moveState: " + sketch.pc.getMoveState() + "\n");
        content.append("stress: " + sketch.levelState.stress + "\n");
        content.append("framesPerResketch: " + sketch.levelState.framesPerResketch + "\n");
        content.append("roughStrokeVariability: " + sketch.roughStrokeVariabilityRate + "\n");
        content.append("roughStrokeShakiness: " + sketch.roughStrokeShakiness + "\n");
        content.append("platforms: " + sketch.level.platforms.size() + "\n");

        sketch.text(content.toString(), textSize, textSize);
    }

}
