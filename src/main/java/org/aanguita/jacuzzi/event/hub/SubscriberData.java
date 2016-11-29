package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 16/11/2016.
 */
class SubscriberData {

    private final String subscriberId;

    private final EventHubSubscriber subscriber;

    private final Set<Duple<Channel, Integer>> synchronousChannels;

    private final Set<Duple<Channel, Integer>> asynchronousChannels;

    private SubscriberData(String subscriberId, EventHubSubscriber subscriber) {
        this.subscriberId = subscriberId;
        this.subscriber = subscriber;
        synchronousChannels = new HashSet<>();
        asynchronousChannels = new HashSet<>();
    }

    SubscriberData(String subscriberId, EventHubSubscriber subscriber, int priority, boolean inBackground, String... channelExpressions) {
        this(subscriberId, subscriber);
        subscribe(priority, inBackground, channelExpressions);
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    EventHubSubscriber getSubscriber() {
        return subscriber;
    }

    Set<Duple<Channel, Integer>> getSynchronousChannels() {
        return synchronousChannels;
    }

    Set<Duple<Channel, Integer>> getAsynchronousChannels() {
        return asynchronousChannels;
    }

    void subscribe(int priority, boolean inBackground, String... channelExpressions) {
        Set<Duple<Channel, Integer>> newChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .map(channel -> new Duple<>(channel, priority))
                .collect(Collectors.toSet());
        if (inBackground) {
            asynchronousChannels.addAll(newChannels);
        } else {
            synchronousChannels.addAll(newChannels);
        }
    }

    void unsubscribe(String... channelExpressions) {
        Set<Channel> oldChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .collect(Collectors.toSet());
        synchronousChannels.removeAll(oldChannels);
        asynchronousChannels.removeAll(oldChannels);
    }
}
