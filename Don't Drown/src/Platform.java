import processing.core.PVector;

public class Platform {

    private static final float PF_HEIGHT_DIV = 50f;
    private static final float PF_WIDTH_DIV = 10f;

    private final DontDrown sketch;
    
    public final float height;
    public final float width;

    public PVector pos;
    private int colour = 0xFF00FF00;

    public Platform(DontDrown sketch, float x, float y) {
        this.sketch = sketch;
        this.height = sketch.height / PF_HEIGHT_DIV;
        this.width = sketch.width / PF_WIDTH_DIV;
        this.pos = new PVector(x, y);
    }

    public Platform(DontDrown sketch, float x, float y, float width, float height) {
        this.sketch = sketch;
        this.height = height;
        this.width = width;
        this.pos = new PVector(x, y);
    }

    public void render() {
        sketch.fill(colour);
        sketch.rect(pos.x, pos.y, width, height);
    }

}
