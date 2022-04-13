import java.util.Arrays;

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
        PVector smoothLine = start.copy().sub(end);
        float smoothLineLength = smoothLine.mag();
        float heading = smoothLine.heading();
        heading += HALF_PI;
        PVector padding = PVector.fromAngle(heading).mult(strokeWeight);

        // topLeft is not necessarily the top left corner, but it's easier to keep track
        // of than just numbering the corners
        PVector topLeft = start.copy().sub(padding);
        PVector topRight = end.copy().sub(padding);
        PVector bottomRight = topRight.copy().add(padding);
        PVector bottomLeft = topLeft.copy().add(padding);

        PShape roughLine = createShape();
        roughLine.beginShape();
        roughLine.vertex(topLeft.x, topLeft.y); // top left corner

        int sections = (int) random(0, Math.max(2, shakiness * smoothLineLength));
        PVector section = topLeft.copy();
        PVector direction = (topRight.copy().sub(topLeft)).normalize();
        for (int i = 0; i < sections; i++) {
            section = section.add(direction.copy().mult(random(0, smoothLineLength / sections)));
            roughLine.vertex(section.x + random(-strokeVariability, strokeVariability),
                    section.y + random(-strokeVariability, strokeVariability));
        }

        roughLine.vertex(topRight.x, topRight.y);
        roughLine.vertex(bottomRight.x, bottomRight.y);

        sections = (int) random(0, Math.max(2, shakiness * smoothLineLength));
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

    /* black stroke and empty fill */
    public PShape handDraw(int type, float... params) {
        return handDraw(type, 0xFF000000, 0xFFFFFFFF, params);
    }

    public PShape handDraw(int type, int strokeColour, int fillColour, float... params) {
        switch (type) {
            case PConstants.QUAD:
                // params : x1, y1, x2, y2, x3, y3, x4, y4
                if (params.length != 8) {
                    System.err.println("handDraw(QUAD) requires 8 floats in params");
                    return createShape(type, params);
                } else {
                    PShape quad = createShape(GROUP);

                    PShape fill = createShape(type, params);
                    fill.setFill(fillColour);
                    quad.addChild(fill);

                    PShape line = handDrawLine(new PVector(params[0], params[1]), new PVector(params[2], params[3]));
                    line.setFill(strokeColour);
                    quad.addChild(line);

                    line = handDrawLine(new PVector(params[2], params[3]), new PVector(params[4], params[5]));
                    line.setFill(strokeColour);
                    quad.addChild(line);

                    line = handDrawLine(new PVector(params[4], params[5]), new PVector(params[6], params[7]));
                    line.setFill(strokeColour);
                    quad.addChild(line);

                    line = handDrawLine(new PVector(params[6], params[7]), new PVector(params[0], params[1]));
                    line.setFill(strokeColour);
                    quad.addChild(line);

                    return quad;
                }
            case PConstants.RECT:
                // params : x, y, width, height
                if (params.length != 4) {
                    System.err.println("handDraw(RECT) requires 4 floats in params");
                    return createShape(type, params);
                } else {
                    return handDraw(PConstants.QUAD, strokeColour, fillColour,
                            params[0], params[1],
                            params[0] + params[2], params[1],
                            params[0] + params[2], params[1] + params[3],
                            params[0], params[1] + params[3]);
                }
            case PConstants.ELLIPSE:
                // params : vertices, x, y, width, height
                if (params.length != 5) {
                    System.err.println("handDraw(ELLIPSE) requires 5 floats in params");
                    return createShape(type, Arrays.copyOfRange(params, 1, 4));
                } else {
                    int vertices = (int) params[0];
                    float centreX = params[1];
                    float centreY = params[2];
                    float width = params[3] / 2;
                    float height = params[4] / 2;

                    PShape ellipse = createShape(GROUP);

                    // add the fill as a normal shape
                    PShape fill = createShape(type, centreX, centreY, width * 2, height * 2);
                    fill.setFill(fillColour);
                    ellipse.addChild(fill);

                    // generate the lines around the circumference of the ellipse
                    PVector start = new PVector(centreX + width * cos(0), centreY + height * sin(0));
                    PVector end = new PVector();

                    for (int i = 1; i <= vertices; i++) {
                        float angle = i * TAU / vertices;
                        end.x = (centreX + width * cos(angle));
                        end.y = (centreY + height * sin(angle));

                        PShape line = handDrawLine(start, end);
                        line.setFill(strokeColour);
                        ellipse.addChild(line);

                        start.x = end.x;
                        start.y = end.y;
                    }

                    return ellipse;
                }
            default:
                System.err.println("handDraw() only works with QUAD, RECT, and ELLIPSE objects");
                return createShape(type, params);
        }
    }

}
