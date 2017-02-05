package org.aanguita.jacuzzi.hash;

import java.security.NoSuchAlgorithmException;

/**
 * SHA-512 hash function
 */
public class SHA_512 extends HashFunction {

    public SHA_512() {
        this(null);
    }

    public SHA_512(Integer hashLength) {
        super(hashLength);
        try {
            initialize("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            e.printStackTrace();
            System.exit(1);
        }
    }
}
