package org.aanguita.jacuzzi.io.serialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Alberto on 04/01/2016.
 */
public class VersionedObjectPreview implements VersionedObject {

    private String version;

    private Map<String, Object> attributes;

    private VersionStack parentVersions;

    public VersionedObjectPreview(String path, String... backupPaths) throws VersionedSerializationException, IOException {
        VersionedObjectSerializer.deserialize(this, path, backupPaths);
    }

    public VersionedObjectPreview(byte[] data) throws VersionedSerializationException {
        VersionedObjectSerializer.deserialize(this, data);
    }

    public VersionedObjectPreview(byte[] data, MutableOffset offset) throws VersionedSerializationException {
        VersionedObjectSerializer.deserialize(this, data, offset);
    }

    public String getVersion() {
        return version;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public VersionStack getParentVersions() {
        return parentVersions;
    }

    @Override
    public VersionStack getCurrentVersion() {
        // not used
        throw new RuntimeException("This method must not be used in this object");
    }

    @Override
    public Map<String, Serializable> serialize() {
        // not used
        throw new RuntimeException("This method must not be used in this object");
    }

    @Override
    public void deserialize(String version, Map<String, Object> attributes, VersionStack parentVersions) throws UnrecognizedVersionException {
        this.version = version;
        this.attributes = attributes;
        this.parentVersions = parentVersions;
    }
}
