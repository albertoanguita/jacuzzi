package org.aanguita.jacuzzi.task;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * This interface defines an entity capable of asynchronously handling the delivery and resolution of generic tasks
 * <p>
 * A client creates tasks, and waits for such tasks to be resolved. Another client picks existing tasks and tries
 * to solve them.
 *
 * @author aanguita
 * 14/11/2017
 */
public interface AsyncTaskManager<K, T, R> {

    enum SelectPolicy {
        OLDEST,
        NEWEST,
        RANDOM
    }

    void addTask(K key, T task, Consumer<R> resultEvent);

    R addTaskBlocking(K key, T task, Long timeout) throws TimeoutException;

    default boolean isEmpty() {
        return taskCount() == 0;
    }

    int taskCount();

    T pickTask(SelectPolicy selectPolicy);

    T pickTask(SelectPolicy selectPolicy, Long timeout) throws TimeoutException;

    T pickTask(K key);

    T pickTask(K key, Long timeout) throws TimeoutException;

    void solveTask(K key, R result);
}
