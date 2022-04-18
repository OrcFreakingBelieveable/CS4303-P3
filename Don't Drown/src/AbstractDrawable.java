import processing.core.PShape;
import processing.core.PVector;

public abstract class AbstractDrawable {
    protected final DontDrown sketch;
    protected final LevelState state;

    public PVector oldPos; // movement in the last frame
    public PVector pos; // position

    protected int frameCounter;
    protected PShape token;

    protected AbstractDrawable(DontDrown sketch) {
        this.sketch = sketch;
        this.state = sketch.levelState;
        frameCounter = (int) (sketch.random(0,5)); 
    }

    public void pan(float y) {
        pos.y += y; 
    }

    protected abstract void generateToken();

    public abstract void render();
}
