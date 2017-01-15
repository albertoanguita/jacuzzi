package org.aanguita.jacuzzi.objects;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Created by Alberto on 07/10/2016.
 */
public class ObjectMapPoolAdvancedCreator<K, T, V> {

    private final ConcurrentMap<K, V> objectPool;

    private final Function<Duple<K, T>, V> objectCreator;

    public ObjectMapPoolAdvancedCreator(Function<Duple<K, T>, V> objectCreator) {
        this.objectCreator = objectCreator;
        objectPool = new ConcurrentHashMap<>();
    }

    public V createObject(K key, T creationParameter) {
        V myObject = objectPool.get(key);
        if (myObject == null) {
            synchronized (this) {
                myObject = objectPool.get(key);
                if (myObject == null) {
                    objectPool.put(key, objectCreator.apply(new Duple<>(key, creationParameter)));
                    return objectPool.get(key);
                }
            }
        }
        throw new IllegalArgumentException("Creating an already existing object: " + key);
    }

    public V getObject(K key) {
        V myObject = objectPool.get(key);
        if (myObject != null) {
            return myObject;
        } else {
            throw new IllegalArgumentException("Requesting a non-existing object: " + key);
        }
    }

    public boolean containsKey(K key) {
        return objectPool.containsKey(key);
    }

    public V removeObject(K key) {
        return objectPool.remove(key);
    }
}
