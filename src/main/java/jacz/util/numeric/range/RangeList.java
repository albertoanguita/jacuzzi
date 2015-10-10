package jacz.util.numeric.range;

import java.io.Serializable;
import java.util.*;

/**
 * Generic range list implementation
 */
public class RangeList<T extends Range<Y>, Y extends Number & Comparable<Y>> implements Serializable, Iterable<T> {

    /**
     * Ordered list of the ranges composing this set. No overlapping or in contact ranges can live here (they are
     * merged if needed)
     */
    protected List<T> ranges;


    public RangeList() {
        ranges = new ArrayList<>();
    }

    public RangeList(T initialRange) {
        ranges = new ArrayList<>();
        add(initialRange);
    }

    public RangeList(Collection<T> ranges) {
        this.ranges = new ArrayList<>();
        add(ranges);
    }

    public RangeList(RangeList<T, Y> anotherRangeList) {
        this(anotherRangeList, false);
    }

    protected RangeList(RangeList<T, Y> anotherRangeList, boolean swallow) {
        if (swallow) {
            ranges = anotherRangeList.ranges;
        } else {
            ranges = new ArrayList<>(anotherRangeList.ranges);
        }
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public List<T> getRangesAsList() {
        return new ArrayList<>(ranges);
    }

    public boolean contains(Y value) {
        Integer index = search(value);
        return index != null && index >= 0;
    }

    public Integer search(Y value) {
        return searchBinary(value, 0, ranges.size() - 1);
    }

    private Integer searchBinary(Y value, int min, int max) {
        if (max < min) {
            return -min - 1;
        } else {
            int search = (min + max) / 2;
            Range.ValueComparison comparison = ranges.get(search).compareTo(value);
            switch (comparison) {

                case ANY_EMPTY:
                    // value is null
                    return null;
                case LEFT:
                    // look to the right
                    return searchBinary(value, search + 1, max);
                case RIGHT:
                    // look to the left
                    return searchBinary(value, min, search - 1);
                case CONTAINS:
                    // found!
                    return search;
                default:
                    // cannot happen
                    return null;
            }
        }
    }

    public boolean contains(T range) {
        return search(range) >= 0;
    }

    public int search(T range) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).compareTo(range) == Range.RangeComparison.CONTAINS || ranges.get(i).compareTo(range) == Range.RangeComparison.EQUALS) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Computes the union with a collection of ranges. The result is a list of new ranges
     *
     * @param range range to add
     */
    public void add(T range) {
        // swallow copy of the parameter, to freely modify the list
        if (range.isEmpty()) {
            return;
        }
        int i = 0;
        boolean finished = false;
        boolean checkRightOverlap = false;
        while (!finished && i < ranges.size()) {
            T oneRange = ranges.get(i);
            switch (range.compareTo(oneRange)) {

                case LEFT_NO_CONTACT:
                    // our range must be inserted in this position
                    ranges.add(i, range);
                    finished = true;
                    break;
                case LEFT_CONTACT:
                case LEFT_OVERLAP:
                    // merge our range with this one
                    ranges.set(i, (T) range.buildInstance(range.getMin(), oneRange.getMax()));
                    finished = true;
                    break;
                case EQUALS:
                case INSIDE:
                    finished = true;
                    break;
                case CONTAINS:
                    // replace with ours
                    ranges.set(i, range);
                    finished = true;
                    checkRightOverlap = true;
                    break;
                case RIGHT_OVERLAP:
                case RIGHT_CONTACT:
                    // merge our range with this one
                    ranges.set(i, (T) range.buildInstance(oneRange.getMin(), range.getMax()));
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
            ranges.add(range);
        } else if (checkRightOverlap) {
            // we inserted our range, but it might overlap with others (ours is in i-1)
            if (range.getMax() == null) {
                // the range spans to infinite, remove remaining ranges
                while (ranges.size() > i) {
                    ranges.remove(i);
                }
            } else {
                T insertedRange = ranges.get(i - 1);
                finished = false;
                while (!finished && i < ranges.size()) {
                    T oneRange = ranges.get(i);
                    switch (insertedRange.compareTo(oneRange)) {

                        case ANY_EMPTY:
                        case CONTAINS:
                        case EQUALS:
                        case RIGHT_OVERLAP:
                        case RIGHT_CONTACT:
                        case RIGHT_NO_CONTACT:
                            // remove and continue
                            ranges.remove(i);
                            break;
                        case LEFT_NO_CONTACT:
                            // we found a range to the right, finish
                            finished = true;
                            break;
                        case LEFT_CONTACT:
                        case LEFT_OVERLAP:
                            ranges.set(i, (T) insertedRange.buildInstance(ranges.get(i - 1).getMin(), oneRange.getMax()));
                            ranges.remove(i - 1);
                            finished = true;
                            break;
                        case INSIDE:
                            ranges.remove(i - 1);
                            finished = true;
                            break;
                    }
                }
            }
        }
    }

    public void add(Collection<T> ranges) {
        for (T range : ranges) {
            add(range);
        }
    }

    @SuppressWarnings("unchecked")
    public void add(T... ranges) {
        for (T range : ranges) {
            add(range);
        }
    }

    public void add(RangeList<T, Y> anotherRangeList) {
        for (T range : anotherRangeList.getRangesAsList()) {
            add(range);
        }
    }

    public void remove(T range) {
        int index = searchAffectedRange(range);
        if (index >= 0) {
            if (index == ranges.size()) {
                // the range is out of the scope of this range set
                return;
            }

            // first deal with the segments right of the obtained index. they are either contained or equal to range
            // (up to several) or overlap to the right of range (up to one)
            int secIndex = index + 1;
            while (secIndex < ranges.size() &&
                    range.compareTo(ranges.get(secIndex)) == Range.RangeComparison.CONTAINS) {
                ranges.remove(secIndex);
            }
            if (secIndex < ranges.size() &&
                    (range.compareTo((T) ranges.get(secIndex)) == Range.RangeComparison.LEFT_OVERLAP)) {
                Y min = range.next(range.getMax());
                Y max = ranges.get(secIndex).getMax();
                ranges.set(secIndex, (T) range.buildInstance(min, max));
            }

            // now deal with the range at index. It might have to be deleted, partially deleted, or even split in two
            switch (range.compareTo((T) ranges.get(index))) {

                case LEFT_OVERLAP:
                    Y min = range.next(range.getMax());
                    Y max = ranges.get(index).getMax();
                    ranges.set(index, (T) range.buildInstance(min, max));
                    break;

                case RIGHT_OVERLAP:
                    min = ranges.get(index).getMin();
                    max = range.previous(range.getMin());
                    ranges.set(index, (T) range.buildInstance(min, max));
                    break;

                case INSIDE:
                    Y min1 = ranges.get(index).getMin();
                    Y max1 = range.previous(range.getMin());
                    Y min2 = range.next(range.getMax());
                    Y max2 = ranges.get(index).getMax();
                    ranges.remove(index);
                    if (min1.compareTo(max1) <= 0) {
                        ranges.add(index, (T) range.buildInstance(min1, max1));
                        index++;
                    }
                    if (min2.compareTo(max2) <= 0) {
                        ranges.add(index, (T) range.buildInstance(min2, max2));
                    }
                    break;

                case CONTAINS:
                case EQUALS:
                    ranges.remove(index);
                    break;
            }
        }
    }

    public void remove(List<T> ranges) {
        for (T range : ranges) {
            remove(range);
        }
    }

    @SuppressWarnings("unchecked")
    public void remove(T... ranges) {
        remove(Arrays.asList(ranges));
    }

    public void remove(RangeList<T, Y> anotherRangeList) {
        for (T range : anotherRangeList.getRangesAsList()) {
            remove(range);
        }
    }

    /**
     * Searches the registered range equal or greater than a given range
     *
     * @param range the given range to compare with
     * @return the index of the first found range that is equal (overlaps) or greater than the given range. It
     * always happens that the range at the returned index minus one is to the left of the given range
     * (maybe in contact, but with no overlap)
     * If no range is found, resourceSegments.maxSize() + 1 is returned
     * If the given range is empty, -1 is returned
     */
    private int searchAffectedRange(T range) {
        // perform a binary search to reduce complexity
        int min = 0;
        int max = ranges.size() - 1;
        int mid;
        while (min <= max) {
            mid = (min + max) / 2;
            Range.RangeComparison comp = ranges.get(mid).compareTo(range);
            switch (comp) {
                case LEFT_NO_CONTACT:
                    min = mid + 1;
                    break;

                case RIGHT_NO_CONTACT:
                case RIGHT_CONTACT:
                case RIGHT_OVERLAP:
                case INSIDE:
                    max = mid - 1;
                    break;

                case LEFT_CONTACT:
                    return mid + 1;

                case LEFT_OVERLAP:
                case EQUALS:
                case CONTAINS:
                    return mid;

                case ANY_EMPTY:
                    // the given range is empty
                    return -1;
            }
        }
        return min;
    }

    public RangeList<T, Y> intersection(T range) {
        RangeList<T, Y> intersection = new RangeList<>();
        for (T aRange : ranges) {
            intersection.add((T) aRange.intersection(range));
        }
        return intersection;
    }

    public RangeList<T, Y> intersection(Collection<T> ranges) {
        RangeList<T, Y> intersection = new RangeList<>();
        for (T aRange : ranges) {
            intersection.add(intersection(aRange));
        }
        return intersection;
    }

    @SuppressWarnings("unchecked")
    public RangeList<T, Y> intersection(T... ranges) {
        return intersection(Arrays.asList(ranges));
    }

    public RangeList<T, Y> intersection(RangeList<T, Y> anotherRangeList) {
        return intersection(anotherRangeList.getRangesAsList());
    }

    public Y getPosition(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Negative offset not allowed");
        }
        for (T aRange : ranges) {
            try {
                return aRange.getPosition(offset);
            } catch (IndexOutOfBoundsException e) {
                // check next range
                offset -= aRange.size();
            }
        }
        throw new IndexOutOfBoundsException("Offset not inside range list");
    }

    public Long size() {
        long size = 0;
        for (T range : ranges) {
            if (range.size() == null) {
                return null;
            }
            size += range.size();
        }
        return size;
    }

    public void clear() {
        ranges.clear();
    }

    @Override
    public String toString() {
        return ranges.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RangeList<?, ?> rangeList = (RangeList<?, ?>) o;

        return ranges.equals(rangeList.ranges);
    }

    @Override
    public int hashCode() {
        return ranges.hashCode();
    }

    @Override
    public Iterator<T> iterator() {
        return ranges.iterator();
    }
}
