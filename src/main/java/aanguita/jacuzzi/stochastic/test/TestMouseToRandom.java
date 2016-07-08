package aanguita.jacuzzi.stochastic.test;

import aanguita.jacuzzi.stochastic.MouseToRandom;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28/04/12<br>
 * Last Modified: 28/04/12
 */
public class TestMouseToRandom {

    public static void main(String args[]) {

        /*for (int i = 0; i < 20; i++) {
            System.out.println(System.nanoTime());
        }
        System.out.println(".");
        for (int i = 0; i < 20; i++) {
            System.out.println(System.currentTimeMillis());
        }*/

        MouseToRandom mouseToRandom2 = new MouseToRandom(10);
        mouseToRandom2.mouseCoords(1, 1);
        mouseToRandom2.mouseCoords(2, 1);
        mouseToRandom2.mouseCoords(3, 1);
        mouseToRandom2.mouseCoords(-500000, -1000000);
        mouseToRandom2.mouseCoords(5, 1);
        mouseToRandom2.mouseCoords(-500000, -1000000);
        mouseToRandom2.mouseCoords(5, 1);
        mouseToRandom2.mouseCoords(-500000, -1000000);
        mouseToRandom2.mouseCoords(5, 1);
        mouseToRandom2.mouseCoords(-500000, -1000000);
        mouseToRandom2.mouseCoords(1, 1);


        MouseToRandom mouseToRandom = new MouseToRandom(12, false);
        System.out.println(mouseToRandom.mouseCoords(1, 2));
        System.out.println(mouseToRandom.mouseCoords(1, 2));
        System.out.println(mouseToRandom.mouseCoords(3, -2));
        System.out.println(mouseToRandom.mouseCoords(1, 0));
        System.out.println(mouseToRandom.mouseCoords(-1, -2));
        System.out.println(mouseToRandom.mouseCoords(-2, 2));
        System.out.println(mouseToRandom.mouseCoords(1, 2));
        System.out.println(mouseToRandom.mouseCoords(0, 2));
        System.out.println(mouseToRandom.mouseCoords(3, -1));
        System.out.println(mouseToRandom.mouseCoords(1, 0));
        System.out.println(mouseToRandom.mouseCoords(-1, -2));
        System.out.println(mouseToRandom.mouseCoords(-2, 2));
        System.out.println(mouseToRandom.mouseCoords(1, 2));
        System.out.println(mouseToRandom.mouseCoords(0, 2));
        System.out.println(mouseToRandom.mouseCoords(3, -1));
        System.out.println(mouseToRandom.mouseCoords(0, 2));
        System.out.println(mouseToRandom.mouseCoords(3, -1));
        System.out.println(mouseToRandom.mouseCoords(1, 0));
        System.out.println(mouseToRandom.mouseCoords(-1, -2));
        System.out.println(mouseToRandom.mouseCoords(-2, 2));
        System.out.println(mouseToRandom.mouseCoords(-5, 4));
        System.out.println(mouseToRandom.mouseCoords(-25, 14));
        System.out.println(mouseToRandom.mouseCoords(-15, 34));
        System.out.println(mouseToRandom.mouseCoords(0, 1));
        System.out.println(mouseToRandom.mouseCoords(10, 11));
        System.out.println(mouseToRandom.mouseCoords(105, 151));
        System.out.println(Arrays.toString(mouseToRandom.getRandomBytes()));


        byte b = -1;
        byte b2 = 5 & 0xFF;
        System.out.println(b & 0xFF);
        System.out.println(b2);


        System.out.println(Integer.toHexString(10));


        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024];
            int numRead;
            InputStream fis = new FileInputStream("test.txt");


            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            fis.close();

            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            System.out.println("Hex format : " + sb.toString());

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }

            System.out.println("Hex format : " + hexString.toString());

            // e5c1edb50ff8b4fcc3ead3a845ffbe1ad51c9dae5d44335a5c333b57ac8df062
            // e5c1edb50ff8b4fcc3ead3a845ffbe1ad51c9dae5d44335a5c333b57ac8df062
            // e5c1edb5ff8b4fcc3ead3a845ffbe1ad51c9dae5d44335a5c333b57ac8df062
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
