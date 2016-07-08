package aanguita.jacuzzi.io.serialization;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Versioned object tests
 */
public class TestVersionedObject {

    private static class SerClass implements Serializable {

        public int i;

        public String s;

        private SerClass(int i, String s) {
            this.i = i;
            this.s = s;
        }
    }

    public class VersionedObjectImpl implements VersionedObject {

        private int i;

        private String s;

        private TestSerializer.TestEnum t;

        private boolean b;

        private long l;

        private float f;

        private SerClass serClass;

        private byte[] data;

        public VersionedObjectImpl() {
            i = 5;
            s = "fuck";
            t = TestSerializer.TestEnum.A;
            b = true;
            l = 27;
            f = 0.5f;
            serClass = new SerClass(3, "hello");
            data = new byte[3];
            data[0] = 5;
            data[1] = 27;
            data[2] = -12;
        }

        public VersionedObjectImpl(boolean b) {
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
        public VersionStack getCurrentVersion() {
            return new VersionStack("1.0");
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
        public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
            if (version.equals("1.0")) {
                i = (int) attributes.get("i");
                s = (String) attributes.get("s");
                t = (TestSerializer.TestEnum) attributes.get("t");
                b = (boolean) attributes.get("b");
                l = (long) attributes.get("l");
                f = (float) attributes.get("f");
                serClass = (SerClass) attributes.get("serClass");
                data = (byte[]) attributes.get("data");
            } else {
                throw new UnrecognizedVersionException();
            }
        }
    }

    @Test
    public void testBasic() throws VersionedSerializationException, IOException {

        VersionedObjectImpl testVersionedObject = new VersionedObjectImpl();

        byte[] data = VersionedObjectSerializer.serialize(testVersionedObject);

        VersionedObjectImpl testVersionedObject1 = new VersionedObjectImpl(false);
        VersionedObjectSerializer.deserialize(testVersionedObject1, data);

        Assert.assertEquals(testVersionedObject1.i, testVersionedObject.i);
        Assert.assertEquals(testVersionedObject1.s, testVersionedObject.s);
        Assert.assertEquals(testVersionedObject1.t, testVersionedObject.t);
        Assert.assertEquals(testVersionedObject1.b, testVersionedObject.b);
        Assert.assertEquals(testVersionedObject1.l, testVersionedObject.l);
        Assert.assertEquals(testVersionedObject1.f, testVersionedObject.f, 0);
        Assert.assertEquals(testVersionedObject1.serClass.i, testVersionedObject.serClass.i);
        Assert.assertEquals(testVersionedObject1.serClass.s, testVersionedObject.serClass.s);
        Assert.assertArrayEquals(testVersionedObject1.data, testVersionedObject.data);

        System.out.println("END");
    }
}
