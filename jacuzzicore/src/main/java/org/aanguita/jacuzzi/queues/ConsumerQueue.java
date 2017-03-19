package org.aanguita.jacuzzi.queues;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Created by Alberto on 05/02/2017.
 */
public class ConsumerQueue<T> {

    private final Queue<T> queuedTasks;

    private final Consumer<T> taskConsumer;

    public ConsumerQueue(Consumer<T> taskConsumer) {
        queuedTasks = new ArrayDeque<T>();
        this.taskConsumer = taskConsumer;
    }

    public synchronized void add(T task) {
        queuedTasks.add(task);
    }

    public synchronized void flush() {
        flush(false);
    }

    public synchronized void flush(boolean inBackground) {
        flush(inBackground, 0L);
    }

    public synchronized void flush(boolean inBackground, long delayBetweenTasks) {
        if (inBackground) {
            String threadExecutorId = ThreadExecutor.registerClient("ConsumerQueue");
            ThreadExecutor.submit(() -> flush(false, delayBetweenTasks));
            ThreadExecutor.unregisterClient(threadExecutorId);
        } else {
            while (!queuedTasks.isEmpty()) {
                T task = queuedTasks.remove();
                taskConsumer.accept(task);
                if (!queuedTasks.isEmpty() && delayBetweenTasks > 0) {
                    ThreadUtil.safeSleep(delayBetweenTasks);
                }
            }
        }
    }
}
