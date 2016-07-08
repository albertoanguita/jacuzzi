package org.aanguita.jacuzzi.hash.hashdb;

import org.aanguita.jacuzzi.hash.HashCode64;
import org.aanguita.jacuzzi.hash.HashObject64;

import java.io.*;

/**
 *
 */
public final class HashDatabase64 extends HashDatabase<HashCode64, HashObject64> {

    public static HashDatabase64 load(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
        HashDatabase64 database = (HashDatabase64) objStream.readObject();
        objStream.close();
        return database;
    }

    public void write(String path) throws IOException {
        ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
        objStream.writeObject(this);
        objStream.close();
    }
}
