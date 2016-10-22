package org.aanguita.jacuzzi.fsm;

/**
 * The definitions for an fsm. By implementing this interface, we can define the initial state of an fsm
 * and how it transitions due to new inputs
 */
public interface FSMAction<T, Y> {

    /**
     * Returns the initial state for the fsm
     *
     * @return the initial state for the fsm
     */
    T initialState();

    /**
     * Performs a transition due to a new input
     *
     * @param currentState the current state of the fsm
     * @param input        the new input
     * @return the new state of the fsm
     */
    T processInput(T currentState, Y input);

    /**
     * Indicates if a given state if final
     *
     * @param state the state to evaluate
     * @return true if the given state is final, false otherwise
     */
    boolean isFinalState(T state);

    /**
     * Reports that one of the implemented operations raised an unhandled throwable
     *
     * @param t the unhandled exception
     */
    void raisedUnhandledException(Throwable t);
}
