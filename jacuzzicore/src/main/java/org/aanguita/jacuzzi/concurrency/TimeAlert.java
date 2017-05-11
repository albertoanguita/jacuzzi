package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimer;
import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimerAction;
import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A time alert api. Alerts are added and queued. A single thread handles all added alerts. If the same alert (with equal name) is
 * added twice, it is overriden.
 */
public class TimeAlert implements ParametrizedTimerAction<String> {

    private static final Logger logger = LoggerFactory.getLogger(TimeAlert.class);

    private static class Alert implements Comparable<Alert> {

        private final String name;

        private final long timeToGoOff;

        private final Runnable runnable;

        private Alert(String name, long millis, Runnable runnable) {
            this.name = name;
            this.timeToGoOff = System.currentTimeMillis() + millis;
            this.runnable = runnable;
        }

        private long getRemainingTime() {
            long time = timeToGoOff - System.currentTimeMillis();
            return time >= 0L ? time : 0L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Alert alert = (Alert) o;

            return name.equals(alert.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public int compareTo(Alert o) {
            return timeToGoOff == o.timeToGoOff ? 0 : timeToGoOff < o.timeToGoOff ? -1 : 1;
        }
    }

    private static ObjectMapPool<String, TimeAlert> instances = new ObjectMapPool<>(s -> new TimeAlert());

    private final ConcurrentHashMap<String, Alert> activeAlerts;

    private final Queue<Alert> alertQueue;

    private String nextAlert;

    private ParametrizedTimer<String> timer;

    private String threadExecutorClientId;

    public static TimeAlert getInstance(String name) {
        return instances.getObject(name);
    }

    private TimeAlert() {
        activeAlerts = new ConcurrentHashMap<>();
        alertQueue = new PriorityQueue<>();
        timer = null;
    }

    public synchronized void addAlert(String alertName, long millis, Runnable runnable) {
        if (millis < 0) {
            throw new IllegalArgumentException("Invalid time for alert: " + millis);
        }
        if (activeAlerts.containsKey(alertName)) {
            removeAlert(alertName);
        }
        Alert alert = new Alert(alertName, millis, runnable);
        activeAlerts.put(alertName, alert);
        alertQueue.add(alert);
        if (activeAlerts.size() == 1) {
            threadExecutorClientId = ThreadExecutor.registerClient(this.getClass().getName());
        }
        activateTimer();
    }

    public synchronized void removeAlert(String alertName) {
        Alert alert = activeAlerts.remove(alertName);
        if (alert != null) {
            alertQueue.remove(alert);
            checkEmptyAlerts();
        }
    }

    private synchronized void activateTimer() {
        if (timer != null) {
            timer.stop();
        }
        if (!activeAlerts.isEmpty()) {
            nextAlert = alertQueue.peek().name;
            timer = new ParametrizedTimer<>(alertQueue.peek().getRemainingTime(), this, nextAlert, true, this.getClass().getName());
        }
    }

    private void checkEmptyAlerts() {
        if (activeAlerts.isEmpty()) {
            ThreadExecutor.unregisterClient(threadExecutorClientId);
        }
    }

    @Override
    public synchronized Long wakeUp(ParametrizedTimer<String> timer, String alert) {
        if (alert.equals(nextAlert)) {
            ThreadExecutor.submit(activeAlerts.get(alert).runnable, this.getClass().getName() + "." + alert);
            removeAlert(nextAlert);
            activateTimer();
        }
        return 0L;
    }
}
