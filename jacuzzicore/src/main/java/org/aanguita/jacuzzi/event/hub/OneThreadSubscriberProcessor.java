package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;

import java.util.function.Consumer;

/**
 * Created by Alberto on 11/01/2017.
 */
public class OneThreadSubscriberProcessor implements SubscriberProcessor {

    private final String threadName;

    private final EventHubSubscriber eventHubSubscriber;

    private final Consumer<Exception> exceptionConsumer;

    private final String threadExecutorClientId;

    OneThreadSubscriberProcessor(String threadName, EventHubSubscriber eventHubSubscriber, Consumer<Exception> exceptionConsumer) {
        this.threadName = threadName;
        this.eventHubSubscriber = eventHubSubscriber;
        this.exceptionConsumer = exceptionConsumer;
        threadExecutorClientId = ThreadExecutor.registerClient(threadName + ".EventHub");
    }

    @Override
    public void publish(Publication publication) {
        ThreadExecutor.submit(() -> eventHubSubscriber.event(publication), threadName, exceptionConsumer);
    }

    @Override
    public void close() {
        ThreadExecutor.unregisterClient(threadExecutorClientId);
    }
}
