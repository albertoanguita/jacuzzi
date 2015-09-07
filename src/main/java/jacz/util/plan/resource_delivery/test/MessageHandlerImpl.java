package jacz.util.plan.resource_delivery.test;

import jacz.util.date_time.TimeElapsed;
import jacz.util.plan.resource_delivery.TargetAndResource;
import jacz.util.queues.event_processing.MessageHandler;
import jacz.util.queues.event_processing.MessageProcessor;

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
