package jacz.util.sets;

import java.util.*;

/**
 * A set implementation that takes is able to store duplicate elements. The class is backed by a HashMap that stores
 * the element count for each item stored
 */
public class DuplicateHashSet<E> implements Set<E> {

    private static class PrivateIterator<E> implements Iterator<E> {

        private DuplicateHashSet<E> set;

        private Iterator<E> it;

        private E currentKey;

        private int currentCounter;

        private PrivateIterator(DuplicateHashSet<E> set) {
            this.set = set;
            it = set.map.keySet().iterator();
            if (it.hasNext()) {
                currentKey = it.next();
                currentCounter = 0;
                moveToValid();
            }
        }

        private boolean moveToValid() {
            if (currentCounter == set.map.get(currentKey)) {
                if (it.hasNext()) {
                    currentKey = it.next();
                    currentCounter = 0;
                    return moveToValid();
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        @Override
        public boolean hasNext() {
            return moveToValid();
        }

        @Override
        public E next() {
            if (moveToValid()) {
                E ret = currentKey;
                currentCounter++;
                moveToValid();
                return ret;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            // ignore
        }
    }

    private Map<E, Integer> map;

    private int size;

    public DuplicateHashSet() {
        this.map = new HashMap<E, Integer>();
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) throws ClassCastException {
        //noinspection SuspiciousMethodCalls
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return new PrivateIterator<E>(this);
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        int index = 0;
        for (E e : map.keySet()) {
            for (; index < index + map.get(e); index++) {
                array[index] = e;
            }
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] r = a.length >= size() ? a :
                (T[]) java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), size());
        int index = 0;
        for (E e : map.keySet()) {
            for (; index < index + map.get(e); index++) {
                r[index] = (T) e;
            }
        }
        return r;
    }

    @Override
    public boolean add(E e) {
        boolean ret = false;
        if (!map.containsKey(e)) {
            map.put(e, 0);
            ret = true;
        }
        map.put(e, map.get(e) + 1);
        return ret;
    }

    @Override
    public boolean remove(Object o) throws ClassCastException {
        //noinspection SuspiciousMethodCalls
        if (map.containsKey(o)) {
            map.put((E) o, map.get(o) - 1);
            if (map.get(o) == 0) {
                map.remove(o);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = false;
        for (E e : c) {
            ret = ret || add(e);
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        Set<E> keySet = new HashSet<E>(map.keySet());
        for (E e : keySet) {
            if (!c.contains(e)) {
                map.remove(e);
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = false;
        for (Object e : c) {
            ret = ret || remove(e);
        }
        return ret;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
