package jacz.util.lists;

/**
 * A 4-tuple of four generic elements
 */
public class Four_Tuple<X, Y, Z, U> extends Triple<X, Y, Z> {

    public final U element4;

    public Four_Tuple(X element1, Y element2, Z element3, U element4) {
        super(element1, element2, element3);
        this.element4 = element4;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element4;
    }
}
