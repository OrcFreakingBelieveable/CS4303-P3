import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public abstract class Sketcher extends PApplet {

    protected final float strokeWeight;
    protected final float strokeVariability;
    protected final float shakiness;

    protected Sketcher(float strokeWeight, float strokeVariabilityRate, float shakiness) {
        super();
        this.strokeWeight = strokeWeight;
        this.strokeVariability = strokeVariabilityRate * strokeWeight;
        this.shakiness = shakiness;
    }

    /* Essentially creates a jagged quadrilteral to act as a hand-drawn line */
    protected PShape handDrawLine(PVector start, PVector end) {
        if (start.x > end.x) {
            PVector temp = start.copy();
            start = end.copy();
            end = temp;
        }

        PVector smoothLine = start.copy().sub(end);
        float smoothLineLength = smoothLine.mag();

        PVector topLeft = start.copy();
        PVector topRight = end.copy();
        float heading = smoothLine.heading();
        heading += HALF_PI;
        PVector padding = PVector.fromAngle(heading).mult(strokeWeight);
        PVector bottomRight = topRight.copy().add(padding);
        PVector bottomLeft = topLeft.copy().add(padding);

        PShape roughLine = createShape();
        roughLine.beginShape();
        roughLine.vertex(start.x, start.y); // top left corner

        int sections = (int) random(0, shakiness * smoothLineLength);
        PVector section = topLeft.copy();
        for (int i = 0; i < sections; i++) {
            float x = random(Math.min(section.x, topRight.x - strokeVariability),
                    Math.max(section.x, topRight.x - strokeVariability));
            x += random(-strokeVariability, strokeVariability);
            float y = random(Math.min(section.y, topRight.y - strokeVariability),
                    Math.max(section.y, topRight.y - strokeVariability));
            y += random(-strokeVariability, strokeVariability);
            section = new PVector(x, y);

            roughLine.vertex(x, y);
        }

        roughLine.vertex(topRight.x, topRight.y);
        roughLine.vertex(bottomRight.x, bottomRight.y);

        sections = (int) random(0, shakiness * smoothLineLength);
        section = new PVector(bottomRight.x, bottomRight.y);
        for (int i = 0; i < sections; i++) {
            float x = random(Math.min(section.x, bottomLeft.x - strokeVariability),
                    Math.max(section.x, bottomLeft.x - strokeVariability));
            x += random(-strokeVariability, strokeVariability);
            float y = random(Math.min(section.y, bottomLeft.y - strokeVariability),
                    Math.max(section.y, bottomLeft.y - strokeVariability));
            y += random(-strokeVariability, strokeVariability);
            section = new PVector(x, y);

            roughLine.vertex(x, y);
        }

        roughLine.vertex(bottomLeft.x, bottomLeft.y);
        roughLine.endShape(CLOSE);
        return roughLine;
    }

    public PShape handDraw(int type, float... params) {
        switch (type) {
            case PConstants.QUAD:
                // params : x1, y1, x2, y2, x3, y3, x4, y4
                if (params.length != 8) {
                    System.err.println("handDraw(QUAD) requires 8 floats in params");
                    return createShape(type, params);
                } else {
                    PShape quad = createShape(GROUP);
                    quad.addChild(handDrawLine(new PVector(params[0], params[1]), new PVector(params[2], params[3])));
                    quad.addChild(handDrawLine(new PVector(params[2], params[3] - strokeWeight),
                            new PVector(params[4], params[5])));
                    quad.addChild(handDrawLine(new PVector(params[4], params[5]), new PVector(params[6], params[7])));
                    quad.addChild(handDrawLine(new PVector(params[6], params[7]),
                            new PVector(params[0], params[1] - strokeWeight)));
                    return quad;
                }
            case PConstants.RECT:
            case PConstants.ELLIPSE:
            default:
                System.err.println("handDraw() only works with QUAD, RECT, and ELLIPSE objects");
                return createShape(type, params);
        }
    }

}
