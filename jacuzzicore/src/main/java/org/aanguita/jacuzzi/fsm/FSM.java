package org.aanguita.jacuzzi.fsm;

import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic Finite State Machine implementation. User provides the state definition and transitions.
 * The class controls the FSM life cycle
 * <p>
 * Both the class representing the possible states of the fsm and the class representing the inputs that
 * can be handled by the fsm are generic
 * <p>
 * The fsm implements the equals and hashCode methods, allowing it to serve as key in maps
 */
public class FSM<T, Y> extends StringIdClass {

    /**
     * User-defined transitions for this fsm
     */
    private FSMAction<T, Y> FSMAction;

    /**
     * The current state of the fsm
     */
    private T currentState;

    /**
     * Indicates if this fsm is active. An fsm dies when it reaches a final state
     */
    private final AtomicBoolean active;

    /**
     * Creates an fsm with a name and a set of given transitions
     *
     * @param FSMAction transitions
     */
    public FSM(FSMAction<T, Y> FSMAction) {
        this.FSMAction = FSMAction;
        active = new AtomicBoolean(true);
        try {
            currentState = FSMAction.initialState();
            checkFinalState();
        } catch (Throwable e) {
            unhandledException(e);
        }
    }

    /**
     * Returns the current state of the fsm
     *
     * @return the current state of the fsm
     */
    public T getCurrentState() {
        return currentState;
    }

    /**
     * Indicates if this fsm is still active
     *
     * @return true if the fsm is active. False otherwise
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Checks if we have reached a final state
     */
    private void checkFinalState() {
        try {
            if (FSMAction.isFinalState(currentState)) {
                active.set(false);
            }
        } catch (Throwable e) {
            unhandledException(e);
        }
    }

    /**
     * Provides a new input to the fsm
     *
     * @param input the new input
     * @return true if the fsm is still active after processing the new input
     */
    public boolean newInput(Y input) {
        if (!isActive()) {
            return false;
        }
        try {
            currentState = FSMAction.processInput(currentState, input);
        } catch (Throwable e) {
            unhandledException(e);
            return false;
        }
        checkFinalState();
        return isActive();
    }

    /**
     * An unhandled exception was raised by the FSMAction interface
     *
     * @param e the unhandled exception
     */
    private void unhandledException(Throwable e) {
        FSMAction.raisedUnhandledException(e);
        active.set(false);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == FSM.class && getId().equals(((FSM) obj).getId());
    }

    @Override
    public String toString() {
        return "FSM (id: " + getId() + "): state=" + currentState.toString();
    }
}
