package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.daemon.DaemonQueue;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
class AsynchronousShortLivedThreadEventHub extends QueuedEventHub {

    private final DaemonQueue<Publication> publicationDaemonQueue;

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.ASYNCHRONOUS_SHORT_LIVED_THREAD;
    }

    AsynchronousShortLivedThreadEventHub(String name) {
        super(name);
        publicationDaemonQueue = new DaemonQueue<>(publication -> invokeSubscribers(publication.receivers, false, publication.channel, publication.messages));
    }

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        publicationDaemonQueue.addEvent(new Publication(channel, messages, subscribers));
    }
}
