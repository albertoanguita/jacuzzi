package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.id.AlphaNumFactory;

public abstract class EventHubSubscriberRandomId implements EventHubSubscriber {

    private final String id = AlphaNumFactory.getStaticId();

    @Override
    public String getId() {
        return id;
    }
}
