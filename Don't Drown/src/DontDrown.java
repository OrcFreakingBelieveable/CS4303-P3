import processing.core.PApplet;
import processing.event.MouseEvent;

public class DontDrown extends Sketcher {

    private static final float LOADING_TEXT_DIV = 5f;
    private static final int SCROLL_DIV = 20;

    public enum GameState {
        PRE_STARTUP,
        STARTUP,
        MID_LEVEL,
        IN_MENU,
        ;
    }

    GameState gameState = GameState.PRE_STARTUP;
    GameMenu gameMenu;
    StressAndTokenState levelState;
    PlayerCharacter pc;
    Wave risingWave;
    Wave staticWave;
    DebugOverlay debugOverlay;
    ScoreOverlay scoreOverlay;
    Level[][] levels;
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
        levels = new Level[Debuff.values().length][];
        int deb = 0;
        for (Debuff debuff : Debuff.values()) {
            levels[deb] = new Level[Difficulty.values().length];

            int dif = 0;
            for (Difficulty difficulty : Difficulty.values()) {
                levels[deb][dif++] = new Level(this, debuff, difficulty);
            }
            deb++;
        }
        gameMenu.updateLevelSelector();
    }

    public void startLevel(Level levelToStart) {
        if (levelToStart == null) {
            level = new Level(this, Debuff.NONE, Difficulty.MEDIUM);
        } else {
            level = levelToStart;
        }
        levelState.reset(level);
        collisionDetector.sortLists();
        risingWave.pos.y = Wave.waveInitHeight;
        Platform ground = level.platforms.get(0);
        pc.reset(ground.pos.x + ground.width / 2, ground.pos.y - PlayerCharacter.diameter);
        collisionDetector.pcOldPos = pc.pos.copy();
        gameState = DontDrown.GameState.MID_LEVEL;
    }

    public void endLevel(boolean completed) {
        gameState = GameState.IN_MENU;
        gameMenu.setMenuState(GameMenu.MenuState.LEVEL_SELECTION);
        if (completed && level.highScore < levelState.tokensCollected) {
            level.highScore = levelState.tokensCollected;
            gameMenu.updateLevelSelector();
        }
    }

    @Override
    public void settings() {
        size((int) (displayWidth * 0.9), (int) (displayHeight * 0.9));
        this.RSW_DEF = width / RSW_DEF_DIV;
    }

    @Override
    public void draw() {
        switch (gameState) {
            case PRE_STARTUP:
                textAlign(CENTER, CENTER);
                textSize(height / LOADING_TEXT_DIV);
                fill(0xFF000000);
                text("Loading...", width / 2f, height / 2f);

                gameState = GameState.STARTUP;
                break;
            case STARTUP:
                noStroke();
                levelState = new StressAndTokenState(this);
                pc = new PlayerCharacter(this);
                risingWave = new Wave(this);
                staticWave = new Wave(this);
                levelState.pcCalcs();
                debugOverlay = new DebugOverlay(this);
                scoreOverlay = new ScoreOverlay(this);
                collisionDetector = new CollisionDetector(this);
                gameMenu = new GameMenu(this);
                generateLevels();

                levelState.stress = 0;
                levelState.sketchiness();
                gameState = GameState.IN_MENU;
                break;
            case IN_MENU:
                cursor();
                gameMenu.render();
                if (debugging)
                    debugOverlay.render();
                break;
            case MID_LEVEL:
                noCursor();

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
                scoreOverlay.render();
                if (debugging)
                    debugOverlay.render();
                break;
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'D') {
            debugging = !debugging;
            return;
        }

        switch (gameState) {
            case PRE_STARTUP:
            case STARTUP:
                // ignore inputs
                break;
            case IN_MENU:
                // unpause
                if (gameMenu.getMenuState().equals(GameMenu.MenuState.PAUSE_MENU)) {
                    gameState = DontDrown.GameState.MID_LEVEL;
                }
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
                        case DOWN:
                            pc.drop();
                            break;
                        default:
                            // do nothing
                    }
                } else if (key == 'p' || key == 'P') {
                    gameMenu.midLevel = true;
                    gameMenu.setMenuState(GameMenu.MenuState.PAUSE_MENU);
                    gameState = GameState.IN_MENU;
                } else if (debugging) {
                    switch (key) {
                        case '`':
                            levelState.stress = 100;
                            break;
                        case ' ':
                            startLevel(null);
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
                            if (level.waveRiseRate == level.defaultWaveRiseRate) {
                                level.waveRiseRate = 0;
                            } else {
                                level.waveRiseRate = level.defaultWaveRiseRate;
                            }
                            break;
                        default:
                            if (Character.isDigit(key)) {
                                levelState.stress = Integer.parseInt("" + key) * 10f;
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

    @Override
    public void mouseWheel(MouseEvent event) {
        if (gameState.equals(GameState.IN_MENU)) {
            gameMenu.scrollWrapper(-event.getCount() * height / SCROLL_DIV);
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = { "DontDrown" };
        DontDrown sketch = new DontDrown();
        PApplet.runSketch(processingArgs, sketch);
    }

}
