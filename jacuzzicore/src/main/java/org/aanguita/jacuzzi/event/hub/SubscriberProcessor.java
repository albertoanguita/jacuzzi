package org.aanguita.jacuzzi.event.hub;

/**
 * Created by Alberto on 10/01/2017.
 */
interface SubscriberProcessor {

    void publish(Publication publication);

    void close();
}
