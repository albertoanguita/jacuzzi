package org.aanguita.jacuzzi.event.hub;

import java.util.List;
import java.util.Set;

/**
 * Created by Alberto on 07/10/2016.
 */
public interface EventHub {

    String getName();

    EventHubFactory.Type getType();

    void publish(String channel, Object... messages);

    void publish(Long keepMillis, String channel, Object... messages);

    void registerSubscriber(String subscriberId, EventHubSubscriber subscriber, EventHubFactory.SubscriberProcessorType subscriberProcessorType);

    void subscribe(EventHubSubscriber subscriber, EventHubFactory.SubscriberProcessorType subscriberProcessorType, String... channelExpressions);

    void subscribe(String subscriberId, String... channelExpressions);

    void subscribe(String subscriberId, int priority, String... channelExpressions);

    void unsubscribe(String subscriberId, String... channelExpressions);

    void unsubscribeAll(String subscriberId);

    void unregisterSubscriber(String subscriberId);

    List<Publication> getStoredPublications(String... channelExpressions);

    Set<String> cachedChannels();

    void close();
}
