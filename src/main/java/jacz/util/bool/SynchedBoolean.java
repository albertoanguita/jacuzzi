package jacz.util.bool;

/**
 * todo remove, use AtomicBoolean instead
 */
public class SynchedBoolean extends MutableBoolean {

    public SynchedBoolean(Boolean value) {
        super(value);
    }

    public synchronized Boolean isValue() {
        return super.isValue();
    }

    public synchronized void setValue(Boolean value) {
        super.setValue(value);
    }
}
