package org.aanguita.jacuzzi.hash;

import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hash function
 */
public class SHA_256 extends HashFunction {

    public SHA_256() {
        this(null);
    }

    public SHA_256(Integer hashLength) {
        super(hashLength);
        try {
            initialize("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            e.printStackTrace();
            System.exit(1);
        }
    }
}
