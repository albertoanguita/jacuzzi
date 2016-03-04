package jacz.util.bool;

/**
 * todo remove, use AtomicBoolean???
 */
public class MutableBoolean {

    private Boolean value;

    public MutableBoolean(Boolean value) {
        this.value = value;
    }

    public Boolean isValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
