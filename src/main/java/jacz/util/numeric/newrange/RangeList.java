package jacz.util.numeric.newrange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Ordered list of non-overlapping ranges
 */
public class RangeList<T extends Number & Comparable<T>> implements Serializable {

    /**
     * Ordered list of the ranges composing this set. No overlapping or in contact ranges can live here (they are
     * merged if needed)
     */
    private List<Range<T>> ranges;


    public RangeList() {
        ranges = new ArrayList<>();
    }

    public RangeList(Range<T> initialRange) {
        ranges = new ArrayList<>(1);
        ranges.add(initialRange);
    }

    public RangeList(Collection<Range<T>> ranges) {
        this.ranges = new ArrayList<>();
        add(ranges);
    }

    public RangeList(RangeList<T> anotherRangeList) {
        ranges = new ArrayList<>(anotherRangeList.ranges);
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public List<Range<T>> getRangesAsList() {
        return new ArrayList<>(ranges);
    }

    // todo make contains methods use binary search

    public boolean contains(T value) {
        return searchRange(value) >= 0;
    }

    public int searchRange(T value) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).contains(value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Range<T> range) {
        return searchRange(range) >= 0;
    }

    public int searchRange(Range<T> range) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).compareTo(range) == Range.RangeComparison.CONTAINS || ranges.get(i).compareTo(range) == Range.RangeComparison.EQUALS) {
                return i;
            }
        }
        return -1;
    }

    public void add(Range<T> range) {
        int index = searchAffectedRange(range);
        if (index >= 0) {
            ranges.add(index, range);
            merge(index);
        }
    }

    /**
     * Merge the ranges (required after the insertion of a new range)
     *
     * @param index the index of the range that was just inserted
     */
    private void merge(int index) {
        Range<T> rangeToMerge = ranges.get(index);
        // check contact with a segment of the left
        if (index > 0 && ranges.get(index - 1).compareTo(rangeToMerge) == Range.RangeComparison.LEFT_CONTACT) {
            T min = ranges.get(index - 1).getMin();
            T max = rangeToMerge.getMax();
            ranges.set(index, rangeToMerge.buildInstance(min, max));
            ranges.remove(index - 1);
            index--;
            rangeToMerge = ranges.get(index);
        }
        // check contact, overlapping and containment with segments on the right
        int secIndex = index + 1;
        while (secIndex < ranges.size() && rangeToMerge.compareTo(ranges.get(secIndex)) != Range.RangeComparison.LEFT_NO_CONTACT) {
            secIndex++;
        }
        // if secIndex is bigger than (index + 1) then some segments need to be merged (until segment at secIndex - 1).
        if (secIndex > index + 1) {
            T min = rangeToMerge.getMin();
            T max;
            max = rangeToMerge.getMax().compareTo(ranges.get(secIndex - 1).getMax()) >= 0 ? rangeToMerge.getMax() : ranges.get(secIndex - 1).getMax();
            ranges.set(index, rangeToMerge.buildInstance(min, max));
            for (int i = index + 1; i < secIndex; i++) {
                ranges.remove(index + 1);
            }
        }
    }

    public void add(Collection<Range<T>> ranges) {
        for (Range<T> range : ranges) {
            add(range);
        }
    }

    @SafeVarargs
    public final void add(Range<T>... ranges) {
        add(Arrays.asList(ranges));
    }

    public void add(RangeList<T> anotherRangeList) {
        for (Range<T> range : anotherRangeList.getRangesAsList()) {
            add(range);
        }
    }

    public void remove(Range<T> range) {
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
                    (range.compareTo(ranges.get(secIndex)) == Range.RangeComparison.LEFT_OVERLAP)) {
                T min = range.next(range.getMax());
                T max = ranges.get(secIndex).getMax();
                ranges.set(secIndex, range.buildInstance(min, max));
            }

            // now deal with the range at index. It might have to be deleted, partially deleted, or even split in two
            switch (range.compareTo(ranges.get(index))) {

                case LEFT_OVERLAP:
                    T min = range.next(range.getMax());
                    T max = ranges.get(index).getMax();
                    ranges.set(index, range.buildInstance(min, max));
                    break;

                case RIGHT_OVERLAP:
                    min = ranges.get(index).getMin();
                    max = range.previous(range.getMin());
                    ranges.set(index, range.buildInstance(min, max));
                    break;

                case INSIDE:
                    T min1 = ranges.get(index).getMin();
                    T max1 = range.previous(range.getMin());
                    T min2 = range.next(range.getMax());
                    T max2 = ranges.get(index).getMax();
                    ranges.remove(index);
                    if (min1.compareTo(max1) <= 0) {
                        ranges.add(index, range.buildInstance(min1, max1));
                        index++;
                    }
                    if (min2.compareTo(max2) <= 0) {
                        ranges.add(index, range.buildInstance(min2, max2));
                    }
                    break;

                case CONTAINS:
                case EQUALS:
                    ranges.remove(index);
                    break;
            }
        }
    }

    public void remove(List<Range<T>> ranges) {
        for (Range<T> range : ranges) {
            remove(range);
        }
    }

    @SafeVarargs
    public final void remove(Range<T>... ranges) {
        remove(Arrays.asList(ranges));
    }

    public void remove(RangeList<T> anotherRangeList) {
        for (Range<T> range : anotherRangeList.getRangesAsList()) {
            remove(range);
        }
    }

    /**
     * Searches the registered range equal or greater than a given range
     *
     * @param range the given range to compare with
     * @return the index of the first found range that is equal (overlaps) or greater than the given range. It
     *         always happens that the range at the returned index minus one is to the left of the given range
     *         (maybe in contact, but with no overlap)
     *         If no range is found, resourceSegments.maxSize() + 1 is returned
     *         If the given range is empty, -1 is returned
     */
    private int searchAffectedRange(Range<T> range) {
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

    public RangeList<T> intersection(RangeList<T> anotherRangeList) {
        RangeList<T> intersection = new RangeList<T>();
        for (Range<T> aRange : ranges) {
            intersection.add(aRange.intersection(anotherRangeList.ranges));
        }
        return intersection;
    }

    public T getPosition(T offset) {
        int index = 0;
        while (index < ranges.size() && compareOffsetToRangeSize(offset, ranges.get(index)) > 0) {
            Range<T> r = ranges.get(index);
            offset = r.previous(r.add(r.subtract(offset, r.getMax()), r.getMin()));
            index++;
        }
        if (index < ranges.size()) {
            // range found at index
            return ranges.get(index).add(ranges.get(index).getMin(), offset);
        } else {
            // offset to large
            return null;
        }
    }

    private int compareOffsetToRangeSize(T offset, Range<T> range) {
        T minPlusOffset = range.add(range.getMin(), offset);
        return minPlusOffset.compareTo(range.getMax());
    }

    public long size() {
        long size = 0;
        for (Range<T> range : ranges) {
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

}
