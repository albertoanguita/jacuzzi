package org.aanguita.jacuzzi.hash.test;

import org.aanguita.jacuzzi.hash.CRC;
import org.aanguita.jacuzzi.io.serialization.MutableOffset;
import org.aanguita.jacuzzi.io.serialization.Serializer;

import java.util.Arrays;

/**
 * Created by Alberto on 26/09/2015.
 */
public class TestCRC {

    public static void main(String[] args) throws Exception {

        byte[] data = Serializer.serialize("hello");

        byte[] dataWithCRC = CRC.addCRC(data, 5, true);

        MutableOffset offset = new MutableOffset();
        byte[] data2 = CRC.extractDataWithCRC(dataWithCRC, offset);
        System.out.println(Arrays.equals(data, data2));

        String hello = Serializer.deserializeString(data2, new MutableOffset());
        System.out.println(hello);
    }
}
