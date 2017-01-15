package org.aanguita.jacuzzi.hash;

import java.security.NoSuchAlgorithmException;

/**
 * SHA-1 hash function
 */
public class SHA_1 extends HashFunction {

    public SHA_1() {
        this(null);
    }

    public SHA_1(Integer hashLength) {
        super(hashLength);
        try {
            initialize("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            e.printStackTrace();
            System.exit(1);
        }
    }
}
