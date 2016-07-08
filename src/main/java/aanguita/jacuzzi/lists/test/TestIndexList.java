package aanguita.jacuzzi.lists.test;

import aanguita.jacuzzi.lists.IndexList;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 13-mar-2010<br>
 * Last Modified: 13-mar-2010
 */
public class TestIndexList {

    public static void main(String args[]) {

        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(2);
        list.add(7);
        list.add(14);
        list.add(1);
        list.add(-3);
        list.add(4);
        list.add(8);
        list.add(6);
        list.add(9);

        IndexList<Integer> il = new IndexList<>(list);
        il.sortIndexes(new ComparaInts(), false, 1);
        List<Integer> indexes = il.getIndexList();

        System.out.println(list);
        System.out.println(indexes);
    }
}
