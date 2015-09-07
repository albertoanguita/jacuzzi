package jacz.util.io.object_serialization;

import java.util.Map;

/**
 * Interface with methods for saving and restoring the state of an object
 * <p/>
 * Objects implementing this interface can be saved using the VersionedObjectSerializer
 */
public interface VersionedObject {

    public String getCurrentVersion();

    public Map<String, Object> serialize();

    public void deserialize(String version, Map<String, Object> attributes) throws RuntimeException;

    public void errorDeserializing(String version, Map<String, Object> attributes);
}
