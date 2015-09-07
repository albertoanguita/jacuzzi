package jacz.util.io.xml;

import java.util.*;

/**
 * An XML element of the XML-Dom API
 */
public class Element {

    private final String name;

    private Map<String, String> attributes;

    private List<Element> children;

    private String text;

    public Element(String name) {
        this.name = name;
        attributes = new HashMap<>();
        children = new ArrayList<Element>();
        text = null;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    public String getAttributeValue(String name) {
        return attributes.get(name);
    }

    public void putAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public boolean hasText() {
        return text != null;
    }

    public List<Element> getChildren() {
        return new ArrayList<Element>(children);
    }

    public List<Element> getChildren(String name) {
        List<Element> nameChildren = new ArrayList<Element>();
        for (Element child : children) {
            if (child.getName().equals(name)) {
                nameChildren.add(child);
            }
        }
        return nameChildren;
    }

    public Element getChild(String name) {
        List<Element> children = getChildren(name);
        return (!children.isEmpty()) ? children.get(0) : null;
    }

    public void setChildren(List<Element> children) throws IllegalStateException {
        if (children != null && !children.isEmpty()) {
            checkCanAddChildren();
        }
        this.children = children;
        if (this.children == null) {
            this.children = new ArrayList<Element>();
        }
    }

    public void addChild(Element child) throws IllegalStateException {
        checkCanAddChildren();
        children.add(child);
    }

    public void clearChildren() {
        children.clear();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) throws IllegalStateException {
        checkCanAddText();
        this.text = text;
    }

    public void clearText() {
        text = null;
    }

    private void checkCanAddChildren() throws IllegalStateException {
        if (hasText()) {
            throw new IllegalStateException("Cannot add children to element with text");
        }
    }

    private void checkCanAddText() throws IllegalStateException {
        if (hasChildren()) {
            throw new IllegalStateException("Cannot add text to element with children");
        }
    }
}
