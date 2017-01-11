package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.queues.processor.MessageHandler;
import org.aanguita.jacuzzi.queues.processor.MessageProcessor;

/**
 * Created by Alberto on 11/01/2017.
 */
public class MessageProcessorSubscriberProcessor implements SubscriberProcessor {

    private final MessageProcessor<Publication> processor;

    public MessageProcessorSubscriberProcessor(String subscriberId, EventHubSubscriber eventHubSubscriber) {
        processor = new MessageProcessor<>(subscriberId + ".SubscriberProcessor", new MessageHandler<Publication>() {
            @Override
            public void handleMessage(Publication publication) {
                eventHubSubscriber.event(publication);
            }

            @Override
            public void close() {
                // nothing to do here
            }
        });
        processor.start();
    }

    @Override
    public void publish(Publication publication) {
        try {
            processor.addMessage(publication);
        } catch (InterruptedException e) {
            // ignore, cannot happen
        }
    }

    @Override
    public void close() {
        processor.stop();
    }
}
