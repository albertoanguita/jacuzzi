package aanguita.jacuzzi.lists.test;

import java.util.Comparator;

/**
 *
 */
class ComparaInts implements Comparator<Integer> {
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }
}
