package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.ThreadExecutor;
import org.aanguita.jacuzzi.lists.tuple.Duple;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alberto on 07/10/2016.
 */
class MixedEventHub extends AbstractEventHub {


    MixedEventHub(String name) {
        super(name);
    }

    @Override
    public EventHubFactory.Type getType() {
        return EventHubFactory.Type.MIXED;
    }
}
