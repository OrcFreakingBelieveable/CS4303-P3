import processing.core.PConstants;
import processing.core.PVector;

public class Token extends AbstractDrawable {

    public static final int T_STROKE_COLOUR = 0xDDB09500;
    public static final int T_FILL_COLOUR = 0xAAE3C800;

    private static final float T_HEIGHT_DIV = 40f;
    private static final int T_BOUNCE_FRAMES = 30;

    public final float height;
    public final float width;
    private final float bounceHeight;
    private final float bounceIncr;

    private boolean movingDown = false;

    public Token(DontDrown sketch, float x, float y) {
        super(sketch);
        this.height = sketch.width / T_HEIGHT_DIV;
        this.width = height + sketch.random(-sketch.RSW_DEF, sketch.RSW_DEF);
        this.bounceHeight = height / 4;
        this.bounceIncr = (bounceHeight / T_BOUNCE_FRAMES);
        this.pos = new PVector(x, y);
        pos.y -= bounceIncr * (sketch.frameCount % T_BOUNCE_FRAMES);
    }

    public void integrate() {
        if (sketch.frameCount % T_BOUNCE_FRAMES == 0) {
            movingDown = !movingDown;
        }

        if (movingDown) {
            pos.y += bounceIncr;
        } else {
            pos.y -= bounceIncr;
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

        token = sketch.handDraw(PConstants.QUAD, T_STROKE_COLOUR, T_FILL_COLOUR,
                0, -height / 2,
                width / 2, 0,
                0, height / 2,
                -width / 2, 0);

        token.translate(pos.x, pos.y);
    }

}
