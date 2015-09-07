package jacz.util.concurrency.daemon;

/**
 * Actions for the DaemonQueue
 */
public interface DaemonQueueAction<T> {

    public void solveEvent(T event);
}
