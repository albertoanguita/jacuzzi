package aanguita.jacuzzi.numeric.range;

/**
 * Range of longs
 */
public class LongRange extends Range<Long> {

    public LongRange(Long min, Long max) {
        super(min, max, Long.class);
    }

    @Override
    public LongRange buildInstance(Long min, Long max) {
        return new LongRange(min, max);
    }

    @Override
    public LongRange intersection(Range<Long> range) {
        return (LongRange) super.intersection(range);
    }
}
