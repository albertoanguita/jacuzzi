package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.queues.processor.MessageHandler;
import org.aanguita.jacuzzi.queues.processor.MessageProcessor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Alberto on 08/10/2016.
 */
class AsynchronousPermanentThreadEventHub extends QueuedEventHub {

    private final MessageProcessor<QueuedPublication> publicationMessageProcessor;

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.ASYNCHRONOUS_QUEUE_PERMANENT_THREAD;
    }

    AsynchronousPermanentThreadEventHub(String name) {
        this(name, null);
    }

    AsynchronousPermanentThreadEventHub(String name, Consumer<Exception> exceptionConsumer) {
        super(name, exceptionConsumer);
        publicationMessageProcessor = new MessageProcessor<>(name + ".MessageProcessor", new MessageHandler<QueuedPublication>() {
            @Override
            public void handleMessage(QueuedPublication queuedPublication) {
                invokeSubscribers(queuedPublication.matchingSubscribers, queuedPublication.publication);
            }

            @Override
            public void close() {
                // nothing to do here
            }
        });
        publicationMessageProcessor.start();
    }

    @Override
    protected void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication) {
        try {
            publicationMessageProcessor.addMessage(new QueuedPublication(publication, matchingSubscribers));
        } catch (InterruptedException e) {
            // ignore, cannot happen
        }
    }

    @Override
    public void close() {
        super.close();
        publicationMessageProcessor.stop();
    }
}
