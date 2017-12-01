package org.aanguita.jacuzzi.task;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author aanguita
 * 15/11/2017
 */
public interface SelfExecutedAsyncTaskManager<K, R> {

    enum SelectPolicy {
        OLDEST,
        NEWEST,
        RANDOM
    }

    void addTask(K key, Runnable task, Consumer<R> resultEvent);

    R addTaskBlocking(K key, Runnable task, Long timeout, K... additionalKeys) throws TimeoutException;

    void solveTask(K key, R result);
}
