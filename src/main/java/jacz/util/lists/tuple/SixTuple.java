package jacz.util.lists.tuple;

/**
 * Created by Alberto on 11/12/2015.
 */
public class SixTuple<X, Y, Z, U, V, W> extends FiveTuple<X, Y, Z, U, V> {

    public final W element6;

    public SixTuple(X element1, Y element2, Z element3, U element4, V element5, W element6) {
        super(element1, element2, element3, element4, element5);
        this.element6 = element6;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element6;
    }
}
