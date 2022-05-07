import processing.core.PConstants;

public class DebugOverlay {

    private final DontDrown sketch;
    private final float textSize;

    private static final float DEBUG_TEXT_DIV = 100f;

    public DebugOverlay(DontDrown sketch) {
        this.sketch = sketch;
        this.textSize = sketch.width / DEBUG_TEXT_DIV;
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
        content.append("jumpHeight: " + sketch.pc.jumpHeight + "\n");
        content.append("jumpRange: " + sketch.pc.jumpRange + "\n");
        content.append("horizontal velocity: " + sketch.pc.vel.x + "\n");
        content.append("vertical velocity: " + sketch.pc.vel.y + "\n");
        content.append("stressRating: " + (sketch.levelState.stress - sketch.levelState.stressEffectThreshold) + "\n");
        content.append("pcThrustMultiplier: " + sketch.levelState.pcThrustMultiplier + "\n");
        content.append("pcThrust: " + sketch.levelState.pcThrust + "\n");
        content.append("pcFriction: " + sketch.levelState.pcFriction + "\n");
        content.append("pcResultant: " + sketch.pc.resultant + "\n");
        content.append("fallState: " + sketch.pc.fallState + "\n");
        content.append("steerState: " + sketch.pc.getSteerState() + "\n");
        content.append("moveState: " + sketch.pc.getMoveState() + "\n");
        content.append("stress: " + sketch.levelState.stress + "\n");
        content.append("framesPerResketch: " + sketch.levelState.framesPerResketch + "\n");
        content.append("roughStrokeVariability: " + sketch.roughStrokeVariabilityRate + "\n");
        content.append("roughStrokeShakiness: " + sketch.roughStrokeShakiness + "\n");
        content.append("waveDistance: " + Math.abs(sketch.risingWave.pos.y - sketch.pc.pos.y) + "\n");
        content.append("stressIncrRange: " + sketch.levelState.stressIncrRange + "\n");

        if (sketch.gameState.equals(DontDrown.GameState.MID_LEVEL)) {
            content.append("debuff: " + sketch.levelState.debuff + "\n");
            content.append("difficulty: " + sketch.level.difficulty + "\n");
            content.append("platforms: " + sketch.level.platforms.size() + "\n");
            content.append("tokens: " + sketch.level.tokens.size() + "\n");
            content.append("panningState: " + sketch.level.panningState + "\n");
            content.append("page.height: " + sketch.level.page.height + "\n");
            content.append("page.topLineY: " + sketch.level.page.topLineY + "\n");
            content.append("height: " + sketch.level.height + "\n");
            content.append("top: " + sketch.level.top + "\n");
            content.append("topLimit: " + sketch.level.topLimit + "\n");
            content.append("highestPlatformHeight: " + sketch.level.highestPlatformHeight + "\n");
            content.append("highestPlatform.y: " + sketch.level.highestPlatform.pos.y + "\n");
            
        }
        
        sketch.text(content.toString(), textSize, textSize);
    }

}
