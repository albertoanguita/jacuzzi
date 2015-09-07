package jacz.util.lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class DoubleElementArrayList<T, Y> implements Cloneable, Serializable {

    private ArrayList<T> tList;

    private ArrayList<Y> yList;


    public DoubleElementArrayList() {
        tList = new ArrayList<T>();
        yList = new ArrayList<Y>();
    }

    public DoubleElementArrayList(Collection<? extends T> tList, Collection<? extends Y> yList) {
        this.tList = new ArrayList<T>(tList);
        this.yList = new ArrayList<Y>(yList);
    }

    public DoubleElementArrayList(DoubleElementArrayList<? extends T, ? extends Y> doubleList) {
        tList = new ArrayList<T>(doubleList.tList);
        yList = new ArrayList<Y>(doubleList.yList);
    }

    public DoubleElementArrayList(int initialCapacity) {
        tList = new ArrayList<T>(initialCapacity);
        yList = new ArrayList<Y>(initialCapacity);
    }

    public void add(T t, Y y) {
        tList.add(t);
        yList.add(y);
    }

    public void add(int index, T t, Y y) {
        tList.add(index, t);
        yList.add(index, y);
    }

    public void addAll(Collection<? extends T> tList, Collection<? extends Y> yList) {
        this.tList.addAll(tList);
        this.yList.addAll(yList);
    }

    public void addAll(int index, Collection<? extends T> tList, Collection<? extends Y> yList) {
        this.tList.addAll(index, tList);
        this.yList.addAll(index, yList);
    }

    public void addAll(DoubleElementArrayList<? extends T, ? extends Y> doubleList) {
        this.tList.addAll(doubleList.tList);
        this.yList.addAll(doubleList.yList);
    }

    public void addAll(int index, DoubleElementArrayList<? extends T, ? extends Y> doubleList) {
        this.tList.addAll(index, doubleList.tList);
        this.yList.addAll(index, doubleList.yList);
    }

    public void clear() {
        tList.clear();
        yList.clear();
    }

    public Object clone() throws CloneNotSupportedException {
        try {
            DoubleElementArrayList<T, Y> newList = (DoubleElementArrayList) super.clone();
            newList.tList = (ArrayList<T>) tList.clone();
            newList.yList = (ArrayList<Y>) yList.clone();
            return newList;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    public ArrayList<T> cloneFirstList() {
        return (ArrayList<T>) tList.clone();
    }

    public ArrayList<Y> cloneSecondList() {
        return (ArrayList<Y>) yList.clone();
    }

    public boolean contains(T t, Y y) {
        return tList.contains(t) && yList.contains(y);
    }

    public void ensureCapacity(int minCapacity) {
        tList.ensureCapacity(minCapacity);
        yList.ensureCapacity(minCapacity);
    }

    public T getFirst(int index) {
        return tList.get(index);
    }

    public Y getSecond(int index) {
        return yList.get(index);
    }

    public int indexOf(T t, Y y) {
        int indexOfFirst = indexOfFirst(t);
        int indexOfSecond = indexOfSecond(y);
        if (indexOfFirst == indexOfSecond) {
            return indexOfFirst;
        } else {
            return -1;
        }
    }

    public int indexOfFirst(T t) {
        return tList.indexOf(t);
    }

    public int indexOfSecond(Y y) {
        return yList.indexOf(y);
    }

    public boolean isEmpty() {
        return tList.isEmpty();
    }

    public int lastIndexOf(T t, Y y) {
        int indexOfFirst = lastIndexOfFirst(t);
        int indexOfSecond = lastIndexOfSecond(y);
        if (indexOfFirst == indexOfSecond) {
            return indexOfFirst;
        } else {
            return -1;
        }
    }

    public int lastIndexOfFirst(T t) {
        return tList.lastIndexOf(t);
    }

    public int lastIndexOfSecond(Y y) {
        return yList.lastIndexOf(y);
    }

    ArrayList<Object> remove(int index) throws IndexOutOfBoundsException {
        T t = tList.remove(index);
        Y y = yList.remove(index);
        ArrayList<Object> result = new ArrayList<Object>(2);
        result.add(t);
        result.add(y);
        return result;
    }

    public boolean remove(T t, Y y) {
        int index = indexOf(t, y);
        if (index == -1) {
            return false;
        } else {
            remove(index);
            return true;
        }
    }

    public void set(int index, T t, Y y) {
        tList.set(index, t);
        yList.set(index, y);
    }

    public int size() {
        return tList.size();
    }

    public void trimToSize() {
        tList.trimToSize();
        yList.trimToSize();
    }
}
