package org.aanguita.jacuzzi.concurrency.timer;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.id.StringIdClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alberto on 03/12/2016.
 */
public abstract class AbstractTimer extends StringIdClass {

    private static class WakeUpTask implements Runnable {

        private final AbstractTimer timer;

        private final long millis;

        private WakeUpTask(AbstractTimer timer, long millis) {
            this.timer = timer;
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
                timer.wakeUp(this);
            } catch (InterruptedException e) {
                // the timer interrupted this wake up task because it was stopped -> break
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractTimer.class);

    /**
     * Future object associated to the thread that waits. Used to cancel the thread if timer is killed
     */
    private Future future;

    /**
     * Indicates if this timer is active (running)
     */
    private final AtomicBoolean active;

    /**
     * The last timeout set for this timer
     */
    private long millis;

    /**
     * The time at which this timer was last started
     */
    private long activationTime;

    /**
     * When stopped, indicates how much time the timer has remaining (used for correctly resetting the timer when
     * a resume order is issued). If 0 or negative, resume orders will have no effect
     */
    private long remainingTimeWhenStopped;

    /**
     * Name for the thread associated with the timer (optional)
     */
    private final String threadName;

    /**
     * The activated wake up task object (used to check that the valid wake up task object is the one waking us up)
     */
    private WakeUpTask wakeUpTask;

    private String threadExecutorClientId;

    protected AbstractTimer(long millis, String threadName) {
        validateTime(millis);
        this.millis = millis;
        active = new AtomicBoolean(false);
        remainingTimeWhenStopped = millis;
        this.threadName = threadName + ".Timer";
    }

    protected void initialize(boolean start) {
        if (start) {
            start(millis);
        }
    }

    private void start(long millis) {
        stop(false);
        if (!active.getAndSet(true)) {
            wakeUpTask = new WakeUpTask(this, millis);
            registerThread();
            future = ThreadExecutor.submit(wakeUpTask, threadName);
            activationTime = System.currentTimeMillis();
            remainingTimeWhenStopped = 0;
        }
    }

    private synchronized void registerThread() {
        if (threadExecutorClientId == null) {
            threadExecutorClientId = ThreadExecutor.registerClient(this.getClass().getName() + "(" + threadName + ")");
        }
    }

    private synchronized void shutdownThread() {
        if (threadExecutorClientId != null) {
            ThreadExecutor.unregisterClient(threadExecutorClientId);
            threadExecutorClientId = null;
        }
    }

    private void wakeUp(WakeUpTask wakeUpTask) {
        // first check wake up task object is the one we last activated (null is anonymous, always valid)
        if (isRunning() && wakeUpTask == this.wakeUpTask) {
            Long timerActionResult = null;
            try {
                timerActionResult = wakeUp();
            } catch (Throwable e) {
                //unexpected exception obtained. Print error and terminate
                if (logger.isErrorEnabled()) {
                    logger.error("UNEXPECTED EXCEPTION THROWN BY TIMER ACTION IMPLEMENTATION. THE TIMER WILL STOP EXECUTING. PLEASE CORRECT THE CODE SO NO THROWABLES ARE THROWN AT THIS LEVEL", e);
                }
                stop();
            }
            synchronized (this) {
                // first, check if the timer has been killed, reset or stopped during the task execution
                if (isRunning() && wakeUpTask == this.wakeUpTask) {
                    // the timer has not been reset nor stopped during the wake up action. See if it must be restarted
                    if (timerActionResult != null && timerActionResult > 0) {
                        // the timer must be reset again with a new time
                        reset(timerActionResult);
                        //millisForThisRun = timerActionResult;
                        //setActivationTime();
                    } else if (timerActionResult == null) {
                        // the timer must be reset again with the same time
                        reset();
                    } else {
                        // 0 or negative value -> stop the timer
                        stop();
                    }
                }
            }
        }
    }

    protected abstract Long wakeUp();

    public boolean isRunning() {
        return active.get();
    }

    public boolean isStopped() {
        return !isRunning();
    }

    public synchronized long getMillis() {
        return millis;
    }

    public synchronized long remainingTime() {
        if (active.get()) {
            long time = activationTime + millis - System.currentTimeMillis();
            return Math.max(time, 0L);
        } else {
            return remainingTimeWhenStopped;
        }
    }

    public synchronized void reset() {
        start(millis);
    }

    public synchronized void reset(long millis) {
        validateTime(millis);
        this.millis = millis;
        start(millis);
    }

    /**
     * The timer can still be resumed or reset
     */
    public synchronized void stop() {
        stop(true);
    }

    private synchronized void stop(boolean shutdown) {
        if (active.get()) {
            remainingTimeWhenStopped = remainingTime();
            active.set(false);
            future.cancel(true);
            if (shutdown) {
                shutdownThread();
            }
        }
    }

    public synchronized void resume() {
        if (isStopped()) {
            start(remainingTimeWhenStopped);
        }
    }

    @Deprecated
    public synchronized void kill() {
        stop();
    }

    private void validateTime(long millis) throws IllegalArgumentException {
        if (millis < 0L) {
            throw new IllegalArgumentException("Millis cannot be negative, received " + millis);
        }
    }
}
