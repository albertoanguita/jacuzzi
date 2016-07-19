package org.aanguita.jacuzzi.maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a two-direction map. It thus implements a functional and inverse functional relation of
 * elements (bijective function)
 */
public class DoubleMap<K, V> implements Serializable {

    private Map<K, V> directMap;

    private Map<V, K> reverseMap;

    public DoubleMap() {
        directMap = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    public DoubleMap(int initialCapacity) {
        directMap = new HashMap<>(initialCapacity);
        reverseMap = new HashMap<>(initialCapacity);
    }

    public DoubleMap(int initialCapacity, float loadFactor) {
        directMap = new HashMap<>(initialCapacity, loadFactor);
        reverseMap = new HashMap<>(initialCapacity, loadFactor);
    }

    public DoubleMap(DoubleMap<K, V> doubleMap) {
        directMap = new HashMap<>(doubleMap.directMap);
        reverseMap = new HashMap<>(doubleMap.reverseMap);
    }

    public void put(K k, V v) {
        if (directMap.containsKey(k)) {
            remove(k);
        }
        if (reverseMap.containsKey(v)) {
            removeReverse(v);
        }
        directMap.put(k, v);
        reverseMap.put(v, k);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        m.entrySet().stream().forEach(entry -> put(entry.getKey(), entry.getValue()));
    }

    public V get(K k) {
        return directMap.get(k);
    }

    public K getReverse(V v) {
        return reverseMap.get(v);
    }

    public boolean containsKey(K k) {
        return directMap.containsKey(k);
    }

    public boolean containsValue(V v) {
        return reverseMap.containsKey(v);
    }

    public V remove(K k) {
        V v = directMap.remove(k);
        if (v != null) {
            reverseMap.remove(v);
        }
        return v;
    }

    public K removeReverse(V v) {
        K k = reverseMap.remove(v);
        if (k != null) {
            directMap.remove(k);
        }
        return k;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        directMap.clear();
        reverseMap.clear();
    }

    public int size() {
        return directMap.size();
    }

    public Set<K> keySet() {
        return directMap.keySet();
    }

    public Collection<K> keys() {
        return reverseMap.values();
    }

    public Set<V> valueSet() {
        return reverseMap.keySet();
    }

    public Collection<V> values() {
        return directMap.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return directMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleMap)) return false;

        DoubleMap<?, ?> doubleMap = (DoubleMap<?, ?>) o;

        return directMap.equals(doubleMap.directMap) && reverseMap.equals(doubleMap.reverseMap);
    }

    @Override
    public int hashCode() {
        int result = directMap.hashCode();
        result = 31 * result + reverseMap.hashCode();
        return result;
    }
}
