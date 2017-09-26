package org.aanguita.jacuzzi.concurrency;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aanguita
 *         26/09/2017
 */
public class BlockingState<T> {

    private T value;

    private final TimeAlert alerts;

    private final ConcurrentHashMap<String, Thread> blockedThreads;

    private final Object sync;

    public BlockingState(T value, String timeAlertName) {
        this.value = value;
        alerts = TimeAlert.getInstance(timeAlertName);
        blockedThreads = new ConcurrentHashMap<>();
        sync = new Object();
    }

    public synchronized T getValue() {
        return value;
    }

    public synchronized void setValue(T value) {
        this.value = value;
        synchronized (sync) {
            sync.notifyAll();
        }
    }

    public void blockUntil(T expectedValue) throws InterruptedException {
        synchronized (sync) {
            while (!value.equals(expectedValue)) {
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
