package jacz.util.event.notification;

import jacz.util.identifier.UniqueIdentifier;

/**
 * Interface for emitting notifications in the notification API. Emitters must implement these methods so the receivers can
 * subscribe to them. The implementation of these methods is fixed (see below). The emitter needs a notification processor
 * previously initialized.
 *
 * @Override public UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents) throws IllegalArgumentException {
 * return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents);
 * }
 * @Override public UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException {
 * return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents, millis, timeFactorAtEachEvent, limit);
 * }
 * @Override public void unsubscribe(UniqueIdentifier receiverID) {
 * notificationProcessor.unsubscribeReceiver(receiverID);
 * }
 */
public interface NotificationEmitter {

    UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver) throws IllegalArgumentException;

    UniqueIdentifier subscribe(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException;

    void unsubscribe(UniqueIdentifier receiverID);
}
