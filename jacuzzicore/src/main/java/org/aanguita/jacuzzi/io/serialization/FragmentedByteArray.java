package org.aanguita.jacuzzi.io.serialization;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragmented byte array, specifically suited for primitive byte arrays
 */
public class FragmentedByteArray {

    private final List<byte[]> arrayList;

    private int length;

    public FragmentedByteArray(byte[]... arrays) {
        arrayList = new ArrayList<>();
        length = 0;
        add(arrays);
    }

    public FragmentedByteArray add(byte[]... arrays) {
        for (byte[] array : arrays) {
            addArray(array);
        }
        return this;
    }

    private void addArray(byte[] array) {
        arrayList.add(array);
        length += array.length;
    }

    public byte[] generateArray() {
        byte[] array = new byte[length];
        int pos = 0;
        for (byte[] arrayPart : arrayList) {
            System.arraycopy(arrayPart, 0, array, pos, arrayPart.length);
            pos += arrayPart.length;
        }
        return array;
    }

    public static byte[] addFinal(byte[]... arrays) {
        FragmentedByteArray fragmentedByteArray = new FragmentedByteArray();
        fragmentedByteArray.add(arrays);
        return fragmentedByteArray.generateArray();
    }
}
