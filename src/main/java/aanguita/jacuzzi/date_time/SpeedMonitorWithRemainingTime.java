package aanguita.jacuzzi.date_time;

import aanguita.jacuzzi.concurrency.ThreadUtil;
import aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
import aanguita.jacuzzi.concurrency.timer.Timer;
import aanguita.jacuzzi.numeric.range.LongRange;

import java.util.List;

/**
 * This class adds the ability to monitor when the remaining time to fill a specific capacity falls below a given
 * value. Capacity can be added to the object in order to let more elements fit. Every time the remaining time to
 * fill such capacity falls below a given value, the object will invoke a given method. This method will also be called
 * if when adding capacity, the resulting remaining time still falls below the given mark.
 */
public class SpeedMonitorWithRemainingTime extends SpeedMonitor {

    private static class RemainingTimeTask implements Runnable {

        private final RemainingTimeAction remainingTimeAction;

        private final long remainingTime;

        public RemainingTimeTask(RemainingTimeAction remainingTimeAction, long remainingTime) {
            this.remainingTimeAction = remainingTimeAction;
            this.remainingTime = remainingTime;
        }

        @Override
        public void run() {
            remainingTimeAction.remainingTime(remainingTime);
        }
    }


    /**
     * Capacity of the "progress container" (needed to calculate remaining time until capacity is exhausted)
     */
    private long capacity;

    /**
     * Action to invoke when the remaining time falls below the established limit
     */
    private final RemainingTimeAction remainingTimeAction;

    /**
     * Value of remaining time under which we must notify
     */
    private final long remainingTimeToReport;

    /**
     * Timer for reporting low time remaining situations
     */
    private final Timer remainingTimeTimer;

    /**
     * Value indicating if we just reported a low time remaining situation (to avoid redundant notifications)
     */
    private boolean justReportedRemainingTime;

    public SpeedMonitorWithRemainingTime(long millisToStore, long capacity, RemainingTimeAction remainingTimeAction, long remainingTimeToReport) {
        this(millisToStore, capacity, remainingTimeAction, remainingTimeToReport, ThreadUtil.invokerName(1));
    }

    public SpeedMonitorWithRemainingTime(long millisToStore, long capacity, RemainingTimeAction remainingTimeAction, long remainingTimeToReport, String threadName) {
        this(millisToStore, capacity, remainingTimeAction, remainingTimeToReport, null, -1, threadName);
    }

    public SpeedMonitorWithRemainingTime(long millisToStore, long capacity, RemainingTimeAction remainingTimeAction, long remainingTimeToReport, LongRange speedMonitorRange, int millisAllowedOutOfSpeedRange, String threadName) {
        super(millisToStore, remainingTimeAction, speedMonitorRange, millisAllowedOutOfSpeedRange, threadName);
        this.capacity = capacity;
        this.remainingTimeAction = remainingTimeAction;
        this.remainingTimeToReport = remainingTimeToReport;
        remainingTimeTimer = new Timer(1, this, false, threadName);
        justReportedRemainingTime = false;
    }

    @Override
    public synchronized void addProgress(long quantity) {
        super.addProgress(quantity);
        long resetTime = checkRemainingTime(false);
        if (resetTime > 0L) {
            remainingTimeTimer.reset(resetTime);
        }
    }

    @Override
    public void elementsRemoved(List<Long> elements) {
        super.elementsRemoved(elements);
        long resetTime = checkRemainingTime(false);
        if (resetTime > 0L) {
            remainingTimeTimer.reset(resetTime);
        }
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        Long superResult = super.wakeUp(timer/*, event*/);
        if (timer == remainingTimeTimer) {
            return checkRemainingTime(false);
        } else {
            return superResult;
        }
    }

    public synchronized void addCapacity(long value) {
        capacity += value;
        long resetTime = checkRemainingTime(true);
        if (resetTime > 0L) {
            remainingTimeTimer.reset(resetTime);
        }
    }

    private long checkRemainingTime(boolean ignoreJustReportedRemainingTime) {
        double speed = getAverageSpeed();
        long remainingTime = (long) (1000.0d * ((double) capacity - (double) storedSize) / speed);
        if (remainingTime <= remainingTimeToReport) {
            if (ignoreJustReportedRemainingTime || !justReportedRemainingTime) {
                justReportedRemainingTime = true;
                RemainingTimeTask remainingTimeTask = new RemainingTimeTask(remainingTimeAction, remainingTime);
                ThreadExecutor.submit(remainingTimeTask);
            }
        } else {
            // return time for setting up new timer
            justReportedRemainingTime = false;
            return remainingTime - remainingTimeToReport + 1;
        }
        return 0L;
    }

    @Override
    public synchronized void stop() {
        super.stop();
        remainingTimeTimer.kill();
    }
}
