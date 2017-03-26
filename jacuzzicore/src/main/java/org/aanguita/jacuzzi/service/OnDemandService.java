package org.aanguita.jacuzzi.service;

import java.util.function.Function;

/**
 * This interface defines a service to which clients can attach to receive data periodically. The service will
 * auto-start when the first client registers, and will auto-stop when no clients are left.
 *
 * The service periodically feeds registered clients with a generic type, obtained from some event or some function
 * invoked periodically.
 */
public interface OnDemandService<T> {

    void register(Function<T, Boolean> eventCallback);

    void register(String clientId, Function<T, Boolean> eventCallback);

    void unregister(String clientId);


}
