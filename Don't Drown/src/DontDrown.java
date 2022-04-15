import processing.core.PApplet;

public class DontDrown extends Sketcher {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    LevelState levelState;
    PlayerCharacter pc;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Level level;
    CollisionDetector collisionDetector;

    protected DontDrown() {
        super(HEIGHT);
    }

    private void newLevel() {
        level = new Level(this, HEIGHT, true);
        Platform ground = level.platforms.get(0); 
        pc.pos.x = ground.pos.x + ground.width/2;
        pc.pos.y =  ground.pos.y - pc.diameter;
    }

    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
        levelState = new LevelState(this);
        pc = new PlayerCharacter(this);
        debugOverlay = new DebugOverlay(this);
        scoreOverlay = new ScoreOverlay(this);
        newLevel();
        collisionDetector = new CollisionDetector(this);
    }

    @Override
    public void draw() {
        noStroke();

        // update positions
        levelState.update();
        pc.integrate();
        if (pc.pos.y > height) {
            pc.pos.y = height - pc.diameter; 
        }

        // detect collisions
        collisionDetector.detectCollisions();

        // draw
        background(0xFFFFFFEE);
        level.render(); 
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
                case ' ':
                    newLevel();
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
        } else {
            switch (key) {
                case '+':
                    levelState.stress++;
                    break;
                case '-':
                    levelState.stress--;
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
