package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.XMLWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alberto on 20/09/2015.
 */
public class TestXML {

    private int x;

    private String s;

    private Integer i;

    private List<String> l;

    private Map<String, String> m;

    public TestXML(int x, String s, Integer i, List<String> l, Map<String, String> m) {
        this.x = x;
        this.s = s;
        this.i = i;
        this.l = l;
        this.m = m;
    }

    public static void main(String[] args) throws Exception {
        List<String> l = new ArrayList<>();
        l.add("a");
        l.add("b");
        l.add("c");
        Map<String, String> map1 = new HashMap<>();
        map1.put("a", "aaa");
        map1.put("b", "bbb");
        map1.put("c", "ccc");
        TestXML testXML1 = new TestXML(5, "hello", null, l, map1);

        XMLWriter xmlWriter = new XMLWriter("root");
        xmlWriter.addField("x", Integer.toString(testXML1.x));
        xmlWriter.addField("s", testXML1.s);
        xmlWriter.addField("i", testXML1.i);
        xmlWriter.beginStruct("l");
        xmlWriter.addValue("a");
        xmlWriter.addValue("b");
        xmlWriter.addValue("c");
        xmlWriter.endStruct();

        xmlWriter.write("Test.xml");
    }
}
