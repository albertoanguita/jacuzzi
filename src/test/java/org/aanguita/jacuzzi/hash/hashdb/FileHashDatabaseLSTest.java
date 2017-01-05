package org.aanguita.jacuzzi.hash.hashdb;

import org.aanguita.jacuzzi.io.files.FileReaderWriter;
import org.aanguita.jacuzzi.io.files.FileUtil;
import org.aanguita.jacuzzi.io.serialization.VersionedSerializationException;
import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * todo restore
 */
public class FileHashDatabaseLSTest {

    private static final String dir = "./etc/test-files/";

    /*@Test
    public void test() throws IOException, VersionedSerializationException {

        // create test files
        Files.createDirectories(Paths.get(dir));
        FileUtil.cleanDirectory(dir);
        List<Duple<String, String>> pathAndHash = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FileReaderWriter.writeTextFile(path(i), "file " + i);
            pathAndHash.add(new Duple<>(path(i), FileHashDatabase.getHash(new File(path(i)))));
        }

        FileHashDatabaseLS fhd = new FileHashDatabaseLS(dir + "fhd.bd", true);
        Assert.assertEquals(new ArrayList<>(), fhd.getRepairedFiles());
        for (int i = 0; i < 5; i++) {
            fhd.put(path((i)));
        }

        Assert.assertEquals(5, fhd.size());
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(fhd.containsKey(pathAndHash.get(i).element2));
            Assert.assertTrue(fhd.containsPath(pathAndHash.get(i).element1));
            Assert.assertEquals(new File(pathAndHash.get(i).element1).getAbsolutePath(), new File(fhd.getFilePath(pathAndHash.get(i).element2)).getAbsolutePath());
            Assert.assertEquals(new File(pathAndHash.get(i).element1).getAbsolutePath(), fhd.getFile(pathAndHash.get(i).element2).getAbsolutePath());
        }

        fhd.remove(pathAndHash.get(0).element2);
        fhd.removeValue(pathAndHash.get(1).element1);
        Assert.assertEquals(3, fhd.size());
        Assert.assertFalse(fhd.containsKey(pathAndHash.get(0).element2));
        Assert.assertFalse(fhd.containsPath(pathAndHash.get(0).element1));
        Assert.assertEquals(null, fhd.getFilePath(pathAndHash.get(0).element2));
        Assert.assertFalse(fhd.containsKey(pathAndHash.get(1).element2));
        Assert.assertFalse(fhd.containsPath(pathAndHash.get(1).element1));
        Assert.assertEquals(null, fhd.getFilePath(pathAndHash.get(1).element2));
        for (int i = 2; i < 5; i++) {
            Assert.assertTrue(fhd.containsKey(pathAndHash.get(i).element2));
            Assert.assertTrue(fhd.containsPath(pathAndHash.get(i).element1));
            Assert.assertEquals(new File(pathAndHash.get(i).element1).getAbsolutePath(), new File(fhd.getFilePath(pathAndHash.get(i).element2)).getAbsolutePath());
        }


        fhd = new FileHashDatabaseLS(dir + "fhd.bd", true);
        Assert.assertEquals(new ArrayList<>(), fhd.getRepairedFiles());
        Assert.assertEquals(3, fhd.size());
        Assert.assertFalse(fhd.containsKey(pathAndHash.get(0).element2));
        Assert.assertFalse(fhd.containsPath(pathAndHash.get(0).element1));
        Assert.assertEquals(null, fhd.getFilePath(pathAndHash.get(0).element2));
        Assert.assertFalse(fhd.containsKey(pathAndHash.get(1).element2));
        Assert.assertFalse(fhd.containsPath(pathAndHash.get(1).element1));
        Assert.assertEquals(null, fhd.getFilePath(pathAndHash.get(1).element2));
        for (int i = 2; i < 5; i++) {
            Assert.assertTrue(fhd.containsKey(pathAndHash.get(i).element2));
            Assert.assertTrue(fhd.containsPath(pathAndHash.get(i).element1));
            Assert.assertEquals(new File(pathAndHash.get(i).element1).getAbsolutePath(), new File(fhd.getFilePath(pathAndHash.get(i).element2)).getAbsolutePath());
        }

        FileUtil.cleanDirectory(dir);
    }

    private String path(int index) {
        return dir + index;
    }*/
}