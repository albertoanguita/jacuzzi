package jacz.util.numeric;

import java.io.Serializable;
import java.util.Map;

/**
 * A map of ranges to generic objects
 */
public class RangeMap<T extends Range<T, Y> & RangeInterface<T, Y>, Y extends Comparable<Y>, Z> implements Serializable {

    /**
     * Ordered list of the ranges composing this set. No overlapping or in contact ranges can live here (they are
     * merged if needed)
     */
    private Map<T, Z> rangeMap;

}
