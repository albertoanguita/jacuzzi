package jacz.util.numeric.newrange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A generic range implementation, for discrete numbers
 */
public class Range<T extends Number & Comparable<T>> implements Serializable {

    public enum ValueComparison {

        ANY_EMPTY,
        LEFT,
        RIGHT,
        CONTAINS,
    }

    public enum RangeComparison {
        // any of the ranges is empty
        ANY_EMPTY,
        // our range is to the left of range, no overlapping and no contact
        LEFT_NO_CONTACT,
        // our range is to the left of range, no overlapping but in contact
        LEFT_CONTACT,
        // our range overlaps with range at its left
        LEFT_OVERLAP,
        // our range is equal to range
        EQUALS,
        // our range lies completely inside range
        INSIDE,
        // our range completely contains range
        CONTAINS,
        // our range overlaps with range, at its right
        RIGHT_OVERLAP,
        // our range is to the right of range, no overlapping but in contact
        RIGHT_CONTACT,
        // our range is to the right of range, no overlapping and no contact
        RIGHT_NO_CONTACT
    }

    private final T min;

    private final T max;

    private final Class<T> clazz;

    protected Range(T min, T max, Class<T> clazz) {
        this.min = min;
        this.max = max;
        this.clazz = clazz;
    }

    public Range(Range<T> range) {
        this.min = range.min;
        this.max = range.max;
        this.clazz = range.clazz;
    }

    protected Range<T> buildInstance(T min, T max) {
        return new Range<T>(min, max, clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range)) return false;

        Range range = (Range) o;

        if (isEmpty() && range.isEmpty()) {
            return true;
        }
        if (min != null && !min.equals(range.min)) {
            return false;
        } else if (min == null && range.min != null) {
            return false;
        }
        if (max != null && !max.equals(range.max)) {
            return false;
        } else if (max == null && range.max != null) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "[";
        if (min != null) {
            str += min.toString();
        } else {
            str += "-inf";
        }
        str += ", ";
        if (max != null) {
            str += max.toString();
        } else {
            str += "+inf";
        }
        str += "]";
        return str;
    }

    public Long size() {
        return max.longValue() - min.longValue() + 1;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public boolean isEmpty() {
        return (min != null && max != null && min.compareTo(max) > 0);
    }


    private T getZero() {

        if (clazz.equals(Byte.class)) {
            return clazz.cast((byte) 0);
        } else if (clazz.equals(Short.class)) {
            return clazz.cast((short) 0);
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(0);
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(0L);
        } else {
            throw new RuntimeException("Invalid Range class");
        }
    }


    T previous(T value) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value.byteValue() - 1);
        } else if (clazz.equals(Short.class)) {
            return clazz.cast(value.shortValue() - 1);
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(value.intValue() - 1);
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(value.longValue() - 1);
        } else {
            throw new RuntimeException("Invalid Range class");
        }
    }

    T next(T value) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value.byteValue() + 1);
        } else if (clazz.equals(Short.class)) {
            return clazz.cast(value.shortValue() + 1);
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(value.intValue() + 1);
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(value.longValue() + 1);
        } else {
            throw new RuntimeException("Invalid Range class");
        }
    }

    T add(T value1, T value2) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value1.byteValue() + value2.byteValue());
        } else if (clazz.equals(Short.class)) {
            return clazz.cast(value1.shortValue() + value2.shortValue());
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(value1.intValue() + value2.intValue());
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(value1.longValue() + value2.longValue());
        } else {
            throw new RuntimeException("Invalid Range class");
        }
    }

    T subtract(T value1, T value2) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value1.byteValue() - value2.byteValue());
        } else if (clazz.equals(Short.class)) {
            return clazz.cast(value1.shortValue() - value2.shortValue());
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(value1.intValue() - value2.intValue());
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(value1.longValue() - value2.longValue());
        } else {
            throw new RuntimeException("Invalid Range class");
        }
    }

    Range<T> generateEmptyRange() {
        return buildInstance(next(getZero()), getZero());
    }

    public boolean contains(T value) {
        return compareTo(value) == ValueComparison.CONTAINS;
    }

    public ValueComparison compareTo(T value) {
        if (value == null || isEmpty()) {
            return ValueComparison.ANY_EMPTY;
        }
        int leftComp;
        if (min != null && min.compareTo(value) > 0) {
            return ValueComparison.RIGHT;
        } else if (max != null && max.compareTo(value) < 0) {
            return ValueComparison.LEFT;
        } else {
            return ValueComparison.CONTAINS;
        }
    }


    /**
     * Indicates the way our given range compares with a given rage. The result is a Comparison value, indicating how
     * our range places <u>with respect</u> to the given range. Example: if we use integer ranges, and our range
     * is [1,2], and we compare it to [4,5], the result will be Comparison.LEFT_NO_CONTACT.
     * <p/>
     *
     * @param range the range to test with our range
     * @return range comparison result, given by a RangeComparison value
     */
    public RangeComparison compareTo(Range<T> range) {
        // Tested carefully, all OK. 18-08-2010 by Alberto.

        if (isEmpty() || range.isEmpty()) {
            return RangeComparison.ANY_EMPTY;
        }
        // comparison of my min value with range.min and range.max
        int leftLeftComp;
        if (min == null && range.min == null) {
            leftLeftComp = 0;
        } else if (min == null) {
            leftLeftComp = -1;
        } else if (range.min == null) {
            leftLeftComp = 1;
        } else {
            leftLeftComp = min.compareTo(range.min);
        }
        int leftRightComp;
        if (min == null || range.max == null) {
            leftRightComp = -1;
        } else {
            leftRightComp = min.compareTo(range.max);
        }
        // comparison of my max value with range.min and range.max
        int rightRightComp;
        if (max == null && range.max == null) {
            rightRightComp = 0;
        } else if (max == null) {
            rightRightComp = 1;
        } else if (range.max == null) {
            rightRightComp = -1;
        } else {
            rightRightComp = max.compareTo(range.max);
        }
        int rightLeftComp;
        if (max == null || range.min == null) {
            rightLeftComp = 1;
        } else {
            rightLeftComp = max.compareTo(range.min);
        }

        if (leftLeftComp == 0 && rightRightComp == 0) {
            return RangeComparison.EQUALS;
        }
        if (rightLeftComp < 0) {
            // left
            if (next(max).equals(range.min)) {
                return RangeComparison.LEFT_CONTACT;
            } else {
                return RangeComparison.LEFT_NO_CONTACT;
            }
        }
        if (leftLeftComp < 0 && rightRightComp < 0 & rightLeftComp >= 0) {
            return RangeComparison.LEFT_OVERLAP;
        }
        if (leftLeftComp >= 0 && rightRightComp <= 0) {
            return RangeComparison.INSIDE;
        }
        if (leftLeftComp <= 0 && rightRightComp >= 0) {
            return RangeComparison.CONTAINS;
        }
        if (leftLeftComp > 0 && rightRightComp > 0 & leftRightComp <= 0) {
            return RangeComparison.RIGHT_OVERLAP;
        }
        if (leftRightComp > 0) {
            // right
            if (next(range.max).equals(min)) {
                return RangeComparison.RIGHT_CONTACT;
            } else {
                return RangeComparison.RIGHT_NO_CONTACT;
            }
        }
        return null;
    }

    /**
     * Computes the intersection with a given range. The result is a new range.
     *
     * @param range range to compute intersection with
     * @return the resulting intersected range
     */
    public Range<T> intersection(Range<T> range) {
        RangeComparison comparison = compareTo(range);
        switch (comparison) {

            case ANY_EMPTY:
            case LEFT_NO_CONTACT:
            case LEFT_CONTACT:
            case RIGHT_CONTACT:
            case RIGHT_NO_CONTACT:
                return generateEmptyRange();
            case LEFT_OVERLAP:
                return buildInstance(range.min, max);
            case EQUALS:
                return buildInstance(min, max);
            case INSIDE:
                return buildInstance(min, max);
            case CONTAINS:
                return buildInstance(range.min, range.max);
            case RIGHT_OVERLAP:
                return buildInstance(min, range.max);
            default:
                return null;
        }
    }

    /**
     * Computes the intersection with a given collection of ranges. The result is a range.
     *
     * @param ranges ranges to compute intersection with
     * @return the resulting list of intersected ranges
     */
    public Range<T> intersection(Collection<Range<T>> ranges) {
        Range<T> intersectionRange = this;
        for (Range<T> oneRange : ranges) {
            intersectionRange = intersectionRange.intersection(oneRange);
        }
        return intersectionRange;
    }

    /**
     * Static implementation of the intersection method. Computes the intersection of a collection of ranges between
     * themselves (these do not need to be ordered). The result is a new range.
     *
     * @param ranges collection of ranges to intersect. Must be non-empty
     * @param <T>    the type of the ranges
     * @return the resulting list of intersected ranges
     * @throws IllegalArgumentException if the given list of ranges is empty
     */
    public static <T extends Number & Comparable<T>> Range<T> intersectionStatic(Collection<Range<T>> ranges) throws IllegalArgumentException {
        if (!ranges.isEmpty()) {
            Iterator<Range<T>> it = ranges.iterator();
            Range<T> oneRange = it.next();
            it.remove();
            return oneRange.intersection(ranges);
        } else {
            throw new IllegalArgumentException("Collection of ranges must have at least one element");
        }
    }

    /**
     * Computes the union with another range. The result is a list of new ranges (2 at most)
     *
     * @param range range to compute union with
     * @return the resulting list of unioned ranges
     */
    public List<Range<T>> union(Range<T> range) {
        List<Range<T>> rangeList = new ArrayList<>();
        rangeList.add(range);
        return union(rangeList);
    }

    /**
     * Computes the union with a collection of ranges. The result is a list of new ranges
     *
     * @param ranges collection of ranges to compute union with
     * @return the resulting list of union ranges
     */
    public List<Range<T>> union(List<Range<T>> ranges) {
        // swallow copy of the parameter, to freely modify the list
        List<Range<T>> unionRanges = new ArrayList<>(ranges);
        if (isEmpty()) {
            return unionRanges;
        }
        // todo remove empty ranges
        int i = 0;
        boolean finished = false;
        boolean checkRightOverlap = false;
        while (!finished && i < unionRanges.size()) {
            Range<T> oneRange = ranges.get(i);
            switch (compareTo(oneRange)) {

                case ANY_EMPTY:
                    // this range is empty -> remove and continue
                    unionRanges.remove(i);
                    continue;
                case LEFT_NO_CONTACT:
                    // our range must be inserted in this position
                    unionRanges.add(i, this);
                    finished = true;
                    break;
                case LEFT_CONTACT:
                case LEFT_OVERLAP:
                    // merge our range with this one
                    unionRanges.set(i, buildInstance(min, oneRange.max));
                    finished = true;
                    break;
                case EQUALS:
                case INSIDE:
                    finished = true;
                    break;
                case CONTAINS:
                    // replace with ours
                    unionRanges.set(i, this);
                    finished = true;
                    checkRightOverlap = true;
                    break;
                case RIGHT_OVERLAP:
                case RIGHT_CONTACT:
                    // merge our range with this one
                    unionRanges.set(i, buildInstance(oneRange.min, max));
                    finished = true;
                    checkRightOverlap = true;
                    break;
                case RIGHT_NO_CONTACT:
                    // position not found yet, keep searching
                    break;
            }
            i++;
        }
        if (!finished) {
            // no position was found, place it at the end
            unionRanges.add(this);
        } else if (checkRightOverlap) {
            // we inserted our range, but it might overlap with others (ours is in i-1)
            if (max == null) {
                // we span to infinite, remove remaining ranges
                while (unionRanges.size() > i + 1) {
                    unionRanges.remove(i + 1);
                }
            } else {
                Range<T> insertedRange = unionRanges.get(i - 1);
                finished = false;
                while (!finished && i < unionRanges.size()) {
                    Range<T> oneRange = unionRanges.get(i);
                    switch (insertedRange.compareTo(oneRange)) {

                        case ANY_EMPTY:
                        case CONTAINS:
                        case EQUALS:
                        case RIGHT_OVERLAP:
                        case RIGHT_CONTACT:
                        case RIGHT_NO_CONTACT:
                            // remove and continue
                            unionRanges.remove(i);
                            break;
                        case LEFT_NO_CONTACT:
                            // we found a range to the right, finish
                            finished = true;
                            break;
                        case LEFT_CONTACT:
                        case LEFT_OVERLAP:
                            unionRanges.set(i, buildInstance(unionRanges.get(i - 1).min, oneRange.max));
                            unionRanges.remove(i - 1);
                            finished = true;
                            break;
                        case INSIDE:
                            unionRanges.remove(i - 1);
                            finished = true;
                            break;
                    }
                }
            }
        }
        return unionRanges;
    }

    public static <T extends Number & Comparable<T>> List<Range<T>> unionStatic(Collection<Range<T>> ranges) {
        List<Range<T>> union = new ArrayList<>();
        Iterator<Range<T>> it = ranges.iterator();
        while (it.hasNext()) {
            Range<T> oneRange = it.next();
            it.remove();
            union = oneRange.union(union);
        }
        return union;
    }

    public List<Range<T>> subtract(Range<T> range) {
        List<Range<T>> subtractList = new ArrayList<>();
        switch (compareTo(range)) {

            case ANY_EMPTY:
            case LEFT_NO_CONTACT:
            case LEFT_CONTACT:
            case RIGHT_CONTACT:
            case RIGHT_NO_CONTACT:
                subtractList.add(this);
                break;
            case LEFT_OVERLAP:
                subtractList.add(buildInstance(min, previous(range.min)));
                break;
            case EQUALS:
            case INSIDE:
                subtractList.add(generateEmptyRange());
                break;
            case CONTAINS:
                subtractList.add(buildInstance(min, previous(range.min)));
                subtractList.add(buildInstance(next(range.max), max));
                break;
            case RIGHT_OVERLAP:
                subtractList.add(buildInstance(next(range.max), max));
                break;
        }
        return subtractList;
    }

    public List<Range<T>> subtract(Collection<Range<T>> ranges) {
        List<Range<T>> subtractList = new ArrayList<>();
        subtractList.add(this);
        for (Range<T> range : ranges) {
            List<Range<T>> subtractListAux = new ArrayList<>();
            for (Range<T> subtractedRange : subtractList) {
                subtractListAux.addAll(subtractedRange.subtract(range));
            }
            subtractList = subtractListAux;
        }
        return subtractList;
    }


    public static void main(String[] args) {
        List<Range<Integer>> ranges = new ArrayList<>();
        ranges.add(new Range<>(-5, -1, Integer.class));
        ranges.add(new Range<>(5, 6, Integer.class));
        ranges.add(new Range<>(5, -4, Integer.class));
        ranges.add(new Range<>(10, 16, Integer.class));
        ranges.add(new Range<>(25, 26, Integer.class));
        ranges.add(new Range<>(27, 36, Integer.class));
        ranges.add(new Range<>(45, 50, Integer.class));

        List<Range<Integer>> mergedRanges = new Range<>(0, 9, Integer.class).union(ranges);
        System.out.println(mergedRanges);

        System.out.println("END");
    }

}
