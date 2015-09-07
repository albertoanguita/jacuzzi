package jacz.util.numeric;

/**
 * This class represents a discrete degree value (a discrete value that goes from 0 to 100 by default).
 * It uses an Integer as internal representation of the degree. Null values are allowed.
 * <p/>
 * A different range than 0 to 100 can be selected at construction time
 */
public final class DiscreteDegree {

    public static final int DEFAULT_MIN = 0;

    public static final int DEFAULT_MAX = 100;

    private Integer degree;

    private final int min;

    private final int max;

    public DiscreteDegree(Integer degree) {
        this(degree, DEFAULT_MIN, DEFAULT_MAX);
    }

    public DiscreteDegree(Integer degree, int min, int max) {
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

    public Integer getValue() {
        return degree;
    }

    public void setDegree(Integer degree) {
        this.degree = degree;
        checkCorrectValue();
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
