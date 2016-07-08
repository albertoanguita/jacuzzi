package aanguita.jacuzzi.hash.hashdb.test;

import aanguita.jacuzzi.hash.hashdb.HashDatabase128;
import aanguita.jacuzzi.hash.hashdb.HashDatabase32;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 24-feb-2010<br>
 * Last Modified: 24-feb-2010
 */
public class Test {

    public static void main(String args[]) {

        try {
            ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream("ni.txt"));
            objStream.writeObject(new NiClase("joder"));
            objStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        HashDatabase32 db = new HashDatabase32();


        try {
            db = HashDatabase32.load("bd_32.bd");
        } catch (Exception e) {
            e.printStackTrace();
        }


        Object32 o1 = new Object32(15);
        Object32 o2 = new Object32(6);

        db.put(o1);
        db.put(o2);
        Object32 o3 = (Object32) db.get(o1.hash());

        boolean b = db.containsValue(new Object32(16));


        HashDatabase128 db128 = new HashDatabase128();
        Object128 o128 = new Object128("joder");
        Object128 o1282 = new Object128("joder2");
        db128.put(o128);
        db128.put(o1282);

        try {
            db.write("bd_32.bd");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("FIN");
    }
}
