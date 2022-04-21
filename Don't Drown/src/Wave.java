import processing.core.PVector;

public class Wave extends AbstractDrawable {

    public static final float WAVE_INIT_HEIGHT_MOD_DIV = 20f;
    public static final float WAVE_SECTION_DEPTH_DIV = 160f;
    public static final int WAVE_SECTIONS = 12;
    public static final int WAVE_VERTICES_PER_SECTION = 3;
    public static final int WAVE_RISE_RATE_DIV = 8; // seconds to reach the top of the screen

    private static final int FILL_COLOUR = 0xAA0050EF;
    private static final int STROKE_COLOUR = 0xDD0050EF;

    public final float waveInitHeight;
    public final float waveSectionDepth;
    public final float waveRiseRate;

    public float waveDepth;

    protected Wave(DontDrown sketch) {
        super(sketch);
        waveInitHeight = sketch.height + sketch.width / WAVE_INIT_HEIGHT_MOD_DIV;
        waveSectionDepth = sketch.width / WAVE_SECTION_DEPTH_DIV;
        waveRiseRate = sketch.height / (60f * WAVE_RISE_RATE_DIV);
        pos = new PVector(0, waveInitHeight);
        waveDepth = sketch.height;
    }

    @Override
    protected void generateToken() {
        sketch.colorModeRGB();
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        token = sketch.handDraw(Sketcher.WAVE, STROKE_COLOUR, FILL_COLOUR, sketch.width, waveDepth, WAVE_SECTIONS,
                waveSectionDepth, WAVE_VERTICES_PER_SECTION, frameCounter);

        token.translate(pos.x, pos.y);
    }

    @Override
    public void render() {
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        if (token == null ||
                (pos.y - waveSectionDepth <= sketch.height) // only re-sketch if on-screen
                        && (frameCounter++ % (state.framesPerResketch * 2) == 0)) {
            generateToken();
        } else if (oldPos.x != pos.x || oldPos.y != pos.y) {
            PVector movement = pos.copy().sub(oldPos);
            token.translate(movement.x, movement.y);
        }

        oldPos = pos.copy();
        sketch.shape(token);
    }

}
