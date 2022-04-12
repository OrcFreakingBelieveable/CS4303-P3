import java.util.ArrayList;

import processing.core.PApplet;

public class DontDrown extends PApplet {

    GameState gameState;
    PlayerCharacter pc;
    DebugOverlay debugOverlay;
    Platform ground;
    ArrayList<Platform> platforms = new ArrayList<>();
    CollisionDetector collisionDetector;

    @Override
    public void settings() {
        size(1280, 720);
        gameState = new GameState(this);
        pc = new PlayerCharacter(this, gameState);
        debugOverlay = new DebugOverlay(this);
        ground = new Platform(this, 0, height, width, 10);
        platforms.add(ground);
        platforms.add(new Platform(this, 1280 * 3 / 4f, 720 * 5 / 6f));
        collisionDetector = new CollisionDetector(this);
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
        if (gameState.debugging)
            debugOverlay.render();

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
                    gameState.debugging = !gameState.debugging;
                    break;                
                default:
                    if (Character.isDigit(key)) {
                        gameState.stress = Integer.parseInt("" + key) * 10; 
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
