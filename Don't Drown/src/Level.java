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
    private final float verticality; // a measure of the level's difficulty

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

    public Level(DontDrown sketch, String name, int height, boolean hasGround, float pToken, float verticality) {
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
        this.verticality = verticality;
        tokenElevation = 0.75f * sketch.width / PlayerCharacter.PC_DIAMETER_DIV;

        generatePlatformsAndTokens(hasGround);
    }

    private void addToken(float x, float y) {
        tokens.add(new Token(sketch, x, y));
    }

    private void generatePlatformsAndTokens(boolean hasGround) {
        Platform currentPlatform;
        int currentPlatformIndex = -1;
        final float jumpRange = sketch.pc.jumpRange;
        final float hMinJumpRangeMult = 0.5f;
        final float hMaxJumpRangeMult = 1f;
        final float vMinJumpRangeMult = 0f;
        final float vMaxJumpRangeMult = 0.25f;
        final float jumpHeight = sketch.pc.jumpHeight;
        final float hMinJumpHeightMult = 0.25f;
        final float hMaxJumpHeightMult = 0.5f;
        final float vMinJumpHeightMult = 0.75f;
        final float vMaxJumpHeightMult = 1f;
        int stuckCount = 0;

        if (hasGround) {
            currentPlatform = new Platform(sketch, Page.marginX, lowestPlatformHeight, playableWidth);
            platforms.add(currentPlatform);
        } else {
            currentPlatform = new Platform(sketch, sketch.random(playableWidth), lowestPlatformHeight);
            platforms.add(currentPlatform);
        }
        currentPlatformIndex++; // = 0

        highestPlatform = currentPlatform;

        float diffX = 0, diffY = 0;
        boolean goingUp, goingLeft = false;
        boolean canGoUp, canGoLeft, canGoRight;

        while (currentPlatform.pos.y > highestPlatformHeight) {

            if (stuckCount == 3) {
                if (currentPlatformIndex > 0) {
                    currentPlatform = platforms.get(--currentPlatformIndex);
                    stuckCount = 0;
                } else {
                    break;
                }
            }

            Platform nextPlatform = new Platform(sketch, 0, 0);
            boolean wentUp = diffY >= jumpHeight * vMinJumpHeightMult;
            canGoUp = !wentUp // don't jump upwards twice
                    && currentPlatform.pos.y < highestPlatformHeight - jumpHeight * vMinJumpHeightMult;
            canGoLeft = (wentUp || diffX <= 0 || currentPlatform.pos.x > sketch.width - currentPlatform.width)
                    && currentPlatform.pos.x > Page.marginX + nextPlatform.width;
            canGoRight = (wentUp || diffX >= 0 || currentPlatform.pos.x < Page.marginX + nextPlatform.width)
                    && currentPlatform.pos.x < sketch.width - currentPlatform.width - nextPlatform.width;

            goingLeft = !canGoRight || canGoLeft
                    && sketch.random(0f, 1f) < (currentPlatform.pos.x - Page.marginX) / playableWidth; // random chance

            if (currentPlatform.width == playableWidth) {
                diffY = jumpHeight * sketch.random(vMinJumpHeightMult, vMaxJumpHeightMult);
                diffX = sketch.random(0, playableWidth - nextPlatform.width);
            } else {
                goingUp = canGoUp && sketch.random(0f, 1f) < verticality;

                if (goingUp) {
                    diffY = jumpHeight * sketch.random(vMinJumpHeightMult, vMaxJumpHeightMult);
                    diffX = jumpRange * sketch.random(vMinJumpRangeMult, vMaxJumpRangeMult);
                } else {
                    diffY = jumpHeight * sketch.random(hMinJumpHeightMult, hMaxJumpHeightMult);
                    diffX = Math.max(currentPlatform.width,
                            jumpRange * sketch.random(hMinJumpRangeMult, hMaxJumpRangeMult));
                }

                if (goingLeft) {
                    diffX = -(diffX);
                }
            }

            float x = Math.max(Page.marginX, currentPlatform.pos.x + diffX);
            x = Math.min(x, sketch.width - nextPlatform.width);
            float y = Math.min(height - nextPlatform.height, currentPlatform.pos.y - diffY);
            y = Math.max(highestPlatformHeight, y);

            boolean overlapping = false;

            /*
             * for (Platform platform : platforms) {
             * if (Math.abs(x - platform.pos.x) < 2 * Math.max(nextPlatform.width,
             * platform.width)
             * && Math.abs(y - platform.pos.y) < jumpHeight / 2) {
             * overlapping = true;
             * }
             * }
             */

            if (!overlapping) {
                nextPlatform.initPos = new PVector(x, y);
                nextPlatform.pos = nextPlatform.initPos.copy();
                currentPlatform = nextPlatform;
                platforms.add(currentPlatform);
                currentPlatformIndex = platforms.size() - 1;
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
