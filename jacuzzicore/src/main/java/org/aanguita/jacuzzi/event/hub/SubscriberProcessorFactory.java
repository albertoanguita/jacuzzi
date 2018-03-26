package org.aanguita.jacuzzi.event.hub;

import java.util.function.Consumer;

/**
 * Created by Alberto on 11/01/2017.
 */
public class SubscriberProcessorFactory {

    static SubscriberProcessor createSubscriberProcessor(
            EventHubFactory.Type type,
            String threadName,
            EventHubSubscriber eventHubSubscriber,
            Consumer<Exception> exceptionConsumer) {
        switch (type) {

            case SYNCHRONOUS:
                return new SynchronousSubscriberProcessor(eventHubSubscriber);

            case ASYNCHRONOUS:
                return new OneThreadSubscriberProcessor(threadName, eventHubSubscriber, exceptionConsumer);

            case ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD:
                return new OnDemandSubscriberProcessor(threadName, eventHubSubscriber, exceptionConsumer);

            case ASYNCHRONOUS_QUEUE_PERMANENT_THREAD:
                return new MessageProcessorSubscriberProcessor(threadName, eventHubSubscriber, exceptionConsumer);

            default:
                throw new IllegalArgumentException("Unrecognized subscriber processor type: " + type);
        }
    }
}
