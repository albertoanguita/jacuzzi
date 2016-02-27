package jacz.util.AI.search;

import jacz.util.concurrency.timer.Timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 26/02/2016.
 */
public class DiscreteDynamicState<S, G> extends DynamicState<S, G> {

    private final Map<S, Timer> stateTimers;

    private final Map<S, Runnable> enterStateHooks;

    private final Map<S, Runnable> exitStateHooks;

    public DiscreteDynamicState(S state, G goal, Transitions<S, G> transitions) {
        super(state, goal, transitions);
        stateTimers = new HashMap<>();
        enterStateHooks = new HashMap<>();
        exitStateHooks = new HashMap<>();
    }

    public synchronized void setStateTimer(S state, long millis) {
        removeStateTimer(state);
        stateTimers.put(state, new Timer(millis, this, false, this.getClass().toString() + "(" + state.toString() + ")"));
        if (state.equals(this.state)) {
            // timer must start now
            stateTimers.get(state).reset();
        }
    }

    public synchronized void removeStateTimer(S state) {
        if (stateTimers.containsKey(state)) {
            stateTimers.remove(state).kill();
        }
    }

    public synchronized void setEnterStateHook(S state, Runnable task) {
        enterStateHooks.put(state, task);
    }

    public synchronized void removeEnterStateHook(S state) {
        enterStateHooks.remove(state);
    }

    public synchronized void setExitStateHook(S state, Runnable task) {
        exitStateHooks.put(state, task);
    }

    public synchronized void removeExitStateHook(S state) {
        exitStateHooks.remove(state);
    }

    public synchronized void setState(S newState) {
        if (!state.equals(newState)) {
            S oldState = state;
            state = newState;
            stateHasChanged(oldState);
            daemon.stateChange();
        }
    }

    private void stateHasChanged(S oldState) {
        checkStateTimers(oldState);
        checkStateHooks(oldState);
    }

    private void checkStateTimers(S oldState) {
        if (stateTimers.containsKey(oldState)) {
            // stop timer of old state
            stateTimers.get(oldState).stop();
        }
        if (stateTimers.containsKey(state)) {
            // start new state timer
            stateTimers.get(oldState).reset();
        }
    }

    private void checkStateHooks(S oldState) {
        if (exitStateHooks.containsKey(oldState)) {
            hookExecutor.executeTask(exitStateHooks.get(oldState));
        }
        if (enterStateHooks.containsKey(state)) {
            hookExecutor.executeTask(enterStateHooks.get(state));
        }
    }
}
