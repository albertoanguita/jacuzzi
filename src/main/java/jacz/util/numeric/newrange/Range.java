package jacz.util.numeric.newrange;

import jacz.util.numeric.RangeToValueComparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Alberto on 21/09/2015.
 */
public class Range<T extends Number & Comparable<T>> {

    public enum RangeComparison {
        ANY_EMPTY,
        LEFT_NO_CONTACT,
        LEFT_CONTACT,
        LEFT_OVERLAP,
        EQUALS,
        INSIDE,
        CONTAINS,
        RIGHT_OVERLAP,
        RIGHT_CONTACT,
        RIGHT_NO_CONTACT
    }

    private final T min;

    private final T max;

    private final Class<T> clazz;

    public Range(T min, T max, Class<T> clazz) {
        this.min = min;
        this.max = max;
        this.clazz = clazz;
    }

    public Range(Range<T> range) {
        this.min = range.min;
        this.max = range.max;
        this.clazz = range.clazz;
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


    public T getZero() {

        if (clazz.equals(Byte.class)) {
            return clazz.cast((byte) 0);
        }

        return clazz.cast(0);

    }


    T getPrevious(T value) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value.byteValue() - 1);
        }
        return null;
    }

    T next(T value) {
        if (clazz.equals(Byte.class)) {
            return clazz.cast(value.byteValue() + 1);
        }
        return null;
    }

    private Range<T> generateEmptyRange() {
        return new Range<>(next(getZero()), getZero(), clazz);
    }

    public boolean contains(T value) {
        return compareTo(value) == RangeToValueComparison.CONTAINS;
    }

    public RangeToValueComparison compareTo(T value) {
        if (value == null || isEmpty()) {
            return RangeToValueComparison.ANY_EMPTY;
        }
        int leftComp;
        if (min != null && min.compareTo(value) > 0) {
            return RangeToValueComparison.RIGHT;
        } else if (max != null && max.compareTo(value) < 0) {
            return RangeToValueComparison.LEFT;
        } else {
            return RangeToValueComparison.CONTAINS;
        }
    }


    /**
     * Indicates the way our given range compares with a given rage. The result is a Comparison value, indicating how
     * our range places <u>with respect</u> to the given range. Example: if we use integer ranges, and our range
     * is [1,2], and we compare it to [4,5], the result will be Comparison.LEFT_NO_CONTACT.
     * <p/>
     *
     * @param range the range to test with our range
     * @return an integer value indicating the different overlapping possibilities:
     * <ul>
     * <li>ANY_EMPTY if any of the ranges is isEmpty</li>
     * <li>LEFT_NO_CONTACT if our range is to the left of range, no overlapping and no contact</li>
     * <li>LEFT_CONTACT if our range is to the left of range, no overlapping but in contact</li>
     * <li>LEFT_OVERLAP if our range overlaps with range to the left</li>
     * <li>INSIDE if our range lies completely inside range</li>
     * <li>EQUALS if our range is equal to range</li>
     * <li>CONTAINS if our range completely contains range</li>
     * <li>RIGHT_OVERLAP if our range overlaps with range to the right</li>
     * <li>RIGHT_CONTACT if our range is to the right of range, no overlapping but in contact</li>
     * <li>RIGHT_NO_CONTACT if our range is to the right of range, no overlapping and no contact</li>
     * </ul>
     */
    public RangeComparison compareTo(Range<T> range) {
        // Tested carefully, all OK. 18-08-2010 by Alberto.

        if (isEmpty() || range.isEmpty()) {
            return RangeComparison.ANY_EMPTY;
        }
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
                return new Range<>(range.min, max, clazz);
            case EQUALS:
                return new Range<>(min, max, clazz);
            case INSIDE:
                return new Range<>(min, max, clazz);
            case CONTAINS:
                return new Range<>(range.min, range.max, clazz);
            case RIGHT_OVERLAP:
                return new Range<>(min, range.max, clazz);
            default:
                return null;
        }
    }

    public List<Range<T>> intersection(Collection<Range<T>> ranges) {
        List<Range<T>> intersectionList = new ArrayList<>();
        if (isEmpty()) {
            intersectionList.add(generateEmptyRange());
        } else {
            for (Range<T> oneRange : ranges) {
//                merge(intersectionList, intersection(oneRange));
            }
            if (intersectionList.size() == 0) {
                intersectionList.add(generateEmptyRange());
            }
        }
        return intersectionList;
    }

    public static <T extends Number & Comparable<T>> List<Range<T>> intersectionStat(Collection<Range<T>> ranges) {
        List<Range<T>> intersectionList = new ArrayList<>();
        if (!ranges.isEmpty()) {
            Iterator<Range<T>> it = ranges.iterator();
            Range<T> oneRange = it.next();
            it.remove();
            return oneRange.intersection(ranges);
        }
        return intersectionList;
    }

    public List<Range<T>> union(Range<T> range) {
        List<Range<T>> unionList = new ArrayList<>();
        if (isEmpty()) {
            unionList.add(new Range<T>(range));
        } else if (range.isEmpty()) {
            unionList.add(new Range<T>(this));
        } else {
            RangeComparison comparison = compareTo(range);
            switch (comparison) {

                case LEFT_NO_CONTACT:
                    unionList.add(new Range<>(this));
                    unionList.add(new Range<>(range));
                case LEFT_CONTACT:
                    unionList.add(new Range<>(min, range.max, clazz));
                case RIGHT_CONTACT:
                    unionList.add(new Range<>(range.min, max, clazz));
                case RIGHT_NO_CONTACT:
                    unionList.add(new Range<>(range));
                    unionList.add(new Range<>(this));
                case LEFT_OVERLAP:
                    unionList.add(new Range<>(min, range.max, clazz));
                case EQUALS:
                    unionList.add(new Range<>(this));
                case INSIDE:
                    unionList.add(new Range<>(range));
                case CONTAINS:
                    unionList.add(new Range<>(this));
                case RIGHT_OVERLAP:
                    unionList.add(new Range<>(range.min, max, clazz));
                default:
                    return null;
            }
        }
        return unionList;
    }

//    public List<Range<T>> union(Collection<Range<T>> ranges) {
//
//    }

//    public static <T extends Number & Comparable<T>> List<Range<T>> unionStat(Collection<Range<T>> ranges) {
//        List<Range<T>> unionList = new ArrayList<>();
//        if (!ranges.isEmpty()) {
//            Iterator<Range<T>> it = ranges.iterator();
//            Range<T> oneRange = it.next();
//            it.remove();
//            return oneRange.union(ranges);
//        }
//        return unionList;
//    }

//    public List<Range<T>> subtract(Range<T> range) {
//        List<Range<T>> rangeList = new ArrayList<>();
//        int overlapping = overlapping(range);
//        if (Math.abs(overlapping) >= 3) {
//            rangeList.add(initialRange);
//        } else if (overlapping == -2) {
//            rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
//        } else if (overlapping == 2) {
//            rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
//        } else if (overlapping == -1) {
//            if (min.equals(range.getMin())) {
//                rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
//            } else if (max.equals(range.getMax())) {
//                rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
//            } else {
//                rangeList.add(initialRange.buildInstance(min, initialRange.previous(range.getMin())));
//                rangeList.add(initialRange.buildInstance(initialRange.next(range.getMax()), max));
//            }
//        }
//        return rangeList;
//    }


    public static void main(String[] args) {

        Range<Byte> range = new Range<>((byte) -1, (byte) 3, Byte.class);

        byte zero = range.getZero();

        System.out.println("END");
    }

}
