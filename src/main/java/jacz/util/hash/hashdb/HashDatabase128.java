package jacz.util.hash.hashdb;

import jacz.util.hash.HashCode128;
import jacz.util.hash.HashObject128;

import java.io.*;

/**
 *
 */
public final class HashDatabase128 extends HashDatabase<HashCode128, HashObject128> {

    public static HashDatabase128 load(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
        HashDatabase128 database = (HashDatabase128) objStream.readObject();
        objStream.close();
        return database;
    }

    public void write(String path) throws IOException {
        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
        objStream.writeObject(this);
        objStream.close();
    }
}
