package aanguita.jacuzzi.date_time;

import aanguita.jacuzzi.concurrency.ThreadUtil;
import aanguita.jacuzzi.lists.tuple.Duple;

/**
 * This class adapts the behaviour of the SpeedMonitor class to implement a SpeedLimiter that acts as a limiter in the
 * speed of some process. Through this class we do not read the speed of a process, but rather limit it (with the
 * method setSpeed). The method addProgress will halt the necessary time to ensure the assigned speed is not surpassed.
 */
public class SpeedLimiter extends SpeedMonitor {

    /**
     * Maximum allowed speed, as set by the client. Can by dynamically set.
     * <p/>
     * 0 or negative values indicate that no speed limit is set
     */
    private Double speed;

    public SpeedLimiter(long millisToStore, Double speed) {
        super(millisToStore);
        this.speed = speed;
    }

    public synchronized Double getSpeedLimit() {
        return speed;
    }

    public synchronized void setSpeedLimit(Double speed) {
        this.speed = speed;
    }

    public void addProgress(long quantity) {
        super.addProgress(quantity);
        Duple<Double, Long> currentSpeedAndTimeLapse = getAverageSpeedAndTimeLapse();
        long millisToWait = 0L;
        synchronized (this) {
            if (currentSpeedAndTimeLapse != null && currentSpeedAndTimeLapse.element1 > speed) {
                // we are going to fast, we must wait to avoid surpassing the speed limit
                double errorProportion = currentSpeedAndTimeLapse.element1 / speed;
                millisToWait = (long) ((errorProportion - 1d) * (double) currentSpeedAndTimeLapse.element2);
            }
        }
        if (millisToWait > 0) {
            ThreadUtil.safeSleep(millisToWait);
        }
    }
}
