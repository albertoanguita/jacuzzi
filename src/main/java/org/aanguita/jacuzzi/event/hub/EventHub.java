package org.aanguita.jacuzzi.event.hub;

import java.util.Set;

/**
 * Created by Alberto on 07/10/2016.
 */
public interface EventHub {

    String getName();

    EventHubFactory.Type getType();

    void publish(String channel, Object... messages);

    void publish(String channel, boolean inBackground, Object... messages);

    void subscribe(String subscriberId, EventHubSubscriber subscriber, String... channelExpressions);

    void subscribe(String subscriberId, EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions);

    void unsubscribe(String subscriberId, String... channelExpressions);

    Set<String> cachedChannels();

    void close();
}
