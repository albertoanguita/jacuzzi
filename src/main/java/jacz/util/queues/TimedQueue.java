package jacz.util.queues;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.timer.Timer;
import jacz.util.concurrency.timer.TimerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A queue where elements die after a period of time, and are automatically removed from the queue
 */
public class TimedQueue<T> implements TimerAction {

    public static class TimedQueueElement<E> implements Comparable<TimedQueueElement<E>> {

        private final long timeStamp;

        private final E element;

        private TimedQueueElement(E element) {
            this.timeStamp = System.currentTimeMillis();
            this.element = element;
        }

        private TimedQueueElement(long timeStamp, E element) {
            this.timeStamp = timeStamp;
            this.element = element;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public E getElement() {
            return element;
        }

        @Override
        public int compareTo(TimedQueueElement<E> o) {
            if (timeStamp == o.timeStamp) {
                return 0;
            } else if (timeStamp < o.timeStamp) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static class TimedQueueInterfaceTask<T> implements Runnable {

        private final TimedQueueInterface<T> timedQueueInterface;

        private final List<T> removedElements;

        private TimedQueueInterfaceTask(TimedQueueInterface<T> timedQueueInterface, List<T> removedElements) {
            this.timedQueueInterface = timedQueueInterface;
            this.removedElements = removedElements;
        }

        @Override
        public void run() {
            timedQueueInterface.elementsRemoved(removedElements);
        }
    }

    public static interface TimedQueueInterface<T> {

        void elementsRemoved(List<T> elements);
    }

    private final List<TimedQueueElement<T>> queue;

    private final long millisToStore;

    private final TimedQueueInterface<T> timedQueueInterface;

    /**
     * Timer for automatically removing old elements. If no automatic removal, it will be set to null
     */
    private final Timer removeTimer;

    private final ExecutorService sequentialTaskExecutor;

    private final AtomicBoolean alive;

    public TimedQueue(long millisToStore) {
        this(millisToStore, null, false);
    }

    public TimedQueue(long millisToStore, TimedQueueInterface<T> timedQueueInterface, boolean automaticRemoval) {
        this(millisToStore, timedQueueInterface, automaticRemoval, ThreadUtil.invokerName(1));
    }

    public TimedQueue(long millisToStore, TimedQueueInterface<T> timedQueueInterface, boolean automaticRemoval, String threadName) {
        queue = new ArrayList<>();
        this.millisToStore = millisToStore;
        this.timedQueueInterface = timedQueueInterface;
        if (automaticRemoval) {
            removeTimer = new Timer(millisToStore, this, false, threadName + "/TimedQueue");
        } else {
            removeTimer = null;
        }
        if (timedQueueInterface != null) {
            sequentialTaskExecutor = Executors.newSingleThreadExecutor();
        } else {
            sequentialTaskExecutor = null;
        }
        alive = new AtomicBoolean(true);
    }

    public synchronized void stop() {
        if (alive.get()) {
            alive.set(false);
            if (removeTimer != null) {
                removeTimer.kill();
            }
            if (sequentialTaskExecutor != null) {
                sequentialTaskExecutor.shutdown();
            }
        }
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public long getMillisToStore() {
        return millisToStore;
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized T get(int index) {
        return queue.get(index).element;
    }

    public synchronized TimedQueueElement<T> getTimedElement(int index) {
        return queue.get(index);
    }

    public synchronized int getIndexFrom(long millis) {
        TimedQueueElement<T> comparableElement = new TimedQueueElement<>(millis, null);
        int index = Collections.binarySearch(queue, comparableElement);
        if (index < 0) {
            // time not found, flip so it corresponds to the insertion point
            index = - (index + 1);
        }
        return index;
    }

    public synchronized List<T> pop() {
        return pop(1);
    }

    public synchronized List<T> pop(int maxCount) {
        List<T> elements = new ArrayList<>();
        while (!queue.isEmpty() && elements.size() < maxCount) {
            elements.add(queue.remove(0).element);
        }
        return elements;
    }

    public synchronized List<TimedQueueElement<T>> popTimed() {
        return popTimed(1);
    }

    public synchronized List<TimedQueueElement<T>> popTimed(int maxCount) {
        List<TimedQueueElement<T>> elements = new ArrayList<>();
        while (!queue.isEmpty() && elements.size() < maxCount) {
            elements.add(queue.remove(0));
        }
        return elements;
    }

    public synchronized List<T> peek() {
        return peek(1);
    }

    public synchronized List<T> peek(int maxCount) {
        List<T> elements = new ArrayList<>();
        int i = 0;
        while (i < queue.size() && elements.size() < maxCount) {
            elements.add(queue.get(i).element);
            i++;
        }
        return elements;
    }

    public synchronized List<TimedQueueElement<T>> peekTimed() {
        return peekTimed(1);
    }

    public synchronized List<TimedQueueElement<T>> peekTimed(int maxCount) {
        List<TimedQueueElement<T>> elements = new ArrayList<>();
        int i = 0;
        while (i < queue.size() && elements.size() < maxCount) {
            elements.add(queue.get(i));
            i++;
        }
        return elements;
    }

    public synchronized void addElement(T element) {
        queue.add(new TimedQueueElement<T>(element));
        if (queue.size() == 1 && removeTimer != null) {
            removeTimer.reset(millisToStore);
        }
    }

    public synchronized void clear() {
        queue.clear();
    }

    public synchronized void clearOldElements() {
        removeOldElements();
    }

    private synchronized long removeOldElements() {
        long currentTime = System.currentTimeMillis();
        long oldestTimeMarkAllowed = currentTime - millisToStore;
        List<T> removedElements = new ArrayList<T>();
        while (queue.size() > 0 && queue.get(0).timeStamp <= oldestTimeMarkAllowed) {
            removedElements.add(queue.remove(0).element);
        }
        if (!removedElements.isEmpty() && timedQueueInterface != null) {
            // invoke in parallel to avoid locks
            sequentialTaskExecutor.submit(new TimedQueueInterfaceTask<T>(timedQueueInterface, removedElements));
        }
        if (!queue.isEmpty()) {
            // calculate time for next removal
            return queue.get(0).timeStamp - oldestTimeMarkAllowed;
        } else {
            return 0L;
        }
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        // time to remove some elements
        // if necessary, reset the timer with the new time for removal
        return removeOldElements();
    }
}
