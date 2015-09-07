package jacz.util.lists.test;

import jacz.util.lists.Filterable;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
public class FiltInt implements Filterable {

    private Integer i;

    public FiltInt(Integer i) {
        this.i = i;
    }

    @Override
    public boolean filter(Object filter) {
        if (filter instanceof Integer) {
            return i.compareTo((Integer) filter) == 0;
        } else if (filter instanceof Boolean) {
            Boolean b = (Boolean) filter;
            return b ? i > 0 : i <= 0;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return i.toString();
    }
}
