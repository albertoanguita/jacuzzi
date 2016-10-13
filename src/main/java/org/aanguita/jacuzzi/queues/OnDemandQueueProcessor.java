package org.aanguita.jacuzzi.queues;

import org.aanguita.jacuzzi.concurrency.monitor.Monitor;
import org.aanguita.jacuzzi.concurrency.monitor.StateSolver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

/**
 * A queue of events handled by a monitor processor
 */
public class OnDemandQueueProcessor<T> implements StateSolver {

    /**
     * Default capacity for the event queue
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 1024;

    /**
     * Fairness of petitions is always true
     */
    private final static boolean MESSAGE_FAIRNESS = true;


    private final ArrayBlockingQueue<T> eventQueue;

    private final Monitor monitor;

    private final Consumer<T> consumer;

    public OnDemandQueueProcessor(Consumer<T> daemonQueueAction) {
        this(daemonQueueAction, DEFAULT_QUEUE_CAPACITY);
    }

    public OnDemandQueueProcessor(Consumer<T> daemonQueueAction, int queueCapacity) {
        eventQueue = new ArrayBlockingQueue<>(queueCapacity, MESSAGE_FAIRNESS);
        monitor = new Monitor(this);
        this.consumer = daemonQueueAction;
    }

    public void addEvent(T event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            // avoid interrupts
            addEvent(event);
        }
        monitor.stateChange();
    }

    @Override
    public boolean solveState() {
        if (!eventQueue.isEmpty()) {
            try {
                consumer.accept(eventQueue.take());
            } catch (InterruptedException e) {
                // ignore, solve state in next iteration
            }
            return false;
        }
        return true;
    }
}
