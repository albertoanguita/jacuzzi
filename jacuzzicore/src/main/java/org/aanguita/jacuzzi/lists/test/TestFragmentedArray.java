package org.aanguita.jacuzzi.lists.test;

import org.aanguita.jacuzzi.lists.FragmentedArray;

import java.util.Arrays;

/**
 *
 */
public class TestFragmentedArray {

    public static void main(String args[]) {

        Integer[] initialArray = new Integer[1];
        initialArray[0] = 5;
        FragmentedArray<Integer> fr = new FragmentedArray<>(initialArray);
        fr.addArray();
        fr.addArrayLeft(-1, 0);
        fr.addArrayLeft(1, 2);
        fr.addArray(3, 4, 5);
        fr.addArray(6, 7, 8);
        fr.addArray(9, 10, 11);

//        Integer[] ar = fr.getArray(0, 9);
        Integer[] ar = fr.getArray();
//        Integer[] ar = fr.getArray(0, 9);

        System.out.println(Arrays.toString(ar));
        System.out.println("FIN");
    }
}
