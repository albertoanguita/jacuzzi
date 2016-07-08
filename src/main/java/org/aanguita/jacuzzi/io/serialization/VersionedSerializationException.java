package org.aanguita.jacuzzi.io.serialization;

import java.util.Map;

/**
 * Exception issued during the deserialization of versioned objects
 */
public class VersionedSerializationException extends Exception {

    public enum Reason {
        INCORRECT_DATA,
        CLASS_NOT_FOUND,
        CRC_MISMATCH,
        UNRECOGNIZED_VERSION
    }

    public final VersionStack versionStack;

    public final Map<String, Object> attributes;

    public final Reason reason;

    public VersionedSerializationException(VersionStack versionStack, Map<String, Object> attributes, Reason reason) {
        this.versionStack = versionStack;
        this.attributes = attributes;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "VersionedSerializationException{" +
                "versionStack='" + versionStack + '\'' +
                ", attributes=" + attributes +
                ", reason=" + reason +
                '}';
    }
}
