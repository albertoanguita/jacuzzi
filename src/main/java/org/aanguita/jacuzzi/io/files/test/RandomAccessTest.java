package org.aanguita.jacuzzi.io.files.test;

import org.aanguita.jacuzzi.io.files.RandomAccess;

import java.io.File;
import java.io.IOException;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 25-may-2010<br>
 * Last Modified: 25-may-2010
 */
public class RandomAccessTest {

    public static void main(String args[]) {

        String path = ".\\trunk\\src\\jacuzzi\\util\\files\\test\\randomaccessfile.txt";

        System.out.println(File.separator);
        System.out.println(File.pathSeparator);

        try {
            byte[] data = RandomAccess.read(path, 0, 2);
            for (byte b : data) {
                System.out.print((char) b);
            }
            System.out.println();

            RandomAccess.write(path, 12, data);

            RandomAccess.append(path, data);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("FIN");
    }
}
