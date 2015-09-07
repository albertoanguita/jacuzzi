package jacz.util.date_time;

import java.io.Serializable;

/**
 * A simple class for measuring time deltas
 */
public class TimeElapsed implements Serializable {

    private long time;

    public TimeElapsed() {
        startTimer();
    }

    public synchronized long startTimer() {
        time = System.currentTimeMillis();
        return time;
    }

    public synchronized long measureTime() {
        return measureTime(false);
    }

    public synchronized long measureTime(boolean restartTimer) {
        long currentTime = System.currentTimeMillis();
        long measure = currentTime - time;
        if (restartTimer) {
            time = currentTime;
        }
        return measure;
    }
}
