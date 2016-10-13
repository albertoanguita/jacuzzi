package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.Collection;

/**
 * Created by Alberto on 07/10/2016.
 */
abstract class QueuedEventHub extends AbstractEventHub {

    protected static class Publication {

        protected final String channel;

        protected final Object[] messages;

        protected final Collection<Duple<EventHubSubscriber, Boolean>> receivers;

        protected Publication(String channel, Object[] messages, Collection<Duple<EventHubSubscriber, Boolean>> receivers) {
            this.channel = channel;
            this.messages = messages;
            this.receivers = receivers;
        }
    }

    QueuedEventHub(String name) {
        super(name);
    }
}
