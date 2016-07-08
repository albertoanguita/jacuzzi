package org.aanguita.jacuzzi.id;

import java.io.Serializable;

/**
 * Created by Alberto on 16/03/2016.
 */
public abstract class IdFactory<Id extends Serializable> implements Serializable {

    protected Id id;

    protected IdFactory(Id firstId) {
        id = firstId;
    }

    public synchronized Id getId() {
        Id clonedId = cloneId();
        nextId();
        return clonedId;
    }

    protected abstract Id cloneId();

    protected abstract void nextId();
}
