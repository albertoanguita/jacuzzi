package jacz.util.io.object_serialization;

import jacz.util.io.xml.Element;
import jacz.util.io.xml.XMLDom;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Created by Alberto on 20/09/2015.
 */
public class XMLWriter {

    static final String FIELD = "field";

    static final String NAME = "name";

    static final String LIST = "list";

    static final String VALUE = "value";

    static final String MAP = "map";

    final Element root;

    private Element current;

    public XMLWriter(String root) {
        this.root = new Element(root);
        current = this.root;
    }

    public void addField(String name, Object value) {
        Element element = new Element(FIELD, current);
        element.putAttribute(NAME, name);
        if (value == null) {
            value = "@@@null@@@";
        }
        element.setText(value.toString());
    }

    public void beginList(String name) {
        Element element = new Element(LIST, current);
        if (name != null) {
            element.putAttribute(NAME, name);
        }
        current = element;
    }

    public void beginList() {
        beginList(null);
    }

    public void addValue(String value) {
        Element element = new Element(VALUE, current);
        element.setText(value);
    }

    public void endList() {
        current = current.getParent();
    }

    public void beginMap(String name) {
        Element element = new Element(MAP, current);
        if (name != null) {
            element.putAttribute(NAME, name);
        }
        current = element;
    }

    public void beginMap() {
        beginMap(null);
    }

    public void endMap() {
        endList();
    }

    public void write(String path) throws IOException, XMLStreamException {
        XMLDom.write(path, root);
    }
}
