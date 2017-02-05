package org.aanguita.jacuzzi.AI.evolve;

import java.util.function.Predicate;

/**
 * Created by Alberto on 21/03/2016.
 */
class CompareStatePredicate<S> implements Predicate<S> {

    private final S state;

    public CompareStatePredicate(S state) {
        this.state = state;
    }

    @Override
    public boolean test(S state) {
        return this.state.equals(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompareStatePredicate<?> that = (CompareStatePredicate<?>) o;

        return state.equals(that.state);

    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
