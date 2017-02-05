package org.aanguita.jacuzzi.io.test;

import org.aanguita.jacuzzi.io.SixBitSerializer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 29/04/12<br>
 * Last Modified: 29/04/12
 */
public class TestSixBitSerializer {

    public static void main(String args[]) {

        char c = SixBitSerializer.FIRST_CHAR;
        for (int i = 0; i < 70; i++) {
            System.out.println(c);
            c = SixBitSerializer.getNextChar(c);
        }

        Random random = new Random(1);

        byte[] randomBytes = new byte[100];

        byte[] id;
        byte[] id2 = new byte[4];


        random.nextBytes(randomBytes);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(randomBytes);
        id = messageDigest.digest();

        System.arraycopy(id, 0, id2, 0, id2.length);

        String s = SixBitSerializer.serialize(id2);


        byte[] data = new byte[1];
        data[0] = 15;
        //data[1] = 14;
        //data[2] = 60;
        //data[3] = 15;


        System.out.println(SixBitSerializer.serialize(data));

        //System.out.println(Arrays.toString(SixBitSerializer.deserialize("0gUYf", 1)));
        System.out.println(Arrays.toString(SixBitSerializer.deserialize("33", 1)));

        SixBitSerializer.deserialize("m8DQmSf-yB4si2EbyrPJ_PozPLIyH0ra6KL_O8j3999", 32);

        SixBitSerializer.deserialize("m8DQmSf-yB4si2EbyrPJ_PozPLIyH0ra6KL_O8j399f", 32);

        // this must throw an illegal argument exception
        SixBitSerializer.deserialize("m8DQmSf-yB4si2EbyrPJ_PozPLIyH0ra6KL_O8j399g", 32);

    }
}
