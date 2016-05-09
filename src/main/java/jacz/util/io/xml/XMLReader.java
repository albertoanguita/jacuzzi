package jacz.util.io.xml;

import jacz.util.hash.CRCMismatchException;
import jacz.util.lists.tuple.Duple;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Alberto on 20/09/2015.
 */
public class XMLReader {

    private Element current;

    private final List<String> repairedFiles;

    private final Stack<List<Element>> stack;

    public XMLReader(String path, String... backupPaths) throws IOException, XMLStreamException {
        this(path, false, backupPaths);
    }

    public XMLReader(String path, boolean repairIfBroken, String... backupPaths) throws IOException, XMLStreamException {
        if (repairIfBroken) {
            Duple<Element, List<String>> elementAndRepairedFiles = XMLDom.parseAndRepairBroken(path, backupPaths);
            current = elementAndRepairedFiles.element1;
            repairedFiles = elementAndRepairedFiles.element2;
        } else {
            current = XMLDom.parse(path, backupPaths);
            repairedFiles = new ArrayList<>();
        }
        stack = new Stack<>();
    }

    public XMLReader(String path, boolean repairIfBroken, boolean withCRC, String... backupPaths) throws IOException, XMLStreamException, CRCMismatchException {
        if (withCRC) {
            if (repairIfBroken) {
                Duple<Element, List<String>> elementAndRepairedFiles = XMLDom.parseWithCRCAndRepairBroken(path, backupPaths);
                current = elementAndRepairedFiles.element1;
                repairedFiles = elementAndRepairedFiles.element2;
            } else {
                current = XMLDom.parseWithCRC(path, backupPaths);
                repairedFiles = new ArrayList<>();
            }
        } else {
            if (repairIfBroken) {
                Duple<Element, List<String>> elementAndRepairedFiles = XMLDom.parseAndRepairBroken(path, backupPaths);
                current = elementAndRepairedFiles.element1;
                repairedFiles = elementAndRepairedFiles.element2;
            } else {
                current = XMLDom.parse(path, backupPaths);
                repairedFiles = new ArrayList<>();
            }
        }
        stack = new Stack<>();
    }

    public List<String> getRepairedFiles() {
        return repairedFiles;
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
