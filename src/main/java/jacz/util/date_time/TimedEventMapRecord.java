package jacz.util.date_time;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers a collection of TimedEventRecords, indexed by a generic element
 */
public class TimedEventMapRecord<E> {

    /**
     * For each peer, stores the last time that we synched the shared library with him
     */
    private final Map<E, TimedEventRecord> eventMap;

    /**
     * events occurred more recently than this period of time are considered as recent (in ms)
     */
    private final long recentlyThreshold;

    public TimedEventMapRecord(long threshold) {
        eventMap = new HashMap<>();
        this.recentlyThreshold = threshold;
    }

    public synchronized boolean lastEventIsRecent(E e) {
        return eventMap.containsKey(e) && eventMap.get(e).lastEventIsRecent();
    }

    public synchronized void newEvent(E e) {
        if (!eventMap.containsKey(e)) {
            eventMap.put(e, new TimedEventRecord(recentlyThreshold, true));
        } else {
            eventMap.get(e).newEvent();
        }
    }
}
