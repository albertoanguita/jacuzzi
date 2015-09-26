package jacz.util.identifier;

import jacz.util.io.object_serialization.SerializationException;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.io.object_serialization.MutableOffset;

import java.io.Serializable;

/**
 * A class whose objects store an immutable unique identifier. Identifiers are internally represented by a long.
 * An object of this class cannot be modified after it has been requested, making it suitable for being index of
 * tables.
 * <p/>
 * User: Admin<br>
 * Date: 02-mar-2009<br>
 * Last Modified: 02-mar-2009
 */
public final class UniqueIdentifier implements Serializable {

    private final Long id;

    UniqueIdentifier(Long id) {
        this.id = id;
    }

    public UniqueIdentifier(String s) throws NumberFormatException {
        id = Long.parseLong(s);
    }

    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueIdentifier that = (UniqueIdentifier) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public byte[] serialize() {
        return Serializer.serialize(id);
    }

    public static UniqueIdentifier deserialize(byte[] data) throws SerializationException {
        return new UniqueIdentifier(Serializer.deserializeLong(data, new MutableOffset()));
    }

    public static UniqueIdentifier deserializeWithOffset(byte[] data, MutableOffset offset) throws SerializationException {
        return new UniqueIdentifier(Serializer.deserializeLong(data, offset));
    }
}
