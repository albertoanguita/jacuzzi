package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimer;
import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimerAction;
import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A time alert api. Alerts are added and queued. A single thread handles all added alerts. If the same alert (with equal name) is
 * added twice, it is overriden.
 */
public class TimeAlert implements ParametrizedTimerAction<String> {

    private static final Logger logger = LoggerFactory.getLogger(TimeAlert.class);

    private static class Alert implements Comparable<Alert> {

        private final String name;

        private final long timeToGoOff;

        private final Consumer<String> consumer;

        private Alert(String name, long millis, Consumer<String> consumer) {
            this.name = name;
            this.timeToGoOff = System.currentTimeMillis() + millis;
            this.consumer = consumer;
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

    public synchronized void addAlert(String alertName, long millis, Consumer<String> consumer) {
        if (millis < 0) {
            throw new IllegalArgumentException("Invalid time for alert: " + millis);
        }
        if (activeAlerts.containsKey(alertName)) {
            removeAlert(alertName);
        }
        Alert alert = new Alert(alertName, millis, consumer);
        activeAlerts.put(alertName, alert);
        alertQueue.add(alert);
        if (activeAlerts.size() == 1) {
            threadExecutorClientId = ThreadExecutor.registerClient(this.getClass().getName());
        }
        activateTimer();
    }

    public synchronized void addAlertIfEarlier(String alertName, long millis, Consumer<String> consumer) {
        Long remainingTime = getAlertRemainingTime(alertName);
        if (remainingTime == null || remainingTime > millis) {
            addAlert(alertName, millis, consumer);
        }
    }

    public synchronized void addAlertIfLater(String alertName, long millis, Consumer<String> consumer) {
        Long remainingTime = getAlertRemainingTime(alertName);
        if (remainingTime == null || remainingTime < millis) {
            addAlert(alertName, millis, consumer);
        }
    }

    public synchronized Long getAlertRemainingTime(String alertName) {
        Alert alert = activeAlerts.get(alertName);
        return alert != null ? alert.getRemainingTime() : null;
    }

    public synchronized void removeAlert(String alertName) {
        Alert alert = activeAlerts.remove(alertName);
        if (alert != null) {
            alertQueue.remove(alert);
            activateTimer();
            checkEmptyAlerts();
        }
    }

    public synchronized void removeAllAlerts() {
        if (timer != null) {
            timer.stop();
        }
        activeAlerts.clear();
        alertQueue.clear();
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
    public synchronized Long wakeUp(ParametrizedTimer<String> timer, String alertName) {
        if (alertName.equals(nextAlert)) {
            Alert alert = activeAlerts.get(alertName);
            ThreadExecutor.submit(() -> alert.consumer.accept(alertName), this.getClass().getName() + "." + alert);
            removeAlert(nextAlert);
            activateTimer();
        }
        return 0L;
    }
}
