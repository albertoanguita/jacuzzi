package jacz.util.concurrency.daemon;

/**
 * Actions for the DaemonQueue
 */
public interface DaemonQueueAction<T> {

    void solveEvent(T event);
}
