package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * todo test all types of events
 */
public class AbstractEventHubTest {

    private static class SubscriberMock implements EventHubSubscriber {

        private final List<Publication> publications;

        public SubscriberMock() {
            publications = new ArrayList<>();
        }

        @Override
        public synchronized void event(Publication publication) {
            publications.add(publication);
        }
    }

    EventHub eventHub;

    SubscriberMock mockedSubscriberAll;

    SubscriberMock mockedSubscriberSome;

    SubscriberMock mockedSubscriberOne;

    @Before
    public void setUp() throws Exception {
        eventHub = EventHubFactory.createEventHub("test", EventHubFactory.Type.SYNCHRONOUS);
        mockedSubscriberAll = new SubscriberMock();
        mockedSubscriberSome = new SubscriberMock();
        mockedSubscriberOne = new SubscriberMock();
    }

    @After
    public void tearDown() throws Exception {
        eventHub.close();
    }

    @Test
    public void testNoSubscribers() {
        eventHub.publish("hello");
        eventHub.publish("dear/hi", 5);
        eventHub.publish(5000L, "dear/keep", 5);
        List<Publication> publicationList = eventHub.getStoredPublications("*/keep");
        assertEquals(1, publicationList.size());
        verifyMatches(publicationList.get(0), "test", "dear/keep", 5);
        ThreadUtil.safeSleep(6000L);
        assertTrue(eventHub.getStoredPublications("*/keep").isEmpty());
    }

    @Test
    public void testThreeSubscribers() {
        eventHub.registerSubscriber("all", mockedSubscriberAll, EventHubFactory.Type.ASYNCHRONOUS);
        eventHub.registerSubscriber("some", mockedSubscriberSome, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD);
        eventHub.registerSubscriber("one", mockedSubscriberOne, EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD);
        eventHub.subscribe("all", "*");
        eventHub.subscribe("some", "test/?");
        eventHub.subscribe("one", "test/one");

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
        verifyMatches(mockedSubscriberAll.publications.get(0), "test", "hello", time1);
        verifyMatches(mockedSubscriberAll.publications.get(1), "test", "test/two", time2, 5);
        verifyMatches(mockedSubscriberAll.publications.get(2), "test", "test/one", time3, 5, true);
        verifyMatches(mockedSubscriberSome.publications.get(0), "test", "test/two", time1, 5);
        verifyMatches(mockedSubscriberSome.publications.get(1), "test", "test/one", time2, 5, true);
        verifyMatches(mockedSubscriberOne.publications.get(0), "test", "test/one", time1, 5, true);
    }

    private void verifyMatches(Publication publication, String hubName, String channel, long time, Object... messages) {
        assertEquals(hubName, publication.getEventHubName());
        assertEquals(channel, publication.getChannel().getOriginal());
        assertTrue(time - publication.getTimestamp() < 100L);
        assertArrayEquals(messages, publication.getMessages());
    }
}