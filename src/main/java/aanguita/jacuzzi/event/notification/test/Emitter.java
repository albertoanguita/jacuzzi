package aanguita.jacuzzi.event.notification.test;

import aanguita.jacuzzi.event.notification.NotificationEmitter;
import aanguita.jacuzzi.event.notification.NotificationProcessor;
import aanguita.jacuzzi.event.notification.NotificationReceiver;

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
    public String subscribe(String receiverID, NotificationReceiver notificationReceiver) throws IllegalArgumentException {
        return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver);
    }

    @Override
    public String subscribe(String receiverID, NotificationReceiver notificationReceiver, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException {
        return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, millis, timeFactorAtEachEvent, limit);
    }

    @Override
    public void unsubscribe(String receiverID) {
        notificationProcessor.unsubscribeReceiver(receiverID);
    }
}
