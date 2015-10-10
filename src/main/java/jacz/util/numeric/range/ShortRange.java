package jacz.util.numeric.range;

/**
 * Created by Alberto on 07/10/2015.
 */
public class ShortRange extends Range<Short> {

    public ShortRange(Short min, Short max) {
        super(min, max, Short.class);
    }

    @Override
    public ShortRange buildInstance(Short min, Short max) {
        return new ShortRange(min, max);
    }

    @Override
    public ShortRange intersection(Range<Short> range) {
        return (ShortRange) super.intersection(range);
    }
}
