package jacz.util.io.object_serialization;

import java.util.Map;

/**
 * Exception issued during the deserialization of versioned objects
 */
public class VersionedSerializationException extends Exception {

    public enum Reason {
        NULL_VALUES_FOUND,
        CLASS_NOT_FOUND,
        CRC_MISMATCH
    }

    public final String version;

    public final Map<String, Object> attributes;

    public final Reason reason;

    public VersionedSerializationException(String version, Map<String, Object> attributes, Reason reason) {
        this.version = version;
        this.attributes = attributes;
        this.reason = reason;
    }
}
