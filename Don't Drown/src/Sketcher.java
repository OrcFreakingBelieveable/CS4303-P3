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
        PVector direction = (topRight.copy().sub(topLeft)).normalize();
        for (int i = 0; i < sections; i++) {
            section = section.add(direction.copy().mult(random(0, smoothLineLength / sections)));
            roughLine.vertex(section.x + random(-strokeVariability, strokeVariability),
                    section.y + random(-strokeVariability, strokeVariability));
        }

        roughLine.vertex(topRight.x, topRight.y);
        roughLine.vertex(bottomRight.x, bottomRight.y);

        sections = (int) random(0, shakiness * smoothLineLength);
        section = bottomRight.copy();
        direction = (bottomLeft.copy().sub(bottomRight)).normalize();
        for (int i = 0; i < sections; i++) {
            section = section.add(direction.copy().mult(random(0, smoothLineLength / sections)));
            roughLine.vertex(section.x + random(-strokeVariability, strokeVariability),
                    section.y + random(-strokeVariability, strokeVariability));
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
                    quad.addChild(handDrawLine(new PVector(params[2], params[3]),
                            new PVector(params[4], params[5])));
                    quad.addChild(handDrawLine(new PVector(params[4], params[5]), new PVector(params[6], params[7])));
                    quad.addChild(handDrawLine(new PVector(params[6], params[7]),
                            new PVector(params[0], params[1])));
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
