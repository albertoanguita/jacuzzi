package jacz.util.maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A generic map that allows defining the actions that take place in the get and put methods
 */
public class AutoKeyMap<K, V, E extends Throwable> implements Serializable {

    public static interface KeyGenerator<K, V, E extends Throwable> {

        public K generateKey(V value) throws E;
    }

    private final Map<K, V> map;

    private final KeyGenerator<K, V, E> keyGenerator;

    public AutoKeyMap(KeyGenerator<K, V, E> keyGenerator) {
        map = new HashMap<>();
        this.keyGenerator = keyGenerator;
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
        return containsKey(keyGenerator.generateKey(value));
    }

    public K put(V value) throws E {
        K key = keyGenerator.generateKey(value);
        map.put(key, value);
        return key;
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
}
