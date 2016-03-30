package jacz.util.concurrency.timer;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskSemaphore;
import jacz.util.id.AlphaNumFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class offers a timer which activates after a specified time, invoking a given method. The timer will keep
 * activating itself periodically, until the given method tells it to stop.
 * <p/>
 * Note that if you have activated a timer and then you assign your Timer object a null value or any other Timer,
 * the first timer will still activate itself. You must stop it first!!!
 */
public class Timer {

    public enum State {
        RUNNING,
        STOPPED,
        KILLED
    }

    private final String id;

    private final TimerAction timerAction;

    private TaskSemaphore tfi;

    private AtomicBoolean active;

    private AtomicBoolean alive;

    private long millis;

    private long activationTime;

    private long remainingTimeWhenStopped;

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
        this.threadName = threadName;
        if (start) {
            start();
        }
    }

    public String getId() {
        return id;
    }

    private void start() {
        if (alive.get()) {
            active.set(true);
            wakeUpTask = new WakeUpTask(this);
            tfi = ParallelTaskExecutor.executeTask(wakeUpTask, threadName + "/Timer");
            setActivationTime();
        }
    }

    private synchronized void setActivationTime() {
        activationTime = System.currentTimeMillis();
    }

    boolean wakeUp(WakeUpTask wakeUpTask) {
        // first check wake up task object is the one we last activated (null is anonymous, always valid)
        AtomicBoolean validWakeUp = new AtomicBoolean(wakeUpTask == null || wakeUpTask == this.wakeUpTask);
        if (validWakeUp.get() && active.get() && alive.get()) {
            Long timerAction = this.timerAction.wakeUp(this);
            synchronized (this) {
                // recalculate active. It might have been disabled by another external call just a moment ago,
                // so we add it to the equation
                active.set(active.get() && (timerAction == null || timerAction > 0));
                if (timerAction != null) {
                    millis = timerAction;
                }
                setActivationTime();
                return active.get();
            }
        } else {
            // this is an old wake up task -> kill it
            return false;
        }
    }

//    public synchronized boolean isRunning() {
//        return active.get();
//    }

    public synchronized State getState() {
        if (!alive.get()) {
            return State.KILLED;
        } else {
            return active.get() ? State.RUNNING : State.STOPPED;
        }
    }

    synchronized long getMillis() {
        return millis;
    }

    public synchronized long remainingTime() {
        if (active.get()) {
            long remainingTime = activationTime + millis - System.currentTimeMillis();
            // prevent from returning negative values
            return Math.max(remainingTime, 1);
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
        stop();
        this.millis = millis;
        start();
    }

    /**
     * The timer can still be reset
     */
    public synchronized void stop() {
        if (active.get()) {
            remainingTimeWhenStopped = remainingTime();
            active.set(false);
            tfi.interrupt();
        }
    }

    public synchronized void resume() {
        if (!active.get()) {
            reset(remainingTimeWhenStopped);
        }
    }

    /**
     * The timer cannot be reset again
     */
    public synchronized void kill() {
        alive.set(false);
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
