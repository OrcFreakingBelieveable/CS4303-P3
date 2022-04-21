import processing.core.PApplet;

public class DontDrown extends Sketcher {

    LevelState levelState;
    PlayerCharacter pc;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Level level;
    CollisionDetector collisionDetector;
    public boolean debugging = true;

    public void colorModeHSB() {
        colorMode(HSB, 360f, 1f, 1f, 1f);
    }

    public void colorModeRGB() {
        colorMode(ARGB, 255, 255, 255, 255);
    }

    private void newLevel(DontDrown sketch) {
        level = new Level(sketch, height * 2, true, 1f);
        levelState.reset(level);
        Platform ground = level.platforms.get(0);
        pc.reset(ground.pos.x + ground.width / 2, ground.pos.y - pc.diameter);
    }

    @Override
    public void settings() {
        size(displayWidth, displayHeight);
        this.RSW_DEF = width / RSW_DEF_DIV;
        levelState = new LevelState(this);
        pc = new PlayerCharacter(this);
        levelState.pcCalcs();
        debugOverlay = new DebugOverlay(this);
        scoreOverlay = new ScoreOverlay(this);
        newLevel(this);
        collisionDetector = new CollisionDetector(this);
    }

    @Override
    public void draw() {
        noStroke();

        // update positions
        levelState.update();
        pc.integrate();
        level.integrate();

        // check if panning needed
        if (pc.pos.y < scoreOverlay.endOfPadding + 2 * pc.jumpHeight) {
            level.panningState = Level.PanningState.UP;
        } else if (pc.pos.y > height - (scoreOverlay.endOfPadding + pc.jumpHeight)) {
            level.panningState = Level.PanningState.DOWN;
        } else {
            level.panningState = Level.PanningState.NEITHER;
        }

        // draw
        level.render();
        pc.render();
        if (debugging)
            debugOverlay.render();
        scoreOverlay.render();
        
        // detect collisions (after they visually occur)
        collisionDetector.detectCollisions();
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
        } else if (key == 'd' || key == 'D') {
            debugging = !debugging;
        } else if (debugging) {
            switch (key) {
                case '`':
                    levelState.stress = 100;
                    break;
                case ' ':
                    newLevel(this);
                    break;
                case 'f':
                case 'F':
                    if (frameRate > 40) {
                        frameRate(30);
                    } else if (frameRate > 20) {
                        frameRate(10);
                    } else {
                        frameRate(60);
                    }
                    break;
                case ',':
                    level.panningState = Level.PanningState.DOWN;
                    break;
                case '.':
                    level.panningState = Level.PanningState.NEITHER;
                    break;
                case '/':
                    level.panningState = Level.PanningState.UP;
                    break;
                case '+':
                    levelState.stress++;
                    break;
                case '-':
                    levelState.stress--;
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
