package jacz.util.io.object_serialization;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface with methods for saving and restoring the state of an object
 * <p/>
 * Objects implementing this interface can be saved using the VersionedObjectSerializer
 */
public interface VersionedObject {

    String getCurrentVersion();

    Map<String, Serializable> serialize();

    void deserialize(String version, Map<String, Object> attributes) throws UnrecognizedVersionException;
}
