package jacz.util.lists;

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
}
