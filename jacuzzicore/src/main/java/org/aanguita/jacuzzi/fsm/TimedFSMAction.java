package org.aanguita.jacuzzi.fsm;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 18-abr-2010<br>
 * Last Modified: 18-abr-2010
 */
public interface TimedFSMAction<T, Y> extends FSMAction<T, Y> {

    void timedOut(T currentState);


    /**
     * Reports that the fsm has been externally stopped
     */
    void stopped();
}
