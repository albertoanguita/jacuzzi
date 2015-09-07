package jacz.util.io.xml.test;

import jacz.util.io.xml.Element;
import jacz.util.io.xml.XMLDom;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class TestReadXML {

    public static void main(String[] args) throws IOException, XMLStreamException {
        File file = new File("a.xml");

        Element element = XMLDom.parse(file);
        XMLDom.write("b.xml", element);
        System.out.println("END");
    }
}
