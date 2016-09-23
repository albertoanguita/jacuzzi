package org.aanguita.jacuzzi.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @deprecated use ObjectMapPool
 * A generic map of re-entrant locks
 */
public class LockMap<T> {

    private final Map<T, Lock> locks;

    public LockMap() {
        locks = new HashMap<>();
    }

    public synchronized Lock getLock(T index) {
        if (!locks.containsKey(index)) {
            locks.put(index, new ReentrantLock());
        }
        return locks.get(index);
    }

    public synchronized void removeLock(T index) {
        locks.remove(index);
    }
}
