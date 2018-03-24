package org.aanguita.jacuzzi.concurrency.timer;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;

import java.util.function.Consumer;

/**
 * This class offers a timer which activates after a specified time, invoking a given method. The timer will keep
 * activating itself periodically, until the given method tells it to stop.
 * <p>
 * Note that if you have activated a timer and then you assign your Timer object a null value or any other Timer,
 * the first timer will still activate itself. You must stop it first!!!
 */
public class Timer extends AbstractTimer {

    /**
     * The action to perform each time the timer goes off
     */
    private final TimerAction timerAction;

    public Timer(long millis, TimerAction timerAction) {
        this(millis, timerAction, ThreadUtil.invokerName(1));
    }

    public Timer(long millis, TimerAction timerAction, String threadName) {
        this(millis, timerAction, true, threadName);
    }

    public Timer(long millis, TimerAction timerAction, boolean start, String threadName) {
        this(millis, timerAction, true, threadName, null);
    }

    public Timer(long millis, TimerAction timerAction, boolean start, String threadName, Consumer<Throwable> throwableConsumer) {
        super(millis, threadName, throwableConsumer);
        this.timerAction = timerAction;
        initialize(start);
    }

    @Override
    protected Long wakeUp() {
        return timerAction.wakeUp(this);
    }
}
