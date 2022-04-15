import java.util.ArrayList;

import processing.core.PVector;

public class Level {

    private final DontDrown sketch;
    private final LevelState state;

    public final int height;
    public final int lowestPlatformHeight;
    public final int highestPlatformHeight;
    private final int width;

    private int viewportHeight; // the y origin (top) of the viewport relative to the level

    public Token[] tokens;
    public ArrayList<Platform> platforms = new ArrayList<>();
    public float waveHeight;

    public Level(DontDrown sketch, int height, boolean hasGround) {
        this.sketch = sketch;
        this.state = sketch.levelState;
        this.height = height;
        this.viewportHeight = sketch.height;
        this.lowestPlatformHeight = 9 * viewportHeight / 10;
        this.highestPlatformHeight = viewportHeight / 8;
        this.width = sketch.width;
        waveHeight = height;
        generatePlatformsAndTokens(hasGround, 0.5f);
    }

    private void generatePlatformsAndTokens(boolean hasGround, float goBackPercentage) {
        Platform currentPlatform;
        float jumpRange = sketch.pc.jumpRange;
        float jumpHeight = sketch.pc.jumpHeight;
        float fallGradient = -jumpHeight / (jumpRange / 2); // approximates the curve of the fall of the jump

        if (hasGround) {
            currentPlatform = new Platform(sketch, 0, lowestPlatformHeight, width);
            platforms.add(currentPlatform);
        } else {
            currentPlatform = new Platform(sketch, sketch.random(width), lowestPlatformHeight);
            platforms.add(currentPlatform);
        }

        while (currentPlatform.pos.y > highestPlatformHeight) {
            Platform nextPlatform = new Platform(sketch, 0, 0);
            boolean goingLeft = sketch.random(0, 1) < currentPlatform.pos.x / (width - currentPlatform.width);
            float diffX = goingLeft ? sketch.random(-jumpRange, 0) : sketch.random(0, jumpRange - nextPlatform.width);
            float diffY;

            if (goingLeft) {
                // TODO prevent overlapping platforms
                if (diffX >= -nextPlatform.width) {
                    // nextPlatform horizontally overlaps with currentPlatform
                    // therefore it should be between a half jump and a full jump higher
                    diffY = jumpHeight * sketch.random(0.5f, 1f);
                    // TODO find out why this doesn't work
                } else if (diffX >= -(jumpRange / 2)) {
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
                    float maxDiffY = jumpHeight - fallGradient * (diffX + jumpRange / 2);
                    diffY = sketch.random(0f, 1f) < goBackPercentage
                            ? sketch.random(currentPlatform.pos.y - lowestPlatformHeight, 0)
                            : maxDiffY * sketch.random(0f, 1f);
                }
            } else {
                // TODO review if platform width needs to be reconsidered here
                if (diffX <= currentPlatform.width) {
                    // nextPlatform horizontally overlaps with currentPlatform
                    // therefore it should be between a half jump and a full jump higher
                    diffY = jumpHeight * sketch.random(0.5f, 1f);
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
                    float maxDiffY = jumpHeight + fallGradient * (diffX - jumpRange / 2);
                    diffY = sketch.random(0f, 1f) < goBackPercentage
                            ? sketch.random(currentPlatform.pos.y - lowestPlatformHeight, 0)
                            : maxDiffY * sketch.random(0f, 1f);
                }
            }

            float x = Math.max(0, currentPlatform.pos.x + diffX);
            x = Math.min(x, width - nextPlatform.width);
            float y = Math.min(height - nextPlatform.height, currentPlatform.pos.y - diffY);
            y = Math.max(highestPlatformHeight, y);
            nextPlatform.pos = new PVector(x, y);
            currentPlatform = nextPlatform;
            platforms.add(currentPlatform);
        }
    }

    public void integrate() {

    }

    public void render() {
        int i = 0;
        for (Platform platform : platforms) {
            platform.render();
            sketch.text(i++, platform.pos.x, platform.pos.y);
        }
    }

}
