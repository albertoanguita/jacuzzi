package aanguita.jacuzzi.lists.tuple;

/**
 * A triple of three generic elements
 */
public class Triple<X, Y, Z> extends Duple<X, Y> {

    public final Z element3;

    public Triple(X element1, Y element2, Z element3) {
        super(element1, element2);
        this.element3 = element3;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;

        return element3.equals(triple.element3);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + element3.hashCode();
        return result;
    }
}
