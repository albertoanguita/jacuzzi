package aanguita.jacuzzi.hash;

import java.security.NoSuchAlgorithmException;

/**
 * MD5 hash function
 */
public class MD5 extends HashFunction {

    public MD5() {
        this(null);
    }

    public MD5(Integer hashLength) {
        super(hashLength);
        try {
            initialize(("MD5"));
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            e.printStackTrace();
            System.exit(1);
        }
    }
}
