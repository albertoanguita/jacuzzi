package org.aanguita.jacuzzi.plan.resource_delivery.test;

import org.aanguita.jacuzzi.date_time.TimeElapsed;
import org.aanguita.jacuzzi.plan.resource_delivery.TargetAndResource;
import org.aanguita.jacuzzi.queues.event_processing.MessageHandler;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-jun-2010<br>
 * Last Modified: 09-jun-2010
 */
public class MessageHandlerImpl implements MessageHandler {

    private TimeElapsed timeElapsed;

    public MessageHandlerImpl(TimeElapsed timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    @Override
    public void handleMessage(Object message) {
        TargetAndResource<MyTarget, ResourceImpl> tr = (TargetAndResource<MyTarget, ResourceImpl>) message;
        System.out.println(" *" + timeElapsed.measureTime() + " " + tr.getTarget() + ": " + tr.getResource());
    }

    @Override
    public void finalizeHandler() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
