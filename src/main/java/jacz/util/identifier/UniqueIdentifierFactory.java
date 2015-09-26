package jacz.util.identifier;

import jacz.util.io.object_serialization.SerializationException;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.files.FileReaderWriter;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public final class UniqueIdentifierFactory implements Serializable {

    private Long nextId;

    private static Long nextStaticId = getFirstIdentifier();

    public UniqueIdentifierFactory() {
        nextId = getFirstIdentifier();
    }

    public UniqueIdentifierFactory(String path) throws IOException, ClassNotFoundException {
        nextId = (Long) FileReaderWriter.readObject(path);
    }

    public UniqueIdentifierFactory(byte[] data, MutableOffset mutableOffset) throws SerializationException {
        nextId = Serializer.deserializeLong(data, mutableOffset);
    }

    private static Long getFirstIdentifier() {
        return (long) 0;
    }

    @Override
    public String toString() {
        return nextId.toString();
    }

    public static UniqueIdentifierFactory createFromString(String s) {
        UniqueIdentifierFactory uniqueIdentifierFactory = new UniqueIdentifierFactory();
        uniqueIdentifierFactory.nextId = Long.parseLong(s);
        return uniqueIdentifierFactory;
    }

    public synchronized UniqueIdentifier getOneIdentifier() {
        return new UniqueIdentifier(nextId++);
    }

    public static synchronized UniqueIdentifier getOneStaticIdentifier() {
        return new UniqueIdentifier(nextStaticId++);
    }


    public synchronized void save(String path) throws IOException {
        FileReaderWriter.writeObject(path, nextId);
    }

    public synchronized byte[] serialize() {
        return Serializer.serialize(nextId);
    }
}
