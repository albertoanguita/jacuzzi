package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.id.AlphaNumFactory;

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
        publish(0L, channel, messages);
    }

    @Override
    public void publish(Long keepMillis, String channel, Object... messages) {
        long timestamp = System.currentTimeMillis();
        Channel parsedChannel = new Channel(channel);
        Publication publication = new Publication(getName(), parsedChannel, timestamp, messages);
        List<MatchingSubscriber> subscribersAndBackground = findSubscribers(parsedChannel);
        publish(subscribersAndBackground, publication);
        publicationRepository.storePublication(publication, keepMillis);
    }

    private synchronized List<MatchingSubscriber> findSubscribers(Channel channel) {
        if (channelCache.containsChannel(channel)) {
            // use the cache
            return channelCache.getSubscribersForExpression(channel);
        } else {
            // calculate the matching subscribers, and also add them to the cache for this channel
            List<MatchingSubscriber> matchingSubscribers = new ArrayList<>();
            for (SubscriberData subscriberData : subscribers.values()) {
                Integer publishingPriority = highestPriorityMatch(subscriberData.getChannels(), channel);
                if (publishingPriority != null) {
                    matchingSubscribers.add(new MatchingSubscriber(publishingPriority, subscriberData.getSubscriberProcessor()));
                }
            }
            channelCache.addChannel(channel, matchingSubscribers);
            // the cache has ordered the matching subscribers, so we use it
            return channelCache.getSubscribersForExpression(channel);
        }
    }

    private static Integer highestPriorityMatch(Set<SubscriberData.ChannelWithPriority> channels, Channel channel) {
        Optional<SubscriberData.ChannelWithPriority> optional = channels.stream().filter(aChannelWithPriority -> aChannelWithPriority.getChannel().matches(channel)).max((o1, o2) -> o2.getPriority() - o1.getPriority());
        return optional.isPresent() ? optional.get().getPriority() : null;
    }

    protected abstract void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication);

    protected void invokeSubscribers(List<MatchingSubscriber> matchingSubscribers, Publication publication) {
        for (MatchingSubscriber matchingSubscriber : matchingSubscribers) {
            matchingSubscriber.publish(publication);
        }
    }

    @Override
    public void registerSubscriber(String subscriberId, EventHubSubscriber subscriber, EventHubFactory.SubscriberProcessorType subscriberProcessorType) {
        if (subscriberId == null) {
            subscriberId = assignSubscriberId(ThreadUtil.invokerName(1));
        }
        if (subscribers.containsKey(subscriberId)) {
            throw new IllegalArgumentException("Subscriber id already registered: " + subscriberId);
        } else {
            subscribers.put(subscriberId, new SubscriberData(subscriberId, subscriber, SubscriberProcessorFactory.createSubscriberProcessor(subscriberProcessorType, subscriberId, subscriber)));
        }
    }

    @Override
    public synchronized void subscribe(EventHubSubscriber subscriber, EventHubFactory.SubscriberProcessorType subscriberProcessorType, String... channelExpressions) {
        String subscriberId = assignSubscriberId(ThreadUtil.invokerName(1));
        registerSubscriber(subscriberId, subscriber, subscriberProcessorType);
        subscribe(subscriberId, channelExpressions);
    }

    @Override
    public synchronized void subscribe(String subscriberId, String... channelExpressions) {
        subscribe(subscriberId, 0, channelExpressions);
    }

    @Override
    public synchronized void subscribe(String subscriberId, int priority, String... channelExpressions) {
        if (!subscribers.containsKey(subscriberId)) {
            throw new IllegalArgumentException("Attempting to subscribe an unregistered subscriber: " + subscriberId);
        } else {
            subscribers.get(subscriberId).subscribe(priority, channelExpressions);
        }
        channelCache.invalidate();
        ThreadExecutor.submit(() -> publicationRepository.getStoredPublications(channelExpressions).forEach(p -> subscribers.get(subscriberId).getSubscriber().event(p)));
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
        subscribers.values().forEach(SubscriberData::close);
        ThreadExecutor.shutdownClient(threadExecutorClientId);
        EventHubFactory.removeEventHub(getName());
    }
}
