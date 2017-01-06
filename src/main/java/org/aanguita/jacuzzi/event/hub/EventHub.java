package org.aanguita.jacuzzi.event.hub;

import java.util.List;
import java.util.Set;

/**
 * Created by Alberto on 07/10/2016.
 */
public interface EventHub {

    String getName();

    EventHubFactory.Type getType();

    void publish(String channel, Object message);

    void publish(String channel, Long keepMillis, boolean inBackground, Object... messages);

    void subscribe(EventHubSubscriber subscriber, String... channelExpressions);

    void subscribe(String subscriberId, EventHubSubscriber subscriber, String... channelExpressions);

    void subscribe(String subscriberId, EventHubSubscriber subscriber, int priority, boolean inBackground, String... channelExpressions);

    void unsubscribe(String subscriberId, String... channelExpressions);

    List<Publication> getStoredPublications(String... channelExpressions);

    Set<String> cachedChannels();

    void close();
}
