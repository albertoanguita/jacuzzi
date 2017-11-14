package org.aanguita.jacuzzi.task;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;
import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.objects.ObjectMapPool;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * @author aanguita
 * 14/11/2017
 */
public class AsyncTaskManagerImpl<K, T, R> implements AsyncTaskManager<K, T, R> {

    private final ConcurrentMap<K, T> tasks;

    private final ObjectMapPool<K, Lock> taskLocks;

    private final ObjectMapPool<K, SimpleSemaphore> blockingResults;

    @Override
    public synchronized void addTask(K key, T task) {
        tasks.put(key, task);
        blockingResults.getObject(key).pause();
    }

    @Override
    public synchronized R addTask(K key, T task, Long timeout) throws TimeoutException {
        addTask(key, task);
        SimpleSemaphore simpleSemaphore = blockingResults.getObject(key);
        if (timeout != null) {
            simpleSemaphore.access(timeout);
        } else {
            simpleSemaphore.access();
        }
        return null;
    }

    @Override
    public int taskCount() {
        return tasks.size();
    }

    @Override
    public T pickTask(SelectPolicy selectPolicy) {
        return null;
    }

    @Override
    public T pickTask(SelectPolicy selectPolicy, Long timeout) {
        return null;
    }

    @Override
    public T pickTask(K key) {
        return tasks.get(key);
    }

    @Override
    public T pickTask(K key, Long timeout) {
        return null;
    }

    @Override
    public void solveTask(K key, R result) {

    }
}
