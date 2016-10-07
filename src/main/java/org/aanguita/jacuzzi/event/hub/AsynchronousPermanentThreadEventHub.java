package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.queues.event_processing.MessageHandler;
import org.aanguita.jacuzzi.queues.event_processing.MessageProcessor;

import java.util.List;

/**
 * Created by Alberto on 08/10/2016.
 */
public class AsynchronousPermanentThreadEventHub extends QueuedEventHub {

    // todo add type
    private final MessageProcessor publicationMessageProcessor;

    AsynchronousPermanentThreadEventHub(String name) {
        super(name);
        publicationMessageProcessor = new MessageProcessor(name + "/MessageProcessor", new MessageHandler() {
            @Override
            public void handleMessage(Object message) {
                Publication publication = (Publication) message;
                invokeSubscribers(publication.receivers, false, publication.channel, publication.messages);
            }

            @Override
            public void finalizeHandler() {
                // nothing to do here
            }
        });
    }

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        try {
            publicationMessageProcessor.addMessage(new Publication(channel, messages, subscribers));
        } catch (InterruptedException e) {
            // ignore, cannot happen
        }
    }
}
