package jacz.util.event.notification;

import jacz.util.concurrency.timer.SimpleTimerAction;
import jacz.util.concurrency.timer.Timer;
import jacz.util.identifier.UniqueIdentifier;

import java.util.*;

/**
 * This class handles the process of notifying events emitted by an emitter to the subscribed receivers
 */
class NotificationReceiverHandler implements SimpleTimerAction {

    private final NotificationReceiver notificationReceiver;

    private final UniqueIdentifier emitterID;

    /**
     * Delay for emitting notifications (null for no delay)
     */
    private final Long millis;

    private final double timeFactorAtEachEvent;

    private final int limit;

    /**
     * Amount of stored events
     */
    private int eventCount;

    /**
     * Lists of received messages (for non-grouped)
     */
    private List<List<Object>> nonGroupedMessages;

    /**
     * Set of nonGroupedMessages (for grouped).
     */
    private List<Object> groupedMessages;

    /**
     * Timer for emitting notifications (if we want to have a delay)
     */
    private final Timer timer;


    NotificationReceiverHandler(NotificationReceiver notificationReceiver, UniqueIdentifier emitterID, Long millis, double timeFactorAtEachEvent, int limit, String threadName) {
        this.notificationReceiver = notificationReceiver;
        this.emitterID = emitterID;
        this.millis = (millis != null && millis < 1L) ? null : millis;
        if (this.millis != null) {
            timer = new Timer(millis, this, false, threadName + "/" + NotificationProcessor.class.getName());
        } else {
            timer = null;
        }
        this.timeFactorAtEachEvent = (timeFactorAtEachEvent > 1d) ? 1.0d : (timeFactorAtEachEvent < 0d ? 0d : timeFactorAtEachEvent);
        this.limit = (limit < 1) ? 1 : limit;
        eventCount = 0;
        nonGroupedMessages = new ArrayList<>();
        groupedMessages = new ArrayList<>();
    }

    synchronized void newEvent(Object... messages) {
        eventCount++;
        nonGroupedMessages.add(Arrays.asList(messages));
        groupedMessages.addAll(Arrays.asList(messages));
        if (eventCount == limit) {
            notifyReceiver();
            stopTimer();
        } else if (millis != null) {
            setTimer();
        }
    }

    private void setTimer() {
        if (!timer.isRunning()) {
            timer.reset(millis);
        } else {
            timer.reset(timeFactorAtEachEvent);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private synchronized void notifyReceiver() {
        // todo parallelize this to avoid possible deadlocks
        if (eventCount > 0) {
            notificationReceiver.newEvent(emitterID, eventCount, new ArrayList<>(nonGroupedMessages), new ArrayList<>(groupedMessages));
            resetMessages();
            eventCount = 0;
        }
    }

    private void resetMessages() {
        groupedMessages.clear();
        nonGroupedMessages.clear();
    }

    @Override
    public synchronized Long wakeUp(Timer timer) {
        // check in case it has been stopped before
        if (timer.isRunning()) {
            notifyReceiver();
        }
        return 0L;
    }

    public synchronized void stop() {
        if (timer != null) {
            timer.kill();
        }
    }
}
