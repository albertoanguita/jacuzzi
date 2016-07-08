package aanguita.jacuzzi.lists.test;

import aanguita.jacuzzi.lists.tuple.Duple;
import aanguita.jacuzzi.lists.tuple.Triple;

/**
 * Created by Alberto on 21/12/2015.
 */
public class TestDuple {

    public static void main(String[] args) {

        Duple<String, Integer> duple1 = new Duple<>("hello", 5);
        Duple<String, Integer> duple2 = new Duple<>("hi", 5);
        Duple<String, Integer> duple3 = new Duple<>("hello", 5);
        Duple<String, Integer> duple4 = new Duple<>("hello", 7);

        System.out.println(duple1);
        System.out.println(duple1.equals(duple2));
        System.out.println(duple1.equals(duple3));
        System.out.println(duple1.equals(duple4));

        Triple<String, Integer, Boolean> triple1 = new Triple<>("hello", 5, true);
        Triple<String, Integer, Boolean> triple2 = new Triple<>("hello", 5, true);

        System.out.println(triple1);
        System.out.println(triple1.equals(triple2));
    }
}
