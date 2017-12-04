package org.aanguita.jacuzzi.goal;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;

import java.util.concurrent.TimeoutException;

/**
 * Created by Alberto on 12/03/2017.
 * todo remove synchronizeds so state and goal can be fetched during transitions
 */
public abstract class AbstractGoalExecutor<S> implements GoalExecutor<S> {

    protected S state;

    protected S goal;

    private final StateHooks<S> stateHooks;

    private final SimpleSemaphore atDesiredState;

    public AbstractGoalExecutor(S initialState, String threadName) {
        this.state = initialState;
        this.goal = initialState;
        stateHooks = new StateHooks<>(initialState, threadName + ".GoalExecutor.StateHooks");
        atDesiredState = new SimpleSemaphore();
    }

    public synchronized S getState() {
        return state;
    }

    @Override
    public synchronized void setState(S newState) {
        if (!state.equals(newState)) {
            this.state = newState;
            setAtDesiredState();
            stateHooks.setState(newState);
        }
    }

    public synchronized S getGoal() {
        return goal;
    }

    @Override
    public synchronized void setGoal(S newGoal) {
        if (!goal.equals(newGoal)) {
            goal = newGoal;
            setAtDesiredState();
        }
    }

    private void setAtDesiredState() {
        if (state.equals(goal)) {
            atDesiredState.resume();
        } else {
            atDesiredState.pause();
        }
    }

    @Override
    public synchronized boolean hasReachedGoal() {
        return state.equals(goal);
    }

    @Override
    public synchronized void addEnterStateHook(S state, Runnable task) {
        stateHooks.addEnterStateHook(state, task);
    }

    @Override
    public synchronized void removeEnterStateHook(S state, Runnable task) {
        stateHooks.removeEnterStateHook(state, task);
    }

    @Override
    public synchronized void setPeriodicStateHook(S state, Runnable task, long delay) {
        stateHooks.setPeriodicStateHook(state, task, delay);
    }

    @Override
    public synchronized void removePeriodicStateHook(S state) {
        stateHooks.removePeriodicStateHook(state);
    }

    @Override
    public synchronized void addExitStateHook(S state, Runnable task) {
        stateHooks.addExitStateHook(state, task);
    }

    @Override
    public synchronized void removeExitStateHook(S state, Runnable task) {
        stateHooks.removeExitStateHook(state, task);
    }

    @Override
    public void blockUntilGoalReached() {
        atDesiredState.access();
    }

    @Override
    public void blockUntilGoalReached(long timeout) throws TimeoutException {
        atDesiredState.access(timeout);
    }

    @Override
    public void stop() {
        stateHooks.stop();
    }
}
