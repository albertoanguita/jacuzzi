package org.aanguita.jacuzzi.numeric;

/**
 * This class represents a continuous degree value (a continuous value that goes from 0 to 1 by default).
 * It uses a double as internal representation of the degree. Null values are allowed.
 * <p/>
 * A different range than 0 to 1 can be selected at construction time
 */
public final class ContinuousDegree {

    public static final double DEFAULT_MIN = 0.0d;

    public static final double DEFAULT_MAX = 1.0d;

    private Double degree;

    private final double min;

    private final double max;

    public ContinuousDegree(Double degree) {
        this(degree, DEFAULT_MIN, DEFAULT_MAX);
    }

    public ContinuousDegree(Double degree, double min, double max) {
        this.degree = degree;
        this.min = min;
        this.max = max;
        checkCorrectValue();
    }

    private void checkCorrectValue() {
        if (degree != null) {
            if (degree.compareTo(min) < 0) {
                degree = min;
            } else if (degree.compareTo(max) > 0) {
                degree = max;
            }
        }
    }

    public Double getValue() {
        return degree;
    }

    public void setDegree(Double degree) {
        this.degree = degree;
        checkCorrectValue();
    }

    public boolean isMin() {
        return degree.equals(min);
    }

    public boolean isMax() {
        return degree.equals(max);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
