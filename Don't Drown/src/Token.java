import processing.core.PConstants;
import processing.core.PVector;

public class Token extends AbstractDrawable {

    private static final float T_HEIGHT_DIV = 40f;

    public final float height;
    public final float width;
    private final float bounceHeight;
    private final int bounceBuffer;

    private int strokeColour = 0xDDB09500;
    private int fillColour = 0xAAE3C800;
    private int bounceFrames = 30;
    private boolean movingDown = false;

    public Token(DontDrown sketch, float x, float y) {
        super(sketch);
        this.height = sketch.width / T_HEIGHT_DIV;
        this.width = height + sketch.random(-sketch.RSW_DEF, sketch.RSW_DEF);
        this.bounceHeight = height / 4;
        this.bounceBuffer = (int) sketch.random(0, bounceFrames / 2f);
        this.pos = new PVector(x, y);
        pos.y -= (bounceHeight - bounceBuffer) * (bounceHeight / bounceFrames);
    }

    public void integrate() {
        if ((sketch.frameCount - bounceBuffer) % bounceFrames == 0) {
            movingDown = !movingDown;
        }

        if (movingDown) {
            pos.y += bounceHeight / bounceFrames;
        } else {
            pos.y -= bounceHeight / bounceFrames;
        }
    }

    public void render() {
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        if (token == null ||
                (pos.y <= sketch.height && pos.y >= height) // only re-sketch on-screen tokens
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

    @Override
    protected void generateToken() {
        sketch.colorModeRGB();
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        token = sketch.handDraw(PConstants.QUAD, strokeColour, fillColour,
                0, -height / 2,
                width / 2, 0,
                0, height / 2,
                -width / 2, 0);

        token.translate(pos.x, pos.y);
    }

}
