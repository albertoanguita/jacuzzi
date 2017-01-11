package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.queues.OnDemandQueueProcessor;

/**
 * Created by Alberto on 11/01/2017.
 */
public class OnDemandSubscriberProcessor implements SubscriberProcessor {

    private final OnDemandQueueProcessor<Publication> processor;

    public OnDemandSubscriberProcessor(EventHubSubscriber eventHubSubscriber) {
        processor = new OnDemandQueueProcessor<>(eventHubSubscriber::event);
    }

    @Override
    public void publish(Publication publication) {
        processor.addEvent(publication);
    }

    @Override
    public void close() {
        processor.stop();
    }
}
