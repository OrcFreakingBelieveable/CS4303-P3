import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class Test extends Sketcher {

    PShape s1, s2, s3, s4, s5, s6, s7;
    int frame = 0;

    @Override
    public void settings() {
        size(1024, 768);
        roughStrokeVariabilityRate = 1f;
        roughStrokeShakiness = 0.1f;
    }

    @Override
    public void draw() {
        background(0xFFBBBBBB);
        stroke(255);
        noStroke();
        fill(0xFF000000);
        if (frame++ % 20 == 0) {
            s1 = handDrawLine(new PVector(100f, 100f), new PVector(200f, 100f));
            s2 = handDrawLine(new PVector(200f, 100f - roughStrokeWeight), new PVector(200f, 200f));
            s3 = handDrawLine(new PVector(200f, 200f), new PVector(100f, 200f));
            s4 = handDrawLine(new PVector(100f, 200f), new PVector(100f, 100f - roughStrokeWeight));

            // s5 = handDraw(QUAD, 100f, 100f, 200f, 100f, 200f, 200f, 100f, 200f);
            // s5 = handDraw(QUAD, 0xFF000000, 0xFFFF0066, 100f, 150f, 150f, 100f, 200f, 150f, 150f, 200f);
            // s5 = handDraw(RECT, 0xFF000000, 0xFFFF0066, 100f, 100f, 100f, 200f);
            // s5 = handDraw(ELLIPSE, 0xFF000000, 0xFFFFFF00, 40, width / 2, height / 2, 150f, 150f);
       
            roughStrokeWeight = 3.5f; 

            roughStrokeVariabilityRate = 0.3f;
            roughStrokeShakiness = 0.05f;
            s5 = handDraw(RECT, 100f, 100f, 100f, 100f);
            roughStrokeVariabilityRate = 0.65f;
            roughStrokeShakiness = 0.125f;
            s6 = handDraw(RECT, 250f, 100f, 100f, 100f);
            roughStrokeVariabilityRate = 1f;
            roughStrokeShakiness = 0.2f;
            s7 = handDraw(RECT, 400f, 100f, 100f, 100f);
        }
        /*
         * shape(s1);
         * shape(s2);
         * shape(s3);
         * shape(s4);
         */
        shape(s5);
        shape(s6);
        shape(s7);
        text((int)frameRate, 50, 50);
    }

    public static void main(String[] args) {
        String[] processingArgs = { "Don't Drown" };
        Test sketch = new Test();
        PApplet.runSketch(processingArgs, sketch);
    }

}
