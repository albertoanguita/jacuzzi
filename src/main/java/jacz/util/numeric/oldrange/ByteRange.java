package jacz.util.numeric.oldrange;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 15-may-2010<br>
 * Last Modified: 15-may-2010
 */
public class ByteRange extends Range<ByteRange, Byte> implements RangeInterface<ByteRange, Byte> {

    public ByteRange(Byte min, Byte max) {
        super(new ByteRange(min, max, false));
    }

    ByteRange(Byte min, Byte max, boolean b) {
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
    public ByteRange buildInstance(Byte min, Byte max) {
        return new ByteRange(min, max);
    }

    @Override
    public Byte getMin() {
        return min;
    }

    @Override
    public Byte getMax() {
        return max;
    }

    @Override
    public Byte getZero() {
        return (byte) 0;
    }

    @Override
    public Byte previous(Byte value) {
        if (value == null) {
            return null;
        } else {
            return (byte) (value - 1);
        }
    }

    @Override
    public Byte next(Byte value) {
        if (value == null) {
            return null;
        } else {
            return (byte) (value + 1);
        }
    }

    @Override
    public Byte add(Byte value1, Byte value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return (byte) (value1 + value2);
        }
    }

    @Override
    public Byte substract(Byte value1, Byte value2) {
        if (value1 == null || value2 == null) {
            return null;
        } else {
            return (byte) (value1 - value2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteRange) {
            ByteRange range = (ByteRange) obj;
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
