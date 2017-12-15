package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.id.AlphaNumFactory;

/**
 * @author aanguita
 *         23/09/2016
 *
 *         todo make special notifications when it is connected to an event hub and when it is disconnected, with default
 *         empty implementations
 */
public interface EventHubSubscriber {

    String getId();

    void event(Publication publication);
}
