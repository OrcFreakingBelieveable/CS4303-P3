import java.util.ArrayList;

import processing.core.PApplet;

public class DDSketch extends PApplet {

    PlayerCharacter pc;
    DebugOverlay overlay;
    ArrayList<Platform> platforms = new ArrayList<>();
    CollisionDetector collisionDetector;

    @Override
    public void settings() {
        size(1280, 720);
        pc = new PlayerCharacter(this);
        overlay = new DebugOverlay(this);
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
        background(0xFFFF0000);
        for (Platform platform : platforms) {
            platform.render();
        }
        pc.render();
        overlay.render();

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
        String[] processingArgs = { "" };
        DDSketch sketch = new DDSketch();
        PApplet.runSketch(processingArgs, sketch);
    }

}
