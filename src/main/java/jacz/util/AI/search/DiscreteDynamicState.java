package jacz.util.AI.search;

/**
 * Created by Alberto on 26/02/2016.
 */
public class DiscreteDynamicState<S, G> extends DynamicState<S, G> {

    private G goal;

    public DiscreteDynamicState(S state, G goal, Transitions<S, G> transitions) {
        super(state, goal, transitions);
    }
}
