import java.io.File;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;

public class DontDrown extends PApplet {

    PImage[] pcTokens = new PImage[6];

    LevelState levelState;
    PlayerCharacter pc;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Platform ground;
    ArrayList<Platform> platforms = new ArrayList<>();
    CollisionDetector collisionDetector;

    private void loadPCTokens() {
        float v = 1.0f / 9.0f;

        for (int i = 0; i < pcTokens.length; i++) {
            String tokenName = "tokens" + File.separator + "pc-" + nf(i, 2) + ".png";
            PImage token = loadImage(tokenName);
            token.resize(0, (int) (pc.diameter * 1.1));
            token.loadPixels();
            PImage blurredToken = createImage(token.width, token.height, ARGB);
            blurredToken.loadPixels();
            for (int y = 1; y < token.height - 1; y++) { // Skip top and bottom edges
                for (int x = 1; x < token.width - 1; x++) { // Skip left and right edges
                    float rgbSum = 0; 
                    float aSum = 0; 
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            // Calculate the adjacent pixel for this point
                            int pos = (y + ky) * token.width + (x + kx);
                            // Image is grayscale, red/green/blue are identical
                            float val = token.pixels[pos] & 0xFF;
                            rgbSum += v * val;

                            val = (token.pixels[pos] >> 24) & 0xFF;
                            aSum += v * val; 
                        }
                    }
                    // For this pixel in the new image, set the gray value
                    // based on the sum from the kernel
                    blurredToken.pixels[y * token.width + x] = color(rgbSum, aSum);
                }
            }
            // State that there are changes to edgeImg.pixels[]
            blurredToken.updatePixels();
            pcTokens[i] = blurredToken;
        }
    }

    @Override
    public void settings() {
        size(1280, 720);

        levelState = new LevelState(this);
        pc = new PlayerCharacter(this, levelState);
        debugOverlay = new DebugOverlay(this);
        scoreOverlay = new ScoreOverlay(this);
        ground = new Platform(this, 0, height, width, 10);
        platforms.add(ground);
        platforms.add(new Platform(this, 1280 * 3 / 4f, 720 * 5 / 6f));
        collisionDetector = new CollisionDetector(this);

        loadPCTokens();
    }

    @Override
    public void draw() {
        // update positions
        pc.integrate();

        // detect collisions
        collisionDetector.detectCollisions();

        // draw
        background(0xFFFFFFEE);
        for (Platform platform : platforms) {
            platform.render();
        }
        pc.render();
        if (levelState.debugging)
            debugOverlay.render();
        scoreOverlay.render();

    }

    @Override
    public void keyPressed() {
        if (key == CODED) {
            switch (keyCode) {
                case LEFT:
                    pc.steer(PlayerCharacter.SteerState.LEFT);
                    break;
                case RIGHT:
                    pc.steer(PlayerCharacter.SteerState.RIGHT);
                    break;
                case UP:
                    pc.jump();
                    break;
                default:
                    // do nothing
            }
        } else {
            switch (key) {
                case 'd':
                case 'D':
                    levelState.debugging = !levelState.debugging;
                    break;
                case '`':
                    levelState.stress = 100;
                    break;
                default:
                    if (Character.isDigit(key)) {
                        levelState.stress = Integer.parseInt("" + key) * 10;
                    }
                    // do nothing
            }
        }
    }

    @Override
    public void keyReleased() {
        if (key == CODED) {
            switch (keyCode) {
                case LEFT:
                    if (pc.getSteerState().equals(PlayerCharacter.SteerState.LEFT)) {
                        pc.steer(PlayerCharacter.SteerState.NEITHER);
                    }
                    break;
                case RIGHT:
                    if (pc.getSteerState().equals(PlayerCharacter.SteerState.RIGHT)) {
                        pc.steer(PlayerCharacter.SteerState.NEITHER);
                    }
                    break;
                default:
                    // do nothing
            }
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = { "Don't Drown" };
        DontDrown sketch = new DontDrown();
        PApplet.runSketch(processingArgs, sketch);
    }

}
