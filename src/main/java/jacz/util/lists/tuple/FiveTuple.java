package jacz.util.lists.tuple;

/**
 * Created by Alberto on 11/12/2015.
 */
public class FiveTuple<X, Y, Z, U, V> extends FourTuple<X, Y, Z, U> {

    public final V element5;

    public FiveTuple(X element1, Y element2, Z element3, U element4, V element5) {
        super(element1, element2, element3, element4);
        this.element5 = element5;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FiveTuple<?, ?, ?, ?, ?> fiveTuple = (FiveTuple<?, ?, ?, ?, ?>) o;

        return element5.equals(fiveTuple.element5);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + element5.hashCode();
        return result;
    }
}
