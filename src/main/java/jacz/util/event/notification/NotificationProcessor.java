package jacz.util.event.notification;

import jacz.util.concurrency.ThreadUtil;
import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An API for handling event notifications. Classes can emit events, and other classes can subscribe to the emitted so they receive
 * the events.
 */
public class NotificationProcessor {

    /**
     * ID of the emitter (if receiver is subscribed to several emitters, this will help him identifying them). It is
     * given to the subscribers each time they subscribe (they can, if they want to, ignore it)
     */
    private UniqueIdentifier emitterID;

    /**
     * List of subscribed receivers (they must receive any notification of the associated emitter)
     */
    private Map<UniqueIdentifier, NotificationReceiverHandler> subscribedReceivers;

    public NotificationProcessor() {
        this(UniqueIdentifierFactory.getOneStaticIdentifier());
    }

    public NotificationProcessor(UniqueIdentifier emitterID) {
        this.emitterID = emitterID;
        subscribedReceivers = new HashMap<UniqueIdentifier, NotificationReceiverHandler>();
    }

    /**
     * Subscribes a new object for receiving event notifications
     *
     * @param notificationReceiver object that will receive event notifications
     * @return the ID of this emitter
     */
    public synchronized UniqueIdentifier subscribeReceiver(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents) {
        return subscribeReceiver(receiverID, notificationReceiver, groupEvents, ThreadUtil.invokerName(1));
    }

    public synchronized UniqueIdentifier subscribeReceiver(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, String threadName) {
        subscribedReceivers.put(receiverID, new NotificationReceiverHandler(notificationReceiver, emitterID, groupEvents, null, 0, 1, threadName));
        return emitterID;
    }

    /**
     * Subscribes a new object for receiving event notifications. Time and event count limits are provided, mea
     *
     * @param notificationReceiver object that will receive event notifications
     * @return the ID of this emitter
     */
    public synchronized UniqueIdentifier subscribeReceiver(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, long millis, double timeFactorAtEachEvent, int limit) {
        return subscribeReceiver(receiverID, notificationReceiver, groupEvents, millis, timeFactorAtEachEvent, limit, ThreadUtil.invokerName(1));
    }

    public synchronized UniqueIdentifier subscribeReceiver(UniqueIdentifier receiverID, NotificationReceiver notificationReceiver, boolean groupEvents, long millis, double timeFactorAtEachEvent, int limit, String threadName) {
        Long checkedMillis = checkTime(millis);
        limit = checkLimit(limit);
        subscribedReceivers.put(receiverID, new NotificationReceiverHandler(notificationReceiver, emitterID, groupEvents, checkedMillis, timeFactorAtEachEvent, limit, threadName));
        return emitterID;
    }

    public synchronized void unsubscribeReceiver(UniqueIdentifier receiverID) {
        subscribedReceivers.remove(receiverID).stop();
    }

    private Long checkTime(long millis) {
        return millis < 1 ? null : millis;
    }

    private int checkLimit(int limit) {
        return limit < 1 ? 1 : limit;
    }

    public synchronized void newEvent(Object... messages) {
        for (NotificationReceiverHandler notificationReceiverHandler : subscribedReceivers.values()) {
            notificationReceiverHandler.newEvent(messages);
        }
    }
}
