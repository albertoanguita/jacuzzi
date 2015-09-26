package jacz.util.hash;

import java.security.NoSuchAlgorithmException;

/**
 * SHA-384 hash function
 */
public class SHA_384 extends HashFunction {

    public SHA_384() {
        this(null);
    }

    public SHA_384(Integer hashLength) {
        super(hashLength);
        try {
            initialize("SHA-384");
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            e.printStackTrace();
            System.exit(1);
        }
    }
}
