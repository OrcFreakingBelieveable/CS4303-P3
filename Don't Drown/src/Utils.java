public class Utils {

    /**
     * The following code is adapted from
     * https://www.javatpoint.com/java-program-to-solve-quadratic-equation
     * Accessed 2022-01-28
     */
    public static float solveQuadratic(double a, double b, double c) {
        // ax^2 + bx + c = 0
        // x = (-b+-sqrt(b^2-4ac)) / 2a
        // d = b^2 - 4ac
        // x = (-b+-sqrt(d)) / 2a
        double d = Math.pow(b, 2) - 4 * a * c;
        if (d < 0) {
            return Float.NaN;
        } else if (d == 0) {
            return (float) (-b / (2 * a));
        } else {
            double x1 = (-b + Math.sqrt(d)) / (2 * a);
            double x2 = (-b - Math.sqrt(d)) / (2 * a);
            return (float) (Math.max(x1, x2));
        }
    }
    /* End of adapted code */

}
