package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 11/01/2017.
 */
public class SubscriberProcessorFactory {

    static SubscriberProcessor createSubscriberProcessor(EventHubFactory.Type type, String subscriberId, EventHubSubscriber eventHubSubscriber) {
        switch (type) {

            case SYNCHRONOUS:
                return new OneThreadSubscriberProcessor(subscriberId, eventHubSubscriber);

            case ASYNCHRONOUS:
                return new OneThreadSubscriberProcessor(subscriberId, eventHubSubscriber);

            case ASYNCHRONOUS_QUEUE_EVENTUAL_THREAD:
                return new OnDemandSubscriberProcessor(eventHubSubscriber);

            case ASYNCHRONOUS_QUEUE_PERMANENT_THREAD:
                return new MessageProcessorSubscriberProcessor(subscriberId, eventHubSubscriber);

            default:
                throw new IllegalArgumentException("Unrecognized subscriber processor type: " + type);
        }
    }
}
