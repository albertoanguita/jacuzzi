package jacz.util.AI.evolve;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 27/02/2016.
 */
public class ContinuousEvolvingState<S, G> extends EvolvingState<S, G, ContinuousEvolvingState.StatePortion<S>> {

    public interface StatePortion<S> {

        boolean isInPortion(S state);
    }

    public ContinuousEvolvingState(S state, G initialGoal, Transitions<S, G> transitions) {
        super(state, initialGoal, transitions);
    }

    @Override
    public void setState(S newState) {
        // forbidden
        throw new IllegalStateException("State cannot be modified in " + this.getClass().getName());
    }

    @Override
    public synchronized void stateHasChanged() {
        super.stateHasChanged();
    }

    public synchronized void setStateTimer(ContinuousEvolvingState.StatePortion<S> portion, long millis) {
        super.setStateTimer(portion, millis);
    }

    public synchronized void setStateTimer(ContinuousEvolvingState.StatePortion<S> portion, long millis, Runnable runnable) {
        super.setStateTimer(portion, millis, runnable);
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
