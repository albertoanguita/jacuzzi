package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.After;
import org.junit.Assert;
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
        public void event(Publication publication) {
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
        eventHub.registerSubscriber("all", mockedSubscriberAll, EventHubFactory.SubscriberProcessorType.ONE_THREAD_PER_PUBLICATION);
        eventHub.registerSubscriber("some", mockedSubscriberSome, EventHubFactory.SubscriberProcessorType.ONE_THREAD_PER_PUBLICATION);
        eventHub.registerSubscriber("one", mockedSubscriberOne, EventHubFactory.SubscriberProcessorType.ONE_THREAD_PER_PUBLICATION);
        eventHub.subscribe("all", "*");
        eventHub.subscribe("some", "test/?");
        eventHub.subscribe("one", "test/one");

        String event1 = "hello";
        String event2 = "test/two";
        String event3 = "test/one";
        Integer i = 5;
        Boolean b = true;

        Assert.assertEquals(Collections.emptySet(), eventHub.cachedChannels());
        eventHub.publish(event1);
        Assert.assertEquals(new HashSet<>(Collections.singletonList(event1)), eventHub.cachedChannels());
        eventHub.publish(event2, i);
        Assert.assertEquals(new HashSet<>(Arrays.asList(event1, event2)), eventHub.cachedChannels());
        eventHub.publish(event3, i, b);
        Assert.assertEquals(new HashSet<>(Arrays.asList(event1, event2, event3)), eventHub.cachedChannels());

        assertEquals(3, mockedSubscriberAll.publications.size());
        assertEquals(2, mockedSubscriberSome.publications.size());
        assertEquals(1, mockedSubscriberOne.publications.size());
        verifyMatches(mockedSubscriberAll.publications.get(0), "test", "hello");
        verifyMatches(mockedSubscriberAll.publications.get(1), "test", "test/two", 5);
        verifyMatches(mockedSubscriberAll.publications.get(2), "test", "test/one", 5, true);
        verifyMatches(mockedSubscriberSome.publications.get(0), "test", "test/two", 5);
        verifyMatches(mockedSubscriberSome.publications.get(1), "test", "test/one", 5, true);
        verifyMatches(mockedSubscriberOne.publications.get(0), "test", "test/one", 5, true);
    }

    private void verifyMatches(Publication publication, String hubName, String channel, Object... messages) {
        assertEquals(hubName, publication.getEventHubName());
        assertEquals(channel, publication.getChannel().getOriginal());
        assertTrue(System.currentTimeMillis() - publication.getTimestamp() < 500L);
        assertArrayEquals(messages, publication.getMessages());
    }
}