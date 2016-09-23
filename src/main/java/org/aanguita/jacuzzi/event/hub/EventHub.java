package org.aanguita.jacuzzi.event.hub;

/**
 * This class provides named event hubs. An event hub allows publishing messages with some tags, while other classes can subscribe from specific
 * tags and receive the corresponding notifications. In esence, this class provides an observer pattern implementation, allowing very decoupled
 * code to use it.
 * <p>
 * This class provides static methods for creating event hubs. It also maintains a coherent pool of named event hubs, so these can be retrieved
 * asynchronously. Clients can register as many event hubs as desired, identifying them by a name.
 *
 * @author aanguita
 *         21/09/2016
 */
public class EventHub {

    private final String name;

    public EventHub(String name) {
        this.name = name;
    }
}
