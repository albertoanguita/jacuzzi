package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 16/11/2016.
 */
class MatchingSubscriber {

    private final int priority;

    private final SubscriberProcessor subscriberProcessor;

    MatchingSubscriber(int priority, SubscriberProcessor subscriberProcessor) {
        this.priority = priority;
        this.subscriberProcessor = subscriberProcessor;
    }

    int getPriority() {
        return priority;
    }

    void publish(Publication publication) {
        try {
            subscriberProcessor.publish(publication);
        } catch (Throwable e) {
            // no exceptions should rise here todo error factory
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MatchingSubscriber{" +
                "priority=" + priority +
                ", subscriberProcessor=" + subscriberProcessor +
                '}';
    }
}
