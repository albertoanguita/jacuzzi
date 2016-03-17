package jacz.util.concurrency.timer;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskSemaphore;
import jacz.util.id.AlphaNumFactory;

/**
 * This class offers a timer which activates after a specified time, invoking a given method. The timer will keep
 * activating itself periodically, until the given method tells it to stop.
 * <p/>
 * Note that if you have activated a timer and then you assign your Timer object a null value or any other Timer,
 * the first timer will still activate itself. You must stop it first!!!
 * <p/>
 * todo check synchronization
 */
public class Timer<T> {

    private class ConcurrentGoOffTask implements Runnable {

        private final Timer timer;

        private ConcurrentGoOffTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            timer.sequentialGoOff();
        }
    }

    private class ConcurrentResetTask implements Runnable {

        private Timer timer;

        private Double factorWithLastDelay;

        private Long millis;

        private ConcurrentResetTask(Timer timer) {
            this(timer, null, null);
        }

        private ConcurrentResetTask(Timer timer, Double factorWithLastDelay) {
            this(timer, factorWithLastDelay, null);
        }

        private ConcurrentResetTask(Timer timer, Long millis) {
            this(timer, null, millis);
        }

        private ConcurrentResetTask(Timer timer, Double factorWithLastDelay, Long millis) {
            this.timer = timer;
            this.factorWithLastDelay = factorWithLastDelay;
            this.millis = millis;
        }

        @Override
        public void run() {
            if (factorWithLastDelay == null && millis == null) {
                timer.sequentialReset();
            } else if (factorWithLastDelay != null) {
                timer.sequentialReset(factorWithLastDelay);
            } else {
                timer.sequentialReset(millis);
            }
        }
    }

    private class ConcurrentStopTask implements Runnable {

        private Timer timer;

        private ConcurrentStopTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            timer.sequentialStop();
        }
    }

    private class ConcurrentKillTask implements Runnable {

        private Timer timer;

        private ConcurrentKillTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            timer.sequentialKill();
        }
    }

    private final String id;

    private final TimerAction timerAction;

    private TaskSemaphore tfi;

    private boolean active;

    private boolean alive;

    /**
     * This object is used for synchronizing concurrent accesses to the active attribute. We need this because we want a non-synched
     * method that retrieves the value of active
     */
    private final Object activeSynch;

    private long millis;

    private long activationTime;

    private final Object activationTimeAndMillisSynch;

    private boolean isExecutingWakeUp;

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
        active = false;
        alive = true;
        activeSynch = new Object();
        activationTimeAndMillisSynch = new Object();
        isExecutingWakeUp = false;
        this.threadName = threadName;
        if (start) {
            start();
        }
    }

    public String getId() {
        return id;
    }

    private void start() {
        if (alive) {
            active = true;
            wakeUpTask = new WakeUpTask(this);
            tfi = ParallelTaskExecutor.executeTask(wakeUpTask, threadName + "/Timer");
            setActivationTime();
        }
    }

    private void setActivationTime() {
        synchronized (activationTimeAndMillisSynch) {
            activationTime = System.currentTimeMillis();
        }
    }

    synchronized boolean wakeUp(WakeUpTask wakeUpTask) {
        // first check wake up task object is the one we last activated (null is anonymous, always valid)
        isExecutingWakeUp = true;
        try {
            if (wakeUpTask == null || wakeUpTask == this.wakeUpTask) {
                Long timerAction;
                timerAction = this.timerAction.wakeUp(this);
                synchronized (activeSynch) {
                    active = timerAction == null || timerAction > 0;
                }
                synchronized (activationTimeAndMillisSynch) {
                    if (timerAction != null) {
                        millis = timerAction;
                    }
                    setActivationTime();
                }
                return active;
            } else {
                // this is an old wake up task -> kill it
                return false;
            }
        } finally {
            isExecutingWakeUp = false;
        }
    }

    public boolean isRunning() {
        synchronized (activeSynch) {
            return active;
        }
    }

    synchronized long getMillis() {
        return millis;
    }

    public long remainingTime() {
        synchronized (activationTimeAndMillisSynch) {
            long remainingTime = activationTime + millis - System.currentTimeMillis();
            // prevent from returning negative values
            return Math.max(remainingTime, 1);
        }
    }

    private synchronized void sequentialGoOff() {
        checkIsNotExecutingWakeUp();
        sequentialStop();
        wakeUp(null);
        start();
    }

    /**
     * Makes the timer go off, no matter how much time is left. The timer is reset with the same time as before
     */
    public void goOff() {
        ParallelTaskExecutor.executeTask(new ConcurrentGoOffTask(this), "timer-goOff");
    }

    private synchronized void sequentialReset() {
        checkIsNotExecutingWakeUp();
        sequentialReset(millis);
    }

    private synchronized void sequentialReset(double factorWithLastDelay) {
        // only reset time if remaining time is not reduced
        checkIsNotExecutingWakeUp();
        if (factorWithLastDelay < 0.0d) {
            factorWithLastDelay = 0.0d;
        } else if (factorWithLastDelay > 1.0d) {
            factorWithLastDelay = 1.0d;
        }
        long resetTime = (long) (millis * factorWithLastDelay);
        if (!active || (resetTime > remainingTime())) {
            sequentialReset((long) (millis * factorWithLastDelay));
        }
    }

    private synchronized void sequentialReset(long millis) {
        checkIsNotExecutingWakeUp();
        sequentialStop();
        synchronized (activationTimeAndMillisSynch) {
            this.millis = millis;
        }
        start();
    }

    public void reset() {
        ParallelTaskExecutor.executeTask(new ConcurrentResetTask(this), "timer-reset");
    }

    public void reset(double factorWithLastDelay) {
        ParallelTaskExecutor.executeTask(new ConcurrentResetTask(this, factorWithLastDelay), "timer-resetWithDelay");
    }

    public void reset(long millis) {
        ParallelTaskExecutor.executeTask(new ConcurrentResetTask(this, millis), "timer-resetWithMillis");
    }


    /**
     * Stops this timer. Note that you must stop a timer before assigning the variable to another timer, or null.
     * <p/>
     * Imagine you are using the object timer of the class Timer, and it has already been started through any of the
     * constructors. If you do timer = null or timer = new Timer(...) before doing timer.stop(), the old Timer will
     * still be working and will eventually invoke the wakeUp method.
     */
    private synchronized void sequentialStop() {
        checkIsNotExecutingWakeUp();
        if (active) {
            tfi.interrupt();
            active = false;
        }
    }

    /**
     * The timer can still be reset
     */
    public void stop() {
        ParallelTaskExecutor.executeTask(new ConcurrentStopTask(this), "timer-stop");
    }

    /**
     * Stops and kills this timer. The timer cannot be reused again, even with reset.
     * Note that you must stop a timer before assigning the variable to another timer, or null.
     * <p/>
     * Imagine you are using the object timer of the class Timer, and it has already been started through any of the
     * constructors. If you do timer = null or timer = new Timer(...) before doing timer.stop(), the old Timer will
     * still be working and will eventually invoke the wakeUp method.
     */
    private synchronized void sequentialKill() {
        alive = false;
        sequentialStop();
    }

    /**
     * The timer cannot be reset again
     */
    public void kill() {
        ParallelTaskExecutor.executeTask(new ConcurrentKillTask(this), "timer-kill");
    }

    private void checkIsNotExecutingWakeUp() {
        if (isExecutingWakeUp) {
            throw new RuntimeException("Cannot access goOff, reset or stop methods during the execution of wakeUp routine");
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
