package aanguita.jacuzzi.hash.hashdb;

import aanguita.jacuzzi.hash.HashCode32;
import aanguita.jacuzzi.hash.HashObject32;

import java.io.*;

/**
 *
 */
public final class HashDatabase32 extends HashDatabase<HashCode32, HashObject32> {

    public static HashDatabase32 load(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
        HashDatabase32 database = (HashDatabase32) objStream.readObject();
        objStream.close();
        return database;
    }

    public void write(String path) throws IOException {
        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
        objStream.writeObject(this);
        objStream.close();
    }
}
