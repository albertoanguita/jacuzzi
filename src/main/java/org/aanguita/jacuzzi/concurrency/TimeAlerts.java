package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.concurrency.timer.Timer;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Alberto on 03/12/2016.
 */
public class TimeAlerts implements TimerAction {

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

    private final Timer timer;

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
        timer = new Timer(0, this, false, this.getClass().getName());
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

    private synchronized void activateTimer() {
        timer.stop();
        if (!activeAlerts.isEmpty()) {
            timer.reset(alertQueue.peek().millis);
            nextAlert = alertQueue.peek().name;
        }
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

    private void checkEmptyAlerts() {
        if (activeAlerts.isEmpty()) {
            ThreadExecutor.shutdownClient(threadExecutorClientId);
        }
    }

    @Override
    public Long wakeUp(Timer timer) {
        ThreadExecutor.submit(activeAlerts.get(nextAlert).runnable, this.getClass().getName() + "." + nextAlert);
        removeAlert(nextAlert);
        activateTimer();
        return null;
    }
}
