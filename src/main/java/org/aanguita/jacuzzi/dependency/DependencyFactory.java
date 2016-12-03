package org.aanguita.jacuzzi.dependency;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alberto on 01/12/2016.
 */
public class DependencyFactory {

    private static final Map<String, Proxy> proxies = new HashMap<>();

    public static synchronized void register(String proxyName, Proxy proxy) {
        if (proxies.containsKey(proxyName) && !(proxies.get(proxyName) instanceof UnregisteredGenericProxy)) {
            throw new IllegalStateException("Tried to register a genericProxy on an already registered genericProxy name: " + proxyName);
        }
        if (proxies.containsKey(proxyName)) {
            // an unregistered genericProxy already occupies this name. Send all accumulated messages and replace
            // todo
        } else {
            proxies.put(proxyName, proxy);
        }
    }

    public static synchronized Proxy getDependency(String proxyName) {
        if (!proxies.containsKey(proxyName)) {
            proxies.put(proxyName, new UnregisteredGenericProxy());
        }
        return proxies.get(proxyName);
    }
}
