package jacz.util.fsm;

import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;

/**
 * Generic Finite State Machine implementation. User provides the state definition and transitions. The class controls the FSM life cycle
 */
public class GenericFSM<T, Y> {

    protected UniqueIdentifier id;

    private String name;

    private GenericFSMAction<T, Y> genericFSMAction;

    protected T currentState;

    protected boolean active;

    protected boolean started;

    public GenericFSM(GenericFSMAction<T, Y> genericFSMAction) {
        initialize("unnamedFSM", genericFSMAction);
    }

    public GenericFSM(String name, GenericFSMAction<T, Y> genericFSMAction) {
        initialize(name, genericFSMAction);
    }

    private void initialize(String name, GenericFSMAction<T, Y> genericFSMAction) {
        id = UniqueIdentifierFactory.getOneStaticIdentifier();
        this.name = name;
        this.genericFSMAction = genericFSMAction;
        active = true;
        started = false;
    }

    public String getName() {
        return name;
    }

    public T getCurrentState() {
        return currentState;
    }

    public boolean isActive() {
        return active;
    }

    public boolean start() {
        currentState = genericFSMAction.init();
        checkFinalState();
        started = true;
        return isActive();
    }

    private void checkFinalState() {
        if (genericFSMAction.isFinalState(currentState)) {
            active = false;
        }
    }

    public boolean newInput(Y input) {
        if (!started) {
            start();
        }
        if (!isActive()) {
            return false;
        }
        try {
            currentState = genericFSMAction.processInput(currentState, input);
        } catch (IllegalArgumentException e) {
            active = false;
            return false;
        }
        checkFinalState();
        return isActive();
    }

    public void stop() {
        if (active) {
            genericFSMAction.stopped();
            active = false;
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
        return name + "{id: " + id.toString() + "}";
    }
}
