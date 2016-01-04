package jacz.util.hash.hashdb.test;

import jacz.util.hash.hashdb.FileHashDatabase;
import jacz.util.io.serialization.VersionedObjectSerializer;

/**
 * Created by Alberto on 04/01/2016.
 */
public class TestSerializeFHD {

    public static void main(String[] args) throws Exception {

        FileHashDatabase fhd = new FileHashDatabase();

        fhd.put(".\\pom.xml");

        VersionedObjectSerializer.serialize(fhd, "fhd.vso");

        FileHashDatabase fhd2 = new FileHashDatabase("fhd.vso");

        System.out.println("FIN");

    }
}
