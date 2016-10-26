package org.aanguita.jacuzzi.event.hub;

/**
 * @author aanguita
 *         23/09/2016
 */
public interface EventHubSubscriber {

    void event(String channel, Object... messages);
}
