package org.aanguita.jacuzzi.concurrency.timer;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.log.ErrorLog;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class offers a timer which activates after a specified time, invoking a given method. The timer will keep
 * activating itself periodically, until the given method tells it to stop.
 * <p>
 * Note that if you have activated a timer and then you assign your Timer object a null value or any other Timer,
 * the first timer will still activate itself. You must stop it first!!!
 */
public class Timer {

    private static class WakeUpTask implements Runnable {

        private final Timer timer;

        private final long millis;

        public WakeUpTask(Timer timer, long millis) {
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


    /**
     * Unique identifier of this timer, for comparison purposes
     */
    private final String id;

    /**
     * The action to perform each time the timer goes off
     */
    private final TimerAction timerAction;

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

    public Timer(long millis, TimerAction timerAction) {
        this(millis, timerAction, ThreadUtil.invokerName(1));
    }

    public Timer(long millis, TimerAction timerAction, String threadName) {
        this(millis, timerAction, true, threadName);
    }

    public Timer(long millis, TimerAction timerAction, boolean start, String threadName) {
        id = AlphaNumFactory.getStaticId();
        this.millis = millis;
        this.timerAction = timerAction;
        active = new AtomicBoolean(false);
        remainingTimeWhenStopped = 0;
        this.threadName = threadName + "/Timer";
        if (start) {
            start(millis);
        }
    }

    public String getId() {
        return id;
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
            ThreadExecutor.shutdownClient(threadExecutorClientId);
            threadExecutorClientId = null;
        }
    }

    void wakeUp(WakeUpTask wakeUpTask) {
        // first check wake up task object is the one we last activated (null is anonymous, always valid)
        if (isRunning() && wakeUpTask == this.wakeUpTask) {
            Long timerActionResult = null;
            try {
                timerActionResult = timerAction.wakeUp(this);
            } catch (Exception e) {
                //unexpected exception obtained. Print error and terminate
                ErrorLog.reportError(this.getClass().getName(), "Unexpected exception in timer action implementation", e, Arrays.toString(e.getStackTrace()));
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
            return activationTime + millis - System.currentTimeMillis();
        } else {
            return remainingTimeWhenStopped;
        }
    }

    public synchronized void reset() {
        start(millis);
    }

    public synchronized void reset(long millis) {
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
        if (active.getAndSet(false)) {
            remainingTimeWhenStopped = remainingTime();
            future.cancel(true);
            if (shutdown) {
                shutdownThread();
            }
        }
    }

    public synchronized void resume() {
        if (isStopped() && remainingTimeWhenStopped > 0) {
            start(remainingTimeWhenStopped);
        }
    }

    @Deprecated
    public synchronized void kill() {
        stop();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timer timer = (Timer) o;
        return id.equals(timer.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
