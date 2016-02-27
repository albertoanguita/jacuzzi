package jacz.util.AI.search;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 27/02/2016.
 */
public class ContinuousDynamicState<S, G> extends DynamicState<S, G> {

    public interface StatePortion<S> {

        boolean isInPortion(S state);
    }

    private Set<StatePortion<S>> activeStatePortions;

    public ContinuousDynamicState(S initialState, G initialGoal, Transitions<S, G> transitions) {
        super(initialState, initialGoal, transitions);
        activeStatePortions = new HashSet<>();
    }

    public void setStateTimer(StatePortion<S> statePortion, long millis) {

    }

    public void removeStateTimer(StatePortion<S> statePortion) {

    }

    public void setEnterStateHook(StatePortion<S> statePortion, Runnable task) {

    }

    public void removeEnterStateHook(StatePortion<S> statePortion, Runnable task) {

    }

    public void setExitStateHook(StatePortion<S> statePortion, Runnable task) {

    }

    public void removeExitStateHook(StatePortion<S> statePortion, Runnable task) {

    }

    public void stateHasChanged() {
        daemon.stateChange();
    }
}
