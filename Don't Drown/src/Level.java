import java.util.ArrayList;

import processing.core.PVector;

public class Level {

    public static final float PAN_RATE_DIV = PlayerCharacter.PC_DIAMETER_DIV * 10f;

    private final DontDrown sketch;

    public final String name;
    private final Page page;
    public final float panRate;
    public final int height;
    public final int topLimit; // used to stop over-panning
    public final float lowestPlatformHeight;
    public final float highestPlatformHeight;
    public final float playableWidth; // width - margin

    private final float tokenElevation; // height above platforms
    private final float pToken; // probability of a token spawning per platform

    public enum PanningState {
        UP,
        DOWN,
        NEITHER;
    }

    private float viewportHeight; // the y origin (top) of the viewport relative to the level

    public PanningState panningState = PanningState.NEITHER;

    public int top; // the top of the level relative to the viewport
    public ArrayList<Token> tokens = new ArrayList<>();
    public ArrayList<Platform> platforms = new ArrayList<>();
    public int highScore = 0;
    public Platform highestPlatform;

    public Level(DontDrown sketch, String name, int height, boolean hasGround, float pToken) {
        this.sketch = sketch;
        this.name = name;
        this.height = height;
        page = new Page(sketch, height);
        viewportHeight = sketch.height;
        lowestPlatformHeight = .75f * sketch.height;
        topLimit = sketch.height - height;
        top = topLimit;
        highestPlatformHeight = top + viewportHeight / 10 + sketch.scoreOverlay.endOfPadding;
        playableWidth = sketch.width - Page.marginX;
        panRate = height / PAN_RATE_DIV;
        this.pToken = pToken;
        tokenElevation = 0.75f * sketch.width / PlayerCharacter.PC_DIAMETER_DIV;

        generatePlatformsAndTokens(hasGround, 0.5f);
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
            currentPlatform = new Platform(sketch, Page.marginX, lowestPlatformHeight, playableWidth);
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
                boolean goingLeft = sketch.random(0f, 1f) < (currentPlatform.pos.x - Page.marginX) / playableWidth;
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

            float x = Math.max(Page.marginX, currentPlatform.pos.x + diffX);
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
                nextPlatform.initPos = new PVector(x, y);
                nextPlatform.pos = nextPlatform.initPos.copy();
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

    public void reset() {
        panningState = PanningState.NEITHER;
        viewportHeight = sketch.height;
        top = topLimit;

        if (page.lines != null) {
            page.lines.resetMatrix();
        }
        for (Platform platform : platforms) {
            platform.pos = platform.initPos.copy();
        }
        for (Token token : tokens) {
            token.reset();
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
            if (top - panRate <= page.topLineY) {
                pan((float) topLimit - top);
                panningState = PanningState.NEITHER;
            } else {
                pan(-Math.max(panRate, Math.abs(sketch.pc.vel.y)));
            }
        }
    }

    private void pan(float y) {
        top += y;
        page.lines.translate(0, y);
        for (Platform platform : platforms) {
            platform.pos.y += y;
        }
        for (Token token : tokens) {
            token.pos.y += y;
        }
        sketch.pc.pos.y += y;
        sketch.risingWave.pos.y += y;
    }

    public void render() {
        page.render();

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
