package aanguita.jacuzzi.numeric.range;

import java.util.Collection;

/**
 * Short range list
 */
public class ShortRangeList extends RangeList<ShortRange, Short> {

    public ShortRangeList() {
        super();
    }

    public ShortRangeList(ShortRange initialRange) {
        super(initialRange);
    }

    public ShortRangeList(Collection<ShortRange> ranges) {
        super(ranges);
    }

    public ShortRangeList(RangeList<ShortRange, Short> anotherRangeList) {
        super(anotherRangeList);
    }

    private ShortRangeList(RangeList<ShortRange, Short> anotherRangeList, boolean swallow) {
        super(anotherRangeList, swallow);
    }

    public ShortRangeList(Short... values) {
        this();
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Even number of values required");
        }
        for (int i = 0; i < values.length; i += 2) {
            add(new ShortRange(values[i], values[i + 1]));
        }
    }

    @Override
    public ShortRangeList intersection(ShortRange range) {
        return new ShortRangeList(super.intersection(range), true);
    }

    @Override
    public ShortRangeList intersection(RangeList<ShortRange, Short> anotherRangeList) {
        return new ShortRangeList(super.intersection(anotherRangeList), true);
    }

    @Override
    public ShortRangeList intersection(Collection<ShortRange> ranges) {
        return new ShortRangeList(super.intersection(ranges), true);
    }

    @Override
    public ShortRangeList intersection(ShortRange... ranges) {
        return new ShortRangeList(super.intersection(ranges), true);
    }
}
