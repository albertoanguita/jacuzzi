package jacz.util.lists.tuple;

/**
 * A 4-tuple of four generic elements
 */
public class FourTuple<X, Y, Z, U> extends Triple<X, Y, Z> {

    public final U element4;

    public FourTuple(X element1, Y element2, Z element3, U element4) {
        super(element1, element2, element3);
        this.element4 = element4;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FourTuple<?, ?, ?, ?> that = (FourTuple<?, ?, ?, ?>) o;

        return element4.equals(that.element4);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + element4.hashCode();
        return result;
    }
}
