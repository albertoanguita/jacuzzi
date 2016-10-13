package org.aanguita.jacuzzi.event.hub;

import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
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

    @Override
    protected void publish(List<Duple<EventHubSubscriber, Boolean>> subscribers, String channel, boolean inBackground, Object... messages) {
        if (inBackground) {
            ThreadExecutor.submit(() -> invokeSubscribers(subscribers, true, channel, messages));
        } else {
            invokeSubscribers(subscribers, false, channel, messages);
        }
    }
}
