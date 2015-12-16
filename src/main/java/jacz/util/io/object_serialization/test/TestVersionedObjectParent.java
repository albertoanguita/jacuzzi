package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 16/12/2015.
 */
public class TestVersionedObjectParent implements VersionedObject {

    private int i;

    public TestVersionedObjectParent(int i) {
        this.i = i;
    }

    @Override
    public VersionStack getCurrentVersion() {
        return new VersionStack("1.0");
    }

    @Override
    public Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<>();
        map.put("i", i);
        return map;
    }

    @Override
    public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
        if (version.equals("1.0")) {
            i = (int) attributes.get("i");
        } else {
            throw new UnrecognizedVersionException();
        }
    }
}
