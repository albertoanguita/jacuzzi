package org.aanguita.jacuzzi.io.serialization;

/**
 * Offset used in the serialization of objects
 */
public class Offset {

    private int offset;

    public Offset() {
        offset = 0;
    }

    public int value() {
        return offset;
    }

    public void add(int value) {
        offset += value;
    }
}
