package jacz.util.AI.evolve;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 26/02/2016.
 */
public class DiscreteDynamicState<S, G> extends DynamicState<S, G, S> {

    public DiscreteDynamicState(S initialState, G goal, Transitions<S, G> transitions) {
        super(initialState, goal, transitions);
//        stateTimers = new HashMap<>();
//        enterStateHooks = new HashMap<>();
//        exitStateHooks = new HashMap<>();
    }


    public synchronized void setState(S newState) {
        if (!state.equals(newState)) {
            state = newState;
            stateHasChanged();
//            S oldState = state;
//            state = newState;
//            stateHasChanged(oldState);
//            daemon.stateChange();
        }
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
