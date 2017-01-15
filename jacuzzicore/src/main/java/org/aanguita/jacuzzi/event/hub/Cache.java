package org.aanguita.jacuzzi.event.hub;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by Alberto on 16/11/2016.
 */
class Cache {

    private final ConcurrentMap<Channel, List<MatchingSubscriber>> cachedExpressions;

    Cache() {
        cachedExpressions = new ConcurrentHashMap<>();
    }

    void invalidate() {
        cachedExpressions.clear();
    }

    boolean containsChannel(Channel channel) {
        return cachedExpressions.containsKey(channel);
    }

    void addChannel(Channel channel, List<MatchingSubscriber> matchingSubscribers) {
        matchingSubscribers.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
        cachedExpressions.put(channel, matchingSubscribers);
    }

    List<MatchingSubscriber> getSubscribersForExpression(Channel channel) {
        return cachedExpressions.get(channel);
    }

    Set<String> cachedChannels() {
        return cachedExpressions.keySet().stream().map(Channel::getOriginal).collect(Collectors.toSet());
    }
}
