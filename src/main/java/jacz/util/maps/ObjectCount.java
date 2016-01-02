package jacz.util.maps;

import java.util.*;

/**
 * An object count controller. Maintains the count of different objects (objects are identified by its equal()).
 * <p/>
 * Objects can be added and removed.
 * <p/>
 * Methods are not thread-safe
 */
public class ObjectCount<E> {

    private final Map<E, Integer> objectCount;

    private final boolean allowNegative;

    private final boolean allowNewObjects;

    private int totalCount;

    public ObjectCount() {
        this(new HashSet<E>());
    }

    public ObjectCount(Set<E> objects) {
        this(objects, false, true);
    }

    public ObjectCount(Set<E> objects, boolean allowNegative, boolean allowNewObjects) {
        this.objectCount = new HashMap<>(objects.size());
        for (E object : objects) {
            objectCount.put(object, 0);
        }
        this.allowNegative = allowNegative;
        this.allowNewObjects = allowNewObjects;
    }

    private void addObjectToMap(E object) {
        objectCount.put(object, 0);
    }

    public Set<E> objectSet() {
        return new HashSet<>(objectCount.keySet());
    }

    public final boolean containsObject(E object) {
        return objectCount.containsKey(object);
    }

    @SafeVarargs
    public final int getObjectCount(E... object) {
        return getObjectCount(Arrays.asList(object));
    }

    public int getObjectCount(Collection<E> objects) {
        int count = 0;
        for (E object : objects) {
            count += objectCount.get(object);
        }
        return count;
    }

    public void addObject(E object) {
        if (!allowNewObjects && !objectCount.containsKey(object)) {
            throw new RuntimeException("Non existing object: " + object.toString());
        } else if (!objectCount.containsKey(object)) {
            objectCount.put(object, 0);
        }
        objectCount.put(object, objectCount.get(object) + 1);
        totalCount++;
        checkEmptyObject(object);
    }

    public void subtractObject(E object) {
        if (!objectCount.containsKey(object)) {
            if (!allowNewObjects) {
                throw new RuntimeException("Non existing object: " + object.toString());
            } else {
                addObjectToMap(object);
            }
        }
        if (!allowNegative && objectCount.get(object) == 0) {
            throw new RuntimeException("Object count is zero: " + object.toString());
        }
        objectCount.put(object, objectCount.get(object) - 1);
        totalCount--;
        checkEmptyObject(object);
    }

    private void checkEmptyObject(E object) {
        if (objectCount.get(object) == 0) {
            objectCount.remove(object);
        }
    }

    public int getTotalCount() {
        return totalCount;
    }
}
