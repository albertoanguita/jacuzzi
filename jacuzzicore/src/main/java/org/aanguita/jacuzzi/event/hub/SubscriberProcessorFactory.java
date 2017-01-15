package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 11/01/2017.
 */
public class SubscriberProcessorFactory {

    static SubscriberProcessor createSubscriberProcessor(EventHubFactory.SubscriberProcessorType type, String subscriberId, EventHubSubscriber eventHubSubscriber) {
        switch (type) {

            case ONE_THREAD_PER_PUBLICATION:
                return new OneThreadSubscriberProcessor(subscriberId, eventHubSubscriber);

            case ON_DEMAND_QUEUE_PROCESSOR:
                return new OnDemandSubscriberProcessor(eventHubSubscriber);

            case MESSAGE_PROCESSOR:
                return new MessageProcessorSubscriberProcessor(subscriberId, eventHubSubscriber);

            default:
                throw new IllegalArgumentException("Unrecognized subscriber processor type: " + type);
        }
    }
}
