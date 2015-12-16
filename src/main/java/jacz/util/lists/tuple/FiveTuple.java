package jacz.util.lists.tuple;

import jacz.util.lists.Four_Tuple;

/**
 * Created by Alberto on 11/12/2015.
 */
public class FiveTuple<X, Y, Z, U, V> extends Four_Tuple<X, Y, Z, U> {

    public final V element5;

    public FiveTuple(X element1, Y element2, Z element3, U element4, V element5) {
        super(element1, element2, element3, element4);
        this.element5 = element5;
    }

    @Override
    protected String concatenate() {
        return super.concatenate() + ", " + element5;
    }
}
