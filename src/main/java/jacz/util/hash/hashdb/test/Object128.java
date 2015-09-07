package jacz.util.hash.hashdb.test;

import jacz.util.hash.HashCode128;
import jacz.util.hash.HashObject128;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 24-feb-2010<br>
 * Last Modified: 24-feb-2010
 */
class Object128 implements HashObject128 {

    private String str;

    public Object128(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public HashCode128 hash() {
        System.out.println(Object128.class.toString() + str);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest((Object128.class.toString() + str).getBytes());
            System.out.println(new HexBinaryAdapter().marshal(hash));
            return new HashCode128(new HexBinaryAdapter().marshal(hash));
        } catch (NoSuchAlgorithmException e) {
            // cannot happen
            return null;
        }
    }
}
