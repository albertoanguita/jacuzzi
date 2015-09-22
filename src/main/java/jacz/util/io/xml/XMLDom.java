package jacz.util.io.xml;

import javax.xml.stream.*;
import java.io.*;
import java.net.URL;

/**
 * A simple XML-Dom API. It does not recognize the initial XML code with encoding information or XML version, nor PCData elements
 */
public class XMLDom {

    public static Element parse(String path) throws FileNotFoundException, XMLStreamException {
        return parse(new File(path));
    }

    /**
     * Parses a file into the internal DOM model
     *
     * @param file file to parse
     * @return the root Element of the parsed model
     * @throws FileNotFoundException the file is not found
     * @throws XMLStreamException    problems while reading the XML document
     */
    public static Element parse(File file) throws FileNotFoundException, XMLStreamException {
        return parse(new FileReader(file));
    }

    public static Element parse(URL url) throws IOException, XMLStreamException {
        return parse(url.openStream());
    }

    public static Element parse(InputStreamReader inputStreamReader) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        return parse(xif.createXMLStreamReader(inputStreamReader));
    }

    public static Element parse(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        return parse(xif.createXMLStreamReader(inputStream));
    }

    private static Element parse(XMLStreamReader xsr) throws XMLStreamException {
        // we skip the events until we reach the root element
        while (xsr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            xsr.next();
        }
        return parseElement(xsr);
    }

    private static Element parseElement(XMLStreamReader xsr) throws XMLStreamException {
        // xsr points to a START_ELEMENT event. Create the element and read all its attributes
        // Then read all its children events
        Element element = new Element(xsr.getLocalName());
        // text that will be added to the element. Text can come in different events, so we add it here and add it to the element at the end
        StringBuilder elementText = new StringBuilder();
        int attributeCount = xsr.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            element.putAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
        }
        while (xsr.hasNext()) {
            xsr.next();
            if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT) {
                // element is closed. Move the cursor and return it
                // check if there is some text to add before (empty text is not added, but added text is not trimmed)
                // we set empty text also if the element has no children
                if (!elementText.toString().trim().isEmpty() || !element.hasChildren()) {
                    element.setText(elementText.toString());
                }
                xsr.next();
                return element;
            } else if (xsr.getEventType() == XMLStreamConstants.CHARACTERS) {
                // an attribute of the current element
                elementText.append(xsr.getText());
            } else if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                // new element begins -> read it recursively and add it to the current element
                element.addChild(parseElement(xsr));
            }
        }
        // we reached the end of the document without the tag end -> error parsing
        throw new XMLStreamException("End of the document unexpectedly reached. Element " + element.getName() + " not closed");
    }

    public static void write(String path, Element element) throws XMLStreamException, IOException {
        write(new File(path), element);
    }

    public static void write(File file, Element element) throws XMLStreamException, IOException {
        write(new FileWriter(file), element);
    }

    public static void write(Writer stream, Element element) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = xof.createXMLStreamWriter(stream);
        writeElement(xtw, element);
        xtw.flush();
        xtw.close();
    }

    public static void write(OutputStream stream, Element element) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = xof.createXMLStreamWriter(stream);
        writeElement(xtw, element);
        xtw.flush();
        xtw.close();
    }

    private static void writeElement(XMLStreamWriter xtw, Element element) throws XMLStreamException {
        xtw.writeStartElement(element.getName());
        for (String attributeName : element.getAttributeNames()) {
            xtw.writeAttribute(attributeName, element.getAttributeValue(attributeName));
        }
        if (element.hasText()) {
            xtw.writeCharacters(element.getText());
        } else {
            for (Element child : element.getChildren()) {
                writeElement(xtw, child);
            }
        }
        xtw.writeEndElement();
    }
}
