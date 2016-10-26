package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This class provides named event hubs. An event hub allows publishing messages with some tags, while other classes can subscribe from specific
 * tags and receive the corresponding notifications. In essence, this class provides an observer pattern implementation, allowing very decoupled
 * code to use it.
 * <p>
 * This class provides static methods for creating event hubs. It also maintains a coherent pool of named event hubs, so these can be retrieved
 * asynchronously. Clients can register as many event hubs as desired, identifying them by a name.
 *
 * @author aanguita
 *         21/09/2016
 */
abstract class AbstractEventHub implements EventHub {

    private static class Channel {

        private static final String ONE_LEVEL_WILDCARD = "?";

        private static final String MULTILEVEL_WILDCARD = "*";

        private final String original;

        private final List<String> levels;

        private Channel(String channel) {
            original = channel;
            levels = new ArrayList<>(Arrays.asList(channel.split("/")));
        }

        private boolean matches(Channel channel) {
            int index = 0;
            int thisSize = levels.size();
            int otherSize = channel.levels.size();
            while (index < thisSize && index < otherSize) {
                if (levels.get(index).equals(Channel.MULTILEVEL_WILDCARD)) {
                    return true;
                } else if (levels.get(index).equals(Channel.ONE_LEVEL_WILDCARD) || levels.get(index).equals(channel.levels.get(index))) {
                    index++;
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Channel channel = (Channel) o;

            return original.equals(channel.original);
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public String toString() {
            return "Channel{" + original + '}';
        }
    }

    private static class SubscriberAndChannelExpressions {

        private final EventHubSubscriber subscriber;

        private final Set<Channel> synchronousChannels;

        private final Set<Channel> asynchronousChannels;

        private SubscriberAndChannelExpressions(EventHubSubscriber subscriber) {
            this.subscriber = subscriber;
            synchronousChannels = new HashSet<>();
            asynchronousChannels = new HashSet<>();
        }

        private SubscriberAndChannelExpressions(EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions) {
            this(subscriber);
            subscribe(inBackground, channelExpressions);
        }

        private void subscribe(boolean inBackground, String... channelExpressions) {
            Set<Channel> newChannels = Arrays.stream(channelExpressions)
                    .map(Channel::new)
                    .collect(Collectors.toSet());
            if (inBackground) {
                asynchronousChannels.addAll(newChannels);
            } else {
                synchronousChannels.addAll(newChannels);
            }
        }

        private void unsubscribe(String... channelExpressions) {
            Set<Channel> oldChannels = Arrays.stream(channelExpressions)
                    .map(Channel::new)
                    .collect(Collectors.toSet());
            synchronousChannels.removeAll(oldChannels);
            asynchronousChannels.removeAll(oldChannels);
        }
    }

    private static class ChannelCache {

        private final Map<Channel, List<Duple<EventHubSubscriber, Boolean>>> cachedExpressions;

        private ChannelCache() {
            cachedExpressions = new HashMap<>();
        }

        private void invalidate() {
            cachedExpressions.clear();
        }

        private boolean containsChannel(Channel channel) {
            return cachedExpressions.containsKey(channel);
        }

        private void initChannel(Channel channel) {
            cachedExpressions.put(channel, new ArrayList<>());
        }

        private void addSubscriber(Channel channel, EventHubSubscriber eventHubSubscriber, boolean inBackground) {
            cachedExpressions.get(channel).add(new Duple<>(eventHubSubscriber, inBackground));
        }

        private List<Duple<EventHubSubscriber, Boolean>> getSubscribersForExpression(Channel channel) {
            return cachedExpressions.get(channel);
        }
    }

    private final String name;

    private final Map<String, SubscriberAndChannelExpressions> subscribers;

    private final ChannelCache channelCache;

    AbstractEventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
        channelCache = new ChannelCache();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void publish(String channel, Object... messages) {
        publish(channel, false, messages);
    }

    @Override
    public void publish(String channel, boolean inBackground, Object... messages) {
        Channel parsedChannel = new Channel(channel);
        List<Duple<EventHubSubscriber, Boolean>> subscribers = findSubscribers(parsedChannel);
        publish(subscribers, channel, inBackground, messages);
    }

    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        if (inBackground) {
            ThreadExecutor.submit(() -> invokeSubscribers(subscribers, true, channel, messages));
        } else {
            invokeSubscribers(subscribers, false, channel, messages);
        }
    }

    protected void invokeSubscribers(Collection<Duple<EventHubSubscriber, Boolean>> subscribers, boolean haveThreadAvailable, String channel, Object... messages) {
        for (Duple<EventHubSubscriber, Boolean> eventHubSubscriber : subscribers) {
            haveThreadAvailable = invokeSubscriber(eventHubSubscriber, haveThreadAvailable, channel, messages);
        }
    }

    protected boolean invokeSubscriber(Duple<EventHubSubscriber, Boolean> eventHubSubscriber, boolean haveThreadAvailable, String channel, Object... messages) {
        if (eventHubSubscriber.element2) {
            // this subscriber wants a thread for his own
            if (haveThreadAvailable) {
                // use the available thread
                eventHubSubscriber.element1.event(channel, messages);
                haveThreadAvailable = false;
            } else {
                // create a new thread only for him
                ThreadExecutor.submit(() -> eventHubSubscriber.element1.event(channel, messages));
            }
        } else {
            // do not spawn a new thread
            eventHubSubscriber.element1.event(channel, messages);
        }
        return haveThreadAvailable;
    }

    private synchronized List<Duple<EventHubSubscriber, Boolean>> findSubscribers(Channel channel) {
        List<Duple<EventHubSubscriber, Boolean>> subscribersAndBackground = new ArrayList<>();
        if (channelCache.containsChannel(channel)) {
            // use the cache
            return channelCache.getSubscribersForExpression(channel);
        } else {
            // set the cache for this channel
            channelCache.initChannel(channel);
            for (SubscriberAndChannelExpressions subscriber : subscribers.values()) {
                Duple<Boolean, Boolean> expressionsMatchChannel = subscriberMatchesChannel(subscriber, channel);
                if (expressionsMatchChannel.element1) {
                    subscribersAndBackground.add(new Duple<>(subscriber.subscriber, expressionsMatchChannel.element2));
                    channelCache.addSubscriber(channel, subscriber.subscriber, expressionsMatchChannel.element2);
                }
            }
        }
        return subscribersAndBackground;
    }

    private Duple<Boolean, Boolean> subscriberMatchesChannel(SubscriberAndChannelExpressions subscriber, Channel channel) {
        if (expressionsMatchChannel(subscriber.asynchronousChannels, channel)) {
            return new Duple<>(true, true);
        } else if (expressionsMatchChannel(subscriber.synchronousChannels, channel)) {
            return new Duple<>(true, false);
        } else {
            return new Duple<>(false, false);
        }
    }

    private boolean expressionsMatchChannel(Set<Channel> channels, Channel channel) {
        return channels.stream().anyMatch(aChannel -> aChannel.matches(channel));
    }

    @Override
    public synchronized void subscribe(String subscriberId, EventHubSubscriber subscriber, String... channelExpressions) {
        subscribe(subscriberId, subscriber, false, channelExpressions);
    }

    @Override
    public synchronized void subscribe(String subscriberId, EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions) {
        if (!subscribers.containsKey(subscriberId)) {
            subscribers.put(subscriberId, new SubscriberAndChannelExpressions(subscriber, inBackground, channelExpressions));
        } else {
            subscribers.get(subscriberId).subscribe(inBackground, channelExpressions);
        }
        channelCache.invalidate();
    }

    @Override
    public synchronized void unsubscribe(String subscriberId, String... channelExpressions) {
        if (subscribers.containsKey(subscriberId)) {
            subscribers.get(subscriberId).unsubscribe(channelExpressions);
        }
        channelCache.invalidate();
    }

    /**
     * Override if some resources need to be closed/cleaned up
     */
    @Override
    public void close() {
    }
}
