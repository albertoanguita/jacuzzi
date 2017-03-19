package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

/**
 * Created by Alberto on 11/01/2017.
 */
public class OneThreadSubscriberProcessor implements SubscriberProcessor {

    private final EventHubSubscriber eventHubSubscriber;

    private final String threadExecutorClientId;

    OneThreadSubscriberProcessor(String subscriberId, EventHubSubscriber eventHubSubscriber) {
        this.eventHubSubscriber = eventHubSubscriber;
        threadExecutorClientId = ThreadExecutor.registerClient(subscriberId + ".EventHub");
    }

    @Override
    public void publish(Publication publication) {
        ThreadExecutor.submit(() -> eventHubSubscriber.event(publication));
    }

    @Override
    public void close() {
        ThreadExecutor.unregisterClient(threadExecutorClientId);
    }
}
