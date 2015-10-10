package jacz.util.numeric.range;

import java.util.Collection;

/**
 * List of integer ranges
 */
public class IntegerRangeList extends RangeList<IntegerRange, Integer> {

    public IntegerRangeList() {
        super();
    }

    public IntegerRangeList(IntegerRange initialRange) {
        super(initialRange);
    }

    public IntegerRangeList(Collection<IntegerRange> ranges) {
        super(ranges);
    }

    public IntegerRangeList(RangeList<IntegerRange, Integer> anotherRangeList) {
        super(anotherRangeList);
    }

    private IntegerRangeList(RangeList<IntegerRange, Integer> anotherRangeList, boolean swallow) {
        super(anotherRangeList, swallow);
    }

    public IntegerRangeList(Integer... values) {
        this();
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of values required");
        }
        for (int i = 0; i < values.length; i += 2) {
            add(new IntegerRange(values[i], values[i + 1]));
        }
    }

    @Override
    public IntegerRangeList intersection(IntegerRange range) {
        return new IntegerRangeList(super.intersection(range), true);
    }

    @Override
    public IntegerRangeList intersection(RangeList<IntegerRange, Integer> anotherRangeList) {
        return new IntegerRangeList(super.intersection(anotherRangeList), true);
    }

    @Override
    public IntegerRangeList intersection(Collection<IntegerRange> ranges) {
        return new IntegerRangeList(super.intersection(ranges), true);
    }

    @Override
    public IntegerRangeList intersection(IntegerRange... ranges) {
        return new IntegerRangeList(super.intersection(ranges), true);
    }
}
