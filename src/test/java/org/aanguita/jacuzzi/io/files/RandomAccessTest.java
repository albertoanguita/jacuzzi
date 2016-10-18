package org.aanguita.jacuzzi.io.files;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Alberto on 19/10/2016.
 */
public class RandomAccessTest {

    @Test
    public void test() throws IOException {

        // a 12-length byte array for the tests
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        // a 12-length byte array for the tests
        byte[] zeroes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Path test = Paths.get("test.dat");

        if (Files.exists(test)) {
            Files.delete(test);
        }

        RandomAccess.write2(test, 0, zeroes);
        RandomAccess.write2("test.dat", 0, Arrays.copyOfRange(data, 0, 3));
        RandomAccess.write2("test.dat", 3, ByteBuffer.wrap(Arrays.copyOfRange(data, 3, 4)));
        RandomAccess.write2(test, 4, Arrays.copyOfRange(data, 4, 6));
        RandomAccess.write2(test, 6, ByteBuffer.wrap(Arrays.copyOfRange(data, 6, 12)));

        assertArrayEquals(data, Files.readAllBytes(test));

        assertArrayEquals(data, RandomAccess.read2("test.dat"));
        assertArrayEquals(data, RandomAccess.read2(test));
        assertArrayEquals(Arrays.copyOfRange(data, 5, 12), RandomAccess.read2("test.dat", 5));
        assertArrayEquals(Arrays.copyOfRange(data, 5, 12), RandomAccess.read2(test, 5));

        assertArrayEquals(Arrays.copyOfRange(data, 0, 2), RandomAccess.read2("test.dat", 0, 2));
        assertArrayEquals(Arrays.copyOfRange(data, 2, 7), RandomAccess.read2(test, 2, 5));
        assertArrayEquals(Arrays.copyOfRange(data, 7, 8), RandomAccess.read2("test.dat", 7, 1));
        assertArrayEquals(Arrays.copyOfRange(data, 8, 12), RandomAccess.read2(test, 8, 4));

        byte[] appendedArray = {21, 22, 23, 24};
        RandomAccess.append2("test.dat", Arrays.copyOfRange(appendedArray, 0, 1));
        RandomAccess.append2(test, Arrays.copyOfRange(appendedArray, 1, 2));
        RandomAccess.append2("test.dat", ByteBuffer.wrap(Arrays.copyOfRange(appendedArray, 2, 3)));
        RandomAccess.append2(test, ByteBuffer.wrap(Arrays.copyOfRange(appendedArray, 3, 4)));

        byte[] totalArray = new byte[16];
        System.arraycopy(data, 0, totalArray, 0, 12);
        System.arraycopy(appendedArray, 0, totalArray, 12, 4);
        assertArrayEquals(totalArray, Files.readAllBytes(test));

        Files.delete(test);
    }
}