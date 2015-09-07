package jacz.util.io.object_serialization;

/**
 * todo remove
 */
public interface SerializableObject<T> {

    public byte[] serializeToByteArray();

    public T deserializeFromByteArray(byte[] data, MutableOffset offset);
}
