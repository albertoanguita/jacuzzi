package aanguita.jacuzzi.maps;

import aanguita.jacuzzi.lists.OrderedList;

import java.io.Serializable;
import java.util.*;

/**
 * A map whose indexes are comparable and allows efficiently retrieving the keys that are greater or lesser than a value
 * <p/>
 * Accessing a specific element is takes linear time
 */
public class ComparableIndexMap<K extends Comparable<K>, V> implements Serializable {

    private final Map<K, V> map;

    private final OrderedList<K> indexList;

    public ComparableIndexMap() {
        map = new HashMap<>();
        indexList = new OrderedList<>();
    }

    public void clear() {
        map.clear();
        indexList.clear();
    }

    public int size() {
        return indexList.size();
    }

    public void put(K key, V value) {
        map.put(key, value);
        indexList.add(key);
    }

    public K getKey(int index) {
        return indexList.get(index);
    }

    public V get(K key) {
        return map.get(key);
    }

    public V remove(K key) {
        if (indexList.removeElement(key)) {
            return map.remove(key);
        } else {
            return null;
        }
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Set<K> keySetSmallerThan(K key) {
        Set<K> set = new HashSet<>();
        for (int i = indexList.indexOfSmallerThan(key); i >= 0; i--) {
            set.add(indexList.get(i));
        }
        return set;
    }

    public Set<K> keySetSmallerOrEqualThan(K key) {
        Set<K> set = new HashSet<>();
        for (int i = indexList.indexOfSmallerOrEqualThan(key); i >= 0; i--) {
            set.add(indexList.get(i));
        }
        return set;
    }

    public Set<K> keySetGreaterOrEqualThan(K key) {
        Set<K> set = new HashSet<>();
        for (int i = indexList.indexOfGreaterOrEqualThan(key); i < indexList.size(); i++) {
            set.add(indexList.get(i));
        }
        return set;
    }

    public Set<K> keySetGreaterThan(K key) {
        Set<K> set = new HashSet<>();
        for (int i = indexList.indexOfGreaterThan(key); i < indexList.size(); i++) {
            set.add(indexList.get(i));
        }
        return set;
    }

    public Collection<V> values() {
        return map.values();
    }

    public Collection<V> valuesSmallerThan(K key) {
        Collection<V> values = new ArrayList<>();
        for (int i = indexList.indexOfSmallerThan(key); i >= 0; i--) {
            values.add(get(indexList.get(i)));
        }
        return values;
    }

    public Collection<V> valuesSmallerOrEqualThan(K key) {
        Collection<V> values = new ArrayList<>();
        for (int i = indexList.indexOfSmallerOrEqualThan(key); i >= 0; i--) {
            values.add(get(indexList.get(i)));
        }
        return values;
    }

    public Collection<V> valuesGreaterOrEqualThan(K key) {
        Collection<V> values = new ArrayList<>();
        for (int i = indexList.indexOfGreaterOrEqualThan(key); i < indexList.size(); i++) {
            values.add(get(indexList.get(i)));
        }
        return values;
    }

    public Collection<V> valuesGreaterThan(K key) {
        Collection<V> values = new ArrayList<>();
        for (int i = indexList.indexOfGreaterThan(key); i < indexList.size(); i++) {
            values.add(get(indexList.get(i)));
        }
        return values;
    }
}
