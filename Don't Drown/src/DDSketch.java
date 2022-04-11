import processing.core.PApplet;

public class DDSketch extends PApplet {

    PlayerCharacter pc;
    DebugOverlay overlay;

    @Override
    public void settings() {
        size(1280, 720);
        pc = new PlayerCharacter(this);
        overlay = new DebugOverlay(this);
    }

    @Override
    public void draw() {
        // update positions
        pc.integrate();

        // draw
        background(0xFFFF0000);
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
