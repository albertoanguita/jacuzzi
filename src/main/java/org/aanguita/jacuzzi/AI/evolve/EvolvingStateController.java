package org.aanguita.jacuzzi.AI.evolve;

/**
 * Created by Alberto on 28/02/2016.
 */
public interface EvolvingStateController<S, G> {

    void stateHasChanged();

    void setState(S newState);

    void setGoal(G newGoal);

    void setGoal(G newGoal, boolean evolve);
}
