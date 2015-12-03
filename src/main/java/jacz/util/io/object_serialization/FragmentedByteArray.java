package jacz.util.io.object_serialization;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FragmentedByteArray {

    private final List<byte[]> arrayList;

    private int length;

    public FragmentedByteArray(byte[]... arrays) {
        arrayList = new ArrayList<>();
        length = 0;
        addArrays(arrays);
    }

    public FragmentedByteArray addArrays(byte[]... arrays) {
        for (byte[] array : arrays) {
            addArray(array);
        }
        return this;
    }

    public FragmentedByteArray addArray(byte[] array) {
        arrayList.add(array);
        length += array.length;
        return this;
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

    public static byte[] addArraysFinal(byte[]... arrays) {
        FragmentedByteArray fragmentedByteArray = new FragmentedByteArray();
        fragmentedByteArray.addArrays(arrays);
        return fragmentedByteArray.generateArray();
    }
}
