package jacz.util.numeric;

import java.io.Serializable;
import java.util.*;

/**
 * Stores a set of ranges. Internally, the ranges are stored in order, for faster operations. Ranges can be added
 * or deleted from the set. The set will be maintained merged.
 */
public class RangeSet<T extends Range<T, Y> & RangeInterface<T, Y>, Y extends Comparable<Y>> implements Serializable {

    /**
     * Ordered list of the ranges composing this set. No overlapping or in contact ranges can live here (they are
     * merged if needed)
     */
    private List<T> ranges;


    public RangeSet() {
        ranges = new ArrayList<T>();
    }

    public RangeSet(T initialRange) {
        ranges = new ArrayList<T>(1);
        ranges.add(initialRange);
    }

    public RangeSet(Collection<T> ranges) {
        this.ranges = new ArrayList<T>();
        add(ranges);
    }

    public RangeSet(RangeSet<T, Y> anotherRangeSet) {
        ranges = new ArrayList<T>(anotherRangeSet.ranges);
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public List<T> getRanges() {
        return new ArrayList<T>(ranges);
    }

    // todo make contains methods use binary search

    public boolean contains(Y value) {
        return searchRange(value) >= 0;
    }

    public int searchRange(Y value) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).contains(value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(T range) {
        return searchRange(range) >= 0;
    }

    public int searchRange(T range) {
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).compareTo(range) == RangeToRangeComparison.CONTAINS || ranges.get(i).compareTo(range) == RangeToRangeComparison.EQUALS) {
                return i;
            }
        }
        return -1;
    }

    public void add(T range) {
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
        T rangeToMerge = ranges.get(index);
        // check contact with a segment of the left
        if (index > 0 && ranges.get(index - 1).compareTo(rangeToMerge) == RangeToRangeComparison.LEFT_CONTACT) {
            Y min = ranges.get(index - 1).getMin();
            Y max = rangeToMerge.getMax();
            ranges.set(index, rangeToMerge.buildInstance(min, max));
            ranges.remove(index - 1);
            index--;
            rangeToMerge = ranges.get(index);
        }
        // check contact, overlapping and containment with segments on the right
        int secIndex = index + 1;
        while (secIndex < ranges.size() && rangeToMerge.compareTo(ranges.get(secIndex)) != RangeToRangeComparison.LEFT_NO_CONTACT) {
            secIndex++;
        }
        // if secIndex is bigger than (index + 1) then some segments need to be merged (until segment at secIndex - 1).
        if (secIndex > index + 1) {
            Y min = rangeToMerge.getMin();
            Y max;
            max = rangeToMerge.getMax().compareTo(ranges.get(secIndex - 1).getMax()) >= 0 ? rangeToMerge.getMax() : ranges.get(secIndex - 1).getMax();
            ranges.set(index, rangeToMerge.buildInstance(min, max));
            for (int i = index + 1; i < secIndex; i++) {
                ranges.remove(index + 1);
            }
        }
    }

    public void add(Collection<T> ranges) {
        for (T range : ranges) {
            add(range);
        }
    }

    public void add(T... ranges) {
        add(Arrays.asList(ranges));
    }

    public void add(RangeSet<T, Y> anotherRangeSet) {
        for (T range : anotherRangeSet.getRanges()) {
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
                    range.compareTo(ranges.get(secIndex)) == RangeToRangeComparison.CONTAINS) {
                ranges.remove(secIndex);
            }
            if (secIndex < ranges.size() &&
                    (range.compareTo(ranges.get(secIndex)) == RangeToRangeComparison.LEFT_OVERLAP)) {
                Y min = range.next(range.getMax());
                Y max = ranges.get(secIndex).getMax();
                ranges.set(secIndex, range.buildInstance(min, max));
            }

            // now deal with the range at index. It might have to be deleted, partially deleted, or even split in two
            switch (range.compareTo(ranges.get(index))) {

                case LEFT_OVERLAP:
                    Y min = range.next(range.getMax());
                    Y max = ranges.get(index).getMax();
                    ranges.set(index, range.buildInstance(min, max));
                    break;

                case RIGHT_OVERLAP:
                    min = ranges.get(index).getMin();
                    max = range.previous(range.getMin());
                    ranges.set(index, range.buildInstance(min, max));
                    break;

                case INSIDE:
                    Y min1 = ranges.get(index).getMin();
                    Y max1 = range.previous(range.getMin());
                    Y min2 = range.next(range.getMax());
                    Y max2 = ranges.get(index).getMax();
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

    public void remove(List<T> ranges) {
        for (T range : ranges) {
            remove(range);
        }
    }

    public void remove(T... ranges) {
        remove(Arrays.asList(ranges));
    }

    public void remove(RangeSet<T, Y> anotherRangeSet) {
        for (T range : anotherRangeSet.getRanges()) {
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
    private int searchAffectedRange(T range) {
        // perform a binary search to reduce complexity
        int min = 0;
        int max = ranges.size() - 1;
        int mid = 0;
        while (min <= max) {
            mid = (min + max) / 2;
            RangeToRangeComparison comp = ranges.get(mid).compareTo(range);
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

    public RangeSet<T, Y> intersection(RangeSet<T, Y> anotherRangeSet) {
        RangeSet<T, Y> intersection = new RangeSet<T, Y>();
        for (T aRange : ranges) {
            intersection.add(aRange.intersection(anotherRangeSet.ranges));
        }
        return intersection;
    }

    public Y getPosition(Y offset) {
        int index = 0;
        while (index < ranges.size() && compareOffsetToRangeSize(offset, ranges.get(index)) > 0) {
            T r = ranges.get(index);
            offset = r.previous(r.add(r.substract(offset, r.getMax()), r.getMin()));
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

    private int compareOffsetToRangeSize(Y offset, T range) {
        Y minPlusOffset = range.add(range.getMin(), offset);
        return minPlusOffset.compareTo(range.getMax());
    }

    public long size() {
        long size = 0;
        for (T range : ranges) {
            size += range.size();
        }
        return size;
    }

    public void clear() {
        for (T range : getRanges()) {
            remove(range);
        }
    }

    @Override
    public String toString() {
        return ranges.toString();
    }

}
