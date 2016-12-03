package org.aanguita.jacuzzi.dependency;

import com.sun.scenario.effect.impl.prism.PrImage;
import org.aanguita.jacuzzi.concurrency.controller.ConcurrencyController;
import org.aanguita.jacuzzi.concurrency.controller.ConcurrencyControllerAction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This interface provides access to the methods of registered dependencies
 */
public class GenericProxy implements Proxy {

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

    Object call(String methodName, Long timeout, Object... params) {
        try {
            Method method = object.getClass().getMethod(methodName);
            return method.invoke(object, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    void call(String methodName, Long timeout, Runnable onSuccess, Runnable onError, Object... params) {

    }
}
