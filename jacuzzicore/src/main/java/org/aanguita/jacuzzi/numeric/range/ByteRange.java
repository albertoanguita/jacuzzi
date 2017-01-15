package org.aanguita.jacuzzi.numeric.range;

/**
 * Created by Alberto on 07/10/2015.
 */
public class ByteRange extends Range<Byte> {

    public ByteRange(Byte min, Byte max) {
        super(min, max, Byte.class);
    }

    @Override
    public ByteRange buildInstance(Byte min, Byte max) {
        return new ByteRange(min, max);
    }

    @Override
    public ByteRange intersection(Range<Byte> range) {
        return (ByteRange) super.intersection(range);
    }
}
