package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides named event hubs. An event hub allows publishing messages with some tags, while other classes can subscribe from specific
 * tags and receive the corresponding notifications. In esence, this class provides an observer pattern implementation, allowing very decoupled
 * code to use it.
 * <p>
 * This class provides static methods for creating event hubs. It also maintains a coherent pool of named event hubs, so these can be retrieved
 * asynchronously. Clients can register as many event hubs as desired, identifying them by a name.
 *
 * @author aanguita
 *         21/09/2016
 */
abstract class AbstractEventHub implements EventHub {

    private static class ParsedChannel {

        protected static final String ONE_LEVEL_WILDCARD = "?";

        protected static final String MULTILEVEL_WILDCARD = "*";

        protected final List<String> levels;

        public static ParsedChannel parseChannel(String channel) {
            ParsedChannel parsedChannel = new ParsedChannel(channel);
            for (String level : parsedChannel.levels) {
                if (level.equals(ONE_LEVEL_WILDCARD) || level.equals(MULTILEVEL_WILDCARD)) {
                    throw new IllegalArgumentException("Levels of a channel cannot match a wildcard: " + level);
                }
            }
            return parsedChannel;
        }

        private ParsedChannel(String channel) {
            levels = new ArrayList<>(Arrays.asList(channel.split("/")));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParsedChannel that = (ParsedChannel) o;

            return levels.equals(that.levels);
        }
    }

    private static class ParsedChannelExpression extends ParsedChannel {

        private final boolean inBackground;

        public ParsedChannelExpression(String channel, boolean inBackground) {
            super(channel);
            this.inBackground = inBackground;
        }

        public boolean expressionMatchesChannel(ParsedChannel parsedChannel) {
            int index = 0;
            int thisSize = levels.size();
            int otherSize = parsedChannel.levels.size();
            while (index < thisSize && index < otherSize) {
                if (levels.get(index).equals(MULTILEVEL_WILDCARD)) {
                    return true;
                } else if (levels.get(index).equals(ONE_LEVEL_WILDCARD) || levels.get(index).equals(parsedChannel.levels.get(index))) {
                    index++;
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    private static class SubscriberAndChannelExpressions {

        private final EventHubSubscriber subscriber;

        private final Set<ParsedChannelExpression> channelExpressions;

        public SubscriberAndChannelExpressions(EventHubSubscriber subscriber) {
            this.subscriber = subscriber;
            channelExpressions = new HashSet<>();
        }

        public SubscriberAndChannelExpressions(EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions) {
            this(subscriber);
            subscribe(inBackground, channelExpressions);
        }

        private void subscribe(boolean inBackground, String... channelExpressions) {
            this.channelExpressions.addAll(Arrays.stream(channelExpressions)
                    .map(channel -> new ParsedChannelExpression(channel, inBackground))
                    .collect(Collectors.toList()));
        }

        private void unsubscribe(String... channelExpressions) {
            this.channelExpressions.removeAll(Arrays.stream(channelExpressions)
                    .map(channel -> new ParsedChannelExpression(channel, false))
                    .collect(Collectors.toList()));
        }
    }

    private final String name;

    private final Map<String, SubscriberAndChannelExpressions> subscribers;



    AbstractEventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
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
        ParsedChannel parsedChannel = ParsedChannel.parseChannel(channel);
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

    protected synchronized List<Duple<EventHubSubscriber, Boolean>> findSubscribers(ParsedChannel parsedChannel) {
        List<Duple<EventHubSubscriber, Boolean>> foundSubscribers = new ArrayList<>();
        for (SubscriberAndChannelExpressions subscriberAndChannelExpressions : subscribers.values()) {
            Duple<Boolean, Boolean> expressionsMatchChannel = expressionsMatchChannel(subscriberAndChannelExpressions.channelExpressions, parsedChannel);
            if (expressionsMatchChannel.element1) {
                foundSubscribers.add(new Duple<>(subscriberAndChannelExpressions.subscriber, expressionsMatchChannel.element2));
            }
        }
        return foundSubscribers;
    }

    private Duple<Boolean, Boolean> expressionsMatchChannel(Set<ParsedChannelExpression> channelExpressions, ParsedChannel parsedChannel) {
        boolean oneExpressionMatches = false;
        for (ParsedChannelExpression channelExpression : channelExpressions) {
            if (channelExpression.expressionMatchesChannel(parsedChannel)) {
                oneExpressionMatches = true;
                if (channelExpression.inBackground) {
                    return new Duple<>(true, true);
                }
            }
        }
        return new Duple<>(oneExpressionMatches, false);
    }

    @Override
    public synchronized void subscribe(EventHubSubscriber subscriber, String... channelExpressions) {
        subscribe(subscriber, false, channelExpressions);
    }

    @Override
    public synchronized void subscribe(EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions) {
        if (!subscribers.containsKey(subscriber.getId())) {
            subscribers.put(subscriber.getId(), new SubscriberAndChannelExpressions(subscriber, inBackground, channelExpressions));
        } else {
            subscribers.get(subscriber.getId()).subscribe(inBackground, channelExpressions);
        }
    }

    @Override
    public synchronized void unsubscribe(EventHubSubscriber subscriber, String... channelExpressions) {
        if (subscribers.containsKey(subscriber.getId())) {
            subscribers.get(subscriber.getId()).unsubscribe(channelExpressions);
        }
    }

    /**
     * Override if some resources need to be closed/cleaned up
     */
    @Override
    public void close() {}
}
