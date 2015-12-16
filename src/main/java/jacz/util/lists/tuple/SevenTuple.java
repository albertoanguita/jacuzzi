package jacz.util.lists.tuple;

/**
 * Created by Alberto on 12/12/2015.
 */
public class SevenTuple<X, Y, Z, U, V, W, M> extends SixTuple<X, Y, Z, U, V, W> {

    public final M element7;

    public SevenTuple(X element1, Y element2, Z element3, U element4, V element5, W element6, M element7) {
        super(element1, element2, element3, element4, element5, element6);
        this.element7 = element7;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element7;
    }
}
