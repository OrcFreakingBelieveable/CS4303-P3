import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Platform extends AbstractDrawable {

    private static final float PF_WIDTH_DIV = 10f;
    private static final float PF_HEIGHT_DIV = 7f; // as a ratio of width 

    public final float height;
    public final float width;

    public boolean supportingPC = false;

    private int strokeColour = 0xDDD79B00;
    private int fillColour = 0xAAFFE6CC;

    public Platform(DontDrown sketch, float x, float y) {
        super(sketch);
        this.width = sketch.width / PF_WIDTH_DIV;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
    }

    public Platform(DontDrown sketch, float x, float y, float width) {
        super(sketch);
        this.width = width;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
    }

    protected void generateToken() {
        sketch.colorModeRGB(); 
        sketch.roughStrokeWeight = sketch.RSW_DEF;
        float thickStrokeWeight = 2 * sketch.RSW_DEF;

        token = new PShape(PConstants.GROUP);
        token.addChild(sketch.handDraw(PConstants.QUAD, strokeColour, fillColour,
                2 * sketch.RSW_DEF, sketch.RSW_DEF, width - 2 * sketch.RSW_DEF, sketch.RSW_DEF,
                width - (2 * sketch.RSW_DEF + width / 16), height, 2 * sketch.RSW_DEF + width / 16, height));

        sketch.roughStrokeWeight = thickStrokeWeight;

        token.addChild(sketch.handDrawLine(strokeColour, new PVector(0, 0), new PVector(width, 0)));

        token.translate(pos.x, pos.y);
    }

    public void render() {
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        if (token == null ||
                (pos.y <= sketch.height && pos.y >= height) // only re-sketch on-screen platforms
                        && (frameCounter++ % state.framesPerResketch == 0
                                || Math.abs(state.stress - state.oldStress) > 5)) {
            generateToken();
        } else if (oldPos.x != pos.x || oldPos.y != pos.y) {
            PVector movement = pos.copy().sub(oldPos);
            token.translate(movement.x, movement.y);
        }

        oldPos = pos.copy();
        sketch.shape(token);
    }

}
