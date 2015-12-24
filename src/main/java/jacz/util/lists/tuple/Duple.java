package jacz.util.lists.tuple;

import java.io.Serializable;

/**
 * A duple of two generic elements
 */
public class Duple<X, Y> implements Serializable {

    public final X element1;

    public final Y element2;

    public Duple(X element1, Y element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    protected String concatenate() {
        return element1 + ", " + element2;
    }

    @Override
    public String toString() {
        return "{" + concatenate() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Duple<?, ?> duple = (Duple<?, ?>) o;

        if (!element1.equals(duple.element1)) return false;
        return element2.equals(duple.element2);

    }

    @Override
    public int hashCode() {
        int result = element1.hashCode();
        result = 31 * result + element2.hashCode();
        return result;
    }
}
