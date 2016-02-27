package jacz.util.bool;

/**
 * Created by Alberto on 27/02/2016.
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
