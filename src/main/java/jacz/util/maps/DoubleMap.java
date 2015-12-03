package jacz.util.maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class implements a two-direction map. It thus implements a functional and inverse functional relation of
 * elements (bijective function)
 */
public class DoubleMap<T, S> implements Serializable {

    private Map<T, S> directMap;

    private Map<S, T> reverseMap;

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

    public DoubleMap(DoubleMap<T, S> doubleMap) {
        directMap = new HashMap<>(doubleMap.directMap);
        reverseMap = new HashMap<>(doubleMap.reverseMap);
    }

    public void put(T t, S s) {
        if (directMap.containsKey(t)) {
            remove(t);
        }
        if (reverseMap.containsKey(s)) {
            removeReverse(s);
        }
        directMap.put(t, s);
        reverseMap.put(s, t);
    }

    public S get(T t) {
        return directMap.get(t);
    }

    public T getReverse(S s) {
        return reverseMap.get(s);
    }

    public boolean contains(T t) {
        return directMap.containsKey(t);
    }

    public boolean containsReverse(S s) {
        return reverseMap.containsKey(s);
    }

    public S remove(T t) {
        S s = directMap.remove(t);
        if (s != null) {
            reverseMap.remove(s);
        }
        return s;
    }

    public T removeReverse(S s) {
        T t = reverseMap.remove(s);
        if (t != null) {
            directMap.remove(t);
        }
        return t;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return directMap.size();
    }

    public Set<T> keySet() {
        return directMap.keySet();
    }

    public Collection<S> values() {
        return directMap.values();
    }

    public Set<Map.Entry<T, S>> entrySet() {
        return directMap.entrySet();
    }
}
