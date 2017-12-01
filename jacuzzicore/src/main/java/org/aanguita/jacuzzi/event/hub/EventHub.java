package org.aanguita.jacuzzi.event.hub;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Alberto on 07/10/2016.
 * todo simplify registration procedure
 */
public interface EventHub {

    String getName();

    void start();

    void pause();

    void resume();

    EventHubFactory.Type getType();

    void publish(String channel, Object... messages);

    void publish(Long keepMillis, String channel, Object... messages);

    Collection<String> getSubscribers();

    void registerSubscriber(String subscriberId, EventHubSubscriber subscriber, EventHubFactory.Type type);

    void subscribe(EventHubSubscriber subscriber, EventHubFactory.Type type, String... channelExpressions);

    void subscribe(String subscriberId, String... channelExpressions);

    void subscribe(String subscriberId, int priority, String... channelExpressions);

    void unsubscribe(String subscriberId, String channelExpression, String... otherChannelExpressions);

    void unsubscribeAll(String subscriberId);

    void unregisterSubscriber(String subscriberId);

    int getSubscribersCount();

    int getSubscribersCount(String channelExpression);

    boolean hasSubscribers();

    List<Publication> getStoredPublications(String... channelExpressions);

    Set<String> cachedChannels();

    void close();
}
