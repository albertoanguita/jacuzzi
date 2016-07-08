package org.aanguita.jacuzzi.maps;

import java.util.*;

/**
 * A map-based cache which keeps track of the count of added elements and automatically erases the oldest ones (accessed longer ago) when
 * the max allowed number is exceeded.
 */
public class ElementCacheMap<T, S> {

    private static class AnnotatedValue<T, S> implements Comparable<AnnotatedValue<T, S>> {

        final S value;

        final long date;

        final T key;

        private AnnotatedValue(S value, long date, T key) {
            this.value = value;
            this.date = date;
            this.key = key;
        }

        @Override
        public int compareTo(AnnotatedValue<T, S> o) {
            if (date < o.date) {
                return -1;
            } else if (date > o.date) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Table with the stored elements
     */
    private final Map<T, AnnotatedValue<T, S>> map;

    /**
     * Ordered queue of paths, that allows finding the oldest path when size exceeds the max allowed
     */
    private final PriorityQueue<AnnotatedValue<T, S>> orderedValues;

    /**
     * Max allowed element count
     */
    private final int maxSize;


    public ElementCacheMap(int maxSize) {
        map = new HashMap<>();
        orderedValues = new PriorityQueue<>();
        this.maxSize = maxSize;
    }

    public void put(T key, S value) {
        map.put(key, new AnnotatedValue<>(value, System.currentTimeMillis(), key));
        adjustSize();
    }

    private void adjustSize() {
        while (map.size() > maxSize) {
            AnnotatedValue<T, S> oldestValue = orderedValues.poll();
            map.remove(oldestValue.key);
        }
    }

    public S get(T key) {
        AnnotatedValue<T, S> value = map.get(key);
        return value != null ? value.value : null;
    }

    public boolean contains(T key) {
        return map.containsKey(key);
    }

    public S remove(T key) {
        AnnotatedValue<T, S> removedValue = map.remove(key);
        return removedValue != null ? removedValue.value : null;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public Set<T> keySet() {
        return map.keySet();
    }

    public Collection<S> values() {
        Collection<S> values = new HashSet<>();
        for (AnnotatedValue<T, S> annotatedValues : map.values()) {
            values.add(annotatedValues.value);
        }
        return values;
    }
}
