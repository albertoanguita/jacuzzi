package jacz.util.numeric.oldrange2;

/**
 * Created by Alberto on 07/10/2015.
 */
public interface RangeInterface<T extends Number & Comparable<T>> {

    RangeInterface<T> buildInstance(T min, T max);

    @Override
    boolean equals(Object o);

    @Override
    String toString();

    Long size();

    T getMin();

    T getMax();

    boolean isEmpty();

    T previous(T value);

    T next(T value);

    boolean contains(T value);

    Range.ValueComparison compareTo(T value);


    /**
     * Indicates the way our given range compares with a given rage. The result is a Comparison value, indicating how
     * our range places <u>with respect</u> to the given range. Example: if we use integer ranges, and our range
     * is [1,2], and we compare it to [4,5], the result will be Comparison.LEFT_NO_CONTACT.
     * <p/>
     *
     * @param range the range to test with our range
     * @return range comparison result, given by a RangeComparison value
     */
    Range.RangeComparison compareTo(RangeInterface<T> range);

    /**
     * Computes the intersection with a given range. The result is a new range.
     *
     * @param range range to compute intersection with
     * @return the resulting intersected range
     */
    Range<T> intersection(Range<T> range);

    /**
     * Computes the intersection with a given collection of ranges. The result is a range.
     *
     * @param ranges ranges to compute intersection with
     * @return the resulting list of intersected ranges
     */
    RangeList<T> intersection(RangeList<T> ranges);

    /**
     * Computes the union with another range. The result is a list of new ranges (2 at most)
     *
     * @param range range to compute union with
     * @return the resulting list of unioned ranges
     */
    RangeList<T> union(Range<T> range);

    /**
     * Computes the union with a collection of ranges. The result is a list of new ranges
     *
     * @param ranges collection of ranges to compute union with
     * @return the resulting list of union ranges
     */
    RangeList<T> union(RangeList<T> ranges);

    RangeList<T> subtract(Range<T> range);

    RangeList<T> subtract(RangeList<T> ranges);

    T getPosition(long offset);
}
