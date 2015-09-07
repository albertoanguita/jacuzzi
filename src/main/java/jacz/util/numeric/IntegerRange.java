package jacz.util.numeric;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 25-mar-2007<br>
 * Last Modified: 25-mar-2007
 */
public class IntegerRange extends Range<IntegerRange, Integer> implements RangeInterface<IntegerRange, Integer> {

    public IntegerRange(Integer min, Integer max) {
        super(new IntegerRange(min, max, false));
    }

    IntegerRange(Integer min, Integer max, boolean b) {
        super();
        this.min = min;
        this.max = max;
    }

    @Override
    public Long size() {
        if (isEmpty()) {
            return (long) 0;
        } else {
            return (long) max - min + 1;
        }
    }

    @Override
    public IntegerRange buildInstance(Integer min, Integer max) {
        return new IntegerRange(min, max);
    }

    @Override
    public Integer getMin() {
        return min;
    }

    @Override
    public Integer getMax() {
        return max;
    }

    @Override
    public Integer getZero() {
        return 0;
    }

    @Override
    public Integer previous(Integer value) {
        if (value == null) {
            return null;
        } else {
            return (value - 1);
        }
    }

    @Override
    public Integer next(Integer value) {
        if (value == null) {
            return null;
        } else {
            return (value + 1);
        }
    }

    @Override
    public Integer add(Integer value1, Integer value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return value1 + value2;
        }
    }

    @Override
    public Integer substract(Integer value1, Integer value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return value1 - value2;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerRange) {
            IntegerRange range = (IntegerRange) obj;
            if (isEmpty() && range.isEmpty()) {
                return true;
            } else if ((isEmpty() && !range.isEmpty()) || (!isEmpty() && range.isEmpty())) {
                return false;
            } else {
                boolean minEqual =
                        (min == null && range.min == null) ||
                                (min != null && range.min != null && min.equals(range.min));
                boolean maxEqual =
                        (max == null && range.max == null) ||
                                (max != null && range.max != null && max.equals(range.max));
                return minEqual && maxEqual;
            }
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        IntegerRange r1 = new IntegerRange(7, 2);
        IntegerRange r2 = new IntegerRange(6, 5);

        System.out.println(r1.equals(r2));
    }
}
