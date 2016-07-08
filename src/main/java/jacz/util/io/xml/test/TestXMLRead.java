package jacz.util.io.xml.test;

import jacz.util.io.xml.XMLReader;

/**
 * Created by Alberto on 20/09/2015.
 */
public class TestXMLRead {

    public static void main(String[] args) throws Exception {

        XMLReader xmlReader = new XMLReader("./etc/tests/Test3.xml");

        String x = xmlReader.getFieldValue("x");
        String s = xmlReader.getFieldValue("s");
        String i = xmlReader.getFieldValue("i");
        xmlReader.getStruct("l");
        while (xmlReader.hasMoreChildren()) {
            xmlReader.getNextStruct();
            System.out.println("new sub list");
            while (xmlReader.hasMoreChildren()) {
                System.out.println(xmlReader.getNextValue());
            }
        }

        xmlReader.getStruct("m");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getNextFieldAndValue());
        }

        xmlReader.getStruct("m2");
        xmlReader.getStruct("l");
        System.out.println("sublist l");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getNextValue());
        }
        xmlReader.getStruct("l2");
        System.out.println("sublist l2");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getNextValue());
        }
        xmlReader.getStruct("l3");
        System.out.println("sublist l3");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getNextValue());
        }

        xmlReader.gotoParent();


        xmlReader.getStruct("m3");
        while (xmlReader.hasMoreChildren()) {
            System.out.println(xmlReader.getNextStructAndName());
            while (xmlReader.hasMoreChildren()) {
                System.out.println(xmlReader.getNextFieldAndValue());
            }
        }


        System.out.println("END");
    }
}
