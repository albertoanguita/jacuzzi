package jacz.util.numeric;

import jacz.util.concurrency.execution_control.PausableElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A queue of ranges
 */
public class RangeQueue<T extends Range<T, Y> & RangeInterface<T, Y>, Y extends Comparable<Y>> {


    /**
     * List of ranges. The one in position 0 is the first added (the oldest, and the first to be processed)
     */
    private List<T> ranges;

    /**
     * Lock indicating that there is a thread trying to retrieve the first data in this object. It helps
     * blocking such thread in case no data is available, running it again when some data is put
     * <p/>
     * This pausable element should be paused whenever no data is available, and resumed whenever some data is
     * available
     */
    private PausableElement retrieveDataLock;

    public RangeQueue() {
        ranges = new ArrayList<T>();
        retrieveDataLock = new PausableElement();
    }

    /**
     * Retrieves a shallow copy of the ranges composing this range queue
     *
     * @return a copy of the ranges composing this queue
     */
    public synchronized List<T> getRanges() {
        return new ArrayList<T>(ranges);
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

    public synchronized void add(T range) {
        ranges.add(range);
        mergeRanges();
        retrieveDataLock.resume();
    }

    private void mergeRanges() {
        // a ranges was just added. Check if it must be merged with previous ranges
        if (ranges.size() > 1 && ranges.get(ranges.size() - 2).compareTo(ranges.get(ranges.size() - 1)) == RangeToRangeComparison.LEFT_CONTACT) {
            T newRange = ranges.get(ranges.size() - 1).buildInstance(ranges.get(ranges.size() - 2).getMin(), ranges.get(ranges.size() - 1).getMax());
            ranges.set(ranges.size() - 2, newRange);
            ranges.remove(ranges.size() - 1);
        }
    }

    public T peek(Y maxSize) {
        T result = null;
        boolean finished = false;
        while (!finished) {
            retrieveDataLock.access();
            synchronized (this) {
                if (!isEmpty()) {
                    // there is data to retrieve -> get it and finish
                    T firstRange = ranges.get(0);
                    // build a range from 1 to maxSize for comparisons
                    T comparableRange = firstRange.buildInstance(firstRange.next(firstRange.substract(maxSize, maxSize)), maxSize);
                    if (firstRange.size().compareTo(comparableRange.size()) <= 0) {
                        // this range is smaller than what we need, retrieve it all
                        result = firstRange;
                    } else {
                        result = firstRange.buildInstance(firstRange.getMin(), firstRange.previous(firstRange.add(firstRange.getMin(), maxSize)));
                    }
                    if (isEmpty()) {
                        retrieveDataLock.pause();
                    }
                    finished = true;
                }
            }
        }
        return result;
    }

    public T remove(Y maxSize) {
        // this method must return a range of the required maxSize (or less if the first range in the queue is not
        // large enough). In case there are no ranges in this object, this method must block until some ranges
        // are added
        T result = null;
        boolean finished = false;
        while (!finished) {
            retrieveDataLock.access();
            synchronized (this) {
                if (!isEmpty()) {
                    // there is data to retrieve -> get it and finish
                    T firstRange = ranges.remove(0);
                    // build a range from 1 to maxSize for comparisons
                    T comparableRange = firstRange.buildInstance(firstRange.next(firstRange.substract(maxSize, maxSize)), maxSize);
                    if (firstRange.size().compareTo(comparableRange.size()) <= 0) {
                        // this range is smaller than what we need, retrieve it all
                        result = firstRange;
                    } else {
                        result = firstRange.buildInstance(firstRange.getMin(), firstRange.previous(firstRange.add(firstRange.getMin(), maxSize)));
                        ranges.add(0, firstRange.buildInstance(firstRange.add(firstRange.getMin(), maxSize), firstRange.getMax()));
                    }
                    if (isEmpty()) {
                        retrieveDataLock.pause();
                    }
                    finished = true;
                }
            }
        }
        return result;
    }

    public synchronized boolean removeNonBlocking(T receivedRange) {
        if (isEmpty()) {
            return false;
        }
        T firstRange = ranges.remove(0);
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
        for (T range : ranges) {
            bytes += range.size();
        }
        return bytes;
    }

    @Override
    public String toString() {
        return ranges.toString();
    }
}
