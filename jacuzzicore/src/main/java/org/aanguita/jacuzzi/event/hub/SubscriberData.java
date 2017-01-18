package org.aanguita.jacuzzi.event.hub;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 16/11/2016.
 */
class SubscriberData {

    static class ChannelWithPriority {

        private final Channel channel;

        private final int priority;

        private ChannelWithPriority(Channel channel) {
            this(channel, 0);
        }

        private ChannelWithPriority(Channel channel, int priority) {
            this.channel = channel;
            this.priority = priority;
        }

        Channel getChannel() {
            return channel;
        }

        int getPriority() {
            return priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChannelWithPriority that = (ChannelWithPriority) o;

            return channel.equals(that.channel);
        }

        @Override
        public int hashCode() {
            return channel.hashCode();
        }
    }

    private final String subscriberId;

    private final EventHubSubscriber subscriber;

    private final SubscriberProcessor subscriberProcessor;

    private final Set<ChannelWithPriority> channels;

    SubscriberData(String subscriberId, EventHubSubscriber subscriber, SubscriberProcessor subscriberProcessor) {
        this.subscriberId = subscriberId;
        this.subscriber = subscriber;
        this.subscriberProcessor = subscriberProcessor;
        channels = new HashSet<>();
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    EventHubSubscriber getSubscriber() {
        return subscriber;
    }

    SubscriberProcessor getSubscriberProcessor() {
        return subscriberProcessor;
    }

    Set<ChannelWithPriority> getChannels() {
        return channels;
    }

    void subscribe(int priority, String... channelExpressions) {
        Set<ChannelWithPriority> newChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .map(channel -> new ChannelWithPriority(channel, priority))
                .collect(Collectors.toSet());
        channels.addAll(newChannels);
    }

    void unsubscribe(String... channelExpressions) {
        Set<ChannelWithPriority> oldChannels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .map(ChannelWithPriority::new)
                .collect(Collectors.toSet());
        channels.removeAll(oldChannels);
    }

    void unsubscribeAll() {
        channels.clear();
    }

    void close() {
        subscriberProcessor.close();
    }
}
