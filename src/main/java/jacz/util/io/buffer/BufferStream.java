package jacz.util.io.buffer;

import java.io.IOException;

/**
 * Interface for implementing byte array input streams. Used in the ReadBuffer class
 */
public interface BufferStream {

    public boolean hasMoreBytes();

    public byte[] readNextBytes() throws IOException;
}
