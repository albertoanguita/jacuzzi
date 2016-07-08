package org.aanguita.jacuzzi.io.xml.test;

import org.aanguita.jacuzzi.io.xml.Element;
import org.aanguita.jacuzzi.io.xml.XMLDom;

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
