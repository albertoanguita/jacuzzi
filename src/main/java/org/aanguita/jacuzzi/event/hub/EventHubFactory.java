package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.objects.ObjectMapPoolAdvancedCreator;

/**
 * Created by Alberto on 07/10/2016.
 */
public class EventHubFactory {

    public enum Type {
        SYNCHRONOUS,
        MIXED,
        ASYNCHRONOUS_SHORT_LIVED_THREAD,
        ASYNCHRONOUS_PERMANENT_THREAD
    }

    private static ObjectMapPoolAdvancedCreator<String, Type, EventHub> instances = new ObjectMapPoolAdvancedCreator<>(
            stringTypeDuple -> createEventHub(stringTypeDuple.element1, stringTypeDuple.element2));

    private static EventHub createEventHub(String name, Type type) {
        switch (type) {

            case SYNCHRONOUS:
                return new SynchronousEventHub(name);
            case MIXED:
                return new MixedEventHub(name);
            case ASYNCHRONOUS_SHORT_LIVED_THREAD:
                return new AsynchronousShortLivedThreadEventHub(name);
            case ASYNCHRONOUS_PERMANENT_THREAD:
                return new AsynchronousPermanentThreadEventHub(name);
            default:
                throw new IllegalArgumentException("Invalid event hub type: " + type);
        }
    }
}
