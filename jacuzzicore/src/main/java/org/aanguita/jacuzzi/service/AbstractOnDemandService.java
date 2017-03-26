package org.aanguita.jacuzzi.service;

import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Alberto on 26/03/2017.
 */
public abstract class AbstractOnDemandService<T> extends StringIdClass implements OnDemandService<T> {

    private final Map<String, Function<T, Boolean>> registeredClients;

    protected final Supplier<T> eventSupplier;

    public AbstractOnDemandService(Supplier<T> eventSupplier) {
        this.registeredClients = new HashMap<>();
        this.eventSupplier = eventSupplier;
    }

    @Override
    public synchronized void register(String clientId, Function<T, Boolean> eventCallback) {
        registeredClients.put(clientId, eventCallback);
        if (registeredClients.size() == 1) {
            // first client added
            startService();
        }
    }

    @Override
    public synchronized void unregister(String clientId) {
        registeredClients.remove(clientId);
        if (registeredClients.isEmpty()) {
            stopService();
        }
    }

    public synchronized void event(T event) {
        registeredClients.entrySet().removeIf(client -> client.getValue().apply(event));
    }

    abstract void startService();

    abstract void stopService();
}
