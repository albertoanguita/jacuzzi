package jacz.util.lists.tuple;

/**
 * Created by Alberto on 12/12/2015.
 */
public class EightTuple<X, Y, Z, U, V, W, M, N> extends SevenTuple<X, Y, Z, U, V, W, M> {

    public final N element8;

    public EightTuple(X element1, Y element2, Z element3, U element4, V element5, W element6, M element7, N element8) {
        super(element1, element2, element3, element4, element5, element6, element7);
        this.element8 = element8;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EightTuple<?, ?, ?, ?, ?, ?, ?, ?> that = (EightTuple<?, ?, ?, ?, ?, ?, ?, ?>) o;

        return element8.equals(that.element8);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + element8.hashCode();
        return result;
    }
}
