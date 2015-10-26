package jacz.util.maps;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple object count that stores a single object
 */
public class SimpleObjectCount {

    private static final Boolean STORED_OBJECT = true;

    private final ObjectCount<Boolean> objectCount;

    public SimpleObjectCount() {
        this(false, true);
    }

    public SimpleObjectCount(boolean allowNegative, boolean allowNewObjects) {
        Set<Boolean> elements = new HashSet<>();
        elements.add(STORED_OBJECT);
        objectCount = new ObjectCount<>(elements, allowNegative, allowNewObjects);
    }

    public void addObject() {
        objectCount.addObject(STORED_OBJECT);
    }

    public void subtractObject() {
        objectCount.subtractObject(STORED_OBJECT);
    }

    public int getCount() {
        return objectCount.getObjectCount(STORED_OBJECT);
    }
}
