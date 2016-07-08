package aanguita.jacuzzi.io.buffer;

import java.io.IOException;
import java.util.Arrays;

/**
 * A reading buffer that stores bytes. The buffer is given a minimum size which is maintained through all execution
 * <p/>
 * The buffer is fed with an BufferStream implementation which contains arrays of bytes. Whenever the buffer needs more data,
 * it will get it from here (as many as needed to maintain the buffer larger than "size")
 */
public class ReadBuffer {

    private final int size;

    private final BufferStream bufferStream;

    byte[] buffer;

    public ReadBuffer(int size, BufferStream bufferStream) throws IOException {
        this.size = size;
        this.bufferStream = bufferStream;
        buffer = new byte[0];
        fillBuffer(size);
    }

    /**
     * Tries to fill the buffer with new data until it at least has the indicated size
     *
     * @param size minimum size that the buffer must have after the fill procedure
     * @throws IOException there where problems reading bytes from the stream buffer
     */
    private void fillBuffer(int size) throws IOException {
        while (buffer.length < size && bufferStream.hasMoreBytes()) {
            byte[] data = bufferStream.readNextBytes();
            int originalBufferLength = buffer.length;
            buffer = Arrays.copyOf(buffer, buffer.length + data.length);
            System.arraycopy(data, 0, buffer, originalBufferLength, data.length);
        }
    }

    public byte[] read(int size) throws IndexOutOfBoundsException, IOException {
        // fill the buffer until it has the necessary size for returning the requested data
        fillBuffer(size);
        if (buffer.length >= size) {
            // enough data to fulfill the request
            byte[] data = Arrays.copyOf(buffer, size);
            buffer = Arrays.copyOfRange(buffer, size, buffer.length);
            // maintain the buffer filled after the data has been extracted
            fillBuffer(this.size);
            return data;
        } else {
            // not enough data to fulfill the request
            throw new IndexOutOfBoundsException("Not enough data to fulfill the read request. Size requested: " + size + ". Data left size: " + buffer.length);
        }
    }
}
