package org.aanguita.jacuzzi.event.hub;

/**
 * @author aanguita
 *         23/09/2016
 *
 *         todo make special notifications when it is connected to an event hub and when it is disconnected, with default
 *         empty implementations
 */
public interface EventHubSubscriber {

    void event(Publication publication);
}
