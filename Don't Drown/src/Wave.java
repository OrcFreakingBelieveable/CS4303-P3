import processing.core.PShape;
import processing.core.PVector;

public class Wave extends AbstractDrawable {

    public static final float WAVE_INIT_HEIGHT_MOD_DIV = 20f;
    public static final float WAVE_SECTION_DEPTH_DIV = 160f;
    public static final int WAVE_SECTIONS = 12;
    public static final int WAVE_VERTICES_PER_SECTION = 6;
    public static final int WAVE_RISE_RATE_DIV = 12; // seconds to reach the top of the viewport

    private static final int FILL_COLOUR = 0xFF99BBFF;
    private static final int STROKE_COLOUR = 0xFF0050EF;

    private static PShape[][] staticTokens = null;
    public static float waveInitHeight;

    public final float defaultWaveRiseRate;
    public float waveRiseRate;

    protected Wave(DontDrown sketch) {
        super(sketch, (staticTokens == null ? generateTokens(sketch) : staticTokens));
        pos = new PVector(0, waveInitHeight);
        defaultWaveRiseRate = sketch.height / (60f * WAVE_RISE_RATE_DIV);
        waveRiseRate = defaultWaveRiseRate;
    }

    protected static PShape[][] generateTokens(DontDrown sketch) {
        staticTokens = new PShape[LevelState.ABS_MAX_STRESS + 1][VARIANT_TOKENS];
        waveInitHeight = sketch.height + sketch.width / WAVE_INIT_HEIGHT_MOD_DIV;
        float waveSectionDepth = sketch.width / WAVE_SECTION_DEPTH_DIV;
        float waveDepth = sketch.height;

        sketch.colorModeRGB();
        sketch.roughStrokeWeight = sketch.RSW_DEF;

        for (int j = 0; j < VARIANT_TOKENS / 2; j++) {
            for (int i = 0; i <= LevelState.ABS_MAX_STRESS; i++) {
                sketch.levelState.stress = i;
                sketch.levelState.sketchiness();

                staticTokens[i][j] = sketch.handDraw(Sketcher.WAVE, STROKE_COLOUR, FILL_COLOUR,
                        sketch.width, waveDepth, WAVE_SECTIONS, waveSectionDepth, WAVE_VERTICES_PER_SECTION,
                        j * 10);
            }
        }
        for (int j = VARIANT_TOKENS / 2; j < VARIANT_TOKENS; j++) {
            for (int i = 0; i <= LevelState.ABS_MAX_STRESS; i++) {
                sketch.levelState.stress = i;
                sketch.levelState.sketchiness();

                staticTokens[i][j] = sketch.handDraw(Sketcher.WAVE, STROKE_COLOUR, FILL_COLOUR,
                        sketch.width, waveDepth, WAVE_SECTIONS, waveSectionDepth, WAVE_VERTICES_PER_SECTION,
                        -(j + 1 - VARIANT_TOKENS) * 10);
            }
        }

        return staticTokens;
    }

    protected boolean onScreen() {
        return true;
    }

    public void render() {
        renderAD();
    }
}
