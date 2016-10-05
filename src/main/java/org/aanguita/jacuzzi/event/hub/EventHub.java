package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
import org.aanguita.jacuzzi.objects.ObjectMapPool;

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
public class EventHub {

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
    }

    private static class ParsedChannelExpression extends ParsedChannel {

        private boolean inBackground;

//        public static ParsedChannelExpression parseChannelExpression(String channel) {
//            return new ParsedChannelExpression(channel);
//        }

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

        public SubscriberAndChannelExpressions(EventHubSubscriber subscriber, String... channelExpressions) {
            this(subscriber);
            subscribe(channelExpressions);
        }

        private void subscribe(boolean inBackground, String... channelExpressions) {
            this.channelExpressions.addAll(Arrays.stream(channelExpressions)
                    .map(ParsedChannelExpression::new)
                    .collect(Collectors.toList()));
        }

        private void unsubscribe(String... channelExpressions) {
            this.channelExpressions.removeAll(Arrays.stream(channelExpressions)
                    .map(ParsedChannelExpression::new)
                    .collect(Collectors.toList()));
        }
    }

    private static ObjectMapPool<String, EventHub> instances = new ObjectMapPool<>(EventHub::new);

    private final String name;

    private final Map<String, SubscriberAndChannelExpressions> subscribers;

    public static EventHub getEventHub(String name) {
        return instances.getObject(name);
    }

    private EventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void publish(String channel, Object... messages) {
        publish(channel, false, messages);
    }

    public void publish(String channel, boolean inBackground, Object... messages) {
        ParsedChannel parsedChannel = ParsedChannel.parseChannel(channel);
        List<EventHubSubscriber> subscribers = findSubscribers(parsedChannel);
        if (inBackground) {
            ThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    invokeSubscribers(subscribers, true, channel, messages);
                }
            });
        } else {
            invokeSubscribers(subscribers, false, channel, messages);
        }
        for (EventHubSubscriber eventHubSubscriber : findSubscribers(parsedChannel)) {
            eventHubSubscriber.event(channel, messages);
        }
    }

    private void invokeSubscribers(Collection<EventHubSubscriber> subscribers, boolean newThread, String channel, Object... messages) {
        for (EventHubSubscriber eventHubSubscriber : subscribers) {
            eventHubSubscriber.event(channel, messages);
        }
    }

    private synchronized List<EventHubSubscriber> findSubscribers(ParsedChannel parsedChannel) {
        List<EventHubSubscriber> foundSubscribers = new ArrayList<>();
        for (SubscriberAndChannelExpressions subscriberAndChannelExpressions : subscribers.values()) {
            if (expressionsMatchChannel(subscriberAndChannelExpressions.channelExpressions, parsedChannel)) {
                foundSubscribers.add(subscriberAndChannelExpressions.subscriber);
            }
        }
        return foundSubscribers;
    }

    private boolean expressionsMatchChannel(Set<ParsedChannelExpression> channelExpressions, ParsedChannel parsedChannel) {
        for (ParsedChannelExpression channelExpression : channelExpressions) {
            if (channelExpression.expressionMatchesChannel(parsedChannel)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(EventHubSubscriber subscriber, String... channelExpressions) {
        subscribe(subscriber, false, channelExpressions);
    }

    public synchronized void subscribe(EventHubSubscriber subscriber, boolean inBackground, String... channelExpressions) {
        if (!subscribers.containsKey(subscriber.getId())) {
            subscribers.put(subscriber.getId(), new SubscriberAndChannelExpressions(subscriber, channelExpressions));
        } else {
            subscribers.get(subscriber.getId()).subscribe(channelExpressions);
        }
    }

    public synchronized void unsubscribe(EventHubSubscriber subscriber, String... channelExpressions) {
        if (subscribers.containsKey(subscriber.getId())) {
            subscribers.get(subscriber.getId()).unsubscribe(channelExpressions);
        }
    }
}
