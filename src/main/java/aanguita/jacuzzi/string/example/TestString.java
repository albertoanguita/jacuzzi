package aanguita.jacuzzi.string.example;

import aanguita.jacuzzi.string.StringOps;

import java.util.Map;
import java.util.HashMap;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 20-nov-2008<br>
 * Last Modified: 20-nov-2008
 */
public class TestString {

    public static void main(String args[]) throws Exception {

        Map<String, String> m = new HashMap<>();
//        m.put("G:\\pelis", "http://localhost/pelis1");
        m.put("hola", "adios");
        m.put("a", "aa");
//        String s = StringOps.transformStringWithMappings("G:\\pelis\\06\\2001 Despega Como Puedas - Spanish Divx - Masiero -.avi", m);
        String s = StringOps.transformStringWithMappings("a, hola", m);

        System.out.println(s);


        System.out.println(StringOps.separateTokens("aaa,", ",", false, "", -1));
    }
}
