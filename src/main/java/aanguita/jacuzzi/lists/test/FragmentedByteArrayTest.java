package aanguita.jacuzzi.lists.test;

import aanguita.jacuzzi.io.serialization.FragmentedByteArray;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 19/06/14
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
public class FragmentedByteArrayTest {

    public static void main(String[] args) {

        byte[] a1 = new byte[3];
        a1[0] = 0;
        a1[1] = 1;
        a1[2] = 2;
        byte[] a2 = new byte[2];
        a2[0] = 3;
        a2[1] = 4;
        byte[] addArray = FragmentedByteArray.addFinal(a1, a2);

        System.out.println(Arrays.toString(addArray));
    }
}
