package jacz.util.concurrency.timer;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.task_executor.ThreadExecutor;
import jacz.util.id.AlphaNumFactory;
import jacz.util.log.ErrorLog;
import jacz.util.objects.Util;

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

    /**
     * Possible states of a timer
     */
    public enum State {
        // timer is running
        RUNNING,
        // timer is stopped (can be resumed)
        STOPPED,
        // timer is not alive anymore (can no longer be used)
        KILLED
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
     * Indicates if this timer is active (alive and running)
     */
    private final AtomicBoolean active;

    /**
     * Indicates if this timer is alive
     */
    private final AtomicBoolean alive;

    /**
     * The last timeout set for this timer
     */
    private long millis;

    /**
     * The time that the current timer run will have to wait (if reset, it will be similar to millis. If resumed,
     * it will be the remaining time).
     * <p>
     * This is the actual time that the wake up task will query for
     */
    private long millisForThisRun;

    /**
     * The time at which this timer was last activated
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
        alive = new AtomicBoolean(true);
        remainingTimeWhenStopped = 0;
        this.threadName = threadName + "/Timer";
        ThreadExecutor.registerClient(this.getClass().getName());
        if (start) {
            start();
        }
    }

    public String getId() {
        return id;
    }

    private void start() {
        if (alive.get() && !active.get()) {
            active.set(true);
            wakeUpTask = new WakeUpTask(this);
            future = ThreadExecutor.submit(wakeUpTask, threadName);
            setActivationTime();
            remainingTimeWhenStopped = 0;
        }
    }

    private synchronized void setActivationTime() {
        activationTime = System.currentTimeMillis();
    }

    boolean wakeUp(WakeUpTask wakeUpTask) {
        // first check wake up task object is the one we last activated (null is anonymous, always valid)
        AtomicBoolean validWakeUp = new AtomicBoolean(wakeUpTask == null || wakeUpTask == this.wakeUpTask);
        if (validWakeUp.get() && active.get() && alive.get()) {
            Long timerActionResult = null;
            try {
                timerActionResult = timerAction.wakeUp(this);
            } catch (Exception e) {
                //unexpected exception obtained. Print error and terminate
                ErrorLog.reportError(this.getClass().getName(), "Unexpected exception in timer action implementation", e, Arrays.toString(e.getStackTrace()));
                kill();
            }
            synchronized (this) {
                // first, check if the timer has been killed, reset or stopped during the task execution
                if (!alive.get()) {
                    // the timer was killed
                    return false;
                } else if (!Util.equals(this.wakeUpTask, wakeUpTask)) {
                    // the timer has been reset (new wake up task)
                    return true;
                } else if (!active.get()) {
                    // the timer has been stopped
                    return false;
                } else {
                    // recalculate active
                    if (timerActionResult != null && timerActionResult > 0) {
                        millis = timerActionResult;
                        setActivationTime();
                        return true;
                    }
                    if (timerActionResult == null) {
                        setActivationTime();
                        return true;
                    } else {
                        // 0 or negative value -> stop the timer
                        stop();
                        return false;
                    }
                }
            }
        } else {
            // this is an old wake up task -> kill it
            return false;
        }
    }

    public synchronized State getState() {
        if (!alive.get()) {
            return State.KILLED;
        } else {
            return active.get() ? State.RUNNING : State.STOPPED;
        }
    }

    public synchronized long getMillis() {
        return millis;
    }

    synchronized long getMillisForThisRun() {
        return millisForThisRun;
    }

    public synchronized long remainingTime() {
        if (active.get()) {
            return activationTime + millis - System.currentTimeMillis();
        } else {
            return remainingTimeWhenStopped;
        }
    }

    /**
     * Makes the timer go off, no matter how much time is left. The timer is reset with the same time as before
     */
    public synchronized void goOff() {
        stop();
        wakeUp(null);
        start();
    }

    public synchronized void reset() {
        reset(millis);
    }

    public synchronized void reset(double factorWithLastDelay) {
        // only reset time if remaining time is not reduced
        if (factorWithLastDelay < 0.0d) {
            factorWithLastDelay = 0.0d;
        } else if (factorWithLastDelay > 1.0d) {
            factorWithLastDelay = 1.0d;
        }
        long resetTime = (long) (millis * factorWithLastDelay);
        if (!active.get() || (resetTime > remainingTime())) {
            reset((long) (millis * factorWithLastDelay));
        }
    }

    public synchronized void reset(long millis) {
        this.millis = millis;
        runTimer(millis);
    }

    private void runTimer(long millisForThisRun) {
        stop();
        this.millisForThisRun = millisForThisRun;
        start();
    }

    /**
     * The timer can still be resumed or reset
     */
    public synchronized void stop() {
        if (active.get()) {
            remainingTimeWhenStopped = remainingTime();
            active.set(false);
            future.cancel(true);
        }
    }

    public synchronized void resume() {
        if (!active.get() && remainingTimeWhenStopped > 0) {
            runTimer(remainingTimeWhenStopped);
        }
    }

    /**
     * The timer is stopped, and cannot be resumed/reset again
     */
    public synchronized void kill() {
        if (alive.get()) {
            alive.set(false);
            stop();
            ThreadExecutor.shutdownClient(this.getClass().getName());
        }
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
