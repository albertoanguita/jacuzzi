package org.aanguita.jacuzzi.numeric.range;

/**
 * Range of integers
 */
public class IntegerRange extends Range<Integer> {

    public IntegerRange(Integer min, Integer max) {
        super(min, max, Integer.class);
    }

    @Override
    public IntegerRange buildInstance(Integer min, Integer max) {
        return new IntegerRange(min, max);
    }

    @Override
    public IntegerRange intersection(Range<Integer> range) {
        return (IntegerRange) super.intersection(range);
    }
}
