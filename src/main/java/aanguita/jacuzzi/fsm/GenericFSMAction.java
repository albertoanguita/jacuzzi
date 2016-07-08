package aanguita.jacuzzi.fsm;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-mar-2010<br>
 * Last Modified: 09-mar-2010
 */
public interface GenericFSMAction<T, Y> {

    T processInput(T currentState, Y input);

    T init();

    boolean isFinalState(T state);

    void stopped();

    void raisedUnhandledException(Exception e);
}
