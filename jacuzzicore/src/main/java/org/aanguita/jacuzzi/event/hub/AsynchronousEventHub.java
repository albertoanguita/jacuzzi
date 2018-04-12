package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Alberto on 21/01/2017.
 */
public class AsynchronousEventHub extends AbstractEventHub {

    AsynchronousEventHub(String name) {
        super(name);
    }

    AsynchronousEventHub(String name, Consumer<Exception> exceptionConsumer) {
        super(name, exceptionConsumer);
    }

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.SYNCHRONOUS;
    }

    @Override
    protected void publish(List<MatchingSubscriber> matchingSubscribers, Publication publication) {
        ThreadExecutor.submit(() -> invokeSubscribers(matchingSubscribers, publication));
    }
}
