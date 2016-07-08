package aanguita.jacuzzi.sets.test;

import aanguita.jacuzzi.sets.DuplicateHashSet;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 3/08/11
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class DuplicateHashSetTest {

    public static void main(String args[]) {

        Set<String> s = new DuplicateHashSet<>();

        s.add("hola");
        s.add("hola");
        s.add("hola");
        s.add("je");
        s.add("je");
        s.remove("je");
        s.remove("holaa");
        for (String value : s) {
            System.out.println(value);
        }
        System.out.println(s.contains("holaa"));

        System.out.println("FIN");
    }
}
