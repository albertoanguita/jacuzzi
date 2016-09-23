package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.objects.ObjectMapPool;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private static class SubscriberAndChannelExpressions {

        private final EventHubSubscriber subscriber;

        private final Set<String> channelExpressions;

        public SubscriberAndChannelExpressions(EventHubSubscriber subscriber) {
            this.subscriber = subscriber;
            channelExpressions = new HashSet<>();
        }

        public SubscriberAndChannelExpressions(EventHubSubscriber subscriber, String... channelExpressions) {
            this(subscriber);
            subscribe(channelExpressions);
        }

        private void subscribe(String... channelExpressions) {
            this.channelExpressions.addAll(Arrays.asList(channelExpressions));
        }

        private void unsubscribe(String... channelExpressions) {
            this.channelExpressions.removeAll(Arrays.asList(channelExpressions));
        }
    }

    private static ObjectMapPool<String, EventHub> instances = new ObjectMapPool<>(EventHub::new);

    private final String name;

    private final Map<String, SubscriberAndChannelExpressions> subscribers;

    public static EventHub getEventHub(String name) {
        return instances.getObject(name);
    }

    private EventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
    }

    public void publish(String channel, Object... messages) {

    }

    public void subscribe(EventHubSubscriber subscriber, String... channelExpressions) {

    }

    public void unsubscribe(EventHubSubscriber subscriber, String... channelExpressions) {

    }
}
