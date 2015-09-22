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

    static final String STRUCT = "str";

    static final String NAME = "name";

    static final String VALUE = "value";

    static final String NULL = "@@@null@@@";

    final Element root;

    private Element current;

    public XMLWriter(String root) {
        this.root = new Element(root);
        current = this.root;
    }

    public void addField(String name, Object value) {
        Element element = new Element(FIELD, current);
        element.putAttribute(NAME, name);
        value = checkNull(value);
        element.setText(value.toString());
    }

    public void addValue(Object value) {
        Element element = new Element(VALUE, current);
        value = checkNull(value);
        element.setText(value.toString());
    }

    private Object checkNull(Object value) {
        return value == null ? NULL : value;
    }

    public void beginStruct(String name) {
        Element element = new Element(STRUCT, current);
        if (name != null) {
            element.putAttribute(NAME, name);
        }
        current = element;
    }

    public void beginStruct() {
        beginStruct(null);
    }

    public void endStruct() {
        current = current.getParent();
    }

    public void write(String path) throws IOException, XMLStreamException {
        XMLDom.write(path, root);
    }
}
