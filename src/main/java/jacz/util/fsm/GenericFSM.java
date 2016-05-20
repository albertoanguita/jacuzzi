package jacz.util.fsm;

import jacz.util.id.AlphaNumFactory;

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
        currentState = genericFSMAction.init();
        checkFinalState();
        started.set(true);
        return isActive();
    }

    private void checkFinalState() {
        if (genericFSMAction.isFinalState(currentState)) {
            active.set(false);
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
        } catch (IllegalArgumentException e) {
            active.set(false);
            return false;
        }
        checkFinalState();
        return isActive();
    }

    public void stop() {
        if (active.getAndSet(false)) {
            genericFSMAction.stopped();
        }
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
