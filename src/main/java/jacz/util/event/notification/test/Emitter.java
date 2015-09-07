package jacz.util.event.notification.test;

import jacz.util.event.notification.*;
import jacz.util.identifier.UniqueIdentifier;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2010<br>
 * Last Modified: 28-abr-2010
 */
public class Emitter implements NotificationEmitter {

    private NotificationProcessor notificationProcessor;

    private int i;

    public Emitter() {
        notificationProcessor = new NotificationProcessor();
        i = 0;
    }


    public void add() {
        System.out.println("new event...");
        i++;
        notificationProcessor.newEvent(i / 2);
    }

    @Override
    public UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents) throws IllegalArgumentException {
        return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents);
    }

    @Override
    public UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException {
        return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents, millis, timeFactorAtEachEvent, limit);
    }

    @Override
    public void unsubscribe(UniqueIdentifier receiverID) {
        notificationProcessor.unsubscribeReceiver(receiverID);
    }
}
