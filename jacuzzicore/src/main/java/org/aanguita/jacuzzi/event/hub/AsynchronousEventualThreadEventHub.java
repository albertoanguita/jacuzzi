package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.queues.OnDemandQueueProcessor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Alberto on 07/10/2016.
 */
class AsynchronousEventualThreadEventHub extends QueuedEventHub {

    private final OnDemandQueueProcessor<QueuedPublication> onDemandQueueProcessor;

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD;
    }

    AsynchronousEventualThreadEventHub(String name) {
        super(name);
        onDemandQueueProcessor = new OnDemandQueueProcessor<>(queuedPublication -> invokeSubscribers(queuedPublication.matchingSubscribers, queuedPublication.publication));
    }

    AsynchronousEventualThreadEventHub(String name, Consumer<Exception> exceptionConsumer) {
        super(name, exceptionConsumer);
        onDemandQueueProcessor = new OnDemandQueueProcessor<>(queuedPublication -> invokeSubscribers(queuedPublication.matchingSubscribers, queuedPublication.publication));
    }

    @Override
    protected void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication) {
        onDemandQueueProcessor.addEvent(new QueuedPublication(publication, matchingSubscribers));
    }

    @Override
    public void close() {
        super.close();
        onDemandQueueProcessor.stop();
    }
}
