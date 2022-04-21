import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Platform extends AbstractDrawable {

    private static final float PF_WIDTH_DIV = 10f;
    private static final float PF_HEIGHT_DIV = 7f; // as a ratio of width

    public final float height;
    public final float width;

    private static PShape[][] staticTokensDefaultWidth;
    private static int strokeColour = 0xDDD79B00;
    private static int fillColour = 0xAAFFE6CC;

    public boolean supportingPC = false;

    public Platform(DontDrown sketch, float x, float y) {
        super(sketch, (staticTokensDefaultWidth == null ? generateTokens(sketch) : staticTokensDefaultWidth));
        this.width = sketch.width / PF_WIDTH_DIV;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
    }

    public Platform(DontDrown sketch, float x, float y, float width) {
        super(sketch, generateTokens(sketch, width));
        this.width = width;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
    }

    protected static PShape[][] generateTokens(DontDrown sketch, float width) {
        float height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        PShape[][] tokens = new PShape[LevelState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];

        sketch.colorModeRGB();
        float thickStrokeWeight = 2 * sketch.RSW_DEF;

        for (int i = 0; i <= LevelState.ABS_MAX_STRESS; i++) {
            sketch.levelState.stress = i;
            sketch.levelState.sketchiness();

            for (int j = 0; j < VARIANT_TOKENS; j++) {
                PShape token = new PShape(PConstants.GROUP);

                // add basic shape 
                sketch.roughStrokeWeight = sketch.RSW_DEF;
                token.addChild(sketch.handDraw(PConstants.QUAD, strokeColour, fillColour,
                        2 * sketch.RSW_DEF, sketch.RSW_DEF, width - 2 * sketch.RSW_DEF, sketch.RSW_DEF,
                        width - (2 * sketch.RSW_DEF + width / 16), height, 2 * sketch.RSW_DEF + width / 16, height));

                // add extra thick top line 
                sketch.roughStrokeWeight = thickStrokeWeight;
                token.addChild(sketch.handDrawLine(strokeColour, new PVector(0, 0), new PVector(width, 0)));

                tokens[i][j] = token;
            }
        }
        return tokens;
    }

    protected static PShape[][] generateTokens(DontDrown sketch) {
        float width = sketch.width / PF_WIDTH_DIV;

        staticTokensDefaultWidth = generateTokens(sketch, width);
        return staticTokensDefaultWidth;
    }

    protected boolean onScreen() {
        return pos.y <= sketch.height && pos.y >= height;
    }
}
