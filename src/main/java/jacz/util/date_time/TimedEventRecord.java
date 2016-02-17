package jacz.util.date_time;

/**
 * Registers the time at which some events happen. Allows to calculate (given some parameters) if the last event
 * is recent or not. Useful to distribute events in time
 * <p/>
 * Currently it only registers the last event. Extend this class to more general cases
 */
public class TimedEventRecord {

    /**
     * events occurred more recently than this period of time are considered as recent (in ms)
     */
    private final long recentlyThreshold;

    /**
     * the last time that an event happened (null if did not happen yet)
     */
    private Long lastEventTime;

    public TimedEventRecord(long recentlyThreshold) {
        this(recentlyThreshold, false);
    }

    public TimedEventRecord(long recentlyThreshold, boolean registerEvent) {
        this.recentlyThreshold = recentlyThreshold;
        if (registerEvent) {
            newEvent();
        } else {
            lastEventTime = null;
        }
    }

    public synchronized boolean lastEventIsRecent() {
        return lastEventTime != null && lastEventTime > getCurrentTime() - recentlyThreshold;
    }

    public synchronized void newEvent() {
        lastEventTime = getCurrentTime();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
