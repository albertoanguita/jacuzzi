package jacz.util.numeric.newrange;

import jacz.util.concurrency.execution_control.PausableElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alberto on 01/10/2015.
 */
public class RangeQueue<T extends Number & Comparable<T>> implements Serializable {


    /**
     * List of ranges. The one in position 0 is the first added (the oldest, and the first to be processed)
     */
    private List<Range<T>> ranges;

    /**
     * Lock indicating that there is a thread trying to retrieve the first data in this object. It helps
     * blocking such thread in case no data is available, running it again when some data is put
     * <p/>
     * This pausable element should be paused whenever no data is available, and resumed whenever some data is
     * available
     */
    private PausableElement retrieveDataLock;

    public RangeQueue() {
        this(false);
    }

    public RangeQueue(boolean fair) {
        ranges = new ArrayList<>();
        retrieveDataLock = new PausableElement(fair);
        retrieveDataLock.pause();
    }

    /**
     * Retrieves a shallow copy of the ranges composing this range queue
     *
     * @return a copy of the ranges composing this queue
     */
    public synchronized List<Range<T>> getRanges() {
        return new ArrayList<>(ranges);
    }

    /**
     * Tells whether this range queue is isEmpty
     *
     * @return true if the range queue is currently isEmpty
     */
    public synchronized boolean isEmpty() {
        return ranges.size() == 0;
    }


    public synchronized void clear() {
        ranges.clear();
        retrieveDataLock.pause();
    }

    public synchronized void add(Range<T> range) {
        if (!ranges.isEmpty() && ranges.get(range.size().intValue() - 1).getMax().equals(range.previous(range.getMin()))) {
            // append to last range
            ranges.set(range.size().intValue() - 1, range.buildInstance(ranges.get(range.size().intValue() - 1).getMin(), range.previous(range.getMax())));
        } else {
            // add new range
            ranges.add(range);
        }
        retrieveDataLock.resume();
    }

    public Range<T> peek(T maxSize) {
        return fetch(maxSize, false);
    }

    public Range<T> remove(T maxSize) {
        return fetch(maxSize, true);
    }

    private Range<T> fetch(T maxSize, boolean remove) {
        Range<T> result = null;
        boolean finished = false;
        while (!finished) {
            retrieveDataLock.access();
            synchronized (this) {
                if (!isEmpty()) {
                    // there is data to retrieve -> get it and finish
                    Range<T> firstRange = remove ? ranges.remove(0) : ranges.get(0);
                    if (firstRange.size().compareTo(maxSize.longValue()) <= 0) {
                        // this range is smaller or equal than what we need, retrieve it all
                        result = firstRange;
                    } else {
                        result = firstRange.buildInstance(firstRange.getMin(), firstRange.previous(firstRange.add(firstRange.getMin(), maxSize)));
                        if (remove) {
                            // add rest of removed range back to queue
                            ranges.add(0, firstRange.buildInstance(firstRange.add(firstRange.getMin(), maxSize), firstRange.getMax()));
                        }
                    }
                    if (isEmpty()) {
                        retrieveDataLock.pause();
                    }
                    finished = true;
                } else {
                    retrieveDataLock.pause();
                }
            }
        }
        return result;
    }

    public synchronized boolean removeNonBlocking(Range<T> receivedRange) {
        if (isEmpty()) {
            return false;
        }
        Range<T> firstRange = ranges.remove(0);
        if (firstRange.size() < receivedRange.size() || !firstRange.getMin().equals(receivedRange.getMin())) {
            ranges.add(0, firstRange);
            return false;
        }
        if (firstRange.size() > receivedRange.size()) {
            ranges.add(0, firstRange.buildInstance(firstRange.next(receivedRange.getMax()), firstRange.getMax()));
        }
        if (isEmpty()) {
            retrieveDataLock.pause();
        }
        return true;
    }

    public synchronized long remainingBytes() {
        long bytes = 0;
        for (Range<T> range : ranges) {
            bytes += range.size();
        }
        return bytes;
    }

    @Override
    public String toString() {
        return ranges.toString();
    }
}
