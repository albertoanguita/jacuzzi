package aanguita.jacuzzi.numeric.range;

import aanguita.jacuzzi.concurrency.execution_control.TrafficControl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic range queue implementation
 */
public class RangeQueue<T extends Range<Y>, Y extends Number & Comparable<Y>> implements Serializable {


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
    private TrafficControl retrieveDataLock;

    public RangeQueue() {
        this(false);
    }

    public RangeQueue(boolean fair) {
        ranges = new ArrayList<>();
        retrieveDataLock = new TrafficControl(fair);
        retrieveDataLock.pause();
    }

    /**
     * Retrieves a shallow copy of the ranges composing this range queue
     *
     * @return a copy of the ranges composing this queue
     */
    public synchronized List<T> getRanges() {
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

    public synchronized void add(T range) {
        if (range.isEmpty()) {
            return;
        }
        if (!ranges.isEmpty() && ranges.get(ranges.size() - 1).getMax().equals(range.previous(range.getMin()))) {
            // append to last range
            ranges.set(ranges.size() - 1, (T) range.buildInstance(ranges.get(ranges.size() - 1).getMin(), range.getMax()));
        } else {
            // add new range
            ranges.add(range);
        }
        retrieveDataLock.resume();
    }

    public T peek(Y maxSize) {
        return fetch(maxSize, false);
    }

    public T remove(Y maxSize) {
        return fetch(maxSize, true);
    }

    private T fetch(Y maxSize, boolean remove) {
        T result = null;
        boolean finished = false;
        while (!finished) {
            retrieveDataLock.access();
            synchronized (this) {
                if (!isEmpty()) {
                    // there is data to retrieve -> get it and finish
                    T firstRange = remove ? ranges.remove(0) : ranges.get(0);
                    if (firstRange.size().compareTo(maxSize.longValue()) <= 0) {
                        // this range is smaller or equal than what we need, retrieve it all
                        result = firstRange;
                    } else {
                        result = (T) firstRange.buildInstance(firstRange.getMin(), firstRange.previous(firstRange.add(firstRange.getMin(), maxSize)));
                        if (remove) {
                            // add rest of removed range back to queue
                            ranges.add(0, (T) firstRange.buildInstance(firstRange.add(firstRange.getMin(), maxSize), firstRange.getMax()));
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

    public synchronized boolean removeRange(T rangeToRemove) {
        if (rangeToRemove.isEmpty()) {
            return true;
        } else if (isEmpty()) {
            return false;
        } else {
            // range to remove must be in first range of queue (otherwise, return false)
            T firstRange = ranges.remove(0);
            if (firstRange.size() < rangeToRemove.size() || !firstRange.getMin().equals(rangeToRemove.getMin())) {
                // put the range back into the queue
                ranges.add(0, firstRange);
                return false;
            } else if (firstRange.size() > rangeToRemove.size()) {
                // put back the part of the first range not needed
                ranges.add(0, (T) firstRange.buildInstance(firstRange.next(rangeToRemove.getMax()), firstRange.getMax()));
            }
            if (isEmpty()) {
                // the queue is now empty --> block future retrievals
                retrieveDataLock.pause();
            }
            return true;
        }
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
