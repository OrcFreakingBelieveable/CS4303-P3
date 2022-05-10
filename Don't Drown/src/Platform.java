import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Platform extends AbstractDrawable {

    public static final float PF_WIDTH_DIV = 10f;
    private static final float PF_HEIGHT_DIV = 7f; // as a ratio of width

    public PVector initPos;
    public final float height;
    public final float width;

    private static PShape[][] staticTokensDefaultWidth;
    private static PShape[][] staticTokensTop; // coloured differently to other tokens 

    private static final int STROKE_COLOUR = 0xDDD79B00;
    private static final int FILL_COLOUR = 0xAAFFE6CC;
    private static final int TOP_STROKE_COLOUR = 0xDDD7C800;
    private static final int TOP_FILL_COLOUR = 0xAAFFE678;

    public Platform(DontDrown sketch, float x, float y) {
        super(sketch, (staticTokensDefaultWidth == null ? generateTokens(sketch) : staticTokensDefaultWidth));
        this.width = sketch.width / PF_WIDTH_DIV;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
        this.initPos = pos.copy();
    }

    public Platform(DontDrown sketch, float x, float y, float width) {
        super(sketch, generateTokens(sketch, width, STROKE_COLOUR, FILL_COLOUR));
        this.width = width;
        this.height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        this.pos = new PVector(x, y);
        this.initPos = pos.copy();
    }

    /**
     * Generates a copy of a platform that is coloured to mark it as a the top
     * platform. Should be used to replace that platform.
     * 
     * @param source the platform to copy
     */
    public Platform(Platform source) {
        super(source.sketch, (staticTokensTop == null ? generateTopTokens(source.sketch) : staticTokensTop));
        this.width = source.width;
        this.height = source.height;
        this.pos = source.pos.copy();
        this.initPos = pos.copy();
    }

    protected static PShape[][] generateTokens(DontDrown sketch, float width, int strokeColour, int fillColour) {
        float height = sketch.width / PF_WIDTH_DIV / PF_HEIGHT_DIV;
        PShape[][] tokens = new PShape[StressAndTokenState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];

        sketch.colorModeRGB();
        float thickStrokeWeight = 2 * sketch.RSW_DEF;

        for (int i = 0; i <= StressAndTokenState.ABS_MAX_STRESS; i++) {
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

        staticTokensDefaultWidth = generateTokens(sketch, width, STROKE_COLOUR, FILL_COLOUR);
        return staticTokensDefaultWidth;
    }

    protected static PShape[][] generateTopTokens(DontDrown sketch) {
        float width = sketch.width / PF_WIDTH_DIV;

        staticTokensTop = generateTokens(sketch, width, TOP_STROKE_COLOUR, TOP_FILL_COLOUR);
        return staticTokensTop;
    }

    protected boolean onScreen() {
        return pos.y <= sketch.height && pos.y >= height;
    }

    public void render() {
        renderAD();
    }
}
