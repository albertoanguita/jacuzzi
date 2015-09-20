package jacz.util.io.object_serialization;

import jacz.util.io.xml.Element;
import jacz.util.io.xml.XMLDom;
import jacz.util.lists.Duple;

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

    private Stack<List<Element>> stack;

    public static XMLReader parse(String path) throws FileNotFoundException, XMLStreamException {
        return new XMLReader(XMLDom.parse(path));
    }

    private XMLReader(Element current) {
        this.current = current;
        stack = new Stack<>();
    }

    public String getField(String name) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(XMLWriter.NAME, name);
        Element element = current.getChild(XMLWriter.FIELD, attributes);
        return (element != null) ? element.getText() : null;
    }

    public void getList(String name) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(XMLWriter.NAME, name);
        current = current.getChild(XMLWriter.LIST, attributes);
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

    public String getValue() {
        return stack.peek().remove(0).getText();
    }

    public void getList() {
        current = stack.peek().remove(0);
        stack.push(current.getChildren());
    }

    public void getMap(String name) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(XMLWriter.NAME, name);
        current = current.getChild(XMLWriter.MAP, attributes);
        stack.push(current.getChildren());
    }

    public Duple<String, String> getMapEntry() {
        Element element = stack.peek().remove(0);
        return new Duple<>(element.getAttributeValue(XMLWriter.NAME), element.getText());
    }

    public String getMapList() {
        current = stack.peek().remove(0);
        stack.push(current.getChildren());
        return current.getAttributeValue(XMLWriter.NAME);
    }

    public String getMapMap() {
        return getMapList();
    }



//    public void getList(String name) {
//        current = null;
//        return null;
//    }

}
