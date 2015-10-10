package jacz.util.numeric.oldrange;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-may-2010<br>
 * Last Modified: 16-may-2010
 */
public class LongRange extends Range<LongRange, Long> implements RangeInterface<LongRange, Long> {


    public LongRange(Long min, Long max) {
        super(new LongRange(min, max, false));
    }

    LongRange(Long min, Long max, boolean b) {
        super();
        this.min = min;
        this.max = max;
    }

    @Override
    public Long size() {
        if (isEmpty()) {
            return (long) 0;
        } else {
            return max - min + 1;
        }
    }

    @Override
    public LongRange buildInstance(Long min, Long max) {
        return new LongRange(min, max);
    }

    @Override
    public Long getMin() {
        return min;
    }

    @Override
    public Long getMax() {
        return max;
    }

    @Override
    public Long getZero() {
        return (long) 0;
    }

    @Override
    public Long previous(Long value) {
        if (value == null) {
            return null;
        } else {
            return value - 1;
        }
    }

    @Override
    public Long next(Long value) {
        if (value == null) {
            return null;
        } else {
            return value + 1;
        }
    }

    @Override
    public Long add(Long value1, Long value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return value1 + value2;
        }
    }

    @Override
    public Long substract(Long value1, Long value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return value1 - value2;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LongRange) {
            LongRange range = (LongRange) obj;
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
