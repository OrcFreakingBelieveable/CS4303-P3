import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Level {

    private static final int LINE_COLOUR = 0xFF666666;
    private static final int MARGIN_COLOUR = 0x88B85450;
    public static final int MARGIN_DIV = 10;
    public static final float PAN_RATE_DIV = PlayerCharacter.PC_DIAMETER_DIV * 10f;

    private final DontDrown sketch;

    public final float panRate;
    public final int height;
    public final int topLimit;
    public final float lowestPlatformHeight;
    public final float highestPlatformHeight;
    public final float playableWidth;
    public final float marginX;

    private final float tokenElevation; // height above platforms
    private final float pToken;

    public enum PanningState {
        UP,
        DOWN,
        NEITHER;
    }

    private float viewportHeight; // the y origin (top) of the viewport relative to the level

    public PanningState panningState = PanningState.NEITHER;

    public float top;
    public PShape lines = null;
    public ArrayList<Token> tokens = new ArrayList<>();
    public ArrayList<Platform> platforms = new ArrayList<>();
    public Platform highestPlatform;
    public float waveHeight;

    public Level(DontDrown sketch, int height, boolean hasGround, float pToken) {
        this.sketch = sketch;
        this.height = height;
        this.viewportHeight = sketch.height;
        this.lowestPlatformHeight = 9 * viewportHeight / 10;
        topLimit = sketch.height - height;
        top = topLimit;
        this.highestPlatformHeight = topLimit + viewportHeight / 10 + sketch.scoreOverlay.endOfPadding;
        this.marginX = (float) sketch.width / MARGIN_DIV;
        this.playableWidth = sketch.width - marginX;
        waveHeight = height;
        panRate = height / PAN_RATE_DIV;
        this.pToken = pToken;
        tokenElevation = 0.75f * sketch.width / PlayerCharacter.PC_DIAMETER_DIV;

        generatePlatformsAndTokens(hasGround, 0.5f);
    }

    private PShape drawLine(PVector start, PVector end, float weight, int colour) {
        PVector smoothLine = start.copy().sub(end);
        float angle = smoothLine.heading();
        angle += PConstants.HALF_PI;
        PVector padding = PVector.fromAngle(angle).mult(weight / 2);
        PVector topLeft = start.copy().sub(padding);
        PVector topRight = end.copy().sub(padding);
        PVector bottomRight = topRight.copy().add(padding);
        PVector bottomLeft = topLeft.copy().add(padding);

        PShape line = sketch.createShape(PConstants.QUAD,
                topLeft.x, topLeft.y, topRight.x, topRight.y,
                bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y);

        line.setFill(colour);
        return line;
    }

    private void generateLines() {
        lines = sketch.createShape(PConstants.GROUP);
        final float lineGap = sketch.width / PlayerCharacter.PC_DIAMETER_DIV;
        for (int i = 1; i <= height / lineGap; i++) {
            lines.addChild(
                    drawLine(new PVector(0, topLimit + i * lineGap), new PVector(sketch.width, topLimit + i * lineGap),
                            1, LINE_COLOUR));
        }
        lines.addChild(drawLine(new PVector(marginX, topLimit), new PVector(marginX, height), 1, MARGIN_COLOUR));
    }

    private void addToken(float x, float y) {
        tokens.add(new Token(sketch, x, y));
    }

    private void generatePlatformsAndTokens(boolean hasGround, float goBackPercentage) {
        Platform currentPlatform;
        float jumpRange = sketch.pc.jumpRange;
        float jumpHeight = sketch.pc.jumpHeight;
        float fallGradient = -jumpHeight / (jumpRange / 2); // approximates the curve of the fall of the jump
        int stuckCount = 0;

        if (hasGround) {
            currentPlatform = new Platform(sketch, marginX, lowestPlatformHeight, playableWidth);
            platforms.add(currentPlatform);
        } else {
            currentPlatform = new Platform(sketch, sketch.random(playableWidth), lowestPlatformHeight);
            platforms.add(currentPlatform);
        }

        highestPlatform = currentPlatform;

        while (currentPlatform.pos.y > highestPlatformHeight && stuckCount < 3) {
            Platform nextPlatform = new Platform(sketch, 0, 0);
            float diffX;
            float diffY;

            if (currentPlatform.width == playableWidth) {
                diffX = sketch.random(0, playableWidth - nextPlatform.width);
            } else {
                boolean goingLeft = sketch.random(0f, 1f) < (currentPlatform.pos.x - marginX) / playableWidth;
                diffX = goingLeft ? sketch.random(-jumpRange, 0) : sketch.random(0, jumpRange - nextPlatform.width);
            }

            if (Math.abs(diffX) < 2 * Math.max(nextPlatform.width, currentPlatform.width)) {
                // nextPlatform horizontally overlaps with currentPlatform
                // therefore it should be between a half jump and a full jump higher
                diffY = jumpHeight * sketch.random(0.75f, 1f);
            } else if (diffX <= (jumpRange / 2)) {
                // nextPlatform is within jump peak distance
                // therefore it can be up to jump height above the current platform, and
                // ... anywhere below it
                diffY = sketch.random(0f, 1f) < goBackPercentage
                        ? sketch.random(currentPlatform.pos.y - lowestPlatformHeight, 0)
                        : jumpHeight * sketch.random(0f, 1f);
            } else {
                // nextPlatform is beyond peak jump distance
                // therefore it is either below the currentPlatform, or at a height dependent
                // .. upon horizontal displacement
                float maxDiffY = jumpHeight + fallGradient * (Math.abs(diffX) + jumpRange / 2);
                diffY = sketch.random(0f, 1f) < goBackPercentage
                        ? sketch.random(currentPlatform.pos.y - lowestPlatformHeight, 0)
                        : maxDiffY * sketch.random(0f, 1f);
            }

            float x = Math.max(marginX, currentPlatform.pos.x + diffX);
            x = Math.min(x, sketch.width - nextPlatform.width);
            float y = Math.min(height - nextPlatform.height, currentPlatform.pos.y - diffY);
            y = Math.max(highestPlatformHeight, y);

            boolean overlapping = false;

            for (Platform platform : platforms) {
                if (Math.abs(x - platform.pos.x) < 2 * Math.max(nextPlatform.width, platform.width)
                        && Math.abs(y - platform.pos.y) < jumpHeight / 2) {
                    overlapping = true;
                }
            }

            if (!overlapping) {
                nextPlatform.pos = new PVector(x, y);
                currentPlatform = nextPlatform;
                platforms.add(currentPlatform);
                stuckCount = 0;
                if (sketch.random(0, 1) < pToken) {
                    addToken(x + currentPlatform.width / 2, y - tokenElevation);
                }
                if (currentPlatform.pos.y < highestPlatform.pos.y)
                    highestPlatform = currentPlatform;
            } else {
                stuckCount++;
            }
        }
    }

    public void integrate() {
        for (Token token : tokens) {
            token.integrate();
        }

        if (panningState.equals(PanningState.UP)) {
            if (top + panRate >= 0f) {
                pan(0f - top);
                panningState = PanningState.NEITHER;
            } else {
                pan(Math.max(panRate, Math.abs(sketch.pc.vel.y)));
            }
        } else if (panningState.equals(PanningState.DOWN)) {
            if (top - panRate <= topLimit) {
                pan(topLimit - top);
                panningState = PanningState.NEITHER;
            } else {
                pan(-Math.max(panRate, Math.abs(sketch.pc.vel.y)));
            }
        }
    }

    private void pan(float y) {
        top += y;
        lines.translate(0, y);
        for (Platform platform : platforms) {
            platform.pan(y);
        }
        for (Token token : tokens) {
            token.pan(y);
        }
        sketch.pc.pan(y);
    }

    public void render() {
        if (lines == null) {
            generateLines();
        }

        sketch.colorModeRGB();
        sketch.background(0xFFFFFFEE);
        sketch.shape(lines);

        int i = 0;
        for (Platform platform : platforms) {
            platform.render();
            if (sketch.debugging)
                sketch.text(i++, platform.pos.x, platform.pos.y);
        }

        for (Token token : tokens) {
            token.render();
        }
    }

}
