package org.aanguita.jacuzzi.concurrency.timer;

/**
 * A task associated to a timer with a generic parameter
 */
public interface ParametrizedTimerAction<T> {

    /**
     * This method is invoked by a timer when its specified delay concludes (finished = true)
     *
     * @param timer the timer that invokes this action
     * @return this value indicates the timer what it must do after the invocation to this method is complete:
     *         - A positive value: the timer will restart with the delay indicated by this value (in millis)
     *         - Null: the timer will restart with the last given delay
     *         - Zero or negative value: the timer will be stopped (can be restarted again using reset())
     */
    Long wakeUp(ParametrizedTimer<T> timer, T data);
}
