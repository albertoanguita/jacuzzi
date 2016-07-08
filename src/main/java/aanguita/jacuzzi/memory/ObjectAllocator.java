package aanguita.jacuzzi.memory;

import java.util.*;

/**
 * A generic object pool, for facilitating the reusing of objects so the garbage collector does not have so much work
 * <p/>
 * The ObjectAllocator must be used with objects that are never equal to each other. Otherwise, they will be merged when freed
 *
 * todo remove, its benefit is not clear
 */
public class ObjectAllocator {

    /**
     * This attributes stores free objects, which can be acquired. They are already built and occupying space in memory, but no one
     * else is using them. They are classified by their class
     */
    private final Map<Class<?>, Set<Object>> objectPool;

    public ObjectAllocator() {
        objectPool = new HashMap<>();
    }

    /**
     * Request a new free object from the object pool
     *
     * @param class_ class of the requested object
     * @param <T>    the requested object (if no free objects are available, it is build with the default constructor, so the class must have
     *               that default constructor)
     * @return an object of the requested class. The object might need to be re-initialized, since its attributes are in an undetermined state
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public synchronized <T> T request(Class<T> class_) throws IllegalAccessException, InstantiationException {
        if (!objectPool.containsKey(class_)) {
            objectPool.put(class_, new HashSet<>());
        }
        Set<?> objectSet = objectPool.get(class_);
        if (objectSet.isEmpty()) {
            return class_.newInstance();
        } else {
            Iterator<?> it = objectSet.iterator();
            Object o = it.next();
            it.remove();
            return (T) o;
        }
    }

    /**
     * Indicates that this object is no longer to be used, so it can be reused in other parts of the code
     *
     * @param o the object to free
     */
    public synchronized void free(Object o) {
        if (!objectPool.containsKey(o.getClass())) {
            objectPool.put(o.getClass(), new HashSet<>());
        }
        objectPool.get(o.getClass()).add(o);
    }

}
