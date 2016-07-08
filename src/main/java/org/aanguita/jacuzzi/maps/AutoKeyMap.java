package org.aanguita.jacuzzi.maps;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A generic map that allows adding values without key. A KeyGenerator is provided with the code to calculate
 * the key for values
 */
public class AutoKeyMap<K, V, E extends Throwable> implements Serializable {

    public interface KeyGenerator<K, V, E extends Throwable> {

        K generateKey(V value) throws E;
    }

    private final Map<K, V> map;

    private final KeyGenerator<K, V, E> keyGenerator;

    public AutoKeyMap(KeyGenerator<K, V, E> keyGenerator) {
        map = new HashMap<>();
        this.keyGenerator = keyGenerator;
    }

    public KeyGenerator<K, V, E> getKeyGenerator() {
        return keyGenerator;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    public V get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) throws E {
        return map.containsValue(value);
    }

    public Duple<Boolean, K> containsSimilarValue(V value) throws E {
        K key = keyGenerator.generateKey(value);
        return new Duple<>(containsKey(key), key);
    }

    public K put(V value) throws E {
        K key = keyGenerator.generateKey(value);
        map.put(key, value);
        return key;
    }

    public V put(K key, V value) throws E {
        return map.put(key, value);
    }

    public V remove(K key) {
        return map.remove(key);
    }

    public K removeValue(V value) throws E {
        K key = keyGenerator.generateKey(value);
        if (containsKey(key)) {
            remove(key);
            return key;
        } else {
            return null;
        }
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
