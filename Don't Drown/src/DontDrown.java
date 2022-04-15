import java.util.ArrayList;

import processing.core.PApplet;

public class DontDrown extends Sketcher {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    LevelState levelState;
    PlayerCharacter pc;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Platform ground;
    ArrayList<Platform> platforms = new ArrayList<>();
    CollisionDetector collisionDetector;

    protected DontDrown() {
        super(WIDTH, HEIGHT);
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
        levelState = new LevelState(this);
        pc = new PlayerCharacter(this, levelState);
        debugOverlay = new DebugOverlay(this);
        scoreOverlay = new ScoreOverlay(this);
        ground = new Platform(this, levelState, 0, height, width, 10);
        platforms.add(ground);
        platforms.add(new Platform(this, levelState, 1280 * 3 / 4f, 720 * 5 / 6f));
        collisionDetector = new CollisionDetector(this);

    }

    @Override
    public void draw() {
        noStroke();

        // update positions
        levelState.update();
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
