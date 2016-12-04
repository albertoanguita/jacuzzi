package org.aanguita.jacuzzi.dependency;

import com.sun.scenario.effect.impl.prism.PrImage;
import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.concurrency.controller.ConcurrencyController;
import org.aanguita.jacuzzi.concurrency.controller.ConcurrencyControllerAction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * This interface provides access to the methods of registered dependencies
 */
public class GenericProxy implements Proxy {

    private static class OnSynchSuccess implements Consumer<Object> {

        @Override
        public void accept(Object o) {

        }
    }

    private final String proxyName;

    private final Object object;

    private final ConcurrencyController concurrencyController;

    public GenericProxy(String proxyName, Object object) {
        this(proxyName, object, null);
    }

    public GenericProxy(String proxyName, Object object, ConcurrencyControllerAction concurrencyControllerAction) {
        this.proxyName = proxyName;
        this.object = object;
        this.concurrencyController = concurrencyControllerAction != null ? new ConcurrencyController(concurrencyControllerAction) : null;
    }

    @Override
    public String getName() {
        return proxyName;
    }

    Object call(String methodName, Long timeout, Object... params) throws TimeoutException {
        return null;
    }

    void call(String methodName, Long timeout, Consumer<Object> onSuccess, Consumer<Exception> onError, Object... params) throws TimeoutException {
        try {
            if (concurrencyController.beginActivity(methodName, timeout)) {
                Method method = object.getClass().getMethod(methodName);
                Object result = method.invoke(object, params);
                ThreadExecutor.submit(() -> onSuccess.accept(result));
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            ThreadExecutor.submit(() -> onError.accept(e));
        } finally {
            concurrencyController.endActivity(methodName);
        }
    }
}
