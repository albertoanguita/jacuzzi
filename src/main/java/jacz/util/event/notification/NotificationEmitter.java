package jacz.util.event.notification;

/**
 * Interface for emitting notifications in the notification API. Emitters must implement these methods so the receivers can
 * subscribe to them. The implementation of these methods is fixed (see below). The emitter needs a notification processor
 * previously initialized.
 *
 * @Override public String subscribe(String receiverID, NotificationReceiver notificationReceiver, boolean groupEvents) throws IllegalArgumentException {
 * return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents);
 * }
 * @Override public String subscribe(String receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException {
 * return notificationProcessor.subscribeReceiver(receiverID, notificationReceiver, groupEvents, millis, timeFactorAtEachEvent, limit);
 * }
 * @Override public void unsubscribe(String receiverID) {
 * notificationProcessor.unsubscribeReceiver(receiverID);
 * }
 */
public interface NotificationEmitter {

    String subscribe(String receiverID, NotificationReceiver notificationReceiver) throws IllegalArgumentException;

    String subscribe(String receiverID, NotificationReceiver notificationReceiver, long millis, double timeFactorAtEachEvent, int limit) throws IllegalArgumentException;

    void unsubscribe(String receiverID);
}
