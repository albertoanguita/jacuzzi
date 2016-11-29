package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
class SimpleEventHub extends AbstractEventHub {


    SimpleEventHub(String name) {
        super(name);
    }

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.SIMPLE;
    }

    @Override
    protected void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication, boolean inBackground) {
        if (inBackground) {
            ThreadExecutor.submit(() -> invokeSubscribers(matchingSubscribers, true, publication));
        } else {
            invokeSubscribers(matchingSubscribers, false, publication);
        }
    }
}
