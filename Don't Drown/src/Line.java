import processing.core.PVector;

public class Line {
    // y = mx + c

    private final float xStart, xEnd;
    private final float yStart, yEnd;
    private final float c;
    private final float m;
    private final boolean isVertical, isHorizontal;

    public Line(PVector start, PVector end) {
        if (start.x == end.x) {
            isVertical = true;
            isHorizontal = false;
            xStart = start.x;
            xEnd = start.x;
            yStart = start.y;
            yEnd = end.y;
            this.m = Float.NaN;
            this.c = Float.NaN;
        } else if (start.y == end.y) {
            isVertical = false;
            isHorizontal = true;
            xStart = start.x;
            xEnd = end.x;
            yStart = start.y;
            yEnd = start.y;
            this.m = Float.NaN;
            this.c = Float.NaN;
        } else {
            isVertical = false;
            isHorizontal = false;

            if (start.x < end.x) {
                this.xStart = start.x;
                this.yStart = start.y;
                this.xEnd = end.x;
                this.yEnd = end.y;
            } else {
                this.xStart = end.x;
                this.yStart = end.y;
                this.xEnd = start.x;
                this.yEnd = start.y;
            }
            this.m = (yEnd - yStart) / (xEnd - xStart);
            this.c = start.y - m * start.x;
        }
    }

    public static boolean yOverlap(Line line1, Line line2) {
        return line1.yStart <= line2.yEnd && line1.yEnd >= line2.yStart;
    }

    public static boolean xOverlap(Line line1, Line line2) {
        return line1.xStart <= line2.xEnd && line1.xEnd >= line2.xStart;
    }

    public boolean inRange(float val, boolean x) {
        if (x) {
            return val >= xStart && val <= xEnd;
        } else {
            return val >= Math.min(yStart, yEnd) && val <= Math.max(yStart, yEnd);
        }
    }

    public static boolean overlap(Line line1, Line line2) {
        if (line1.isVertical && line2.isVertical) {
            return (line1.xStart == line2.xStart && yOverlap(line1, line2));
        } else if (line1.isHorizontal && line2.isHorizontal) {
            return (line1.yStart == line2.yStart && xOverlap(line1, line2));
        } else if (line1.isVertical) {
            float yIntercept = line2.m * line1.xStart + line2.c;
            return (line1.inRange(yIntercept, false));
        } else if (line2.isVertical) {
            float yIntercept = line1.m * line2.xStart + line1.c;
            return (line2.inRange(yIntercept, false));
        } else if (line1.isHorizontal) {
            float xIntercept = (line1.yStart - line2.c) / line2.m;
            return (line1.inRange(xIntercept, true));
        } else if (line2.isHorizontal) {
            float xIntercept = (line2.yStart - line1.c) / line1.m;
            return (line2.inRange(xIntercept, true));
        } else if (yOverlap(line1, line2) && xOverlap(line1, line2)) {
            // y = m1x + c1 = m2x + c2 -> x(m1 - m2) = c2 - c1 -> x = (c2 - c1)/(m1 - m2)
            float c = (line2.c - line1.c);
            float m = (line1.m - line2.m);
            if (m == 0) {
                // lines are parallel
                return c == 0;
            } else {
                float x = c / m;
                return (line1.xStart <= x && line1.xEnd >= x && line2.xStart <= x && line2.xEnd >= x);
            }
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        final Line line1 = new Line(new PVector(50, 10), new PVector(150, 70));
        Line line2 = new Line(new PVector(60, 10), new PVector(160, 70));
        boolean overlap = overlap(line1, line2);
        System.out.println("Test 01: " + (!overlap ? "passed" : "failed"));

        line2 = new Line(new PVector(55, 10), new PVector(55, 50));
        overlap = overlap(line1, line2);
        System.out.println("Test 02: " + (overlap ? "passed" : "failed"));

        line2 = new Line(new PVector(50, 70), new PVector(120, 10));
        overlap = overlap(line1, line2);
        System.out.println("Test 03: " + (overlap ? "passed" : "failed"));
    }
}
