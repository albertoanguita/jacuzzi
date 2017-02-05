package org.aanguita.jacuzzi.maps;

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

    public void add() {
        objectCount.addObject(STORED_OBJECT);
    }

    public void add(int count) {
        for (int i = 0; i < count; i++) {
            add();
        }
    }

    public void subtract() {
        objectCount.subtractObject(STORED_OBJECT);
    }

    public void subtract(int count) {
        for (int i = 0; i < count; i++) {
            subtract();
        }
    }

    public int getCount() {
        return objectCount.getObjectCount(STORED_OBJECT);
    }
}
