package org.aanguita.jacuzzi.service.ondemand;

import org.aanguita.jacuzzi.concurrency.SimpleSemaphore;
import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.id.StringIdClass;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Alberto on 26/03/2017.
 */
public abstract class AbstractOnDemandService<T> extends StringIdClass implements OnDemandService<T> {

    private static class RegisteredClient<T> {

        private final Function<T, Boolean> client;

        private final SimpleSemaphore semaphore;

        private RegisteredClient(Function<T, Boolean> client) {
            this.client = client;
            semaphore = new SimpleSemaphore();
            semaphore.pause();
        }

        private void release() {
            semaphore.resume();
        }
    }

    private final Map<String, RegisteredClient<T>> registeredClients;

    protected final Supplier<T> eventSupplier;

    public AbstractOnDemandService(Supplier<T> eventSupplier) {
        this.registeredClients = new HashMap<>();
        this.eventSupplier = eventSupplier;
    }

    @Override
    public synchronized String register(Function<T, Boolean> eventCallback) {
        String clientId = AlphaNumFactory.getStaticId();
        register(clientId, eventCallback);
        return clientId;
    }

    @Override
    public synchronized void register(String clientId, Function<T, Boolean> eventCallback) {
        registeredClients.put(clientId, new RegisteredClient<T>(eventCallback));
        if (registeredClients.size() == 1) {
            // first client added
            startService();
        }
    }

    @Override
    public synchronized void unregister(String clientId) {
        registeredClients.remove(clientId).release();
        checkEmpty();
    }

    @Override
    public void blockUntilClientIsUnregistered(String clientId) {
        SimpleSemaphore semaphore = getSemaphore(clientId);
        if (semaphore != null) {
            semaphore.access();
        }
    }

    @Override
    public void blockUntilClientIsUnregistered(String clientId, long timeout) throws TimeoutException {
        SimpleSemaphore semaphore = getSemaphore(clientId);
        if (semaphore != null) {
            semaphore.access(timeout);
        }
    }

    private synchronized SimpleSemaphore getSemaphore(String clientId) {
        synchronized (this) {
            return registeredClients.containsKey(clientId) ? registeredClients.get(clientId).semaphore : null;
        }
    }

    public synchronized void event(T event) {
        registeredClients.entrySet().removeIf(client -> {
            if (client.getValue().client.apply(event)) {
                client.getValue().release();
                return true;
            } else {
                return false;
            }
        });
        checkEmpty();
    }

    private void checkEmpty() {
        if (registeredClients.isEmpty()) {
            stopService();
        }
    }

    abstract void startService();

    abstract void stopService();
}
