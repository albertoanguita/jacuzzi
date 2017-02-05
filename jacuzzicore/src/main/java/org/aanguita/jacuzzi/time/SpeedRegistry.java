package org.aanguita.jacuzzi.time;

import org.aanguita.jacuzzi.concurrency.timer.TimerAction;
import org.aanguita.jacuzzi.concurrency.timer.Timer;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Maintains a registry of average speeds from a speed monitor
 */
public class SpeedRegistry implements TimerAction {

    private final SpeedMonitor speedMonitor;

    private final long monitorPeriod;

    private int capacity;

    private final Queue<Double> speedRegistry;

    private final Timer timer;

    public SpeedRegistry(long millisToStore, long timeToStore, long monitorPeriod) {
        this(new SpeedMonitor(millisToStore), timeToStore, monitorPeriod);
    }

    public SpeedRegistry(SpeedMonitor speedMonitor, long timeToStore, long monitorPeriod) {
        this.speedMonitor = speedMonitor;
        this.monitorPeriod = monitorPeriod;
        setTimeToStore(timeToStore);
        speedRegistry = new ArrayDeque<>();
        timer = new Timer(monitorPeriod, this);
    }

    public long getMonitorPeriod() {
        return monitorPeriod;
    }

    public void setTimeToStore(long timeToStore) {
        capacity = (int) (timeToStore / monitorPeriod);
    }

    public synchronized Double[] getRegistry() {
        return speedRegistry.toArray(new Double[speedRegistry.size()]);
    }

    public void addProgress(long quantity) {
        speedMonitor.addProgress(quantity);
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        speedRegistry.add(speedMonitor.getAverageSpeed());
        while (speedRegistry.size() > capacity) {
            speedRegistry.poll();
        }
        return null;
    }

    public synchronized void stop() {
        speedMonitor.stop();
        timer.stop();
    }

    @Override
    public String toString() {
        return "SpeedRegistry{" +
                "speedRegistry=" + speedRegistry +
                '}';
    }
}
