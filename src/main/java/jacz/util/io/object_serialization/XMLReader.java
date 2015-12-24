package jacz.util.io.object_serialization;

import jacz.util.hash.CRCMismatchException;
import jacz.util.io.xml.Element;
import jacz.util.io.xml.XMLDom;
import jacz.util.lists.tuple.Duple;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Alberto on 20/09/2015.
 */
public class XMLReader {

    private Element current;

    private final Stack<List<Element>> stack;

    public XMLReader(String path, String... backupPaths) throws FileNotFoundException, XMLStreamException {
        current = XMLDom.parse(path, backupPaths);
        stack = new Stack<>();
    }

    public XMLReader(String path, boolean withCRC, String... backupPaths) throws FileNotFoundException, XMLStreamException, CRCMismatchException {
        if (withCRC) {
            current = XMLDom.parseWithCRC(path, backupPaths);
        } else {
            current = XMLDom.parse(path, backupPaths);
        }
        stack = new Stack<>();
    }

    public String getFieldValue(String name) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(XMLWriter.NAME, name);
        Element element = current.getChild(XMLWriter.FIELD, attributes);
        return (element != null) ? checkNull(element.getText()) : null;
    }

    private String checkNull(String value) {
        return value.equals(XMLWriter.NULL) ? null : value;
    }

    public void getStruct(String name) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(XMLWriter.NAME, name);
        current = current.getChild(XMLWriter.STRUCT, attributes);
        stack.push(current.getChildren());
    }

    public boolean hasMoreChildren() {
        if (stack.peek().isEmpty()) {
            gotoParent();
            return false;
        } else {
            return true;
        }
    }

    public void gotoParent() {
        stack.pop();
        current = current.getParent();
    }

    public void getNextStruct() {
        current = stack.peek().remove(0);
        stack.push(current.getChildren());
    }

    public String getNextValue() {
        return checkNull(stack.peek().remove(0).getText());
    }

    public Duple<String, String> getNextFieldAndValue() {
        Element element = stack.peek().remove(0);
        return new Duple<>(element.getAttributeValue(XMLWriter.NAME), checkNull(element.getText()));
    }

    public String getNextStructAndName() {
        current = stack.peek().remove(0);
        stack.push(current.getChildren());
        return current.getAttributeValue(XMLWriter.NAME);
    }
}
