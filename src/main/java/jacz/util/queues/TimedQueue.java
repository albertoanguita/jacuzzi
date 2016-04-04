package jacz.util.queues;

import jacz.util.concurrency.task_executor.ThreadExecutor;
import jacz.util.concurrency.timer.Timer;
import jacz.util.concurrency.timer.TimerAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A queue where elements die after a period of time, and are automatically removed from the queue
 */
public class TimedQueue<T> implements TimerAction {

    private class QueueElement<T> implements Comparable<QueueElement<T>> {

        private final long timeStamp;

        private final T element;

        private QueueElement(T element) {
            this.timeStamp = System.currentTimeMillis();
            this.element = element;
        }

        private QueueElement(long timeStamp, T element) {
            this.timeStamp = timeStamp;
            this.element = element;
        }


        @Override
        public int compareTo(QueueElement<T> o) {
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

        public void elementsRemoved(List<T> elements);
    }

    private final List<QueueElement<T>> queue;

    private final long millisToStore;

    private final TimedQueueInterface<T> timedQueueInterface;

    private final Timer removeTimer;

    public TimedQueue(long millisToStore, TimedQueueInterface<T> timedQueueInterface) {
        queue = new ArrayList<>();
        this.millisToStore = millisToStore;
        this.timedQueueInterface = timedQueueInterface;
        removeTimer = new Timer(millisToStore, this, false, "TimedQueueInterface");
        ThreadExecutor.registerClient(this.getClass().getName());
    }

    public TimedQueue(long millisToStore, TimedQueueInterface<T> timedQueueInterface, String threadName) {
        queue = new ArrayList<>();
        this.millisToStore = millisToStore;
        this.timedQueueInterface = timedQueueInterface;
        removeTimer = new Timer(millisToStore, this, false, "TimedQueueInterface(" + threadName + ")");
        ThreadExecutor.registerClient(this.getClass().getName());
    }

    public synchronized void stop() {
        removeTimer.kill();
        ThreadExecutor.shutdownClient(this.getClass().getName());
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

    public synchronized int getIndexFrom(long millis) {
        int index = Collections.binarySearch(queue, new QueueElement<T>(millis, null));
        if (index < 0) {
            // time not found, flip so it corresponds to the insertion point
            index = - (index + 1);
        }
        return index;
    }

    public synchronized void addElement(T element) {
        queue.add(new QueueElement<T>(element));
        if (queue.size() == 1) {
            removeTimer.reset(millisToStore);
        }
    }

    private synchronized long removeOldElements() {
        long currentTime = System.currentTimeMillis();
        long oldestTimeMarkAllowed = currentTime - millisToStore;
        List<T> removedElements = new ArrayList<T>();
        while (queue.size() > 0 && queue.get(0).timeStamp <= oldestTimeMarkAllowed) {
            removedElements.add(queue.remove(0).element);
        }
        if (!removedElements.isEmpty()) {
            // invoke in parallel to avoid locks
            ThreadExecutor.submit(new TimedQueueInterfaceTask<T>(timedQueueInterface, removedElements));
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
