package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

/**
 * todo test all types of events
 */
public class AbstractEventHubTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractEventHubTest.class);

    private static class SubscriberMock implements EventHubSubscriber {

        private static int order = 0;

        private final String name;

        private final List<Publication> publications;

        private final Map<Publication, Integer> timestamps;

        public SubscriberMock(String name) {
            this.name = name;
            publications = new ArrayList<>();
            timestamps = new HashMap<>();
        }

        public List<Publication> getPublications() {
            return publications;
        }

        public Map<Publication, Integer> getTimestamps() {
            return timestamps;
        }

        public void clearPublications() {
            publications.clear();
            timestamps.clear();
        }

        @Override
        public String getId() {
            return name;
        }

        @Override
        public synchronized void event(Publication publication) {
            int order = getOrder();
            logger.debug(this + ": new publication: " + publication + " (order " + order + ")");
            publications.add(publication);
            timestamps.put(publication, order);
        }

        private static synchronized int getOrder() {
            int result = order;
            order++;
            return result;
        }

        @Override
        public String toString() {
            return "SubscriberMock{" +
                    "name='" + name + '\'' +
                    ", publications=" + publications +
                    ", timestamps=" + timestamps +
                    '}';
        }
    }

    EventHub eventHub;

    SubscriberMock mockedSubscriberAll;

    SubscriberMock mockedSubscriberSome;

    SubscriberMock mockedSubscriberOne;

    @Before
    public void setUp() throws Exception {
        eventHub = EventHubFactory.createEventHub("test", EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD);
        mockedSubscriberAll = new SubscriberMock("all");
        mockedSubscriberSome = new SubscriberMock("some");
        mockedSubscriberOne = new SubscriberMock("one");
        eventHub.start();
    }

    @After
    public void tearDown() throws Exception {
        eventHub.close();
    }

    @Test
    public void test() {
        testPriorities(eventHub);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testNoInitialization() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Requesting a non-existing object: not-initialized");
        EventHubFactory.getEventHub("not-initialized");
    }

    private void testPriorities(EventHub eventHub) {
        SubscriberMock subscriberMock1 = new SubscriberMock("1");
        SubscriberMock subscriberMock2 = new SubscriberMock("2");
        SubscriberMock subscriberMock3 = new SubscriberMock("3");
        eventHub.registerSubscriber(subscriberMock1, EventHubFactory.Type.SYNCHRONOUS);
        eventHub.registerSubscriber(subscriberMock2, EventHubFactory.Type.SYNCHRONOUS);
        eventHub.registerSubscriber(subscriberMock3, EventHubFactory.Type.SYNCHRONOUS);
        eventHub.subscribe(subscriberMock1, 20, "test");
        eventHub.subscribe(subscriberMock2, 10, "?");
        eventHub.subscribe(subscriberMock3, 0, "*");
        eventHub.publish("test", "message");
        ThreadUtil.safeSleep(100);

        assertTrue(subscriberMock1.getTimestamps().get(subscriberMock1.getPublications().get(0)) < subscriberMock2.getTimestamps().get(subscriberMock1.getPublications().get(0)));
        assertTrue(subscriberMock2.getTimestamps().get(subscriberMock1.getPublications().get(0)) < subscriberMock3.getTimestamps().get(subscriberMock1.getPublications().get(0)));
        eventHub.unsubscribeAll(subscriberMock1);
        eventHub.unsubscribeAll(subscriberMock2);
        eventHub.unsubscribeAll(subscriberMock3);
        subscriberMock1.clearPublications();
        subscriberMock2.clearPublications();
        subscriberMock3.clearPublications();
        eventHub.subscribe(subscriberMock1, 0, "test");
        eventHub.subscribe(subscriberMock2, 10, "?");
        eventHub.subscribe(subscriberMock3, 20, "*");
        eventHub.publish("test", "message");
        ThreadUtil.safeSleep(100);
        assertTrue(subscriberMock1.getTimestamps().get(subscriberMock1.getPublications().get(0)) > subscriberMock2.getTimestamps().get(subscriberMock1.getPublications().get(0)));
        assertTrue(subscriberMock2.getTimestamps().get(subscriberMock1.getPublications().get(0)) > subscriberMock3.getTimestamps().get(subscriberMock1.getPublications().get(0)));
    }

    @Test
    public void testNoSubscribers() {
        eventHub.publish("hello");
        eventHub.publish("dear/hi", 5);
        eventHub.publish(5000L, "dear/keep", 5);
        List<Publication> publicationList = eventHub.getStoredPublications("*/keep");
        assertEquals(1, publicationList.size());
        verifyMatches(publicationList.get(0), "test", "dear/keep", System.currentTimeMillis(), 5);
        ThreadUtil.safeSleep(6000L);
        assertTrue(eventHub.getStoredPublications("*/keep").isEmpty());
    }

    @Test
    public void testCount() {
        eventHub.registerSubscriber(mockedSubscriberAll, EventHubFactory.Type.ASYNCHRONOUS);
        eventHub.registerSubscriber(mockedSubscriberSome, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.registerSubscriber(mockedSubscriberOne, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD);
        eventHub.subscribe(mockedSubscriberAll, "*");
        eventHub.subscribe(mockedSubscriberSome, "test/*");
        eventHub.subscribe(mockedSubscriberOne, "test/one");

        assertEquals(1, eventHub.getSubscribersCount("notest"));
        assertEquals(2, eventHub.getSubscribersCount("test/two"));
        assertEquals(3, eventHub.getSubscribersCount("test/one"));

        eventHub.unsubscribeAll(mockedSubscriberOne);
        assertEquals(2, eventHub.getSubscribersCount("test/one"));
    }

    @Test
    public void testThreeSubscribers() {
        eventHub.registerSubscriber(mockedSubscriberAll, EventHubFactory.Type.ASYNCHRONOUS);
        eventHub.registerSubscriber(mockedSubscriberSome, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.registerSubscriber(mockedSubscriberOne, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD);
        eventHub.subscribe(mockedSubscriberAll, "*");
        eventHub.subscribe(mockedSubscriberSome, "test/?");
        eventHub.subscribe(mockedSubscriberOne, "test/one");

        String event1 = "hello";
        String event2 = "test/two";
        String event3 = "test/one";
        Integer i = 5;
        Boolean b = true;

        assertEquals(Collections.emptySet(), eventHub.cachedChannels());
        eventHub.publish(event1);
        long time1 = System.currentTimeMillis();
        ThreadUtil.safeSleep(100);
        assertEquals(new HashSet<>(Collections.singletonList(event1)), eventHub.cachedChannels());
        eventHub.publish(event2, i);
        long time2 = System.currentTimeMillis();
        ThreadUtil.safeSleep(100);
        assertEquals(new HashSet<>(Arrays.asList(event1, event2)), eventHub.cachedChannels());
        eventHub.publish(event3, i, b);
        long time3 = System.currentTimeMillis();
        ThreadUtil.safeSleep(100);
        assertEquals(new HashSet<>(Arrays.asList(event1, event2, event3)), eventHub.cachedChannels());


        ThreadUtil.safeSleep(500);

        assertEquals(3, mockedSubscriberAll.publications.size());
        assertEquals(2, mockedSubscriberSome.publications.size());
        assertEquals(1, mockedSubscriberOne.publications.size());
        verifyMatches(mockedSubscriberAll.getPublications().get(0), "test", "hello", time1);
        verifyMatches(mockedSubscriberAll.getPublications().get(1), "test", "test/two", time2, 5);
        verifyMatches(mockedSubscriberAll.getPublications().get(2), "test", "test/one", time3, 5, true);
        verifyMatches(mockedSubscriberSome.getPublications().get(0), "test", "test/two", time1, 5);
        verifyMatches(mockedSubscriberSome.getPublications().get(1), "test", "test/one", time2, 5, true);
        verifyMatches(mockedSubscriberOne.getPublications().get(0), "test", "test/one", time1, 5, true);
    }

    @Test
    public void testPause() {
        eventHub.registerSubscriber(mockedSubscriberAll, EventHubFactory.Type.ASYNCHRONOUS);
        eventHub.registerSubscriber(mockedSubscriberSome, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.registerSubscriber(mockedSubscriberOne, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD);
        eventHub.subscribe(mockedSubscriberAll, "*");
        eventHub.subscribe(mockedSubscriberSome, "test/?");
        eventHub.subscribe(mockedSubscriberOne, "test/one");

        String event1 = "hello";
        String event2 = "test/two";
        String event3 = "test/one";
        Integer i = 5;
        Boolean b = true;

        eventHub.pause();

        eventHub.publish(event1);
        eventHub.publish(event2, i);
        eventHub.publish(event3, i, b);

        ThreadUtil.safeSleep(500);

        assertEquals(0, mockedSubscriberAll.publications.size());
        assertEquals(0, mockedSubscriberSome.publications.size());
        assertEquals(0, mockedSubscriberOne.publications.size());

        eventHub.resume();
        ThreadUtil.safeSleep(500);

        assertEquals(3, mockedSubscriberAll.publications.size());
        assertEquals(2, mockedSubscriberSome.publications.size());
        assertEquals(1, mockedSubscriberOne.publications.size());

        eventHub.pause("hello");
        eventHub.publish(event1);
        eventHub.publish(event2, i);
        eventHub.publish(event3, i, b);

        ThreadUtil.safeSleep(500);

        assertEquals(5, mockedSubscriberAll.publications.size());
        assertEquals(4, mockedSubscriberSome.publications.size());
        assertEquals(2, mockedSubscriberOne.publications.size());

        eventHub.resume("hello");
        ThreadUtil.safeSleep(500);

        assertEquals(6, mockedSubscriberAll.publications.size());
        assertEquals(4, mockedSubscriberSome.publications.size());
        assertEquals(2, mockedSubscriberOne.publications.size());
    }

    private void verifyMatches(Publication publication, String hubName, String channel, long time, Object... messages) {
        assertEquals(hubName, publication.getEventHubName());
        assertEquals(channel, publication.getChannel().getOriginal());
        assertTrue(time - publication.getTimestamp() < 100L);
        assertArrayEquals(messages, publication.getMessages());
    }
}