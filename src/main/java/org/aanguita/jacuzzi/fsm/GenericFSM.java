package org.aanguita.jacuzzi.fsm;

import org.aanguita.jacuzzi.id.AlphaNumFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic Finite State Machine implementation. User provides the state definition and transitions. The class controls the FSM life cycle
 */
public class GenericFSM<T, Y> {

    protected String id;

    private String name;

    private GenericFSMAction<T, Y> genericFSMAction;

    protected T currentState;

    protected AtomicBoolean active;

    protected AtomicBoolean started;

    public GenericFSM(GenericFSMAction<T, Y> genericFSMAction) {
        initialize("unnamedFSM", genericFSMAction);
    }

    public GenericFSM(String name, GenericFSMAction<T, Y> genericFSMAction) {
        initialize(name, genericFSMAction);
    }

    private void initialize(String name, GenericFSMAction<T, Y> genericFSMAction) {
        id = AlphaNumFactory.getStaticId();
        this.name = name;
        this.genericFSMAction = genericFSMAction;
        active = new AtomicBoolean(true);
        started = new AtomicBoolean(false);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public T getCurrentState() {
        return currentState;
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean start() {
        try {
            currentState = genericFSMAction.init();
            checkFinalState();
            started.set(true);
        } catch (Exception e) {
            unhandledException(e);
        }
        return isActive();
    }

    private void checkFinalState() {
        try {
            if (genericFSMAction.isFinalState(currentState)) {
                active.set(false);
            }
        } catch (Exception e) {
            unhandledException(e);
        }
    }

    public boolean newInput(Y input) {
        if (!started.get()) {
            start();
        }
        if (!isActive()) {
            return false;
        }
        try {
            currentState = genericFSMAction.processInput(currentState, input);
        } catch (Exception e) {
            unhandledException(e);
            return false;
        }
        checkFinalState();
        return isActive();
    }

    public void stop() {
        if (active.getAndSet(false)) {
            try {
                genericFSMAction.stopped();
            } catch (Exception e) {
                unhandledException(e);
            }
        }
    }

    private void unhandledException(Exception e) {
        genericFSMAction.raisedUnhandledException(e);
        active.set(false);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == GenericFSM.class && id.equals(((GenericFSM) obj).id);
    }

    @Override
    public String toString() {
        return name + "{id: " + id + "}";
    }
}
