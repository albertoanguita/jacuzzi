package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;
import org.aanguita.jacuzzi.queues.OnDemandQueueProcessor;

import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
class AsynchronousEventualThreadEventHub extends QueuedEventHub {

    private final OnDemandQueueProcessor<Publication> onDemandQueueProcessor;

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.ASYNCHRONOUS_EVENTUAL_THREAD;
    }

    AsynchronousEventualThreadEventHub(String name) {
        super(name);
        onDemandQueueProcessor = new OnDemandQueueProcessor<>(publication -> invokeSubscribers(publication.receivers, false, publication.channel, publication.messages));
    }

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        onDemandQueueProcessor.addEvent(new Publication(channel, messages, subscribers));
    }
}
