package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
abstract class QueuedEventHub extends AbstractEventHub {

    protected static class QueuedPublication {

        protected final Publication publication;

        protected final List<MatchingSubscriber> matchingSubscribers;

        protected QueuedPublication(Publication publication, List<MatchingSubscriber> matchingSubscribers) {
            this.publication = publication;
            this.matchingSubscribers = matchingSubscribers;
        }
    }

    QueuedEventHub(String name) {
        super(name);
    }
}
