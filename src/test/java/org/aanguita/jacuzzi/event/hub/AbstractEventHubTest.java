package org.aanguita.jacuzzi.event.hub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by Alberto on 23/09/2016.
 */
public class AbstractEventHubTest {

    EventHub eventHub;

    EventHubSubscriber mockedSubscriberAll;

    EventHubSubscriber mockedSubscriberSome;

    EventHubSubscriber mockedSubscriberOne;

    @Before
    public void setUp() throws Exception {
        eventHub = EventHubFactory.createEventHub("test", EventHubFactory.Type.ASYNCHRONOUS_PERMANENT_THREAD);
        mockedSubscriberAll = mock(EventHubSubscriber.class);
        mockedSubscriberSome = mock(EventHubSubscriber.class);
        mockedSubscriberOne = mock(EventHubSubscriber.class);
        eventHub.subscribe("all", mockedSubscriberAll, "*");
        eventHub.subscribe("some", mockedSubscriberSome, "test/?");
        eventHub.subscribe("one", mockedSubscriberOne, "test/one");
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void test() {

        String event1 = "hello";
        String event2 = "test/two";
        String event3 = "test/one";
        Integer i = 5;
        Boolean b = true;

        eventHub.publish(event1);
        eventHub.publish(event2, i);
        eventHub.publish(event3, i, b);

        verify(mockedSubscriberAll).event(event1);
        verify(mockedSubscriberAll).event(event2, i);
        verify(mockedSubscriberAll).event(event3, i, b);
        verify(mockedSubscriberSome, never()).event(event1);
        verify(mockedSubscriberSome).event(event2, i);
        verify(mockedSubscriberSome).event(event3, i, b);
        verify(mockedSubscriberOne, never()).event(event1);
        verify(mockedSubscriberOne, never()).event(event2, i);
        verify(mockedSubscriberOne).event(event3, i, b);
    }
}