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

    void pause(String channel);

    void resume(String channel);

    EventHubFactory.Type getType();

    void publish(String channel, Object... messages);

    void publish(Long keepMillis, String channel, Object... messages);

    Collection<String> getSubscribers();

    void registerSubscriber(EventHubSubscriber subscriber, EventHubFactory.Type type);

//    void subscribe(EventHubSubscriber subscriber, EventHubFactory.Type type, String... channelExpressions);

    void subscribe(EventHubSubscriber subscriber, String... channelExpressions);

    void subscribe(EventHubSubscriber subscriber, int priority, String... channelExpressions);

    void unsubscribe(EventHubSubscriber subscriber, String channelExpression, String... otherChannelExpressions);

    void unsubscribeAll(EventHubSubscriber subscriber);

    void unregisterSubscriber(EventHubSubscriber subscriber);

    int getSubscribersCount();

    int getSubscribersCount(String channelExpression);

    boolean hasSubscribers();

    List<Publication> getStoredPublications(String... channelExpressions);

    Set<String> cachedChannels();

    void close();
}
