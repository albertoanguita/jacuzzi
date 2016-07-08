package org.aanguita.jacuzzi.io.buffer;

import java.io.IOException;

/**
 * Interface for implementing byte array input streams. Used in the ReadBuffer class
 */
public interface BufferStream {

    boolean hasMoreBytes();

    byte[] readNextBytes() throws IOException;
}
