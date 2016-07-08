package org.aanguita.jacuzzi.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Utility methods for random read, write and append
 */
public class RandomAccess {

    public static byte[] read(String path, long offset, int length) throws IndexOutOfBoundsException, IOException {
        return read(new File(path), offset, length);
    }

    public static byte[] read(File file) throws IndexOutOfBoundsException, IOException {
        return read(file, 0l);
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
     * @param forceWrite whether the new contents of the file must be forced to the underlying storage device (if possible)
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public static void write(File file, long offset, byte[] data, boolean forceWrite) throws IndexOutOfBoundsException, IOException {
        String mode;
        if (forceWrite) {
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
