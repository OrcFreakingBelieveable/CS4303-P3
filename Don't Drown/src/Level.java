import java.util.ArrayList;

import processing.core.PVector;

public class Level {

    public static final float PAN_RATE_DIV = PlayerCharacter.PC_DIAMETER_DIV * 10f;

    private final DontDrown sketch;

    public final Debuff debuff;
    public final Difficulty difficulty;
    public final Page page;
    public final float panRate;
    public final int height;
    public final float topLimit; // used to stop over-panning
    public final float lowestPlatformHeight;
    public final float highestPlatformHeight;
    public final float playableWidth; // width - margin
    public final int betweenRedHerrings; // minimum layers between red herring platforms
    public final float defaultWaveRiseRate;
    public float waveRiseRate;

    private final float tokenElevation; // height above platforms
    private final float verticality; // a measure of the level's difficulty

    public enum PanningState {
        UP,
        DOWN,
        NEITHER;
    }

    public PanningState panningState = PanningState.NEITHER;

    public float top; // the top of the level relative to the viewport
    public ArrayList<Token> tokens = new ArrayList<>();
    public ArrayList<Platform> platforms = new ArrayList<>();
    public int highScore = 0;
    public Platform highestPlatform;

    public Level(DontDrown sketch, Debuff debuff, Difficulty difficulty) {
        this.sketch = sketch;
        this.debuff = debuff;
        this.difficulty = difficulty;
        this.height = (int) (sketch.height * difficulty.heightMult);
        page = new Page(sketch, height, false);
        lowestPlatformHeight = .75f * sketch.height;
        topLimit = (float) sketch.height - height;
        top = topLimit;
        highestPlatformHeight = page.topLineY + sketch.height / 10f;
        playableWidth = sketch.width - Page.marginX;
        panRate = height / PAN_RATE_DIV;
        this.verticality = difficulty.verticality;
        this.betweenRedHerrings = difficulty.betweenRedHerrings;
        tokenElevation = 0.75f * sketch.width / PlayerCharacter.PC_DIAMETER_DIV;
        defaultWaveRiseRate = sketch.height / (60f * difficulty.waveRiseTime);
        waveRiseRate = defaultWaveRiseRate;

        generatePlatformsAndTokens(difficulty.hasGround);
    }

    private void addToken(float x, float y) {
        tokens.add(new Token(sketch, x, y));
    }

    private PVector placePlatform(Platform currentPlatform, Platform nextPlatform, float diffX, float diffY) {
        float x = Math.max(Page.marginX, currentPlatform.pos.x + diffX);
        x = Math.min(x, sketch.width - nextPlatform.width);
        float y = Math.min(height - nextPlatform.height, currentPlatform.pos.y - diffY);
        y = Math.max(highestPlatformHeight, y);
        return new PVector(x, y);
    }

    private PVector placePlatform(Platform toPlace, float proposedX, float proposedY) {
        float x = Math.max(Page.marginX, proposedX);
        x = Math.min(x, sketch.width - toPlace.width);
        float y = Math.min(height - toPlace.height, proposedY);
        y = Math.max(highestPlatformHeight, y);
        return new PVector(x, y);
    }

    private void generatePlatformsAndTokens(boolean hasGround) {
        Platform prevPlatform = null;
        Platform currentPlatform;
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

        if (hasGround) {
            currentPlatform = new Platform(sketch, Page.marginX, lowestPlatformHeight, playableWidth);
            platforms.add(currentPlatform);
        } else {
            currentPlatform = new Platform(sketch,
                    Page.marginX + sketch.random(playableWidth - sketch.width / Platform.PF_WIDTH_DIV),
                    lowestPlatformHeight);
            platforms.add(currentPlatform);
        }
        highestPlatform = currentPlatform;

        float diffX = 0, diffY = 0;
        boolean goingLeft = false;
        boolean redHerring = false; // whether or not to add an extra platform with a token
        int sinceRedHerring = betweenRedHerrings;

        while (currentPlatform.pos.y > highestPlatformHeight + (jumpHeight * vMinJumpHeightMult)) {

            Platform nextPlatform = new Platform(sketch, 0, 0);

            if (redHerring && prevPlatform != null) {
                // place a token on a platform off the beaten path
                Platform redHerringP = new Platform(sketch, 0, 0);
                redHerringP.initPos = placePlatform(redHerringP,
                        prevPlatform.pos.x - diffX,
                        currentPlatform.pos.y);
                redHerringP.pos = redHerringP.initPos.copy();
                addToken(redHerringP.pos.x + redHerringP.width / 2, redHerringP.pos.y - tokenElevation);
                platforms.add(redHerringP);
                sinceRedHerring = 0;
            } else {
                sinceRedHerring++;
            }

            if (currentPlatform.width == playableWidth) {
                // first platform after the ground is a special case
                diffY = jumpHeight * sketch.random(vMinJumpHeightMult, vMaxJumpHeightMult);
                diffX = sketch.random(0, playableWidth - nextPlatform.width);
            } else {
                boolean wentUp = diffY >= jumpHeight * vMinJumpHeightMult;
                boolean edgeReached = currentPlatform.pos.x < Page.marginX + nextPlatform.width
                        || currentPlatform.pos.x > sketch.width - currentPlatform.width - nextPlatform.width;

                if (edgeReached) {
                    redHerring = false;

                    // turn around
                    goingLeft = !goingLeft;

                    // reflection jump
                    diffY = jumpHeight * sketch.random(vMinJumpHeightMult, vMaxJumpHeightMult);
                    diffX = Math.max(currentPlatform.width,
                            jumpRange * sketch.random(hMinJumpRangeMult, hMaxJumpRangeMult));
                } else {
                    // random chance to change horizontal direction
                    if (!redHerring && sketch.random(0f, 1f) < 0.1) {
                        goingLeft = !goingLeft;
                        redHerring = false;
                    } else {
                        redHerring = /* !redHerring && */sinceRedHerring >= betweenRedHerrings && wentUp;
                    }

                    if (!wentUp && sketch.random(0f, 1f) < verticality) {
                        // vertical jump (can't have two in a row)
                        diffY = jumpHeight * sketch.random(vMinJumpHeightMult, vMaxJumpHeightMult);
                        diffX = jumpRange * sketch.random(vMinJumpRangeMult, vMaxJumpRangeMult);
                    } else {
                        // horizontal jump
                        diffY = jumpHeight * sketch.random(hMinJumpHeightMult, hMaxJumpHeightMult);
                        diffX = Math.max(currentPlatform.width,
                                jumpRange * sketch.random(hMinJumpRangeMult, hMaxJumpRangeMult));
                    }

                }

                if (goingLeft) {
                    diffX = -(diffX);
                }
            }

            nextPlatform.initPos = placePlatform(currentPlatform, nextPlatform, diffX, diffY);
            nextPlatform.pos = nextPlatform.initPos.copy();
            prevPlatform = currentPlatform;
            currentPlatform = nextPlatform;
            platforms.add(currentPlatform);

            if (currentPlatform.pos.y < highestPlatform.pos.y)
                highestPlatform = currentPlatform;

        }
    }

    public void reset() {
        panningState = PanningState.NEITHER;
        top = topLimit;
        waveRiseRate = defaultWaveRiseRate; 

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
