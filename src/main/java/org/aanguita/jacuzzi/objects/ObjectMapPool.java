package org.aanguita.jacuzzi.objects;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * A pool of objects referenced by a generic index. Object access is thread safe
 */
public class ObjectMapPool<K, V> {

    private final ConcurrentMap<K, V> objectPool;

    private final Supplier<V> objectCreator;

    public ObjectMapPool(Supplier<V> objectCreator) {
        this.objectCreator = objectCreator;
        objectPool = new ConcurrentHashMap<>();
    }

    public V getObject(K key) {
        V myObject = objectPool.get(key);
        if (myObject == null) {
            synchronized (this) {
                myObject = objectPool.get(key);
                if (myObject == null) {
                    objectPool.put(key, objectCreator.get());
                    myObject = objectPool.get(key);
                }
            }
        }
        return myObject;
    }

    public boolean containsKey(K key) {
        return objectPool.containsKey(key);
    }

    public V removeObject(K key) {
        return objectPool.remove(key);
    }
}
