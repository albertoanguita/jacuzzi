package aanguita.jacuzzi.hash.hashdb;

import aanguita.jacuzzi.hash.HashCode;
import aanguita.jacuzzi.hash.HashObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A hash-index table that stores generic objects
 */
abstract class HashDatabase<T extends HashCode, Y extends HashObject> implements Serializable {

    protected Map<T, Y> data;

    public HashDatabase() {
        data = new HashMap<>();
    }

    public boolean containsKey(T key) {
        return data.containsKey(key);
    }

    public boolean containsValue(Y value) {
        @SuppressWarnings({"unchecked"})
        T key = (T) value.hash();
        return containsKey(key);
    }

    public Object get(T key) {
        return data.get(key);
    }

    public void put(Y o) {
        // to be checked by subclasses
        @SuppressWarnings({"unchecked"})
        T hash = (T) o.hash();
        if (!containsKey(hash)) {
            data.put(hash, o);
        }
    }

    /*public static <T> T load(Class cl, String path) throws ClassNotFoundException, IOException {
        ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(path));
        cl db = (cl) objstream.readObject();
        objstream.close();
        return db;
    }*/
}
