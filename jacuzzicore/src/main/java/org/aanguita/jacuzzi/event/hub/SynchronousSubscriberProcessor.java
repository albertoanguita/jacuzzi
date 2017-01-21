package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

/**
 * Created by Alberto on 21/01/2017.
 */
public class SynchronousSubscriberProcessor implements SubscriberProcessor {

    private final EventHubSubscriber eventHubSubscriber;

    SynchronousSubscriberProcessor(String subscriberId, EventHubSubscriber eventHubSubscriber) {
        this.eventHubSubscriber = eventHubSubscriber;
    }

    @Override
    public void publish(Publication publication) {
        eventHubSubscriber.event(publication);
    }

    @Override
    public void close() {
        // no actions required
    }
}
