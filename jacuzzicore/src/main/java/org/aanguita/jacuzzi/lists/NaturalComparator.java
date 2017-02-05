package org.aanguita.jacuzzi.lists;

import java.util.Comparator;

/**
 * This class implements the Comparator interface for objects that implement the Comparable interface. It is used in
 * the sort algorithm of the Lists class, so that a single implementation with the Comparator interface can be used,
 * making another implementation with comparable objects unnecessary
 */
class NaturalComparator<T extends Comparable> implements Comparator<T> {

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else //noinspection SimplifiableIfStatement
            if (!(obj instanceof NaturalComparator)) {
                return false;
            } else {
                return equals(obj);
            }
    }

    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }
}
