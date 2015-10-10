package jacz.util.numeric.oldrange;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 15-may-2010<br>
 * Last Modified: 15-may-2010
 */
public class ShortRange extends Range<ShortRange, Short> implements RangeInterface<ShortRange, Short> {

    public ShortRange(Short min, Short max) {
        super(new ShortRange(min, max, false));
    }

    ShortRange(Short min, Short max, boolean b) {
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
    public ShortRange buildInstance(Short min, Short max) {
        return new ShortRange(min, max);
    }

    @Override
    public Short getMin() {
        return min;
    }

    @Override
    public Short getMax() {
        return max;
    }

    @Override
    public Short getZero() {
        return (short) 0;
    }

    @Override
    public Short previous(Short value) {
        if (value == null) {
            return null;
        } else {
            return (short) (value - 1);
        }
    }

    @Override
    public Short next(Short value) {
        if (value == null) {
            return null;
        } else {
            return (short) (value + 1);
        }
    }

    @Override
    public Short add(Short value1, Short value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return (short) (value1 + value2);
        }
    }

    @Override
    public Short substract(Short value1, Short value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return (short) (value1 - value2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShortRange) {
            ShortRange range = (ShortRange) obj;
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
}
