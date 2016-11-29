package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.*;


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

    private final String name;

    private final Map<String, SubscriberData> subscribers;

    private final Cache channelCache;

    private final PublicationRepository publicationRepository;

    private final String threadExecutorClientId;

    AbstractEventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
        channelCache = new Cache();
        publicationRepository = new PublicationRepository();
        threadExecutorClientId = ThreadExecutor.registerClient(name + ".EventHub");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void publish(String channel, Object... messages) {
        publish(channel, 0L, false, messages);
    }

    @Override
    public void publish(String channel, Long keepMillis, boolean inBackground, Object... messages) {
        long timestamp = System.currentTimeMillis();
        Channel parsedChannel = new Channel(channel);
        Publication publication = new Publication(getName(), parsedChannel, timestamp, messages);
        List<MatchingSubscriber> subscribersAndBackground = findSubscribers(parsedChannel);
        publish(subscribersAndBackground, publication, inBackground);
        publicationRepository.storePublication(publication, keepMillis);
    }

    private synchronized List<MatchingSubscriber> findSubscribers(Channel channel) {
        List<MatchingSubscriber> subscribersAndBackground = new ArrayList<>();
        if (channelCache.containsChannel(channel)) {
            // use the cache
            return channelCache.getSubscribersForExpression(channel);
        } else {
            // set the cache for this channel
            List<MatchingSubscriber> unorderedMatchingSubscribers = new ArrayList<>();
            for (SubscriberData subscriberData : subscribers.values()) {
                Duple<Integer, Boolean> expressionsMatchChannel = subscriberMatchesChannel(subscriberData, channel);
                if (expressionsMatchChannel != null) {
                    subscribersAndBackground.add(new MatchingSubscriber(subscriberData.getSubscriber(), expressionsMatchChannel.element1, expressionsMatchChannel.element2));
                    unorderedMatchingSubscribers.add(new MatchingSubscriber(subscriberData.getSubscriber(), expressionsMatchChannel.element1, expressionsMatchChannel.element2));
                }
            }
            channelCache.addChannel(channel, unorderedMatchingSubscribers);
        }
        return subscribersAndBackground;
    }

    protected abstract void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication, boolean inBackground);

    protected void invokeSubscribers(List<MatchingSubscriber> matchingSubscribers, boolean haveThreadAvailable, Publication publication) {
        for (int i = 0; i < matchingSubscribers.size() - 1; i++) {
            invokeSubscriber(matchingSubscribers.get(i), false, publication);
        }
        invokeSubscriber(matchingSubscribers.get(matchingSubscribers.size() - 1), haveThreadAvailable, publication);
    }

    private void invokeSubscriber(MatchingSubscriber matchingSubscriber, boolean haveThreadAvailable, Publication publication) {
        if (matchingSubscriber.isInBackground()) {
            // this subscriber wants a thread for his own
            if (haveThreadAvailable) {
                // use the available thread
                matchingSubscriber.getEventHubSubscriber().event(publication);
            } else {
                // create a new thread only for him
                ThreadExecutor.submit(() -> matchingSubscriber.getEventHubSubscriber().event(publication));
            }
        } else {
            // do not spawn a new thread
            matchingSubscriber.getEventHubSubscriber().event(publication);
        }
    }

    private static Duple<Integer, Boolean> subscriberMatchesChannel(SubscriberData subscriber, Channel channel) {
        Integer channelMatchPriority = expressionWithHighestPriorityMatch(subscriber.getAsynchronousChannels(), channel);
        if (channelMatchPriority != null) {
            return new Duple<>(channelMatchPriority, true);
        } else {
            channelMatchPriority = expressionWithHighestPriorityMatch(subscriber.getSynchronousChannels(), channel);
            if (channelMatchPriority != null) {
                return new Duple<>(channelMatchPriority, false);
            } else {
                return null;
            }
        }
    }

    private static Integer expressionWithHighestPriorityMatch(Set<Duple<Channel, Integer>> channels, Channel channel) {
        Optional<Duple<Channel, Integer>> optional = channels.stream().filter(aChannelWithPriority -> aChannelWithPriority.element1.matches(channel)).max((o1, o2) -> o1.element2.compareTo(o2.element2));
        return optional.isPresent() ? optional.get().element2 : null;
    }

    @Override
    public synchronized void subscribe(EventHubSubscriber subscriber, String... channelExpressions) {
        subscribe(null, subscriber, 0, false, channelExpressions);
    }

    @Override
    public synchronized void subscribe(String subscriberId, EventHubSubscriber subscriber, String... channelExpressions) {
        subscribe(subscriberId, subscriber, 0, false, channelExpressions);
    }

    @Override
    public synchronized void subscribe(String subscriberId, EventHubSubscriber subscriber, int priority, boolean inBackground, String... channelExpressions) {
        if (subscriberId == null) {
            subscriberId = assignSubscriberId(ThreadUtil.invokerName(1));
        }
        if (!subscribers.containsKey(subscriberId)) {
            subscribers.put(subscriberId, new SubscriberData(subscriberId, subscriber, priority, inBackground, channelExpressions));
        } else {
            subscribers.get(subscriberId).subscribe(priority, inBackground, channelExpressions);
        }
        channelCache.invalidate();
        ThreadExecutor.submit(() -> publicationRepository.getStoredPublications(channelExpressions).forEach(subscriber::event));
    }

    private String assignSubscriberId(String idProposal) {
        if (!subscribers.containsKey(idProposal)) {
            return idProposal;
        } else {
            return idProposal + "-" + AlphaNumFactory.getStaticId();
        }
    }

    @Override
    public synchronized void unsubscribe(String subscriberId, String... channelExpressions) {
        if (subscribers.containsKey(subscriberId)) {
            subscribers.get(subscriberId).unsubscribe(channelExpressions);
        }
        channelCache.invalidate();
    }

    @Override
    public List<Publication> getStoredPublications(String... channelExpressions) {
        return publicationRepository.getStoredPublications(channelExpressions);
    }

    @Override
    public Set<String> cachedChannels() {
        return channelCache.cachedChannels();
    }

    /**
     * Override if some resources need to be closed/cleaned up
     */
    @Override
    public void close() {
        ThreadExecutor.shutdownClient(threadExecutorClientId);
    }
}
