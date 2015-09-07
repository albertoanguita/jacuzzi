package jacz.util.concurrency.timer;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 18-abr-2010<br>
 * Last Modified: 18-abr-2010
 */
public interface SimpleTimerAction {

    /**
     * This method is invoked by a timer when its specified delay concludes (finished = true)
     *
     * @param timer the timer that invokes this action
     * @return this value indicates the timer what it must do after the invocation to this method is complete:
     *         - A positive value: the timer will restart with the delay indicated by this value (in millis)
     *         - Null: the timer will restart with the last given delay
     *         - Zero or negative value: the timer will die
     */
    public Long wakeUp(Timer timer);
}
