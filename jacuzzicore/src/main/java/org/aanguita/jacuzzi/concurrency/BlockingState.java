package org.aanguita.jacuzzi.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author aanguita
 *         26/09/2017
 */
public class BlockingState<T> {

    private AtomicReference<T> value;

    private final TimeAlert alerts;

    private final ConcurrentHashMap<String, Thread> blockedThreads;

    private final Object sync;

    public BlockingState(T value, String timeAlertName) {
        this.value = new AtomicReference<T>(value);
        alerts = TimeAlert.getInstance(timeAlertName);
        blockedThreads = new ConcurrentHashMap<>();
        sync = new Object();
    }

    public T get() {
        return value.get();
    }

    public void set(T value) {
        this.value.set(value);
        synchronized (sync) {
            sync.notifyAll();
        }
    }

    public T getAndSet(T value) {
        try {
            return this.value.getAndSet(value);
        } finally {
            synchronized (sync) {
                sync.notifyAll();
            }
        }
    }

    public void blockUntil(T expectedValue) throws InterruptedException {
        synchronized (sync) {
            while (!value.get().equals(expectedValue)) {
                sync.wait();
            }
        }
        // remove existing alerts
        alerts.removeAlert(Long.toString(Thread.currentThread().getId()));
    }

    public void blockUntil(T expectedValue, long timeout) throws InterruptedException {
        // set the interrupter for this thread and then make a normal block
        String alertName = Long.toString(Thread.currentThread().getId());
        blockedThreads.put(alertName, Thread.currentThread());
        alerts.addAlert(
                alertName,
                timeout,
                alert -> blockedThreads.remove(alert).interrupt());
        try {
            blockUntil(expectedValue);
        } finally {
            blockedThreads.remove(alertName);
        }
    }

    public void stop() {
        alerts.removeAllAlerts();
        blockedThreads.values().forEach(Thread::interrupt);
    }
}
