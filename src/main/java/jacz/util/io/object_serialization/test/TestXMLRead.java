package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.XMLReader;

/**
 * Created by Alberto on 20/09/2015.
 */
public class TestXMLRead {

    public static void main(String[] args) throws Exception {

        XMLReader xmlReader = XMLReader.parse("Test2.xml");

        String x = xmlReader.getField("x");
        String s = xmlReader.getField("s");
        String i = xmlReader.getField("i");
        xmlReader.getList("l");
        while (xmlReader.hasMoreChildren()) {
            xmlReader.getList();
            System.out.println("new sub list");
            while (xmlReader.hasMoreChildren()) {
                System.out.println(xmlReader.getValue());
            }
        }

        xmlReader.getMap("m");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getMapEntry());
        }

        xmlReader.getMap("m2");
        xmlReader.getList("l");
        System.out.println("sublist l");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getValue());
        }
        xmlReader.getList("l2");
        System.out.println("sublist l2");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getValue());
        }
        xmlReader.getList("l3");
        System.out.println("sublist l3");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getValue());
        }

        xmlReader.gotoParent();


        xmlReader.getMap("m3");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getMapMap());
            while (xmlReader.hasMoreChildren()) {
                System.out.println(xmlReader.getMapEntry());
            }
        }


        System.out.println("END");
    }
}
