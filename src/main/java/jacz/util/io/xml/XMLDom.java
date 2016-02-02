package jacz.util.io.xml;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import jacz.util.hash.CRCMismatchException;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

import javax.xml.stream.*;
import java.io.*;
import java.net.URL;
import java.util.Arrays;

/**
 * A simple XML-Dom API. It does not recognize the initial XML code with encoding information or XML version, nor PCData elements
 */
public class XMLDom {

    private static final String CRC_ELEMENT = "crc-element";

    public static Element parse(String path, String... backupPaths) throws FileNotFoundException, XMLStreamException {
        try {
            return parse(new File(path));
        } catch (Exception e) {
            if (backupPaths.length > 0) {
                String newPath = backupPaths[0];
                backupPaths = Arrays.copyOfRange(backupPaths, 1, backupPaths.length);
                return parse(newPath, backupPaths);
            } else {
                throw e;
            }
        }
    }

    public static Element parseWithCRC(String path, String... backupPaths) throws FileNotFoundException, XMLStreamException, CRCMismatchException {
        try {
            return parseWithCRC(new File(path));
        } catch (Exception e) {
            if (backupPaths.length > 0) {
                String newPath = backupPaths[0];
                backupPaths = Arrays.copyOfRange(backupPaths, 1, backupPaths.length);
                return parseWithCRC(newPath, backupPaths);
            } else {
                throw e;
            }
        }
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

    public static Element parseWithCRC(File file) throws FileNotFoundException, XMLStreamException, CRCMismatchException {
        return parseWithCRC(new FileReader(file));
    }

    public static Element parse(URL url) throws IOException, XMLStreamException {
        return parse(url.openStream());
    }

    public static Element parseWithCRC(URL url) throws IOException, XMLStreamException, CRCMismatchException {
        return parseWithCRC(url.openStream());
    }

    public static Element parse(InputStreamReader inputStreamReader) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        return parse(xif.createXMLStreamReader(inputStreamReader));
    }

    public static Element parseWithCRC(InputStreamReader inputStreamReader) throws FileNotFoundException, XMLStreamException, CRCMismatchException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        Element element = parse(xif.createXMLStreamReader(inputStreamReader));
        checkCRC(element);
        element.removeChildren(CRC_ELEMENT);
        return element;
    }

    public static Element parse(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        return parse(xif.createXMLStreamReader(inputStream));
    }

    public static Element parseWithCRC(InputStream inputStream) throws XMLStreamException, CRCMismatchException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        Element element = parse(xif.createXMLStreamReader(inputStream));
        checkCRC(element);
        element.removeChildren(CRC_ELEMENT);
        return element;
    }

    private static Element parse(XMLStreamReader xsr) throws XMLStreamException {
        // we skip the events until we reach the root element
        while (xsr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            xsr.next();
        }
        return parseElement(xsr);
    }

    private static void checkCRC(Element element) throws CRCMismatchException {
        Element CRCElement = element.getChild(CRC_ELEMENT);
        if (CRCElement != null) {
            String crc = CRCElement.getText();
            // the length in bytes is the number of characters divided by 2
            int crcLength = crc.length() / 2;
            if (!crc.equals(element.getHash(crcLength, CRC_ELEMENT))) {
                throw new CRCMismatchException();
            }
        }
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
//                xsr.next();
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

    public static void write(String path, Element element, String... backupPaths) throws XMLStreamException, IOException {
        write(path, element, 0, backupPaths);
    }

    public static void write(String path, Element element, int hashLength, String... backupPaths) throws XMLStreamException, IOException {
        write(new File(path), element, hashLength);
        for (String backupPath : backupPaths) {
            write(new File(backupPath), element, hashLength);
        }
    }

    public static void write(File file, Element element) throws XMLStreamException, IOException {
        write(file, element, 0);
    }

    public static void write(File file, Element element, int hashLength) throws XMLStreamException, IOException {
        write(new FileWriter(file), element, hashLength);
    }

    public static void write(Writer stream, Element element) throws XMLStreamException {
        write(stream, element, 0);
    }

    public static void write(Writer stream, Element element, int hashLength) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw = xof.createXMLStreamWriter(stream);
        writeXMLStreamWriter(xtw, element, hashLength);
    }

    public static void write(OutputStream stream, Element element) throws XMLStreamException {
        write(stream, element, 0);
    }

//    public static void write(OutputStream stream, Element element, int hashLength) throws XMLStreamException {
//        try {
//            Serializer serializer = new Processor(new Configuration()).newSerializer();
//            serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
//            serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
//            serializer.setOutputStream(stream);
//            XMLStreamWriter xtw = serializer.getXMLStreamWriter();
//            writeXMLStreamWriter(xtw, element, hashLength);
//        } catch (SaxonApiException e) {
//            throw new XMLStreamException(e.getMessage());
//        }
//    }

    public static void write(OutputStream stream, Element element, int hashLength) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(stream));
        writeXMLStreamWriter(xtw, element, hashLength);
    }

//    public static void write(OutputStream stream, Element element, int hashLength) throws XMLStreamException {
//        XMLOutputFactory xof = XMLOutputFactory.newInstance();
//        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(stream));
//        writeXMLStreamWriter(xtw, element, hashLength);
//    }
//
    private static void writeXMLStreamWriter(XMLStreamWriter xtw, Element element, int hashLength) throws XMLStreamException {
        if (hashLength > 0) {
            String hash = element.getHash(hashLength);
            Element hashElement = new Element(CRC_ELEMENT);
            hashElement.setText(hash);
            element.addChild(hashElement);
        }
        writeElement(xtw, element);
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
        if (hashLength > 0) {
            element.removeChildren(CRC_ELEMENT);
        }
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
