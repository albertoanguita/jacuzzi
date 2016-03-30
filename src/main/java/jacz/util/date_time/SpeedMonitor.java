package jacz.util.date_time;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.timer.TimerAction;
import jacz.util.concurrency.timer.Timer;
import jacz.util.lists.tuple.Duple;
import jacz.util.numeric.range.LongRange;
import jacz.util.numeric.range.Range;
import jacz.util.queues.TimedQueue;

import java.util.List;

/**
 * This class contains logic and methods that allow measuring the speed of a process. The progress of a process is
 * measured by the quantity achieved (a long). The measure process can be set up to consider a specific amount of
 * past time (for example, average speed in the last 10 minutes).
 */
public class SpeedMonitor implements TimerAction, TimedQueue.TimedQueueInterface<Long> {

    /**
     * This class describes states in which a SpeedMonitor object can be. There are three possible states: no elements
     * have been inserted yet, one element has been inserted, and more than one elements have been inserted (rest).
     * The initial state is no elements inserted, followed by one element inserted and finally by rest (from which
     * the object never leaves). This state matters in different calculations, like the average speed.
     */
//    public enum State {
//        NO_ELEMENTS_INSERTED,
//        ONE_ELEMENT_INSERTED,
//        REST;
//
//        State next() {
//            if (this == NO_ELEMENTS_INSERTED) {
//                return ONE_ELEMENT_INSERTED;
//            } else {
//                return REST;
//            }
//        }
//    }

    /**
     * Time mark when measure process was initiated
     */
    protected final long initialTimeMark;

    /**
     * This variable indicates whether we are currently still within the time designated to measure (e.g. time to
     * measure is 10 seconds and the object was created 5 seconds ago). It is only useful in the average speed
     * calculation
     */
    protected boolean outOfInitialRange;

    /**
     * This list represents the progressed quantities, together with their time marks. The first element is always
     * the oldest one. By rule, no elements older than the millisToStore value are stored (if an average speed
     * measure for longer than that is asked, it is extrapolated)
     */
//    protected List<ProgressElement> progress;
    protected TimedQueue<Long> progress;

    /**
     * This field stores the maxSize of the currently stored elements (the sum of their sizes). Storing this value
     * allows a faster speed calculation
     */
    protected long storedSize;

//    private final Timer oldElementsTimer;

    /**
     * The registered speed monitor (if any). null if no monitor is registered. This monitor will have to be invoked
     * when the speed is out of some specified range
     */
    private SpeedMonitorAction speedMonitorAction;

    /**
     * Range of speeds to check (values outside this range will provoke notifications)
     */
    private LongRange speedMonitorRange;

    /**
     * Time allowed to pass between the detection of a speed anomaly and its corresponding notification). If 0, it is
     * directly notified without elapse of time
     */
    private int millisAllowedOutOfSpeedRange;

    /**
     * Timer for reporting above speed situations
     */
    private final Timer reportSpeedAboveTimer;

    /**
     * Timer for reporting below speed situations
     */
    private final Timer reportSpeedBelowTimer;

    /**
     * Value indicating if we just reported an above speed situation (to avoid redundant notifications)
     */
    private boolean justReportedAboveSpeed;

    /**
     * Value indicating if we just reported an below speed situation (to avoid redundant notifications)
     */
    private boolean justReportedBelowSpeed;

    /**
     * State of this object (indicates how many elements were inserted so far: none, one, or more than one)
     */
//    private State state;
    public SpeedMonitor(long millisToStore) {
        this(millisToStore, null, null, -1);
    }

    public SpeedMonitor(long millisToStore, SpeedMonitorAction speedMonitorAction, LongRange speedMonitorRange, int millisAllowedOutOfSpeedRange) {
        this(millisToStore, speedMonitorAction, speedMonitorRange, millisAllowedOutOfSpeedRange, ThreadUtil.invokerName(1));
    }

    public SpeedMonitor(long millisToStore, SpeedMonitorAction speedMonitorAction, LongRange speedMonitorRange, int millisAllowedOutOfSpeedRange, String threadName) {
        initialTimeMark = System.currentTimeMillis();
        outOfInitialRange = false;
        progress = new TimedQueue<>(millisToStore, this, threadName);
        storedSize = 0;
        this.speedMonitorAction = speedMonitorAction;
        this.speedMonitorRange = speedMonitorRange;
        this.millisAllowedOutOfSpeedRange = millisAllowedOutOfSpeedRange;
        if (speedMonitorAction != null) {
            reportSpeedAboveTimer = new Timer(millisAllowedOutOfSpeedRange, this, false, threadName + "/reportSpeedAboveTimer");
            reportSpeedBelowTimer = new Timer(millisAllowedOutOfSpeedRange, this, false, threadName + "/reportSpeedBelowTimer");
        } else {
            reportSpeedAboveTimer = null;
            reportSpeedBelowTimer = null;
        }
        justReportedAboveSpeed = false;
        justReportedBelowSpeed = false;
//        state = State.NO_ELEMENTS_INSERTED;
    }

    public synchronized void setSpeedMonitorRange(LongRange newSpeedMonitorRange) {
        this.speedMonitorRange = newSpeedMonitorRange;
        checkSpeed(true);
        checkSpeed(false);
    }

    public synchronized void addProgress(long quantity) {
//        if (state != State.REST) {
//            state = state.next();
//        }
        progress.addElement(quantity);
        storedSize += quantity;
        // check (if necessary) that we have not surpassed the max speed
        if (speedMonitorAction != null) {
            // check only above limit, because after having added progress the speed is higher than before
            checkSpeed(true);
        }
    }

    public synchronized double getAverageSpeed() {
        Duple<Double, Long> speedAndTimeLapse = getAverageSpeedAndTimeLapse();
        return speedAndTimeLapse.element1;
    }

    public synchronized Duple<Double, Long> getAverageSpeedAndTimeLapse() {
        long currentTime = System.currentTimeMillis();
        if (outOfInitialRange) {
                return new Duple<>(1000d * storedSize / (double) progress.getMillisToStore(), progress.getMillisToStore());
        } else {
            if (currentTime > initialTimeMark + progress.getMillisToStore()) {
                outOfInitialRange = true;
                return getAverageSpeedAndTimeLapse();
            } else {
                // extrapolate
                if (currentTime == initialTimeMark) {
                    // make sure we don't get a divide by zero error
                    currentTime++;
                }
                    return new Duple<>(1000d * (double) storedSize / ((double) currentTime - (double) initialTimeMark), currentTime - initialTimeMark);
            }
        }
    }

//    public synchronized double getAverageSpeed(long lastMillis) {
//        Duple<Double, Long> speedAndTimeLapse = getAverageSpeedAndTimeLapse(lastMillis);
//        if (speedAndTimeLapse != null) {
//            return speedAndTimeLapse.element1;
//        } else {
//            return 0d;
//        }
//    }

//    public synchronized Duple<Double, Long> getAverageSpeedAndTimeLapse(long lastMillis) {
//        if (state == State.REST) {
//            if (lastMillis < progress.getMillisToStore()) {
//                long currentTime = System.currentTimeMillis();
//                long oldestTimeMarkAllowed = currentTime - lastMillis;
//                int index = progress.getIndexFrom(oldestTimeMarkAllowed);
//                long achievedProgress = 0;
//                for (; index < progress.size(); index++) {
//                    achievedProgress += progress.get(index);
//                }
//                return new Duple<>(1000d * (double) achievedProgress / (double) lastMillis, lastMillis);
//            } else {
//                // extrapolate by getting the average speed on the standard time
//                return getAverageSpeedAndTimeLapse();
//            }
//        } else {
//            return null;
//        }
//    }

//    private long eraseBias(final long currentTime, long millisForMeasure, final long storedSize, final List<ProgressElement> progress, int indexOfOldestElementUsed) {
//        // push half of the oldest element to avoid bias and give a more realistic speed value
//        // this helps counting with elements which were "recently" erased but that, obviously, do not count for
//        // calculating speed. By adding an additional value of half of the oldest element, we are somehow
//        // mitigating that effect. Explanation below:
//        // the average case is the oldest element is X milliseconds left until erasure, and the previous element was erased also X milliseconds ago and was the same maxSize as the oldest.
//        /*double storedSizeForSpeed = (double) storedSize;
//        if (progress.maxSize() > 0) {
//            storedSizeForSpeed += ((double) progress.get(0).quantity / 2.0d);
//        }*/
//        // todo we do not perform any bias correction. It is not clear it gives any benefits, and it complicates the
//        // calculus of when speed goes out of range. check in the future
//        return storedSize;
//    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        if (timer == reportSpeedAboveTimer) {
            justReportedAboveSpeed = true;
            Double speed = getAverageSpeed();
            if (speed != null) {
                SpeedOutOfRangeTask spmTask = new SpeedOutOfRangeTask(speedMonitorAction, true, speed);
                ParallelTaskExecutor.executeTask(spmTask);
            }
            // kill the timer
            return 0L;
        } else if (timer == reportSpeedBelowTimer) {
            justReportedBelowSpeed = true;
            Double speed = getAverageSpeed();
            if (speed != null) {
                SpeedOutOfRangeTask spmTask = new SpeedOutOfRangeTask(speedMonitorAction, false, speed);
                ParallelTaskExecutor.executeTask(spmTask);
            }
            // kill the timer
            return 0L;
        }
        return 0L;
    }

    /**
     * Since this class uses timers to monitor some stuff, this method allows telling it that we are done using it,
     * so all timers can be deactivated. It is recommended to invoke this method, because in other case unexpected
     * reminder calls can be received after some period of time
     */
    public synchronized void stop() {
//        if (oldElementsTimer != null) {
//            oldElementsTimer.stop();
//        }
        progress.stop();
        if (reportSpeedAboveTimer != null) {
            reportSpeedAboveTimer.kill();
        }
        if (reportSpeedBelowTimer != null) {
            reportSpeedBelowTimer.kill();
        }
    }

    private synchronized void checkSpeed(boolean above) {
        Double speed = getAverageSpeed();
        if (speed == null) {
            // no speed measure yet
            // todo check this
            return;
        }
        if (above) {
            // speed has raised: we must check if either just entered above limit, or we escaped from below limit
            if (speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.LEFT) {
                // above limit
                if (!justReportedAboveSpeed) {
                    if (millisAllowedOutOfSpeedRange >= 0) {
                        if (reportSpeedAboveTimer.getState() != Timer.State.RUNNING) {
                            //reportSpeedAboveTimer = new Timer<ComplexTimerEvent>(millisAllowedOutOfSpeedRange, this, ComplexTimerEvent.SPEED_ABOVE_LIMIT);
                            reportSpeedAboveTimer.reset();
                        }
                    } else {
                        justReportedAboveSpeed = true;
                        SpeedOutOfRangeTask spmTask = new SpeedOutOfRangeTask(speedMonitorAction, true, speed);
                        ParallelTaskExecutor.executeTask(spmTask);
                    }
                }
            } else {
                justReportedAboveSpeed = false;
            }
            if (speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.CONTAINS || speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.LEFT) {
                // we are in the OK range or upper, check if we just left the below range
                if (reportSpeedBelowTimer != null && reportSpeedBelowTimer.getState() == Timer.State.RUNNING) {
                    reportSpeedBelowTimer.stop();
                }
            }
        } else {
            if (speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.RIGHT) {
                if (!justReportedBelowSpeed) {
                    if (millisAllowedOutOfSpeedRange >= 0) {
                        if (reportSpeedBelowTimer.getState() != Timer.State.RUNNING) {
                            reportSpeedBelowTimer.reset();
                        }
                    } else {
                        justReportedBelowSpeed = true;
                        SpeedOutOfRangeTask spmTask = new SpeedOutOfRangeTask(speedMonitorAction, false, speed);
                        ParallelTaskExecutor.executeTask(spmTask);
                    }
                }
            } else {
                justReportedBelowSpeed = false;
            }
            if (speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.CONTAINS || speedMonitorRange.compareTo(speed.longValue()) == Range.ValueComparison.RIGHT) {
                // we are in the OK range or lower, check if we just left the above range
                if (reportSpeedAboveTimer != null && reportSpeedAboveTimer.getState() == Timer.State.RUNNING) {
                    reportSpeedAboveTimer.stop();
                }
            }
        }
    }

    @Override
    public synchronized void elementsRemoved(List<Long> elements) {
        for (long element : elements) {
            storedSize -= element;
        }
        if (speedMonitorAction != null) {
            checkSpeed(false);
        }
    }
}
