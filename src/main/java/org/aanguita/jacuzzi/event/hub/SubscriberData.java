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

    private final SubscriberProcessor subscriberProcessor;

    private final Set<Duple<Channel, Integer>> channels;

//    private final Set<Duple<Channel, Integer>> synchronousChannels;
//
//    private final Set<Duple<Channel, Integer>> asynchronousChannels;

    SubscriberData(String subscriberId, EventHubSubscriber subscriber, SubscriberProcessor subscriberProcessor) {
        this.subscriberId = subscriberId;
        this.subscriber = subscriber;
        this.subscriberProcessor = subscriberProcessor;
        channels = new HashSet<>();
//        synchronousChannels = new HashSet<>();
//        asynchronousChannels = new HashSet<>();
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    EventHubSubscriber getSubscriber() {
        return subscriber;
    }

    public SubscriberProcessor getSubscriberProcessor() {
        return subscriberProcessor;
    }

    public Set<Duple<Channel, Integer>> getChannels() {
        return channels;
    }

    //    Set<Duple<Channel, Integer>> getSynchronousChannels() {
//        return synchronousChannels;
//    }
//
//    Set<Duple<Channel, Integer>> getAsynchronousChannels() {
//        return asynchronousChannels;
//    }

    void subscribe(int priority, String... channelExpressions) {
        Set<Duple<Channel, Integer>> newChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .map(channel -> new Duple<>(channel, priority))
                .collect(Collectors.toSet());
        channels.addAll(newChannels);
//        if (inBackground) {
//            asynchronousChannels.addAll(newChannels);
//        } else {
//            synchronousChannels.addAll(newChannels);
//        }
    }

    void unsubscribe(String... channelExpressions) {
        Set<Channel> oldChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .collect(Collectors.toSet());
        // todo fix
        channels.removeAll(oldChannels);
//        synchronousChannels.removeAll(oldChannels);
//        asynchronousChannels.removeAll(oldChannels);
    }

    void close() {
        subscriberProcessor.close();
    }
}
