package org.aanguita.jacuzzi.maps.test;

import org.aanguita.jacuzzi.maps.ThesauriStringMap;
import org.aanguita.jacuzzi.string.StringBuilderPool;

import java.util.*;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 2/08/11<br>
 * Last Modified: 2/08/11
 */
public class ThesauriMapTest {

    public static void main(String args[]) {

        test1();
    }


    private static void test1() {

        ThesauriStringMap<Integer> m = new ThesauriStringMap<>(false, 1000);

        m.put("", 0);
        m.put("aaa", 1);
        m.put("aaaii", 2);
        m.put("aaaoo", 3);
        Integer in = m.put("aabbb", 4);
        //Integer in = m.remove("aaa");
        in = m.put("acd", 10);
        //m.put("abc", 3);
        //m.put("bbb", 4);
        //m.put("a", 5);

        Iterator<String> i = m.iterator();
        while (i.hasNext()) {
            String s = i.next();
            System.out.println(s + " -> " + m.get(s));
        }

        /*Collection<Integer> s = m.values();
        for (Integer inter : s) {
            System.out.println(inter);
        }
        Collection<Integer> col = new HashSet<Integer>();
        col.add(1);
        col.add(2);*/

        System.out.println("FIN");
    }

    private static void test2() {


        StringBuilder str1 = new StringBuilder("1234567890");
        StringBuilder str2 = new StringBuilder();
        StringBuilder str3 = new StringBuilder();
        StringBuilderPool.divide(str1, str2, str3, 5);


        char from = 'a';
        char to = 'z';

        //HashMap<String, Integer> m = new HashMap<String, Integer>();
        ThesauriStringMap<Integer> m = new ThesauriStringMap<>(false, 250000);

        StringBuilder str = new StringBuilder("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        for (int i = 0; i < 5000000; i++) {
            if (i % 10000 == 0) {
                System.out.println(i);
            }
            m.put(str.toString(), i);
            nextStr(str, from, to);
        }
        System.out.println("wait...");

        System.gc();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("FIN");
    }

    private static char nextChar(char c) {
        return (char) (c + 1);
    }

    private static StringBuilder nextStr(StringBuilder str, char from, char to) {
        boolean finished = false;
        int index = str.length() - 1;
        while (!finished) {
            str.setCharAt(index, nextChar(str.charAt(index)));
            if (str.charAt(index) > to) {
                str.setCharAt(index, from);
                index--;
            } else {
                finished = true;
            }
        }
        return str;
    }


}
