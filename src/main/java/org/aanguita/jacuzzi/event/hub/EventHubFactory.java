package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.objects.ObjectMapPoolAdvancedCreator;

/**
 * Created by Alberto on 07/10/2016.
 */
public class EventHubFactory {

    public enum Type {
        SIMPLE,
        ASYNCHRONOUS_EVENTUAL_THREAD,
        ASYNCHRONOUS_PERMANENT_THREAD
    }

    private static ObjectMapPoolAdvancedCreator<String, Type, EventHub> eventHubs = new ObjectMapPoolAdvancedCreator<>(
            stringTypeDuple -> create(stringTypeDuple.element1, stringTypeDuple.element2));

    public static EventHub createEventHub(String name, Type type) {
        return eventHubs.createObject(name, type);
    }

    public static EventHub getEventHub(String name) {
        return eventHubs.getObject(name);
    }

    private static EventHub create(String name, Type type) {
        switch (type) {

            case SIMPLE:
                return new SimpleEventHub(name);
            case ASYNCHRONOUS_EVENTUAL_THREAD:
                return new AsynchronousEventualThreadEventHub(name);
            case ASYNCHRONOUS_PERMANENT_THREAD:
                return new AsynchronousPermanentThreadEventHub(name);
            default:
                throw new IllegalArgumentException("Invalid event hub type: " + type);
        }
    }
}
