package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.queues.OnDemandQueueProcessor;

import java.util.function.Consumer;

/**
 * Created by Alberto on 11/01/2017.
 */
public class OnDemandSubscriberProcessor implements SubscriberProcessor {

    private final OnDemandQueueProcessor<Publication> processor;

    public OnDemandSubscriberProcessor(String threadName, EventHubSubscriber eventHubSubscriber, Consumer<Exception> exceptionConsumer) {
        processor = new OnDemandQueueProcessor<>(eventHubSubscriber::event, OnDemandQueueProcessor.DEFAULT_QUEUE_CAPACITY, 1, threadName, exceptionConsumer);
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
