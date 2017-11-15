package org.aanguita.jacuzzi.task;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;
import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.objects.ObjectMapPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author aanguita
 * 15/11/2017
 */
public class SelfExecutedAsyncTaskManagerImpl<K, R> implements SelfExecutedAsyncTaskManager<K, R> {

    private final Map<K, Consumer<R>> resultEvents;

    private final ConcurrentMap<K, R> results;

    private final ObjectMapPool<K, Lock> taskLocks;

    private final ObjectMapPool<K, SimpleSemaphore> resultSemaphores;

    public SelfExecutedAsyncTaskManagerImpl(boolean fair) {
        resultEvents = new HashMap<>();
        results = new ConcurrentHashMap<>();
        taskLocks = new ObjectMapPool<>(key -> new ReentrantLock(fair));
        resultSemaphores = new ObjectMapPool<>(key -> new SimpleSemaphore());
    }

    @Override
    public synchronized void addTask(K key, Runnable task, Consumer<R> resultEvent) {
        resultSemaphores.getObject(key).pause();
        if (resultEvent != null) {
            resultEvents.put(key, resultEvent);
        }
        ThreadExecutor.submitUnregistered(task);
    }

    @Override
    public R addTaskBlocking(K key, Runnable task, Long timeout) throws TimeoutException {
        Lock lock = taskLocks.getObject(key);
        lock.lock();
        try {
            addTask(key, task, null);
            SimpleSemaphore simpleSemaphore = resultSemaphores.getObject(key);
            if (timeout != null) {
                simpleSemaphore.access(timeout);
            } else {
                simpleSemaphore.access();
            }
            return results.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void solveTask(K key, R result) {
        results.put(key, result);
        resultSemaphores.getObject(key).resume();
        if (resultEvents.containsKey(key)) {
            resultEvents.remove(key).accept(result);
        }
    }
}
