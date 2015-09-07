package jacz.util.lists;

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
}
