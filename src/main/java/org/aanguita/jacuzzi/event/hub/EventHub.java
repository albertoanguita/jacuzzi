package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 07/10/2016.
 */
public interface EventHub {

    String getName();

    EventHubFactory.Type getType();

    void publish(String channel, Object... messages);

    void publish(String channel, boolean inBackground, Object... messages);

    void subscribe(EventHubSubscriber subscriber, String... channelExpressions);

    void subscribe(EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions);

    void unsubscribe(EventHubSubscriber subscriber, String... channelExpressions);

    void close();
}
