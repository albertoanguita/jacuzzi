package org.aanguita.jacuzzi.queues;

import org.aanguita.jacuzzi.concurrency.monitor.Monitor;
import org.aanguita.jacuzzi.concurrency.monitor.StateSolver;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A queue of events handled by a monitor processor
 */
public class OnDemandQueueProcessor<T> implements StateSolver {

    java.rmi.AccessException accessException;

    /**
     * Default capacity for the event queue
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 1024;

    /**
     * Fairness of petitions is always true
     */
    private final static boolean MESSAGE_FAIRNESS = true;


    private final ArrayBlockingQueue<T> eventQueue;

    private final List<Monitor> monitors;

    private final Consumer<T> consumer;

    public OnDemandQueueProcessor(Consumer<T> messageConsumer) {
        this(messageConsumer, DEFAULT_QUEUE_CAPACITY);
    }

    public OnDemandQueueProcessor(Consumer<T> messageConsumer, int queueCapacity) {
        this(messageConsumer, queueCapacity, 1);
    }

    public OnDemandQueueProcessor(Consumer<T> messageConsumer, int queueCapacity, int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("maxThreads must be a positive integer, received " + maxThreads);
        }
        eventQueue = new ArrayBlockingQueue<>(queueCapacity, MESSAGE_FAIRNESS);
        monitors = initializeMonitors(maxThreads);
        this.consumer = messageConsumer;
    }

    private List<Monitor> initializeMonitors(int numMonitors) {
        return IntStream.range(0, numMonitors).mapToObj(i -> new Monitor(this)).collect(Collectors.toList());
    }

    public void addEvent(T event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            // avoid interrupts
            addEvent(event);
        }
        wakeUpAMonitor();
    }

    private void wakeUpAMonitor() {
        // attempt to wake the first sleeping monitor. If all are working, notify the first one
        for (Monitor monitor : monitors) {
            if (monitor.isStateSolved()) {
                // wake up this one!
                monitor.stateChange();
                return;
            }
        }
        // all are working
        monitors.get(0).stateChange();
    }

    @Override
    public boolean solveState() {
        T message;
        synchronized (this) {
            message = eventQueue.poll();
        }
        if (message != null) {
            consumer.accept(message);
            return false;
        } else {
            return true;
        }
    }

    public void stop() {
        monitors.forEach(Monitor::stop);
    }
}
