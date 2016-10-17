package org.aanguita.jacuzzi.io.files;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;


/**
 * Utility methods for random read, write and append
 *
 * todo use nio2 as in https://docs.oracle.com/javase/tutorial/essential/io/rafs.html
 */
public class RandomAccess {

    public static byte[] read(String path, long offset, int length) throws IndexOutOfBoundsException, IOException {
        return read(new File(path), offset, length);
    }

    public static byte[] read(File file) throws IndexOutOfBoundsException, IOException {
        return read(file, 0L);
    }

    public static byte[] read(File file, long offset) throws IndexOutOfBoundsException, IOException {
        return read(file, offset, (int) (file.length() - offset));
    }

    public static byte[] read(File file, long offset, int length) throws IndexOutOfBoundsException, IOException {
        RandomAccessFile rFile = null;
        FileChannel fc = null;
        try {
            rFile = new RandomAccessFile(file, "r");
            fc = rFile.getChannel();
            if (offset < 0 || length < 0 || offset + length > rFile.length()) {
                throw new IndexOutOfBoundsException("Wrong index for random data reading at file: " + file.getAbsolutePath() + " (offset: " + offset + ", length: " + length + ")");
            }
            fc.position(offset);
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[length]);
            while (byteBuffer.hasRemaining()) {
                fc.read(byteBuffer);
            }
            return byteBuffer.array();
        } finally {
            if (fc != null) {
                fc.close();
            }
            if (rFile != null) {
                rFile.close();
            }
        }
    }

    public static byte[] read2(String file, long offset, int length) throws IndexOutOfBoundsException, IOException {
        return read2(Paths.get(file), offset, length);
    }

    public static byte[] read2(Path file, long offset, int length) throws IndexOutOfBoundsException, IOException {
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

    public static void write2(String file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write2(Paths.get(file), offset, data);
    }

    public static void write2(Path file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write2(file, offset, ByteBuffer.wrap(data));
    }

    public static void write2(String file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write2(Paths.get(file), offset, src);
    }

    public static void write2(Path file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write2(file, offset, src, WRITE);
    }

    public static void write2(Path file, long offset, ByteBuffer src, OpenOption... options) throws IndexOutOfBoundsException, IOException {
        try (FileChannel fc = (FileChannel.open(file, options))) {
            fc.position(offset);
            while (src.hasRemaining()) {
                fc.write(src);
            }
        }
    }

    public static void append2(String file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        append2(Paths.get(file), offset, data);
    }

    public static void append2(Path file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        append2(file, offset, ByteBuffer.wrap(data));
    }

    public static void append2(String file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        append2(Paths.get(file), offset, src);
    }

    public static void append2(Path file, long offset, ByteBuffer src) throws IndexOutOfBoundsException, IOException {
        write2(file, offset, src, WRITE, APPEND);
    }

    public static void write(String path, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write(new File(path), offset, data, false);
    }

    public static void write(File file, long offset, byte[] data) throws IndexOutOfBoundsException, IOException {
        write(file, offset, data, false);
    }

    public static void write(String path, long offset, byte[] data, boolean forceWrite) throws IndexOutOfBoundsException, IOException {
        write(new File(path), offset, data, forceWrite);
    }

    /**
     * Write to a file
     *
     * @param file       file to write to
     * @param offset     offset from which start to write
     * @param data       data to write
     * @param forceSynch whether the new contents of the file must be synchronized to the underlying storage device (if possible)
     * @throws IndexOutOfBoundsException the given offset is not valid
     * @throws IOException               error writing to file
     */
    public static void write(File file, long offset, byte[] data, boolean forceSynch) throws IndexOutOfBoundsException, IOException {
        String mode;
        if (forceSynch) {
            mode = "rws";
        } else {
            mode = "rw";
        }
        RandomAccessFile rFile = null;
        FileChannel fc = null;
        try {
            rFile = new RandomAccessFile(file, mode);
            fc = rFile.getChannel();
            if (offset < 0 || offset > rFile.length()) {
                throw new IndexOutOfBoundsException("Wrong index for random data writing at file: " + file.getAbsolutePath() + " (offset: " + offset + ", file length: " + rFile.length() + ")");
            }
            fc.position(offset);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            while (byteBuffer.hasRemaining()) {
                fc.write(byteBuffer);
            }
        } finally {
            if (fc != null) {
                fc.close();
            }
            if (rFile != null) {
                rFile.close();
            }
        }
    }

    public static void append(String path, byte[] data) throws IOException {
        append(new File(path), data, true);
    }

    public static void append(File file, byte[] data) throws IOException {
        append(file, data, true);
    }

    public static void append(String path, byte[] data, boolean forceWrite) throws IOException {
        append(new File(path), data, forceWrite);
    }

    public static void append(File file, byte[] data, boolean forceWrite) throws IOException {
        try {
            write(file, file.length(), data, forceWrite);
        } catch (IndexOutOfBoundsException e) {
            // ignore, cannot happen
        }
    }
}
