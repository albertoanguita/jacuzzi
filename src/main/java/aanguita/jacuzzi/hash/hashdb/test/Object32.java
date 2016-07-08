package aanguita.jacuzzi.hash.hashdb.test;

import aanguita.jacuzzi.hash.HashCode32;
import aanguita.jacuzzi.hash.HashObject32;

import java.io.Serializable;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 24-feb-2010<br>
 * Last Modified: 24-feb-2010
 */
class Object32 implements HashObject32, Serializable {

    private Integer i;

    Object32(Integer i) {
        this.i = i;
    }

    public Integer getI() {
        return i;
    }

    public HashCode32 hash() {
        return new HashCode32(i);
    }
}
