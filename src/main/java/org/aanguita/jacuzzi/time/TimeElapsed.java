package org.aanguita.jacuzzi.time;

import java.io.Serializable;

/**
 * A simple class for measuring time deltas
 * <p>
 * The class is not thread-safe. If used in concurrent environments, proper synchronization must be set up
 */
public class TimeElapsed implements Serializable {

    private long time;

    public TimeElapsed() {
        startTimer();
    }

    public long startTimer() {
        time = System.currentTimeMillis();
        return time;
    }

    public long measureTime() {
        return measureTime(false);
    }

    public long measureTime(boolean restartTimer) {
        long currentTime = System.currentTimeMillis();
        long measure = currentTime - time;
        if (restartTimer) {
            time = currentTime;
        }
        return measure;
    }
}
