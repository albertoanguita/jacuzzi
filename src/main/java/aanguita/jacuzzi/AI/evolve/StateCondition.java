package aanguita.jacuzzi.AI.evolve;

/**
 * Created by Alberto on 21/03/2016.
 */
public interface StateCondition<S> {

    boolean isInCondition(S state);
}
