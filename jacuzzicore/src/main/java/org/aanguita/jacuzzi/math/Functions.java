package org.aanguita.jacuzzi.math;

/**
 * Methods that implement known mathematical functions
 */
public class Functions {

    /**
     * Maps values from -infinite to +infinite to the 0, +1 range. The range and the stretch can be configured
     *
     * @param x       the x value
     * @param min     the min value that the function reaches
     * @param max     the max value that the function reaches
     * @param stretch factor for horizontally stretching the values
     * @return the y value
     */
    public static float sigmoid(float x, float min, float max, float stretch) {
        float factor = max - min;
        return min + factor / (1f + (float) Math.pow(Math.E, -x / stretch));
    }

    /**
     * Maps values from -infinite to +infinite to the 0, +1 range. The range and the stretch can be configured
     *
     * @param x       the x value
     * @param min     the min value that the function reaches
     * @param max     the max value that the function reaches
     * @param stretch factor for horizontally stretching the values
     * @return the y value
     */
    public static double sigmoid(double x, double min, double max, double stretch) {
        double factor = max - min;
        return min + factor / (1d + Math.pow(Math.E, -x / stretch));
    }
}
