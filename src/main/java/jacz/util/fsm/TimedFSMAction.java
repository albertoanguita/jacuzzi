package jacz.util.fsm;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 18-abr-2010<br>
 * Last Modified: 18-abr-2010
 */
public interface TimedFSMAction<T, Y> extends GenericFSMAction<T, Y> {

    public void timedOut(T currentState);
}
