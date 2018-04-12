package org.aanguita.jacuzzi.event.hub;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Alberto on 07/10/2016.
 */
class SynchronousEventHub extends AbstractEventHub {

    SynchronousEventHub(String name) {
        super(name);
    }

    SynchronousEventHub(String name, Consumer<Exception> exceptionConsumer) {
        super(name, exceptionConsumer);
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
