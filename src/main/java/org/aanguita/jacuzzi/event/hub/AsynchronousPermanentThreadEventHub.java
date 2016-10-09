package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.queues.event_processing.MessageHandler;
import org.aanguita.jacuzzi.queues.event_processing.MessageProcessor;

import java.util.List;

/**
 * Created by Alberto on 08/10/2016.
 */
class AsynchronousPermanentThreadEventHub extends QueuedEventHub {

    private final MessageProcessor<Publication> publicationMessageProcessor;

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.ASYNCHRONOUS_PERMANENT_THREAD;
    }

    AsynchronousPermanentThreadEventHub(String name) {
        super(name);
        publicationMessageProcessor = new MessageProcessor<>(name + "/MessageProcessor", new MessageHandler<Publication>() {
            @Override
            public void handleMessage(Publication publication) {
                invokeSubscribers(publication.receivers, false, publication.channel, publication.messages);
            }

            @Override
            public void close() {
                // nothing to do here
            }
        });
        publicationMessageProcessor.start();
    }

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        try {
            publicationMessageProcessor.addMessage(new Publication(channel, messages, subscribers));
        } catch (InterruptedException e) {
            // ignore, cannot happen
        }
    }

    @Override
    public void close() {
        publicationMessageProcessor.stop();
    }
}
