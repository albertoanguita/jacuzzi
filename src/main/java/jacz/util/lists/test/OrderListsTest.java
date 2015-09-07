package jacz.util.lists.test;

import jacz.util.lists.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OrderListsTest {

    public static void main(String args[]) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(5);
        list.add(2);
        list.add(7);
        list.add(14);
        list.add(1);
        list.add(-3);
        list.add(4);
        list.add(8);
        list.add(6);
        list.add(9);

        List<Integer> relList1 = new ArrayList<Integer>();
        relList1.add(5);
        relList1.add(2);
        relList1.add(7);
        relList1.add(14);
        relList1.add(1);
        relList1.add(-3);
        relList1.add(4);
        relList1.add(8);
        relList1.add(6);
        relList1.add(9);

        List<Double> relList2 = new ArrayList<Double>();
        relList2.add(5.0);
        relList2.add(2.0);
        relList2.add(7.0);
        relList2.add(14.0);
        relList2.add(1.0);
        relList2.add(-3.0);
        relList2.add(4.0);
        relList2.add(8.0);
        relList2.add(6.0);
        relList2.add(9.0);

        System.out.println("Lista original: " + list);

        // order the integer list with 2 related lists. Use one thread only. Specify a Comparator for the ints
        //Lists.sort(list, new ComparaInts(), true, 1, true, relList1, relList2);

        // order the integer list with 2 related lists. Use one thread only. Use natural ordering of the ints
        //Lists.sort(list, false, 1, true, relList1, relList2);

        // order the integer list with 2 related lists. No multithreading. Use natural ordering of the ints
        //Lists.sort(list, false, relList1, relList2);

        // order the integer list with 2 related lists. Use 16 threads always. Use natural ordering of the ints
        Lists.sort(list, false, 4, relList1, relList2);

        // order the integer list with 2 related lists. Use 16 threads, or the available cores in the machine if this
        // is lower. Use natural ordering of the ints
        //Lists.sort(list, false, 16, false, relList1, relList2);

        // order the integer list with 2 related lists. Use as many cores as available. Use natural ordering of the ints
        //Lists.sort(list, false, -1, false, relList1, relList2);

        System.out.println("Lista ordenada: " + list);
        System.out.println("List2 ordenada: " + relList1);
        System.out.println("List3 ordenada: " + relList2);

    }

}
