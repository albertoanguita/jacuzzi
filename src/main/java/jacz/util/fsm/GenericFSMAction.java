package jacz.util.fsm;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-mar-2010<br>
 * Last Modified: 09-mar-2010
 */
public interface GenericFSMAction<T, Y> {

    public T processInput(T currentState, Y input) throws IllegalArgumentException;

    public T init();

    public boolean isFinalState(T state);

    public void stopped();
}
