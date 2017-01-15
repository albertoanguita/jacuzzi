package org.aanguita.jacuzzi.concurrency.timer;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;

/**
 * Created by Alberto on 03/12/2016.
 */
public class ParametrizedTimer<T> extends AbstractTimer {

    /**
     * The action to perform each time the timer goes off
     */
    private final ParametrizedTimerAction timerAction;

    private T data;

    public ParametrizedTimer(long millis, ParametrizedTimerAction timerAction, T data) {
        this(millis, timerAction, data, ThreadUtil.invokerName(1));
    }

    public ParametrizedTimer(long millis, ParametrizedTimerAction timerAction, T data, String threadName) {
        this(millis, timerAction, data, true, threadName);
    }

    public ParametrizedTimer(long millis, ParametrizedTimerAction timerAction, T data, boolean start, String threadName) {
        super(millis, threadName);
        this.timerAction = timerAction;
        this.data = data;
        initialize(start);
    }

    @Override
    protected Long wakeUp() {
        return timerAction.wakeUp(this, data);
    }

    public synchronized void reset(T data) {
        this.data = data;
        super.reset();
    }

    public synchronized void reset(long millis, T data) {
        this.data = data;
        super.reset(millis);
    }


}