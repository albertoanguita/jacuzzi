package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
class SynchronousEventHub extends AbstractEventHub {

    SynchronousEventHub(String name) {
        super(name);
    }

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.SYNCHRONOUS;
    }

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        invokeSubscribers(subscribers, false, channel, messages);
    }

    @Override
    protected boolean invokeSubscriber(Duple<EventHubSubscriber, Boolean> eventHubSubscriber, boolean haveThreadAvailable, String channel, Object... messages) {
        eventHubSubscriber.element1.event(channel, messages);
        return false;
    }
}
