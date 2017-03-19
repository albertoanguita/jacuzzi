package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimer;
import org.aanguita.jacuzzi.concurrency.timer.ParametrizedTimerAction;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alberto on 03/12/2016.
 */
public class TimeAlerts implements ParametrizedTimerAction<String> {

    private static class Alert {

        private final String name;

        private final long millis;

        private final Runnable runnable;

        private Alert(String name, long millis, Runnable runnable) {
            this.name = name;
            this.millis = millis;
            this.runnable = runnable;
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
    }

    private static TimeAlerts instance = null;

    private final ConcurrentHashMap<String, Alert> activeAlerts;

    private final Queue<Alert> alertQueue;

    private String nextAlert;

    private ParametrizedTimer<String> timer;

    private String threadExecutorClientId;

    private static TimeAlerts getInstance() {
        if (instance == null) {
            instance = new TimeAlerts();
        }
        return instance;
    }

    private TimeAlerts() {
        activeAlerts = new ConcurrentHashMap<>();
        alertQueue = new PriorityQueue<>();
        timer = null;
    }

    public static synchronized void addAlert(String alertName, long millis, Runnable runnable) {
        instance.addAlertInstance(alertName, millis, runnable);
    }

    private synchronized void addAlertInstance(String alertName, long millis, Runnable runnable) {
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

    public static synchronized void removeAlert(String alertName) {
        instance.removeAlertInstance(alertName);
    }

    private synchronized void removeAlertInstance(String alertName) {
        Alert alert = activeAlerts.remove(alertName);
        if (alert != null) {
            alertQueue.remove(alert);
        }
        checkEmptyAlerts();
    }

    private synchronized void activateTimer() {
        if (timer != null) {
            timer.stop();
        }
        if (!activeAlerts.isEmpty()) {
            nextAlert = alertQueue.peek().name;
            timer = new ParametrizedTimer<>(alertQueue.peek().millis, this, nextAlert, true, this.getClass().getName());
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
            removeAlertInstance(nextAlert);
            activateTimer();
        }
        return 0L;
    }
}
