package org.aanguita.jacuzzi.event.hub;

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
    protected void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication) {
        invokeSubscribers(matchingSubscribers, publication);
    }
}
