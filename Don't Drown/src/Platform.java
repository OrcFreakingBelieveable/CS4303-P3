import processing.core.PVector;

public class Platform {

    private static final float PF_HEIGHT_DIV = 50f;
    private static final float PF_WIDTH_DIV = 10f;

    private final DDSketch sketch;
    
    public final float height;
    public final float width;

    public PVector pos;
    private int colour = 0xFF00FF00;

    public Platform(DDSketch sketch, float x, float y) {
        this.sketch = sketch;
        this.height = sketch.height / PF_HEIGHT_DIV;
        this.width = sketch.width / PF_WIDTH_DIV;
        this.pos = new PVector(x, y);
    }

    public void render() {
        sketch.fill(colour);
        sketch.rect(pos.x, pos.y, width, height);
    }

}
