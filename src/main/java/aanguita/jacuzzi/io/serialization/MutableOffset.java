package aanguita.jacuzzi.io.serialization;

/**
 * Offset used in the serialization of objects
 */
public class MutableOffset {

    private int offset;

    public MutableOffset() {
        offset = 0;
    }

    public int value() {
        return offset;
    }

    public void add(int value) {
        offset += value;
    }
}
