package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.objects.ObjectMapPoolAdvancedCreator;

import java.util.function.Consumer;

/**
 * Created by Alberto on 07/10/2016.
 */
public class EventHubFactory {

    public enum Type {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD,
        ASYNCHRONOUS_QUEUE_PERMANENT_THREAD
    }

    private static ObjectMapPoolAdvancedCreator<String, Duple<Type, Consumer<Exception>>, EventHub> eventHubs = new ObjectMapPoolAdvancedCreator<>(
            stringTypeDuple -> create(stringTypeDuple.element1, stringTypeDuple.element2.element1, stringTypeDuple.element2.element2));

    public static EventHub createEventHub(String name, Type type) {
        return createEventHub(name, type, null);
    }

    public static EventHub createEventHub(String name, Type type, Consumer<Exception> exceptionConsumer) {
        return eventHubs.createObject(name, new Duple<>(type, exceptionConsumer));
    }

    public static EventHub getEventHub(String name) {
        return eventHubs.getObject(name);
    }

    private static EventHub create(String name, Type type, Consumer<Exception> exceptionConsumer) {
        switch (type) {

            case SYNCHRONOUS:
                return new SynchronousEventHub(name, exceptionConsumer);
            case ASYNCHRONOUS:
                return new AsynchronousEventHub(name, exceptionConsumer);
            case ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD:
                return new AsynchronousEventualThreadEventHub(name, exceptionConsumer);
            case ASYNCHRONOUS_QUEUE_PERMANENT_THREAD:
                return new AsynchronousPermanentThreadEventHub(name, exceptionConsumer);
            default:
                throw new IllegalArgumentException("Invalid event hub type: " + type);
        }
    }

    static void removeEventHub(String name) {
        eventHubs.removeObject(name);
    }
}
