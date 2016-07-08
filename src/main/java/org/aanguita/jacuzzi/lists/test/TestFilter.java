package org.aanguita.jacuzzi.lists.test;

import org.aanguita.jacuzzi.lists.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
public class TestFilter {

    public static void main(String args[]) {

        List<FiltInt> list = new ArrayList<>();
        list.add(new FiltInt(0));
        list.add(new FiltInt(1));
        list.add(new FiltInt(2));
        list.add(new FiltInt(5));
        list.add(new FiltInt(6));
        list.add(new FiltInt(8));
        list.add(new FiltInt(9));
        list.add(new FiltInt(10));

        List<Boolean> mask = Lists.filterElements(list, 5, 4);
        List<Boolean> mask2 = Lists.filterElements(list, false);

        System.out.println(list);
        System.out.println(mask);
        System.out.println(mask2);
    }
}
