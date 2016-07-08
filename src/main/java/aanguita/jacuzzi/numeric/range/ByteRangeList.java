package aanguita.jacuzzi.numeric.range;

import java.util.Collection;

/**
 * Byte range list
 */
public class ByteRangeList extends RangeList<ByteRange, Byte> {

    public ByteRangeList() {
        super();
    }

    public ByteRangeList(ByteRange initialRange) {
        super(initialRange);
    }

    public ByteRangeList(Collection<ByteRange> ranges) {
        super(ranges);
    }

    public ByteRangeList(RangeList<ByteRange, Byte> anotherRangeList) {
        super(anotherRangeList);
    }

    private ByteRangeList(RangeList<ByteRange, Byte> anotherRangeList, boolean swallow) {
        super(anotherRangeList, swallow);
    }

    public ByteRangeList(Byte... values) {
        this();
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of values required");
        }
        for (int i = 0; i < values.length; i += 2) {
            add(new ByteRange(values[i], values[i + 1]));
        }
    }

    @Override
    public ByteRangeList intersection(ByteRange range) {
        return new ByteRangeList(super.intersection(range), true);
    }

    @Override
    public ByteRangeList intersection(RangeList<ByteRange, Byte> anotherRangeList) {
        return new ByteRangeList(super.intersection(anotherRangeList), true);
    }

    @Override
    public ByteRangeList intersection(Collection<ByteRange> ranges) {
        return new ByteRangeList(super.intersection(ranges), true);
    }

    @Override
    public ByteRangeList intersection(ByteRange... ranges) {
        return new ByteRangeList(super.intersection(ranges), true);
    }
}
