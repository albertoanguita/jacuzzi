package jacz.util.concurrency;

import jacz.util.maps.ObjectCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class implements a store of generic elements that require manual removal at the end of execution.
 * <p/>
 * It allows debugging in case, e.g., not all threads are closed at a process completion.
 */
public class ManuallyRemovedElementBag {

    private static final Logger logger = LoggerFactory.getLogger(ManuallyRemovedElementBag.class);

    private static Map<String, ManuallyRemovedElementBag> instancesMap = new HashMap<>();

    private ObjectCount<String> elementCount;

    public static synchronized ManuallyRemovedElementBag getInstance(String bag) {
        if (!instancesMap.containsKey(bag)) {
            instancesMap.put(bag, new ManuallyRemovedElementBag());
        }
        return instancesMap.get(bag);
    }

    private ManuallyRemovedElementBag() {
        elementCount = new ObjectCount<>(new HashSet<>(), true, true);
    }

    public synchronized void createElement(String name) {
        elementCount.addObject(name);
        logger.debug("created manually removed element: " + name + getObjectCount(name));
    }

    public synchronized void destroyElement(String name) {
        elementCount.subtractObject(name);
        logger.debug("destroyed manually removed element: " + name + getObjectCount(name));
    }

    public synchronized ObjectCount<String> getRemainingElements() {
        return elementCount;
    }

    private String getObjectCount(String name) {
        return ". There are " + elementCount.getObjectCount(name) + " objects";
    }
}
