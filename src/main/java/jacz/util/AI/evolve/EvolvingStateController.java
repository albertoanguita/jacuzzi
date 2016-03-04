package jacz.util.AI.evolve;

/**
 * Created by Alberto on 28/02/2016.
 */
public interface EvolvingStateController<S, G> {

    S state();

    void setState(S newState);

    G goal();

    void setGoal(G newGoal);

    void setGoal(G newGoal, boolean evolve);

    void evolve();

    void stateHasChanged();
}
