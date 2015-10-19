package jacz.util.io.object_serialization.test;

import jacz.util.io.object_serialization.UnrecognizedVersionException;
import jacz.util.io.object_serialization.VersionedObject;
import jacz.util.io.object_serialization.VersionedObjectSerializer;
import jacz.util.io.object_serialization.VersionedSerializationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TestVersionedObject implements VersionedObject {

    private static class SerClass implements Serializable {

        public int i;

        public String s;

        private SerClass(int i, String s) {
            this.i = i;
            this.s = s;
        }
    }

    public static void main(String[] args) throws VersionedSerializationException, IOException {

        TestVersionedObject testVersionedObject = new TestVersionedObject();

        byte[] data = VersionedObjectSerializer.serialize(testVersionedObject);

        TestVersionedObject testVersionedObject1 = new TestVersionedObject(false);
        VersionedObjectSerializer.deserialize(testVersionedObject1, data);

        System.out.println("END");
    }


    private int i;

    private String s;

    private Test.TestEnum t;

    private boolean b;

    private long l;

    private float f;

    private SerClass serClass;

    private byte[] data;

    public TestVersionedObject() {
        i = 5;
        s = "fuck";
        t = Test.TestEnum.A;
        b = true;
        l = 27;
        f = 0.5f;
        serClass = new SerClass(3, "hello");
        data = new byte[3];
        data[0] = 5;
        data[1] = 27;
        data[2] = -12;
    }

    public TestVersionedObject(boolean b) {
        i = 0;
        s = null;
        t = null;
        b = false;
        l = 0;
        f = 0;
        serClass = null;
        data = new byte[7];
    }

    @Override
    public String getCurrentVersion() {
        return "1.0";
    }

    @Override
    public Map<String, Serializable> serialize() {
        Map<String, Serializable> map = new HashMap<>();
        map.put("i", i);
        map.put("s", s);
        map.put("t", t);
        map.put("b", b);
        map.put("l", l);
        map.put("f", f);
        map.put("serClass", serClass);
        map.put("data", data);
        return map;
    }

    @Override
    public void deserialize(Map<String, Object> attributes) {
        i = (int) attributes.get("i");
        s = (String) attributes.get("s");
        t = (Test.TestEnum) attributes.get("t");
        b = (boolean) attributes.get("b");
        l = (long) attributes.get("l");
        f = (float) attributes.get("f");
        serClass = (SerClass) attributes.get("serClass");
        data = (byte[]) attributes.get("data");
    }

    @Override
    public void deserializeOldVersion(String version, Map<String, Object> attributes) throws UnrecognizedVersionException {
        throw new UnrecognizedVersionException();
    }
}
