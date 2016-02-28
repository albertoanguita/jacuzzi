package jacz.util.AI.evolve;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 27/02/2016.
 */
public class ContinuousDynamicState<S, G> extends DynamicState<S, G, ContinuousDynamicState.StatePortion<S>> {

    public interface StatePortion<S> {

        boolean isInPortion(S state);
    }

    public ContinuousDynamicState(S state, G initialGoal, Transitions<S, G> transitions) {
        super(state, initialGoal, transitions);
    }

    @Override
    protected boolean stateIsInPortion(S state, StatePortion<S> portion) {
        return portion.isInPortion(state);
    }

    @Override
    protected Set<StatePortion<S>> matchingPortions(S state, Set<StatePortion<S>> portions) {
        Set<StatePortion<S>> matchedPortions = new HashSet<>();
        for (StatePortion<S> statePortion : portions) {
            if (statePortion.isInPortion(state)) {
                matchedPortions.add(statePortion);
            }
        }
        return matchedPortions;
    }
}
