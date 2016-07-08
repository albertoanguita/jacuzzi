package org.aanguita.jacuzzi.concurrency.daemon;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * A queue of events handled by a daemon processor
 */
public class DaemonQueue<T> implements DaemonAction {

    /**
     * Default capacity for the event queue
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 1024;

    /**
     * Fairness of petitions is always true
     */
    private final static boolean MESSAGE_FAIRNESS = true;


    private final ArrayBlockingQueue<T> eventQueue;

    private final Daemon daemon;

    private final DaemonQueueAction<T> daemonQueueAction;

    public DaemonQueue(DaemonQueueAction<T> daemonQueueAction) {
        this(daemonQueueAction, DEFAULT_QUEUE_CAPACITY);
    }

    public DaemonQueue(DaemonQueueAction<T> daemonQueueAction, int queueCapacity) {
        eventQueue = new ArrayBlockingQueue<>(queueCapacity, MESSAGE_FAIRNESS);
        daemon = new Daemon(this);
        this.daemonQueueAction = daemonQueueAction;
    }

    public void addEvent(T event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            // avoid interrupts
            addEvent(event);
        }
        daemon.stateChange();
    }

    @Override
    public boolean solveState() {
        if (!eventQueue.isEmpty()) {
            try {
                daemonQueueAction.solveEvent(eventQueue.take());
            } catch (InterruptedException e) {
                // ignore, solve state in next iteration
            }
            return false;
        }
        return true;
    }
}
