package org.aanguita.jacuzzi.io.files;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;


/**
 * Utility methods for random read, write and append
 * todo add javadocs
 */
public class RandomAccess {

    public static byte[] read(String file) throws IndexOutOfBoundsException, IOException {
        return read(Paths.get(file));
    }

    public static byte[] read(Path file) throws IndexOutOfBoundsException, IOException {
        return read(file, 0L);
    }

    public static byte[] read(String file, long offset) throws IndexOutOfBoundsException, IOException {
        return read(Paths.get(file), offset);
    }

    public static byte[] read(Path file, long offset) throws IndexOutOfBoundsException, IOException {
        return read(file, offset, (int) (Files.size(file) - offset));
    }

    public static byte[] read(String file, long offset, int length) throws IndexOutOfBoundsException, IOException {
        return read(Paths.get(file), offset, length);
    }

    public static byte[] read(Path file, long offset, int length) throws IndexOutOfBoundsException, IOException {
        ByteBuffer bytes = ByteBuffer.allocate(length);
        try (FileChannel fc = (FileChannel.open(file, READ))) {
            fc.position(offset);
            int numRead;
            do {
                numRead = fc.read(bytes);
            } while (numRead != -1 && bytes.hasRemaining());
        }
        return bytes.array();
    }

    public static void write(String file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write(Paths.get(file), offset, data);
    }

    public static void write(Path file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write(file, offset, ByteBuffer.wrap(data));
    }

    public static void write(String file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write(Paths.get(file), offset, src);
    }

    public static void write(Path file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write(file, offset, src, WRITE, CREATE);
    }

    public static void write(Path file, long offset, ByteBuffer src, OpenOption... options) throws IndexOutOfBoundsException, IOException {
        try (FileChannel fc = (FileChannel.open(file, options))) {
            fc.position(offset);
            while (src.hasRemaining()) {
                fc.write(src);
            }
        }
    }

    public static void append(String file, byte[] data) throws IndexOutOfBoundsException, IOException {
        append(Paths.get(file), data);
    }

    public static void append(Path file, byte[] data) throws IndexOutOfBoundsException, IOException {
        append(file, ByteBuffer.wrap(data));
    }

    public static void append(String file, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        append(Paths.get(file), src);
    }

    public static void append(Path file, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write(file, 0, src, WRITE, APPEND);
    }
}
