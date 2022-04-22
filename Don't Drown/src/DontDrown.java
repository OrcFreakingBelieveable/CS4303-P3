import processing.core.PApplet;

public class DontDrown extends Sketcher {

    public static String[] levelTitles = new String[] { "Tutorial", "Panic Prone", "Overworked", "Can't Unwind",
            "Stress Motivated", "Tunnel Vision", "Lacking Self-awareness" };

    public enum GameState {
        STARTUP,
        MID_LEVEL,
        IN_MENU,
        ;
    }

    GameState gameState = GameState.STARTUP;
    GameMenu gameMenu;
    LevelState levelState;
    PlayerCharacter pc;
    Wave risingWave;
    Wave staticWave;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Level[] levels;
    Level level;
    CollisionDetector collisionDetector;
    public boolean debugging = true;

    public void colorModeHSB() {
        colorMode(HSB, 360f, 1f, 1f, 1f);
    }

    public void colorModeRGB() {
        colorMode(ARGB, 255, 255, 255, 255);
    }

    private void generateLevels() {
        levels = new Level[] {
                new Level(this, "Feeling Typical", height * 2, true, 0.1f),
                new Level(this, "Panic Prone", height * 2, true, 0.1f),
                new Level(this, "Overworked", height * 2, true, 0.1f),
                new Level(this, "Can't Unwind", height * 2, true, 0.1f),
                new Level(this, "Stress Motivated", height * 2, true, 0.1f),
                new Level(this, "Tunnel Vision", height * 2, true, 0.1f),
                new Level(this, "Lacking Self-awareness", height * 2, true, 0.1f) };
    }

    private void newLevel(DontDrown sketch) {
        level = new Level(sketch, "Generic", height * 2, true, 0.1f);
        levelState.reset(level);
        risingWave.pos.y = Wave.waveInitHeight;
        Platform ground = level.platforms.get(0);
        pc.reset(ground.pos.x + ground.width / 2, ground.pos.y - PlayerCharacter.diameter);
        collisionDetector.pcOldPos = pc.pos.copy();
    }

    @Override
    public void settings() {
        size((int) (displayWidth * 0.9), (int) (displayHeight * 0.9));
        this.RSW_DEF = width / RSW_DEF_DIV;
    }

    @Override
    public void draw() {
        switch (gameState) {
            case STARTUP:
                noStroke();
                levelState = new LevelState(this);
                pc = new PlayerCharacter(this);
                risingWave = new Wave(this);
                staticWave = new Wave(this);
                levelState.pcCalcs();
                debugOverlay = new DebugOverlay(this);
                scoreOverlay = new ScoreOverlay(this);
                collisionDetector = new CollisionDetector(this);
                gameMenu = new GameMenu(this);
                generateLevels();
                newLevel(this);

                gameState = GameState.IN_MENU;
                break;
            case IN_MENU:
                gameMenu.render();
                break;
            case MID_LEVEL:
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

                // detect collisions
                collisionDetector.detectCollisions();

                // draw
                level.render();
                pc.render();
                risingWave.render();
                if (debugging)
                    debugOverlay.render();
                scoreOverlay.render();

                break;

        }

    }

    @Override
    public void keyPressed() {
        switch (gameState) {
            case IN_MENU:
            case STARTUP:
                // ignore inputs
                break;
            case MID_LEVEL:
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
                } else if (key == 'p' || key == 'P') {
                    gameMenu.midLevel = true;
                    gameMenu.menuState = GameMenu.MenuState.PAUSE_MENU;
                    gameState = GameState.IN_MENU;
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
                        case 'w':
                        case 'W':
                            if (risingWave.waveRiseRate == risingWave.defaultWaveRiseRate) {
                                risingWave.waveRiseRate = 0;
                            } else {
                                risingWave.waveRiseRate = risingWave.defaultWaveRiseRate;
                            }
                            break;
                        default:
                            if (Character.isDigit(key)) {
                                levelState.stress = Integer.parseInt("" + key) * 10;
                            }
                            // do nothing
                    }
                }
                break;
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

    @Override
    public void mouseClicked() {
        if (gameState.equals(GameState.IN_MENU)) {
            gameMenu.resolveClick();
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = { "DontDrown" };
        DontDrown sketch = new DontDrown();
        PApplet.runSketch(processingArgs, sketch);
    }

}
