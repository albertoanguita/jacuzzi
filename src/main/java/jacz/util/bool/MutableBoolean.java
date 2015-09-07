package jacz.util.bool;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-abr-2010<br>
 * Last Modified: 09-abr-2010
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
