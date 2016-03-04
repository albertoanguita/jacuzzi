package jacz.util.AI.evolve;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 26/02/2016.
 */
public class DiscreteEvolvingState<S, G> extends EvolvingState<S, G, S> {

    public DiscreteEvolvingState(S initialState, G goal, Transitions<S, G> transitions) {
        super(initialState, goal, transitions);
    }

    public synchronized void setState(S newState) {
        if (!state.equals(newState)) {
            state = newState;
            stateHasChanged();
        }
    }

    public synchronized void setStateTimer(S state, long millis) {
        super.setStateTimer(state, millis);
    }

    public synchronized void setStateTimer(S state, long millis, Runnable runnable) {
        super.setStateTimer(state, millis, runnable);
    }

    public synchronized void removeStateTimer(S state) {
        super.removeStateTimer(state);
    }



    @Override
    protected boolean stateIsInPortion(S state, S portion) {
        return state.equals(portion);
    }

    @Override
    protected Set<S> matchingPortions(S state, Set<S> portions) {
        Set<S> matchedPortions = new HashSet<>();
        if (portions.contains(state)) {
            matchedPortions.add(state);
        }
        return matchedPortions;
    }
}
