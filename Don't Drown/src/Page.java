import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

/**
 * The lined paper that serves as the background of the game.
 */
public class Page {

    private static final int PAGE_COLOUR = 0xFFFFFFEE;
    private static final int LINE_COLOUR = 0xFF666666;
    private static final int MARGIN_COLOUR = 0x88B85450;
    public static final int MARGIN_DIV = 10;

    public static float marginX;

    private final DontDrown sketch;

    public final int height;
    public final float topLineY;
    public final float lineGap; // space between lines; used to line up menu text
    public final boolean startAtTop; // whether or not the page should default to the top or bottom when displaying

    public PShape lines = null;

    public Page(DontDrown sketch) {
        if (marginX == 0.0f)
            setMargin(sketch);
        startAtTop = true; // doesn't matter because page matches viewport height
        this.sketch = sketch;
        height = sketch.height;
        this.topLineY = sketch.height - height + sketch.scoreOverlay.endOfPadding;
        this.lineGap = sketch.width / PlayerCharacter.PC_DIAMETER_DIV;
    }

    public Page(DontDrown sketch, int height, boolean startAtTop) {
        if (marginX == 0.0f)
            setMargin(sketch);
        this.sketch = sketch;
        this.startAtTop = startAtTop;
        this.height = height;
        this.topLineY = sketch.height - height + sketch.scoreOverlay.endOfPadding;
        this.lineGap = sketch.width / PlayerCharacter.PC_DIAMETER_DIV;
    }

    /*
     * Sets the position of the left-hand margin, which is consistent between pages
     */
    private static void setMargin(DontDrown sketch) {
        marginX = (float) sketch.width / MARGIN_DIV;
    }

    /* Places the margin and uniformly spaced horizontal lines on the page */
    private void generateLines() {
        lines = sketch.createShape(PConstants.GROUP);
        for (int i = 1; i <= height / lineGap; i++) {
            lines.addChild(
                    drawLine(new PVector(0, topLineY + i * lineGap),
                            new PVector(sketch.width, topLineY + i * lineGap),
                            1, LINE_COLOUR));
        }
        lines.addChild(drawLine(new PVector(marginX, sketch.height),
                new PVector(marginX, (float) sketch.height - height), 1, MARGIN_COLOUR));

        if (startAtTop) {
            lines.translate(0, (float) height - sketch.height);
        }
    }

    /*
     * Draws lines as rectangles so that their weight is more easily customised, and
     * they can be moved to pan/scroll the page.
     */
    private PShape drawLine(PVector start, PVector end, float weight, int colour) {
        PVector smoothLine = start.copy().sub(end);
        float angle = smoothLine.heading();
        angle += PConstants.HALF_PI;
        PVector padding = PVector.fromAngle(angle).mult(weight / 2);
        PVector topLeft = start.copy().sub(padding);
        PVector topRight = end.copy().sub(padding);
        PVector bottomRight = topRight.copy().add(padding);
        PVector bottomLeft = topLeft.copy().add(padding);

        PShape line = sketch.createShape(PConstants.QUAD,
                topLeft.x, topLeft.y, topRight.x, topRight.y,
                bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y);

        line.setFill(colour);
        return line;
    }

    public void render() {
        if (lines == null) {
            generateLines();
        }

        sketch.colorModeRGB();
        sketch.background(PAGE_COLOUR);
        sketch.shape(lines);
    }

}
