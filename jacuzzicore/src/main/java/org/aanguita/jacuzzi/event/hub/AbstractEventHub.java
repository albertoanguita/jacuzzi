package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.queues.ConsumerQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


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

    private static class PublicationRequest {

        private final Long keepMillis;

        private final Channel parsedChannel;

        private final long timestamp;

        private final Object[] messages;

        private PublicationRequest(Long keepMillis, Channel parsedChannel, long timestamp, Object[] messages) {
            this.keepMillis = keepMillis;
            this.parsedChannel = parsedChannel;
            this.timestamp = timestamp;
            this.messages = messages;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractEventHub.class);

    private final String name;

    private final Map<String, SubscriberData> subscribers;

    private final Cache channelCache;

    private final PublicationRepository publicationRepository;

    private final ConsumerQueue<PublicationRequest> queuedPublications;

    private final String threadExecutorClientId;

    private final AtomicBoolean alive;

    private boolean running;

    AbstractEventHub(String name) {
        this.name = name;
        subscribers = new HashMap<>();
        channelCache = new Cache();
        publicationRepository = new PublicationRepository();
        queuedPublications = new ConsumerQueue<>(publicationRequest -> publish(publicationRequest.keepMillis, publicationRequest.parsedChannel, publicationRequest.timestamp, publicationRequest.messages));
        threadExecutorClientId = ThreadExecutor.registerClient(name + ".EventHub");
        alive = new AtomicBoolean(true);
        running = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void start() {
        resume();
    }

    @Override
    public synchronized void pause() {
        running = false;
    }

    @Override
    public synchronized void resume() {
        running = true;
        queuedPublications.flush(false);
    }

    @Override
    public synchronized void publish(String channel, Object... messages) {
        publish(0L, channel, messages);
    }

    @Override
    public synchronized void publish(Long keepMillis, String channel, Object... messages) {
        if (alive.get()) {
            long timestamp = System.currentTimeMillis();
            Channel parsedChannel = new Channel(channel);
            if (running) {
                publish(keepMillis, parsedChannel, timestamp, messages);
            } else {
                queuedPublications.add(new PublicationRequest(keepMillis, parsedChannel, timestamp, messages));
            }
        }
    }

    private void publish(Long keepMillis, Channel parsedChannel, long timestamp, Object... messages) {
        Publication publication = new Publication(getName(), parsedChannel, timestamp, messages);
        List<MatchingSubscriber> matchingSubscribers = findSubscribers(parsedChannel);
        logger.trace("Found matching subscribers for publication: {}", matchingSubscribers);
        publish(matchingSubscribers, publication);
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
    public synchronized void registerSubscriber(String subscriberId, EventHubSubscriber subscriber, EventHubFactory.Type type) {
        if (alive.get()) {
            if (subscriberId == null) {
                subscriberId = assignSubscriberId(ThreadUtil.invokerName(1));
            }
            if (subscribers.containsKey(subscriberId)) {
                throw new IllegalArgumentException("Subscriber id already registered: " + subscriberId);
            } else {
                subscribers.put(subscriberId, new SubscriberData(subscriberId, subscriber, SubscriberProcessorFactory.createSubscriberProcessor(type, subscriberId, subscriber)));
            }
        }
    }

    @Override
    public synchronized void subscribe(EventHubSubscriber subscriber, EventHubFactory.Type type, String... channelExpressions) {
        if (alive.get()) {
            String subscriberId = assignSubscriberId(ThreadUtil.invokerName(1));
            registerSubscriber(subscriberId, subscriber, type);
            subscribe(subscriberId, channelExpressions);
        }
    }

    @Override
    public synchronized void subscribe(String subscriberId, String... channelExpressions) {
        if (alive.get()) {
            subscribe(subscriberId, 0, channelExpressions);
        }
    }

    @Override
    public synchronized void subscribe(String subscriberId, int priority, String... channelExpressions) {
        if (alive.get()) {
            if (!subscribers.containsKey(subscriberId)) {
                throw new IllegalArgumentException("Attempting to subscribe an unregistered subscriber: " + subscriberId);
            } else {
                subscribers.get(subscriberId).subscribe(priority, channelExpressions);
            }
            channelCache.invalidate();
            ThreadExecutor.submit(() -> publicationRepository.getStoredPublications(channelExpressions).forEach(p -> subscribers.get(subscriberId).getSubscriber().event(p)));
        }
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
        if (alive.get()) {
            if (subscribers.containsKey(subscriberId)) {
                subscribers.get(subscriberId).unsubscribe(channelExpressions);
            }
            channelCache.invalidate();
        }
    }

    @Override
    public synchronized void unsubscribeAll(String subscriberId) {
        if (alive.get()) {
            if (subscribers.containsKey(subscriberId)) {
                subscribers.get(subscriberId).unsubscribeAll();
            }
            channelCache.invalidate();
        }
    }

    @Override
    public synchronized void unregisterSubscriber(String subscriberId) {
        if (alive.get()) {
            if (subscribers.containsKey(subscriberId)) {
                subscribers.remove(subscriberId);
            }
            channelCache.invalidate();
        }
    }

    @Override
    public synchronized List<Publication> getStoredPublications(String... channelExpressions) {
        if (alive.get()) {
            return publicationRepository.getStoredPublications(channelExpressions);
        } else {
            return new ArrayList<>();
        }
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
        if (alive.getAndSet(false)) {
            channelCache.invalidate();
            publicationRepository.clear();
            subscribers.values().forEach(SubscriberData::close);
            subscribers.clear();
            ThreadExecutor.unregisterClient(threadExecutorClientId);
            EventHubFactory.removeEventHub(getName());
        }
    }
}
