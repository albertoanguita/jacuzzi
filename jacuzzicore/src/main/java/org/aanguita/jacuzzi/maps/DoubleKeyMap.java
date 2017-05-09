package org.aanguita.jacuzzi.maps;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.lists.tuple.Triple;

import java.io.Serializable;
import java.util.*;

/**
 * A map implementation with two keys. Values can be retrieved with either key.
 */
public class DoubleKeyMap<K, S, V> implements Serializable {

    private final Map<K, Triple<K, S, V>> mainMap;

    private final Map<S, Triple<K, S, V>> secondaryMap;

    public DoubleKeyMap() {
        mainMap = new HashMap<>();
        secondaryMap = new HashMap<>();
    }

    public void put(K key, S secondaryKey, V value) {
        Triple<K, S, V> tripleValue = new Triple<>(key, secondaryKey, value);
        mainMap.put(key, tripleValue);
        secondaryMap.put(secondaryKey, tripleValue);
    }

    public V get(K key) {
        return mainMap.containsKey(key) ? mainMap.get(key).element3 : null;
    }

    public S getSecondaryKey(K key) {
        return mainMap.containsKey(key) ? mainMap.get(key).element2 : null;
    }

    public V getSecondary(S key) {
        return secondaryMap.containsKey(key) ? secondaryMap.get(key).element3 : null;
    }

    public K getMainKey(S key) {
        return secondaryMap.containsKey(key) ? secondaryMap.get(key).element1 : null;
    }

    public boolean containsKey(K key) {
        return mainMap.containsKey(key);
    }

    public boolean containsSecondaryKey(S secondaryKey) {
        return secondaryMap.containsKey(secondaryKey);
    }

    public Duple<S, V> remove(K key) {
        Triple<K, S, V> tripleValue = mainMap.remove(key);
        if (tripleValue != null) {
            secondaryMap.remove(tripleValue.element2);
            return new Duple<>(tripleValue.element2, tripleValue.element3);
        } else {
            return null;
        }
    }

    public Duple<K, V> removeSecondary(S secondaryKey) {
        Triple<K, S, V> tripleValue = secondaryMap.remove(secondaryKey);
        if (tripleValue != null) {
            mainMap.remove(tripleValue.element1);
            return new Duple<>(tripleValue.element1, tripleValue.element3);
        } else {
            return null;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        mainMap.clear();
        secondaryMap.clear();
    }

    public int size() {
        return mainMap.size();
    }

    public Set<K> keySet() {
        return mainMap.keySet();
    }

    public Set<S> secondaryKeySet() {
        return secondaryMap.keySet();
    }

    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        for (Triple<K, S, V> tripleValue : mainMap.values()) {
            values.add(tripleValue.element3);
        }
        return values;
    }

    public Collection<Triple<K, S, V>> entrySet() {
        return mainMap.values();
    }

}
