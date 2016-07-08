package org.aanguita.jacuzzi.AI.evolve;

/**
 * Created by Alberto on 21/03/2016.
 */
public class SimpleStateCondition<S> implements StateCondition<S> {

    private final S state;

    public SimpleStateCondition(S state) {
        this.state = state;
    }

    @Override
    public boolean isInCondition(S state) {
        return this.state.equals(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleStateCondition<?> that = (SimpleStateCondition<?>) o;

        return state.equals(that.state);

    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
