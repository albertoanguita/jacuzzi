package org.aanguita.jacuzzi.numeric.range;

import java.util.Collection;

/**
 * List of long ranges
 */
public class LongRangeList extends RangeList<LongRange, Long> {

    public LongRangeList() {
        super();
    }

    public LongRangeList(LongRange initialRange) {
        super(initialRange);
    }

    public LongRangeList(Collection<LongRange> ranges) {
        super(ranges);
    }

    public LongRangeList(RangeList<LongRange, Long> anotherRangeList) {
        super(anotherRangeList);
    }

    protected LongRangeList(RangeList<LongRange, Long> anotherRangeList, boolean swallow) {
        super(anotherRangeList, swallow);
    }

    public LongRangeList(Long... values) {
        this();
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of values required");
        }
        for (int i = 0; i < values.length; i += 2) {
            add(new LongRange(values[i], values[i + 1]));
        }
    }

    @Override
    public LongRangeList intersection(LongRange range) {
        return new LongRangeList(super.intersection(range), true);
    }

    @Override
    public LongRangeList intersection(RangeList<LongRange, Long> anotherRangeList) {
        return new LongRangeList(super.intersection(anotherRangeList), true);
    }

    @Override
    public LongRangeList intersection(Collection<LongRange> ranges) {
        return new LongRangeList(super.intersection(ranges), true);
    }

    @Override
    public LongRangeList intersection(LongRange... ranges) {
        return new LongRangeList(super.intersection(ranges), true);
    }
}
