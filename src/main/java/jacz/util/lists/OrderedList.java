package jacz.util.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list where elements are maintained in order. Insertions and searches are performed using the binary search algorithm
 */
public class OrderedList<T extends Comparable<T>> implements Serializable {

    /**
     * List of elements that is maintained in order
     */
    private final List<T> list;

    public OrderedList() {
        list = new ArrayList<>();
    }

    public void clear() {
        list.clear();
    }

    public int size() {
        return list.size();
    }

    public void add(T element) {
        list.add(indexOfInsertion(element), element);
    }

    private int indexOfInsertion(T element) {
        int index = Collections.binarySearch(list, element);
        if (index < 0) {
            index = - (index + 1);
        }
        return index;
    }

    public void addSmallest(T element) {
        if (size() > 0 && list.get(0).compareTo(element) < 0) {
            throw new IllegalArgumentException(element + " is not smaller or equal than " + list.get(0));
        }
        list.add(0, element);
    }

    public void addGreatest(T element) {
        if (size() > 0 && list.get(size() - 1).compareTo(element) > 0) {
            throw new IllegalArgumentException(element + " is not greater or equal than " + list.get(size() - 1));
        }
        list.add(size() - 1, element);
    }

    public T get(int index) {
        return list.get(index);
    }

    public T remove(int index) {
        return list.remove(index);
    }

    public boolean removeElement(T element) {
        int index = Collections.binarySearch(list, element);
        if (index >= 0) {
            list.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public int indexOfSmallerThan(T element) {
        int index = indexOfInsertion(element);
        while (index >= 0 && list.get(index).compareTo(element) >= 0) {
            index--;
        }
        return index;
    }

    public int indexOfSmallerOrEqualThan(T element) {
        int index = indexOfInsertion(element);
        while (index >= 0 && list.get(index).compareTo(element) > 0) {
            index--;
        }
        return index;
    }

    public int indexOfGreaterOrEqualThan(T element) {
        int index = indexOfInsertion(element);
        while (index < size() && list.get(index).compareTo(element) < 0) {
            index++;
        }
        return index;
    }

    public int indexOfGreaterThan(T element) {
        int index = indexOfInsertion(element);
        while (index < size() && list.get(index).compareTo(element) <= 0) {
            index++;
        }
        return index;
    }
}
