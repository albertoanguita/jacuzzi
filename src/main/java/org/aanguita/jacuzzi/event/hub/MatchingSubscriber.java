package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 16/11/2016.
 */
class MatchingSubscriber {

    private final EventHubSubscriber eventHubSubscriber;

    private final int priority;

    private final boolean inBackground;

    MatchingSubscriber(EventHubSubscriber eventHubSubscriber, int priority, boolean inBackground) {
        this.eventHubSubscriber = eventHubSubscriber;
        this.priority = priority;
        this.inBackground = inBackground;
    }

    EventHubSubscriber getEventHubSubscriber() {
        return eventHubSubscriber;
    }

    int getPriority() {
        return priority;
    }

    boolean isInBackground() {
        return inBackground;
    }
}
