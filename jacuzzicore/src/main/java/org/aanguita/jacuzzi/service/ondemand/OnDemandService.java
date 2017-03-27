package org.aanguita.jacuzzi.service.ondemand;

import java.util.function.Function;

/**
 * This interface defines a service to which clients can attach to receive data periodically. The service will
 * auto-start when the first client registers, and will auto-stop when no clients are left.
 * <p>
 * The service periodically feeds registered clients with a generic type, obtained from some event or some function
 * invoked periodically.
 */
public interface OnDemandService<T> {

    /**
     * Registers an anonymous client. It will only be possible to unregister the client through a true-response of the event callback.
     *
     * @param eventCallback the service will invoke this callback for new generated events. The callback return must be:
     *                      <li>
     *                      <ul>false if the client expects more events, and thus must stay registered after this invocation</ul>
     *                      <ul>true if the client does not require more events, and thus must be unregistered after this invocation</ul>
     *                      </li>
     *                      A null return is not permitted, and will leave the service in an undesired state
     */
    void register(Function<T, Boolean> eventCallback);

    /**
     * Registers a new client in the service
     *
     * @param clientId      identifier of the client
     * @param eventCallback the service will invoke this callback for new generated events. The callback return must be:
     *                      <li>
     *                      <ul>false if the client expects more events, and thus must stay registered after this invocation</ul>
     *                      <ul>true if the client does not require more events, and thus must be unregistered after this invocation</ul>
     *                      </li>
     *                      A null return is not permitted, and will leave the service in an undesired state
     */
    void register(String clientId, Function<T, Boolean> eventCallback);

    /**
     * Unregisters a previously registered client
     *
     * @param clientId identifier of the client
     */
    void unregister(String clientId);
}
