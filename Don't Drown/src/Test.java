import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class Test extends Sketcher {

    PShape s1, s2, s3, s4, s5;
    int frame = 0;

    public Test() {
        super(3f, 0.3f, 0.05f);
    }

    @Override
    public void settings() {
        size(1024, 768);
    }

    @Override
    public void draw() {
        background(0xFFBBBBBB);
        stroke(255);
        noStroke();
        fill(0xFF000000);
        if (frame++ % 20 == 0) {
            s1 = handDrawLine(new PVector(100f, 100f), new PVector(200f, 100f));
            s2 = handDrawLine(new PVector(200f, 100f - strokeWeight), new PVector(200f, 200f));
            s3 = handDrawLine(new PVector(200f, 200f), new PVector(100f, 200f));
            s4 = handDrawLine(new PVector(100f, 200f), new PVector(100f, 100f - strokeWeight));

            s5 = handDraw(QUAD, 100f, 100f, 200f, 100f, 200f, 200f, 100f, 200f);
            // s5 = handDraw(QUAD, 0xFF000000, 0xFFFF0066, 100f, 150f, 150f, 100f, 200f, 150f, 150f, 200f);
            // s5 = handDraw(RECT, 0xFF000000, 0xFFFF0066, 100f, 100f, 100f, 200f);
            // s5 = handDraw(ELLIPSE, 0xFF000000, 0xFFFFFF00, 40, width / 2, height / 2, 150f, 150f);
        }
        /*
         * shape(s1);
         * shape(s2);
         * shape(s3);
         * shape(s4);
         */
        shape(s5);
        text((int)frameRate, 50, 50);
    }

    public static void main(String[] args) {
        String[] processingArgs = { "Don't Drown" };
        Test sketch = new Test();
        PApplet.runSketch(processingArgs, sketch);
    }

}