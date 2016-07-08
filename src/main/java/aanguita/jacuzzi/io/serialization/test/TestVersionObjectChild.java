package aanguita.jacuzzi.io.serialization.test;

import aanguita.jacuzzi.io.serialization.UnrecognizedVersionException;
import aanguita.jacuzzi.io.serialization.VersionStack;
import aanguita.jacuzzi.io.serialization.VersionedObjectSerializer;
import aanguita.jacuzzi.io.serialization.VersionedSerializationException;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 16/12/2015.
 */
public class TestVersionObjectChild extends TestVersionedObjectParent {

    private String son;

    public TestVersionObjectChild(String son, int i) {
        super(i);
        this.son = son;
    }

    @Override
    public VersionStack getCurrentVersion() {
        return new VersionStack("2", super.getCurrentVersion());
    }

    @Override
    public Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<>(super.serialize());
        map.put("son", son);
        return map;
    }

    @Override
    public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
        if (version.equals("2")) {
            son = (String) attributes.get("son");
            super.deserialize(parentVersions.retrieveVersion(), attributes, parentVersions);
        } else {
            throw new UnrecognizedVersionException();
        }
    }


    public static void main(String[] args) throws VersionedSerializationException, NotSerializableException {
        TestVersionObjectChild testVersionObjectChild = new TestVersionObjectChild("sonnn", 3);

        byte[] data = VersionedObjectSerializer.serialize(testVersionObjectChild);

        TestVersionObjectChild testVersionObjectChild2 = new TestVersionObjectChild("son2", 5);
        VersionedObjectSerializer.deserialize(testVersionObjectChild2, data);

        System.out.println("END");

    }
}
