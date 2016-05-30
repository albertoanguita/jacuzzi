package jacz.util.io.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class implements a serializable object which itself contains a list of objects. It is useful for client
 * applications of the ChannelModule, so they can send a list of objects in a single invocation
 */
public class ObjectListWrapper implements Serializable {

    /**
     * The list of objects stored by this ObjectListWrapper
     */
    private final List<Object> objects;

    /**
     * Class constructor. Builds an isEmpty list of objects
     */
    public ObjectListWrapper() {
        objects = new ArrayList<>();
    }

    /**
     * Class constructor. Initializes the list of stored objects with the objects given in the parameter
     *
     * @param objects the objects to initially store in this ObjectListWrapper
     */
    public ObjectListWrapper(Object... objects) {
        this.objects = new ArrayList<>(Arrays.asList(objects));
    }

    /**
     * Retrieves the list of objects stored in this ObjectListWrapper
     *
     * @return the list of objects stored in this ObjectListWrapper
     */
    public List<Object> getObjects() {
        return objects;
    }

    /**
     * Adds a list of objects to the objects stored in this ObjectListWrapper
     *
     * @param objects objects to push to the list of stored objects
     */
    public void addObjects(Object... objects) {
        this.objects.addAll(Arrays.asList(objects));
    }
}
